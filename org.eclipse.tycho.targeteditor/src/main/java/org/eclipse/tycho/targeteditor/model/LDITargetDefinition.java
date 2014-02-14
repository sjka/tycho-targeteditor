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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.core.IModelChangeProvider;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.ModelChangedEvent;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.internal.core.target.AbstractBundleContainer;
import org.eclipse.pde.internal.core.target.IUBundleContainer;

@SuppressWarnings("restriction")
class LDITargetDefinition extends ModelChangeProvider implements ILDITargetDefintion, IModelChangeProvider {

    private static final int DEFAULT_RESOLUTION_FLAG = IUBundleContainer.INCLUDE_REQUIRED
            | IUBundleContainer.INCLUDE_SOURCE;
    private final ITargetDefinition targetDefinition;
    private List<ILDIRepositoryLocation> ldiRepositoryLocations;

    public LDITargetDefinition(final ITargetDefinition targetDefinition) {
        this.targetDefinition = targetDefinition;
    }

    @Override
    public List<ILDIRepositoryLocation> getRepositoryLocations() {
        if (ldiRepositoryLocations == null) {
            ldiRepositoryLocations = new ArrayList<ILDIRepositoryLocation>();
            final ITargetLocation[] bundleContainers = targetDefinition.getTargetLocations();
            if (bundleContainers != null) {
                for (int i = 0; i < bundleContainers.length; i++) {
                    final ITargetLocation container = bundleContainers[i];
                    if (isRepositoryLocation(container)) {
                        final IUBundleContainer iuBundleContainer = (IUBundleContainer) container;
                        ldiRepositoryLocations.add(new LDIRepositoryLocation(this, iuBundleContainer));
                    }
                }
            }
        }
        return Collections.unmodifiableList(ldiRepositoryLocations);
    }

    @Override
    public List<AbstractBundleContainer> getNonRepoLocations() {
        final List<AbstractBundleContainer> result = new ArrayList<AbstractBundleContainer>();
        final ITargetLocation[] bundleContainers = targetDefinition.getTargetLocations();
        if (bundleContainers != null) {
            for (int i = 0; i < bundleContainers.length; i++) {
                final ITargetLocation container = bundleContainers[i];
                if (!isRepositoryLocation(container)) {
                    result.add((AbstractBundleContainer) container);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    private boolean isRepositoryLocation(final ITargetLocation container) {
        return container instanceof IUBundleContainer;
    }

    @Override
    public ILDIRepositoryLocation addRepository(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Argument must not be null");
        }
        getRepositoryLocations();
        final IUBundleContainer newiuBundleContainer = (IUBundleContainer) LDIModelFactory.getTargetPlatformService()
                .newIULocation(new String[0], new String[0], new URI[] { uri }, DEFAULT_RESOLUTION_FLAG);
        addContainer(newiuBundleContainer);
        final LDIRepositoryLocation newRepositoryLocation = new LDIRepositoryLocation(this, newiuBundleContainer);
        ldiRepositoryLocations.add(newRepositoryLocation);
        final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, this,
                ILDITargetDefintion.EVENT_ADDED_LOCATION, null, newRepositoryLocation);
        fireModelChanged(modelChangedEvent);
        return newRepositoryLocation;
    }

    @Override
    public void removeLocation(final ILDIRepositoryLocation locationToBeRemoved) {
        if (locationToBeRemoved == null) {
            throw new IllegalArgumentException("locationToBeRemoved must not be null.");
        }
        ldiRepositoryLocations.remove(locationToBeRemoved);
        final ITargetLocation[] containers = targetDefinition.getTargetLocations();
        final List<ITargetLocation> remainingContainers = new ArrayList<ITargetLocation>();
        for (final ITargetLocation container : containers) {
            if (!locationToBeRemoved.isLdiLocationFor(container)) {
                remainingContainers.add(container);
            }
        }
        targetDefinition
                .setTargetLocations(remainingContainers.toArray(new ITargetLocation[remainingContainers.size()]));
        final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, IModelChangedEvent.REMOVE,
                new Object[] { locationToBeRemoved }, null);
        fireModelChanged(modelChangedEvent);
    }

    @Override
    public ILDIRepositoryLocation findLocationOf(final IRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null.");
        }
        for (final ILDIRepositoryLocation repositoryLocation : getRepositoryLocations()) {
            final List<IRepository> repositories = repositoryLocation.getRepositories();
            if (repositories.contains(repository)) {
                return repositoryLocation;
            }
        }
        return null;
    }

    @Override
    public void clearResolutionStatus() {
        final ITargetLocation[] bundleContainers = targetDefinition.getTargetLocations();
        if (bundleContainers != null) {
            for (int i = 0; i < bundleContainers.length; i++) {
                final ITargetLocation container = bundleContainers[i];
                LDIModelFactory.clearResolutionStatus(container);
            }
        }
        final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, this,
                ILDITargetDefintion.EVENT_PROPERTY_RESET_CONTAINER_RESOLUTION_STATE, null, null);
        fireModelChanged(modelChangedEvent);
    }

    @Override
    public IStatus getResolutionStatus() {
        return targetDefinition.getStatus();
    }

    void replace(final IUBundleContainer iuBundleContainer, final IUBundleContainer newiuBundleContainer) {
        final ITargetLocation[] bContainers = targetDefinition.getTargetLocations();
        for (int i = 0; i < bContainers.length; i++) {
            if (bContainers[i] == iuBundleContainer) {
                bContainers[i] = newiuBundleContainer;
                break;
            }
        }
        targetDefinition.setTargetLocations(bContainers);
    }

    private void addContainer(final IUBundleContainer newiuBundleContainer) {
        ITargetLocation[] oldContainers = targetDefinition.getTargetLocations();
        if (oldContainers == null) {
            oldContainers = new ITargetLocation[0];
        }
        final ITargetLocation[] newContainers = new ITargetLocation[oldContainers.length + 1];
        System.arraycopy(oldContainers, 0, newContainers, 0, oldContainers.length);
        newContainers[newContainers.length - 1] = newiuBundleContainer;
        targetDefinition.setTargetLocations(newContainers);
    }

}
