package fiji.plugin.trackmate.kymograph;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.io.TmXmlReader;
import ij.ImageJ;
import ij.ImagePlus;

public class KymographCreatorTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );
		final String filePath = "../TrackMate/samples/MAX_Merged.xml";

		final Integer trackID1 = 0;
		final Integer trackID2 = 2;
		final KymographCreationParams params = KymographCreationParams.create()
				.trackID1( trackID1 )
				.trackID2( trackID2 )
				.thickness( 5 )
				.alignment( KymographAlignment.FIRST )
				.projectionMethod( KymographProjectionMethod.MIP )
				.get();

		final TmXmlReader reader = new TmXmlReader( new File( filePath ) );
		if ( !reader.isReadingOk() )
		{
			System.err.println( reader.getErrorMessage() );
			return;
		}

		final Model model = reader.getModel();
		final ImagePlus imp = reader.readImage();
		imp.show();

		final KymographCreator creator = new KymographCreator( model, imp, params );
		if ( !creator.checkInput() || !creator.process() )
		{
			System.out.println( creator.getErrorMessage() );
			return;
		}
		final ImagePlus out = creator.getResult();
		out.show();
		System.out.println( "Finished!" );
	}
}
