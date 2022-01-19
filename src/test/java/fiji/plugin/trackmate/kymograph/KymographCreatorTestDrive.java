/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
