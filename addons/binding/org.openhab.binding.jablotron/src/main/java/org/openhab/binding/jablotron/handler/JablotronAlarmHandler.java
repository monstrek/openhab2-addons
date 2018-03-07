package org.openhab.binding.jablotron.handler;

import com.google.gson.Gson;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.jablotron.config.DeviceConfig;
import org.openhab.binding.jablotron.internal.Utils;

import java.util.concurrent.ScheduledFuture;

public abstract class JablotronAlarmHandler extends BaseThingHandler {

    protected Gson gson = new Gson();

    protected DeviceConfig thingConfig;
    protected String session = "";
    protected int stavA = 0;
    protected int stavB = 0;
    protected int stavABC = 0;
    protected boolean controlDisabled = true;
    protected boolean inService = true;
    protected int lastHours = Utils.getHoursOfDay();

    ScheduledFuture<?> future = null;
    public JablotronAlarmHandler(Thing thing) {
        super(thing);
    }
}
