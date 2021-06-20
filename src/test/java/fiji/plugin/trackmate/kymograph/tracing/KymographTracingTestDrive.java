package fiji.plugin.trackmate.kymograph.tracing;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.kymograph.tracing.ui.KymographTracingController;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class KymographTracingTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );
		TMUtils.getContext();

		final String path = "samples/Kymograph.tif";
		final ImagePlus imp = IJ.openImage( path );
		imp.show();

		new KymographTracingController( imp );
	}
}
