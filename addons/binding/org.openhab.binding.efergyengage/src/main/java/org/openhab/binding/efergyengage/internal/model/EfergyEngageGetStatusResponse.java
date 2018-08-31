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

import java.util.ArrayList;

/**
 * The {@link EfergyEngageGetStatusResponse} represents the model of
 * the efergy status data.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class EfergyEngageGetStatusResponse {
    ArrayList<EfergyEngageMac> listOfMacs;

    public ArrayList<EfergyEngageMac> getListOfMacs() {
        return listOfMacs;
    }
}
