/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal.service;

import com.google.gson.*;
import org.apache.http.*;
import org.eclipse.smarthome.core.library.types.*;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.philipstv.handler.*;
import org.openhab.binding.philipstv.internal.service.model.*;
import org.slf4j.*;

import java.io.*;
import java.security.*;

import static org.openhab.binding.philipstv.PhilipsTvBindingConstants.*;
import static org.openhab.binding.philipstv.internal.service.KeyCode.*;

/**
 * The {@link PowerService} is responsible for handling power states commands, which are sent to the
 * power channel.
 * @author Benjamin Meyer - Initial contribution
 */
public class PowerService implements PhilipsTvService {

  public static final int DELAY_MILLIS = 2 * 1000;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ConnectionService connectionService = new ConnectionService();

  @Override
  public void handleCommand(String channel, Command command, PhilipsTvHandler handler) {
    try {
      if (command instanceof RefreshType) {
        String powerState = getPowerState(handler.credentials, handler.target);
        if (powerState.equals(POWER_ON)) {
          handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
        } else if (powerState.equals(POWER_OFF)) {
          handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "");
        }
      } else if (command instanceof OnOffType) {
        setPowerState(handler.credentials, command, handler.target);
        if (command == OnOffType.ON) {
          handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Tv turned on.");
        } else {
          handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Tv turned off.");
        }
      } else {
        logger.warn("Unknown command: {} for Channel {}", command, channel);
      }
    } catch (Exception e) {
      if (isTvOfflineException(e)) {
        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
      } else if (isTvNotListeningException(e)) {
        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, TV_NOT_LISTENING_MSG);
      } else {
        logger.error("Unexpected Error handling the PowerState command {} for Channel {}: {}", command, channel, e.getMessage(), e);
      }
    }
  }

  private String getPowerState(CredentialDetails credentials,
      HttpHost target) throws IOException, ParseException, JsonSyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    String jsonContent = connectionService.doHttpsGet(credentials, target, TV_POWERSTATE_PATH);
    JsonObject jsonObject = new JsonParser().parse(jsonContent).getAsJsonObject();
    String powerState = jsonObject.get("powerstate").getAsString();
    return powerState.equalsIgnoreCase(POWER_ON) ? POWER_ON : POWER_OFF;
  }

  private void setPowerState(CredentialDetails credentials, Command command,
      HttpHost target) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    JsonObject powerStateJson = new JsonObject();
    if (command.equals(OnOffType.ON)) {
      powerStateJson.addProperty("powerstate", POWER_ON);
    } else { // OFF
      powerStateJson.addProperty("powerstate", KEY_STANDBY.toString());
    }
    connectionService.doHttpsPost(credentials, target, TV_POWERSTATE_PATH, powerStateJson.toString());
  }
}
