package fiji.plugin.trackmate.kymograph.tracing;

import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.DefaultXYDataset;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Kymograph;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Segment;
import fiji.plugin.trackmate.util.ExportableChartPanel;
import fiji.plugin.trackmate.visualization.GlasbeyLut;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleIntHashMap;
import net.imglib2.RealLocalizable;

public class KymographsAnalysis
{

	protected static final Shape DEFAULT_SHAPE = new Ellipse2D.Double( -3, -3, 6, 6 );

	public static final JFrame plot( final Kymographs kymographs )
	{
		final String timeUnits = "frame"; // TODO
		final String spaceUnits = "pixel"; // TODO
		final double timeInterval = 1.; // TODO

		// Position.
		final DefaultXYDataset positionDataset = positionDataset( kymographs, timeInterval );
		final ExportableChartPanel positionChart = chart( positionDataset, timeUnits, "Position", spaceUnits );

		// Velocity.
		final DefaultXYDataset velocityDataset = velocityDataset( kymographs, timeInterval );
		final ExportableChartPanel velocityChart = chart( velocityDataset, timeUnits, "Velocity", spaceUnits + "/" + timeUnits );

		// Smoorh velocity.
		final DefaultXYDataset smoothVelocityDataset = smoothVelocityDataset( kymographs, timeInterval );
		final ExportableChartPanel smoothVelocityChart = chart( smoothVelocityDataset, timeUnits, "Smoothed velocity", spaceUnits + "/" + timeUnits );

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

		// The frame
		final JFrame frame = new JFrame();
		frame.setTitle( "Position and Velocity analysis for kymographs: " + kymographs.toString() );
		frame.setIconImage( Icons.PLOT_ICON.getImage() );
		frame.getContentPane().add( scrollPane );
		frame.validate();
		frame.setSize( new java.awt.Dimension( 520, 320 ) );
		return frame;
	}

	private static DefaultXYDataset velocityDataset( final Kymographs kymographs, final double timeInterval )
	{
		final DefaultXYDataset dataset = new DefaultXYDataset();
		for ( final Kymograph kymograph : kymographs )
		{
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

	private static DefaultXYDataset smoothVelocityDataset( final Kymographs kymographs, final double timeInterval )
	{
		final DefaultXYDataset dataset = new DefaultXYDataset();
		for ( final Kymograph kymograph : kymographs )
		{
			final double[] timeMinMax = timeMinMax( kymograph );
			final double min = timeMinMax[ 0 ];
			final double max = timeMinMax[ 1 ];
			final double[] x = ramp( min, max, timeInterval );
			final double[] y = new double[ x.length ];
			final PolynomialSplineFunction function = interpolate( kymograph, timeInterval );
			for ( int i = 0; i < x.length; i++ )
				y[ i ] = function.value( x[ i ] );

			final LoessInterpolator smoothingInterpolator = new LoessInterpolator( 0.25, 0 );
			final PolynomialSplineFunction smoothFunction = smoothingInterpolator.interpolate( x, y );

			final UnivariateFunction derivative = smoothFunction.derivative();
			final double[] ys = new double[ x.length ];
			for ( int i = 0; i < x.length; i++ )
				ys[ i ] = derivative.value( x[ i ] );

			dataset.addSeries( kymograph.toString(), new double[][] { x, ys } );
		}
		return dataset;
	}

	private static final DefaultXYDataset positionDataset( final Kymographs kymographs, final double timeInterval )
	{
		final DefaultXYDataset dataset = new DefaultXYDataset();
		for ( final Kymograph kymograph : kymographs )
		{
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

	private static final ExportableChartPanel chart(
			final DefaultXYDataset dataset,
			final String timeUnits,
			final String ylabel,
			final String yUnits )
	{

		final Color bgColor = Color.LIGHT_GRAY;

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
			renderer.setSeriesPaint( i, GlasbeyLut.next(), false );

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

		// The chart panel.
		final ExportableChartPanel chartPanel = new ExportableChartPanel( chart );
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
				final double time = point.getDoublePosition( 1 );
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
				// Position is in X.
				final double position = point.getDoublePosition( 0 );
				// Time is in Y in kymographs.
				final double time = point.getDoublePosition( 1 );

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
		final PolynomialSplineFunction function = ( sortedTime.length < 3 )
				? linearInterpolator.interpolate( linearTimes, linearPos )
				: splineInterpolator.interpolate( linearTimes, linearPos );
		return function;
	}
}
