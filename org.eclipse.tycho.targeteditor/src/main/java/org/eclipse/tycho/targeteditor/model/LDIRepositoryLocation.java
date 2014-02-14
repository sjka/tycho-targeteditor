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

import org.eclipse.equinox.p2.metadata.IVersionedId;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.core.ModelChangedEvent;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.internal.core.target.IUBundleContainer;

@SuppressWarnings("restriction")
class LDIRepositoryLocation implements ILDIRepositoryLocation, IModelChangedListener {

    private final LDITargetDefinition ldiTargetDefinition;
    private IUBundleContainer iuBundleContainer;
    private List<IRepository> repositories = null;
    private List<IMutableVersionedId> units = null;

    public LDIRepositoryLocation(final LDITargetDefinition ldiTargetDefinition,
            final IUBundleContainer iuBundleContainer) {
        this.ldiTargetDefinition = ldiTargetDefinition;
        this.iuBundleContainer = iuBundleContainer;

    }

    @Override
    public List<IRepository> getRepositories() {
        if (repositories == null) {
            repositories = new ArrayList<IRepository>();
            final URI[] repoUries = iuBundleContainer.getRepositories();
            if (repoUries != null) {
                for (final URI uri : repoUries) {
                    final IRepository repo = NexusRepository.createRepository(uri);
                    repo.addModelChangedListener(this);
                    repositories.add(repo);
                }
            }
        }
        return Collections.unmodifiableList(repositories);
    }

    @Override
    public List<IMutableVersionedId> getUnits() {
        if (units == null) {
            units = new ArrayList<IMutableVersionedId>();
            final String[] ids = LDIModelFactory.getIds(iuBundleContainer);
            final Version[] versions = LDIModelFactory.getVersions(iuBundleContainer);
            if (ids != null) {
                for (int i = 0; i < ids.length; i++) {
                    final IMutableVersionedId unit = new MutableVersionedId(ids[i], versions[i]);
                    unit.addModelChangedListener(this);
                    units.add(unit);
                }
            }
        }
        return Collections.unmodifiableList(units);
    }

    @Override
    public IMutableVersionedId[] addUnits(final IVersionedId... vIds) {
        if (vIds == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }
        if (vIds.length == 0) {
            return new IMutableVersionedId[0];
        }

        final List<IMutableVersionedId> newUnits = new ArrayList<IMutableVersionedId>();
        for (final IVersionedId newVersionedId : vIds) {
            if (newVersionedId == null) {
                throw new IllegalArgumentException("Non of the arguments must be null");
            }
            final IMutableVersionedId newUnit = new MutableVersionedId(newVersionedId.getId(),
                    newVersionedId.getVersion());
            newUnit.addModelChangedListener(this);
            newUnits.add(newUnit);
        }
        getUnits();
        units.addAll(newUnits);
        final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(ldiTargetDefinition, this,
                ILDITargetDefintion.EVENT_ADDED_UNITS, null, Collections.unmodifiableList(newUnits));
        modelChanged(modelChangedEvent);
        return newUnits.toArray(new IMutableVersionedId[newUnits.size()]);
    }

    @Override
    public void modelChanged(final IModelChangedEvent event) {
        final List<URI> newRepoURIs = new ArrayList<URI>();
        for (final IRepository repo : getRepositories()) {
            newRepoURIs.add(repo.getURI());
        }
        final List<String> unitIds = new ArrayList<String>();
        final List<String> unitVersions = new ArrayList<String>();
        for (final IMutableVersionedId unit : getUnits()) {
            unitIds.add(unit.getId());
            unitVersions.add(unit.getVersion().toString());
        }

        final IUBundleContainer newiuBundleContainer = (IUBundleContainer) LDIModelFactory.getTargetPlatformService()
                .newIULocation(unitIds.toArray(new String[unitIds.size()]),
                        unitVersions.toArray(new String[unitVersions.size()]),
                        newRepoURIs.toArray(new URI[newRepoURIs.size()]),
                        LDIModelFactory.getResolutionFlags(iuBundleContainer));
        ldiTargetDefinition.replace(iuBundleContainer, newiuBundleContainer);
        iuBundleContainer = newiuBundleContainer;
        ldiTargetDefinition.fireModelChanged(event);
    }

    @Override
    public boolean isLdiLocationFor(final ITargetLocation container) {
        return container == iuBundleContainer;
    }

}
