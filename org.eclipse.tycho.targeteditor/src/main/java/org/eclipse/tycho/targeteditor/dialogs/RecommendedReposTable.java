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
package org.eclipse.tycho.targeteditor.dialogs;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tycho.targeteditor.HintBox;
import org.eclipse.tycho.targeteditor.RepositoryTableComparator;
import org.eclipse.tycho.targeteditor.xml.Repository;

public class RecommendedReposTable {
    static final String DESCRIPTION_LABEL = "Description:";
    static final String FILTER_TEXT_ID = "RecommendedReposTable_filterText";
    private static final int LINE_COUNT = 3;

    private ViewerFilter filter;
    private TableViewer viewer;
    private Table repositoryTable;
    private HintBox hintBox;
    private final IWizardPage parentWizardPage;
    private Text descriptionText;
    private Composite composite;

    public RecommendedReposTable(final IWizardPage wizardPage, final Composite parent,
            final IContentProvider tableContentProvider, final RecommendedRepositoriesLabelProvider labelProvider) {
        parentWizardPage = wizardPage;
        createContent(parent, tableContentProvider, labelProvider);
    }

    void createContent(final Composite parent, final IContentProvider tableContentProvider,
            final RecommendedRepositoriesLabelProvider labelProvider) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(GridLayoutFactory.swtDefaults().create());
        hintBox = createFilterText(composite);
        createTable(composite, tableContentProvider, labelProvider);

        createDescriptionLabel();

        descriptionText = createDescriptionTextArea(composite);

    }

    private HintBox createFilterText(final Composite parent) {
        HintBox hintBox = new HintBox(parent, SWT.BORDER);
        hintBox.getText().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hintBox.getText().setData("org.eclipse.swtbot.widget.key", FILTER_TEXT_ID);

        return hintBox;
    }

    private void createTable(final Composite parent, final IContentProvider tableContentProvider,
            final RecommendedRepositoriesLabelProvider labelProvider) {
        viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        repositoryTable = viewer.getTable();
        final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.heightHint = 120;
        repositoryTable.setLayoutData(layoutData);
        repositoryTable.setHeaderVisible(true);

        final TableViewerColumn columnViewer1 = new TableViewerColumn(viewer, SWT.NONE);

        columnViewer1.setLabelProvider(labelProvider.getNameColumLabelProvider());
        columnViewer1.getColumn().setWidth(150);
        columnViewer1.getColumn().setResizable(true);
        columnViewer1.getColumn().setText("Name");

        final TableViewerColumn columnViewer2 = new TableViewerColumn(viewer, SWT.NONE);

        columnViewer2.setLabelProvider(labelProvider.getGavColumLabelProvider());
        columnViewer2.getColumn().setWidth(500);
        columnViewer2.getColumn().setResizable(true);
        columnViewer2.getColumn().setText("GAV");

        viewer.setContentProvider(tableContentProvider);
        viewer.setComparator(new RepositoryTableComparator());

        filter = new ViewerFilter() {

            @Override
            public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                final String elementText = labelProvider.getFilterText(element);
                return elementText.toLowerCase().contains(hintBox.getTextValue().toLowerCase());
            }

        };
        viewer.addFilter(filter);
        hintBox.addListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                viewer.refresh();
                if (viewer.getSelection().isEmpty()) {
                    selectFirst();
                }
            }
        });

        viewer.addSelectionChangedListener(((AddRepositorySelectOptionWizardPage) parentWizardPage));
    }

    private void createDescriptionLabel() {
        final Label descriptionLabel = new Label(composite, SWT.LEFT);
        descriptionLabel.setText(DESCRIPTION_LABEL);
        final GridData labelGridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
        descriptionLabel.setLayoutData(labelGridData);
    }

    private Text createDescriptionTextArea(final Composite container) {
        final Text textArea = new Text(container, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
        final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        gd.minimumHeight = textArea.getFont().getFontData()[0].getHeight() * 2 * LINE_COUNT;
        textArea.setLayoutData(gd);
        textArea.setBounds(container.getBounds());
        return textArea;
    }

    void setLayoutData(final Object layoutData) {
        composite.setLayoutData(layoutData);
    }

    public Table getRepositoryTable() {
        return repositoryTable;
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

    public Repository getSelection() {
        final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        return (Repository) selection.getFirstElement();
    }

    public void setEnabled(final boolean enabled) {
        repositoryTable.setEnabled(enabled);
        hintBox.getText().setEnabled(enabled);
        descriptionText.setEnabled(enabled);
    }

    public String getDescriptionText() {
        return descriptionText.getText();
    }

    public void setDescriptionText(final String descriptionText) {
        this.descriptionText.setText(descriptionText);
    }

}
