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

		final TmXmlReader reader = new TmXmlReader( new File( filePath ) );
		if ( !reader.isReadingOk() )
		{
			System.err.println( reader.getErrorMessage() );
			return;
		}

		final Model model = reader.getModel();
		final ImagePlus imp = reader.readImage();
		imp.show();

		/*
		 * Thin line.
		 */

		final KymographCreationParams params1 = KymographCreationParams.create()
				.trackID1( trackID1 )
				.trackID2( trackID2 )
				.thickness( 1 )
				.alignment( KymographAlignment.FIRST )
				.projectionMethod( KymographProjectionMethod.MIP )
				.get();
		final KymographCreator creator = new KymographCreator( model, imp, params1 );
		if ( !creator.checkInput() || !creator.process() )
		{
			System.out.println( creator.getErrorMessage() );
			return;
		}
		final ImagePlus out1 = creator.getResult();
		out1.show();

		/*
		 * Thick line.
		 */

		final KymographCreationParams params2 = KymographCreationParams.create()
				.trackID1( trackID1 )
				.trackID2( trackID2 )
				.thickness( 10 )
				.alignment( KymographAlignment.CENTER )
				.projectionMethod( KymographProjectionMethod.MEAN )
				.get();

		creator.setParams( params2 );
		if ( !creator.checkInput() || !creator.process() )
		{
			System.out.println( creator.getErrorMessage() );
			return;
		}
		final ImagePlus out2 = creator.getResult();
		out2.show();

		System.out.println( "Finished!" );
	}
}
