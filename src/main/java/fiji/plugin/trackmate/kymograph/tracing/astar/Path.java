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

import java.util.Arrays;
import java.util.Iterator;

import net.imglib2.Localizable;
import net.imglib2.Point;

public class Path implements Iterable< Localizable >
{

	private long[] ps;

	private int size;

	public Path()
	{
		this.ps = new long[ 8 ];
	}

	void add( final int x, final int y )
	{
		final long p = toPoint( x, y );
		if ( size >= ps.length )
			grow( size + 1 );

		ps[ size ] = p;
		size++;
	}

	void remove()
	{
		size--;
	}

	private long get( final int i )
	{
		assert i >= 0 && i < size;
		return ps[ size - 1 - i ];
	}

	public int size()
	{
		return size;
	}

	public boolean isEmpty()
	{
		return size < 2;
	}

	public void clear()
	{
		size = 0;
	}

	private void grow( final int minCapacity )
	{
		final int oldCapacity = ps.length;
		int newCapacity = oldCapacity + ( ( oldCapacity < 64 ) ? ( oldCapacity + 2 ) : ( oldCapacity >> 1 ) );

		if ( newCapacity < minCapacity )
			newCapacity = minCapacity;

		if ( newCapacity < 0 )
			throw new RuntimeException( "Overflow" );
		ps = Arrays.copyOf( ps, newCapacity );
	}

	private static long toPoint( final int x, final int y )
	{
		return ( ( ( long ) x ) << 32 ) | ( y & 0xFFFFFFFFL );
	}

	private static int getX( final long p )
	{
		return ( int ) ( p >>> 32 );
	}

	private static int getY( final long p )
	{
		return ( int ) p;
	}

	@Override
	public Iterator< Localizable > iterator()
	{
		final Point point = new Point( 2 );
		return new Iterator< Localizable >()
		{

			int index = -1;

			@Override
			public boolean hasNext()
			{
				return index + 1 < size;
			}

			@Override
			public Localizable next()
			{
				index++;
				final long p = get( index );
				point.setPosition( getX( p ), 0 );
				point.setPosition( getY( p ), 1 );
				return point;
			}
		};
	}
}
