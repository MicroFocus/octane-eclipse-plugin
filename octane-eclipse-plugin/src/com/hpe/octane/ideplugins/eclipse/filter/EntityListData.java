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
package com.hpe.octane.ideplugins.eclipse.filter;

import java.util.Collection;
import java.util.Set;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.filter.ArrayEntityListData.DataChangedHandler;

public interface EntityListData {

    void setEntityList(Collection<EntityModel> entityList);

    Collection<EntityModel> getEntityList();

    // Get the unfiltered list
    Collection<EntityModel> getOriginalEntityList();

    void add(EntityModel entityModel);

    void remove(EntityModel entityModel);

    void setTypeFilter(Set<Entity> entityTypeFilter);

    void setStringFilter(String query);

    void setStringFilterFields(Set<String> queryFields);

    boolean matchStringFilter(EntityModel entityModel, String queryString, Set<String> affectedFields);

    boolean matchTypeFilter(EntityModel entityModel, Set<Entity> entityTypeFilter);

    void addDataChangedHandler(DataChangedHandler dataChangedHandler);

    void removeDataChangedHandler(DataChangedHandler dataChangedHandler);

}
