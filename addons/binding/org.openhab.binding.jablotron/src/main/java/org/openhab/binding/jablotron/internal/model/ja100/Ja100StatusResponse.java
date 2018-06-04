/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model.ja100;

import com.google.gson.*;
import org.openhab.binding.jablotron.handler.JablotronBridgeHandler;
import org.openhab.binding.jablotron.internal.model.oasis.OasisEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * The {@link Ja100StatusResponse} class defines the JA100 get status
 * response.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class Ja100StatusResponse {

    private Gson gson = new Gson();
    private JsonParser parser = new JsonParser();

    private final Logger logger = LoggerFactory.getLogger(JablotronBridgeHandler.class);

    //@SerializedName("last_entry")
    //OasisLastEntry lastEntry;
    int status;
    JsonElement sekce;
    JsonElement pgm;
    Integer isAlarm;
    //JsonElement alarm;
    //boolean controlDisabled;
    //int service;
    //JsonElement vypis;

    /*
    public OasisLastEntry getLast_entry() {
        return lastEntry;
    }
    */

    public int getStatus() {
        return status;
    }

    /*
    public JsonElement getVypis() {
        return vypis;
    }*/

    /*
    public int getService() {
        return service;
    }

    public int getIsAlarm() {
        return isAlarm;
    }
    */

    public boolean isOKStatus() {
        return status == 200;
    }

    public boolean isBusyStatus() {
        return status == 201;
    }

    public boolean isNoSessionStatus() {
        return status == 800;
    }


    public boolean inService() {
        //return service == 1;
        return false;
    }

    public boolean isAlarm() {
        return isAlarm != null && isAlarm.intValue() == 1;
    }

    public boolean hasEvents() {
        //return vypis != null && !vypis.equals(JsonNull.INSTANCE);
        return false;
    }

    public boolean hasSectionStatus() {
        return sekce != null && !sekce.equals(JsonNull.INSTANCE);
    }

    /*
    public Date getLastEventTime() {
        if (lastEntry != null) {
            return getZonedDateTime(lastEntry.cid.time);
        } else
            return null;
    }*/

    private Date getZonedDateTime(long lastEventTime) {
        Instant dt = Instant.ofEpochSecond(lastEventTime);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(dt, ZoneId.of("Europe/Prague"));
        return Date.from(zdt.toInstant());
    }

    public ArrayList<OasisEvent> getEvents() {
        if (!hasEvents()) {
            return null;
        }


        ArrayList<OasisEvent> result = new ArrayList<>();

        /*
        JsonObject jobject = vypis.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
            String key = entry.getKey();
            if (jobject.get(key) instanceof JsonObject) {
                //each day
                JsonObject event = jobject.get(key).getAsJsonObject();
                for (Map.Entry<String, JsonElement> eventEntry : event.entrySet()) {
                    String eventKey = eventEntry.getKey();
                    if (event.get(eventKey) instanceof JsonObject) {
                        OasisEvent ev = gson.fromJson(event.get(eventKey), OasisEvent.class);
                        result.add(ev);
                    }
                }

            }
        }*/
        return result;

    }

    public int getSekceStatus(int sekceId) {

        if (sekce.isJsonArray()) {
            if (sekce.getAsJsonArray().size() > sekceId) {
                JsonObject event = sekce.getAsJsonArray().get(sekceId).getAsJsonObject();
                return event.get("stav").getAsInt();
            }
            return 0;
        }

        if (sekce.isJsonObject()) {
            JsonObject jobject = sekce.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
                String key = entry.getKey();
                if (key.equals(String.valueOf(sekceId))) {
                    if (jobject.get(key) instanceof JsonObject) {
                        //each day
                        JsonObject event = jobject.get(key).getAsJsonObject();
                        return event.get("stav").getAsInt();
                    }
                }
            }
            return 0;
        }
        logger.error("Cannot parse sekce response: {}", sekce.getAsString());
        return 0;
    }

    public int getPgmStatus(int pgmId) {

        if (pgm.isJsonArray()) {
            if (pgm.getAsJsonArray().size() > pgmId) {
                JsonObject event = pgm.getAsJsonArray().get(pgmId).getAsJsonObject();
                return event.get("stav").getAsInt();
            }
            return 0;
        }

        if (pgm.isJsonObject()) {
            JsonObject jobject = pgm.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
                String key = entry.getKey();
                if (key.equals(String.valueOf(pgmId))) {
                    if (jobject.get(key) instanceof JsonObject) {
                        //each day
                        JsonObject event = jobject.get(key).getAsJsonObject();
                        return event.get("stav").getAsInt();
                    }
                }
            }
            return 0;
        }

        logger.error("Cannot parse pgm response: {}", pgm.getAsString());
        return 0;
    }
}
