/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vektiva;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VektivaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class VektivaBindingConstants {

    private static final String BINDING_ID = "vektiva";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SMARWI = new ThingTypeUID(BINDING_ID, "smarwi");

    // List of all Channel ids
    public static final String CHANNEL_CONTROL = "control";

    // commands
    public static final String COMMAND_OPEN = "open";
    public static final String COMMAND_CLOSE = "close";
    public static final String COMMAND_STOP = "stop";

    // response
    public static final String RESPONSE_OK = "OK";
}
