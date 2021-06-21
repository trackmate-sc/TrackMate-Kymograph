package fiji.plugin.trackmate.kymograph.tracing.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.gui.displaysettings.SliderPanelDouble;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.BoundedDoubleElement;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Kymograph;
import fiji.plugin.trackmate.kymograph.tracing.Kymographs.Segment;
import fiji.plugin.trackmate.kymograph.tracing.TracingParameters;
import fiji.plugin.trackmate.util.JLabelLogger;

public class KymographTracingPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final Logger logger;

	final JButton btnAnalyze;

	final JButton btnSave;

	final JButton btnPreview;

	public KymographTracingPanel( final Kymographs kymographs, final TracingParameters tracingParameters )
	{
		setLayout( new BorderLayout( 0, 0 ) );

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight( 0.5 );
		add( splitPane );

		final JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent( scrollPane );

		/*
		 * The tree component.
		 */

		final JTree tree = new JTree( createTreeModel( kymographs ) );
		tree.setEditable( true );
		scrollPane.setViewportView( tree );

		// Selection listener.
		tree.addTreeSelectionListener( e -> SwingUtilities.invokeLater( () -> {
			if ( e.getNewLeadSelectionPath() != null && e.getNewLeadSelectionPath().getLastPathComponent() != null )
				selected( kymographs,
						( ( DefaultMutableTreeNode ) e.getNewLeadSelectionPath().getLastPathComponent() ).getUserObject() );
		} ) );

		// Key bindings.
		tree.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), DeleteCurrentAction.NAME );
		tree.getActionMap().put( DeleteCurrentAction.NAME, new DeleteCurrentAction( tree, kymographs ) );

		/*
		 * The rest.
		 */

		// Tracing params elements:
		final BoundedDoubleElement sigma = StyleElements.boundedDoubleElement( "Sigma (pixels)", 0.5, 5., tracingParameters::getSigma, tracingParameters::setSigma );
		final BoundedDoubleElement threshold = StyleElements.boundedDoubleElement( "Treshold", 0., 1., tracingParameters::getThreshold, tracingParameters::setThreshold );
		final BoundedDoubleElement penaly = StyleElements.boundedDoubleElement( "Penalty", 0., 100., tracingParameters::getPenalty, tracingParameters::setPenalty );

		final JPanel panelBtns = new JPanel();
		splitPane.setRightComponent( panelBtns );
		final GridBagLayout gblPanelBtns = new GridBagLayout();
		gblPanelBtns.columnWidths = new int[] { 0, 0, 0 };
		gblPanelBtns.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanelBtns.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gblPanelBtns.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelBtns.setLayout( gblPanelBtns );

		final JLabel lblTitle = new JLabel( "Kymograph tracer" );
		lblTitle.setFont( Fonts.BIG_FONT );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 2;
		gbcLblTitle.insets = new Insets( 5, 5, 5, 0 );
		gbcLblTitle.fill = GridBagConstraints.BOTH;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		panelBtns.add( lblTitle, gbcLblTitle );

		final JLabel lblFilterParams = new JLabel( "Filter parameters" );
		lblFilterParams.setFont( lblFilterParams.getFont().deriveFont( lblFilterParams.getFont().getStyle() | Font.BOLD ) );
		lblFilterParams.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblFilterParams = new GridBagConstraints();
		gbcLblFilterParams.gridwidth = 2;
		gbcLblFilterParams.anchor = GridBagConstraints.SOUTH;
		gbcLblFilterParams.insets = new Insets( 5, 5, 5, 0 );
		gbcLblFilterParams.gridx = 0;
		gbcLblFilterParams.gridy = 2;
		panelBtns.add( lblFilterParams, gbcLblFilterParams );

		final JLabel lblFilterSigma = new JLabel( "Filter scale" );
		final GridBagConstraints gbcLblFilterSigma = new GridBagConstraints();
		gbcLblFilterSigma.anchor = GridBagConstraints.EAST;
		gbcLblFilterSigma.insets = new Insets( 0, 5, 5, 5 );
		gbcLblFilterSigma.gridx = 0;
		gbcLblFilterSigma.gridy = 3;
		panelBtns.add( lblFilterSigma, gbcLblFilterSigma );

		final SliderPanelDouble sigmaPanel = StyleElements.linkedSliderPanel( sigma, 4 );
		final GridBagConstraints gbcSigma = new GridBagConstraints();
		gbcSigma.fill = GridBagConstraints.HORIZONTAL;
		gbcSigma.insets = new Insets( 5, 5, 5, 5 );
		gbcSigma.gridx = 1;
		gbcSigma.gridy = 3;
		panelBtns.add( sigmaPanel, gbcSigma );

		btnPreview = new JButton( "Show filtered image", Icons.PREVIEW_ICON );
		final GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridwidth = 2;
		gbc_btnNewButton.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton.insets = new Insets( 5, 5, 5, 0 );
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 4;
		panelBtns.add( btnPreview, gbc_btnNewButton );

		final JLabel lblTracingParams = new JLabel( "Tracing parameters" );
		lblTracingParams.setFont( lblTracingParams.getFont().deriveFont( lblTracingParams.getFont().getStyle() | Font.BOLD ) );
		lblTracingParams.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblTracingParams = new GridBagConstraints();
		gbcLblTracingParams.gridwidth = 2;
		gbcLblTracingParams.anchor = GridBagConstraints.SOUTH;
		gbcLblTracingParams.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTracingParams.insets = new Insets( 5, 5, 5, 0 );
		gbcLblTracingParams.gridx = 0;
		gbcLblTracingParams.gridy = 5;
		panelBtns.add( lblTracingParams, gbcLblTracingParams );

		final JLabel lblThreshold = new JLabel( "Threshold" );
		final GridBagConstraints gbcLblThreshold = new GridBagConstraints();
		gbcLblThreshold.anchor = GridBagConstraints.EAST;
		gbcLblThreshold.insets = new Insets( 0, 0, 5, 5 );
		gbcLblThreshold.gridx = 0;
		gbcLblThreshold.gridy = 6;
		panelBtns.add( lblThreshold, gbcLblThreshold );

		final SliderPanelDouble thresholdPanel = StyleElements.linkedSliderPanel( threshold, 4 );
		final GridBagConstraints gbcTreshold = new GridBagConstraints();
		gbcTreshold.fill = GridBagConstraints.HORIZONTAL;
		gbcTreshold.insets = new Insets( 5, 5, 5, 5 );
		gbcTreshold.gridx = 1;
		gbcTreshold.gridy = 6;
		panelBtns.add( thresholdPanel, gbcTreshold );

		final JLabel lblPenalty = new JLabel( "Penalty" );
		final GridBagConstraints gbcLblPenalty = new GridBagConstraints();
		gbcLblPenalty.anchor = GridBagConstraints.EAST;
		gbcLblPenalty.insets = new Insets( 0, 0, 5, 5 );
		gbcLblPenalty.gridx = 0;
		gbcLblPenalty.gridy = 7;
		panelBtns.add( lblPenalty, gbcLblPenalty );

		final SliderPanelDouble penaltyPanel = StyleElements.linkedSliderPanel( penaly, 5 );
		final GridBagConstraints gbcPenalty = new GridBagConstraints();
		gbcPenalty.fill = GridBagConstraints.HORIZONTAL;
		gbcPenalty.insets = new Insets( 5, 5, 5, 5 );
		gbcPenalty.gridx = 1;
		gbcPenalty.gridy = 7;
		panelBtns.add( penaltyPanel, gbcPenalty );

		/*
		 * Logger.
		 */

		final JLabelLogger lblLog = new JLabelLogger();
		lblLog.setText( " " );
		this.logger = lblLog.getLogger();

		final GridBagConstraints gbcLblLog = new GridBagConstraints();
		gbcLblLog.gridwidth = 2;
		gbcLblLog.insets = new Insets( 5, 5, 5, 0 );
		gbcLblLog.fill = GridBagConstraints.BOTH;
		gbcLblLog.gridx = 0;
		gbcLblLog.gridy = 8;
		panelBtns.add( lblLog, gbcLblLog );

		/*
		 * Load / Save.
		 */

		final JPanel panel = new JPanel();
		final GridBagConstraints gbcPanel = new GridBagConstraints();
		gbcPanel.gridwidth = 2;
		gbcPanel.anchor = GridBagConstraints.SOUTH;
		gbcPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcPanel.insets = new Insets( 5, 5, 0, 0 );
		gbcPanel.gridx = 0;
		gbcPanel.gridy = 9;
		panelBtns.add( panel, gbcPanel );
		panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );

		btnAnalyze = new JButton( "Analyze", Icons.PLOT_ICON );
		btnSave = new JButton( "Save", Icons.SAVE_ICON );

		panel.add( btnAnalyze );
		panel.add( Box.createHorizontalGlue() );
		panel.add( btnSave );

		/*
		 * Listeners.
		 */

		kymographs.listeners().add( () -> {
			tree.setModel( createTreeModel( kymographs ) );
			for ( int i = 0; i < tree.getRowCount(); i++ )
				tree.expandRow( i );
		} );
	}

	private void selected( final Kymographs model, final Object obj )
	{
		if ( obj instanceof Kymograph )
			model.select( ( Kymograph ) obj );
		else if ( obj instanceof Segment )
			model.select( ( Segment ) obj );
	}

	public Logger getLogger()
	{
		return logger;
	}

	private TreeModel createTreeModel( final Kymographs kymographs )
	{

		final DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Kymographs" );
		for ( final Kymograph kymograph : kymographs )
		{
			final DefaultMutableTreeNode kymographNode = new DefaultMutableTreeNode( kymograph, true );
			root.add( kymographNode );
			for ( final Segment segment : kymograph )
			{
				final DefaultMutableTreeNode segmentNode = new DefaultMutableTreeNode( segment, false );
				kymographNode.add( segmentNode );
			}
		}
		final MyTreeModel model = new MyTreeModel( root );
		return model;
	}

	private class MyTreeModel extends DefaultTreeModel
	{

		private static final long serialVersionUID = 1L;

		public MyTreeModel( final TreeNode root )
		{
			super( root, true );
		}

		/*
		 * Handle name changes.
		 */
		@Override
		public void valueForPathChanged( final TreePath path, final Object newValue )
		{
			final DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) path.getLastPathComponent();
			final String name = ( String ) newValue;
			final Object obj = node.getUserObject();
			if ( obj instanceof Kymograph )
			{
				( ( Kymograph ) obj ).setName( name );
				nodeChanged( node );
			}
			else if ( obj instanceof Segment )
			{
				( ( Segment ) obj ).setName( name );
				nodeChanged( node );
			}
			else
			{
				super.valueForPathChanged( path, newValue );
			}
		}
	}

	private static class DeleteCurrentAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private static final String NAME = "deleteCurrentNode";

		private final JTree tree;

		private final Kymographs kymographs;

		public DeleteCurrentAction( final JTree tree, final Kymographs kymographs )
		{
			super( NAME );
			this.tree = tree;
			this.kymographs = kymographs;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final TreePath currentSelection = tree.getSelectionPath();
			if ( currentSelection != null )
			{
				final DefaultMutableTreeNode currentNode = ( DefaultMutableTreeNode ) ( currentSelection.getLastPathComponent() );
				final MutableTreeNode parent = ( MutableTreeNode ) ( currentNode.getParent() );
				if ( parent != null )
				{
					final Object obj = currentNode.getUserObject();
					if ( obj instanceof Kymograph )
					{
						kymographs.removeKymograph( ( ( Kymograph ) obj ) );
						// Will trigger listeners and regenerate the model.
					}
					else if ( obj instanceof Segment )
					{
						final Kymograph kymograph = ( ( Kymograph ) ( ( DefaultMutableTreeNode ) parent ).getUserObject() );
						final Segment segment = ( ( Segment ) obj );
						kymographs.removeSegment( kymograph, segment );
					}
				}
			}
		}
	}
}
