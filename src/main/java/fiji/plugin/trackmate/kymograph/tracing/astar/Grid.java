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
package fiji.plugin.trackmate.kymograph.tracing.astar;


public class Grid
{

	private static final int NODE_BITS = 12;

	private static final int NODE_MASK = Utils.mask( NODE_BITS );

	private static final int NODE_NULL = 0;

	private static final int NODE_CLOSED = NODE_MASK;

	private static final int NODE_PARENT_DIRECTION_BITS = 3;

	private static final int NODE_PARENT_DIRECTION_MASK = Utils.mask( NODE_PARENT_DIRECTION_BITS );

	private static final int NODE_PARENT_DIRECTION_SHIFT = NODE_BITS;

	private static final int NODE_PARENT_DIRECTION_SHIFT_MASK =
			NODE_PARENT_DIRECTION_MASK << NODE_PARENT_DIRECTION_SHIFT;

	private static final int WALKABLE_BITS = 1;

	private static final int WALKABLE_MASK = Utils.mask( WALKABLE_BITS );

	private static final int WALKABLE_SHIFT = NODE_PARENT_DIRECTION_SHIFT + NODE_PARENT_DIRECTION_BITS;

	private static final int WALKABLE_SHIFT_MASK = WALKABLE_MASK << WALKABLE_SHIFT;

	// const
	static final int DIRECTION_UP = 0;

	static final int DIRECTION_DOWN = 1;

	static final int DIRECTION_LEFT = 2;

	static final int DIRECTION_RIGHT = 3;

	static final int DIRECTION_LEFT_UP = 4;

	static final int DIRECTION_LEFT_DOWN = 5;

	static final int DIRECTION_RIGHT_UP = 6;

	static final int DIRECTION_RIGHT_DOWN = 7;

	static final int MAX_OPEN_NODE_SIZE = NODE_MASK - 1;

	// data
	private final short[][] grid;

	private final int width;

	private final int height;

	public Grid( final int width, final int height )
	{
		Utils.check( width > 0 && width <= Node.X_MASK + 1 );
		Utils.check( height > 0 && height <= Node.Y_MASK + 1 );
		this.grid = new short[ width ][ height ];
		this.width = width;
		this.height = height;
	}

	int info( final int x, final int y )
	{
		return grid[ x ][ y ] & ( WALKABLE_SHIFT_MASK | NODE_MASK );
	}

	static boolean isNullNode( final int info )
	{
		return info == NODE_NULL;
	}

	/**
	 * Returns <code>true</code> if the node with the specified info index has
	 * been closed.
	 * 
	 * @param info
	 *            the node info.
	 * @return <code>true</code> if the node has been closed.
	 */
	static boolean isClosedNode( final int info )
	{
		return info == NODE_CLOSED;
	}

	static int openNodeIdx( final int info )
	{
		assert info > 0 && info <= MAX_OPEN_NODE_SIZE;
		return info - 1;
	}

	/**
	 * Closes a node.
	 * 
	 * @param x
	 *            X position of the node.
	 * @param y
	 *            Y position of the node.
	 */
	void nodeClosed( final int x, final int y )
	{
		grid[ x ][ y ] |= NODE_CLOSED;
	}

	void openNodeIdxUpdate( final int x, final int y, final int idx )
	{
		assert idx >= 0 && idx < MAX_OPEN_NODE_SIZE;
		grid[ x ][ y ] = ( short ) ( grid[ x ][ y ] & ~NODE_MASK | ( idx + 1 ) );
	}

	void nodeParentDirectionUpdate( final int x, final int y, final int d )
	{
		assert d >= 0 && d <= NODE_PARENT_DIRECTION_MASK;
		grid[ x ][ y ] =
				( short ) ( grid[ x ][ y ] & ~NODE_PARENT_DIRECTION_SHIFT_MASK | ( d << NODE_PARENT_DIRECTION_SHIFT ) );
	}

	int nodeParentDirection( final int x, final int y )
	{
		return grid[ x ][ y ] >>> NODE_PARENT_DIRECTION_SHIFT & NODE_PARENT_DIRECTION_MASK;
	}

	void clear()
	{
		for ( int i = 0; i < width; i++ )
		{
			for ( int j = 0; j < height; j++ )
			{
				grid[ i ][ j ] &= WALKABLE_SHIFT_MASK;
			}
		}
	}

	boolean isClean()
	{
		for ( int i = 0; i < width; i++ )
			for ( int j = 0; j < height; j++ )
				if ( ( grid[ i ][ j ] & ( NODE_PARENT_DIRECTION_SHIFT_MASK | NODE_MASK ) ) != 0 )
					return false;

		return true;
	}
}
