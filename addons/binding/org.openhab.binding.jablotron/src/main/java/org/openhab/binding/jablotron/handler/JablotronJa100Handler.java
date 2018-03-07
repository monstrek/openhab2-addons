/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.handler;

import com.google.gson.*;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.jablotron.config.DeviceConfig;
import org.openhab.binding.jablotron.internal.Utils;
import org.openhab.binding.jablotron.model.JablotronControlResponse;
import org.openhab.binding.jablotron.model.JablotronEvent;
import org.openhab.binding.jablotron.model.JablotronLoginResponse;
import org.openhab.binding.jablotron.model.JablotronStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

/**
 * The {@link JablotronJa100Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronJa100Handler extends JablotronAlarmHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronJa100Handler.class);

    public JablotronJa100Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_COMMAND) && command instanceof StringType) {
            scheduler.schedule(() -> {
                sendCommand(command.toString(), thingConfig.getUrl());
            }, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public void initialize() {
        thingConfig = getConfigAs(DeviceConfig.class);
        scheduler.schedule(() -> {
            doInit();
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();
        logout();
        if (future != null) {
            future.cancel(true);
        }
    }

    private void doInit() {
        login();
        initializeService();

        future = scheduler.scheduleWithFixedDelay(() -> {
            updateAlarmStatus();
        }, 1, thingConfig.getRefresh(), TimeUnit.SECONDS);
    }

    private void readAlarmStatus(JablotronStatusResponse response) {
        logger.debug("Reading alarm status...");
        controlDisabled = response.isControlDisabled();

        stavA = response.getSekce().get(0).getStav();
        stavB = response.getSekce().get(1).getStav();
        stavABC = response.getSekce().get(2).getStav();

        logger.debug("Stav A: {}", stavA);
        logger.debug("Stav B: {}", stavB);
        logger.debug("Stav ABC: {}", stavABC);

        for (Channel channel : getThing().getChannels()) {
            State newState = null;
            String type = channel.getUID().getId();

            switch (type) {
                case CHANNEL_STATUS_A:
                    newState = (stavA == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_B:
                    newState = (stavB == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_ABC:
                    newState = (stavABC == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_ALARM:
                    newState = (response.isAlarm()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                    break;
                case CHANNEL_LAST_EVENT_TIME:
                    Date lastEvent = response.getLastEventTime();
                    if (lastEvent != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(lastEvent);
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.systemDefault());
                        newState = new DateTimeType(zdt);
                    }
                    break;
                default:
                    break;
            }

            if (newState != null) {
                //eventPublisher.postUpdate(itemName, newState);
                updateState(channel.getUID(), newState);
            }
        }
    }

    private synchronized JablotronStatusResponse sendGetStatusRequest() {

        String url = JABLOTRON_URL + "app/ja100/ajax/stav.php?" + Utils.getBrowserTimestamp();
        try {
            URL cookieUrl = new URL(url);

            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Referer", JABLOTRON_URL + JA100_SERVICE_URL + thingConfig.getServiceId());
            connection.setRequestProperty("Cookie", session);
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            setConnectionDefaults(connection);

            String line = Utils.readResponse(connection);
            logger.info("getStatus response: {}", line);
            return gson.fromJson(line, JablotronStatusResponse.class);
        } catch (SocketTimeoutException ste) {
            logger.error("Timeout during getting alarm status!");
            return null;
        } catch (Exception e) {
            logger.error("sendGetStatusRequest exception", e);
            return null;
        }
    }

    private synchronized boolean updateAlarmStatus() {
        logger.debug("updating alarm status...");

        try {
            // relogin every hour
            int hours = Utils.getHoursOfDay();
            if (lastHours >= 0 && lastHours != hours) {
                relogin();
            } else {
                initializeService(false);
            }
            lastHours = hours;

            JablotronStatusResponse response = sendGetStatusRequest();

            if (response == null || response.getStatus() != 200) {
                session = "";
                controlDisabled = true;
                inService = false;
                login();
                initializeService();
                response = sendGetStatusRequest();
            }
            if (response.isBusyStatus()) {
                logger.warn("JA100 is busy...giving up");
                logout();
                return false;
            }
            if (response.hasEvents()) {
                ArrayList<JablotronEvent> events = response.getEvents();
                for (JablotronEvent event : events) {
                    logger.debug("Found event: {} {} {}", event.getDatum(), event.getCode(), event.getEvent());
                    updateLastEvent(event);

                }
            } else {
                ArrayList<JablotronEvent> history = getServiceHistory();
                logger.debug("History log contains {} events", history.size());
                if (history.size() > 0) {
                    JablotronEvent event = history.get(0);
                    updateLastEvent(event);
                    logger.debug("Last event: {} is of class: {} has code: {}", event.getEvent(), event.getEventClass(), event.getCode());
                }
            }

            inService = response.inService();

            if (inService) {
                logger.warn("Alarm is in service mode...");
                return false;
            }

            if (response.isOKStatus() && response.hasSectionStatus()) {
                readAlarmStatus(response);
            } else {
                logger.error("Cannot get alarm status!");
                session = "";
                return false;
            }
            for (Channel channel : getThing().getChannels()) {
                String type = channel.getUID().getId();
                if (type.equals(CHANNEL_LAST_CHECK_TIME)) {
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(Calendar.getInstance().toInstant(), ZoneId.systemDefault());
                    State status = new DateTimeType(zdt);
                    updateState(channel.getUID(), status);
                }
            }

            return true;
        } catch (Exception ex) {
            logger.error("Error during alarm status update", ex);
            return false;
        }
    }

    private void relogin() {
        logger.debug("Doing relogin");
        logout(false);
        login();
        initializeService(false);
    }

    private void updateLastEvent(JablotronEvent event) {
        updateChannel(CHANNEL_LAST_EVENT_CODE, event.getCode());
        updateChannel(CHANNEL_LAST_EVENT, event.getEvent());
        updateChannel(CHANNEL_LAST_EVENT_CLASS, event.getEventClass());
    }

    private void updateChannel(String channelName, String text) {
        for (Channel channel : getThing().getChannels()) {
            if (channel.getUID().getId().equals(channelName)) {
                updateState(channel.getUID(), new StringType(text));
            }
        }
    }


    public synchronized void sendCommand(String code, String serviceUrl) {
        int status = 0;
        Integer result = 0;
        try {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                login();
                initializeService();
            }
            if (!updateAlarmStatus()) {
                logger.error("Cannot send user code due to alarm status!");
                return;
            }
            int timeout = 30;
            while (controlDisabled && --timeout >= 0) {
                logger.info("Waiting for control enabling...");
                Thread.sleep(1000);
                boolean ok = updateAlarmStatus();
                if (!ok) {
                    return;
                }
            }
            if (timeout < 0) {
                logger.warn("Timeout during waiting for control enabling");
                return;
            }

            JablotronControlResponse response = sendUserCode("", serviceUrl);
            if (response == null) {
                logger.warn("null response received");
                return;
            }

            status = response.getStatus();
            result = response.getVysledek();
            if (result != null) {
                if (status == 200 && result == 4) {
                    logger.debug("Sending user code: {}", code);
                    response = sendUserCode(code, serviceUrl);
                } else {
                    logger.warn("Received unknown status: {}", status);
                }
                if (response != null && response.getVysledek() != null) {
                    handleHttpRequestStatus(response.getStatus());
                } else {
                    logger.warn("null response/status received");
                    logout();
                }
            } else {
                logger.warn("null status received");
                logout();
            }
        } catch (Exception e) {
            logger.error("internalReceiveCommand exception", e);
        }
    }

    private void handleHttpRequestStatus(int status) throws InterruptedException {
        switch (status) {
            case 0:
                logout();
                break;
            case 201:
                logout();
                break;
            case 300:
                logger.error("Redirect not supported");
                break;
            case 800:
                login();
                initializeService();
                break;
            case 200:
                scheduler.schedule((Runnable) this::updateAlarmStatus, 1, TimeUnit.SECONDS);
                scheduler.schedule((Runnable) this::updateAlarmStatus, 15, TimeUnit.SECONDS);
                break;
            default:
                logger.error("Unknown status code received: {}", status);
        }
    }

    private synchronized JablotronControlResponse sendUserCode(String code, String serviceUrl) {
        String url;

        try {
            url = JABLOTRON_URL + "app/ja100/ajax/ovladani.php";
            String urlParameters = "section=STATE&status=" + ((code.isEmpty()) ? "1" : "") + "&code=" + code;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            JablotronControlResponse response;

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Referer", serviceUrl);
            connection.setRequestProperty("Cookie", session);
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            setConnectionDefaults(connection);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }
            String line = Utils.readResponse(connection);
            logger.info("sendUserCode response: {}", line);
            response = gson.fromJson(line, JablotronControlResponse.class);

            logger.debug("sendUserCode result: {}", response.getVysledek());
            return response;
        } catch (Exception ex) {
            logger.error("sendUserCode exception", ex);
        }
        return null;
    }

    private void logout(boolean setOffline) {

        String url = JABLOTRON_URL + "logout";
        try {
            URL cookieUrl = new URL(url);

            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Referer", JABLOTRON_URL + JA100_SERVICE_URL + thingConfig.getServiceId());
            connection.setRequestProperty("Cookie", session);
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            setConnectionDefaults(connection);

            String line = Utils.readResponse(connection);
            logger.debug("logout... {}", line);
        } catch (Exception e) {
            //Silence
        } finally {
            controlDisabled = true;
            inService = false;
            session = "";
            if (setOffline) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    private void logout() {
        logout(true);
    }

    private void setConnectionDefaults(HttpsURLConnection connection) {
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", AGENT);
        connection.setRequestProperty("Accept-Language", "cs-CZ");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setUseCaches(false);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
    }

    private synchronized void login() {
        String url = null;

        try {
            //login
            stavA = 0;
            stavB = 0;
            stavABC = 0;

            JablotronBridgeHandler bridge = (JablotronBridgeHandler) this.getBridge().getHandler();
            if (bridge == null) {
                logger.error("Bridge handler is null!");
                return;
            }
            url = JABLOTRON_URL + "ajax/login.php";
            String urlParameters = "login=" + bridge.bridgeConfig.getLogin() + "&heslo=" + bridge.bridgeConfig.getPassword() + "&aStatus=200&loginType=Login";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Referer", JABLOTRON_URL);
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            setConnectionDefaults(connection);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            String line = Utils.readResponse(connection);
            logger.info("Login response: {}", line);
            JablotronLoginResponse response = gson.fromJson(line, JablotronLoginResponse.class);

            if (!response.isOKStatus())
                return;

            //get cookie
            session = Utils.getSessionCookie(connection);
            if (!session.equals("")) {
                logger.debug("Successfully logged to Jablonet cloud!");
            } else {
                logger.error("Cannot log in to Jablonet cloud!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot login to Jablonet cloud");
            }

        } catch (MalformedURLException e) {
            logger.error("The URL '{}' is malformed", url, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot login to Jablonet cloud");
        } catch (Exception e) {
            logger.error("Cannot get Jablotron login cookie", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot login to Jablonet cloud");
        }
    }

    private void initializeService() {
        initializeService(true);
    }

    private void initializeService(boolean verbose) {
        String url = thingConfig.getUrl();
        String serviceId = thingConfig.getServiceId();
        try {
            URL cookieUrl = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Referer", JABLOTRON_URL);
            connection.setRequestProperty("Cookie", session);
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            setConnectionDefaults(connection);

            if (connection.getResponseCode() == 200) {
                if (verbose) {
                    logger.info("Jablotron JA100 service: {} successfully initialized", serviceId);
                } else {
                    logger.debug("Jablotron JA100 service: {} successfully initialized", serviceId);
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.error("Cannot initialize Jablotron service: {}", serviceId);
                logger.error("Got response code: {} and message: {}", connection.getResponseCode(), connection.getResponseMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot initialize JA100 service");
            }
        } catch (Exception ex) {
            logger.error("Cannot initialize Jablotron service: {}", serviceId, ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot initialize JA100 service");
        }
    }

    private ArrayList<JablotronEvent> getServiceHistory() {
        String serviceId = thingConfig.getServiceId();
        try {
            URL cookieUrl = new URL("https://www.jablonet.net/app/ja100/ajax/historie.php");
            String urlParameters = "from=this_month&to=&gps=0&log=0&header=0";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            HttpsURLConnection connection = (HttpsURLConnection) cookieUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Referer", JABLOTRON_URL);
            connection.setRequestProperty("Cookie", session);
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            setConnectionDefaults(connection);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }
            String line = Utils.readResponse(connection);
            logger.info("History response: {}", line);

            ArrayList<JablotronEvent> result = new ArrayList<>();

            JsonParser parser = new JsonParser();
            JsonObject jobject = parser.parse(line).getAsJsonObject();
            if (jobject.has("events")) {
                jobject = jobject.get("events").getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
                    String key = entry.getKey();
                    if (jobject.get(key) instanceof JsonArray) {
                        JablotronEvent[] events = gson.fromJson(jobject.get(key), JablotronEvent[].class);
                        result.addAll(Arrays.asList(events));
                    }
                }
            }
            return result;
        } catch (Exception ex) {
            logger.error("Cannot get Jablotron service history: {}", serviceId, ex);
        }
        return null;
    }

}
