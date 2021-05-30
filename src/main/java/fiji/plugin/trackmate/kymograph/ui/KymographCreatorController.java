package fiji.plugin.trackmate.kymograph.ui;

import javax.swing.JDialog;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.KymographCreationParams;

public class KymographCreatorController
{

	private final Model model;

	private JDialog dialog;

	public KymographCreatorController( final Model model )
	{
		this.model = model;
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
		System.out.println( params ); // DEBUG
		// TODO Auto-generated method stub
	}

}
