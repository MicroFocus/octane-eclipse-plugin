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
package com.hpe.octane.ideplugins.eclipse.ui.entitydetail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.octane.ideplugins.eclipse.ui.comment.EntityCommentComposite;
import com.hpe.octane.ideplugins.eclipse.ui.comment.job.GetCommentsJob;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.field.BooleanFieldEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.field.EntityComboBoxFieldEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.field.FieldEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.field.ReferenceFieldEditor;

public class EntityComposite extends Composite {

    private EntityCommentComposite entityCommentComposite;
    private EntityHeaderComposite entityHeaderComposite;
    private EntityFieldsComposite entityFieldsComposite;
    private ScrolledComposite scrolledComposite;
    private Label label;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public EntityComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));

        entityHeaderComposite = new EntityHeaderComposite(this, SWT.NONE);
        entityHeaderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

        label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        scrolledComposite = new ScrolledComposite(this, SWT.H_SCROLL |
                SWT.V_SCROLL);
        scrolledComposite.setMinSize(new Point(800, 600));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, true, 1, 1));

        entityFieldsComposite = new EntityFieldsComposite(scrolledComposite, SWT.NONE);
        entityFieldsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        entityFieldsComposite.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseScrolled(MouseEvent e) {				
				entityFieldsComposite.closeEntityComboBoxes();
			}
		});
        
        scrolledComposite.setContent(entityFieldsComposite);

        entityCommentComposite = new EntityCommentComposite(this, SWT.NONE);
        GridData entityCommentCompositeGridData = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
        entityCommentCompositeGridData.widthHint = 350;
        entityCommentComposite.setLayoutData(entityCommentCompositeGridData);

        entityHeaderComposite.addCommentsSelectionListener(new Listener() {
            @Override
            public void handleEvent(Event event) {
                setCommentsVisible(!entityCommentComposite.getVisible());
            }
        });
    }

    public void setCommentsVisible(boolean isVisible) {
        entityCommentComposite.setVisible(isVisible);
        ((GridData) entityCommentComposite.getLayoutData()).exclude = !isVisible;
        layout(true, true);
        redraw();
        update();
    }

    public void setEntityModel(EntityModelWrapper entityModelWrapper) {
        entityHeaderComposite.setEntityModel(entityModelWrapper);
        entityFieldsComposite.setEntityModel(entityModelWrapper);

        setCommentsVisible(false);
        if (GetCommentsJob.hasCommentSupport(entityModelWrapper.getEntityType())) {
            entityCommentComposite.setEntityModel(entityModelWrapper.getReadOnlyEntityModel());
        }
        setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
        layout(true, true);
        redraw();
        update();
    }

    public void addSaveSelectionListener(Listener listener) {
        entityHeaderComposite.addSaveSelectionListener(listener);
    }

    public void addRefreshSelectionListener(Listener listener) {
        entityHeaderComposite.addRefreshSelectionListener(listener);
    }

}
