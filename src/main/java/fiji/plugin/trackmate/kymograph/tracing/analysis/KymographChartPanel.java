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
package fiji.plugin.trackmate.kymograph.tracing.analysis;

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import com.itextpdf.text.DocumentException;

import fiji.plugin.trackmate.util.ChartExporter;
import ij.IJ;

public class KymographChartPanel extends ChartPanel
{

	private static final long serialVersionUID = 1L;

	private static File currentDir;

	/*
	 * CONSTRUCTORS
	 */

	public KymographChartPanel( final JFreeChart chart )
	{
		super( chart );
	}

	public KymographChartPanel(
			final JFreeChart chart,
			final boolean properties,
			final boolean save,
			final boolean print,
			final boolean zoom,
			final boolean tooltips )
	{
		super( chart, properties, save, print, zoom, tooltips );
	}

	public KymographChartPanel( final JFreeChart chart, final int width, final int height,
			final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth,
			final int maximumDrawHeight, final boolean useBuffer, final boolean properties,
			final boolean save, final boolean print, final boolean zoom, final boolean tooltips )
	{
		super( chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight,
				useBuffer, properties, save, print, zoom, tooltips );
	}

	public KymographChartPanel( final JFreeChart chart, final int width, final int height,
			final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth,
			final int maximumDrawHeight, final boolean useBuffer, final boolean properties,
			final boolean copy, final boolean save, final boolean print, final boolean zoom,
			final boolean tooltips )
	{
		super( chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight,
				useBuffer, properties, copy, save, print, zoom, tooltips );
	}

	/*
	 * METHODS
	 */

	@Override
	protected JPopupMenu createPopupMenu( final boolean properties, final boolean copy, final boolean save, final boolean print, final boolean zoom )
	{
		final JPopupMenu menu = super.createPopupMenu( properties, copy, false, print, zoom );

		menu.addSeparator();

		final JMenuItem exportToFile = new JMenuItem( "Export plot to file" );
		exportToFile.addActionListener( e -> doSaveAs() );
		menu.add( exportToFile );

		return menu;
	}

	/**
	 * Opens a file chooser and gives the user an opportunity to save the chart
	 * in PNG, PDF or SVG format.
	 */
	@Override
	public void doSaveAs()
	{
		if ( null == currentDir )
			currentDir = getDefaultDirectoryForSaveAs();

		final File file;
		if ( IJ.isMacintosh() )
		{
			Container dialogParent = getParent();
			while ( !( dialogParent instanceof Frame ) )
				dialogParent = dialogParent.getParent();

			final Frame frame = ( Frame ) dialogParent;
			final FileDialog dialog = new FileDialog( frame, "Export chart to PNG, PDF or SVG", FileDialog.SAVE );
			final FilenameFilter filter = ( dir, name ) -> name.endsWith( ".png" ) || name.endsWith( ".pdf" ) || name.endsWith( ".svg" );
			dialog.setFilenameFilter( filter );
			dialog.setDirectory( currentDir.getAbsolutePath() );
			dialog.setFile( new File( currentDir, getChart().getTitle().getText().replaceAll( "\\.+$", "" ) + ".pdf" ).getAbsolutePath() );
			dialog.setVisible( true );
			final String selectedFile = dialog.getFile();
			if ( null == selectedFile )
				return;

			file = new File( dialog.getDirectory(), selectedFile );
			currentDir = new File( dialog.getDirectory() );
		}
		else
		{
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle( "Export chart to PNG, PDF or SVG" );
			fileChooser.setCurrentDirectory( currentDir );
			fileChooser.addChoosableFileFilter( new FileNameExtensionFilter("PNG Image File", "png" ) );
			fileChooser.addChoosableFileFilter( new FileNameExtensionFilter("Portable Document File (PDF)", "pdf" ) );
			fileChooser.addChoosableFileFilter( new FileNameExtensionFilter( "Scalable Vector Graphics (SVG)", "svg" ) );
			fileChooser.setSelectedFile( new File( currentDir, getChart().getTitle().getText().replaceAll( "\\.+$", "" ) + ".pdf" ) );
			final int option = fileChooser.showSaveDialog( this );
			if ( option != JFileChooser.APPROVE_OPTION )
				return;

			file = fileChooser.getSelectedFile();
			currentDir = fileChooser.getCurrentDirectory();
		}
		try
		{
			if ( file.getPath().endsWith( ".png" ) )
				ChartUtils.saveChartAsPNG( file, getChart(), getWidth(), getHeight() );
			else if ( file.getPath().endsWith( ".pdf" ) )
				ChartExporter.exportChartAsPDF( file, getChart(), getWidth(), getHeight() );
			else if ( file.getPath().endsWith( ".svg" ) )
				ChartExporter.exportChartAsSVG( file, getChart(), getWidth(), getHeight() );
			else
				IJ.error( "Invalid file extension.", "Please choose a filename with one of the 3 supported extension: .png, .pdf or .svg." );
		}
		catch ( final IOException | DocumentException e )
		{
			e.printStackTrace();
		}
	}
}
