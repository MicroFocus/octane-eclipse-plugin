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

package com.hpe.octane.ideplugins.eclipse.ui.util.error;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.ErrorModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.ui.util.TruncatingStyledText;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class ErrorComposite extends Composite {

    public static final String ERORR_MODEL_FIELD_DESCRIPTION_TRANSLATED = "description_translated";
    public static final String ERORR_MODEL_FIELD_DESCRIPTION = "description";
    public static final String ERORR_MODEL_FIELD_PROPERTIES = "properties";
    public static final String ERORR_MODEL_FIELD_ERROR_CODE = "error_code";
    public static final String ERORR_MODEL_FIELD_HTTP_STATUS_CODE = "httpStatusCode";
    public static final String ERORR_MODEL_FIELD_CORRELATION_ID = "correlation_id";
    public static final String ERROR_MODEL_FIELD_STACK_TRACE = "stack_trace";

    private Composite compositeExceptionData;
    private Composite compositeBtns;
    private Label lblSeparator;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public ErrorComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(1, false));

        setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
        setForeground(SWTResourceManager.getColor(SWT.COLOR_LIST_FOREGROUND));
        setBackgroundMode(SWT.INHERIT_FORCE);

        compositeExceptionData = new Composite(this, SWT.NONE);
        compositeExceptionData.setLayout(new GridLayout(1, false));
        compositeExceptionData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        lblSeparator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
        lblSeparator.setVisible(false);

        compositeBtns = new Composite(this, SWT.NONE);
        FillLayout fl_compositeBtns = new FillLayout(SWT.HORIZONTAL);
        fl_compositeBtns.spacing = 10;
        compositeBtns.setLayout(fl_compositeBtns);
        compositeBtns.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
    }

    public void addButton(String btnText, Runnable clickedRunnable) {
        lblSeparator.setVisible(true);
        Button btn = new Button(compositeBtns, SWT.NONE);
        btn.setText(btnText);
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickedRunnable.run();
            }
        });

        setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
        layout(true);
    }

    public void clearButtons() {
        lblSeparator.setVisible(false);
        Arrays.stream(compositeBtns.getChildren()).forEach(Control::dispose);
    }

    public void displayException(Exception ex) {
        if (ex instanceof OctaneException) {
            displayOctaneException((OctaneException) ex);
        } else {
            displayGenericException(ex);
        }
    }

    private void displayGenericException(Exception ex) {
        compositeExceptionData.setLayout(new GridLayout(1, false));

        TruncatingStyledText txtErrorField = new TruncatingStyledText(compositeExceptionData, SWT.MULTI);
        txtErrorField.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
        txtErrorField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

        txtErrorField.setText(ex.getMessage());
    }

    private void displayOctaneException(OctaneException ex) {
        compositeExceptionData.setLayout(new GridLayout(2, false));

        ErrorModel errorModel = ex.getError();
        String description = getDescriptionFromOctaneException(errorModel);

        if (description != null) {
            TruncatingStyledText txtErrorField = new TruncatingStyledText(compositeExceptionData, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
            txtErrorField.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
            txtErrorField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            txtErrorField.setText(description);

            errorModel.removeValue(ERORR_MODEL_FIELD_DESCRIPTION);
            errorModel.removeValue(ERORR_MODEL_FIELD_DESCRIPTION_TRANSLATED);
        }

        // Rest of values displayed generically
        ex.getError().getValues().forEach(fieldModel -> {
            String fieldValueTxt = getFieldModelValueIfPresentAndNotEmpty(errorModel, fieldModel.getName());
            if (fieldValueTxt == null) {
                return;
            }

            Label lblErrorField = new Label(compositeExceptionData, SWT.NONE);
            lblErrorField.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            lblErrorField.setText(convertFieldNameToLabel(fieldModel.getName()));
            
            if (fieldModel.getName().equals(ERROR_MODEL_FIELD_STACK_TRACE)) {
                Text stackTraceText = new Text(compositeExceptionData, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
                GridData gdStackTraceText = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
                stackTraceText.setLayoutData(gdStackTraceText);
                stackTraceText.setText(fieldValueTxt);
                stackTraceText.setSize(stackTraceText.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            } else {
                TruncatingStyledText txtErrorFieldValue = new TruncatingStyledText(compositeExceptionData, SWT.NONE);
                txtErrorFieldValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                txtErrorFieldValue.setText("  " + fieldValueTxt);
            }
        });
    }

    private static String getFieldModelValueIfPresentAndNotEmpty(ErrorModel errorModel, String fieldName) {
        if (errorModel.getValue(fieldName) == null) {
            return null;
        }

        @SuppressWarnings("rawtypes")
        FieldModel fieldModel = errorModel.getValue(fieldName);
        String value = Util.getUiDataFromModel(fieldModel);
        return value.isEmpty() ? null : value;
    }

    public static String getDescriptionFromOctaneException(ErrorModel errorModel) {
        String description = getFieldModelValueIfPresentAndNotEmpty(errorModel, ERORR_MODEL_FIELD_DESCRIPTION);

        // If null try the alternative
        if (description == null) {
            description = getFieldModelValueIfPresentAndNotEmpty(errorModel, ERORR_MODEL_FIELD_DESCRIPTION_TRANSLATED);
        }

        return description;
    }

    private static String convertFieldNameToLabel(String fieldname) {
        fieldname = fieldname.replace("_", " ");
        fieldname = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(fieldname)).collect(Collectors.joining(" "));
        fieldname = Arrays.stream(fieldname.split("\\s+"))
                .map(str -> StringUtils.capitalize(str))
                .collect(Collectors.joining(" "));
        fieldname = fieldname + ": ";
        return fieldname;
    }

}