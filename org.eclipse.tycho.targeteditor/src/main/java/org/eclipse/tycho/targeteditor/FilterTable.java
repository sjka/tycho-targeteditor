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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;

public class FilterTable extends Composite {

    static final String FILTER_TEXT_ID = "FilterTable_filterText";
    private ViewerFilter filter;
    public String filterString = "";
    private TableViewer viewer;
    private Table repositoryTable;
    private HintBox hintBox;

    public FilterTable(final IManagedForm managedForm, final Composite parent,
            final IContentProvider tableContentProvider, ILabelProvider labelProvider) {
        super(parent, managedForm.getToolkit().getOrientation());
        createContent(managedForm, tableContentProvider, labelProvider);
        managedForm.getToolkit().adapt(this);
    }

    void createContent(final IManagedForm managedForm, IContentProvider tableContentProvider,
            ILabelProvider labelProvider) {
        hintBox = createHintBox(managedForm);
        createTable(managedForm, tableContentProvider, labelProvider);
        addFormPart(managedForm);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);
    }

    private void addFormPart(final IManagedForm managedForm) {
        final AbstractFormPart formPart = new AbstractFormPart() {
            @Override
            public void refresh() {
                viewer.refresh(true);
            }

            @Override
            public boolean isStale() {
                return true;
            }
        };
        managedForm.addPart(formPart);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                managedForm.fireSelectionChanged(formPart, event.getSelection());
            }
        });
    }

    private void createTable(final IManagedForm managedForm, IContentProvider tableContentProvider,
            final ILabelProvider labelProvider) {
        repositoryTable = managedForm.getToolkit().createTable(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        repositoryTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = new TableViewer(repositoryTable);

        viewer.setContentProvider(tableContentProvider);
        final ILabelDecorator decorator = new RepositoryTableLabelDecorator();

        viewer.setLabelProvider(new DecoratingLabelProvider(labelProvider, decorator));
        viewer.setComparator(new RepositoryTableComparator());

        filter = new ViewerFilter() {

            @Override
            public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                final String elementText = labelProvider.getText(element);
                return elementText.toLowerCase().contains(hintBox.getTextValue().toLowerCase());
            }

        };

        hintBox.addListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                viewer.refresh();
                if (viewer.getSelection().isEmpty()) {
                    selectFirst();
                }
            }
        });
        viewer.addFilter(filter);

    }

    private HintBox createHintBox(final IManagedForm managedForm) {
        HintBox hintBox = new HintBox(this, SWT.BORDER);
        final Text filterText = hintBox.getText();

        filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        filterText.setData("org.eclipse.swtbot.widget.key", FILTER_TEXT_ID);
        return hintBox;
    }

    public void setInput(final Object input) {
        viewer.setInput(input);
    }

    public void selectFirst() {
        final Object firstElement = viewer.getElementAt(0);
        if (firstElement != null) {
            viewer.setSelection(new StructuredSelection(firstElement), true);
        }
    }

}
