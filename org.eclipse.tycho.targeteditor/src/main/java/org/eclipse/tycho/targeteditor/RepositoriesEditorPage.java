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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.core.IModelChangeProvider;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.ui.editor.targetdefinition.TargetEditor;
import org.eclipse.pde.internal.ui.shared.target.ITargetChangedListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tycho.targeteditor.model.ILDIRepositoryLocation;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;
import org.eclipse.tycho.targeteditor.model.IMutableVersionedId;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

@SuppressWarnings("restriction")
public class RepositoriesEditorPage extends FormPage implements IModelChangedListener, LDITargetDefinitionProvider {

    private static final String LDI_EDITOR_HELP_ID = "org.eclipse.tycho.targeteditor.targetEditor";

    static class LifeCycleForm extends AbstractFormPart {

        @Override
        public void refresh() {
            // avoid to reset stale and dirty on refresh
        }

        @Override
        public void commit(final boolean onSave) {
            // avoid to reset dirty on refresh commit triggered by TargetEditor e.g on page change
            if (onSave) {
                super.commit(onSave);
            }
        }

        public void reset() {
            super.refresh();
            getManagedForm().dirtyStateChanged();
        }

    }

    private RepositoryMainForm mainForm;
    private ILDITargetDefintion ldiTargetDefinition;
    private LifeCycleForm lifeCycleForm;
    protected Job resolveTargetDefinitionTriggerJob;

    public RepositoriesEditorPage(final LDITargetEditor editor, final String id, final String title) {
        super(editor, id, title);
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input) {
        super.init(site, input);
        RepositoryVersionCache.getInstance().addModelChangedListener(this);
        resolveTargetDefinitionTriggerJob = new Job("Trigger resolve job") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        final ITargetChangedListener targetChangeListener = ((TargetEditor) getEditor())
                                .getTargetChangedListener();
                        targetChangeListener.contentsChanged(((TargetEditor) getEditor()).getTarget(), this, true,
                                false);
                    }
                });

                return Status.OK_STATUS;
            }
        };
        resolveTargetDefinitionTriggerJob.setPriority(Job.DECORATE);
        resolveTargetDefinitionTriggerJob.setSystem(true);
    }

    private void updateRepositoryVersionCache() {
        for (final ILDIRepositoryLocation location : getLDITargetDefinition().getRepositoryLocations()) {
            for (final IRepository repo : location.getRepositories()) {
                if (repo instanceof INexusRepository) {
                    RepositoryVersionCache.getInstance().getAllAvailableVersions((INexusRepository) repo);
                }
            }
        }
    }

    @Override
    public ILDITargetDefintion getLDITargetDefinition() {
        if (ldiTargetDefinition == null) {
            ldiTargetDefinition = LDIModelFactory
                    .createLDITargetDefinition(((LDITargetEditor) getEditor()).getTarget());
            ldiTargetDefinition.addModelChangedListener(this);
            updateRepositoryVersionCache();
        }
        return ldiTargetDefinition;
    }

    private void resetInput() {
        if (ldiTargetDefinition != null) {
            this.ldiTargetDefinition.removeModelChangedListener(this);
            this.ldiTargetDefinition = null;
        }
        mainForm.resetInput();
    }

    public void doRevert() {
        resetInput();
        lifeCycleForm.reset();
    }

    @Override
    public void setActive(final boolean active) {
        resetInput();
        super.setActive(active);
    }

    @Override
    public void dispose() {
        RepositoryVersionCache.getInstance().removeModelChangedListener(this);
        super.dispose();
    }

    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        mainForm = new RepositoryMainForm(this);
        final ScrolledForm form = managedForm.getForm();
        form.setText("Target definition");
        form.setExpandHorizontal(true);
        mainForm.createContent(managedForm);
        managedForm.getToolkit().decorateFormHeading(form.getForm());
        lifeCycleForm = new LifeCycleForm();
        managedForm.addPart(lifeCycleForm);
        ((TargetEditor) getEditor()).contributeToToolbar(managedForm.getForm(), LDI_EDITOR_HELP_ID);

        final Object resolveJobFamily = LDIModelFactory.getResolveJobFamily(((TargetEditor) getEditor()));
        ShowResolutionProblemAction.createAndAssign(this, managedForm, resolveJobFamily);
    }

    @Override
    public void modelChanged(final IModelChangedEvent event) {
        final IManagedForm managedForm = getManagedForm();
        if (managedForm != null) {
            managedForm.getForm().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    final IModelChangeProvider changeProvider = event.getChangeProvider();
                    final boolean isResolveRequired = (changeProvider instanceof INexusRepository)
                            || (changeProvider instanceof IMutableVersionedId)
                            || (changeProvider instanceof ILDITargetDefintion);
                    final boolean isCacheRefresh = ILDITargetDefintion.EVENT_PROPERTY_RESET_CONTAINER_RESOLUTION_STATE
                            .equals(event.getChangedProperty());
                    final boolean isSourceChanged = isResolveRequired && !isCacheRefresh;
                    if (isSourceChanged) {
                        if (lifeCycleForm != null) {
                            lifeCycleForm.markDirty();
                            getEditor().editorDirtyStateChanged();
                        }
                    }
                    if (isResolveRequired) {
                        //  trigger resolve with delay. e.g programatic changes in loops should not be resolved separately
                        resolveTargetDefinitionTriggerJob.schedule(400);
                    }
                    final IFormPart[] parts = managedForm.getParts();
                    for (int i = 0; i < parts.length; i++) {
                        final IFormPart part = parts[i];
                        part.refresh();
                    }
                }
            });
        }
    }

}
