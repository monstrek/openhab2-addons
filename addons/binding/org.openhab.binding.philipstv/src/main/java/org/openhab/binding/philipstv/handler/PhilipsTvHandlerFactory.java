/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.handler;

import org.openhab.binding.philipstv.*;

import static org.openhab.binding.philipstv.PhilipsTvBindingConstants.*;

/**
 * The {@link PhilipsTvHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Benjamin Meyer - Initial contribution
 */

public class PhilipsTvHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_PHILIPS_TV);

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    private PhilipsTvDynamicStateDescriptionProvider stateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PHILIPS_TV.equals(thingTypeUID)) {
            return new PhilipsTvHandler(thing, discoveryServiceRegistry, stateDescriptionProvider);
        }

        return null;
    }

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(PhilipsTvDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    protected void unsetDynamicStateDescriptionProvider(PhilipsTvDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = null;
    }

}
