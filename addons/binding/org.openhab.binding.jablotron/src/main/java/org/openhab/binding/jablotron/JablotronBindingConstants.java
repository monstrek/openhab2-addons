/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link JablotronBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronBindingConstants {

    private static final String BINDING_ID = "jablotron";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_OASIS = new ThingTypeUID(BINDING_ID, "oasis");
    public static final ThingTypeUID THING_TYPE_JA100 = new ThingTypeUID(BINDING_ID, "ja100");

    // Common channels
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_LAST_EVENT = "lastEvent";
    public static final String CHANNEL_LAST_EVENT_CLASS = "lastEventClass";
    public static final String CHANNEL_LAST_EVENT_TIME = "lastEventTime";
    public static final String CHANNEL_LAST_CHECK_TIME = "lastCheckTime";

    // List of all OASIS Channel ids
    public static final String CHANNEL_LAST_EVENT_CODE = "lastEventCode";
    public static final String CHANNEL_STATUS_A = "statusA";
    public static final String CHANNEL_STATUS_B = "statusB";
    public static final String CHANNEL_STATUS_ABC = "statusABC";
    public static final String CHANNEL_STATUS_PGX = "statusPGX";
    public static final String CHANNEL_STATUS_PGY = "statusPGY";

    // JA-100 channels
    public static final String CHANNEL_LAST_EVENT_SECTION = "lastEventSection";
    public static final String CHANNEL_STATUS_1 = "status_1";
    public static final String CHANNEL_STATUS_2 = "status_2";
    public static final String CHANNEL_STATUS_3 = "status_3";
    public static final String CHANNEL_STATUS_4 = "status_4";
    public static final String CHANNEL_STATUS_5 = "status_5";
    public static final String CHANNEL_STATUS_6 = "status_6";
    public static final String CHANNEL_STATUS_7 = "status_7";
    public static final String CHANNEL_STATUS_8 = "status_8";
    public static final String CHANNEL_STATUS_9 = "status_9";
    public static final String CHANNEL_STATUS_10 = "status_10";
    public static final String CHANNEL_STATUS_11 = "status_11";
    public static final String CHANNEL_STATUS_12 = "status_12";
    public static final String CHANNEL_STATUS_13 = "status_13";
    public static final String CHANNEL_STATUS_14 = "status_14";
    public static final String CHANNEL_STATUS_15 = "status_15";


    public static final String CHANNEL_STATUS_PGM_1 = "statusPGM_1";
    public static final String CHANNEL_STATUS_PGM_2 = "statusPGM_2";
    public static final String CHANNEL_STATUS_PGM_3 = "statusPGM_3";
    public static final String CHANNEL_STATUS_PGM_4 = "statusPGM_4";
    public static final String CHANNEL_STATUS_PGM_5 = "statusPGM_5";
    public static final String CHANNEL_STATUS_PGM_6 = "statusPGM_6";
    public static final String CHANNEL_STATUS_PGM_7 = "statusPGM_7";
    public static final String CHANNEL_STATUS_PGM_8 = "statusPGM_8";
    public static final String CHANNEL_STATUS_PGM_9 = "statusPGM_9";
    public static final String CHANNEL_STATUS_PGM_10 = "statusPGM_10";
    public static final String CHANNEL_STATUS_PGM_11 = "statusPGM_11";
    public static final String CHANNEL_STATUS_PGM_12 = "statusPGM_12";
    public static final String CHANNEL_STATUS_PGM_13 = "statusPGM_13";
    public static final String CHANNEL_STATUS_PGM_14 = "statusPGM_14";
    public static final String CHANNEL_STATUS_PGM_15 = "statusPGM_15";
    public static final String CHANNEL_STATUS_PGM_16 = "statusPGM_16";
    public static final String CHANNEL_STATUS_PGM_17 = "statusPGM_17";
    public static final String CHANNEL_STATUS_PGM_18 = "statusPGM_18";
    public static final String CHANNEL_STATUS_PGM_19 = "statusPGM_19";
    public static final String CHANNEL_STATUS_PGM_20 = "statusPGM_20";


    // Constants
    public static final String JABLOTRON_URL = "https://www.jablonet.net/";
    public static final String OASIS_SERVICE_URL = "app/oasis?service=";
    public static final String JA100_SERVICE_URL = "app/ja100?service=";
    public static final String AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36";
    public static final int TIMEOUT = 10;
}
