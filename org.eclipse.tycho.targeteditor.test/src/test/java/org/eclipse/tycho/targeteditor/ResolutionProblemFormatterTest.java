/**
 * Copyright (c) 2011, 2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 */
package org.eclipse.tycho.targeteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.ShowResolutionProblemAction;
import org.eclipse.tycho.targeteditor.ShowResolutionProblemAction.ResolutionStatusFormatter;
import org.junit.Test;

/**
 * Does not perform char based formatting test. Checks that relevant information is available.
 * 
 */
public class ResolutionProblemFormatterTest {
    private static final String STATUS_MSG_1 = "msg1";
    private static final String STATUS_MSG_2 = "msg2";
    private static final String STATUS_MSG_MULTI = "multiStatusMsg";

    @Test
    public void testFormatter_simple() {
        final IStatus simpleStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_1);
        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.format(simpleStatus);

        assertTrue(formatted.contains(STATUS_MSG_1));
    }

    @Test
    public void testFormatter_multi() {
        final IStatus status1 = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_1);
        final IStatus status2 = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_2);
        final MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, STATUS_MSG_MULTI, null);
        multiStatus.add(status1);
        multiStatus.add(status2);
        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.format(multiStatus);

        assertTrue(formatted.contains(STATUS_MSG_1));
        assertTrue(formatted.contains(STATUS_MSG_2));
        assertTrue(formatted.contains(STATUS_MSG_MULTI));
    }

    @Test
    public void testFormatter_multiDuplicates() {
        final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_1);
        final IStatus similar_status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_1);
        final MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, STATUS_MSG_MULTI, null);
        multiStatus.add(status);
        multiStatus.add(similar_status);

        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.format(multiStatus);
        final Pattern p = Pattern.compile(STATUS_MSG_1);
        final Matcher m = p.matcher(formatted);
        // msg1 appears only once
        assertTrue(m.find());
        assertFalse(m.find());
    }

    @Test
    public void testFormatter_containedExceptions() {
        // one more stack element for testing
        final RuntimeException rootException = createRootException();
        final RuntimeException exception = new RuntimeException("ex1Msg", rootException);

        final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_1, exception);
        final MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, STATUS_MSG_MULTI, null);
        multiStatus.add(status);
        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.format(multiStatus);

        System.out.println(formatted);

        assertTrue(formatted.contains(exception.getMessage()));
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTrace) {
            assertTrue(formatted.contains(stackTraceElement.toString()));
        }

        assertTrue(formatted.contains(rootException.getMessage()));
        stackTrace = rootException.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTrace) {
            assertTrue(formatted.contains(stackTraceElement.toString()));
        }
    }

    private RuntimeException createRootException() {
        return new RuntimeException("rootMsg");
    }

    @Test
    public void testFormatter_null() {
        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.format(null);

        assertEquals(ResolutionStatusFormatter.MSG_UNRESOLVED + "\n", formatted);
    }

    @Test
    public void testFormatter_shortMsg_null() {
        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.getShortMsgText(null);

        assertEquals(ResolutionStatusFormatter.MSG_UNRESOLVED, formatted);
    }

    @Test
    public void testFormatter_shortMsg_single() {
        final IStatus status1 = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_1);
        final MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, STATUS_MSG_MULTI, null);
        multiStatus.add(status1);
        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.getShortMsgText(multiStatus);

        assertTrue(formatted.equals(STATUS_MSG_1));
    }

    @Test
    public void testFormatter_shortMsg_multiple() {
        final IStatus status1 = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_1);
        final IStatus status2 = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_MSG_2);
        final MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, STATUS_MSG_MULTI, null);
        multiStatus.add(status1);
        multiStatus.add(status2);
        final String formatted = ShowResolutionProblemAction.ResolutionStatusFormatter.getShortMsgText(multiStatus);

        assertTrue(formatted.startsWith("[2 problems] "));
        assertTrue(formatted.contains(STATUS_MSG_1));

    }

}
