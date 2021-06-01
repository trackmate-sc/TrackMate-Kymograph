package fiji.plugin.trackmate.kymograph;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.LoadTrackMatePlugIn;
import ij.ImageJ;

public class TrackMateKymographActionTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );
//		final String filePath = "../TrackMate/samples/MAX_Merged.xml";
		final String filePath = "samples/MAX_U251mitoREDlifeAct6703.xml";
		new LoadTrackMatePlugIn().run( filePath );
//		final String imgPath = "samples/MAX_U251 mitoRED lifeAct670 3.tif";
//		new TrackMatePlugIn().run( imgPath );
	}
}
