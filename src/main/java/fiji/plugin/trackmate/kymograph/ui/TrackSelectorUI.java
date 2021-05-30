package fiji.plugin.trackmate.kymograph.ui;

import java.awt.Component;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.ModelChangeEvent;
import fiji.plugin.trackmate.ModelChangeListener;
import fiji.plugin.trackmate.TrackModel;

public class TrackSelectorUI implements ModelChangeListener
{

	private final Model model;

	private final ListCellRenderer< Integer > renderer;

	private final WeakHashMap< JComboBox< Integer >, Boolean > uiObjs;

	@SuppressWarnings( "unchecked" )
	public TrackSelectorUI( final Model model )
	{
		this.model = model;
		this.renderer = new MyComboxRender();
		this.uiObjs = new WeakHashMap<>();
	}

	public final JComboBox< Integer > create()
	{
		final JComboBox< Integer > cmbbox = new JComboBox<>( createComboBoxModel( model ) );
		cmbbox.setRenderer( renderer );
		uiObjs.put( cmbbox, Boolean.TRUE );
		return cmbbox;
	}

	private static final DefaultComboBoxModel< Integer > createComboBoxModel( final Model model )
	{
		// Collect track names, sorted.
		final TrackModel trackModel = model.getTrackModel();
		final Set< Integer > trackIDs = trackModel.trackIDs( true );
		return new DefaultComboBoxModel<>( new Vector<>( trackIDs ) );
	}

	private final class MyComboxRender extends BasicComboBoxRenderer
	{

		private static final long serialVersionUID = 1L;

		@SuppressWarnings( "rawtypes" )
		@Override
		public Component getListCellRendererComponent(
				final JList list,
				final Object value,
				final int index,
				final boolean isSelected,
				final boolean cellHasFocus )
		{
			super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			final Integer trackID = ( Integer ) value;
			setText( model.getTrackModel().name( trackID ) );
			return this;
		}
	}

	@Override
	public void modelChanged( final ModelChangeEvent event )
	{
		if ( event.getEventID() == ModelChangeEvent.MODEL_MODIFIED
				|| event.getEventID() == ModelChangeEvent.TRACKS_COMPUTED
				|| event.getEventID() == ModelChangeEvent.TRACKS_VISIBILITY_CHANGED )
		{
			for ( final JComboBox< Integer > cmbbox : uiObjs.keySet() )
				cmbbox.setModel( createComboBoxModel( model ) );
		}
	}
}
