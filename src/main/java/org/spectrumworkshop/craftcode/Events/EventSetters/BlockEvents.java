package org.spectrumworkshop.craftcode.Events.EventSetters;

import org.bukkit.event.Listener;
import org.spectrumworkshop.craftcode.Events.BlockPlace;
import org.spectrumworkshop.craftcode.Events.Event.ScriptEvent;

public class BlockEvents {

    public static Listener SetEvent(ScriptEvent event) {
        switch (event.GetName()) {
            case "BlockPlace" -> {
                return new BlockPlace(event);
            }
        }
        return null;
    }
}