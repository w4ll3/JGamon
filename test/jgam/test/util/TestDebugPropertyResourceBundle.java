package jgam.test.util;

import junit.framework.*;
import jgam.util.*;

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
public class TestDebugPropertyResourceBundle extends TestCase {
    private DebugPropertyResourceBundle debugPropertyResourceBundle = null;

    protected void setUp() throws Exception {
        super.setUp();
        debugPropertyResourceBundle = new DebugPropertyResourceBundle("jgam.msg.JGam");
    }

    protected void tearDown() throws Exception {
        debugPropertyResourceBundle = null;
        super.tearDown();
    }

    public void testGetString() {
        String key = "help";
        String expectedReturn = null;
        String actualReturn = debugPropertyResourceBundle.getString(key);
        assertTrue(!actualReturn.equals("[jgam.msg.JGam: help]"));

        key = "noknownkey";
        expectedReturn = "[jgam.msg.JGam: noknownkey]";
        actualReturn = debugPropertyResourceBundle.getString(key);
        assertEquals("unknown value", expectedReturn, actualReturn);
    }

    public void testNoResource() {
        DebugPropertyResourceBundle bpr = new DebugPropertyResourceBundle("nothing_useful_here");
        String ret = bpr.getString("egal");
        assertEquals("[nothing_useful_here missing!]", ret);
    }

}
