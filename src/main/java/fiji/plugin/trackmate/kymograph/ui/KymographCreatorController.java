package fiji.plugin.trackmate.kymograph.ui;

import javax.swing.JDialog;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.KymographCreationParams;
import fiji.plugin.trackmate.kymograph.KymographCreator;
import ij.ImagePlus;

public class KymographCreatorController
{

	private JDialog dialog;

	private final KymographCreator creator;

	private final Model model;

	public KymographCreatorController( final Model model, final ImagePlus imp )
	{
		this.model = model;
		this.creator = new KymographCreator( model, imp, KymographCreationParams.create().get() );
	}

	public void showUI()
	{
		if ( null == dialog )
		{
			dialog = new JDialog();
			dialog.setLocationByPlatform( true );
			dialog.setLocationRelativeTo( null );
			dialog.setTitle( "TrackMate Kymograph" );
			dialog.setIconImage( Icons.TRACKMATE_ICON.getImage() );

			final TrackSelectorUI selectorUI = new TrackSelectorUI( model );
			model.addModelChangeListener( selectorUI );
			final KymographCreatorPanel panel = new KymographCreatorPanel( selectorUI );
			dialog.getContentPane().add( panel );
			dialog.pack();

			panel.btnCreate.addActionListener( e -> create( panel.getKymographCreationParams() ) );
		}
		dialog.setVisible( true );
	}

	private void create( final KymographCreationParams params )
	{
		model.getLogger().log( "Generating kymograph with the following parameters: " + params.toString() );
		creator.setParams( params );
		if ( !creator.checkInput() || !creator.process() )
		{
			model.getLogger().error( creator.getErrorMessage() );
			return;
		}
		final ImagePlus out = creator.getResult();
		out.show();
		model.getLogger().log( "\nDone." );
	}
}
