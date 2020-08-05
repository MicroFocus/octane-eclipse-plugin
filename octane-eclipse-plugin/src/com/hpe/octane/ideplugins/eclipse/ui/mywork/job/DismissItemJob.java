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
package com.hpe.octane.ideplugins.eclipse.ui.mywork.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.CommentService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.octane.ideplugins.eclipse.Activator;

public class DismissItemJob extends Job {

    MyWorkService myWorkService = Activator.getInstance(MyWorkService.class);
    EntityModel entityModel;
    private boolean wasRemoved = false;
   
    private CommentService commentService = Activator.getInstance(CommentService.class);

    public DismissItemJob(String name, EntityModel entityModel) {
        super(name);
        this.entityModel = entityModel;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        if (entityModel.getType().equals("comment")) {
        	wasRemoved = commentService.dismissComment(entityModel);
        } 
        else {
        	wasRemoved = myWorkService.removeFromMyWork(entityModel);
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    public boolean wasRemoved() {
        return wasRemoved;
    }

}
