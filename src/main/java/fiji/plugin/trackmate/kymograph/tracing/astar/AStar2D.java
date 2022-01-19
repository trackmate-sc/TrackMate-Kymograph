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

import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_DOWN;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_LEFT;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_LEFT_DOWN;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_LEFT_UP;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_RIGHT;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_RIGHT_DOWN;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_RIGHT_UP;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.DIRECTION_UP;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.isClosedNode;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.isNullNode;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Grid.openNodeIdx;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.getF;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.getG;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.getX;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.getY;
import static fiji.plugin.trackmate.kymograph.tracing.astar.Node.setGF;

import fiji.plugin.trackmate.kymograph.tracing.astar.AStarDirections.AStarDirection;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Localizables;
import net.imglib2.view.Views;

public class AStar2D< T extends RealType< T > >
{

	private final Nodes nodes;

	private final Grid map;

	private final RandomAccess< T > ra;

	private final Interval interval;

	private double minRai;

	private double maxRai;

	private double threshold = 0.5;

	private double intensityPenalty = 10.;

	private AStarHeuristics heuristics = AStarHeuristics.CHEBYSHEV;

	private AStarDirections directions = AStarDirections.HEIGHT_CONNECTED;

	public AStar2D( final RandomAccessible< T > img, final Interval interval )
	{
		if ( img.numDimensions() != 2 )
			throw new IllegalArgumentException( "This algorithm only works on 2D images." );

		this.interval = interval;
		assert intensityPenalty >= 0.;
		assert threshold >= 0.;
		assert threshold <= 1.;

		this.nodes = new Nodes();
		this.map = new Grid( ( int ) interval.dimension( 0 ), ( int ) interval.dimension( 1 ) );

		// Deal with RAI.
		minRai = Double.POSITIVE_INFINITY;
		maxRai = Double.NEGATIVE_INFINITY;
		for ( final T t : Views.interval( img, interval ) )
		{
			final double val = t.getRealDouble();
			if ( val > maxRai )
				maxRai = val;
			if ( val < minRai )
				minRai = val;
		}
		this.ra = img.randomAccess( interval );
	}

	public void setIntensityPenalty( final double intensityPenalty )
	{
		assert intensityPenalty >= 0.;
		this.intensityPenalty = intensityPenalty;
	}

	public void setThreshold( final double threshold )
	{
		assert threshold >= 0.;
		assert threshold <= 1.;
		this.threshold = threshold;
	}

	public void setHeuristics( final AStarHeuristics heuristics )
	{
		assert heuristics != null;
		this.heuristics = heuristics;
	}

	public void setDirections( final AStarDirections directions )
	{
		assert directions != null;
		this.directions = directions;
	}

	public Path search( final Localizable start, final Localizable target )
	{
		return search( start, target, false );
	}

	public Path search( final Localizable start, final Localizable target, final boolean smooth )
	{
		final Path p = new Path();
		search( start, target, p, smooth );
		return p;
	}

	public void search( final Localizable start, final Localizable target, final Path path )
	{
		search( start, target, path, false );
	}

	public void search( final Localizable start, final Localizable target, final Path path, final boolean smooth )
	{
		assert isClean( map );
		path.clear();

		if ( Localizables.equals( start, target ) )
			return;

		try
		{
			nodes.map = map;
			final int hcost = heuristics.cost( start, target );
			final int sx = start.getIntPosition( 0 );
			final int sy = start.getIntPosition( 1 );
			nodes.open( sx, sy, 0, hcost, DIRECTION_UP );

			while ( true )
			{
				final long n = nodes.close();
				if ( n == 0 )
					return;

				final int x = getX( n );
				final int y = getY( n );

				final int ex = target.getIntPosition( 0 );
				final int ey = target.getIntPosition( 1 );
				if ( x == ex && y == ey )
				{
					fillPath( ex, ey, sx, sy, path, map, smooth );
					return;
				}

				final int pg = getG( n );
				for ( final AStarDirection dir : directions )
					openWithWeight( x, y, dir, pg, ex, ey );
			}
		}
		catch ( final Exception e )
		{
			path.clear();
			throw e;
		}
		finally
		{
			clear();
			assert isClean( map );
		}
	}
	
