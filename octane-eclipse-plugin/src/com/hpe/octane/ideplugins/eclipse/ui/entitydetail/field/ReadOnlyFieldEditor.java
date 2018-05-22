/*******************************************************************************
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolTip;

import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.model.EntityModelWrapper;
import com.hpe.octane.ideplugins.eclipse.ui.util.TruncatingStyledText;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class ReadOnlyFieldEditor extends Composite implements FieldEditor {

    private TruncatingStyledText lblFieldValue;
    private ToolTip toolTip;

    public ReadOnlyFieldEditor(Composite parent, int style) {
        super(parent, style);
        toolTip = new ToolTip(parent.getShell(), SWT.NONE);
        
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);
        
        lblFieldValue = new TruncatingStyledText(this, SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER, toolTip);
        lblFieldValue.setMargins(3, 2, 0, 3);
        lblFieldValue.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblFieldValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        
        FieldEditorFactory.createPlaceholderLabel(this);
    }

    @Override
    public void setField(EntityModelWrapper entityModel, String fieldName) {
        lblFieldValue.setText(Util.getUiDataFromModel(entityModel.getValue(fieldName)));
        
        //Removes a bunch of unnecessary listeners 
        if(lblFieldValue.getText().isEmpty()) {
            lblFieldValue.setEnabled(false);
        } else {
            lblFieldValue.setEnabled(true);
        }
    }
    
}
