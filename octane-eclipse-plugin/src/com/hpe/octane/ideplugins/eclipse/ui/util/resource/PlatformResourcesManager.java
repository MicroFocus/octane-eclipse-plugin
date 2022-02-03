/*******************************************************************************
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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
