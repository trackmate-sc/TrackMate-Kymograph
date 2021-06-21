package fiji.plugin.trackmate.kymograph.tracing.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import fiji.plugin.trackmate.util.TMUtils;
import ij.ImagePlus;
import ij.gui.Overlay;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.DoubleType;

public class KymographTracingController
{

	private static JFileChooser fileChooser = new JFileChooser();

	public KymographTracingController( final ImagePlus imp )
	{
		this( imp, new Kymographs( imp.getTitle() ) );
	}

	public KymographTracingController( final ImagePlus imp, final Kymographs kymographs )
	{
		assert imp != null;

		// Display overlay.
		imp.setOverlay( new Overlay() );
		imp.getOverlay().add( new KymographOverlay( kymographs, imp ) );

		// Tracing parameters.
		final TracingParameters tracingParameters = new TracingParameters();

		// Tracer.
		final KymographTracer tracer = new KymographTracer( imp, tracingParameters );

		// UI.
		final KymographTracingPanel panel = new KymographTracingPanel( kymographs, tracingParameters );
		final JFrame frame = new JFrame( "Kymographs of " + imp.getShortTitle() );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( panel );
		frame.setSize( 400, 400 );
		GuiUtils.positionWindow( frame, imp.getWindow() );
		frame.setVisible( true );

		// Tracking tool.
		final KymographTracingTool tool = new KymographTracingTool( imp, kymographs, tracer );
		tool.setLogger( panel.getLogger() );

		// Wire some listeners.
		panel.btnPreview.addActionListener( e -> SwingUtilities.invokeLater( () -> preview( imp, tracingParameters.getSigma() ) ) );
		panel.btnSave.addActionListener( e -> save( proposeJsonFile( imp ), frame ) );
	}

	public static void load( final File kymographFile, final ImagePlus imp )
	{
		fileChooser.setFileFilter( new FileNameExtensionFilter( "JSon files:", ".json" ) );
		fileChooser.setSelectedFile( kymographFile );
		fileChooser.setDialogTitle( "Load from a JSon file" );
		final int returnVal = fileChooser.showOpenDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			final File selectedFile = fileChooser.getSelectedFile();
			try
			{
				final String str = new String( Files.readAllBytes( selectedFile.toPath() ) );
				final Kymographs kymographs = KymographsIO.fromJson( str );
				new KymographTracingController( imp, kymographs );
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	private void save( final File file, final Component parent )
	{
		// TODO Auto-generated method stub
		System.out.println( "TODO save" ); // DEBUG
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
