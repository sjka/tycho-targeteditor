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
package org.eclipse.tycho.targeteditor.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.tycho.targeteditor.model.EDynamicVersions;
import org.junit.Test;

public class EDynamicVersionsTest {

    @Test
    public void testValidDynamicVersions() throws Exception {
        assertTrue(EDynamicVersions.isDynamicVersion(EDynamicVersions.SNAPSHOT.name()));
        assertTrue(EDynamicVersions.isDynamicVersion(EDynamicVersions.RELEASE.name()));
    }

    @Test
    public void testInvalidDynamicVersions() throws Exception {
        assertFalse(EDynamicVersions.isDynamicVersion("snapshot"));
        assertFalse(EDynamicVersions.isDynamicVersion("MILESTONE"));
    }
}
