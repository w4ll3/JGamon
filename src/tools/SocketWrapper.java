package tools;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SocketWrapper extends Thread {

	private int mode;
	private static int SERVER = 0;
	private static int CLIENT = 1;

	private ServerSocket serverSocket;
	private String host;
	private int port;
	private NetTabGUI gui;
	private Socket socket1;
	private Socket socket2;
	private Shortcut shortcut[] = new Shortcut[2];

	public SocketWrapper(NetTabGUI gui, String serverName, int port,
	                     int localPort) throws UnknownHostException, IOException {
		super("SocketWrapper");
		this.gui = gui;
		serverSocket = new ServerSocket(localPort);
		host = serverName;
		this.port = port;
		start();
	}

	public void run() {
		try {
			gui.setStatus("LISTN");
			socket1 = accept(serverSocket);
			gui.setStatus("ACCPT");
			socket2 = new Socket(host, port);
			gui.setStatus("CONN");
			gui.setConnection(socket1.getInetAddress().toString() + ":"
					+ socket1.getPort(), socket2.getInetAddress().toString()
					+ ":" + socket2.getPort());

			shortcut[0] = new Shortcut(SERVER, socket1.getInputStream(),
					socket2.getOutputStream());
			shortcut[0].start();
			shortcut[1] = new Shortcut(CLIENT, socket2.getInputStream(),
					socket1.getOutputStream());
			shortcut[1].start();

			synchronized (this) {
				wait();
			}

			shortcut[0].interrupt();
			shortcut[1].interrupt();
			shortcut[0].join();
			shortcut[1].join();

			System.out.println("Threads have ended");
			close();
			gui.informFinished();

		} catch (InterruptedException e) {
			System.out.println("Interrupted");
		} catch (Exception e) {
			gui.setStatus("EXCEP");
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Socket accept(ServerSocket serverSocket) throws InterruptedException, IOException {
		serverSocket.setSoTimeout(2000);
		while (true) {
			try {
				return serverSocket.accept();
			} catch (SocketTimeoutException e) {
			}
			if (isInterrupted())
				throw new InterruptedException();
		}
	}

	synchronized private void add(byte[] b, int len, int type)
			throws UnsupportedEncodingException, BadLocationException {
		SimpleAttributeSet as = new SimpleAttributeSet();
		as.addAttribute(StyleConstants.Foreground, type == SERVER ? Color.red
				: Color.blue);
		Document d = gui.getTextArea().getDocument();
		int offset = d.getLength();
		String string = new String(b, 0, len, "US-ASCII");
		d.insertString(offset, string, as);
	}

	private class Shortcut extends Thread {
		InputStream is;
		OutputStream os;
		int type;

		Shortcut(int type, InputStream is, OutputStream os) {
			super("Shortcut - " + type);
			this.is = is;
			this.os = os;
			this.type = type;
		}

		public void run() {
			try {
				byte[] buffer = new byte[1024];
				int read = is.read(buffer);
				while (read != -1 && !isInterrupted()) {
					add(buffer, read, type);
					if (gui.getDelay() != 0)
						Thread.sleep(gui.getDelay() * 1000);
					os.write(buffer, 0, read);
					os.flush();
					read = is.read(buffer);
				}
				System.out.println("Socket " + type + " closed");
				synchronized (SocketWrapper.this) {
					SocketWrapper.this.notify();
				}
			} catch (Exception e) {
				System.err.println("Error in " + type);
				e.printStackTrace();
				synchronized (SocketWrapper.this) {
					SocketWrapper.this.notify();
				}
			}
		}
	}

	void close() throws IOException {
		if (socket1 != null)
			socket1.close();
		if (socket2 != null)
			socket2.close();
		gui.setStatus("CLOSD");
		gui.setConnection("", "");
	}

	public void finish() throws IOException, InterruptedException {
		if (shortcut[0] != null)
			shortcut[0].interrupt();
		if (shortcut[1] != null)
			shortcut[1].interrupt();
		interrupt();
		close();
		join();
	}

}
