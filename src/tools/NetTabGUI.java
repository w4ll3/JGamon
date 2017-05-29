package tools;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class NetTabGUI extends javax.swing.JFrame {

	private JMenuItem clearMenuItem;
	private JSeparator jSeparator1;
	private JMenuItem copyMenuItem;
	private JMenu jMenu4;
	private JMenuItem exitMenuItem;
	private JSeparator jSeparator2;
	private JCheckBoxMenuItem hexMenuItem;
	private JMenuItem closeMenuItem;
	private JMenuItem newMenuItem;
	private JCheckBoxMenuItem autoConnectMenuItem;
	private JMenuItem reconnectMenuItem;
	private JTextPane textArea;
	private JLabel label;
	private JLabel statusLabel;
	private JPanel statusBar;
	private JScrollPane scrollPane;
	private JMenu jMenu3;
	private JMenuBar jMenuBar1;
	private int delay = 0;
	private String serverName;
	private int localPort = -1;
	private int port = -1;
	private SocketWrapper socketWrapper;

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(String[] args) {
		NetTabGUI inst = new NetTabGUI();
		inst.parseArgs(args);
		inst.setVisible(true);
		inst.newConnection(null);
	}

	private void parseArgs(String[] args) {

		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-d")) {
					delay = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-h")) {
					hexMenuItem.setSelected(true);
				} else if (args[i].startsWith("-")) {
					throw new IllegalArgumentException("Unknown option: "
							+ args[i]);
				} else if (serverName == null) {
					serverName = args[i];
				} else if (port == -1) {
					port = Integer.decode(args[i]).intValue();
					if (port <= 0 || port >= 65536)
						throw new IllegalArgumentException(
								"Port must be within 1 and 65535: " + args[i]);
				} else if (localPort == -1) {
					localPort = Integer.decode(args[i]).intValue();
					if (localPort <= 0 || localPort >= 65536)
						throw new IllegalArgumentException(
								"Local port must be within 1 and 65535: "
										+ args[i]);
				} else
					throw new IllegalArgumentException(
							"Extra argument on command line: " + args[i]);
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Illegal command line argument: "
					+ e.getMessage());
			help();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("An error occured on command line: " + e);
			help();
			System.exit(1);
		}
	}

	private static void help() {
		System.out.println("Options ... explain here");
	}

	public NetTabGUI() {
		super("NetTabGUI");
		initGUI();
	}

	private void initGUI() {
		try {
			{
				this.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent evt) {
						System.exit(0);
					}
				});
			}
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			{
				scrollPane = new JScrollPane();
			}
			{
				statusBar = new JPanel();
				FlowLayout statusBarLayout = new FlowLayout();
				statusBarLayout.setAlignment(FlowLayout.LEFT);
				statusBarLayout.setVgap(0);
				getContentPane().add(scrollPane, BorderLayout.CENTER);
				{
					textArea = new JTextPane();
					scrollPane.setViewportView(textArea);
					textArea.setEditable(false);
					textArea.setFont(new java.awt.Font("Monospaced", 1, 12));
				}
				getContentPane().add(statusBar, BorderLayout.SOUTH);
				statusBar.setLayout(statusBarLayout);
				{
					statusLabel = new JLabel();
					statusBar.add(statusLabel);
					statusLabel.setText("NEW");
					statusLabel.setBorder(BorderFactory
							.createBevelBorder(BevelBorder.LOWERED));
					statusLabel
							.setPreferredSize(new java.awt.Dimension(50, 20));
					statusLabel.setMinimumSize(new java.awt.Dimension(50, 20));
				}
				{
					label = new JLabel();
					statusBar.add(label);
				}
			}
			setSize(600, 600);
			{
				jMenuBar1 = new JMenuBar();
				setJMenuBar(jMenuBar1);
				{
					jMenu3 = new JMenu();
					jMenuBar1.add(jMenu3);
					jMenu3.setText("Server");
					{
						newMenuItem = new JMenuItem();
						jMenu3.add(newMenuItem);
						newMenuItem.setText("New");
						newMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								newConnection(evt);
							}
						});
					}
					{
						reconnectMenuItem = new JMenuItem();
						jMenu3.add(reconnectMenuItem);
						reconnectMenuItem.setText("Reconnect");
						reconnectMenuItem
								.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent evt) {
										reconnect();
									}
								});
					}
					{
						autoConnectMenuItem = new JCheckBoxMenuItem();
						jMenu3.add(autoConnectMenuItem);
						autoConnectMenuItem.setText("Listen automatically");
					}
					{
						closeMenuItem = new JMenuItem();
						jMenu3.add(closeMenuItem);
						closeMenuItem.setText("Close");
						closeMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								closeSockets();
							}
						});
					}
					{
						jSeparator2 = new JSeparator();
						jMenu3.add(jSeparator2);
					}
					{
						exitMenuItem = new JMenuItem();
						jMenu3.add(exitMenuItem);
						exitMenuItem.setText("Exit");
						exitMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								System.exit(0);
							}
						});
					}
				}
				{
					jMenu4 = new JMenu();
					jMenuBar1.add(jMenu4);
					jMenu4.setText("Edit");
					{
						copyMenuItem = new JMenuItem();
						jMenu4.add(copyMenuItem);
						copyMenuItem.setText("Copy");
						copyMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								copyMenuItemActionPerformed(evt);
							}
						});
					}
					{
						clearMenuItem = new JMenuItem();
						jMenu4.add(clearMenuItem);
						clearMenuItem.setText("Clear");
						clearMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								try {
									Document d = textArea.getDocument();
									d.remove(0, d.getLength());
								} catch (BadLocationException ex) {
									ex.printStackTrace();
								}
							}
						});
					}
					{
						jSeparator1 = new JSeparator();
						jMenu4.add(jSeparator1);
					}
					{
						hexMenuItem = new JCheckBoxMenuItem();
						jMenu4.add(hexMenuItem);
						hexMenuItem.setText("Hexadecimal display");
						hexMenuItem.setEnabled(false);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void closeSockets() {
		try {
			if (socketWrapper != null) {
				socketWrapper.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void newConnection(ActionEvent evt) {
		System.out.println("newFileMenuItem.actionPerformed, event=" + evt);
		// TODO add your code for newFileMenuItem.actionPerformed
		NetTabNewDlg dlg = new NetTabNewDlg(this, serverName, port, localPort);
		dlg.setVisible(true);
		if (dlg.isOk()) {
			serverName = dlg.getServer();
			port = Integer.parseInt(dlg.getPort());
			String lp = dlg.getLocalPort();
			if (lp.trim().length() > 0)
				localPort = Integer.parseInt(lp);
			reconnect();
		}
	}

	private void copyMenuItemActionPerformed(ActionEvent evt) {
		System.out.println("copyMenuItem.actionPerformed, event=" + evt);
		// TODO add your code for copyMenuItem.actionPerformed
	}

	private void reconnect() {
		closeSockets();
		try {
			socketWrapper = new SocketWrapper(this, serverName, port,
					localPort == -1 ? port : localPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setStatus(String status) {
		this.statusLabel.setText(status);
	}

	public void setConnection(String s1, String s2) {
		this.label.setText("<html><font color=\"red\">" + s1 +
				"</font> <font color=\"blue\">" + s2 + "</font></html>");
	}

	public JTextPane getTextArea() {
		return textArea;
	}

	public void informFinished() {
		if (autoConnectMenuItem.isSelected())
			reconnect();
	}

	public int getDelay() {
		return delay;
	}

}
