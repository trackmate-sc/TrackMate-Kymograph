package fiji.plugin.trackmate.kymograph.tracing;

import java.awt.Color;
import java.util.function.IntToLongFunction;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.kymograph.tracing.astar.AStar2D;
import fiji.plugin.trackmate.kymograph.tracing.astar.AStarHeuristics;
import fiji.plugin.trackmate.kymograph.tracing.astar.Path;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

public class AStarTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final String path = "samples/Kymograph-1.tif";
		final ImagePlus imp = IJ.openImage( path );
		imp.show();

//		final int width = 512;
//		final int height = width;
//		final Point start = Point.wrap( new long[] { ( long ) ( 0.2 * width ), ( long ) ( 0.2 * height ) } );
//		final Point end = Point.wrap( new long[] { ( long ) ( 0.8 * width ), ( long ) ( 0.8 * height ) } );
//		final Img< UnsignedShortType > img = testImageSin( width, height, start, end );
//		ImageJFunctions.show( img );

		imp.setOverlay( new Overlay() );

		final Img< ? > img = ImageJFunctions.wrap( imp );
		final Localizable start = Point.wrap( new long[] { 10, 10 } );
		final Localizable end = Point.wrap( new long[] { img.dimension( 0 ) - 10, img.dimension( 1 ) - 10 } );
		@SuppressWarnings( { "unchecked", "rawtypes" } )
		final AStar2D astar = new AStar2D( img, img );

		System.out.println( "\nGreen: Ignore image." );
		astar.setHeuristics( AStarHeuristics.CHEBYSHEV );
		astar.setIntensityPenalty( 10. );
		astar.setThreshold( 0. );
		final long t2a = System.currentTimeMillis();
		final Path path2 = astar.search( start, end );
		final long t2b = System.currentTimeMillis();
		System.out.println( "Search completed in " + ( t2b - t2a ) + " ms." );
		final ij.gui.PolygonRoi roi2 = KymographTracing.toRoi( path2 );
		roi2.setStrokeColor( Color.GREEN );
		imp.getOverlay().add( roi2 );
		imp.updateAndDraw();
	}

	static final Img< UnsignedShortType > testImageSin( final int width, final int height, final Point start, final Point end )
	{
		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( width, height );

		final IntToLongFunction f = x -> {
			final double omega = 2. * Math.PI * ( x - start.getDoublePosition( 0 ) ) / ( end.getDoublePosition( 0 ) - start.getDoublePosition( 0 ) );
			final double amp = Math.abs( end.getDoublePosition( 1 ) - start.getDoublePosition( 1 ) ) * Math.sin( omega );
			final double slope = ( end.getDoublePosition( 1 ) - start.getDoublePosition( 1 ) ) / ( end.getDoublePosition( 0 ) - start.getDoublePosition( 0 ) );
			final double y = slope * ( x - start.getDoublePosition( 0 ) ) + start.getDoublePosition( 1 ) + amp;
			return ( long ) y;
		};

		final RandomAccess< UnsignedShortType > ra = img.randomAccess( img );
		for ( int x = start.getIntPosition( 0 ); x <= end.getIntPosition( 0 ); x++ )
		{
			ra.setPosition( x, 0 );
			ra.setPosition( f.applyAsLong( x ), 1 );
			ra.get().set( 1000 );
		}

		final Img< UnsignedShortType > dilated = Dilation.dilate( img, StructuringElements.square( 3, 2, true ), 5 );
		Gauss3.gauss( 3, Views.extendMirrorSingle( dilated ), dilated );
		return dilated;
	}
}
