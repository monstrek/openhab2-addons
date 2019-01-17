package org.openhab.binding.philipstv;

import org.eclipse.jdt.annotation.*;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.type.*;
import org.eclipse.smarthome.core.types.*;
import org.osgi.service.component.annotations.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Dynamic provider of state options while leaving other state description fields as original.
 *
 * @author Gregory Moyer - Initial contribution
 * @author Christoph Weitkamp - Adapted to Kodi binding
 * @author Benjamin Meyer - Adapted to Philips TV binding
 */
@Component(service = { DynamicStateDescriptionProvider.class,
    PhilipsTvDynamicStateDescriptionProvider.class }, immediate = true)
@NonNullByDefault
public class PhilipsTvDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {
  private final Map<ChannelUID, List<StateOption>> channelOptionsMap = new ConcurrentHashMap<>();

  public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
    channelOptionsMap.put(channelUID, options);
  }

  @Override
  public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
      @Nullable Locale locale) {
    List<StateOption> options = channelOptionsMap.get(channel.getUID());
    if (options == null) {
      return null;
    }

    if (original != null) {
      return new StateDescription(original.getMinimum(), original.getMaximum(), original.getStep(),
          original.getPattern(), original.isReadOnly(), options);
    }

    return new StateDescription(null, null, null, null, false, options);
  }

  @Deactivate
  public void deactivate() {
    channelOptionsMap.clear();
  }
}

