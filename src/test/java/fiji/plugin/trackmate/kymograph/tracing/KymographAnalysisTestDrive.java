package fiji.plugin.trackmate.kymograph.tracing;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.kymograph.tracing.ui.KymographTracingController;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class KymographAnalysisTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException
	{
		final File kymographFile = new File( "samples/Kymograph.json" );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final String path = "samples/Kymograph.tif";
		final ImagePlus imp = IJ.openImage( path );
		imp.show();

		KymographTracingController.load( kymographFile, imp );

		final Kymographs kymographs = KymographsIO.load( kymographFile );
		final JFrame frame = KymographsAnalysis.plot( kymographs );
		GuiUtils.positionWindow( frame, imp.getWindow() );
		frame.setVisible( true );

	}
}
