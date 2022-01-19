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
package fiji.plugin.trackmate.kymograph.tracing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Kymograph;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Segment;
import fiji.plugin.trackmate.visualization.GlasbeyLut;
import ij.ImagePlus;
import ij.gui.Roi;
import net.imglib2.RealLocalizable;

public class KymographOverlay extends Roi
{

	private static final long serialVersionUID = 1L;

	private static final double RADIUS = 0.7;

	private final Kymographs model;

	private final double spaceInterval;

	private final double timeInterval;

	public KymographOverlay( final Kymographs model, final ImagePlus imp )
	{
		super( 0, 0, imp );
		this.model = model;
		this.spaceInterval = imp.getCalibration().pixelWidth;
		this.timeInterval = imp.getCalibration().pixelHeight;
		model.listeners().add( () -> imp.updateAndDraw() );
		model.selectionListeners().add( () -> imp.updateAndDraw() );
	}

	@Override
	public void drawOverlay( final Graphics g )
	{
		final int xcorner = ic.offScreenX( 0 );
		final int ycorner = ic.offScreenY( 0 );
		final double magnification = getMagnification();

		final Graphics2D g2d = ( Graphics2D ) g;
		g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.8f ) );
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.setStroke( new BasicStroke( 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );

		GlasbeyLut.reset();
		for ( final Kymograph kymograph : model )
		{
			g2d.setColor( GlasbeyLut.next() );
			final Path2D.Double path = new Path2D.Double();
			for ( final Segment segment : kymograph )
				paintSegment( g2d, segment, path, xcorner, ycorner, magnification );

			g2d.draw( path );
			final Point2D p = path.getCurrentPoint();
			if ( p != null )
				g2d.fill( knot( p.getX(), p.getY(), RADIUS * magnification ) );
		}

		/*
		 * Paint selection.
		 */

		g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 1f ) );
		g2d.setStroke( new BasicStroke( 4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
		g2d.setColor( Color.GREEN );

		final Path2D.Double path = new Path2D.Double();
		for ( final Segment segment : model.getSelection() )
			paintSegment( g2d, segment, path, xcorner, ycorner, magnification );

		g2d.draw( path );
		final Point2D p = path.getCurrentPoint();
		if ( p != null )
			g2d.fill( knot( p.getX(), p.getY(), RADIUS * magnification ) );
	}

	private void paintSegment(
			final Graphics2D g2d,
			final Segment segment,
			final Path2D path,
			final int xcorner,
			final int ycorner,
			final double magnification )
	{
		final Iterator< RealLocalizable > itSegment = segment.iterator();
		if ( !itSegment.hasNext() )
			return;

		final RealLocalizable first = itSegment.next();
		double xs = toPosition( first, xcorner, magnification, spaceInterval );
		double ys = toTime( first, ycorner, magnification, timeInterval );
		if ( path.getCurrentPoint() == null )
			path.moveTo( xs, ys );
		else
			path.lineTo( xs, ys );

		g2d.fill( knot( xs, ys, RADIUS * magnification ) );
		while ( itSegment.hasNext() )
		{
			final RealLocalizable next = itSegment.next();
			xs = toPosition( next, xcorner, magnification, spaceInterval );
			ys = toTime( next, ycorner, magnification, timeInterval );
			path.lineTo( xs, ys );
		}
	}

	private static final Shape knot( final double xs, final double ys, final double radius )
	{
		return new Ellipse2D.Double( xs - radius, ys - radius, 2. * radius, 2. * radius );
	}

	private static final double toTime( final RealLocalizable p, final int ycorner, final double magnification, final double timeInterval )
	{
		final double yp = p.getDoublePosition( 0 ) / timeInterval + 0.5;
		return ( yp - ycorner ) * magnification;
	}

	private static final double toPosition( final RealLocalizable p, final int xcorner, final double magnification, final double spaceInterval )
	{
		final double xp = p.getDoublePosition( 1 ) / spaceInterval + 0.5;
		return ( xp - xcorner ) * magnification;
	}
}
