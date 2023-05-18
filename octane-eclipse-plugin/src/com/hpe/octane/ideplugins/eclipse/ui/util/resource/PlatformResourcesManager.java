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
package com.hpe.octane.ideplugins.eclipse.ui.util.resource;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class PlatformResourcesManager {
    
    public static Shell getActiveShell() {
        if(isRunningOnEclipsePlatform()) {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        } else {
            return Display.getCurrent().getActiveShell();
        }
    }

    public static Color getPlatformBackgroundColor() {
        if (isRunningOnEclipsePlatform()) {
            return getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
        } else {
            return SWTResourceManager.getColor(SWT.COLOR_WHITE);
        }
    }

    public static Color getPlatformForegroundColor() {
        if (isRunningOnEclipsePlatform()) {
            return getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
        } else {
            return SWTResourceManager.getColor(SWT.COLOR_BLACK);
        }
    }

    public static Image getPlatformImage(String platformImageConstant) {
        if (isRunningOnEclipsePlatform()) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(platformImageConstant);
        } else {
            return ImageResources.PLACEHOLDER.getImage();
        }
    }

    private static ColorRegistry getColorRegistry() {
        return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    }

    /**
     * Check if running on eclipse platform or not
     * 
     * @return
     */
    private static boolean isRunningOnEclipsePlatform() {
        //TODO: try to implement dynamically, currently used for debugging outside of the IDE
        return true;
    }

}
