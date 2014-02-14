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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.tycho.targeteditor.model.Util;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class UpdateVersionFormTest {

    private UpdateIUVersionAction subject;

    @Before
    public void setup() throws IOException, CoreException {
        final ITargetDefinition target = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(target);
        subject = new UpdateIUVersionAction(new LDITargetDefinitionProvider() {
            @Override
            public ILDITargetDefintion getLDITargetDefinition() {
                return ldiTargetDefinition;
            }
        });
    }

    @Test
    public void testVersionModification() {
        assertTrue(subject.containsSpecifiedVersion());
        subject.updateVersionsToUnspecified();
        assertFalse(subject.containsSpecifiedVersion());
    }

}
