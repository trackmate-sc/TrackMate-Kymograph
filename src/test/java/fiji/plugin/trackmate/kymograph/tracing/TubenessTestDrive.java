package fiji.plugin.trackmate.kymograph.tracing;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.kymograph.tracing.filter.Tubeness;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

public class TubenessTestDrive
{
	public static < T extends RealType< T > & NativeType< T > > void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final String path = "samples/Kymograph.tif";
		final ImagePlus imp = IJ.openImage( path );
		imp.show();

		final Img< T > img = ImageJFunctions.wrap( imp );
		final Img< DoubleType > tubeness = Tubeness.tubeness2D( img, 1.5, 1 );
		ImageJFunctions.show( tubeness );
	}
}
