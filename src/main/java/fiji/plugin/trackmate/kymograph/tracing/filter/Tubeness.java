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
package fiji.plugin.trackmate.kymograph.tracing.filter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gradient.HessianMatrix;
import net.imglib2.algorithm.linalg.eigen.TensorEigenValues;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.parallel.DefaultTaskExecutor;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Tubeness
{

	public static < T extends RealType< T > > Img< DoubleType > tubeness2D( final RandomAccessibleInterval< T > input, final double sigma, final int nThreads )
	{
		final int numDimensions = input.numDimensions();
		assert numDimensions == 2;

		// Get a suitable image factory.
		final long[] gradientDims = new long[ numDimensions + 1 ];
		final long[] hessianDims = new long[ numDimensions + 1 ];
		for ( int d = 0; d < numDimensions; d++ )
		{
			hessianDims[ d ] = input.dimension( d );
			gradientDims[ d ] = input.dimension( d );
		}
		hessianDims[ numDimensions ] = numDimensions * ( numDimensions + 1 ) / 2;
		gradientDims[ numDimensions ] = numDimensions;
		final Dimensions hessianDimensions = FinalDimensions.wrap( hessianDims );
		final FinalDimensions gradientDimensions = FinalDimensions.wrap( gradientDims );
		final ImgFactory< DoubleType > factory = Util.getArrayOrCellImgFactory( hessianDimensions, new DoubleType() );
		final Img< DoubleType > hessian = factory.create( hessianDimensions );
		final Img< DoubleType > gradient = factory.create( gradientDimensions );
		final Img< DoubleType > gaussian = factory.create( input );

		// Output
		final Img< DoubleType > tubeness = Util.getArrayOrCellImgFactory( input, new DoubleType() ).create( input );


		try
		{
			// Handle multithreading.
			final ExecutorService es = Executors.newFixedThreadPool( nThreads );
			final DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor( es );
			
			// Hessian calculation.
			HessianMatrix.calculateMatrix( Views.extendBorder( input ), gaussian,
					gradient, hessian, new OutOfBoundsBorderFactory<>(), nThreads, es,
					sigma );

			// Hessian eigenvalues.
			final RandomAccessibleInterval< DoubleType > evs = TensorEigenValues
					.calculateEigenValuesSymmetric( hessian, TensorEigenValues
							.createAppropriateResultImg( hessian, factory ),
							nThreads, es );

			// Project. We take the largest negative eigenvalue
			for ( long z = 0; z < evs.dimension( 2 ); z++ )
			{
				final IntervalView< DoubleType > slice = Views.hyperSlice( evs, 2, z );
				LoopBuilder.setImages( tubeness, slice )
						.multiThreaded( taskExecutor )
						.forEachPixel( ( t, s ) -> t.set( Math.min( t.get(), s.get() ) ) );
			}

			// Invert and scale.
			LoopBuilder.setImages( tubeness )
					.multiThreaded( taskExecutor )
					.forEachPixel( t -> t.set( -sigma * sigma * t.get() ) );

		}
		catch ( IncompatibleTypeException | InterruptedException | ExecutionException e )
		{
			e.printStackTrace();
		}
		return tubeness;
	}
}
