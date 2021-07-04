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
package fiji.plugin.trackmate.kymograph.tracing;

import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
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

	private static KymographTracingTool instance;

	private final Map< ImagePlus, Bundle > bundles;

	/**
	 * Return the singleton instance for this tool. If it was not previously
	 * instantiated, this calls instantiates it.
	 */
	public static KymographTracingTool getInstance()
	{
		if ( null == instance )
			instance = new KymographTracingTool();
		return instance;
	}

	private KymographTracingTool()
	{
		this.bundles = new HashMap<>();
		run( null );
	}

	public void register( final ImagePlus imp, final Kymographs model, final KymographTracer tracer, final Logger logger )
	{
		final Bundle bundle = new Bundle( imp, model, tracer, logger );
		bundles.put( imp, bundle );
		super.registerTool( imp );
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final Bundle bundle = getBundle( e );
		if ( bundle == null )
			return;

		final int x = getOffscreenX( e );
		final int y = getOffscreenY( e );

		if ( bundle.tracer.isTracing() )
		{
			if ( e.getClickCount() > 1 )
			{
				bundle.tracer.finishPath();
				bundle.tracer.getImp().updateAndDraw();
				bundle.logger.log( "Finished kymograph." );
				bundle.modelBuilder.done();
				return;
			}
			
			final Path segment = bundle.tracer.addSegment( x, y );
			bundle.modelBuilder.segment( SEGMENT_BASE_NAME + "_" + bundle.kymographId.get() + "_" + bundle.segmentId.incrementAndGet() );
			for ( final Localizable point : segment )
			{
				// Scale to physical units.
				final double xk = point.getDoublePosition( 0 ) * bundle.spaceInterval;
				final double tk = point.getDoublePosition( 1 ) * bundle.timeInterval;
				bundle.modelBuilder.point( tk, xk );
			}
			bundle.logger.log( "Added segment to kymograph." );

			bundle.tracer.getImp().updateAndDraw();
			return;
		}

		final ImagePlus imp = bundle.tracer.getImp();
		final int channel = imp.getChannel() - 1;
		final int z = imp.getSlice() - 1;
		final int frame = imp.getFrame() - 1;
		bundle.segmentId.set( 0 );
		bundle.modelBuilder.kymograph( KYMOGRAPH_BASE_NAME + "_" + bundle.kymographId.incrementAndGet() );
		bundle.logger.log( "Starting a new kymograph." );
		bundle.tracer.startPath( x, y, channel, z, frame );
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		final Bundle bundle = getBundle( e );
		if ( bundle == null )
			return;

		if ( !bundle.tracer.isTracing() )
			return;

		if ( !e.isShiftDown() )
		{
			bundle.tracer.clearPreview();
			bundle.tracer.getImp().updateAndDraw();
			return;
		}

		final int x = getOffscreenX( e );
		final int y = getOffscreenY( e );
		bundle.tracer.previewSegment( x, y );
		bundle.tracer.getImp().updateAndDraw();
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

	private Bundle getBundle( final ComponentEvent e )
	{
		final ImagePlus imp = getImagePlus( e );
		if ( imp == null )
			return null;
		return bundles.get( imp );
	}

	private static class Bundle
	{

		private final KymographTracer tracer;

		private final double timeInterval;

		private final double spaceInterval;

		private final AtomicInteger kymographId = new AtomicInteger( 0 );

		private final AtomicInteger segmentId = new AtomicInteger( 0 );

		private final Builder modelBuilder;

		private final Logger logger;

		private Bundle( final ImagePlus imp, final Kymographs model, final KymographTracer tracer, final Logger logger )
		{
			this.tracer = tracer;
			this.logger = logger;
			this.modelBuilder = model.add();
			this.timeInterval = imp.getCalibration().pixelHeight;
			this.spaceInterval = imp.getCalibration().pixelWidth;
			kymographId.set( model.size() );
		}
	}
}
