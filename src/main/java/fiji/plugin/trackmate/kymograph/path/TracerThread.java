/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2010 - 2021 Fiji developers.
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

package fiji.plugin.trackmate.kymograph.path;

import ij.ImagePlus;

/**
 * SNT's default tracer thread: explores between two points in an image, doing
 * an A* search with a choice of distance measures.
 */
public class TracerThread extends SearchThread
{

	private int start_x;

	private int start_y;

	private int start_z;

	private int goal_x;

	private int goal_y;

	private int goal_z;

	private final boolean reciprocal;

	private Path result;

	public TracerThread( final ImagePlus imp, final int start_x, final int start_y,
			final int start_z, final int goal_x, final int goal_y, final int goal_z )
	{
		super( imp );
		reciprocal = true;
		init( start_x, start_y, start_z, goal_x, goal_y, goal_z );
	}

	/* If you specify 0 for timeoutSeconds then there is no timeout. */
	public TracerThread( final ImagePlus imagePlus, final float stackMin,
			final float stackMax, final int timeoutSeconds,
			final long reportEveryMilliseconds, final int start_x, final int start_y,
			final int start_z, final int goal_x, final int goal_y, final int goal_z,
			final boolean reciprocal, final boolean singleSlice,
			final double multiplier )
	{
		super( imagePlus, stackMin, stackMax, true, // bidirectional
				true, // definedGoal
				false, // startPaused,
				timeoutSeconds, reportEveryMilliseconds );

		this.reciprocal = reciprocal;
		init( start_x, start_y, start_z, goal_x, goal_y, goal_z );
	}

	private void init( final int start_x, final int start_y, final int start_z, final int goal_x, final int goal_y, final int goal_z )
	{
		this.start_x = start_x;
		this.start_y = start_y;
		this.start_z = start_z;
		this.goal_x = goal_x;
		this.goal_y = goal_y;
		this.goal_z = goal_z;
		minimum_cost_per_unit_distance = minimumCostPerUnitDistance();
		final SearchNode s = createNewNode( start_x, start_y, start_z, 0,
				estimateCostToGoal( start_x, start_y, start_z, true ), null,
				OPEN_FROM_START );
		addNode( s, true );
		final SearchNode g = createNewNode( goal_x, goal_y, goal_z, 0,
				estimateCostToGoal( goal_x, goal_y, goal_z, false ), null, OPEN_FROM_GOAL );
		addNode( g, false );
		this.result = null;
	}

	@Override
	protected boolean atGoal( final int x, final int y, final int z,
			final boolean fromStart )
	{
		if ( fromStart )
			return ( x == goal_x ) && ( y == goal_y ) && ( z == goal_z );
		else
			return ( x == start_x ) && ( y == start_y ) && ( z == start_z );
	}

	@Override
	protected double minimumCostPerUnitDistance()
	{
		final double minimum_cost = reciprocal ? ( 1 / 255.0 ) : 1;
		return minimum_cost;
	}

	@Override
	protected void foundGoal( final Path pathToGoal )
	{
		result = pathToGoal;
	}

	@Override
	public Path getResult()
	{
		if ( result != null && minExpectedSize > 0 && result.size() < minExpectedSize )
		{
			System.out.println( "Result size: " + result.size() + ", Min expected size: " + minExpectedSize );
			return null;
		}
		return result;
	}

	/*
	 * If we're taking the reciprocal of the value at the new point as our cost,
	 * then values of zero cause a problem. This is the value that we use
	 * instead of zero there.
	 */

	static final double RECIPROCAL_FUDGE = 0.5;

	/*
	 * This cost doesn't take into account the distance between the points - it
	 * will be post-multiplied by that value.
	 *
	 * The minimum cost should be > 0 - it is the value that is used in
	 * calculating the heuristic for how far a given point is from the goal.
	 */

	@Override
	protected double costMovingTo( final int new_x, final int new_y,
			final int new_z )
	{

		double cost;
		final double value_at_new_point = getValueAtNewPoint( new_x, new_y, new_z );
		if ( reciprocal )
		{
			cost = 1 / RECIPROCAL_FUDGE;
			if ( value_at_new_point != 0 )
				cost = 1.0 / value_at_new_point;
		}
		else
		{
			cost = 256 - value_at_new_point;
		}
		return cost;
	}

	private double getValueAtNewPoint( final int new_x, final int new_y, final int new_z )
	{
		double value_at_new_point = -1;
		switch ( imageType )
		{
		case ImagePlus.GRAY8:
		case ImagePlus.COLOR_256:
			value_at_new_point = slices_data_b[ new_z ][ new_y * width + new_x ] & 0xFF;
			break;
		case ImagePlus.GRAY16:
		{
			value_at_new_point = slices_data_s[ new_z ][ new_y * width + new_x ];
			break;
		}
		case ImagePlus.GRAY32:
		{
			value_at_new_point = slices_data_f[ new_z ][ new_y * width + new_x ];
			break;
		}
		}
		return 255.0 * ( value_at_new_point - stackMin ) / ( stackMax - stackMin );
	}

	@Override
	float estimateCostToGoal( final int current_x, final int current_y, final int current_z, final boolean fromStart )
	{
		final double xdiff = ( ( fromStart ? goal_x : start_x ) - current_x ) * x_spacing;
		final double ydiff = ( ( fromStart ? goal_y : start_y ) - current_y ) * y_spacing;
		final double zdiff = ( ( fromStart ? goal_z : start_z ) - current_z ) * z_spacing;
		final double distance = Math.sqrt( xdiff * xdiff + ydiff * ydiff + zdiff * zdiff );
		return ( float ) ( minimum_cost_per_unit_distance * distance );
	}
}
