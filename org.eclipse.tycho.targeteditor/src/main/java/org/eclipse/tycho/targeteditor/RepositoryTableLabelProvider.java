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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.internal.core.target.AbstractBundleContainer;
import org.eclipse.pde.internal.core.target.FeatureBundleContainer;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class RepositoryTableLabelProvider extends LabelProvider {

    @Override
    public Image getImage(final Object element) {
        final int elementType = RepositoryTableComparator.getType(element);
        switch (elementType) {

        case RepositoryTableComparator.NEXUS_REPOSITORY_TYPE:
            return Activator.getDefault().getImageFromRegistry(Activator.NEXUS_IMG);

        case RepositoryTableComparator.REPOSITORY_TYPE:
            return getImageFromPDE(PDEPluginImages.DESC_REPOSITORY_OBJ);

        case RepositoryTableComparator.FEATURE_BUNDLE_CONTAINER_TYPE:
            return getImageFromPDE(PDEPluginImages.DESC_FEATURE_OBJ);

        case RepositoryTableComparator.DIRECTORY_BUNDLE_CONTAINER_TYPE:
            final ImageDescriptor directoryImageDescriptor = PlatformUI.getWorkbench().getSharedImages()
                    .getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
            return getImageFromPDE(directoryImageDescriptor);

        case RepositoryTableComparator.PROFILE_BUNDLE_CONTAINER_TYPE:
            return getImageFromPDE(PDEPluginImages.DESC_PRODUCT_DEFINITION);

        default:
            return null;
        }
    }

    private Image getImageFromPDE(final ImageDescriptor imageDescriptor) {
        return PDEPlugin.getDefault().getLabelProvider().get(imageDescriptor, 0);
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof INexusRepository) {
            return getNexusRepositoryText((INexusRepository) element);
        }
        if (element instanceof IRepository) {
            final IRepository repository = (IRepository) element;
            return repository.getURI().toString();
        }
        if (element instanceof AbstractBundleContainer) {
            try {
                final String location = ((AbstractBundleContainer) element).getLocation(true);
                if (element instanceof FeatureBundleContainer) {
                    return ((FeatureBundleContainer) element).getFeatureId() + " - " + location;
                } else {
                    return location;
                }
            } catch (final CoreException e) {
                Activator.getDefault().logException(e);
            }
        }
        return super.getText(element);
    }

    private String getNexusRepositoryText(final INexusRepository repository) {
        final String result = " " + repository.getGroupId() + "  /  " + repository.getArtifactId() + "  /  "
                + repository.getVersion() + "  /  (" + repository.getRepositoryName() + ")";
        return result;

    }
}
