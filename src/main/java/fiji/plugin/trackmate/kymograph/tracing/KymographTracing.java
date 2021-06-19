package fiji.plugin.trackmate.kymograph.tracing;

import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.DOWN;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.LEFT;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.LEFT_DOWN;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.RIGHT;
import static fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.RIGHT_DOWN;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Context;

import fiji.plugin.trackmate.kymograph.tracing.astar.AStar2D;
import fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections;
import fiji.plugin.trackmate.kymograph.tracing.astar.Path;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.GlasbeyLut;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.ops.OpService;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

public class KymographTracing
{

	private static final String PREVIEW_ROI_NAME = "KT_preview_segment";

	private static final String POINT_ROI_NAME = "KT_knots";

	private static final String SEGMENT_ROI_NAME = "KT_segments";

	private final ImagePlus imp;

	private boolean isTracing = false;

	private AStar2D< DoubleType > astar;

	private Point start;

	private Color currentColor = GlasbeyLut.next();

	private final AtomicInteger pathID = new AtomicInteger( 0 );

	private final AtomicInteger segmentID = new AtomicInteger( 0 );

	private List< Path > currentPath;

	public KymographTracing( final ImagePlus imp )
	{
		this.imp = imp;
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
		currentPath = new ArrayList<>();

		@SuppressWarnings( "rawtypes" )
		final ImgPlus img = TMUtils.rawWraps( imp );
		@SuppressWarnings( { "unchecked" } )
		final Img< DoubleType > slice = prepare2Dslice( img, channel, z, frame );
		
		astar = new AStar2D<>( slice, slice );
		// Consider all intensities.
		astar.setThreshold( 0. );
		// Forbid moving back in time.
		astar.setDirections( AStarDirections.create()
				.add( LEFT )
				.add( LEFT_DOWN )
				.add( DOWN )
				.add( RIGHT_DOWN )
				.add( RIGHT )
				.get() );

		currentColor = GlasbeyLut.next();
		final PointRoi points = new PointRoi();
		points.setStrokeColor( currentColor );
		points.setPointType( PointRoi.CIRCLE );
		imp.getOverlay().add( points, POINT_ROI_NAME + "_" + pathID.get() );

		start = Point.wrap( new long[] { x, y } );
		addToOverlay( start );
	}

	public void addSegment( final int x, final int y )
	{
		if ( astar == null )
			return;

		final Path path = getPathTo( x, y );
		if ( null == path )
			return;

		segmentID.incrementAndGet();
		currentPath.add( path );
		addToOverlay( path );
		start = Point.wrap( new long[] { x, y } );
		addToOverlay( start );
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

	public List< Path > finishPath()
	{
		astar = null;
		isTracing = false;
		clearPreview();
		return currentPath;
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

	private void addToOverlay( final Path path )
	{
		if ( path.isEmpty() )
			return;

		final PolygonRoi roi = toRoi( path );
		roi.setStrokeColor( currentColor );
		imp.getOverlay().add( roi, SEGMENT_ROI_NAME + "_" + pathID.get() + "_" + segmentID.get() );
	}

	private void addToOverlay( final Localizable point )
	{
		final PointRoi roi = ( PointRoi ) imp.getOverlay().get( POINT_ROI_NAME + "_" + pathID.get() );
		if ( roi == null )
			return;

		roi.addPoint( point.getDoublePosition( 0 ), point.getDoublePosition( 1 ) );
	}

	private static < T extends RealType< T > > Img< DoubleType > prepare2Dslice( final ImgPlus< T > img, final long channel, final long z, final long frame )
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

		final Context context = TMUtils.getContext();
		final OpService ops = context.getService( OpService.class );

		final double scale = 2;
		final double sigma = scale / Math.sqrt( 2 );

		final Img< DoubleType > tubeness = ops.create().img( copy, new DoubleType() );
		ops.filter().tubeness( tubeness, copy, sigma );

		return tubeness;
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
}
