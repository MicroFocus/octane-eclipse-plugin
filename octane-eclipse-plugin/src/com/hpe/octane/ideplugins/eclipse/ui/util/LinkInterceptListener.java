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
