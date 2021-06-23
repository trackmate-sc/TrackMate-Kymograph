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
package fiji.plugin.trackmate.kymograph.tracing.astar;

class Node
{

	private static final int F_BITS = 16;

	private static final int F_MASK = Utils.mask( F_BITS );

	private static final int G_BITS = 16;

	private static final int G_MASK = Utils.mask( G_BITS );

	private static final int G_SHIFT = F_BITS;

	private static final long G_F_MASK_COMPLEMENT = ~( ( long ) G_MASK << G_SHIFT | F_MASK );

	private static final int Y_BITS = 16;

	static final int Y_MASK = Utils.mask( Y_BITS );

	private static final int Y_SHIFT = G_SHIFT + G_BITS;

	private static final int X_BITS = 16;

	static final int X_MASK = Utils.mask( X_BITS );

	private static final int X_SHIFT = Y_SHIFT + Y_BITS;

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
	 * @param f
	 *            the total estimated cost of the path that passes through this
	 *            node.
	 * @return a new node.
	 */
	static long toNode( final int x, final int y, final int g, final int f )
	{
		if ( f < 0 )
			throw new RuntimeException( "Path too long" );
		return ( long ) x << X_SHIFT | ( long ) y << Y_SHIFT | ( long ) g << G_SHIFT | f;
	}

	/**
	 * Returns the X position of the node.
	 * 
	 * @param node
	 *            the node.
	 * @return the X position of the node.
	 */
	static int getX( final long node )
	{
		return ( int ) ( node >>> X_SHIFT );
	}

	/**
	 * Returns the Y position of the node.
	 * 
	 * @param node
	 *            the node.
	 * @return the Y position of the node.
	 */
	static int getY( final long l )
	{
		return ( int ) ( l >>> Y_SHIFT & Y_MASK );
	}

	/**
	 * Returns the G cost: cost of the path that leads to this node from the
	 * start.
	 * 
	 * @param node
	 *            the node.
	 * @return the G cost.
	 */
	static int getG( final long node )
	{
		return ( int ) ( node >> G_SHIFT & G_MASK );
	}

	/**
	 * Returns the total estimated cost of the path that passes through this
	 * node.
	 * 
	 * @param node
	 *            the node.
	 * @return the F cost.
	 */
	static int getF( final long node )
	{
		return ( int ) ( node & F_MASK );
	}

	/**
	 * Updates the costs stored in a node.
	 * 
	 * @param node
	 *            the node to update.
	 * @param g
	 *            the G cost: cost of the path that leads to this node from the
	 *            start.
	 * @param f
	 *            the total estimated cost of the path that passes through this
	 *            node.
	 * @return the updated node.
	 */
	static long setGF( final long node, final int g, final int f )
	{
		return node & G_F_MASK_COMPLEMENT | ( ( long ) g << G_SHIFT ) | f;
	}
}
