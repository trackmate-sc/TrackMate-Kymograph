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

	public KymographOverlay( final Kymographs model, final ImagePlus imp )
	{
		super( 0, 0, imp );
		this.model = model;
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
		double xs = toX( first, xcorner, magnification );
		double ys = toY( first, ycorner, magnification );
		if ( path.getCurrentPoint() == null )
			path.moveTo( xs, ys );
		else
			path.lineTo( xs, ys );

		g2d.fill( knot( xs, ys, RADIUS * magnification ) );
		while ( itSegment.hasNext() )
		{
			final RealLocalizable next = itSegment.next();
			xs = toX( next, xcorner, magnification );
			ys = toY( next, ycorner, magnification );
			path.lineTo( xs, ys );
		}
	}

	private static final Shape knot( final double xs, final double ys, final double radius )
	{
		return new Ellipse2D.Double( xs - radius, ys - radius, 2. * radius, 2. * radius );
	}

	private static final double toX( final RealLocalizable p, final int xcorner, final double magnification )
	{
		final double xp = p.getDoublePosition( 0 ) + 0.5;
		return ( xp - xcorner ) * magnification;
	}

	private static final double toY( final RealLocalizable p, final int ycorner, final double magnification )
	{
		final double yp = p.getDoublePosition( 1 ) + 0.5;
		return ( yp - ycorner ) * magnification;
	}
}
