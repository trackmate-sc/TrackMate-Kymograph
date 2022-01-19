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

public class Utils
{

	static void check( final boolean b )
	{
		if ( !b )
			throw new RuntimeException();
	}

	static void check( final boolean b, final String msg )
	{
		if ( !b )
			throw new RuntimeException( msg );
	}

	static void check( final boolean b, final String format, final Object... args )
	{
		if ( !b )
			throw new RuntimeException( String.format( format, args ) );
	}

	static int mask( final int nbit )
	{
		check( nbit >= 1 && nbit <= 32 );
		return nbit == 32 ? -1 : ( 1 << nbit ) - 1;
	}
}
