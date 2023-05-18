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
package com.hpe.octane.ideplugins.eclipse.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.Util;

/**
 * Util class for client side filtering of the data
 */
public class ArrayEntityListData implements EntityListData {

    public static interface DataChangedHandler {
        public void dataChanged(Collection<EntityModel> newData);
    }

    private List<DataChangedHandler> dataChangedHandlers = new ArrayList<>();

    // Source data
    private Collection<EntityModel> entityList = new ArrayList<>();

    // Filtering
    private Collection<EntityModel> filteredEntityList = new ArrayList<>();
    private Set<Entity> entityTypeFilter = new HashSet<>();
    private String query;
    private Set<String> queryFields = new HashSet<>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#setEntityList(
     * java.util.Collection)
     */
    @Override
    public void setEntityList(Collection<EntityModel> entityList) {
        this.entityList = entityList;
        applyFilter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#getEntityList()
     */
    @Override
    public Collection<EntityModel> getEntityList() {
        return filteredEntityList;
    }

    @Override
    public Collection<EntityModel> getOriginalEntityList() {
        return entityList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#setTypeFilter(
     * java.util.Set)
     */
    @Override
    public void setTypeFilter(Set<Entity> entityTypeFilter) {
        this.entityTypeFilter = entityTypeFilter;
        applyFilter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#setStringFilter(
     * java.lang.String)
     */
    @Override
    public void setStringFilter(String query) {
        this.query = query;
        applyFilter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#
     * setStringFilterFields(java.util.Set)
     */
    @Override
    public void setStringFilterFields(Set<String> queryFields) {
        this.queryFields = queryFields;
        applyFilter();
    }

    private void applyFilter() {
        Collection<EntityModel> filteredEntities;

        // Type filtering first
        filteredEntities = filterByEntityType(entityList, entityTypeFilter);

        if (StringUtils.isNotBlank(query)) {
            filteredEntities = filterByStringQuery(filteredEntities, query, queryFields);
        }

        filteredEntityList = filteredEntities;
        fireDataChangedHandlers();
    }

    private Collection<EntityModel> filterByEntityType(Collection<EntityModel> entityModelList, Set<Entity> entityTypeFilter) {
        // Type filtering first
        return entityModelList
                .stream()
                .filter(entityModel -> matchTypeFilter(entityModel, entityTypeFilter))
                .collect(Collectors.toList());
    }

    private Collection<EntityModel> filterByStringQuery(Collection<EntityModel> entityModelList, String queryString, Set<String> affectedFields) {
        Collection<EntityModel> result = new ArrayList<>();
        for (EntityModel entityModel : entityModelList) {
            if (matchStringFilter(entityModel, queryString, affectedFields)) {
                result.add(entityModel);
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#
     * matchStringFilter(com.hpe.adm.nga.sdk.model.EntityModel,
     * java.lang.String, java.util.Set)
     */
    @Override
    public boolean matchStringFilter(EntityModel entityModel, String queryString, Set<String> affectedFields) {
        for (String field : affectedFields) {
            if (entityModel.getValue(field) != null) {
                String fieldValue = Util.getUiDataFromModel(entityModel.getValue(field));
                fieldValue = fieldValue.replaceAll("\\s+", "");
                fieldValue = fieldValue.toLowerCase();
                queryString = queryString.toLowerCase();
                queryString = queryString.replaceAll("\\s+", "");
                if (fieldValue.contains(queryString)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#matchTypeFilter(
     * com.hpe.adm.nga.sdk.model.EntityModel, java.util.Set)
     */
    @Override
    public boolean matchTypeFilter(EntityModel entityModel, Set<Entity> entityTypeFilter) {
        return entityTypeFilter.contains(Entity.getEntityType(entityModel));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#
     * addDataChangedHandler(com.hpe.octane.ideplugins.eclipse.filter.
     * EntityListData.DataChangedHandler)
     */
    @Override
    public void addDataChangedHandler(DataChangedHandler dataChangedHandler) {
        dataChangedHandlers.add(dataChangedHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hpe.octane.ideplugins.eclipse.filter.IEntityListData#
     * removeDataChangedHandler(com.hpe.octane.ideplugins.eclipse.filter.
     * EntityListData.DataChangedHandler)
     */
    @Override
    public void removeDataChangedHandler(DataChangedHandler dataChangedHandler) {
        dataChangedHandlers.remove(dataChangedHandler);
    }

    private void fireDataChangedHandlers() {
        dataChangedHandlers.forEach(dataChangedHandler -> dataChangedHandler.dataChanged(filteredEntityList));
    }

    @Override
    public void add(EntityModel entityModel) {
        entityList.add(entityModel);
        applyFilter();
    }

    @Override
    public void remove(EntityModel entityModel) {
        entityList.remove(entityModel);
        applyFilter();
    }

}
