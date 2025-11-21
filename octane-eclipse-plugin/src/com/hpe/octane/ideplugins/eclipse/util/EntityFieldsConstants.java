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
package com.hpe.octane.ideplugins.eclipse.util;

import java.util.HashSet;
import java.util.Set;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

public class EntityFieldsConstants {

    public static final String FIELD_ID = "id";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_SUBTYPE = "subtype";

    public static final String FIELD_DEFECT_TYPE = "defect_type";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_FEATURE = "parent";
    public static final String FIELD_QAOWNER = "qa_owner";
    public static final String FIELD_OWNER = "owner";
    public static final String FIELD_BLOCKED = "blocked";
    public static final String FIELD_BLOCKED_REASON = "blocked_reason";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_PHASE = "phase";
    public static final String FIELD_RELEASE = "release";
    public static final String FIELD_FIXEDINPUSH = "fixed_in_build";
    public static final String FIELD_SPRINT = "sprint";
    public static final String FIELD_DETECTEDBY = "detected_by";
    public static final String FIELD_SEVERITY = "severity";
    public static final String FIELD_ENVIROMENT = "taxonomies";
    public static final String FIELD_STORYPOINTS = "story_points";
    public static final String FIELD_DETECTEDINPPUSH = "detected_in_build";
    public static final String FIELD_DETECTEDINRELEASE = "detected_in_release";
    public static final String FIELD_APPMODULE = "product_areas";
    public static final String FIELD_DEV_STORY_POINTS = "dev_story_points";
    public static final String FIELD_QA_STORY_POINTS = "qa_story_points_f";
    public static final String FIELD_DESIGNER = "designer";
    public static final String FIELD_IS_MANUAL = "manual";
    public static final String FIELD_RUNS_IN_RELEASES = "run_in_releases";
    public static final String FIELD_TEST_SUBTYPE = "subtype";
    public static final String FIELD_TEST_TYPE = "test_type";
    public static final String FIELD_TESTING_TOOL_TYPE = "testing_tool_type";
    public static final String FIELD_LAST_RUNS = "last_runs";
    public static final String FIELD_QUALITY_STORY_TYPE = "quality_story_type";
    public static final String FIELD_CREATED = "created";
    public static final String FIELD_ESTIMATED_DURATTION = "estimated_duration";
    public static final String FIELD_REQUIREMENT_COVERAGE = "requirement_coverage";
    public static final String FIELD_RUN_SET = "run_set";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_CLOSED_ON = "closed_on";
    public static final String FIELD_COVERED_CONTENT = "covered_content";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_STORY = "story";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_CREATION_TIME = "creation_time";
    public static final String FIELD_INVESTED_HOURS = "invested_hours";
    public static final String FIELD_REMAINING_HOURS = "remaining_hours";
    public static final String FIELD_TASK_TYPE = "item_type";
    public static final String FIELD_LAST_MODIFIED = "last_modified";
    public static final String FIELD_ITEM_ORIGIN = "item_origin";
    public static final String FIELD_ESTIMATED_HOURS = "estimated_hours";
    public static final String FIELD_AUTOMATION_STATUS = "automation_status";
    public static final String FIELD_RANK = "rank";
    public static final String FIELD_ACTIVITY_LEVEL = "activity_level";

    // inner user prop
    public static final String FIELD_FULL_NAME = "full_name";

    // FOR TEST RUNS
    public static final String FIELD_TEST_NAME = "test_name";
    public static final String FIELD_TEST_RUN_NAME = "name";
    public static final String FIELD_TEST_RUN_NATIVE_STATUS = "native_status";
    public static final String FIELD_TEST_RUN_STARTED_DATE = "started";
    public static final String FIELD_TEST_RUN_CONTENT = "steps_num";
    public static final String FIELD_TEST_RUN_RUN_BY = "run_by";
    public static final String FIELD_TEST_RUN_STARTED_TIME = "started";
    public static final String FIELD_TEST_RUN_DRAFT_RUN = "draft_run";
    public static final String FIELD_TEST_RUN_DURATION = "duration";
    public static final String FIELD_TEST_RUN_VERSION = "test_version";

    // COMMENTS
    public static final String FIELD_COMMENT_TEXT = "text";

    // Fields that are white listed for verifications
    public static final Set<Entity> supportedEntitiesThatAllowDetailView = new HashSet<Entity>();
    static {
        supportedEntitiesThatAllowDetailView.add(Entity.USER_STORY);
        supportedEntitiesThatAllowDetailView.add(Entity.DEFECT);
        supportedEntitiesThatAllowDetailView.add(Entity.TASK);
        supportedEntitiesThatAllowDetailView.add(Entity.QUALITY_STORY);
        supportedEntitiesThatAllowDetailView.add(Entity.MANUAL_TEST);
        supportedEntitiesThatAllowDetailView.add(Entity.GHERKIN_TEST);
        supportedEntitiesThatAllowDetailView.add(Entity.MANUAL_TEST_RUN);
        supportedEntitiesThatAllowDetailView.add(Entity.TEST_SUITE);
        supportedEntitiesThatAllowDetailView.add(Entity.TEST_SUITE_RUN);
        supportedEntitiesThatAllowDetailView.add(Entity.REQUIREMENT);
        supportedEntitiesThatAllowDetailView.add(Entity.FEATURE);
        supportedEntitiesThatAllowDetailView.add(Entity.UNIT);
        supportedEntitiesThatAllowDetailView.add(Entity.MODEL);
        supportedEntitiesThatAllowDetailView.add(Entity.MANUAL_ACTION);
        supportedEntitiesThatAllowDetailView.add(Entity.AUTO_ACTION);
        supportedEntitiesThatAllowDetailView.add(Entity.QUALITY_GATE);
    }
}
