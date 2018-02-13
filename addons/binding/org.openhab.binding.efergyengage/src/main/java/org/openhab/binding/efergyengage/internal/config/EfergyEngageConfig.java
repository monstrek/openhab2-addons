/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.efergyengage.internal.config;

/**
 * The {@link EfergyEngageConfig} is responsible for representing the
 * Efergy Engage Hub configuration.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class EfergyEngageConfig {

    private String token;
    private int utcOffset;
    private int refresh;
    private String thingUid;

    public String getToken() {
        return token;
    }

    public int getUtcOffset() {
        return utcOffset;
    }

    public int getRefresh() {
        return refresh;
    }

    public void setThingUid(String thingUid) {
        this.thingUid = thingUid;
    }

    public String getThingUid() {
        return thingUid;
    }
}
