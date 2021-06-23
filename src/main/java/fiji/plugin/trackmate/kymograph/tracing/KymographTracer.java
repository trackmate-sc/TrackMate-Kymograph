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
package fiji.plugin.trackmate.kymograph.tracing;

import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.DOWN;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.LEFT;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.LEFT_DOWN;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.RIGHT;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.RIGHT_DOWN;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import fiji.plugin.trackmate.kymograph.tracing.astar.AStar2D;
import fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections;
import fiji.plugin.trackmate.kymograph.tracing.astar.Path;
import fiji.plugin.trackmate.kymograph.tracing.filter.Tubeness;
import fiji.plugin.trackmate.util.TMUtils;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

public class KymographTracer
{

	private static final String PREVIEW_ROI_NAME = "KT_preview_segment";

	private final ImagePlus imp;

	private final TracingParameters tracingParameters;

	private boolean isTracing = false;

	private AStar2D< DoubleType > astar;

	private Point start;

	private final AtomicInteger pathID = new AtomicInteger( 0 );

	private final AtomicInteger segmentID = new AtomicInteger( 0 );

	private int previousChannel = -1;

	private int previousZ = -1;

	private int previousFrame = -1;

	private double previousSigma = -1.;

	public KymographTracer( final ImagePlus imp, final TracingParameters tracingParameters )
	{
		this.imp = imp;
		this.tracingParameters = tracingParameters;
		if ( null == imp.getOverlay() )
			imp.setOverlay( new Overlay() );
	}

	public boolean isTracing()
	{
		return isTracing;
	}

	public void startPath( final int x, final int y, final int channel, final int z, final int frame )
	{
		pathID.incrementAndGet();
		isTracing = true;

		/*
		 * Recreate the astar and slice if we have moved to another slice or
		 * changed the sigma value.
		 */
		if ( channel != previousChannel
				|| z != previousZ
				|| frame != previousFrame
				|| tracingParameters.getSigma() != previousSigma )
		{
			@SuppressWarnings( "rawtypes" )
			final ImgPlus img = TMUtils.rawWraps( imp );
			@SuppressWarnings( { "unchecked" } )
			final Img< DoubleType > filtered = filterSlice( img, channel, z, frame, tracingParameters.getSigma() );

			astar = new AStar2D<>( filtered, filtered );
			// Forbid moving back in time.
			astar.setDirections( AStarDirections.create()
					.add( LEFT )
					.add( LEFT_DOWN )
					.add( DOWN )
					.add( RIGHT_DOWN )
					.add( RIGHT )
					.get() );

			previousChannel = channel;
			previousZ = z;
			previousFrame = frame;
			previousSigma = tracingParameters.getSigma();
		}
		astar.setThreshold( tracingParameters.getThreshold() );
		astar.setIntensityPenalty( tracingParameters.getPenalty() );
		start = Point.wrap( new long[] { x, y } );
	}

	public Path addSegment( final int x, final int y )
	{
		if ( astar == null )
			return null;

		final Path path = getPathTo( x, y );
		if ( null == path )
			return null;

		segmentID.incrementAndGet();
		start = Point.wrap( new long[] { x, y } );
		return path;
	}

	public void previewSegment( final int x, final int y )
	{
		if ( astar == null )
			return;

		clearPreview();
		final Path path = getPathTo( x, y );
		if ( null == path || path.isEmpty() )
			return;

		final PolygonRoi roi = toRoi( path );
		roi.setStrokeColor( Color.CYAN );
		imp.getOverlay().add( roi, PREVIEW_ROI_NAME );
	}

	public void finishPath()
	{
		isTracing = false;
		clearPreview();
	}

	public void clearPreview()
	{
		final Overlay overlay = imp.getOverlay();
		overlay.remove( PREVIEW_ROI_NAME );
	}

	public ImagePlus getImp()
	{
		return imp;
	}

	private Path getPathTo( final int x, final int y )
	{
		if ( astar == null )
			return null;

		final Point target = Point.wrap( new long[] { x, y } );
		final Path path = astar.search( start, target );
		return path;
	}

	static final ij.gui.PolygonRoi toRoi( final Path path )
	{
		final int[] xpoints = new int[ path.size() ];
		final int[] ypoints = new int[ path.size() ];
		int i = 0;
		for ( final Localizable point : path )
		{
			xpoints[ i ] = point.getIntPosition( 0 );
			ypoints[ i ] = point.getIntPosition( 1 );
			i++;
		}
		final PolygonRoi roi = new PolygonRoi( xpoints, ypoints, xpoints.length, ij.gui.PolygonRoi.POLYLINE );
		roi.setUnscalableStrokeWidth( 1.5 );
		return roi;
	}

	public static < T extends RealType< T > > Img< DoubleType > filterSlice(
			final ImgPlus< T > img,
			final long channel,
			final long z,
			final long frame,
			final double sigma )
	{
		/*
		 * Reslice to the right channel, z and frame.
		 */

		final int timeDim = img.dimensionIndex( Axes.TIME );
		final ImgPlus< T > imgT = timeDim < 0
				? img
				: ImgPlusViews.hyperSlice( img, timeDim, frame );

		final int channelDim = imgT.dimensionIndex( Axes.CHANNEL );
		final ImgPlus< T > imgTC = channelDim < 0
				? imgT
				: ImgPlusViews.hyperSlice( imgT, channelDim, channel );

		// Squeeze Z dimension if its size is 1.
		final int zDim = imgTC.dimensionIndex( Axes.Z );
		final ImgPlus< T > imgTCZ;
		if ( zDim >= 0 && imgTC.dimension( zDim ) <= 1 ) // 1-slice but z-dim
			imgTCZ = ImgPlusViews.hyperSlice( imgTC, zDim, imgTC.min( zDim ) );
		else if ( zDim >= 0 && imgTC.dimension( zDim ) > 1 ) // multi z slices
			imgTCZ = ImgPlusViews.hyperSlice( imgTC, zDim, z );
		else
			imgTCZ = imgTC;

		/*
		 * Copy and change the 0 value. In the case if kymograph, we might have
		 * 0 values that are not from the actual data. This messes up with the
		 * AStar algo, so replace the 0 values by the min non-zero value.
		 */

		final ImgPlus< T > copy = imgTCZ.copy();
		double min = Double.POSITIVE_INFINITY;
		for ( final T p : copy )
		{
			final double val = p.getRealDouble();
			if ( val > 0 )
			{
				if ( val < min )
					min = val;
			}
		}

		for ( final T p : copy )
		{
			final double val = p.getRealDouble();
			if ( val == 0. )
				p.setReal( min );
		}

		/*
		 * Tubeness filter.
		 */

		final Img< DoubleType > tubeness = Tubeness.tubeness2D( copy, sigma, Runtime.getRuntime().availableProcessors() / 2 + 1 );
		return tubeness;
	}
}
