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
package fiji.plugin.trackmate.kymograph.ui;

import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Optional;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.DefaultXYDataset;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.KymographCreationParams;
import fiji.plugin.trackmate.util.ExportableChartPanel;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.GlasbeyLut;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * Static utilities related to the generation of kymographs from a TrackMate
 * model.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class KymographUtils
{

	public static final JFrame plotKymographLength( final Model model, final KymographCreationParams params, final String spaceUnits, final String timeUnits )
	{
		/*
		 * Collect data.
		 */

		final int[] minmax = getMinMaxTimePoints( model, params.trackID1, params.trackID1 );
		final int nFrames = minmax[ 1 ] - minmax[ 0 ] + 1;

		final double[] length = new double[ nFrames ];
		final double[] time = new double[ nFrames ];

		for ( int i = 0; i < nFrames; i++ )
		{
			final int tp = i + minmax[ 0 ];

			final Set< Spot > spots1 = model.getTrackModel().trackSpots( params.trackID1 );
			final Optional< Spot > opt1 = spots1.stream().filter( s -> s.getFeature( Spot.FRAME ).intValue() == tp ).findFirst();

			final Set< Spot > spots2 = model.getTrackModel().trackSpots( params.trackID2 );
			final Optional< Spot > opt2 = spots2.stream().filter( s -> s.getFeature( Spot.FRAME ).intValue() == tp ).findFirst();

			if ( !opt1.isPresent() || !opt2.isPresent() )
			{
				length[ i ] = Double.NaN;
				if ( opt1.isPresent() )
					time[ i ] = opt1.get().getFeature( Spot.POSITION_T );
				else if ( opt2.isPresent() )
					time[ i ] = opt2.get().getFeature( Spot.POSITION_T );
				else
					time[ i ] = Double.NaN;
			}
			else
			{
				final Spot s1 = opt1.get();
				final Spot s2 = opt2.get();
				length[ i ] = Math.sqrt( s1.squareDistanceTo( s2 ) );
				time[ i ] = s1.getFeature( Spot.POSITION_T );
			}
		}

		/*
		 * Plot it.
		 */

		final DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries( "Kymograph length", new double[][] { time, length } );
		final ChartPanel chart = chart( dataset, timeUnits, "Kymograph length", spaceUnits, true, true );

		// The Panel.
		final JPanel panel = new JPanel( new BorderLayout( 5, 5 ) );
		panel.add( chart, BorderLayout.CENTER );

		// The frame
		final JFrame frame = new JFrame();
		frame.setTitle( "Kymograph length" );
		frame.setIconImage( Icons.PLOT_ICON.getImage() );
		frame.getContentPane().add( panel );
		frame.validate();
		frame.setSize( new java.awt.Dimension( 520, 320 ) );
		return frame;
	}

	/**
	 * Returns an {@link ExportableChartPanel} displaying the specified dataset.
	 * 
	 * @param dataset
	 *            the dataset to plot.
	 * @param timeUnits
	 *            the time units.
	 * @param ylabel
	 *            the label for the Y axis.
	 * @param yUnits
	 *            the units of the Y axis.
	 * @param drawZeroLine
	 *            if <code>true</code> an horizontal line will be plotted.
	 * @param showTableMenuItem
	 *            if <code>true</code> a menu item to display the tables will be
	 *            shown in the popup menu.
	 * @return a new {@link ExportableChartPanel}.
	 */
	public static final ExportableChartPanel chart(
			final DefaultXYDataset dataset,
			final String timeUnits,
			final String ylabel,
			final String yUnits,
			final boolean drawZeroLine,
			final boolean showTableMenuItem )
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

		// Line at Y = 0.
		if ( drawZeroLine )
		{
			final ValueMarker marker = new ValueMarker( 0. );
			marker.setPaint( Color.black );
			plot.addRangeMarker( marker );
		}

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
				menu.remove( 11 ); // Crash otherwise.
				if ( showTableMenuItem )
				{
					final JMenuItem displayTableItem = new JMenuItem( "Display data tables" );
					displayTableItem.setActionCommand( "TABLES" );
					displayTableItem.addActionListener( e -> showDataTable( dataset, 0, xAxisLabel, yAxisLabel, title ) );
					menu.add( displayTableItem );
				}
				return menu;
			}
		};
		chartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
		return chartPanel;
	}

	/**
	 * Show the data in a dataset as an ImageJ results table.
	 * 
	 * @param dataset
	 *            the dataset to read the data from.
	 * @param series
	 *            the index of the series to read data from.
	 * @param xName
	 *            the name of the X variable.
	 * @param yName
	 *            the name of the Y variable.
	 * @param title
	 *            the title of the table.
	 */
	public static void showDataTable( final DefaultXYDataset dataset, final int series, final String xName, final String yName, final String title )
	{
		final ResultsTable table = new ResultsTable();
		final int n = dataset.getItemCount( series );
		for ( int i = 0; i < n; i++ )
		{
			final double x = dataset.getXValue( series, i );
			final double y = dataset.getYValue( series, i );
			table.addRow();
			table.addValue( xName, x );
			table.addValue( yName, y );
		}
		table.show( title );
	}

	/**
	 * Returns the min and max timpoints of the spots common to the two tracks
	 * specified by their id.
	 * 
	 * @param model
	 *            the model in which the tracks are stored.
	 * @param trackID1
	 *            the id of the first track.
	 * @param trackID2
	 *            the id of the second track.
	 * @return a new <code>int[]</code> array with min and max time-point.
	 */
	public static final int[] getMinMaxTimePoints( final Model model, final Integer trackID1, final Integer trackID2 )
	{
		final Set< Spot > spots1 = model.getTrackModel().trackSpots( trackID1 );
		final int min1 = spots1.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).min().getAsInt();
		final int max1 = spots1.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).max().getAsInt();

		final Set< Spot > spots2 = model.getTrackModel().trackSpots( trackID1 );
		final int min2 = spots2.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).min().getAsInt();
		final int max2 = spots2.stream().mapToInt( s -> s.getFeature( Spot.FRAME ).intValue() ).max().getAsInt();

		return new int[] { Math.max( min1, min2 ), Math.min( max1, max2 ) };
	}

	/**
	 * Returns <code>null</code> if the specified track does not have a spot for
	 * the specified time-point. Otherwise, returns the pixel coordinate of the
	 * spot.
	 * 
	 * @param model
	 *            the model to read the track from.
	 * @param imp
	 *            the image to get the calibration and the dimensionality from.
	 * @param tp
	 *            the time-point (0 based).
	 * @param trackID
	 *            the track ID.
	 * @return a new <code>int[]</code> array with 3 elements (x, y, z).
	 */
	public static final long[] getCoords( final Model model, final ImagePlus imp, final int tp, final Integer trackID )
	{
		final Set< Spot > spots = model.getTrackModel().trackSpots( trackID );
		final Optional< Spot > opt = spots.stream().filter( s -> s.getFeature( Spot.FRAME ).intValue() == tp ).findFirst();
		if ( !opt.isPresent() )
			return null;

		final Spot spot = opt.get();
		final double[] calibration = TMUtils.getSpatialCalibration( imp );
		final long[] coords = new long[ imp.getNSlices() > 1 ? 3 : 2 ];
		for ( int d = 0; d < coords.length; d++ )
			coords[ d ] = Math.round( spot.getDoublePosition( d ) / calibration[ d ] );

		return coords;
	}

	private KymographUtils()
	{}
}
