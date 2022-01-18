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
package com.hpe.octane.ideplugins.eclipse.filter;

import java.util.Set;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;

/**
 * Util class for client side filtering of user item entities
 */
public class UserItemArrayEntityListData extends ArrayEntityListData {

    @Override
    public boolean matchStringFilter(EntityModel entityModel, String queryString, Set<String> affectedFields) {
    	if (Entity.USER_ITEM == Entity.getEntityType(entityModel)) {
    		entityModel = MyWorkUtil.getEntityModelFromUserItem(entityModel);
    	}
        return super.matchStringFilter(entityModel, queryString, affectedFields);
    }

    @Override
    public boolean matchTypeFilter(EntityModel entityModel, Set<Entity> entityTypeFilter) {
    	if (Entity.USER_ITEM == Entity.getEntityType(entityModel)) {
    		entityModel = MyWorkUtil.getEntityModelFromUserItem(entityModel);
    	}
        return super.matchTypeFilter(entityModel, entityTypeFilter);
    }

}
