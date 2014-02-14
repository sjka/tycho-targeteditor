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
package org.eclipse.tycho.targeteditor.preferences;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tycho.targeteditor.preferences.URLValidator;
import org.junit.Test;

public class URLValidatorTest {

    @Test
    public void testValidateURL() throws Exception {
        assertValid("http://example.sap.com/");
    }

    @Test
    public void testValidateUnknownProtocol() {
        assertInvalid("htt://example.sap.com/");
    }

    @Test
    public void testValidateFileURL() throws Exception {
        assertValid("file:///c/us/1/");
    }

    @Test
    public void testValidateInvalidFileURL() {
        assertInvalid("file://c/us/1/");
        assertInvalid("file://localhost/c:/us/1/");
        assertInvalid("file://anyhost/c:/us/1/");
    }

    @Test
    public void testValidateNonUrlUris() throws Exception {
        assertInvalid("mailto:me@sap");
        assertInvalid("relative/hierarchical/URI/");
    }

    @Test
    public void testValidateURLWithoutTrailingSlash() throws Exception {
        assertValid("http://host/path/file");
    }

    @Test
    public void testValidateURLWithFragment() throws Exception {
        assertValid("http://host/path/#fragment");
    }

    @Test
    public void testValidateURLWithQuery() throws Exception {
        assertValid("http://host/path/?query");
    }

    private void assertValid(final String value) throws Exception {
        assertTrue(new URLValidator(value).validate().isOK());
    }

    private void assertInvalid(final String url) {
        final IStatus result = new URLValidator(url).validate();
        assertFalse(result.isOK());
        assertTrue(result.matches(IStatus.ERROR));
    }

}
