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

import net.imglib2.Localizable;
import net.imglib2.util.Util;

public interface AStarHeuristics
{

	public static final int COST_ORTHOGONAL = 5; // 1 * 5

	public static final int COST_DIAGONAL = 7; // 1.4 * 5

	public static AStarHeuristics EUCLIDEAN = ( c, t ) -> ( int ) Util.distance( c, t ) * COST_ORTHOGONAL;

	public static AStarHeuristics CHEBYSHEV = ( c, t ) -> {
		double maxDist = 0.;
		for ( int d = 0; d < c.numDimensions(); d++ )
			maxDist = Math.max( maxDist, Math.abs( c.getDoublePosition( d ) - t.getDoublePosition( d ) ) );
		return ( int ) ( maxDist * COST_ORTHOGONAL );
	};

	/**
	 * Returns the cost to reach the specified target position from the current
	 * position.
	 * 
	 * @param current
	 *            the current position.
	 * @param target
	 *            the target position.
	 * @return the cost as a positive double.
	 */
	public int cost( Localizable current, Localizable target );
}
