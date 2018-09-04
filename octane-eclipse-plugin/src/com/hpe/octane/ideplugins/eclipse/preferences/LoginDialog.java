package com.hpe.octane.ideplugins.eclipse.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class LoginDialog extends Dialog {

    private static final String LOGIN_TEXT = "If the page below does not display correctly, <a href=\"\">click here to use your system default browser.</a>";

    private String loginUrl;
    private boolean wasClosed;
    private Label lblSysBrowser;

    public LoginDialog(Shell shell, String loginPageUrl) {
        super(shell);
        this.loginUrl = loginPageUrl;
        setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("ALM Octane Plugin: Login");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout());

        Link link = new Link(container, SWT.NONE);
        link.setText(LOGIN_TEXT);

        ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.H_SCROLL | SWT.H_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Browser.clearSessions();      
        Browser browser = new Browser(scrolledComposite, SWT.NONE);
        browser.setUrl(LoginDialog.this.loginUrl);
        
        scrolledComposite.setMinSize(800, 600);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        scrolledComposite.setContent(browser);

        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch(LoginDialog.this.loginUrl);
                browser.setVisible(false);

                if (lblSysBrowser == null) {
                    // Only add the label once, but the user can press the link
                    // as many times as he wants
                    lblSysBrowser = new Label(container, SWT.NONE);
                    lblSysBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                    lblSysBrowser.setText("Opening login page in system default browser, waiting for session...");
                }
            }
        });

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public boolean close() {
        this.wasClosed = true;
        return super.close();
    }

    public boolean wasClosed() {
        return wasClosed;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(800, 600);
    }

}