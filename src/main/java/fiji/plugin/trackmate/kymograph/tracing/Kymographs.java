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
package fiji.plugin.trackmate.kymograph.tracing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Kymograph;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

/**
 * Data class that stores a collection of kymographs.
 */
public class Kymographs implements Iterable< Kymograph >
{

	private final List< Kymograph > kymographs;

	private final List< Segment > selection;

	private final transient Listeners.List< UpdateListener > updateListeners;

	private final transient Listeners.List< SelectionListener > selectionListeners;

	private String name;

	private final String spaceUnits;

	private final String timeUnits;

	private final double timeInterval;

	private final double spaceInterval;

	public Kymographs( final String name, final double spaceInterval, final double timeInterval, final String spaceUnits, final String timeUnits )
	{
		this( name, spaceInterval, timeInterval, spaceUnits, timeUnits, new ArrayList<>() );
	}

	private Kymographs( final String name, final double spaceInterval, final double timeInterval, final String spaceUnits, final String timeUnits, final List< Kymograph > kymographs )
	{
		this.name = name;
		this.spaceInterval = spaceInterval;
		this.timeInterval = timeInterval;
		this.spaceUnits = spaceUnits;
		this.timeUnits = timeUnits;
		this.kymographs = kymographs;
		this.selection = new ArrayList<>();
		this.updateListeners = new Listeners.SynchronizedList<>();
		this.selectionListeners = new Listeners.SynchronizedList<>();
	}

	public int size()
	{
		return kymographs.size();
	}

	public String getSpaceUnits()
	{
		return spaceUnits;
	}

	public String getTimeUnits()
	{
		return timeUnits;
	}

	public double getSpaceInterval()
	{
		return spaceInterval;
	}

	public double getTimeInterval()
	{
		return timeInterval;
	}

	@Override
	public Iterator< Kymograph > iterator()
	{
		return kymographs.iterator();
	}

	public void select( final Segment segment )
	{
		selection.clear();
		selection.add( segment );
		notifySelectionListeners();
	}

	public void select( final Kymograph kymograph )
	{
		selection.clear();
		selection.addAll( kymograph.segments );
		notifySelectionListeners();
	}

	public void clearSelection()
	{
		selection.clear();
		notifySelectionListeners();
	}

	public List< Segment > getSelection()
	{
		return selection;
	}

	public Builder add()
	{
		return new Builder( this );
	}

	public void removeKymograph( final Kymograph kymograph )
	{
		selection.removeAll( kymograph.segments );
		kymographs.remove( kymograph );
		notifyListeners();
	}

	public void removeSegment( final Kymograph kymograph, final Segment segment )
	{
		if ( !kymographs.contains( kymograph ) )
			return;

		selection.remove( segment );
		kymograph.segments.remove( segment );
		notifyListeners();
	}

	public Listeners.List< UpdateListener > listeners()
	{
		return updateListeners;
	}

	public Listeners.List< SelectionListener > selectionListeners()
	{
		return selectionListeners;
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.kymographsChanged();
	}

	private void notifySelectionListeners()
	{
		for ( final SelectionListener l : selectionListeners.list )
			l.kymographSelectionChanged();
	}

	public interface UpdateListener
	{
		public void kymographsChanged();
	}

	public interface SelectionListener
	{
		public void kymographSelectionChanged();
	}

	@Override
	public String toString()
	{
		return name;
	}

	public void setName( final String name )
	{
		this.name = name;
	}

	public static final class Builder
	{

		private Segment currentSegment;

		private Kymograph currentKymograph;

		private final Kymographs model;

		private Builder( final Kymographs model )
		{
			this.model = model;
		}

		public Builder kymograph( final String name )
		{
			currentKymograph = new Kymograph( name );
			model.kymographs.add( currentKymograph );
			model.notifyListeners();
			return this;
		}

		public Builder segment( final String name )
		{
			if ( currentKymograph == null )
				throw new IllegalArgumentException( "Please create a new kymograph before adding segments." );

			currentSegment = new Segment( name );
			currentKymograph.segments.add( currentSegment );
			model.notifyListeners();
			return this;
		}

		public Builder point( final RealLocalizable point )
		{
			return point( point.getDoublePosition( 0 ), point.getDoublePosition( 1 ) );
		}

		public Builder point( final double time, final double position )
		{
			if ( currentSegment == null )
				throw new IllegalArgumentException( "Please create a new segment before adding points." );

			currentSegment.points.add( RealPoint.wrap( new double[] { time, position } ) );
			return this;
		}

		public void done()
		{
			currentKymograph = null;
			currentSegment = null;
			model.notifyListeners();
		}
	}

	/**
	 * Represents a kymograph.
	 */
	public static class Kymograph implements Iterable< Segment >
	{

		private final List< Segment > segments = new ArrayList<>();

		private String name;

		public Kymograph( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}

		@Override
		public Iterator< Segment > iterator()
		{
			return segments.iterator();
		}

		public void setName( final String name )
		{
			this.name = name;
		}

		public int size()
		{
			return segments.size();
		}

		public boolean isempty()
		{
			for ( final Segment segment : segments )
				if ( !segment.isempty() )
					return false;
			return true;
		}
	}

	public static class Segment implements Iterable< RealLocalizable >
	{

		private final List< RealLocalizable > points = new ArrayList<>();

		private String name;

		public Segment( final String name )
		{
			this.name = name;
		}

		public boolean isempty()
		{
			return points.isEmpty();
		}

		@Override
		public Iterator< RealLocalizable > iterator()
		{
			return points.iterator();
		}

		@Override
		public String toString()
		{
			return name;
		}

		public void setName( final String name )
		{
			this.name = name;
		}

		public int size()
		{
			return points.size();
		}
	}
}
