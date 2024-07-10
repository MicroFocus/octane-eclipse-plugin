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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.adm.octane.ideplugins.services.nonentity.ImageService;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityFieldsComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.LinkInterceptListener;
import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.PropagateScrollBrowserFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.error.ErrorDialog;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.PlatformResourcesManager;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class DescriptionComposite extends Composite {

    private PropagateScrollBrowserFactory factory = new PropagateScrollBrowserFactory();
    private Color foregroundColor = PlatformResourcesManager.getPlatformForegroundColor();
    private Color backgroundColor = PlatformResourcesManager.getPlatformBackgroundColor();
    private Browser browserDescHtml;
    private LoadingComposite loadingComposite;

    private String description;
    private StackLayoutComposite stackLayoutComposite;
    private Exception pictureException;

    public DescriptionComposite(Composite parent, EntityFieldsComposite parentEntityFieldsComposite, int style) {
        super(parent, style);
        setLayout(new FillLayout(SWT.HORIZONTAL));
        stackLayoutComposite = new StackLayoutComposite(this, SWT.NONE);
        loadingComposite = new LoadingComposite(stackLayoutComposite, SWT.NONE, 128);
        stackLayoutComposite.showControl(loadingComposite);

        browserDescHtml = factory.createBrowser(stackLayoutComposite, SWT.NONE);
        browserDescHtml.addLocationListener(new LinkInterceptListener());
        browserDescHtml.addMouseWheelListener(new MouseWheelListener() {			
			@Override
			public void mouseScrolled(MouseEvent e) {
				parentEntityFieldsComposite.closeEntityComboBoxes();
			}
		});
    }

    public void setEntityModel(EntityModelWrapper entityModelWrapper) {
        browserDescHtml.setText(getBrowserText(entityModelWrapper.getReadOnlyEntityModel()));
    }

    private String getBrowserText(EntityModel entityModel) {
        String initialDescription = Util.getUiDataFromModel(entityModel.getValue((EntityFieldsConstants.FIELD_DESCRIPTION)));
        description = downloadPictures(entityModel);

        if (initialDescription.isEmpty()) {
            description = "No description";
        }
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append("<html><body style=\"background-color:" + getRgbString(backgroundColor) + ";\">");
        sb.append("<font style=\"color:" + getRgbString(foregroundColor) + "\">");
        sb.append(description);
        sb.append("</font>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    private static String getRgbString(Color color) {
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

    private String downloadPictures(EntityModel entityModel) {
        Job getImagesFromServerJob = new Job("Retrieving photos for description") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
                try {
                    description = Activator.getInstance(ImageService.class)
                            .downloadPictures(Util.getUiDataFromModel(entityModel.getValue((EntityFieldsConstants.FIELD_DESCRIPTION))));
                } catch (Exception ex) {
                    description = entityModel.getValue((EntityFieldsConstants.FIELD_DESCRIPTION)).getValue().toString();
                    pictureException = ex;
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };

        getImagesFromServerJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void scheduled(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    stackLayoutComposite.showControl(loadingComposite);
                });
            }

            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    if (description != null) {
                        browserDescHtml.setText(description);
                    }
                    stackLayoutComposite.showControl(browserDescHtml);
                    if(pictureException != null) {
                        ErrorDialog errDialog = new ErrorDialog(getParent().getShell());
                        errDialog.addButton("Close", () -> {
                            errDialog.close();
                        });
                        errDialog.displayException(pictureException, "Core Software Delivery Platform exception");   
                    }
                });
            }
        });

        getImagesFromServerJob.schedule();
        return description;
    }
}