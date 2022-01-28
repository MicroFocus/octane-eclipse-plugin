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
package com.hpe.octane.ideplugins.eclipse.ui.search;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.filter.EntityListData;

public class SearchJob extends Job {

    private EntitySearchService searchService = Activator.getInstance(EntitySearchService.class);
    private boolean isCancelled = false;
    private EntityListData resultEntityListData;
    private String query;
    private Entity[] searchEntityTypes;

    public SearchJob(String name, String query, EntityListData resultEntityListData, Entity... searchEntityTypes) {
        super(name);
        this.resultEntityListData = resultEntityListData;
        this.query = query;
        this.searchEntityTypes = searchEntityTypes;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        isCancelled = false;

        if (query.isEmpty()) {
            return Status.OK_STATUS;
        }
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        Collection<EntityModel> searchResults = searchService.searchGlobal(
                query,
                20,
                this.searchEntityTypes);

        if (!isCancelled) {
            Display.getDefault().asyncExec(() -> {
                resultEntityListData.setEntityList(searchResults);
            });
        }

        monitor.done();
        return Status.OK_STATUS;
    }

    @Override
    protected void canceling() {
        isCancelled = true;
    }

}
