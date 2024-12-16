package org.spectrumworkshop.craftcode.Events;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spectrumworkshop.craftcode.Actions.Actions;
import org.spectrumworkshop.craftcode.Events.Event.EventCondition;
import org.spectrumworkshop.craftcode.Events.Event.ScriptEvent;
import org.spectrumworkshop.craftcode.Interpreters.Interpreter;
import org.spectrumworkshop.craftcode.Values.Basic.CCString;
import org.spectrumworkshop.craftcode.Values.CraftCode.CCEvent;
import org.spectrumworkshop.craftcode.Values.CraftCode.CCSystem;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;

public class PlayerJoin implements Listener, CCEvent, Values {

    private final ScriptEvent eventInfo;
    private PlayerJoinEvent joinEvent;

    public PlayerJoin(ScriptEvent eventInfo) {this.eventInfo = eventInfo;}
    
    @Override
    public String toString() { return "PlayerJoin"; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws Exception {
        System.out.println("Player joins");
        joinEvent = event;
        EventCondition condition = eventInfo.GetCondition();
        if (condition == null || condition.Evaluate(this)) {
            ArrayList<Actions> actions = eventInfo.GetActions();
            Variable varEvent = new Variable("event");
            varEvent.SetValue(this);
            Interpreter.Interpret(actions, new ArrayList<>(Arrays.asList(varEvent)));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public Values Interpret(ArrayList<String> dots, ArrayList<Values> params, 
            ArrayList<Variable> variables) throws Exception {
                String dot = dots.get(0);
                switch (dot) {
                    case "player" -> {
                        return ReturnValue("player");
                    }
                    case "message" -> {
                        if(params.get(0) != null){
                            joinEvent.setJoinMessage("" + CCSystem.EvaluateInput(params.get(0), variables));
                        } else{
                            return ReturnValue("message");
                        }
                        
                    }
                }
                return null;
    }

    @Override
    public Values ReturnValue(String value) throws Exception {
        switch (value) {
            case "player" -> {
                return new CCString(joinEvent.getPlayer().getName());
            }
            case "message" -> {
                return new CCString(joinEvent.joinMessage().toString());
            }
            default -> {
            }
        }
        throw new Exception("Unknown token " + value + "!!");
    }

}
