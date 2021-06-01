/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 The Institut Pasteur.
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
