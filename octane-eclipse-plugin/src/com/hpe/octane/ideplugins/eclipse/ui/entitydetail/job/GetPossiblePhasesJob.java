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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class GetPossiblePhasesJob extends Job {
	
    private static final List<Entity> noPhaseEntites = Arrays.asList(Entity.MANUAL_TEST_RUN, Entity.TEST_SUITE_RUN, Entity.TEST_SUITE);
	
    private EntityService entityService = Activator.getInstance(EntityService.class);
    private EntityModel entityModel;
    private Collection<EntityModel> possibleTransitions;
    
    private Exception exception;

    public GetPossiblePhasesJob(String name, EntityModel entityModel) {
        super(name);
        this.entityModel = entityModel;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        try {
            @SuppressWarnings("rawtypes")
            FieldModel currentPhase = entityModel.getValue(EntityFieldsConstants.FIELD_PHASE);
            String currentPhaseId = Util.getUiDataFromModel(currentPhase, EntityFieldsConstants.FIELD_ID);
            possibleTransitions = entityService.findPossibleTransitionFromCurrentPhase(Entity.getEntityType(entityModel), currentPhaseId);
        } catch (Exception octaneException) {
            this.exception = octaneException;
        }
        monitor.done();
        return Status.OK_STATUS;
    }

	public Collection<EntityModel> getPossibleTransitions() {
		return possibleTransitions;
	}
	
	public Collection<EntityModel> getNoTransitionPhase(){
		if (possibleTransitions.isEmpty()) {
			possibleTransitions.add(new EntityModel("target_phase", "No transition"));
			return possibleTransitions;
		}
		return null;	
	}
	
	public static boolean hasPhases(Entity entity) {
		return !noPhaseEntites.contains(entity);
	}
	
	public Exception getException() {
	    return exception;
	}

}