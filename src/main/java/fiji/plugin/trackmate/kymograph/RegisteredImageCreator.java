/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.kymograph;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.kymograph.ui.KymographUtils;
import fiji.plugin.trackmate.util.TMUtils;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class RegisteredImageCreator implements OutputAlgorithm< ImagePlus >
{

	private static final String BASE_ERROR_MESSAGE = "[RegisteredImageCreator] ";

	private final Model model;

	private final ImagePlus imp;

	private KymographCreationParams params;

	private ImagePlus output;

	private String errorMessage;

	public RegisteredImageCreator( final Model model, final ImagePlus imp, final KymographCreationParams params )
	{
		this.model = model;
		this.imp = imp;
		this.params = params;
	}

	@Override
	public boolean checkInput()
	{
		final boolean containsTrackID1 = model.getTrackModel().trackIDs( true ).contains( params.trackID1 );
		if ( !containsTrackID1 )
		{
			errorMessage = BASE_ERROR_MESSAGE + "Model does not contain a track with ID " + params.trackID1;
			return false;
		}
		final boolean containsTrackID2 = model.getTrackModel().trackIDs( true ).contains( params.trackID2 );
		if ( !containsTrackID2 )
		{
			errorMessage = BASE_ERROR_MESSAGE + "Model does not contain a track with ID " + params.trackID2;
			return false;
		}
		if ( imp.getNSlices() > 1 )
		{
			errorMessage = BASE_ERROR_MESSAGE + "Can only register a 2D image, got a 3D image.";
			return false;
		}
		return true;
	}

	public void setParams( final KymographCreationParams params )
	{
		this.params = params;
	}

	@Override
	public boolean process()
	{
		// Timepoints to process.
		final int[] minmax = KymographUtils.getMinMaxTimePoints( model, params.trackID1, params.trackID1 );
		final int nFrames = minmax[ 1 ] - minmax[ 0 ] + 1;

		// Determine max width.
		final double maxWidth = getMaxWidth( params.trackID1, params.trackID1 );
		// Pad with the thickness on both sides.
		final int width = ( int ) ( maxWidth + 2 * params.thickness );

		// The height.
		final int height = params.thickness;

		// Prepare output.
		final String outputName = String.format( "%s_Registered_%s-%s",
				imp.getShortTitle(),
				params.trackID1.toString(),
				params.trackID2.toString() );
		final int nChannels = imp.getNChannels();
		final int nZSlices = 1;
		output = IJ.createHyperStack( outputName, width, height, nChannels, nZSlices, nFrames, imp.getBitDepth() );
		output.getCalibration().pixelWidth = imp.getCalibration().pixelWidth;
		output.getCalibration().pixelHeight = imp.getCalibration().pixelHeight;
		output.getCalibration().setUnit( imp.getCalibration().getUnit() );
		output.getCalibration().frameInterval = imp.getCalibration().frameInterval;
		output.getCalibration().setTimeUnit( imp.getCalibration().getTimeUnit() );

		if ( output instanceof CompositeImage )
		{
			final LUT[] luts = imp.getLuts();
			( ( CompositeImage ) output ).setLuts( luts );
			( ( CompositeImage ) output ).setDisplayMode( CompositeImage.COMPOSITE );
		}

		// Write into output image.
		writeInto( output );
		return true;
	}

	private < T extends RealType< T > & NativeType< T > > void writeInto( final ImagePlus target )
	{
		@SuppressWarnings( "unchecked" )
		final ImgPlus< T > outimg = TMUtils.rawWraps( target );
		final int nChannels = ( int ) outimg.dimension( outimg.dimensionIndex( Axes.CHANNEL ) );
		final long height = outimg.dimension( outimg.dimensionIndex( Axes.Y ) );

		// Timepoints to process.
		final int[] minmax = KymographUtils.getMinMaxTimePoints( model, params.trackID1, params.trackID1 );
		final int nFrames = minmax[ 1 ] - minmax[ 0 ] + 1;

		for ( int i = 0; i < nFrames; i++ )
		{
			final int tp = i + minmax[ 0 ];
			final long[] coords1 = KymographUtils.getCoords( model, imp, tp, params.trackID1 );
			final long[] coords2 = KymographUtils.getCoords( model, imp, tp, params.trackID2 );
			if ( coords1 == null || coords2 == null )
				continue;

			final double l = getDistance( coords1, coords2 );

			for ( int c = 0; c < nChannels; c++ )
			{
				final ImgPlus< T > slice = TMUtils.hyperSlice( outimg, c, i );
				final RandomAccessible< T > crop = crop( coords1, coords2, c, tp );

				final int xoffset = params.alignment.offset( ( int ) l, ( int ) outimg.dimension( 0 ) );
				final Cursor< T > cursor = slice.localizingCursor();
				final RandomAccess< T > ra = crop.randomAccess();
				while ( cursor.hasNext() )
				{
					cursor.fwd();
					ra.setPosition( cursor );
					ra.move( -xoffset, 0 );
					ra.move( -height / 2, 1 );
					cursor.get().set( ra.get() );
				}
			}
		}
	}

	private < T extends RealType< T > & NativeType< T > > RandomAccessible< T > crop( final long[] from, final long[] to, final int channel, final int timepoint )
	{
		@SuppressWarnings( "unchecked" )
		final ImgPlus< T > img = TMUtils.rawWraps( imp );
		final ImgPlus< T > current = TMUtils.hyperSlice( img, channel, timepoint );

		// Compute transform.
		final AffineTransform2D transform = new AffineTransform2D();
		transform.translate( -from[ 0 ], -from[ 1 ] );
		final double dy = to[ 1 ] - from[ 1 ];
		final double dx = to[ 0 ] - from[ 0 ];
		final double alpha = Math.atan2( dy, dx );
		transform.rotate( -alpha );

		// Transform.
		final RealRandomAccessible< T > source = Views.interpolate( Views.extendZero( current ),
				new NLinearInterpolatorFactory<>() );
		return RealViews.affine( source, transform );
	}

	private double getMaxWidth( final Integer trackID1, final Integer trackID2 )
	{
		final int[] mm = KymographUtils.getMinMaxTimePoints( model, trackID1, trackID2 );
		final int min = mm[ 0 ];
		final int max = mm[ 1 ];
		double maxLength = Double.NEGATIVE_INFINITY;
		for ( int tp = min; tp <= max; tp++ )
		{
			final long[] coords1 = KymographUtils.getCoords( model, imp, tp, params.trackID1 );
			final long[] coords2 = KymographUtils.getCoords( model, imp, tp, params.trackID2 );
			if ( coords1 == null || coords2 == null )
				continue;

			final double l = getDistance( coords1, coords2 );
			if ( l > maxLength )
				maxLength = l;
		}
		return maxLength;
	}

	public static final double getDistance( final long[] coords1, final long[] coords2 )
	{
		final long dx = coords2[ 0 ] - coords1[ 0 ];
		final long dy = coords2[ 1 ] - coords1[ 1 ];
		final double l = Math.sqrt( dx * dx + dy * dy );
		return l;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public ImagePlus getResult()
	{
		return output;
	}
}
