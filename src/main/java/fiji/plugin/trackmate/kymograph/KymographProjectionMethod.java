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

import java.util.Arrays;

public enum KymographProjectionMethod
{

	MIP( "Max" ),
	MEAN( "Mean" );

	private final String name;

	KymographProjectionMethod( final String name )
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public Accumulator accumulator()
	{
		switch ( this )
		{
		case MEAN:
			return new MeanAccumulator();
		case MIP:
		default:
			return new MaxAccumulator();
		}
	}

	public static interface Accumulator
	{

		public void accumulate( double[] line );

		public double[] get();
	}

	private static class MeanAccumulator implements Accumulator
	{

		private double[] sum;

		private int n = 0;

		@Override
		public void accumulate( final double[] line )
		{
			if ( sum == null )
			{
				sum = Arrays.copyOf( line, line.length );
				n = 1;
				return;
			}

			n++;
			for ( int i = 0; i < Math.max( line.length, sum.length ); i++ )
				sum[ i ] += line[ i ];
		}

		@Override
		public double[] get()
		{
			if ( sum == null )
				return null;

			for ( int i = 0; i < sum.length; i++ )
				sum[ i ] /= n;

			return sum;
		}
	}

	private static class MaxAccumulator implements Accumulator
	{

		private double[] storage;

		@Override
		public void accumulate( final double[] line )
		{
			if ( storage == null )
			{
				storage = Arrays.copyOf( line, line.length );
				return;
			}

			for ( int i = 0; i < Math.max( line.length, storage.length ); i++ )
				storage[ i ] = Math.max( storage[ i ], line[ i ] );
		}

		@Override
		public double[] get()
		{
			return storage;
		}
	}
}
