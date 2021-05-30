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
