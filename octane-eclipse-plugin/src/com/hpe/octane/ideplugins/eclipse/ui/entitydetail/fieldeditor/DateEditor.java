package com.hpe.octane.ideplugins.eclipse.ui.entitydetail.fieldeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.model.EntityModelWrapper;

public class DateEditor extends Composite implements FieldEditor{

    public DateEditor(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));
        
        DateTime dateTimeField = new DateTime(this, SWT.BORDER);
        dateTimeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblError = new Label(this, SWT.NONE);
        lblError.setText("ERROR");
        lblError.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
    }

    @Override
    public void setField(EntityModelWrapper entityModel, String... fieldNames) {
        // TODO Auto-generated method stub
    }

    @Override
    public void forceUpdate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setFieldMessage(FieldMessage fieldMessage) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getFieldMessage(FieldMessage fieldMessage) {
        // TODO Auto-generated method stub
        
    }
    
}