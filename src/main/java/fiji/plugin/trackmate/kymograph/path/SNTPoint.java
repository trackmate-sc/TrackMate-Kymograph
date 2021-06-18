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

import java.util.Collection;
import java.util.Iterator;

/**
 * Classes extend this interface implement a point in a 3D space, always using
 * real world coordinates.
 *
 * @author Tiago Ferreira
 */
public interface SNTPoint
{

	public static final int X_AXIS = 1;

	public static final int Y_AXIS = 2;

	public static final int Z_AXIS = 4;

	/** @return the X-coordinate of the point */
	public double getX();

	/** @return the Y-coordinate of the point */
	public double getY();

	/** @return the Z-coordinate of the point */
	public double getZ();

	/** @return the coordinate on the specified axis */
	public double getCoordinateOnAxis( int axis );

	@SuppressWarnings( "unchecked" )
	public static < T extends SNTPoint > T average( final Collection< T > points )
	{
		double x = 0;
		double y = 0;
		double z = 0;
		double v = 0;
		if ( points == null || points.isEmpty() )
			return null;
		final Iterator< ? extends SNTPoint > it = points.iterator();
		boolean pim = false;
		while ( it.hasNext() )
		{
			final SNTPoint p = it.next();
			if ( p == null )
				continue;
			x += p.getX();
			y += p.getY();
			z += p.getZ();
			if ( pim || p instanceof PointInImage )
			{
				v += ( ( PointInImage ) p ).v;
				pim = true;
			}
			else if ( p instanceof SWCPoint )
				v += ( ( SWCPoint ) p ).radius;
		}
		final int n = points.size();
		if ( pim )
		{
			final PointInImage result = new PointInImage( x / n, y / n, z / n );
			result.v = v / n;
			return ( T ) result;
		}
		return ( T ) new SWCPoint( -1, -1, x / n, y / n, z / n, v / n, -1 );
	}

}
