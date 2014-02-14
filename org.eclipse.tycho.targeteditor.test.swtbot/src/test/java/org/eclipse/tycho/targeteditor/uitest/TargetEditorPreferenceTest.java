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
package org.eclipse.tycho.targeteditor.uitest;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.PreferencePagePO;
import org.eclipse.tycho.targeteditor.preferences.PreferenceConstants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TargetEditorPreferenceTest {

    @Rule
    public AutoCloser closer = new AutoCloser();

    @Test
    public void testEmptyURLErrorMessage() throws Exception {
        final PreferencePagePO preferencePage = closer.add(PreferencePagePO.open());
        preferencePage.setUrl("");
        assertEquals("You have to enter a URL", preferencePage.validationResult());
    }

    @Test
    public void testPreferencePersisted() throws Exception {
        final PreferencePagePO preferencePage = PreferencePagePO.open();
        preferencePage.setUrl("hello");
        preferencePage.saveAndClose();
        final PreferencePagePO newPage = closer.add(PreferencePagePO.open());
        assertEquals("hello", newPage.getUrl());
        final String preferenceUrl = Platform.getPreferencesService().getString(Activator.PLUGIN_ID,
                PreferenceConstants.P_RECOMMENDED_REPOSITORIES_URL, null, null);
        assertEquals("hello", preferenceUrl);
        newPage.close();
    }

    @Test
    public void testValidFile() throws Exception {
        final URL repositories = FileLocator.find(Activator.getDefault().getBundle(), new Path(
                "resources/recommendedRepositories.xml"), null);
        final PreferencePagePO preferencePage = closer.add(PreferencePagePO.open());
        preferencePage.setUrl(repositories.toString());
        assertEquals("The URL \"" + repositories + "\" references a valid list of recommended repositories.",
                preferencePage.validationResult());
        preferencePage.close();
    }

    @Test
    public void testInvalidFile() throws Exception {
        final URL invalidUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path(
                "resources/notRecommendedRepositories.xml"), null);
        final PreferencePagePO preferencePage = closer.add(PreferencePagePO.open());
        preferencePage.setUrl(invalidUrl.toString());
        try {
            assertEquals("The content from " + invalidUrl
                    + " could not be parsed: File version 2.0 is not supported by this parser",
                    preferencePage.validationResult());

        } finally {
            final URL validUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path(
                    "resources/recommendedRepositories.xml"), null);
            preferencePage.setUrl(validUrl.toString());
            preferencePage.saveAndClose();
        }
    }
}
