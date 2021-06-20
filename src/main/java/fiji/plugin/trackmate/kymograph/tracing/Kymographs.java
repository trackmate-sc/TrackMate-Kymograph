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

	public Kymographs()
	{
		this( new ArrayList<>() );
	}

	private Kymographs( final List< Kymograph > kymographs )
	{
		this.kymographs = kymographs;
		this.selection = new ArrayList<>();
		this.updateListeners = new Listeners.SynchronizedList<>();
		this.selectionListeners = new Listeners.SynchronizedList<>();
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

		public Builder point( final double x, final double y )
		{
			if ( currentSegment == null )
				throw new IllegalArgumentException( "Please create a new segment before adding points." );

			currentSegment.points.add( RealPoint.wrap( new double[] { x, y } ) );
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
	}

	public static class Segment implements Iterable< RealLocalizable >
	{

		private final List< RealLocalizable > points = new ArrayList<>();

		private String name;

		public Segment( final String name )
		{
			this.name = name;
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
	}
}
