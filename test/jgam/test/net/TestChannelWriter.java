package jgam.test.net;

import junit.framework.*;
import jgam.net.*;
import java.io.*;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class TestChannelWriter extends TestCase {
    private ChannelWriter channelWriter1, channelWriter2;
    private StringWriter w = new StringWriter();

    protected void setUp() throws Exception {
        super.setUp();
        channelWriter1 = new ChannelWriter(w, 1);
        channelWriter2 = new ChannelWriter(w, 2);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWrite() throws IOException {
        String l1 = "test1on1\n";
        String l2 = "test2on2";
        String l3 = "test3on1";
        String l4 = "addon\n";

        channelWriter1.write(l1);
        channelWriter2.write(l2);
        channelWriter1.write(l3);
        channelWriter2.write("\n");
        channelWriter1.write(l4);
        System.err.println(w);

        assertEquals(w.toString(), "1 " + l1 + "2 " + l2 + "\n1 " + l3 + l4);
    }

}
