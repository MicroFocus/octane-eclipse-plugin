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
package com.hpe.octane.ideplugins.eclipse.ui.entitydetail.job;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.MetadataService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class GetEntityModelJob extends Job {

    private long entityId;
    private Entity entityType;
    private EntityModel retrivedEntity;    
    private EntityService entityService = Activator.getInstance(EntityService.class);
    private MetadataService metadataService =  Activator.getInstance(MetadataService.class);
    
    private Exception exception;

    public GetEntityModelJob(String name, Entity entityType, long entityId) {
        super(name);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        try {      
            Set<String> fields = metadataService
                        .getVisibleFields(this.entityType)
                        .stream()
                        .map(fieldMetadata -> fieldMetadata.getName())
                        .collect(Collectors.toSet());
            
            //Explicitly ask for client lock stamp
            if(metadataService.hasClientLockStampSupport(this.entityType)) {
                fields.add(MetadataService.FIELD_CLIENT_LOCK_STAMP);
            }
            
            retrivedEntity = entityService.findEntity(this.entityType, this.entityId, fields);
            
            if(entityType.isSubtype()) {
                retrivedEntity.setValue(new StringFieldModel(EntityFieldsConstants.FIELD_SUBTYPE, entityType.getSubtypeName()));
            }
            
            exception = null;
        } catch (Exception exception) {
        	this.exception = exception;
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    public boolean wasEntityRetrived() {
    	return exception == null;
    }

    public EntityModel getEntiyData() {
        return retrivedEntity;
    }

	public Exception getException() {
		return exception;
	}

}