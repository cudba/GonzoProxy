package ch.compass.gonzoproxy.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.compass.gonzoproxy.controller.RelayController;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionModel;

public class GonzoProxyFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2325146651722965630L;
	private JPanel contentPane;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmNew;
	private JMenuItem mntmOpen;
	private JMenuItem mntmSave;
	private JMenuItem mntmExit;
	private JMenu mnTools;
	private JMenuItem mntmModifier;
	private JMenu mnHelp;
	private JMenuItem mntmLoadTemplate;
	private JMenuItem mntmAbout;
	private JSplitPane splitPane;
	private ApduListPanel panelList;
	private ApduDetailPanel panelDetail;
	private Packet editApdu;
	private SessionModel data;
	final JFileChooser fc;

	public GonzoProxyFrame(RelayController controller) {
		data = controller.getSessionModel();
		fc = new JFileChooser();
		initGui(controller);
	}

	private void initGui(final RelayController controller) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		setTitle("Gonzo Proxy");
		setMinimumSize(new Dimension(850, 450));

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		mnFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(mnFile);

		mntmNew = new JMenuItem("New");
		mntmNew.setMnemonic(KeyEvent.VK_N);
		mntmNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				NewSessionDialog ns = new NewSessionDialog(controller);
				ns.setVisible(true);
			}
		});

		mnFile.add(mntmNew);

		mntmOpen = new JMenuItem("Open");
		mntmOpen.setMnemonic(KeyEvent.VK_O);
		mntmOpen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				int returnVal = fc.showOpenDialog(GonzoProxyFrame.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.getName().endsWith(".gonzo")) {
						controller.openFile(file);
					}else{
						JOptionPane.showMessageDialog(GonzoProxyFrame.this,
							    "Wrong filetype, .gonzo expected",
							    "Filetype",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		mnFile.add(mntmOpen);

		mntmSave = new JMenuItem("Save");
		mntmSave.setMnemonic(KeyEvent.VK_S);
		mntmSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				int returnVal = fc.showSaveDialog(GonzoProxyFrame.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.getName().endsWith(".gonzo")) {
						controller.saveFile(file);
					}else{
						JOptionPane.showMessageDialog(GonzoProxyFrame.this,
							    "Wrong filetype, .gonzo expected",
							    "Filetype",
							    JOptionPane.ERROR_MESSAGE);
					}
				}			
			}
		});
		
		mnFile.add(mntmSave);

		mntmExit = new JMenuItem("Exit");
		mntmExit.setMnemonic(KeyEvent.VK_E);
		mntmExit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GonzoProxyFrame.this.dispose();
			}
		});
		mnFile.add(mntmExit);

		mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		mntmModifier = new JMenuItem("Modifier");
		mntmModifier.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ModifierDialog md = new ModifierDialog(controller);
				md.setVisible(true);
			}
		});
		mnTools.add(mntmModifier);

		mntmLoadTemplate = new JMenuItem("Load template");
		mnTools.add(mntmLoadTemplate);

		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 134, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		contentPane.add(splitPane, gbc_splitPane);

		panelList = new ApduListPanel(controller, new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int index = e.getFirstIndex();

				if (index == -1) {
					GonzoProxyFrame.this.panelDetail.clearFields();
					GonzoProxyFrame.this.panelDetail.setApdu(new Packet());
				} else {
					GonzoProxyFrame.this.editApdu = ((Packet) GonzoProxyFrame.this.data
							.getPacketList().get(index));
					GonzoProxyFrame.this.panelDetail
							.setApdu(GonzoProxyFrame.this.editApdu);
				}

			}
		});
		splitPane.setLeftComponent(panelList);

		panelDetail = new ApduDetailPanel(controller);
		splitPane.setRightComponent(panelDetail);
	}
}
