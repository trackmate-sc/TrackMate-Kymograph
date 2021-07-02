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

package fiji.plugin.trackmate.kymograph.path;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import ij.ImagePlus;

@SuppressWarnings( "serial" )
class TracerCanvas extends MultiDThreePanesCanvas
{

	protected boolean just_near_slices = false;

	protected int eitherSide;

	private final ArrayList< SearchInterface > searchThreads = new ArrayList<>();

	private double nodeSize = -1;

	private int[] transparencies; // in percentage, [0]: default; [1]: out of
									// bounds

	public TracerCanvas( final ImagePlus imagePlus, final PaneOwner owner,
			final int plane )
	{

		super( imagePlus, owner, plane );
	}

	public void addSearchThread( final SearchInterface s )
	{
		synchronized ( searchThreads )
		{
			searchThreads.add( s );
		}
	}

	public void removeSearchThread( final SearchInterface s )
	{
		synchronized ( searchThreads )
		{
			int index = -1;
			for ( int i = 0; i < searchThreads.size(); ++i )
			{
				final SearchInterface inList = searchThreads.get( i );
				if ( s == inList )
					index = i;
			}
			if ( index >= 0 )
				searchThreads.remove( index );
		}
	}

	@Override
	protected void drawOverlay( final Graphics2D g )
	{
		// render crosshairs, cursor text and canvas label
		super.drawOverlay( g );
		final int current_z = imp.getZ() - 1;
		synchronized ( searchThreads )
		{
			for ( final SearchInterface st : searchThreads )
				st.drawProgressOnSlice( plane, current_z, this, g );
		}
	}

	/* Keep another Graphics for double-buffering... */

	private int backBufferWidth;

	private int backBufferHeight;

	private Graphics2D backBufferGraphics;

	private Image backBufferImage;

	protected void resetBackBuffer()
	{

		if ( backBufferGraphics != null )
		{
			backBufferGraphics.dispose();
			backBufferGraphics = null;
		}

		if ( backBufferImage != null )
		{
			backBufferImage.flush();
			backBufferImage = null;
		}

		backBufferWidth = getSize().width;
		backBufferHeight = getSize().height;

		if ( backBufferWidth > 0 && backBufferHeight > 0 )
		{
			backBufferImage = createImage( backBufferWidth, backBufferHeight );
			backBufferGraphics = getGraphics2D( backBufferImage.getGraphics() );
		}
	}

	@Override
	public void paint( final Graphics g )
	{

		if ( backBufferWidth != getSize().width ||
				backBufferHeight != getSize().height || backBufferImage == null ||
				backBufferGraphics == null )
			resetBackBuffer();

		super.paint( backBufferGraphics );
		drawOverlay( backBufferGraphics );
		g.drawImage( backBufferImage, 0, 0, this );
	}

	/**
	 * Returns the MultiDThreePanes plane associated with this canvas.
	 *
	 * @return Either MultiDThreePanes.XY_PLANE, XZ_PLANE, or ZY_PLANE
	 */
	public int getPlane()
	{
		return super.plane;
	}

	/**
	 * Returns the diameter of path nodes rendered at current magnification.
	 *
	 * @return the baseline rendering diameter of a path node
	 */
	public double nodeDiameter()
	{
		if ( nodeSize < 0 )
		{
			if ( magnification < 4 )
				return 2;
			else if ( magnification > 16 )
				return magnification / 2;
			else
				return magnification;
		}
		return nodeSize;
	}

	/**
	 * Sets the baseline for rendering diameter of path nodes
	 *
	 * @param diameter
	 *            the diameter to be used when rendering path nodes. Set it to
	 *            -1 for adopting the default value. Set it to zero to suppress
	 *            node rendering
	 */
	public void setNodeDiameter( final double diameter )
	{
		nodeSize = diameter;
	}

	protected void setDefaultTransparency( final int percentage )
	{
		if ( transparencies == null )
			transparencies = new int[] { percentage, 50 };
		else
			transparencies[ 0 ] = percentage;
	}

	protected void setOutOfBoundsTransparency( final int percentage )
	{
		if ( transparencies == null )
			transparencies = new int[] { 100, percentage };
		else
			transparencies[ 1 ] = percentage;
	}

	protected int getDefaultTransparency()
	{ // in percentage
		return ( transparencies == null ) ? 100 : transparencies[ 0 ];
	}

	protected int getOutOfBoundsTransparency()
	{ // in percentage
		return ( transparencies == null ) ? 50 : transparencies[ 1 ];
	}

}
