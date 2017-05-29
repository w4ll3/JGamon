package jgam.test.net;

import junit.framework.*;

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
public class TestSuiteNet extends TestCase {

    public TestSuiteNet(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(jgam.test.net.TestChannelReader.class);
        suite.addTestSuite(jgam.test.net.TestChannelWriter.class);
        suite.addTestSuite(jgam.test.net.TestJGammonConnection.class);
        return suite;
    }
}
