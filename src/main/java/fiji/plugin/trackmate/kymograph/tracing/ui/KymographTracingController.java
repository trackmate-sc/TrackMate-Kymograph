package fiji.plugin.trackmate.kymograph.tracing.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.tracing.KymographOverlay;
import fiji.plugin.trackmate.kymograph.tracing.KymographTracer;
import fiji.plugin.trackmate.kymograph.tracing.KymographTracingTool;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs;
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

	public KymographTracingController( final ImagePlus imp )
	{
		this( imp, new Kymographs() );
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
		panel.btnSave.addActionListener( e -> save() );
		panel.btnLoad.addActionListener( e -> load() );
	}

	private void load()
	{
		// TODO Auto-generated method stub
		System.out.println( "TODO load" ); // DEBUG
	}

	private void save()
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
}
