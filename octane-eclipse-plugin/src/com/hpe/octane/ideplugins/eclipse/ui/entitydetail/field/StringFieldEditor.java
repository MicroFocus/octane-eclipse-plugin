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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.hpe.adm.nga.sdk.model.ReferenceErrorModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.adm.octane.ideplugins.services.util.Util;

public class StringFieldEditor extends Composite implements FieldEditor {

    protected EntityModelWrapper entityModelWrapper;
    protected String fieldName;
    protected Text textField;
    private ModifyListener modifyListener;

    public StringFieldEditor(Composite parent, int style) {
        super(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        textField = new Text(this, style);
        textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
        
        FieldEditorFactory.createPlaceholderLabel(this);

        modifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String text = textField.getText();
                // whitespace is considered null
                if (text.trim().isEmpty()) {
                    entityModelWrapper.setValue(new ReferenceErrorModel(fieldName, null));
                } else {
                    entityModelWrapper.setValue(new StringFieldModel(fieldName, text));
                }
            }
        };
    }

    @Override
    public void setField(EntityModelWrapper entityModel, String fieldName) {
        this.entityModelWrapper = entityModel;
        this.fieldName = fieldName;
        textField.removeModifyListener(modifyListener);
        textField.setText(Util.getUiDataFromModel(entityModel.getValue(fieldName)));
        textField.addModifyListener(modifyListener);
    }

}