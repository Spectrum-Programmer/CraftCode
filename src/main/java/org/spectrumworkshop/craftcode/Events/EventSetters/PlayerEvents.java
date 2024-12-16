package org.spectrumworkshop.craftcode.Events.EventSetters;

import org.bukkit.event.Listener;
import org.spectrumworkshop.craftcode.Events.Event.ScriptEvent;
import org.spectrumworkshop.craftcode.Events.PlayerJoin;

public class PlayerEvents {

    public static Listener SetEvent(ScriptEvent event) {
        switch (event.GetName()) {
            case "PlayerJoin" -> {
                return new PlayerJoin(event);
            }
        }
        return null;
    }
}
