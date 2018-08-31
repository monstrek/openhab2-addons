/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.efergyengage.internal.model;

import com.google.gson.JsonArray;

/**
 * The {@link EfergyEngageMac} represents the model of
 * the efergy engage device.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class EfergyEngageMac {
    String mac;
    String type;
    String version;
    String status;

    public String getMac() {
        return mac;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }
}
