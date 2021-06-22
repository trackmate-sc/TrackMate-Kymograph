package fiji.plugin.trackmate.kymograph.tracing.astar;

import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.MAX_OPEN_NODE_SIZE;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.getF;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.getX;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.getY;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.toNode;

import java.util.Arrays;

/**
 * Stores a collection of node as a heap.
 */
class Nodes
{
	Grid map;

	/**
	 * The nodes in a heap data structure, implemented in an array. Auto-resize.
	 */
	private long[] nodes;

	/**
	 * Size of the heap.
	 */
	private int size;

	Nodes()
	{
		this.nodes = new long[ 16 ];
	}

	void open( final int x, final int y, final int g, final int h, final int pd )
	{
		if ( size >= MAX_OPEN_NODE_SIZE )
			throw new RuntimeException( "TooManyOpenNodes! max: " + MAX_OPEN_NODE_SIZE );

		if ( size >= nodes.length )
			grow( size + 1 );

		final long node = node( x, y, g, h, pd );
		siftUp( size, node );
		size++;
	}

	long close()
	{
		if ( size == 0 )
			return 0;

		final long r = nodes[ 0 ];
		size--;
		if ( size > 0 )
		{
			final long n = nodes[ size ];
			siftDown( 0, n );
		}
		map.nodeClosed( getX( r ), getY( r ) );
		return r;
	}

	long getOpenNode( final int nodeIndex )
	{
		assert nodeIndex >= 0 && nodeIndex < size;
		return nodes[ nodeIndex ];
	}

	void openNodeParentChanged( final long node, final int idx, final int parentDirection )
	{
		siftUp( idx, node );
		map.nodeParentDirectionUpdate( getX( node ), getY( node ), parentDirection );
	}

	void clear()
	{
		size = 0;
		map.clear();
		map = null;
	}

	boolean isEmpty()
	{
		return size == 0;
	}

	private static final int HEAP_SHIFT = 2;

	private void siftUp( int i, final long node )
	{
		final int nf = getF( node );
		while ( i > 0 )
		{
			final int pi = ( i - 1 ) >>> HEAP_SHIFT;
			final long p = nodes[ pi ];
			if ( nf >= getF( p ) )
				break;

			setNode( i, p );
			i = pi;
		}
		setNode( i, node );
	}

	private void siftDown( int i, final long node )
	{
		final int nf = getF( node );
		while ( i < size )
		{
			int ci = ( i << HEAP_SHIFT ) + 1;
			if ( ci >= size )
				break;

			long c = nodes[ ci ];

			int cj = ci + 1;
			if ( cj < size )
			{
				if ( getF( nodes[ cj ] ) < getF( c ) )
					c = nodes[ ci = cj ];

				if ( ++cj < size )
				{
					if ( getF( nodes[ cj ] ) < getF( c ) )
						c = nodes[ ci = cj ];

					if ( ++cj < size )
					{
						if ( getF( nodes[ cj ] ) < getF( c ) )
							c = nodes[ ci = cj ];
					}
				}
			}

			if ( nf <= getF( c ) )
				break;

			setNode( i, c );
			i = ci;
		}
		setNode( i, node );
	}

	private void setNode( final int nodeIndex, final long node )
	{
		nodes[ nodeIndex ] = node;
		map.openNodeIdxUpdate( getX( node ), getY( node ), nodeIndex );
	}

	/**
	 * Creates a new node.
	 * 
	 * @param x
	 *            the node X position.
	 * @param y
	 *            the node Y position.
	 * @param g
	 *            the G cost: cost of the path that leads to this node from the
	 *            start.
	 * @param h
	 *            the H cost: heuristics estimating the cost from this node to
	 *            the end.
	 * @param parentDirection
	 *            this direction from this node to its parent.
	 * @return a new node.
	 */
	private long node( final int x, final int y, final int g, final int h, final int parentDirection )
	{
		final long node = toNode( x, y, g, g + h );
		map.nodeParentDirectionUpdate( x, y, parentDirection );
		return node;
	}

	private void grow( final int minCapacity )
	{
		final int oldCapacity = nodes.length;
		int newCapacity = oldCapacity + ( ( oldCapacity < 64 ) ? ( oldCapacity + 2 ) : ( oldCapacity >> 1 ) );

		if ( newCapacity < minCapacity )
			newCapacity = minCapacity;

		if ( newCapacity < 0 )
		{ throw new RuntimeException( "Overflow" ); }
		nodes = Arrays.copyOf( nodes, newCapacity );
	}
}
