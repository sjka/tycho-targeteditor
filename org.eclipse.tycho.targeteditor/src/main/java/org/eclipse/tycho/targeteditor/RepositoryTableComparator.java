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

import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.core.target.DirectoryBundleContainer;
import org.eclipse.pde.internal.core.target.FeatureBundleContainer;
import org.eclipse.pde.internal.core.target.ProfileBundleContainer;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;

@SuppressWarnings("restriction")
public class RepositoryTableComparator extends ViewerComparator {

    public static final int NEXUS_REPOSITORY_TYPE = 0;
    public static final int REPOSITORY_TYPE = 1;
    public static final int FEATURE_BUNDLE_CONTAINER_TYPE = 2;
    public static final int DIRECTORY_BUNDLE_CONTAINER_TYPE = 3;
    public static final int PROFILE_BUNDLE_CONTAINER_TYPE = 4;

    public static int getType(final Object element) {
        if (element instanceof INexusRepository) {
            return NEXUS_REPOSITORY_TYPE;
        } else if (element instanceof IRepository) {
            return REPOSITORY_TYPE;
        } else if (element instanceof FeatureBundleContainer) {
            return FEATURE_BUNDLE_CONTAINER_TYPE;
        } else if (element instanceof DirectoryBundleContainer) {
            return DIRECTORY_BUNDLE_CONTAINER_TYPE;
        } else if (element instanceof ProfileBundleContainer) {
            return PROFILE_BUNDLE_CONTAINER_TYPE;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int category(final Object element) {
        return getType(element);
    }
}
