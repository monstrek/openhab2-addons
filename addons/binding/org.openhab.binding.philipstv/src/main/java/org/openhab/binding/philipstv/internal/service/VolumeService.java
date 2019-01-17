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
 * The {@link VolumeService} is responsible for handling volume commands, which are sent to the
 * volume channel or mute channel.
 * @author Benjamin Meyer - Initial contribution
 */
public class VolumeService implements PhilipsTvService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ConnectionService connectionService = new ConnectionService();

  @Override
  public void handleCommand(String channel, Command command, PhilipsTvHandler handler) {
    if (command instanceof RefreshType) {
      if (CHANNEL_VOLUME.equals(channel)) {
        try {
          VolumeDetails volumeDetails = getVolume(handler.credentials, handler.target);
          handler.postUpdateChannel(CHANNEL_VOLUME, new DecimalType(volumeDetails.getCurrentVolume()));
          handler.postUpdateChannel(CHANNEL_MUTE, volumeDetails.isMuted() ? OnOffType.ON : OnOffType.OFF);
        } catch (Exception e) {
          if (isTvOfflineException(e)) {
            logger.warn("Could not refresh TV volume: TV is offline.");
            handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
          } else if (isTvNotListeningException(e)) {
            handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, TV_NOT_LISTENING_MSG);
          } else {
            logger.error("Error retrieving the Volume: {}", e.getMessage(), e);
          }
        }
      }
    } else if (command instanceof DecimalType) {
      try {
        setVolume(handler.credentials, command, handler.target);
        handler.postUpdateChannel(CHANNEL_VOLUME, (DecimalType) command);
      } catch (Exception e) {
        if (isTvOfflineException(e)) {
          logger.warn("Could not execute command for TV volume: TV is offline.");
          handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
        } else {
          logger.error("Error during the setting of Volume: {}", e.getMessage(), e);
        }
      }
    } else if (CHANNEL_MUTE.equals(channel) && (command instanceof OnOffType)) {
      try {
        setMute(handler.credentials, command, handler.target);
      } catch (Exception e) {
        if (isTvOfflineException(e)) {
          logger.warn("Could not execute Mute command: TV is offline.");
          handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
        } else {
          logger.error("Unknown error occurred during setting of Mute: {}", e.getMessage(), e);
        }
      }
    } else {
      logger.error("Unknown command: {} for Channel {}", command, channel);
    }
  }

  private VolumeDetails getVolume(CredentialDetails credentials, HttpHost target) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    String jsonContent = connectionService.doHttpsGet(credentials, target, VOLUME_PATH);
    JsonObject jsonObject = new JsonParser().parse(jsonContent).getAsJsonObject();
    return VolumeDetails.ofCurrentVolumeAndMuted(jsonObject.get("current").getAsString(), jsonObject.get("muted").getAsBoolean());
  }

  private void setVolume(CredentialDetails credentials, Command command,
      HttpHost target) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    JsonObject volumeJson = new JsonObject();
    volumeJson.addProperty("muted", "false");
    volumeJson.addProperty("current", command.toString());
    logger.debug("Set json volume: {}", volumeJson);
    connectionService.doHttpsPost(credentials, target, VOLUME_PATH, volumeJson.toString());
  }

  private void setMute(CredentialDetails credentials, Command command,
      HttpHost target) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    // We just sent the KEY_MUTE and dont bother what was actually requested
    JsonObject muteJson = new JsonObject();
    muteJson.addProperty("key", KEY_MUTE.toString());
    logger.debug("Set json mute state: {}", muteJson);
    connectionService.doHttpsPost(credentials, target, KEY_CODE_PATH, muteJson.toString());
  }
}
