package fiji.plugin.trackmate.kymograph;

import static fiji.plugin.trackmate.gui.Icons.TRACK_SCHEME_ICON_16x16;

import java.awt.Frame;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.AbstractTMAction;
import fiji.plugin.trackmate.action.TrackMateAction;
import fiji.plugin.trackmate.action.TrackMateActionFactory;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.kymograph.ui.KymographCreatorController;

public class KymographAction extends AbstractTMAction
{

	public static final String INFO_TEXT = "<html>"
			+ "This action allows for extracting moving kymographs. Kymographs are "
			+ "2D images built from intensity collected along a line and laid on the X-axis, "
			+ "for each time-point, laid on the Y-axis."
			+ "<p>"
			+ "The intensity of all channel of the source image is collected along "
			+ "a line that joins two tracks, that you can specify. As the spots in the "
			+ "tracks move, the kymograph follows them. This is handy if you need to "
			+ "plot the kymograph along a linear structure that moves over time. To get "
			+ "such a kymograph, create two tracks, one for each extremity of the structure "
			+ "and use this action."
			+ "<p>"
			+ "The extraction works in 2D and in 3D. The <i>thickness</i> parameter lets "
			+ "you configure how many pixels around the lined joining the two tracks "
			+ "should be projected. The <i>projection method</i> specifies how to project "
			+ "them. Because the line in each time-point might have a different length, "
			+ "we must specify how to align them, which is done with the <i>alignment</i> "
			+ "parameter. "
			+ "<p>"
			+ "If spots are missing in the designated tracks, they will produce a black "
			+ "line in the final kymograph. "
			+ "</html>";

	public static final String KEY = "KYMOGRAPH_EXTRACTOR";

	public static final String NAME = "Moving kymographs";

	private KymographCreatorController controller;

	@Override
	public void execute(
			final TrackMate trackmate,
			final SelectionModel selectionModel,
			final DisplaySettings displaySettings,
			final Frame parent )
	{
		if ( controller == null )
			controller = new KymographCreatorController( trackmate.getModel(), trackmate.getSettings().imp );
		controller.showUI();
	}

	@Plugin( type = TrackMateActionFactory.class )
	public static class Factory implements TrackMateActionFactory
	{

		private final KymographAction action = new KymographAction();

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public TrackMateAction create()
		{
			return action;
		}

		@Override
		public ImageIcon getIcon()
		{
			return TRACK_SCHEME_ICON_16x16;
		}

		@Override
		public String getName()
		{
			return NAME;
		}
	}
}
