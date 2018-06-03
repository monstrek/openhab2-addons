/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vektiva.internal.net;

import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.openhab.binding.vektiva.handler.VektivaSmarwiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VektivaSmarwiiSocket} class defines websocket used for connection with
 * the Smarwi thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class VektivaSmarwiiSocket extends WebSocketAdapter {
    private final Logger logger = LoggerFactory.getLogger(VektivaSmarwiHandler.class);
    private VektivaSmarwiHandler handler;

    public VektivaSmarwiiSocket(VektivaSmarwiHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        logger.trace("Got message: {}", message);
        if (handler != null) {
            handler.processStatusResponse(message);
        }
    }
}
