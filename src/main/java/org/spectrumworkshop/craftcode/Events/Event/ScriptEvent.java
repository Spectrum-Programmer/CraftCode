package org.spectrumworkshop.craftcode.Events.Event;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.spectrumworkshop.craftcode.Actions.Actions;
import org.spectrumworkshop.tools.SWRegex;

public class ScriptEvent {

    final private String eventName;
    private EventCondition condition;
    private ArrayList<Actions> actions;

    private final static String PATTERN_START = "^start~µ~(~§~)";
    private final static String PATTERN_EVENT = "^when~µ~(~®~)(?:~µ~with~µ~(~¶~))?~µ~do~µ~(~§~)";

    public ScriptEvent(String name) { eventName = name; }

    // Sets the conditions list to given list
    public void SetCondition(EventCondition conditions) { this.condition = conditions; }
    
    public void SetActions(ArrayList<Actions> actions) { this.actions = actions; }

    public String GetName() {
        return eventName;
    }

    public ArrayList<Actions> GetActions() { return actions; }
    
    public EventCondition GetCondition() { return condition; }

    public static ScriptEvent CreateEvent(String block, ArrayList<String> containers) throws Exception {

        block = block.strip(); // Remove unncessary whitespace
        
        Matcher matcher = SWRegex.CreateMatcher(block, PATTERN_EVENT);
        matcher.find();

        String eventName = matcher.group(1);

        int val;
        String conditions = "";
        String actions;
        Matcher match = SWRegex.CreateMatcher(block, "\\(Container(~©~)\\)");
        if (match.find()) {
            val = Integer.parseInt(match.group(1));
            conditions = containers.get(val);
            actions = containers.get(val + 1);
        } else {
            match = SWRegex.CreateMatcher(block, "\\{Container(~©~)\\}");
            match.find();
            val = Integer.parseInt(match.group(1));
            actions = containers.get(val);
        }

        ScriptEvent event = new ScriptEvent(eventName);
        if (EventCondition.isEventCondition(conditions.strip())) {
            event.SetCondition(EventCondition.CreateEventCondition(conditions, eventName));
        }
        else if (matcher.group(2) == null || matcher.group(2).matches("\\s*")) {
        }
        else { throw new Exception("Incorrect Event Condition format!!");}

        event.SetActions(Actions.CreateActionList(actions.strip()));

        return event;
    }

    public static ScriptEvent CreateStart(String block, ArrayList<String> containers) throws Exception {
        
        ScriptEvent event = new ScriptEvent("Start");

        Matcher match = SWRegex.CreateMatcher(block, "\\{Container(~©~)\\}");
        match.find();
        int val = Integer.parseInt(match.group(1));

        event.SetActions(Actions.CreateActionList(containers.get(val).strip()));

        return event;
    }

    public static boolean isEvent(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        Matcher match = SWRegex.CreateMatcher(block, PATTERN_EVENT);
        return match.find();
    }

    public static boolean isStart(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        Matcher match = SWRegex.CreateMatcher(block, PATTERN_START);
        return match.find();
    }

}