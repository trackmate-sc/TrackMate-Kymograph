/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 The Institut Pasteur.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.scijava.util.DoubleArray;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.kymograph.KymographProjectionMethod.Accumulator;
import fiji.plugin.trackmate.util.TMUtils;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import net.imagej.ImgPlus;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class KymographCreator implements OutputAlgorithm< ImagePlus >
{

	private static final String BASE_ERROR_MESSAGE = "[KymographCreator] ";

	private final Model model;

	private final ImagePlus imp;

	private KymographCreationParams params;

	private ImagePlus output;

	private String errorMessage;

	public KymographCreator( final Model model, final ImagePlus imp, final KymographCreationParams params )
	{
		this.model = model;
		this.imp = imp;
		this.params = params;
	}

	@Override
	public boolean checkInput()
	{
		final boolean containsTrackID1 = model.getTrackModel().trackIDs( true ).contains( params.trackID1 );
		if (!containsTrackID1)
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
		return true;
	}

	public void setParams( final KymographCreationParams params )
	{
		this.params = params;
	}

	@Override
	public boolean process()
	{
		/*
		 * Collect all the intensities.
		 */

		final int[] minmax = getMinMaxTimePoints( params.trackID1, params.trackID1 );
		final List< double[][] > lines = new ArrayList<>( minmax[ 1 ] - minmax[ 0 ] + 1 );
		for ( int tp = minmax[ 0 ]; tp <= minmax[ 1 ]; tp++ )
		{
			final double[][] intensities = collectIntensities( tp );
			lines.add( intensities );
		}

		/*
		 * Determine max width.
		 */

		final int width = lines.stream()
				.filter( Objects::nonNull )
				.mapToInt( l -> l[ 0 ].length )
				.max().getAsInt();

		/*
		 * Prepare output.
		 */
		
		final String outputName = String.format( "%s_Kymograph_%s-%s",
				imp.getShortTitle(),
				params.trackID1.toString(),
				params.trackID2.toString() );
		final int height = minmax[ 1 ] - minmax[ 0 ] + 1;
		final int nChannels = imp.getNChannels();
		final int nZSlices = 1 ;
		final int nFrames = 1;
		output = IJ.createHyperStack( outputName, width, height, nChannels, nZSlices, nFrames, imp.getBitDepth() );
		output.getCalibration().pixelWidth = imp.getCalibration().pixelWidth;
		output.getCalibration().setXUnit( imp.getCalibration().getUnit() );
		output.getCalibration().pixelHeight = imp.getCalibration().frameInterval;
		output.getCalibration().setYUnit( imp.getCalibration().getTimeUnit() );

		if ( output instanceof CompositeImage )
		{
			final LUT[] luts = imp.getLuts();
			( ( CompositeImage ) output ).setLuts( luts );
		}

		/*
		 * Write into output image.
		 */

		writeInto( output, lines );

		return true;
	}

	private < T extends RealType< T > & NativeType< T > > void writeInto( final ImagePlus target, final List< double[][] > lines )
	{
		final Img< T > outimg = ImageJFunctions.wrap( output );
		final int width = ( int ) outimg.dimension( 0 );
		final int height = ( int ) outimg.dimension( 1 );
		final int nChannels = ( int ) outimg.dimension( 2 );

		final RandomAccess< T > ra = outimg.randomAccess( outimg );
		for ( int y = 0; y < height; y++ )
		{
			final double[][] intensities = lines.get( y );
			if ( intensities == null )
				continue;

			ra.setPosition( y, 1 );
			for ( int c = 0; c < nChannels; c++ )
			{
				ra.setPosition( c, 2 );
				final double[] line = intensities[ c ];
				final int offset = params.alignment.offset( line.length, width );
				for ( int x = 0; x < line.length; x++ )
				{
					ra.setPosition( x + offset, 0 );
					ra.get().setReal( line[ x ] );
				}
			}
		}
	}

	/**
	 * Returns <code>null</code> if one of the two tracks does not have a spot
	 * in the specified time-point. Otherwise returns the intensity between the
	 * two tracks at this time-point, as an array of <code>double</code> arrays,
	 * with one element per channel.
	 * 
	 * @param tp
	 *            the time-point.
	 * @return a new <code>double[][]</code> array.
	 */
	private double[][] collectIntensities( final int tp )
	{
		final long[] coords1 = getCoords( tp, params.trackID1 );
		final long[] coords2 = getCoords( tp, params.trackID2 );
		if ( coords1 == null || coords2 == null )
			return null;

		final int nChannels = imp.getNChannels();
		final double[][] intensities = new double[ nChannels ][];
		for ( int c = 0; c < nChannels; c++ )
			intensities[ c ] = getProjectedIntensity( coords1, coords2, c, tp );

		return intensities;
	}

	private < T extends RealType< T > & NativeType< T > > double[] getProjectedIntensity( final long[] from, final long[] to, final int channel, final int timepoint )
	{
		@SuppressWarnings( "unchecked" )
		final ImgPlus< T > img = TMUtils.rawWraps( imp );
		final ImgPlus< T > current = TMUtils.hyperSlice( img, channel, timepoint );

		// Projection accumulator.
		final Accumulator accumulator = params.projectionMethod.accumulator();
		final double[] intensity = getIntensity( from, to, current );
		accumulator.accumulate( intensity );

		// Shift and accumulate intensities.
		final int span = params.thickness / 2;
		for ( int u = 1; u < span; u++ )
		{
			accumulator.accumulate( shiftAndGetIntensity( u, from, to, current ) );
			accumulator.accumulate( shiftAndGetIntensity( -u, from, to, current ) );
		}
		return accumulator.get();
	}

	private < T extends RealType< T > & NativeType< T > > double[] shiftAndGetIntensity( final double shift, final long[] from, final long[] to, final ImgPlus< T > current )
	{
		// Orthogonal vector in XY plane (even if we have 3D data).
		final double dx = to[ 0 ] - from[ 0 ];
		final double dy = to[ 1 ] - from[ 1 ];
		final double l = Math.sqrt( dx * dx + dy * dy );
		final double ovx = -dy / l;
		final double ovy = dx / l;

		final long[] tmpFrom = Arrays.copyOf( from, from.length );
		tmpFrom[ 0 ] = Math.round( shift * ovx + tmpFrom[ 0 ] );
		tmpFrom[ 1 ] = Math.round( shift * ovy + tmpFrom[ 1 ] );
		final long[] tmpTo = Arrays.copyOf( to, to.length );
		tmpTo[ 0 ] = Math.round( shift * ovx + tmpTo[ 0 ] );
		tmpTo[ 1 ] = Math.round( shift * ovy + tmpTo[ 1 ] );
		return getIntensity( tmpFrom, tmpTo, current );
	}

	private < T extends RealType< T > & NativeType< T > > double[] getIntensity( final long[] from, final long[] to, final ImgPlus< T > current )
	{
		final BresenhamLine< T > line = new BresenhamLine<>( current, Point.wrap( from ), Point.wrap( to ) );
		final DoubleArray arr = new DoubleArray();
		while ( line.hasNext() )
			arr.addValue( line.next().getRealDouble() );

		return arr.copyArray();
	}

	/**
	 * Returns <code>null</code> if the specified track does not have a spot for
	 * the specified time-point. Otherwise, returns the pixel coordinate of the
	 * spot.
	 * 
	 * @param tp
	 *            the time-point (0 based).
	 * @param trackID
	 *            the track ID.
	 * @return a new <code>int[]</code> array with 3 elements (x, y, z).
	 */
	public long[] getCoords( final int tp, final Integer trackID )
	{
		final Set< Spot > spots = model.getTrackModel().trackSpots( trackID );
		final Optional< Spot > opt = spots.stream().filter( s -> s.getFeature( Spot.FRAME ).intValue() == tp ).findFirst();
		if ( !opt.isPresent() )
			return null;

		final Spot spot = opt.get();

		final double[] calibration = TMUtils.getSpatialCalibration( imp );
		final long[] coords = new long[ imp.getNSlices() > 1 ? 3 : 2 ];
		for ( int d = 0; d < coords.length; d++ )
			coords[ d ] = Math.round( spot.getDoublePosition( d ) / calibration[ d ] );

		return coords;
	}

	private int[] getMinMaxTimePoints( final Integer trackID1, final Integer trackID2 )
	{
		final Set< Spot > spots1 = model.getTrackModel().trackSpots( trackID1 );
		final int min1 = spots1.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).min().getAsInt();
		final int max1 = spots1.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).max().getAsInt();

		final Set< Spot > spots2 = model.getTrackModel().trackSpots( trackID1 );
		final int min2 = spots2.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).min().getAsInt();
		final int max2 = spots2.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).max().getAsInt();

		return new int[] { Math.max( min1, min2 ), Math.min( max1, max2 ) };
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
