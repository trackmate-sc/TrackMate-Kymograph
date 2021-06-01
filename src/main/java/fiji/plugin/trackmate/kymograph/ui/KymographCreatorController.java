package fiji.plugin.trackmate.kymograph.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.KymographCreationParams;
import fiji.plugin.trackmate.kymograph.KymographCreator;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.RotatedRectRoi;

public class KymographCreatorController
{

	private JDialog dialog;

	private final KymographCreator creator;

	private final Model model;

	private final ImagePlus imp;

	public KymographCreatorController( final Model model, final ImagePlus imp )
	{
		this.model = model;
		this.imp = imp;
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

	private void addKymographOverlay(final KymographCreationParams params)
	{
		Overlay overlay = imp.getOverlay();
		if ( overlay == null )
		{
			overlay = new Overlay();
			imp.setOverlay( overlay );
		}

		clearOverlay();
		final int nFrames = imp.getNFrames();
		for ( int tp = 0; tp < nFrames; tp++ )
		{
			final long[] coords1 = creator.getCoords( tp, params.trackID1 );
			if ( coords1 == null )
				continue;

			final long[] coords2 = creator.getCoords( tp, params.trackID2 );
			if ( coords2 == null )
				continue;

			final double x1 = coords1[ 0 ];
			final double y1 = coords1[ 1 ];
			final double x2 = coords2[ 0 ];
			final double y2 = coords2[ 1 ];
			final double rectWidth = params.thickness;
			final RotatedRectRoi roi = new RotatedRectRoi( x1, y1, x2, y2, rectWidth );
			roi.setPosition( 1, imp.getNSlices() / 2 + 1, tp + 1 );
			roi.setName( "TrackMate-Kymograph-tp" + ( tp + 1 ) );
			overlay.add( roi );
		}
		imp.updateAndDraw();
	}

	private void clearOverlay()
	{
		final Overlay overlay = imp.getOverlay();
		if ( overlay == null )
			return;

		final List< Roi > toRemove = new ArrayList<>();
		for ( final Roi roi : overlay )
		{
			if ( roi.getName() == null )
				continue;

			if ( roi.getName().startsWith( "TrackMate-Kymograph-tp" ) )
				toRemove.add( roi );
		}

		for ( final Roi roi : toRemove )
			overlay.remove( roi );
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

		addKymographOverlay( params );

		model.getLogger().log( "\nDone." );
	}
}
