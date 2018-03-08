/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.model;

import com.google.gson.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Ja100StatusResponse {

    private Gson gson = new Gson();
    private JsonParser parser = new JsonParser();

    //@SerializedName("last_entry")
    //JablotronLastEntry lastEntry;
    int status;
    JsonElement sekce;
    JsonElement pgm;
    //boolean controlDisabled;
    //int service;
    //int isAlarm;
    JsonElement vypis;

    /*
    public JablotronLastEntry getLast_entry() {
        return lastEntry;
    }
    */

    public int getStatus() {
        return status;
    }

    public JsonElement getVypis() {
        return vypis;
    }

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

    /*
    public boolean inService() {
        return service == 1;
    }

    public boolean isAlarm() {
        return isAlarm == 1;
    }
    */

    public boolean hasEvents() {
        return vypis != null && !vypis.equals(JsonNull.INSTANCE);
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

    public ArrayList<JablotronEvent> getEvents() {
        if (!hasEvents()) {
            return null;
        }

        ArrayList<JablotronEvent> result = new ArrayList<>();

        JsonObject jobject = vypis.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
            String key = entry.getKey();
            if (jobject.get(key) instanceof JsonObject) {
                //each day
                JsonObject event = jobject.get(key).getAsJsonObject();
                for (Map.Entry<String, JsonElement> eventEntry : event.entrySet()) {
                    String eventKey = eventEntry.getKey();
                    if (event.get(eventKey) instanceof JsonObject) {
                        JablotronEvent ev = gson.fromJson(event.get(eventKey), JablotronEvent.class);
                        result.add(ev);
                    }
                }

            }
        }
        return result;
    }

    public int getSekceStatus(int position) {
        int i = 0;
        JsonObject jobject = sekce.getAsJsonObject();

        if (jobject.entrySet().size() <= position) {
            return 0;
        }

        for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
            if (i == position) {
                String key = entry.getKey();
                if (jobject.get(key) instanceof JsonObject) {
                    //each day
                    JsonObject event = jobject.get(key).getAsJsonObject();
                    return event.get("stav").getAsInt();
                }
            }
            i++;
        }
        return 0;
    }

    public int getPgmStatus(int position) {

        int i = 0;
        JsonObject jobject = pgm.getAsJsonObject();

        if (jobject.entrySet().size() <= i) {
            return 0;
        }

        for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
            if (i == position) {
                String key = entry.getKey();
                if (jobject.get(key) instanceof JsonObject) {
                    //each day
                    JsonObject event = jobject.get(key).getAsJsonObject();
                    return event.get("stav").getAsInt();
                }
            }
            i++;
        }
        return 0;

    }
}
