/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.jablotron.handler.JablotronBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.openhab.binding.jablotron.JablotronBindingConstants.THING_TYPE_JA100;
import static org.openhab.binding.jablotron.JablotronBindingConstants.THING_TYPE_OASIS;

/**
 * The {@link JablotronDiscoveryService} is responsible for the thing discovery
 * process.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(JablotronDiscoveryService.class);
    private JablotronBridgeHandler bridge = null;
    private DiscoveryServiceCallback discoveryServiceCallback;

    private static final int DISCOVERY_TIMEOUT_SEC = 10;

    public JablotronDiscoveryService(JablotronBridgeHandler bridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
        this.bridge = bridgeHandler;
        bridgeHandler.setDiscoveryService(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return new HashSet<>(Arrays.asList(THING_TYPE_OASIS, THING_TYPE_JA100));
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scanning for items...");
        //bridge.setDiscoveryService(this);
        bridge.startDiscovery();
    }

    public void oasisDiscovered(String label, String serviceId, String url) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("serviceId", serviceId);
        properties.put("url", url);

        ThingUID thingUID = new ThingUID(THING_TYPE_OASIS, bridge.getThing().getUID(), serviceId);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.info("Detected an OASIS alarm with service id: {}", serviceId);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_OASIS).withProperties(properties)
                            .withRepresentationProperty("serviceId").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

    public void ja100Discovered(String label, String serviceId, String url) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("serviceId", serviceId);
        properties.put("url", url);

        ThingUID thingUID = new ThingUID(THING_TYPE_JA100, bridge.getThing().getUID(), serviceId);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.info("Detected a JA100 alarm with service id: {}", serviceId);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_JA100).withProperties(properties)
                            .withRepresentationProperty("serviceId").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }
}
