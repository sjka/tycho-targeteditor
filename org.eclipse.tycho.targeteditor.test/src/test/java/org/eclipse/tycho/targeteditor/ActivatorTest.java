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

import org.eclipse.tycho.targeteditor.Activator;
import org.junit.Before;
import org.junit.Test;

public class ActivatorTest {

    private Activator subject;

    @Before
    public void initTestSubject() {
        subject = Activator.getDefault();
    }

    @Test
    public void testPluginID() {
        assertEquals(subject.getBundle().getSymbolicName(), Activator.PLUGIN_ID);
    }

}
