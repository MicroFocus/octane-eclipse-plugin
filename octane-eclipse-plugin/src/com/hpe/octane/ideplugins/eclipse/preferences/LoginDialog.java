package com.hpe.octane.ideplugins.eclipse.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;

public class LoginDialog extends Dialog {

    public  static final String DEFAULT_TITLE = "ALM Octane Plugin: Login";
    private static final String LOGIN_TEXT = "If the page below does not display correctly, <a href=\"\">click here to use your system default browser.</a>";

    private String loginUrl;
    private boolean wasClosed;
    private Shell shell;

    public LoginDialog(Shell shell, String loginPageUrl) {
        super(shell);
        this.loginUrl = loginPageUrl;
        setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        this.shell = shell;
        shell.setText("ALM Octane Plugin: Login");
    }
    
    public void setTitle(String title) {
        if(shell != null && !shell.isDisposed()) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 10;
        gridLayout.marginWidth = 10;
        container.setLayout(gridLayout);

        Link link = new Link(container, SWT.NONE);
        link.setText(LOGIN_TEXT);
        
        StackLayoutComposite stackLayoutComposite = new StackLayoutComposite(container, SWT.BORDER);
        stackLayoutComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        LoadingComposite loadingComposite = new LoadingComposite(stackLayoutComposite, SWT.NONE);

        ScrolledComposite scrolledComposite = new ScrolledComposite(stackLayoutComposite, SWT.H_SCROLL | SWT.H_SCROLL);
        Browser.clearSessions();   
        Browser browser = new Browser(scrolledComposite, SWT.NONE);
        scrolledComposite.setMinSize(800, 600);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setContent(browser);
        browser.setUrl(LoginDialog.this.loginUrl);
  
        browser.addLocationListener(new LocationListener() { 
            @Override
            public void changing(LocationEvent event) {
                stackLayoutComposite.showControl(loadingComposite);
            }
            @Override
            public void changed(LocationEvent event) {
                stackLayoutComposite.showControl(scrolledComposite);
            }
        });
        
        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch(LoginDialog.this.loginUrl);
            }
        });

        return container;
    }
    
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(0, 0));
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {}

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