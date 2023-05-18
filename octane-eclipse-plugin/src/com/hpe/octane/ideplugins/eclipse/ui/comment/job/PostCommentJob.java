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
package com.hpe.octane.ideplugins.eclipse.ui.comment.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.CommentService;
import com.hpe.octane.ideplugins.eclipse.Activator;

public class PostCommentJob extends Job {
    private EntityModel commentParentEntity;
    private String commentText;
    private CommentService commentService = Activator.getInstance(CommentService.class);
    private boolean isCommentSaved = false;
    private Exception exception;

    public PostCommentJob(String name, EntityModel entityModel, String commentText) {
        super(name);
        this.commentParentEntity = entityModel;
        this.commentText = commentText;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        try {
            commentService.postComment(commentParentEntity, commentText);
            isCommentSaved = true;
        } catch (Exception exception) {
            isCommentSaved = false;
            this.exception = exception;
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    public boolean isCommentsSaved() {
        return isCommentSaved;
    }

    public Exception getException() {
        return exception;
    }
}
