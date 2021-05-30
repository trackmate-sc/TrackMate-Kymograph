package fiji.plugin.trackmate.kymograph.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.kymograph.KymographAlignment;
import fiji.plugin.trackmate.kymograph.KymographCreationParams;
import fiji.plugin.trackmate.kymograph.KymographProjectionMethod;

public class KymographCreatorPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	final JButton btnCreate;

	private final JComboBox< Integer > cmbboxTrack1;

	private final JComboBox< Integer > cmbboxTrack2;

	private final JFormattedTextField ftfThickness;

	private final JComboBox< KymographProjectionMethod > cmbboxProjection;

	private final JComboBox< KymographAlignment > cmbboxAlignment;

	public KymographCreatorPanel( final TrackSelectorUI trackSelectorUI )
	{
		cmbboxTrack1 = trackSelectorUI.create();
		cmbboxTrack2 = trackSelectorUI.create();
		
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 36, 0, 0, 0, 0, 0, 39, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblTitle = new JLabel( "Kymograph creation" );
		lblTitle.setFont( Fonts.BIG_FONT );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 2;
		gbcLblTitle.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		add( lblTitle, gbcLblTitle );

		final JLabel lblTrack1 = new JLabel( "First track" );
		final GridBagConstraints gbcLblTrack1 = new GridBagConstraints();
		gbcLblTrack1.anchor = GridBagConstraints.EAST;
		gbcLblTrack1.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTrack1.gridx = 0;
		gbcLblTrack1.gridy = 1;
		add( lblTrack1, gbcLblTrack1 );

		final GridBagConstraints gbcCmbboxTrack1 = new GridBagConstraints();
		gbcCmbboxTrack1.anchor = GridBagConstraints.EAST;
		gbcCmbboxTrack1.insets = new Insets( 5, 5, 5, 5 );
		gbcCmbboxTrack1.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxTrack1.gridx = 1;
		gbcCmbboxTrack1.gridy = 1;
		add( cmbboxTrack1, gbcCmbboxTrack1 );

		final JLabel lblTrack2 = new JLabel( "Second track" );
		final GridBagConstraints gbcLblTrack2 = new GridBagConstraints();
		gbcLblTrack2.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTrack2.anchor = GridBagConstraints.EAST;
		gbcLblTrack2.gridx = 0;
		gbcLblTrack2.gridy = 2;
		add( lblTrack2, gbcLblTrack2 );

		final GridBagConstraints gbcCmbboxTrack2 = new GridBagConstraints();
		gbcCmbboxTrack2.anchor = GridBagConstraints.EAST;
		gbcCmbboxTrack2.insets = new Insets( 5, 5, 5, 5 );
		gbcCmbboxTrack2.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxTrack2.gridx = 1;
		gbcCmbboxTrack2.gridy = 2;
		add( cmbboxTrack2, gbcCmbboxTrack2 );

		final JLabel lblThickness = new JLabel( "Kymograph thickness" );
		final GridBagConstraints gbcLblThickness = new GridBagConstraints();
		gbcLblThickness.insets = new Insets( 5, 5, 5, 5 );
		gbcLblThickness.anchor = GridBagConstraints.EAST;
		gbcLblThickness.gridx = 0;
		gbcLblThickness.gridy = 3;
		add( lblThickness, gbcLblThickness );

		ftfThickness = new JFormattedTextField( new Integer( KymographCreationParams.create().get().thickness ) );
		ftfThickness.setHorizontalAlignment( SwingConstants.CENTER );
		GuiUtils.selectAllOnFocus( ftfThickness );
		final GridBagConstraints gbcFtfThickness = new GridBagConstraints();
		gbcFtfThickness.insets = new Insets( 5, 5, 5, 5 );
		gbcFtfThickness.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfThickness.gridx = 1;
		gbcFtfThickness.gridy = 3;
		add( ftfThickness, gbcFtfThickness );

		final JLabel lblProjectionMethod = new JLabel( "Projection method" );
		final GridBagConstraints gbcLblProjectionMethod = new GridBagConstraints();
		gbcLblProjectionMethod.anchor = GridBagConstraints.EAST;
		gbcLblProjectionMethod.insets = new Insets( 5, 5, 5, 5 );
		gbcLblProjectionMethod.gridx = 0;
		gbcLblProjectionMethod.gridy = 4;
		add( lblProjectionMethod, gbcLblProjectionMethod );

		cmbboxProjection = new JComboBox<>( new Vector<>( Arrays.asList( KymographProjectionMethod.values() ) ) );
		cmbboxProjection.setSelectedItem( KymographCreationParams.create().get().projectionMethod );
		final GridBagConstraints gbcCmbboxProjection = new GridBagConstraints();
		gbcCmbboxProjection.insets = new Insets( 5, 5, 5, 5 );
		gbcCmbboxProjection.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxProjection.gridx = 1;
		gbcCmbboxProjection.gridy = 4;
		add( cmbboxProjection, gbcCmbboxProjection );

		final JLabel lblAlignment = new JLabel( "Alignment" );
		final GridBagConstraints gbcLblAlignment = new GridBagConstraints();
		gbcLblAlignment.anchor = GridBagConstraints.EAST;
		gbcLblAlignment.insets = new Insets( 5, 5, 5, 5 );
		gbcLblAlignment.gridx = 0;
		gbcLblAlignment.gridy = 5;
		add( lblAlignment, gbcLblAlignment );

		cmbboxAlignment = new JComboBox<>( new Vector<>( Arrays.asList( KymographAlignment.values() ) ) );
		cmbboxAlignment.setSelectedItem( KymographCreationParams.create().get().alignment );
		final GridBagConstraints gbcCmbboxAlignment = new GridBagConstraints();
		gbcCmbboxAlignment.insets = new Insets( 5, 5, 5, 5 );
		gbcCmbboxAlignment.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxAlignment.gridx = 1;
		gbcCmbboxAlignment.gridy = 5;
		add( cmbboxAlignment, gbcCmbboxAlignment );

		btnCreate = new JButton( "Create" );
		final GridBagConstraints gbcBtnCreate = new GridBagConstraints();
		gbcBtnCreate.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnCreate.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnCreate.gridx = 1;
		gbcBtnCreate.gridy = 6;
		add( btnCreate, gbcBtnCreate );
	}
	
	public KymographCreationParams getKymographCreationParams()
	{
		return KymographCreationParams.create()
				.trackID1( ( Integer ) cmbboxTrack1.getSelectedItem() )
				.trackID2( ( Integer ) cmbboxTrack2.getSelectedItem() )
				.thickness( ( ( Number ) ftfThickness.getValue() ).intValue() )
				.projectionMethod( ( KymographProjectionMethod ) cmbboxProjection.getSelectedItem() )
				.alignment( ( KymographAlignment ) cmbboxAlignment.getSelectedItem() )
				.get();
	}
}
