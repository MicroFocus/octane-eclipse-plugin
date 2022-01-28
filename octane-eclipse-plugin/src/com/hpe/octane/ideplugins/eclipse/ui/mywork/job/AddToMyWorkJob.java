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
package com.hpe.octane.ideplugins.eclipse.ui.mywork.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.octane.ideplugins.eclipse.Activator;

public class AddToMyWorkJob extends Job {

    MyWorkService myWorkService = Activator.getInstance(MyWorkService.class);
    EntityModel entityModel;
    private boolean wasAdded = false;

    public AddToMyWorkJob(String name, EntityModel entityModel) {
        super(name);
        this.entityModel = entityModel;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        wasAdded = myWorkService.addToMyWork(entityModel);
        monitor.done();
        return Status.OK_STATUS;
    }

    public boolean wasAdded() {
        return wasAdded;
    }

}
