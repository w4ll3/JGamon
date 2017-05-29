package jgam.test.net;

import junit.framework.*;
import jgam.net.*;
import java.io.*;
import java.util.Timer;
import java.util.Date;
import java.util.TimerTask;

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
public class TestChannelReader extends TestCase {
    private ChannelReader channelReader = null;

    protected void setUp() throws Exception {
        super.setUp();
        channelReader = new ChannelReader(3);
        channelReader.receiveChannelMessage(3, "Test on 3");
        channelReader.receiveChannelMessage(2, "Test on 2");
        channelReader.receiveChannelMessage( -2, "Test on -2");
        channelReader.receiveChannelMessage(3, "Test on 3");
        channelReader.receiveChannelMessage(3, "Test on 3");
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                channelReader.receiveChannelMessage(3, "Test on 3");
            }
        }, 2000);
    }

    protected void tearDown() throws Exception {
        channelReader = null;
        super.tearDown();
    }

    public void testRead() throws IOException {
        for (int i = 0; i < 40; i++) {
            int c = channelReader.read();
            System.err.print((char)c);
            assertEquals("Test on 3\n".charAt(i % 10), c);
        }
    }

}
