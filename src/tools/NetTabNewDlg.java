package tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class NetTabNewDlg extends javax.swing.JDialog {
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JButton connectButton;
	private JButton cancelButton;
	private JPanel botPanel;
	private JLabel jLabel4;
	private JLabel botLabel;
	private JTextField localPort;
	private JLabel jLabel3;
	private JTextField port;
	private JTextField server;

	private boolean ok = false;

	/**
	 * Auto-generated main method to display this JDialog
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		NetTabNewDlg inst = new NetTabNewDlg(frame);
		inst.setVisible(true);
	}

	public NetTabNewDlg(JFrame frame, String host, int port, int localPort) {
		this(frame);
		this.server.setText(host);
		this.port.setText(Integer.toString(port));
		if (localPort != -1)
			this.localPort.setText(Integer.toString(localPort));
	}

	public NetTabNewDlg(JFrame frame) {
		super(frame, true);
		initGUI();
		getRootPane().setDefaultButton(connectButton);
		setLocationRelativeTo(frame);
	}

	private void initGUI() {
		try {
			{
				GridBagLayout thisLayout = new GridBagLayout();
				thisLayout.rowWeights = new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
				thisLayout.rowHeights = new int[]{7, 7, 7, 7, 7, 7};
				thisLayout.columnWeights = new double[]{0.1, 0.1};
				thisLayout.columnWidths = new int[]{7, 7};
				getContentPane().setLayout(thisLayout);
				this.setTitle("New connection");
				this.setResizable(false);
				{
					jLabel1 = new JLabel();
					jLabel1.setLayout(null);
					getContentPane().add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 0, 5), 0, 0));
					jLabel1.setText("Server to connect to:");
				}
				{
					server = new JTextField();
					getContentPane().add(server, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 10), 0, 0));
					server.setPreferredSize(new java.awt.Dimension(100, 20));
				}
				{
					jLabel2 = new JLabel();
					getContentPane().add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
					jLabel2.setText("Port to connect to:");
				}
				{
					port = new JTextField();
					getContentPane().add(port, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
				}
				{
					jLabel3 = new JLabel();
					getContentPane().add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
					jLabel3.setText("Local port [*]:");
				}
				{
					localPort = new JTextField();
					getContentPane().add(localPort, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
				}
				{
					jLabel4 = new JLabel();
					getContentPane().add(jLabel4, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jLabel4.setText("[*] leave empty for same value as port");
				}
				{
					botPanel = new JPanel();
					getContentPane().add(botPanel, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					{
						cancelButton = new JButton();
						botPanel.add(cancelButton);
						cancelButton.setText("Cancel");
						cancelButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								setVisible(false);
							}
						});
					}
					{
						connectButton = new JButton();
						botPanel.add(connectButton);
						connectButton.setText("Connect");
						connectButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								ok = true;
								setVisible(false);
							}
						});
					}
				}
			}
			this.setSize(274, 194);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getLocalPort() {
		return localPort.getText();
	}

	public boolean isOk() {
		return ok;
	}

	public String getPort() {
		return port.getText();
	}

	public String getServer() {
		return server.getText();
	}

}
