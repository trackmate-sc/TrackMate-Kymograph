/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2010 - 2021 Fiji developers.
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

import static fiji.plugin.trackmate.gui.Icons.CSV_ICON;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;
import fiji.plugin.trackmate.visualization.table.TablePanel;

public class KymographTables extends JFrame
{

	private static final long serialVersionUID = 1L;

	public static String selectedFile = System.getProperty( "user.home" ) + File.separator + "export.csv";

	private final List< TablePanel< Map< String, Double > > > tables;

	public KymographTables(
			final TablePanel< Map< String, Double > > positionTable,
			final TablePanel< Map< String, Double > > velocityTable,
			final TablePanel< Map< String, Double > > smoothedVelocityTable )
	{
		super( "Kymograph tables" );
		this.tables = new ArrayList<>( 3 );
		tables.add( positionTable );
		tables.add( velocityTable );
		tables.add( smoothedVelocityTable );

		/*
		 * GUI.
		 */

		setIconImage( Icons.SPOT_TABLE_ICON.getImage() );
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new BorderLayout() );

		// Tables.

		// Tabbed pane.
		final JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.LEFT );
		tabbedPane.add( "Position", positionTable.getPanel() );
		tabbedPane.add( "Velocity", velocityTable.getPanel() );
		tabbedPane.add( "Smoothed velocity", smoothedVelocityTable.getPanel() );

		tabbedPane.setSelectedComponent( positionTable.getPanel() );
		mainPanel.add( tabbedPane, BorderLayout.CENTER );

		// Tool bar.
		final JPanel toolbar = new JPanel();
		final BoxLayout layout = new BoxLayout( toolbar, BoxLayout.LINE_AXIS );
		toolbar.setLayout( layout );
		final JButton exportBtn = new JButton( "Export to CSV", CSV_ICON );
		exportBtn.addActionListener( e -> exportToCsv( tabbedPane.getSelectedIndex() ) );
		toolbar.add( exportBtn );
		toolbar.add( Box.createHorizontalGlue() );
		mainPanel.add( toolbar, BorderLayout.NORTH );

		getContentPane().add( mainPanel );
		pack();
	}

	private  void exportToCsv( final int index )
	{
		final TablePanel< Map< String, Double > > table = tables.get( index );
		final File file = FileChooser.chooseFile(
				this,
				selectedFile,
				new FileNameExtensionFilter( "CSV files", "csv" ),
				"Export table to CSV",
				DialogType.SAVE,
				SelectionMode.FILES_ONLY );
		if ( null == file )
			return;

		selectedFile = file.getAbsolutePath();
		try
		{
			table.exportToCsv( file );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
