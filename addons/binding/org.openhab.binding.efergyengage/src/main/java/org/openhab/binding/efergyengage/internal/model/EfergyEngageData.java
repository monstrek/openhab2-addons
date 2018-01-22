/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.efergyengage.internal.model;

import com.google.gson.JsonArray;

/**
 * The {@link EfergyEngageData} represents the model of
 * the efergy engage data.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class EfergyEngageData {
    String cid;
    JsonArray data;
    Integer age;

    public String getCid() {
        return cid;
    }

    public JsonArray getData() {
        return data;
    }

    public Integer getAge() {
        return age;
    }
}
