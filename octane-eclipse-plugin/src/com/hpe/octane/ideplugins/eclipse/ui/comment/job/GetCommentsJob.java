/*******************************************************************************
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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
package com.hpe.octane.ideplugins.eclipse.ui.comment.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.CommentService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.Activator;

public class GetCommentsJob extends Job {

    private static final List<Entity> noCommentsEntites = Arrays.asList();

    private CommentService commentService = Activator.getInstance(CommentService.class);
    private Collection<EntityModel> comments = new ArrayList<>();
    private EntityModel parentEntity;
    private Exception exception;

    public GetCommentsJob(String name, EntityModel parentEntiy) {
        super(name);
        this.parentEntity = parentEntiy;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        try {
            comments = commentService.getComments(parentEntity);
        } catch (Exception exception) {
            this.exception = exception;
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    public Collection<EntityModel> getComents() {
        return comments;
    }

    public static boolean hasCommentSupport(Entity entity) {
        return !noCommentsEntites.contains(entity);
    }

    public Exception getException() {
        return exception;
    }
}
