/*******************************************************************************
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse.ui.comment;

import java.util.Collection;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.ui.comment.job.GetCommentsJob;
import com.hpe.octane.ideplugins.eclipse.ui.comment.job.PostCommentJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.LinkInterceptListener;
import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite.LoadingPosition;
import com.hpe.octane.ideplugins.eclipse.ui.util.PropagateScrollBrowserFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.error.ErrorComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.PlatformResourcesManager;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class EntityCommentComposite extends StackLayoutComposite {

    private EntityModel entityModel;

    private Browser commentsBrowser;
    private LoadingComposite loadingComposite;
    private Composite commentsComposite;

    private Text commentText;

    private Button postCommentBtn;
    private Label separator;

    public EntityCommentComposite(Composite parent, int style) {
        super(parent, style);

        loadingComposite = new LoadingComposite(this, SWT.NONE);
        loadingComposite.setLoadingVerticalPosition(LoadingPosition.TOP);

        commentsComposite = new Composite(this, SWT.NONE);
        GridLayout gl_commentsComposite = new GridLayout(2, false);
        gl_commentsComposite.marginWidth = 0;
        gl_commentsComposite.horizontalSpacing = 10;
        commentsComposite.setLayout(gl_commentsComposite);

        separator = new Label(commentsComposite, SWT.SEPARATOR | SWT.VERTICAL);
        GridData sepGridData = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 3);
        sepGridData.widthHint = 2;
        separator.setLayoutData(sepGridData);

        Label commentsTitleLabel = new Label(commentsComposite, SWT.NONE);
        commentsTitleLabel.setText("Comments");
        commentsTitleLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));

        Composite inputCommentAndSendButtonComposite = new Composite(commentsComposite, SWT.NONE);
        inputCommentAndSendButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        inputCommentAndSendButtonComposite.setLayout(new GridLayout(2, false));

        commentText = new Text(inputCommentAndSendButtonComposite, SWT.BORDER);
        commentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
        commentText.setToolTipText("Add new comment");
        commentText.addListener(SWT.Traverse, (Event event) -> {
            if (event.detail == SWT.TRAVERSE_RETURN && commentText.isEnabled()) {
                postComment(commentText.getText());
            }
        });

        postCommentBtn = new Button(inputCommentAndSendButtonComposite, SWT.NONE);
        postCommentBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
        postCommentBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (commentText.isEnabled()) {
                    postComment(commentText.getText());
                    commentText.setEnabled(false);
                }
            }
        });
        postCommentBtn.setText("Post");

        commentText.setEnabled(false);
        postCommentBtn.setEnabled(false);

        PropagateScrollBrowserFactory browserFactory = new PropagateScrollBrowserFactory();
        commentsBrowser = browserFactory.createBrowser(commentsComposite, SWT.NONE);
        commentsBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        showControl(commentsComposite);
        commentsBrowser.setText("<html></html>");
        commentsBrowser.addLocationListener(new LinkInterceptListener());
    }

    public void setEntityModel(EntityModel entityModel) {
        this.entityModel = entityModel;
        commentText.setEnabled(true);
        postCommentBtn.setEnabled(true);
        displayComments();
    }

    private void postComment(String text) {
        commentText.setEnabled(false);
        showControl(loadingComposite);

        PostCommentJob sendCommentJob = new PostCommentJob("Posting Comment", entityModel, text);
        sendCommentJob.schedule();
        sendCommentJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    try {
                        Exception exception = sendCommentJob.getException();
                        if (exception != null) {
                            throw exception;
                        } else {
                            displayComments();
                            commentText.setText("");
                            commentText.setEnabled(true);
                        }
                    } catch (Exception e) {
                        ErrorComposite errorComposite = new ErrorComposite(commentsComposite.getParent(), SWT.NONE);
                        errorComposite.addButton("Refresh comments", () -> displayComments());
                        errorComposite.displayException(e);
                        commentText.setText("");
                        commentText.setEnabled(true);
                        showControl(errorComposite);
                    }
                });
            }
        });
    }

    private void displayComments() {
        GetCommentsJob getCommentsJob = new GetCommentsJob("Getting comments", entityModel);
        getCommentsJob.schedule();
        showControl(loadingComposite);

        getCommentsJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    String html = getCommentHtmlString(getCommentsJob.getComents());
                    if (!commentsBrowser.isDisposed()) {
                        try {
                            Exception exception = getCommentsJob.getException();
                            if (exception != null) {
                                throw exception;
                            } else {
                                commentsBrowser.setText(html);
                                showControl(commentsComposite);
                            }
                        } catch (Exception e) {
                            ErrorComposite errorComposite = new ErrorComposite(commentsComposite.getParent(), SWT.NONE);
                            errorComposite.addButton("Refresh comments", () -> displayComments());
                            errorComposite.displayException(e);
                            commentText.setText("");
                            commentText.setEnabled(true);
                            showControl(errorComposite);
                        }

                    }
                });
            }
        });

    }

    private static String getCommentHtmlString(Collection<EntityModel> comments) {

        Color backgroundColor = PlatformResourcesManager.getPlatformBackgroundColor();
        Color foregroundColor = PlatformResourcesManager.getPlatformForegroundColor();

        String backgroundColorString = "rgb(" + backgroundColor.getRed() + "," + backgroundColor.getGreen() + "," + backgroundColor.getBlue() + ")";
        String foregroundColorString = "rgb(" + foregroundColor.getRed() + "," + foregroundColor.getGreen() + "," + foregroundColor.getBlue() + ")";

        StringBuilder commentsBuilder = new StringBuilder();
        commentsBuilder.append("<html><body style=\"background-color:" + backgroundColorString + ";\">");
        commentsBuilder.append("<font style=\"color:" + foregroundColorString + "\">");

        if (!comments.isEmpty()) {
            for (EntityModel comment : comments) {
                String commentsPostTime = Util.getUiDataFromModel(comment.getValue(EntityFieldsConstants.FIELD_CREATION_TIME));
                String userName = Util.getUiDataFromModel(comment.getValue(EntityFieldsConstants.FIELD_AUTHOR), "full_name");
                String commentLine = Util.getUiDataFromModel(comment.getValue(EntityFieldsConstants.FIELD_COMMENT_TEXT));
                commentLine = removeHtmlBaseTags(commentLine);
                String currentText = commentsPostTime + " <b>" + userName + ":</b> <br>" + commentLine + "<hr>";
                commentsBuilder.append(currentText);
            }
        }
        commentsBuilder.append("</body></html>");
        return commentsBuilder.toString();
    }

    private static String removeHtmlBaseTags(String htmlString) {
        htmlString = htmlString.replace("<html>", "");
        htmlString = htmlString.replace("<body>", "");
        htmlString = htmlString.replace("</html>", "");
        htmlString = htmlString.replace("</body>", "");
        return htmlString;
    }

}
