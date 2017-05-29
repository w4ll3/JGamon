package jgam.test.util;

import junit.framework.*;
import jgam.*;
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
public class TestSecureRoll extends TestCase {
    private SecureRoll secureRoll1, secureRoll2;

    protected void setUp() throws Exception {
        super.setUp();
        secureRoll1 = new SecureRoll();
        secureRoll2 = new SecureRoll();

    }

    protected void tearDown() throws Exception {
        secureRoll1 = null;
        secureRoll2 = null;
        super.tearDown();
    }


    public void testNegotiate() throws IOException, ProtocolException, InterruptedException {
        final PipedWriter pw1 = new PipedWriter();
        final BufferedReader pr1 = new BufferedReader(new PipedReader(pw1));
        final PipedWriter pw2 = new PipedWriter();
        final BufferedReader pr2 = new BufferedReader(new PipedReader(pw2));

        Thread parallel = new Thread() {
            public void run() {
                try {
                    secureRoll2.negotiate(pr1, pw2, true);
                    secureRoll2.negotiate(pr1, pw2, false);
                } catch (Exception ex) {
                    throw new Error(ex);
                }
            }
        };

        parallel.start();

        secureRoll1.negotiate(pr2, pw1, false);
        secureRoll1.negotiate(pr2, pw1, true);

        parallel.join();

        assertEquals(secureRoll1.getOneDice(), secureRoll2.getOneDice());
    }

    public void testNegotiateUnsecure() throws IOException, ProtocolException, InterruptedException {
        final PipedWriter pw1 = new PipedWriter();
        final BufferedReader pr1 = new BufferedReader(new PipedReader(pw1));
        final PipedWriter pw2 = new PipedWriter();
        final BufferedReader pr2 = new BufferedReader(new PipedReader(pw2));

        Thread parallel = new Thread() {
            public void run() {
                try {
                    secureRoll2.negotiateUnsecure(pr1, pw2, true);
                    secureRoll2.negotiateUnsecure(pr1, pw2, false);
                } catch (Exception ex) {
                    throw new Error(ex);
                }
            }
        };

        parallel.start();

        secureRoll1.negotiateUnsecure(pr2, pw1, false);
        secureRoll1.negotiateUnsecure(pr2, pw1, true);

        parallel.join();

        assertEquals(secureRoll1.getOneDice(), secureRoll2.getOneDice());
    }

}
