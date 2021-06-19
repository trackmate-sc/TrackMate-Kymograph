package fiji.plugin.trackmate.kymograph.tracing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import fiji.tool.AbstractTool;
import fiji.tool.ToolWithOptions;
import ij.ImagePlus;

public class KymographTracingTool extends AbstractTool implements MouseListener, MouseMotionListener, ToolWithOptions
{

	private final Map< ImagePlus, KymographTracing > toolmap = new HashMap<>();


	private KymographTracing getTracer( final MouseEvent e )
	{
		final ImagePlus imp = getImagePlus( e );
		if ( imp == null )
			return null;
		
		KymographTracing tracer = toolmap.get( imp );
		if ( null == tracer )
		{
			tracer = new KymographTracing( imp );
			toolmap.put( imp, tracer );
		}
		return tracer;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final KymographTracing tracer = getTracer( e );
		if ( null == tracer )
			return;

		final int x = getOffscreenX( e );
		final int y = getOffscreenY( e );

		if ( tracer.isTracing() )
		{
			if ( e.getClickCount() > 1 )
			{
				tracer.finishPath();
				tracer.getImp().updateAndDraw();
				return;
			}
			
			tracer.addSegment( x, y );
			tracer.getImp().updateAndDraw();
			return;
		}

		final ImagePlus imp = tracer.getImp();
		final int channel = imp.getChannel() - 1;
		final int z = imp.getSlice() - 1;
		final int frame = imp.getFrame() - 1;
		tracer.startPath( x, y, channel, z, frame );
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		final KymographTracing tracer = getTracer( e );
		if ( null == tracer || !tracer.isTracing() )
			return;

		if ( !e.isShiftDown() )
		{
			tracer.clearPreview();
			tracer.getImp().updateAndDraw();
			return;
		}
		

		final int x = getOffscreenX( e );
		final int y = getOffscreenY( e );
		tracer.previewSegment( x, y );
		tracer.getImp().updateAndDraw();
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mouseDragged( final MouseEvent e )
	{}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void showOptionDialog()
	{
		System.out.println( "Show dialog" ); // DEBUG
	}

	@Override
	public String getToolName()
	{
		return "Kymograph tracing";
	}

	@Override
	public String getToolIcon()
	{
		return "C000Pdaa79796a6c4c2a1613215276998a6a70";
	}

}