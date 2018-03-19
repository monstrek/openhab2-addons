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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.jablotron.config.DeviceConfig;
import org.openhab.binding.jablotron.internal.Utils;
import org.openhab.binding.jablotron.model.*;
import org.openhab.binding.jablotron.model.ja100.Ja100Event;
import org.openhab.binding.jablotron.model.ja100.Ja100StatusResponse;
import org.openhab.binding.jablotron.model.oasis.OasisEvent;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    private int stav_1 = 0;
    private int stav_2 = 0;
    private int stav_3 = 0;
    private int stav_4 = 0;
    private int stav_5 = 0;
    private int stav_6 = 0;
    private int stav_7 = 0;
    private int stav_8 = 0;
    private int stav_9 = 0;
    private int stav_10 = 0;
    private int stav_11 = 0;
    private int stav_12 = 0;
    private int stav_13 = 0;
    private int stav_14 = 0;
    private int stav_15 = 0;

    private int stavPGM_1 = 0;
    private int stavPGM_2 = 0;
    private int stavPGM_3 = 0;
    private int stavPGM_4 = 0;
    private int stavPGM_5 = 0;
    private int stavPGM_6 = 0;
    private int stavPGM_7 = 0;
    private int stavPGM_8 = 0;
    private int stavPGM_9 = 0;
    private int stavPGM_10 = 0;
    private int stavPGM_11 = 0;
    private int stavPGM_12 = 0;
    private int stavPGM_13 = 0;
    private int stavPGM_14 = 0;
    private int stavPGM_15 = 0;
    private int stavPGM_16 = 0;
    private int stavPGM_17 = 0;
    private int stavPGM_18 = 0;
    private int stavPGM_19 = 0;
    private int stavPGM_20 = 0;

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

    private void readAlarmStatus(Ja100StatusResponse response) {
        logger.debug("Reading alarm status...");

        stav_1 = response.getSekceStatus(0);
        stav_2 = response.getSekceStatus(1);
        stav_3 = response.getSekceStatus(2);
        stav_4 = response.getSekceStatus(3);
        stav_5 = response.getSekceStatus(4);
        stav_6 = response.getSekceStatus(5);
        stav_7 = response.getSekceStatus(6);
        stav_8 = response.getSekceStatus(7);
        stav_9 = response.getSekceStatus(8);
        stav_10 = response.getSekceStatus(9);
        stav_11 = response.getSekceStatus(10);
        stav_12 = response.getSekceStatus(11);
        stav_13 = response.getSekceStatus(12);
        stav_14 = response.getSekceStatus(13);
        stav_15 = response.getSekceStatus(14);

        stavPGM_1 = response.getPgmStatus(0);
        stavPGM_2 = response.getPgmStatus(1);
        stavPGM_3 = response.getPgmStatus(2);
        stavPGM_4 = response.getPgmStatus(3);
        stavPGM_5 = response.getPgmStatus(4);
        stavPGM_6 = response.getPgmStatus(5);
        stavPGM_7 = response.getPgmStatus(6);
        stavPGM_8 = response.getPgmStatus(7);
        stavPGM_9 = response.getPgmStatus(8);
        stavPGM_10 = response.getPgmStatus(9);
        stavPGM_11 = response.getPgmStatus(10);
        stavPGM_12 = response.getPgmStatus(11);
        stavPGM_13 = response.getPgmStatus(12);
        stavPGM_14 = response.getPgmStatus(13);
        stavPGM_15 = response.getPgmStatus(14);
        stavPGM_16 = response.getPgmStatus(15);
        stavPGM_17 = response.getPgmStatus(16);
        stavPGM_18 = response.getPgmStatus(17);
        stavPGM_19 = response.getPgmStatus(18);
        stavPGM_20 = response.getPgmStatus(19);

        logger.info("Status 1: {}", stav_1);
        logger.info("Status 2: {}", stav_2);
        logger.info("Status 3: {}", stav_3);
        logger.info("Status 4: {}", stav_4);
        logger.info("Status 5: {}", stav_5);
        logger.info("Status 6: {}", stav_6);
        logger.info("Status 7: {}", stav_7);
        logger.info("Status 8: {}", stav_8);
        logger.info("Status 9: {}", stav_9);
        logger.info("Status 10: {}", stav_10);
        logger.info("Status 11: {}", stav_11);
        logger.info("Status 12: {}", stav_12);
        logger.info("Status 13: {}", stav_13);
        logger.info("Status 14: {}", stav_14);
        logger.info("Status 15: {}", stav_15);

        logger.info("Status PGM 1: {}", stavPGM_1);
        logger.info("Status PGM 2: {}", stavPGM_2);
        logger.info("Status PGM 3: {}", stavPGM_3);
        logger.info("Status PGM 4: {}", stavPGM_4);
        logger.info("Status PGM 5: {}", stavPGM_5);
        logger.info("Status PGM 6: {}", stavPGM_6);
        logger.info("Status PGM 7: {}", stavPGM_7);
        logger.info("Status PGM 8: {}", stavPGM_8);
        logger.info("Status PGM 9: {}", stavPGM_9);
        logger.info("Status PGM 10: {}", stavPGM_10);
        logger.info("Status PGM 11: {}", stavPGM_11);
        logger.info("Status PGM 12: {}", stavPGM_12);
        logger.info("Status PGM 13: {}", stavPGM_13);
        logger.info("Status PGM 14: {}", stavPGM_14);
        logger.info("Status PGM 15: {}", stavPGM_15);
        logger.info("Status PGM 16: {}", stavPGM_16);
        logger.info("Status PGM 17: {}", stavPGM_17);
        logger.info("Status PGM 18: {}", stavPGM_18);
        logger.info("Status PGM 19: {}", stavPGM_19);
        logger.info("Status PGM 20: {}", stavPGM_20);


        logger.info("About to update thing's channel statuses...");
        for (Channel channel : getThing().getChannels()) {
            State newState = null;
            String type = channel.getUID().getId();

            logger.info("Updating status of channel {}", type);

            switch (type) {
                case CHANNEL_STATUS_1:
                    newState = new DecimalType(stav_1);
                    break;
                case CHANNEL_STATUS_2:
                    newState = new DecimalType(stav_2);
                    break;
                case CHANNEL_STATUS_3:
                    newState = new DecimalType(stav_3);
                    break;
                case CHANNEL_STATUS_4:
                    newState = new DecimalType(stav_4);
                    break;
                case CHANNEL_STATUS_5:
                    newState = new DecimalType(stav_5);
                    break;
                case CHANNEL_STATUS_6:
                    newState = new DecimalType(stav_6);
                    break;
                case CHANNEL_STATUS_7:
                    newState = new DecimalType(stav_7);
                    break;
                case CHANNEL_STATUS_8:
                    newState = new DecimalType(stav_8);
                    break;
                case CHANNEL_STATUS_9:
                    newState = new DecimalType(stav_9);
                    break;
                case CHANNEL_STATUS_10:
                    newState = new DecimalType(stav_10);
                    break;
                case CHANNEL_STATUS_11:
                    newState = new DecimalType(stav_11);
                    break;
                case CHANNEL_STATUS_12:
                    newState = new DecimalType(stav_12);
                    break;
                case CHANNEL_STATUS_13:
                    newState = new DecimalType(stav_13);
                    break;
                case CHANNEL_STATUS_14:
                    newState = new DecimalType(stav_14);
                    break;
                case CHANNEL_STATUS_15:
                    newState = new DecimalType(stav_15);
                    break;
                case CHANNEL_STATUS_PGM_1:
                    newState = (stavPGM_1 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_2:
                    newState = (stavPGM_2 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_3:
                    newState = (stavPGM_3 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_4:
                    newState = (stavPGM_4 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_5:
                    newState = (stavPGM_5 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_6:
                    newState = (stavPGM_6 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_7:
                    newState = (stavPGM_7 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_8:
                    newState = (stavPGM_8 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_9:
                    newState = (stavPGM_9 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_10:
                    newState = (stavPGM_10 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_11:
                    newState = (stavPGM_11 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_12:
                    newState = (stavPGM_12 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_13:
                    newState = (stavPGM_13 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_14:
                    newState = (stavPGM_14 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_15:
                    newState = (stavPGM_15 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_16:
                    newState = (stavPGM_16 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_17:
                    newState = (stavPGM_17 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_18:
                    newState = (stavPGM_18 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_19:
                    newState = (stavPGM_19 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                case CHANNEL_STATUS_PGM_20:
                    newState = (stavPGM_20 == 1) ? OnOffType.ON : OnOffType.OFF;
                    break;
                /*
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
                    */
                default:
                    break;
            }

            if (newState != null) {
                //eventPublisher.postUpdate(itemName, newState);
                updateState(channel.getUID(), newState);
            }
        }
    }

    private synchronized Ja100StatusResponse sendGetStatusRequest() {

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
            return gson.fromJson(line, Ja100StatusResponse.class);
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

            Ja100StatusResponse response = sendGetStatusRequest();

            if (response == null || response.getStatus() != 200) {
                session = "";
                //controlDisabled = true;
                //inService = false;
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
                ArrayList<OasisEvent> events = response.getEvents();
                for (OasisEvent event : events) {
                    logger.debug("Found event: {} {} {}", event.getDatum(), event.getCode(), event.getEvent());
                    //updateLastEvent(event);

                }
            } else {
                ArrayList<Ja100Event> history = getServiceHistory();
                logger.debug("History log contains {} events", history.size());
                if (history.size() > 0) {
                    Ja100Event event = history.get(0);
                    updateLastEvent(event);
                    logger.debug("Last event: {} is of class: {} has section: {}", event.getEvent(), event.getEventClass(), event.getSection());
                }
            }

            //inService = response.inService();
            inService = false;

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

    private void updateLastEvent(Ja100Event event) {
        //ZonedDateTime time = ZonedDateTime.parse("2018-03-15T18:07:32+01:00", DateTimeFormatter.ISO_DATE_TIME);
        //ZonedDateTime time = ZonedDateTime.parse(event.getDate(), DateTimeFormatter.ISO_DATE_TIME);
        //DateTimeType typ = new DateTimeType(time);
        updateChannel(CHANNEL_LAST_EVENT_TIME, event.getZonedDateTime());
        updateChannel(CHANNEL_LAST_EVENT_SECTION, event.getSection());
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

    private void updateChannel(String channelName, ZonedDateTime date) {
        for (Channel channel : getThing().getChannels()) {
            if (channel.getUID().getId().equals(channelName)) {
                updateState(channel.getUID(), new DateTimeType(date));
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
            /*
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
            }*/

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
            //controlDisabled = true;
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
            stav_1 = 0;
            stav_2 = 0;
            stav_3 = 0;
            stav_4 = 0;
            stav_5 = 0;
            stav_6 = 0;
            stav_7 = 0;
            stav_8 = 0;
            stav_9 = 0;
            stav_10 = 0;
            stav_11 = 0;
            stav_12 = 0;
            stav_13 = 0;
            stav_14 = 0;
            stav_15 = 0;

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

    private ArrayList<Ja100Event> getServiceHistory() {
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

            ArrayList<Ja100Event> result = new ArrayList<>();

            JsonParser parser = new JsonParser();
            JsonObject jobject = parser.parse(line).getAsJsonObject();
            if(jobject.has("ResponseCode") && jobject.get("ResponseCode").getAsInt() == 200) {
                logger.info("History successfully retrieved with total of {} events.", jobject.get("EventsCount").getAsInt());
                if(jobject.has("HistoryData")) {
                    jobject = jobject.get("HistoryData").getAsJsonObject();
                    if (jobject.has("Events")) {
                        JsonArray jarray = jobject.get("Events").getAsJsonArray();
                        logger.info("Parsing events...");
                        Ja100Event[] events = gson.fromJson(jarray, Ja100Event[].class);
                        result.addAll(Arrays.asList(events));
                        logger.info("Last event: {}", events[0].toString());
                    }
                }
            }

            /*
            if (jobject.has("events")) {
                jobject = jobject.get("events").getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
                    String key = entry.getKey();
                    if (jobject.get(key) instanceof JsonArray) {
                        OasisEvent[] events = gson.fromJson(jobject.get(key), OasisEvent[].class);
                        result.addAll(Arrays.asList(events));
                    }
                }
            }*/
            return result;
        } catch (Exception ex) {
            logger.error("Cannot get Jablotron service history: {}", serviceId, ex);
        }
        return null;
    }

}
