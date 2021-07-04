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
package fiji.plugin.trackmate.kymograph.tracing.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.tracing.KymographOverlay;
import fiji.plugin.trackmate.kymograph.tracing.KymographTracer;
import fiji.plugin.trackmate.kymograph.tracing.KymographTracingTool;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs;
import fiji.plugin.trackmate.kymograph.tracing.KymographsIO;
import fiji.plugin.trackmate.kymograph.tracing.TracingParameters;
import fiji.plugin.trackmate.kymograph.tracing.analysis.KymographsAnalysis;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.ViewUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.DoubleType;

public class KymographTracingController
{

	private static JFileChooser fileChooser = new JFileChooser();

	static
	{
		fileChooser.setFileFilter( new FileNameExtensionFilter( "JSon files", "json" ) );
		final File defaultFile = new File( new File( System.getProperty( "user.dir" ) ), "Kymographs.json" );
		fileChooser.setSelectedFile( defaultFile );
	}

	private final KymographTracingPanel gui;

	public KymographTracingController( final ImagePlus imp )
	{
		this( imp, new Kymographs( imp.getTitle(), imp.getCalibration().getXUnit(), imp.getCalibration().getYUnit() ) );
	}

	public KymographTracingController( final ImagePlus imp, final Kymographs kymographs )
	{
		assert imp != null;
		fileChooser.setSelectedFile( proposeJsonFile( imp ) );

		// Display overlay.
		imp.setOverlay( new Overlay() );
		imp.getOverlay().add( new KymographOverlay( kymographs, imp ) );
		imp.updateAndDraw();

		// Tracing parameters.
		final TracingParameters tracingParameters = new TracingParameters();

		// Tracer.
		final KymographTracer tracer = new KymographTracer( imp, tracingParameters );

		// UI.
		gui = new KymographTracingPanel( kymographs, tracingParameters );
		final JFrame frame = new JFrame( "Kymographs of " + imp.getShortTitle() );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.setSize( 400, 400 );
		GuiUtils.positionWindow( frame, imp.getWindow() );
		frame.setVisible( true );

		// Tracking tool.
		final KymographTracingTool tool = KymographTracingTool.getInstance();
		tool.register( imp, kymographs, tracer, gui.getLogger() );

		// Wire some listeners.
		gui.btnPreview.addActionListener( e -> SwingUtilities.invokeLater( () -> preview( imp, tracingParameters.getSigma() ) ) );
		gui.btnSave.addActionListener( e -> save( kymographs, frame ) );
		gui.btnPlot.addActionListener( e -> plot( kymographs ) );
		gui.btnTables.addActionListener( e -> showTables( kymographs ) );
	}

	private void plot( final Kymographs kymographs )
	{
		final JFrame frame = KymographsAnalysis.plot( kymographs );
		GuiUtils.positionWindow( frame, SwingUtilities.getWindowAncestor( gui ) );
		frame.setVisible( true );
	}

	private void showTables( final Kymographs kymographs )
	{
		final JFrame frame = KymographsAnalysis.tables( kymographs );
		GuiUtils.positionWindow( frame, SwingUtilities.getWindowAncestor( gui ) );
		frame.setVisible( true );
	}

	public static void load() throws IOException
	{
		fileChooser.setDialogTitle( "Load from a JSon file" );
		final int returnVal = fileChooser.showOpenDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			final File selectedFile = fileChooser.getSelectedFile();
			load( selectedFile.getAbsolutePath() );
		}
	}

	public static void load( final String kymographPath ) throws IOException
	{
		final File kymographFile = new File( kymographPath );
		final String str = new String( Files.readAllBytes( kymographFile.toPath() ) );
		final Kymographs kymographs = KymographsIO.fromJson( str );
		final String imageName = kymographs.toString();
		final File imagePath = new File( kymographFile.getParent(), imageName );
		final ImagePlus imp;
		if ( !imagePath.exists() )
		{
			IJ.log( "Could not read image from path saved in kymograph file: " + imagePath );
			final double spaceInterval = KymographsAnalysis.guessSpaceInterval( kymographs );
			final double timeInterval = KymographsAnalysis.guessTimeInterval( kymographs );
			final double[] positionMinMax = KymographsAnalysis.positionMinMax( kymographs );
			final double[] timeMinMax = KymographsAnalysis.timeMinMax( kymographs );
			final int width = ( int ) ( 1.1 * positionMinMax[ 1 ] / spaceInterval );
			final int height = ( int ) ( 1.1 * timeMinMax[ 1 ] / timeInterval );
			final int nslices = 1;
			final int nframes = 1;
			final double[] calibration = new double[] { spaceInterval, timeInterval, 1. };
			imp = ViewUtils.makeEmptyImagePlus( width, height, nslices, nframes, calibration );
			imp.getCalibration().setXUnit( kymographs.getSpaceUnits() );
			imp.getCalibration().setYUnit( kymographs.getTimeUnits() );
		}
		else
		{
			imp = IJ.openImage( imagePath.getAbsolutePath() );
		}
		imp.show();
		new KymographTracingController( imp, kymographs );
	}

	public static void load( final ImagePlus imp ) throws IOException
	{
		fileChooser.setSelectedFile( proposeJsonFile( imp ) );
		fileChooser.setDialogTitle( "Load from a JSon file" );
		final int returnVal = fileChooser.showOpenDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			final File selectedFile = fileChooser.getSelectedFile();
			load( selectedFile, imp );
		}
	}

	public static void load( final File kymographFile, final ImagePlus imp ) throws IOException
	{
		final String str = new String( Files.readAllBytes( kymographFile.toPath() ) );
		final Kymographs kymographs = KymographsIO.fromJson( str );
		new KymographTracingController( imp, kymographs );
	}

	private void save( final Kymographs kymographs, final Component parent )
	{
		fileChooser.setDialogTitle( "Save to a JSon file" );
		final int returnVal = fileChooser.showSaveDialog( parent );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			final String json = KymographsIO.toJson( kymographs );
			final byte[] strToBytes = json.getBytes();
			final Path path = fileChooser.getSelectedFile().toPath();
			try
			{
				Files.write( path, strToBytes );
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	private void preview( final ImagePlus imp, final double sigma )
	{
		final int channel = imp.getChannel() - 1;
		final int z = imp.getSlice() - 1;
		final int frame = imp.getFrame() - 1;

		@SuppressWarnings( "rawtypes" )
		final ImgPlus img = TMUtils.rawWraps( imp );
		@SuppressWarnings( { "unchecked" } )
		final Img< DoubleType > filtered = KymographTracer.filterSlice( img, channel, z, frame, sigma );
		ImageJFunctions.show( filtered, "Filtered_" + imp.getShortTitle() + "_Sigma_" + sigma );
	}

	private static final File proposeJsonFile( final ImagePlus imp )
	{
		File folder;
		final String fileName;
		if ( null != imp.getOriginalFileInfo() && null != imp.getOriginalFileInfo().directory )
		{
			final String directory = imp.getOriginalFileInfo().directory;
			folder = Paths.get( directory ).toAbsolutePath().toFile();

			if ( null != imp.getOriginalFileInfo().fileName )
			{
				final int i = imp.getOriginalFileInfo().fileName.lastIndexOf( '.' );
				fileName = imp.getOriginalFileInfo().fileName.substring( 0, i ) + ".json";
			}
			else
			{
				fileName = "Kymographs.json";
			}
		}
		else
		{
			folder = new File( System.getProperty( "user.dir" ) );
			fileName = "Kymographs.json";
		}
		return new File( folder, fileName );
	}
}
