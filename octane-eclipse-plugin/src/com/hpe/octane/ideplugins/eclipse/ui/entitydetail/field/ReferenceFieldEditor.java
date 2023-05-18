/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse.ui.entitydetail.field;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.model.MultiReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.octane.ideplugins.eclipse.ui.util.EntityComboBox;
import com.hpe.octane.ideplugins.eclipse.ui.util.EntityComboBox.EntityLoader;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class ReferenceFieldEditor extends Composite implements FieldEditor, EntityComboBoxFieldEditor {

    protected EntityModelWrapper entityModelWrapper;
    protected String fieldName;
    private EntityComboBox entityComboBox;
    private Label btnSetNull;

    public ReferenceFieldEditor(Composite parent, int style) {
        super(parent, style);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        entityComboBox = new EntityComboBox(this, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
        entityComboBox.setLayoutData(gd);

        entityComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (entityComboBox.getSelectionMode() == SWT.MULTI) {
                    entityModelWrapper.setValue(new MultiReferenceFieldModel(fieldName, entityComboBox.getSelectedEntities()));
                    if(entityComboBox.getSelectedEntities().size() == 0) {
                        btnSetNull.setEnabled(false);
                    } else {
                        btnSetNull.setEnabled(true);
                    }
                } else {
                    entityModelWrapper.setValue(new ReferenceFieldModel(fieldName, entityComboBox.getSelectedEntity()));
                    btnSetNull.setEnabled(true);
                }
            }
        });
        
        btnSetNull = new Label(this, SWT.NONE);
        GridData gd_btnSetNull = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnSetNull.widthHint = FieldEditorFactory.PLACEHOLDER_LBL_WIDTH;
        btnSetNull.setLayoutData(gd_btnSetNull);
        btnSetNull.setImage(ImageResources.OCTANE_REMOVE.getImage());
        btnSetNull.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
        
        // Nullify
        btnSetNull.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (entityComboBox.getSelectionMode() == SWT.MULTI) {
                    entityModelWrapper.setValue(new MultiReferenceFieldModel(fieldName, Collections.emptyList()));
                } else {
                    entityModelWrapper.setValue(new ReferenceFieldModel(fieldName, null));
                }
                entityComboBox.clearSelection();
                btnSetNull.setEnabled(false);
            }
        });
    }

    @Override
    public void setField(EntityModelWrapper entityModel, String fieldName) {

        this.entityModelWrapper = entityModel;
        this.fieldName = fieldName;

        @SuppressWarnings("rawtypes")
        FieldModel fieldModel = entityModel.getValue(fieldName);
        
        boolean hasValue = fieldModel != null && fieldModel.getValue() != null;
        
        //Additional check for MultiReferenceFieldModel
        if(hasValue && fieldModel instanceof MultiReferenceFieldModel) {
            hasValue = !((MultiReferenceFieldModel)fieldModel).getValue().isEmpty();
        }

        if (hasValue) {
            if (fieldModel instanceof ReferenceFieldModel && entityComboBox.getSelectionMode() == SWT.SINGLE) {
                entityComboBox.setSelectedEntity(((ReferenceFieldModel) fieldModel).getValue());
            } else if (fieldModel instanceof MultiReferenceFieldModel && entityComboBox.getSelectionMode() == SWT.MULTI) {
                entityComboBox.setSelectedEntities(((MultiReferenceFieldModel) fieldModel).getValue());
            } else {
                throw new RuntimeException("Failed to set value of the Reference field model, field value and metadata not compatible");
            }

            btnSetNull.setEnabled(true);
        } else {
            btnSetNull.setEnabled(false);
            entityComboBox.clearSelection();
        }
    }

    public void setLabelProvider(LabelProvider labelProvider) {
        entityComboBox.setLabelProvider(labelProvider);
    }

    public void setEntityLoader(EntityLoader entityLoader) {
        entityComboBox.setEntityLoader(entityLoader);
    }

    public void setSelectionMode(int selectionMode) {
        entityComboBox.setSelectionMode(selectionMode);
    }

    public void setSelectedEntities(Collection<EntityModel> entityModel) {
        entityComboBox.setSelectedEntities(entityModel);
    }

	public void closeEntityComboBox() {
		entityComboBox.closeAndDisposeShell(); 
	}
}