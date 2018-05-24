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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.hpe.adm.nga.sdk.model.FloatFieldModel;
import com.hpe.adm.nga.sdk.model.LongFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.model.EntityModelWrapper;

public class NumericFieldEditor extends Composite implements FieldEditor {

    protected EntityModelWrapper entityModelWrapper;
    protected String fieldName;
    protected Text textField;

    private long minumumValue = Long.MIN_VALUE;
    private long maximumValue = Long.MAX_VALUE;
    private ModifyListener modifyListener;

    public NumericFieldEditor(Composite parent, int style, boolean isRealNumber) {
        super(parent, style);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        textField = new Text(this, SWT.BORDER);        
        textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
        
        FieldEditorFactory.createPlaceholderLabel(this);

        textField.addListener(SWT.Verify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String string = e.text;

                if (string.isEmpty() || "-".equals(string)) {
                    return;
                }

                long value;

                if (isRealNumber) {
                    try {
                        value = (long) Double.parseDouble(string);
                    } catch (Exception ex) {
                        e.doit = false;
                        return;
                    }
                } else {
                    try {
                        value = Long.parseLong(string);
                    } catch (Exception ex) {
                        e.doit = false;
                        return;
                    }
                }

                if (value < minumumValue) {
                    e.doit = false;
                    return;
                }

                if (value > maximumValue) {
                    e.doit = false;
                    return;
                }
            }
        });

        modifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (textField.getText().isEmpty()) {
                    entityModelWrapper.setValue(new ReferenceFieldModel(fieldName, null));
                } else {
                    if (isRealNumber) {
                        try {
                            Float value = Float.parseFloat(textField.getText());
                            entityModelWrapper.setValue(new FloatFieldModel(fieldName, value));
                        } catch (Exception ignored) {
                        }
                    } else {
                        try {
                            Long value = Long.parseLong(textField.getText());
                            entityModelWrapper.setValue(new LongFieldModel(fieldName, value));
                        } catch (Exception ignored) {
                        }
                    }

                }
            }
        };
    }

    public void setBounds(long minValue, long maxValue) {
        this.minumumValue = minValue;
        this.maximumValue = maxValue;
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