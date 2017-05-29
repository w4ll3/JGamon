package tools;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

public class OriginByteBuffer {

	private byte[] buffer;
	private byte[] origin;
	private int count;

	private Document document;

	private final static int STEPSIZE = 1024;

	String colors[] = {"red", "blue"};

	OriginByteBuffer(Document d) {
		document = d;
		count = 0;
		buffer = new byte[STEPSIZE];
		origin = new byte[STEPSIZE];
	}

	void add(byte b, int org) {
		if (count == buffer.length) {
			buffer = newarray(buffer, buffer.length + STEPSIZE);
			origin = newarray(origin, buffer.length + STEPSIZE);
		}
		buffer[count] = b;
		origin[count] = (byte) org;
		count++;
		updateDoc((char) b, org);
	}

	void add(byte[] b, int off, int len, int org) {
		// TODO better impl of this array add
		for (int i = 0; i < len; i++) {
			add(b[i + off], org);
		}
	}

	private byte[] newarray(byte[] buf, int len) {
		byte[] newbuf = new byte[len];
		System.arraycopy(buf, 0, newbuf, 0, buf.length);
		return newbuf;
	}

	private void updateDoc(char c, int type) {
		try {
			int pos = document.getLength();
			SimpleAttributeSet as = new SimpleAttributeSet();
			as.addAttribute("color", "blue");
			document.insertString(pos, "" + c, as);
		} catch (BadLocationException e) {
		}
	}

}
