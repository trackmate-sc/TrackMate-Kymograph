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
package fiji.plugin.trackmate.kymograph;

public enum KymographAlignment
{

	CENTER( "Center" ),
	FIRST( "First track" ),
	SECOND( "Second track" );

	private final String name;

	KymographAlignment( final String name )
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public int offset( final int length, final int width )
	{
		switch ( this )
		{
		default:
		case FIRST:
			return 0;
		case CENTER:
			return ( width - length ) / 2;
		case SECOND:
			return width - length;
		}
	}
}
