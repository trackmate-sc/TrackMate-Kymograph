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
package fiji.plugin.trackmate.kymograph.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.kymograph.KymographCreationParams;
import fiji.plugin.trackmate.kymograph.KymographCreator;
import fiji.plugin.trackmate.kymograph.RegisteredImageCreator;
import fiji.plugin.trackmate.kymograph.tracing.ui.KymographTracingController;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.RotatedRectRoi;
import ij.measure.Calibration;

public class KymographCreatorController
{

	private JDialog dialog;

	private final KymographCreator kymographCreator;

	private final Model model;

	private final ImagePlus imp;

	private final RegisteredImageCreator registeredImageCreateor;

	public KymographCreatorController( final Model model, final ImagePlus imp )
	{
		this.model = model;
		this.imp = imp;
		final KymographCreationParams params = KymographCreationParams.create().get();
		this.kymographCreator = new KymographCreator( model, imp, params );
		this.registeredImageCreateor = new RegisteredImageCreator( model, imp, params );
	}

	public void showUI()
	{
		if ( null == dialog )
		{
			dialog = new JDialog();
			dialog.setLocationByPlatform( true );
			dialog.setLocationRelativeTo( null );
			dialog.setTitle( "TrackMate Kymograph" );
			dialog.setIconImage( Icons.TRACKMATE_ICON.getImage() );

			final TrackSelectorUI selectorUI = new TrackSelectorUI( model );
			model.addModelChangeListener( selectorUI );
			final KymographCreatorPanel panel = new KymographCreatorPanel( selectorUI );
			dialog.getContentPane().add( panel );
			dialog.pack();

			panel.btnCreate.addActionListener( e -> createKymograph( panel.getKymographCreationParams() ) );
			panel.btnLength.addActionListener( e -> plotKymograhPlength( panel.getKymographCreationParams() ) );
			panel.btnClearOverlay.addActionListener( e -> clearOverlay() );
			panel.btnImg.addActionListener( e -> createRegisteredImage( panel.getKymographCreationParams() ) );
		}
		dialog.setVisible( true );
	}

	private void plotKymograhPlength( final KymographCreationParams params )
	{
		final Calibration cal = imp.getCalibration();
		final JFrame frame = KymographUtils.plotKymographLength( model, params, cal.getUnit(), cal.getTimeUnit() );
		frame.setLocationRelativeTo( dialog );
		frame.setVisible( true );
	}

	private void addKymographOverlay( final KymographCreationParams params )
	{
		Overlay overlay = imp.getOverlay();
		if ( overlay == null )
		{
			overlay = new Overlay();
			imp.setOverlay( overlay );
		}

		clearOverlay();
		final int nFrames = imp.getNFrames();
		for ( int tp = 0; tp < nFrames; tp++ )
		{
			final long[] coords1 = KymographUtils.getCoords( model, imp, tp, params.trackID1 );
			if ( coords1 == null )
				continue;

			final long[] coords2 = KymographUtils.getCoords( model, imp, tp, params.trackID2 );
			if ( coords2 == null )
				continue;

			final double x1 = coords1[ 0 ];
			final double y1 = coords1[ 1 ];
			final double x2 = coords2[ 0 ];
			final double y2 = coords2[ 1 ];
			final double rectWidth = params.thickness;
			final RotatedRectRoi roi = new RotatedRectRoi( x1, y1, x2, y2, rectWidth );
			roi.setPosition( 1, imp.getNSlices() / 2 + 1, tp + 1 );
			roi.setName( "TrackMate-Kymograph-tp" + ( tp + 1 ) );
			overlay.add( roi );
		}
		imp.updateAndDraw();
	}

	private void clearOverlay()
	{
		final Overlay overlay = imp.getOverlay();
		if ( overlay == null )
			return;

		final List< Roi > toRemove = new ArrayList<>();
		for ( final Roi roi : overlay )
		{
			if ( roi.getName() == null )
				continue;

			if ( roi.getName().startsWith( "TrackMate-Kymograph-tp" ) )
				toRemove.add( roi );
		}

		for ( final Roi roi : toRemove )
			overlay.remove( roi );
		imp.updateAndDraw();
	}

	private void createKymograph( final KymographCreationParams params )
	{
		model.getLogger().log( "Generating kymograph with the following parameters: " + params.toString() );
		kymographCreator.setParams( params );
		if ( !kymographCreator.checkInput() || !kymographCreator.process() )
		{
			model.getLogger().error( kymographCreator.getErrorMessage() );
			return;
		}
		final ImagePlus out = kymographCreator.getResult();
		out.show();

		// Launch tracing controller.
		new KymographTracingController( out );

		addKymographOverlay( params );

		model.getLogger().log( "\nDone.\n" );
	}

	private void createRegisteredImage( final KymographCreationParams params )
	{
		model.getLogger().log( "Generating registered image with the following parameters: " + params.toString() );
		registeredImageCreateor.setParams( params );
		if ( !registeredImageCreateor.checkInput() || !registeredImageCreateor.process() )
		{
			model.getLogger().error( registeredImageCreateor.getErrorMessage() );
			return;
		}
		final ImagePlus out = registeredImageCreateor.getResult();
		out.show();

		addKymographOverlay( params );

		model.getLogger().log( "\nDone.\n" );
	}
}
