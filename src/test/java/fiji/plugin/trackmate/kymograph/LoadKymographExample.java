package fiji.plugin.trackmate.kymograph;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.kymograph.plugin.LoadKymographPlugin;
import ij.ImageJ;

public class LoadKymographExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );
		final String filePath = "samples/MAX_U251mitoREDlifeAct6703_Kymograph_0-1.json";
		new LoadKymographPlugin().run( filePath );
	}
}
