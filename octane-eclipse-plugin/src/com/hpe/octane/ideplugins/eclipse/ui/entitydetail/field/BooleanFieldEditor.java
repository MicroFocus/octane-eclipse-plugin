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

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.hpe.adm.nga.sdk.model.BooleanFieldModel;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.octane.ideplugins.eclipse.ui.util.EntityComboBox;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class BooleanFieldEditor extends Composite implements FieldEditor, EntityComboBoxFieldEditor {
    
    protected EntityModelWrapper entityModelWrapper;
    protected String fieldName;
    
    private EntityComboBox booleanEntityComboBox;
    
    private static final EntityModel ENTITY_TRUE = new EntityModel();
    private static final EntityModel ENTITY_FALSE = new EntityModel();
    static {
        ENTITY_TRUE.setValue(new StringFieldModel(EntityFieldsConstants.FIELD_NAME, Boolean.TRUE.toString()));
        ENTITY_FALSE.setValue(new StringFieldModel(EntityFieldsConstants.FIELD_NAME, Boolean.FALSE.toString()));
    }

    public BooleanFieldEditor(Composite parent, int style) {
        super(parent, style);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);
        
        booleanEntityComboBox = new EntityComboBox(this, SWT.NONE);
        booleanEntityComboBox.setEntityLoader(searchQuery -> Arrays.asList(ENTITY_TRUE, ENTITY_FALSE));
        booleanEntityComboBox.setLabelProvider(FieldEditorFactory.DEFAULT_ENTITY_LABEL_PROVIDER);
        booleanEntityComboBox.setLoadingIndicatorEnabled(false);
        booleanEntityComboBox.setFilteringEnabled(false);
        booleanEntityComboBox.setMinSize(new Point(-1, -1));
        
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
        gd.heightHint = booleanEntityComboBox.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 2;
        booleanEntityComboBox.setLayoutData(gd);
        
        booleanEntityComboBox.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        
        FieldEditorFactory.createPlaceholderLabel(this);
        
        booleanEntityComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean newValue = booleanEntityComboBox.getSelectedEntity() == ENTITY_TRUE ? true : false;
                entityModelWrapper.setValue(new BooleanFieldModel(fieldName, newValue)); 
            }
        });
    }

    @Override
    public void setField(EntityModelWrapper entityModel, String fieldName) {
        this.entityModelWrapper = entityModel;
        this.fieldName = fieldName;

        Boolean boolValue = (Boolean) entityModel.getValue(fieldName).getValue();
        if(boolValue) {
            booleanEntityComboBox.setSelectedEntity(ENTITY_TRUE);
        } else {
            booleanEntityComboBox.setSelectedEntity(ENTITY_FALSE);
        }
    }

	public void closeEntityComboBox() {
		booleanEntityComboBox.closeAndDisposeShell(); 
	}
    
}
