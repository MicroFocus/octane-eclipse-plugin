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
package com.hpe.octane.ideplugins.eclipse.ui.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;

import com.hpe.octane.ideplugins.eclipse.Activator;

/**
 * Intercept location events on swt {@link Browser} control
 */
public class LinkInterceptListener implements LocationListener {

    /**
     * method called when the user clicks a link but before the link is opened
     */
    @Override
    public void changing(LocationEvent event) {
        String urlString = event.location;
        if (urlString == null || "about:blank".equals(urlString)) {
            return;
        }

        try {
            URIBuilder url = new URIBuilder(urlString);

            if (url.getHost() != null) {
                String temporaryString = url.toString();
                URI finalUrl = new URI(temporaryString);
                OpenInBrowser.openURI(finalUrl);
                event.doit = false;
                return;
            }

            URI baseURI = new URI(Activator.getConnectionSettings().getBaseUrl());
            url.setHost(baseURI.getHost());
            url.setPort(baseURI.getPort());
            url.setScheme(baseURI.getScheme());

            String temporaryString = url.toString();
            URI finalUrl = new URI(temporaryString);
            OpenInBrowser.openURI(finalUrl);
            event.doit = false; // stop propagation
        } catch (URISyntaxException | IOException e) {
            // tough luck, continue propagation, it's better than
            // nothing
            event.doit = true;
        }
    }

    // method called after the link has been opened in place.
    @Override
    public void changed(LocationEvent event) {
        // Not used
    }
}
