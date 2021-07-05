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
package fiji.plugin.trackmate.kymograph.tracing.analysis;

import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Kymograph;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Segment;
import fiji.plugin.trackmate.util.ExportableChartPanel;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.FeatureColorGenerator;
import fiji.plugin.trackmate.visualization.GlasbeyLut;
import fiji.plugin.trackmate.visualization.table.TablePanel;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleIntHashMap;
import net.imglib2.RealLocalizable;

public class KymographsAnalysis
{

	protected static final Shape DEFAULT_SHAPE = new Ellipse2D.Double( -3, -3, 6, 6 );

	public static final double[] timeMinMax( final Kymographs kymographs )
	{
		return minMax( kymographs, 0 );
	}

	public static final double[] positionMinMax( final Kymographs kymographs )
	{
		return minMax( kymographs, 1 );
	}

	private static final double[] minMax( final Kymographs kymographs, final int dim )
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for ( final Kymograph kymograph : kymographs )
			for ( final Segment segment : kymograph )
				for ( final RealLocalizable point : segment )
				{
					final double x = point.getDoublePosition( dim );
					if ( x > max )
						max = x;
					if ( x < min )
						min = x;
				}
		return new double[] { min, max };
	}

	public static final JFrame tables( final Kymographs kymographs )
	{
		final DefaultXYDataset positionDataset = positionDataset( kymographs );
		final DefaultXYDataset velocityDataset = velocityDataset( kymographs );
		final DefaultXYDataset smoothVelocityDataset = smoothVelocityDataset( kymographs );
		return new KymographTables(
				table( kymographs, positionDataset, Dimension.POSITION ),
				table( kymographs, velocityDataset, Dimension.VELOCITY ),
				table( kymographs, smoothVelocityDataset, Dimension.VELOCITY ) );
	}

	public static final JFrame plot( final Kymographs kymographs )
	{
		final String spaceUnits = kymographs.getSpaceUnits();
		final String timeUnits = kymographs.getTimeUnits();

		// Position.
		final DefaultXYDataset positionDataset = positionDataset( kymographs );
		final ChartPanel positionChart = chart( positionDataset, timeUnits, "Position", spaceUnits );

		// Velocity.
		final DefaultXYDataset velocityDataset = velocityDataset( kymographs );
		final ChartPanel velocityChart = chart( velocityDataset, timeUnits, "Velocity", spaceUnits + "/" + timeUnits );

		// Smoorh velocity.
		final DefaultXYDataset smoothVelocityDataset = smoothVelocityDataset( kymographs );
		final ChartPanel smoothVelocityChart = chart( smoothVelocityDataset, timeUnits, "Smoothed velocity", spaceUnits + "/" + timeUnits );

		// The Panel.
		final JPanel panel = new JPanel();
		final BoxLayout panelLayout = new BoxLayout( panel, BoxLayout.Y_AXIS );
		panel.setLayout( panelLayout );

		panel.add( positionChart );
		panel.add( Box.createVerticalStrut( 5 ) );
		panel.add( velocityChart );
		panel.add( Box.createVerticalStrut( 5 ) );
		panel.add( smoothVelocityChart );

		// Scroll pane
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setViewportView( panel );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );

		// The frame
		final JFrame frame = new JFrame();
		frame.setTitle( "Position and Velocity analysis for kymographs: " + kymographs.toString() );
		frame.setIconImage( Icons.PLOT_ICON.getImage() );
		frame.getContentPane().add( scrollPane );
		frame.validate();
		frame.setSize( new java.awt.Dimension( 520, 320 ) );
		return frame;
	}

	private static DefaultXYDataset velocityDataset( final Kymographs kymographs )
	{
		final double timeInterval = kymographs.getTimeInterval();
		final DefaultXYDataset dataset = new DefaultXYDataset();
		for ( final Kymograph kymograph : kymographs )
		{
			if ( kymograph.isempty() )
				continue;
			final double[] timeMinMax = timeMinMax( kymograph );
			final double min = timeMinMax[ 0 ];
			final double max = timeMinMax[ 1 ];
			final double[] x = ramp( min, max, timeInterval );
			final PolynomialSplineFunction function = interpolate( kymograph, timeInterval );
			final UnivariateFunction derivative = function.derivative();
			final double[] y = new double[ x.length ];
			for ( int i = 0; i < x.length; i++ )
				y[ i ] = derivative.value( x[ i ] );

			dataset.addSeries( kymograph.toString(), new double[][] { x, y } );
		}
		return dataset;
	}

	private static DefaultXYDataset smoothVelocityDataset( final Kymographs kymographs )
	{
		final double timeInterval = kymographs.getTimeInterval();
		final DefaultXYDataset dataset = new DefaultXYDataset();
		for ( final Kymograph kymograph : kymographs )
		{
			if ( kymograph.isempty() )
				continue;
			final double[] timeMinMax = timeMinMax( kymograph );
			final double min = timeMinMax[ 0 ];
			final double max = timeMinMax[ 1 ];
			final double[] x = ramp( min, max, timeInterval );
			final double[] y = new double[ x.length ];
			final PolynomialSplineFunction function = interpolate( kymograph, timeInterval );
			for ( int i = 0; i < x.length; i++ )
				y[ i ] = function.value( x[ i ] );

			final double bandwidth = 0.25;
			final UnivariateInterpolator smoothingInterpolator;
			if ( 2. / x.length < bandwidth )
				smoothingInterpolator = new LoessInterpolator( bandwidth , 0 );
			else
				smoothingInterpolator = new LinearInterpolator();
			final PolynomialSplineFunction smoothFunction = ( PolynomialSplineFunction ) smoothingInterpolator.interpolate( x, y );

			final UnivariateFunction derivative = smoothFunction.derivative();
			final double[] ys = new double[ x.length ];
			for ( int i = 0; i < x.length; i++ )
				ys[ i ] = derivative.value( x[ i ] );

			dataset.addSeries( kymograph.toString(), new double[][] { x, ys } );
		}
		return dataset;
	}

	private static final DefaultXYDataset positionDataset( final Kymographs kymographs )
	{
		final double timeInterval = kymographs.getTimeInterval();
		final DefaultXYDataset dataset = new DefaultXYDataset();
		for ( final Kymograph kymograph : kymographs )
		{
			if ( kymograph.isempty() )
				continue;
			final double[] timeMinMax = timeMinMax( kymograph );
			final double min = timeMinMax[ 0 ];
			final double max = timeMinMax[ 1 ];
			final double[] x = ramp( min, max, timeInterval );
			final PolynomialSplineFunction function = interpolate( kymograph, timeInterval );
			final double[] y = new double[ x.length ];
			for ( int i = 0; i < x.length; i++ )
				y[ i ] = function.value( x[ i ] );

			dataset.addSeries( kymograph.toString(), new double[][] { x, y } );
		}
		return dataset;
	}

	private static final ChartPanel chart(
			final DefaultXYDataset dataset,
			final String timeUnits,
			final String ylabel,
			final String yUnits )
	{

		final Color bgColor = new Color( 220, 220, 220 );

		// The chart.
		final String title = "";
		final String xAxisLabel = "Time (" + timeUnits + ")";
		final String yAxisLabel = ylabel + " (" + yUnits + ")";
		final JFreeChart chart = ChartFactory.createXYLineChart(
				title,
				xAxisLabel,
				yAxisLabel,
				dataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false );
		chart.getTitle().setFont( FONT );
		chart.getLegend().setItemFont( SMALL_FONT );
		chart.setBackgroundPaint( bgColor );
		chart.setBorderVisible( false );
		chart.getLegend().setBackgroundPaint( bgColor );

		// Renderer.
		final XYLineAndShapeRenderer renderer = new XYSplineRenderer();
		// new XYLineAndShapeRenderer( true, false );
		final int nseries = dataset.getSeriesCount();
		GlasbeyLut.reset();
		renderer.setDefaultStroke( new BasicStroke( 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
		renderer.setDefaultShapesVisible( false );
		renderer.setAutoPopulateSeriesStroke( false );
		for ( int i = 0; i < nseries; i++ )
		{
			Color color = GlasbeyLut.next();
			final double colorDistance = GuiUtils.colorDistance( color, bgColor );
			// Invert if color difference is too small, to make it visible.
			if ( colorDistance < 10. )
				color = GuiUtils.invert( color );
			renderer.setSeriesPaint( i, color, false );
		}

		// The plot.
		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer( renderer );
		plot.getRangeAxis().setLabelFont( FONT );
		plot.getRangeAxis().setTickLabelFont( SMALL_FONT );
		plot.getDomainAxis().setLabelFont( FONT );
		plot.getDomainAxis().setTickLabelFont( SMALL_FONT );
		plot.setOutlineVisible( false );
		plot.setDomainCrosshairVisible( false );
		plot.setDomainGridlinesVisible( false );
		plot.setRangeCrosshairVisible( false );
		plot.setRangeGridlinesVisible( false );
		plot.setBackgroundAlpha( 0f );

		// Ticks. Fewer of them.
		plot.getRangeAxis().setTickLabelInsets( new RectangleInsets( 20, 10, 20, 10 ) );
		plot.getDomainAxis().setTickLabelInsets( new RectangleInsets( 10, 20, 10, 20 ) );

		// Plot range.
		( ( NumberAxis ) plot.getRangeAxis() ).setAutoRangeIncludesZero( false );

		/*
		 * The chart panel. Not the true exportable one from TrackMate (we use
		 * special dataset) but one that we can save as image anyway.
		 */
		final ExportableChartPanel chartPanel = new ExportableChartPanel( chart )
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected JPopupMenu createPopupMenu( final boolean properties, final boolean copy, final boolean save, final boolean print, final boolean zoom )
			{
				final JPopupMenu menu = super.createPopupMenu( properties, copy, false, print, zoom );
				menu.remove( 11 );
				return menu;
			}
		};
		chartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
		return chartPanel;
	}

	private static double[] ramp( final double min, final double max, final double timeInterval )
	{
		final double lmax = Math.max( max, min );
		final double lmin = Math.min( max, min );
		final int nPoints = 1 + ( int ) ( ( lmax - lmin ) / timeInterval );
		final double[] ramp = new double[ nPoints ];
		for ( int i = 0; i < nPoints; i++ )
			ramp[ i ] = lmin + i * timeInterval;
		return ramp;
	}

	private static double[] timeMinMax( final Kymograph kymograph )
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for ( final Segment segment : kymograph )
		{
			for ( final RealLocalizable point : segment )
			{
				final double time = point.getDoublePosition( 0 );
				if ( time > max )
					max = time;
				if ( time < min )
					min = time;
			}
		}
		return new double[] { min, max };
	}

	private static final PolynomialSplineFunction interpolate( final Kymograph kymograph, final double timeInterval )
	{
		final SplineInterpolator splineInterpolator = new SplineInterpolator();
		final LinearInterpolator linearInterpolator = new LinearInterpolator();

		/*
		 * Deal with possible multiple X position for one time. We average the
		 * position.
		 */

		// Map time position to sum of X position.
		final TDoubleDoubleHashMap sums = new TDoubleDoubleHashMap();
		// How many items in the sum.
		final TDoubleIntHashMap ns = new TDoubleIntHashMap();

		for ( final Segment segment : kymograph )
		{
			for ( final RealLocalizable point : segment )
			{
				// Time is in X in kymographs.
				final double time = point.getDoublePosition( 0 );
				// Position is in Y.
				final double position = point.getDoublePosition( 1 );

				sums.adjustOrPutValue( time, position, position );
				ns.adjustOrPutValue( time, 1, 1 );
			}
		}
		// Average.
		for ( final double time : sums.keys() )
		{
			final double sum = sums.get( time );
			sums.put( time, sum / ns.get( time ) );
		}

		// Sort
		final double[][] coords = new double[ sums.size() ][ 2 ];
		final double[] times = sums.keys();
		final double[] pos = sums.values();
		for ( int i = 0; i < pos.length; i++ )
		{
			coords[ i ][ 0 ] = times[ i ];
			coords[ i ][ 1 ] = pos[ i ];
		}
		Arrays.sort( coords, ( a, b ) -> Double.compare( a[ 0 ], b[ 0 ] ) );

		// Dezip;
		final double[] sortedTime = new double[ coords.length ];
		final double[] sortedPos = new double[ coords.length ];
		for ( int i = 0; i < coords.length; i++ )
		{
			sortedTime[ i ] = coords[ i ][ 0 ];
			sortedPos[ i ] = coords[ i ][ 1 ];
		}

		/*
		 * First interpolation. Our AStar scheme returns long lines over several
		 * pixels when the direction does not change. This is now what we want
		 * here, as we need to have one explicit measurement of position for
		 * every frame. So we recreate these points by linear interpolation.
		 */
		final PolynomialSplineFunction linearFun = linearInterpolator.interpolate( sortedTime, sortedPos );
		final double mint = sortedTime[ 0 ];
		final double maxt = sortedTime[ sortedTime.length - 1 ];
		final double[] linearTimes = ramp( mint, maxt, timeInterval );
		final double[] linearPos = new double[ linearTimes.length ];
		for ( int i = 0; i < linearTimes.length; i++ )
			linearPos[ i ] = linearFun.value( linearTimes[ i ] );

		// Now we can return a function that interpolates over these points.
		final PolynomialSplineFunction function = ( linearTimes.length < 3 )
				? linearInterpolator.interpolate( linearTimes, linearPos )
				: splineInterpolator.interpolate( linearTimes, linearPos );
		return function;
	}

	private static final TablePanel< Map< String, Double > > table( final Kymographs kymographs, final XYDataset dataset, final Dimension dimension )
	{
		final String xFeature = "Time";

		// Data values for each X, then for each kymograph, possibly missing.
		final TreeMap< Double, Map< String, Double > > dataItems = new TreeMap<>();
		final int nSeries = dataset.getSeriesCount();
		for ( int series = 0; series < nSeries; series++ )
		{
			final String name = dataset.getSeriesKey( series ).toString();
			final int nItems = dataset.getItemCount( series );
			for ( int item = 0; item < nItems; item++ )
			{
				final double x = dataset.getXValue( series, item );
				Map< String, Double > map = dataItems.get( Double.valueOf( x ) );
				if ( map == null )
				{
					map = new HashMap<>();
					dataItems.put( Double.valueOf( x ), map );
				}
				map.put( name, ( Double ) dataset.getY( series, item ) );
				map.putIfAbsent( xFeature, Double.valueOf( x ) );
			}
		}

		// Features.
		final List< String > features = new ArrayList<>( kymographs.size() + 1 );
		features.add( xFeature );
		features.addAll( getNames( kymographs ) );

		// Y values metadata.
		final Map< String, String > featureNames = new HashMap<>( kymographs.size() );
		final Map< String, String > featureShortNames = new HashMap<>( kymographs.size() );
		final Map< String, String > featureUnits = new HashMap<>( kymographs.size() );
		final Map< String, Boolean > isInts = new HashMap<>( kymographs.size() );
		final Map< String, String > infoTexts = new HashMap<>( kymographs.size() );
		for ( final String feature : features )
		{
			featureNames.put( feature, feature );
			featureShortNames.put( feature, feature );
			featureUnits.put( feature, TMUtils.getUnitsFor( dimension, kymographs.getSpaceUnits(), kymographs.getTimeUnits() ) );
			isInts.put( feature, Boolean.FALSE );
			infoTexts.put( feature, "" );
		}

		// X value metadata.
		featureNames.put( xFeature, xFeature );
		featureShortNames.put( xFeature, xFeature );
		featureUnits.put( xFeature, kymographs.getTimeUnits() );
		isInts.put( xFeature, Boolean.FALSE );
		infoTexts.put( xFeature, "" );

		// What we do not use.
		final Function< Map< String, Double >, String > labelGenerator = null;
		final BiConsumer< Map< String, Double >, String > labelSetter = null;
		final Supplier< FeatureColorGenerator< Map< String, Double > > > coloring = null;

		// Value provider.
		final BiFunction< Map< String, Double >, String, Double > featureFun = ( item, feature ) -> item.get( feature );

		// The table.
		final TablePanel< Map< String, Double > > table = new TablePanel<>(
				dataItems.values(),
				features,
				featureFun,
				featureNames,
				featureShortNames,
				featureUnits,
				isInts,
				infoTexts,
				coloring,
				labelGenerator,
				labelSetter );
		return table;
	}

	private static List< String > getNames( final Kymographs kymographs )
	{
		final List< String > names = new ArrayList<>( kymographs.size() );
		for ( final Kymograph kymograph : kymographs )
			names.add( kymograph.toString() );
		return names;
	}
}
