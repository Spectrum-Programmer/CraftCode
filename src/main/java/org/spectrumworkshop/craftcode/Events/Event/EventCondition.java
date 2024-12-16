package org.spectrumworkshop.craftcode.Events.Event;

import java.util.ArrayList;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Boolean;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Condition;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Inequality;
import org.spectrumworkshop.craftcode.Values.Basic.CCString;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.CraftCode.CCEvent;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;
 

public class EventCondition {
    private ArrayList<Condition> conditions;
    private final static String PATTERN = "^(~®~)~µ~\\[~µ~(~®~)?~µ~(==|!=|is|!is|<|>|<=|>=|≤|≥)~µ~((?:~∂~)|~®~|~©~|(?:\".*?\"))]º(?:~µ~,~µ~(~®~)~µ~\\[~µ~(~®~)?~µ~(==|!=|is|!is|<|>|<=|>=|≤|≥)~µ~((?:~∂~)|~®~|~©~|(?:\".*?\"))~µ~])*";

    public EventCondition(String event) { }

    public void SetParams(ArrayList<String> params) { }

    public void SetConditions(ArrayList<Condition> conditions) { this.conditions = conditions;}
 
    private static Values DetermineValue(String string) throws Exception {
        if (DotAction.isDotAction(string)) {
            ArrayList<String> comp = SWRegex.Compress(string);
            String input = comp.get(0);
            comp.remove(0);
            return DotAction.CreateDotAction(input, comp);
        }
        else if (SWRegex.Match(string, SWRegex.CleanRegex("®"))) {
            if (string.equals("true")) {
                return new Boolean(true);
            } else if (string.equals("false")) {
                return new Boolean(false);
            }
        }
        else if (SWRegex.Match(string, "©")) {
            return new Number(Float.parseFloat(string));
        }
        else if (CCString.isString(string)) {
            return CCString.CreateString(string);
        }
        throw new Exception("Event conditions can only include comparisons to numbers, booleans or strings");
    }

    public static boolean isEventCondition(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        return SWRegex.Match(block, SWRegex.CleanRegex(PATTERN, null));
    }

    public static EventCondition CreateEventCondition(String block, String event) throws Exception {
        block = block.strip();
        EventCondition condition = new EventCondition(event);

        ArrayList<String> values = new ArrayList<>();
        ArrayList<Condition> subConditions = new ArrayList<>();
    
        SWRegex matcher = new SWRegex(block, PATTERN);
        if (matcher.Find(null)) {
            ArrayList<String> matcherList = matcher.AsList();
            for (int i = 0; i < matcherList.size(); i += 4) {
                values.add(matcherList.get(i).strip());
                Condition subCondition = new Condition();
                subCondition.AddBool(new Variable(matcherList.get(i + 1).strip()));
                subCondition.AddOp(matcherList.get(i + 2).strip());
                // Number
                // True/False
                // String 
                subCondition.AddBool(DetermineValue(matcherList.get(i+3)));
                subConditions.add(subCondition);
            }
        }
        condition.SetParams(values);
        condition.SetConditions(subConditions);

        return condition;
        /*String eventCondPattern = "^(?:!µ!(!®!)!µ!\\[!µ!(!®!)?!µ!(\\?|~)(=|is|<|>|<=|>=|≤|≥)!µ!(!®!)\\]!µ!(?:,!µ!|$))";
        Matcher matcher = SWRegex.createMatcher(block, eventCondPattern);
        while (matcher.find()) {
            String parameter = matcher.group(1);
            String subValue = matcher.group(2);
            String operator = matcher.group(3) + matcher.group(4);
            String comparison = matcher.group(5);
            values.add(parameter);
            Boolean newBool1 = Boolean.AddBoolean(null);
            newBool1.setVar(subValue);
            Boolean newBool2 = Boolean.AddBoolean(null);
            newBool2.setVar(comparison);
            Condition subCondition = Condition.AddCondition();
            subCondition.addBool(newBool1);
            subCondition.addOp(operator);
            subCondition.addBool(newBool2);
            subConditions.add(subCondition);
            block = block.replaceFirst(SWRegex.createRegex("^(?:!µ!(!®!)!µ!\\[!µ!(!®!)?!µ!(\\?|~)(=|is|<|>|<=|>=|≤|≥)!µ!(!®!)\\]!µ!(?:,!µ!|$))"), "");
            matcher = SWRegex.createMatcher(block, eventCondPattern);
        }
        condition.SetConditions(subConditions);
        condition.SetParams(values);
        return condition;*/
    }

    public boolean Evaluate(CCEvent event) throws Exception {
        for(Condition condition : conditions){
            ArrayList<Values> bools = condition.GetConditions();
            Values bool1 = bools.get(0);
            Values bool2 = bools.get(1);
            String oper = condition.GetOperators().get(0);
            if (bool1 == null) {
                bool1 = new CCString(event.toString());
            } else {
                String name = ((Variable) bool1).GetName();
                bool1 = event.ReturnValue(name);
            }
            if(!(bool1 instanceof CCString && bool2 instanceof CCString) ||
            ((bool1 instanceof Number || bool1 instanceof Boolean) &&
                            (bool2 instanceof Number || bool2 instanceof Boolean))) {
                throw new Exception("Value parameters in event " + event.toString() +
                        " could not be compared");
            }
            if (bool1 instanceof Boolean bool) {
                if (bool.GetValue())
                    bool1 = new Number(1);
                else {
                    bool1 = new Number(0);
                }
            }
            if (bool2 instanceof Boolean bool) {
                if (bool.GetValue())
                    bool2 = new Number(1);
                else {
                    bool2 = new Number(0);
                }
            }
            Inequality tempIneq = new Inequality();
            if(!tempIneq.DoInequality(bool1, oper, bool2)) return false;
        }
        return true;
    }
}