	/**
	 * Possibly opens a new node at the specified coordinates.
	 * <p>
	 * Whether the node is opened or not depends on the image intensity value at
	 * its location, and on the value of {@link #threshold}. If the node is
	 * open, its cost is computed from the path cost so far (<code>pg</code>)
	 * plus the cost to walk to it with a weight that depends on the
	 * {@link #intensityPenalty} value.
	 * 
	 * @param x0
	 *            X position of the previous node.
	 * @param y0
	 *            Y position of the previous node.
	 * @param dir
	 *            in what direction to move to open the new node.
	 * @param pg
	 *            cost from the start to the previous node.
	 * @param ex
	 *            the target position X coordinate.
	 * @param ey
	 *            the target position Y coordinate.
	 */
	private void openWithWeight( final int x0, final int y0, final AStarDirection dir, final int pg, final int ex, final int ey )
	{
		// New node position.
		final int x = x0 + dir.xoffset;
		final int y = y0 + dir.yoffset;

		// Test whether we are out of the image.
		if ( x < 0 || y < 0 || x >= interval.dimension( 0 ) || y >= interval.dimension( 1 ) )
			return;

		/*
		 * x & y are coordinates in the grid, with min at 0. We need to
		 * transform them back to image coordinates here.
		 */
		ra.setPosition( x + interval.min( 0 ), 0 );
		ra.setPosition( y + interval.min( 1 ), 1 );

		// Shall we skip this node if intensity is too low?
		final double range = maxRai - minRai;
		final double minVal = minRai + threshold * range;
		final double val = ra.get().getRealDouble();
		if ( val < minVal )
			return;

		// Compute penalty.
		final double penalty = intensityPenalty * ( maxRai - val ) / ( maxRai - minVal );
		final int weight = ( int ) ( 1 + penalty );

		// Open node.
		final int g = dir.baseCost;
		final int pd = dir.direction;
		open( x, y, pg + g * weight, pd, ex, ey );
	}

	/**
	 * Open a node.
	 * 
	 * @param x
	 *            X position of the node.
	 * @param y
	 *            Y position of the node.
	 * @param g
	 *            cost from start.
	 * @param pd
	 *            move direction.
	 * @param ex
	 *            X position of the target.
	 * @param ey
	 *            Y position of the target.
	 */
	private void open( final int x, final int y, final int g, final int pd, final int ex, final int ey )
	{
		final int info = map.info( x, y );

		if ( isNullNode( info ) )
		{
			final int hcost = heuristics.cost( net.imglib2.Point.wrap( new long[] { x, y } ), net.imglib2.Point.wrap( new long[] { ex, ey } ) );
			nodes.open( x, y, g, hcost, pd );
			return;
		}

		if ( isClosedNode( info ) )
			return;

		final int idx = openNodeIdx( info );
		long n = nodes.getOpenNode( idx );

		final int ng = getG( n );
		if ( g >= ng )
			return;

		n = setGF( n, g, getF( n ) - ng + g );
		nodes.openNodeParentChanged( n, idx, pd );
	}

	private void fillPath( int ex, int ey, final int sx, final int sy, final Path path, final Grid map, final boolean smooth )
	{
		fillPath( ex, ey, path, map, smooth );
		int pd = map.nodeParentDirection( ex, ey );

		while ( true )
		{
			switch ( pd )
			{
			case DIRECTION_UP:
				ey++;
				break;

			case DIRECTION_DOWN:
				ey--;
				break;

			case DIRECTION_LEFT:
				ex--;
				break;

			case DIRECTION_RIGHT:
				ex++;
				break;

			case DIRECTION_LEFT_UP:
				ex--;
				ey++;
				break;

			case DIRECTION_LEFT_DOWN:
				ex--;
				ey--;
				break;

			case DIRECTION_RIGHT_UP:
				ex++;
				ey++;
				break;

			case DIRECTION_RIGHT_DOWN:
				ex++;
				ey--;
				break;

			default:
				throw new RuntimeException( "illegal direction: " + pd );
			}

			if ( ex == sx && ey == sy )
			{
				fillPath( ex, ey, path, map, smooth );
				return;
			}

			final int ppd = map.nodeParentDirection( ex, ey );
			if ( ppd != pd )
			{
				fillPath( ex, ey, path, map, smooth );
				pd = ppd;
			}
		}
	}

	private void fillPath( final int x, final int y, final Path path, final Grid map, final boolean smooth )
	{
		if ( !smooth )
		{
			path.add( x, y );
			return;
		}

		while ( path.size() >= 2 )
			path.remove();

		path.add( x, y );
	}

	private void clear()
	{
		nodes.clear();
	}

	private boolean isClean( final Grid map )
	{
		return nodes.isEmpty() && map.isClean();
	}
}
