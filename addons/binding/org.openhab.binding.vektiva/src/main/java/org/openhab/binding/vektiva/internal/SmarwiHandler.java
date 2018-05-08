/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.vektiva.internal;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.vektiva.internal.VektivaBindingConstants.*;

/**
 * The {@link SmarwiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SmarwiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmarwiHandler.class);

    private SmarwiConfiguration config;

    private HttpClient httpClient = new HttpClient();

    public SmarwiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            checkStatus();
            return;
        }

        if (channelUID.getId().equals(CHANNEL_CONTROL)) {
            logger.debug("Received command: {}", command.toString());
            String cmd = getSmarwiCommand(command);
            if (cmd.equals(COMMAND_OPEN) || cmd.equals(COMMAND_CLOSE) || cmd.equals(COMMAND_STOP)) {
                sendCommand(cmd);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (httpClient != null && httpClient.isStarted()) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                //silence
            }
        }
    }

    private String getSmarwiCommand(Command command) {
        switch (command.toString()) {
            case "UP":
                return COMMAND_OPEN;
            case "DOWN":
                return COMMAND_CLOSE;
            case "STOP":
                return COMMAND_STOP;
            default:
                logger.warn("Unknown smarwi command: {}!", command.toString());
                return "N/A";
        }
    }

    private void sendCommand(String cmd) {
        String url = "http://" + config.ip + "/cmd/" + cmd;

        try {
            ContentResponse resp = httpClient.newRequest(url).method(HttpMethod.GET).send();
            logger.debug("Response: {}", resp.getContentAsString());
            if (resp.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

        } catch (InterruptedException e) {
            logger.error("API execution has been interrupted", e);
            updateStatus(ThingStatus.OFFLINE);
        } catch (TimeoutException e) {
            logger.error("Timeout during API execution", e);
            updateStatus(ThingStatus.OFFLINE);
        } catch (ExecutionException e) {
            logger.error("Exception during API execution", e);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SmarwiConfiguration.class);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        logger.debug("IP address: {}", config.ip);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Cannot start http client!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot start http client!");
            return;
        }

        scheduler.scheduleWithFixedDelay(this::checkStatus, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void checkStatus() {
        String url = "http://" + config.ip + "/statusn";

        try {
            ContentResponse resp = httpClient.newRequest(url).method(HttpMethod.GET).send();
            logger.debug("status values: {}", resp.getContentAsString());
            if (resp.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
                String[] values = resp.getContentAsString().split("\n");

                updateProperty("type", getPropertyValue(values, "t"));
                updateProperty("fw", getPropertyValue(values, "fw"));
                updateProperty("rssi", getPropertyValue(values, "rssi"));
                updateProperty("name", getPropertyValue(values, "cid"));
                updateProperty("id", getPropertyValue(values, "id"));
                updateProperty("status", getPropertyValue(values, "s"));
                updateProperty("error", getPropertyValue(values, "e"));

                updateState(CHANNEL_CONTROL, getPropertyValue(values, "pos").equals("o") ? new PercentType(0) : new PercentType(100));
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "got response code: " + resp.getStatus());
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "exception during status getting");
        }
    }

    private String getPropertyValue(String[] values, String property) {
        for (String val : values) {
            String[] keyVal = val.split(":");
            if (keyVal.length != 2) continue;
            String key = keyVal[0];
            String value = keyVal[1];
            if(property.equals(key)) {
               return value;
            }
        }
        return "N/A";
    }
}
