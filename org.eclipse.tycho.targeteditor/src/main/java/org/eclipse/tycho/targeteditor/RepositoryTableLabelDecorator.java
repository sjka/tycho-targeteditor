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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tycho.targeteditor.model.EDynamicVersions;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.NexusVersionCalculationResult;

public class RepositoryTableLabelDecorator implements ILabelDecorator, IColorDecorator {

    @Override
    public void addListener(final ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }

    @Override
    public void removeListener(final ILabelProviderListener listener) {
    }

    @Override
    public Image decorateImage(final Image image, final Object element) {
        if (element instanceof INexusRepository) {
            final INexusRepository repo = (INexusRepository) element;
            final NexusVersionCalculationResult availableVersions = RepositoryVersionCache.getInstance()
                    .getAllAvailableVersions(repo);
            if (availableVersions != null) {
                final IStatus status = availableVersions.getStatus();
                if (status.matches(IStatus.ERROR)) {
                    return Activator.getDefault().getImageFromRegistry(Activator.ERROR_OVERLAY);
                } else {
                    final String currentVersion = repo.getVersion();
                    if (!EDynamicVersions.isDynamicVersion(currentVersion)) {
                        final String latestVersion = availableVersions.getVersionInfo().getLatestVersion();
                        final String latestReleaseVersion = availableVersions.getVersionInfo()
                                .getLatestReleaseVersion();
                        if (!(currentVersion.equals(latestVersion) || currentVersion.equals(latestReleaseVersion))) {
                            return Activator.getDefault().getImageFromRegistry(Activator.UPDATEABLE_OVERLAY);
                        }
                    }
                }
            } else {
                return Activator.getDefault().getImageFromRegistry(Activator.LOADING_OVERLAY);
            }
        }
        return image;
    }

    @Override
    public String decorateText(final String text, final Object element) {
        return null;
    }

    @Override
    public Color decorateForeground(final Object element) {
        final int decorationColorId = getDecorationColor(element);
        if (decorationColorId != -1) {
            return Display.getDefault().getSystemColor(decorationColorId);
        }
        return null;
    }

    @Override
    public Color decorateBackground(final Object element) {
        return null;
    }

    /**
     * Calculates the SWT color id (see e.g {@link SWT.COLOR_DARK_YELLOW}) to be used to decorate
     * the label text of the given element. <code>-1</code> indicates that no decoration should be
     * done.
     * 
     * @param element
     * @return one of the SWT color constants or -1
     */
    static int getDecorationColor(final Object element) {
        if (element instanceof INexusRepository) {
            final INexusRepository repo = (INexusRepository) element;
            if (NexusRepositoryNames.SNAPSHOT.nexusName().equals(repo.getRepositoryName())) {
                return SWT.COLOR_DARK_YELLOW;
            }
        }
        return -1;
    }
}
