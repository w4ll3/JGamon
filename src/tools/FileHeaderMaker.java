/*
 * JGammon: A backgammon client written in Java
 * Copyright (C) 2005/06 Mattias Ulbrich
 *
 * JGammon includes: - playing over network
 *                   - plugin mechanism for graphical board implementations
 *                   - artificial intelligence player
 *                   - plugin mechanism for AI players
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tools;

import java.io.*;

/**
 * Add a license text to all java files.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class FileHeaderMaker {

	private String header;
	char buf[] = new char[2048];

	public FileHeaderMaker(String license) throws IOException {
		header = readFileToBuffer(new File(license)).toString();
	}

	public void workOnDir(File dir) throws IOException {
		File files[] = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				workOnDir(files[i]);
			else
				workOnFile(files[i]);
		}
	}

	public void workOnFile(File file) throws IOException {
		System.out.println("Work on " + file);

		if (!file.getName().endsWith(".java")) {
			System.out.println("   ... skipped");
			return;
		}

		StringBuffer org = readFileToBuffer(file);

		int index = skipHeader(org);

		Writer w = new FileWriter(file);
		w.write(header);
		w.write(org.substring(index));
		w.close();

	}


	private int skipHeader(StringBuffer org) throws IOException {

		int index = 0;

		while (Character.isSpaceChar(org.charAt(index)))
			index++;

		if (org.charAt(index) == '/' && org.charAt(index + 1) == '*') {
			index = org.indexOf("*/", index + 2);
			if (index != -1)
				return index + 2;
			else
				throw new IOException("FileFormat wrong");
		} else
			return 0;
	}

	private StringBuffer readFileToBuffer(File f) throws IOException {
		FileReader r = new FileReader(f);
		StringBuffer ret = new StringBuffer();
		int c = r.read(buf);
		while (c != -1) {
			ret.append(buf, 0, c);
			c = r.read(buf);
		}
		r.close();
		return ret;
	}

	public static void main(String[] args) throws Exception {
		FileHeaderMaker fileheadermaker = new FileHeaderMaker("header.txt");
		fileheadermaker.workOnDir(new File(args[0]));
	}
}
