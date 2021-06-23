package fiji.plugin.trackmate.kymograph.tracing;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.kymograph.plugin.LoadKymographPlugin;
import ij.ImageJ;

public class KymographAnalysisTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final String path = "samples/Kymograph.json";
		new LoadKymographPlugin().run( path );
	}
}
