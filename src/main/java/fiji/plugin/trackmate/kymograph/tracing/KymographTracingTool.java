package fiji.plugin.trackmate.kymograph.tracing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.atomic.AtomicInteger;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Builder;
import fiji.plugin.trackmate.kymograph.tracing.astar.Path;
import fiji.tool.AbstractTool;
import ij.ImagePlus;
import net.imglib2.Localizable;

public class KymographTracingTool extends AbstractTool implements MouseListener, MouseMotionListener
{

	private static final String KYMOGRAPH_BASE_NAME = "Kymograph";

	private static final String SEGMENT_BASE_NAME = "Segment";

	private final AtomicInteger kymographId = new AtomicInteger( 0 );

	private final AtomicInteger segmentId = new AtomicInteger( 0 );

	private final Builder modelBuilder;

	private Logger logger = Logger.VOID_LOGGER;

	private final KymographTracer tracer;

	private final double timeInterval;

	private final double spaceInterval;

	public KymographTracingTool( final ImagePlus imp, final Kymographs model, final KymographTracer tracer )
	{
		super();
		this.tracer = tracer;
		this.modelBuilder = model.add();
		// Extract physical calibration. We expect time to be along Y.
		this.timeInterval = imp.getCalibration().pixelHeight;
		this.spaceInterval = imp.getCalibration().pixelWidth;
		kymographId.set( model.size() );
		run( null );
		super.registerTool( imp );
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final int x = getOffscreenX( e );
		final int y = getOffscreenY( e );

		if ( tracer.isTracing() )
		{
			if ( e.getClickCount() > 1 )
			{
				tracer.finishPath();
				tracer.getImp().updateAndDraw();
				logger.log( "Finished kymograph." );
				modelBuilder.done();
				return;
			}
			
			final Path segment = tracer.addSegment( x, y );
			modelBuilder.segment( SEGMENT_BASE_NAME + "_" + kymographId.get() + "_" + segmentId.incrementAndGet() );
			for ( final Localizable point : segment )
			{
				// Scale to physical units.
				final double xk = point.getDoublePosition( 0 ) * spaceInterval;
				final double tk = point.getDoublePosition( 1 ) * timeInterval;
				modelBuilder.point( tk, xk );
			}
			logger.log( "Added segment to kymograph." );

			tracer.getImp().updateAndDraw();
			return;
		}

		final ImagePlus imp = tracer.getImp();
		final int channel = imp.getChannel() - 1;
		final int z = imp.getSlice() - 1;
		final int frame = imp.getFrame() - 1;
		segmentId.set( 0 );
		modelBuilder.kymograph( KYMOGRAPH_BASE_NAME + "_" + kymographId.incrementAndGet() );
		logger.log( "Starting a new kymograph." );
		tracer.startPath( x, y, channel, z, frame );
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		if ( !tracer.isTracing() )
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

	/**
	 * Prevents registration on other images.
	 */
	@Override
	protected void registerTool()
	{}

	/**
	 * Prevents registration on other images.
	 */
	@Override
	protected void registerTool( final ImagePlus image )
	{}

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
	public String getToolName()
	{
		return "Kymograph tracer";
	}

	@Override
	public String getToolIcon()
	{
		return "C493D4cD5cD6bD7aD8aDbaDcbDccCfffD5dCf22D38D39D46D47D55Dd6De7De8C16cD19D1aD1bD68D78D88D97Da6C"
				+ "bcaD2dD3dD4dD6cD89Db9DbbDcaC26aD6eD7eD8eD9eDaeDbeDceDdeDeeCf66D37D48D63D82D83D92D93Db3C59"
				+ "eD33D34D35D44D57D58D67D79D87D96Db5Db6Dc5Dd5De5CcdeD21D22D23D25D2cD32D3aD7dD8bD98Da5DadDc3"
				+ "DcdDd4DedC7b7D3cD5bD7bD99D9aDa9DaaDdcCe45D56D64D73Da3Db4Dc4Dd7De9C37bD11D12D13D14D15D16D1"
				+ "7D18CeddD28D2bD6aD74Da2Da4Dc6DeaC36bD1cD1dD1eD2eD3eD45D4eD5eCdaaD29D2aD54D65D72Da7DddDe6";
	}

	public void setLogger( final Logger logger )
	{
		this.logger = logger;
	}
}