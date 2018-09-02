package com.hpe.octane.ideplugins.eclipse.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class LoginDialog extends Dialog {
    
    private String loginUrl;

    public LoginDialog(Shell parentShell, String loginUrl) {
        super(parentShell);
        this.loginUrl = loginUrl;
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        Label button = new Label(container, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button.setText(loginUrl);
        return container;
    }
    
    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }

}
