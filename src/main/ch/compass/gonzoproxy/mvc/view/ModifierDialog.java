package ch.compass.gonzoproxy.mvc.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.compass.gonzoproxy.mvc.controller.RelayController;
import ch.compass.gonzoproxy.mvc.model.RuleModel;
import ch.compass.gonzoproxy.mvc.model.RuleSetModel;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.relay.modifier.Rule;
import ch.compass.gonzoproxy.relay.modifier.RuleSet;

public class ModifierDialog extends JDialog {

	private static final long serialVersionUID = 9047578530331858262L;
	private JPanel contentPane;
	private JTable tableRules;
	private RelayController controller;
	private JList<String> listRuleSet;
	private RuleModel ruleModel;
	private PacketModifier modifier;
	protected RuleSet editRuleSet;
	private JCheckBox chckbxUpdateLengthAutomatically;
	protected Rule editRule;

	public ModifierDialog(RelayController controller) {
		this.controller = controller;
		this.modifier = controller.getPacketModifier();
		initGui();
	}

	private void initGui() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Modifiers for parsable packets");
		setBounds(100, 100, 457, 225);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 131, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		contentPane.add(scrollPane_1, gbc_scrollPane_1);

		listRuleSet = new JList<String>(
				new RuleSetModel(modifier.getRuleSets()));
		listRuleSet.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				setEditRuleSet(modifier.getRuleSets().get(
						listRuleSet.getSelectedIndex()));
				ruleModel.setRules(editRuleSet.getRules());
			}
		});
		scrollPane_1.setViewportView(listRuleSet);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);

		ruleModel = new RuleModel();
		tableRules = new JTable(ruleModel);
		tableRules.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				int row = target.getSelectedRow();
				setEditRule(editRuleSet.getRules().get(row));
			}
		});
		scrollPane.setViewportView(tableRules);

		chckbxUpdateLengthAutomatically = new JCheckBox(
				"Update Content Length automatically");
		GridBagConstraints gbc_chckbxUpdateLengthAutomatically = new GridBagConstraints();
		gbc_chckbxUpdateLengthAutomatically.anchor = GridBagConstraints.WEST;
		gbc_chckbxUpdateLengthAutomatically.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxUpdateLengthAutomatically.gridx = 0;
		gbc_chckbxUpdateLengthAutomatically.gridy = 1;
		contentPane.add(chckbxUpdateLengthAutomatically,
				gbc_chckbxUpdateLengthAutomatically);

		JButton btnDeleteSelectedRule = new JButton("Delete selected rule");
		GridBagConstraints gbc_btnDeleteSelectedRule = new GridBagConstraints();
		gbc_btnDeleteSelectedRule.anchor = GridBagConstraints.EAST;
		gbc_btnDeleteSelectedRule.gridx = 1;
		gbc_btnDeleteSelectedRule.gridy = 1;
		contentPane.add(btnDeleteSelectedRule, gbc_btnDeleteSelectedRule);
	}

	protected void setEditRule(Rule rule) {
		this.editRule = rule;
	}

	protected void setEditRuleSet(RuleSet ruleSet) {
		// TODO Auto-generated method stub
		this.editRuleSet = ruleSet;
		chckbxUpdateLengthAutomatically.setSelected(ruleSet
				.shouldUpdateContentLength());
	}

}
