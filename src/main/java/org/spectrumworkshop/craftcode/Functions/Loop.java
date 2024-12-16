package org.spectrumworkshop.craftcode.Functions;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.spectrumworkshop.craftcode.Actions.Actions;
import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Interpreters.Interpreter;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Boolean;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Condition;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Inequality;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

public class Loop extends Actions { 

    final String type = "ExecuteCommand";
        
    private String variable;
    private Values condition;
    private Range range;
    private ArrayList<Actions> actions;
    private final static String PATTERN = "^repeater~µ~(~¶~)~µ~(~§~)";
    private final static ArrayList<String> regexSubs = null;

    public void SetRange(Range range) { this.range = range; }
    
    public void SetCondition(Values condition) { this.condition = condition; }
    
    public void SetVariable(String string) { this.variable = string; }
    
    public void SetActions(ArrayList<Actions> action) { actions = action; }

    @Override
    public String GetType() { return type; }

    public static boolean isLoop(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        String loopPattern = SWRegex.CleanRegex(PATTERN);
        Matcher match = SWRegex.CreateMatcher(block, loopPattern);
        return match.find();
    }

    public static Loop CreateLoop(String block, ArrayList<String> containers) throws Exception {

        block = block.strip(); 
        Loop loop = new Loop();
        
        SWRegex matcher = new SWRegex(block, PATTERN);

        if (matcher.Find(regexSubs)) {
            ArrayList<String> matcherList = matcher.AsList();
            String compParams = matcherList.get(0);
            String compActions = matcherList.get(1);
            String val = compParams.substring(1, compParams.length() - 1).replaceFirst("Container", "");
            String params = containers.get(Integer.parseInt(val)).strip();
            val = compActions.substring(1, compActions.length() - 1).replaceFirst("Container", "");
            String actions = containers.get(Integer.parseInt(val)).strip();
            SWRegex paramsMatcher = new SWRegex(params, "^(~®~)~µ~(.*)");
            String varString;
            String boolString;
            if (Range.isRange(params) || Inequality.isInequality(params) ||
            Condition.isCondition(params) || DotAction.isDotAction(params) || params.equals("true") ||
            params.equals("false")) {
                varString = null;
                boolString = params;
            }
            else if (paramsMatcher.Find(null, false)) {
                ArrayList<String> list = paramsMatcher.AsList();
                varString = list.get(0).strip();
                boolString = list.get(1).strip();
            }
            else {
                throw new Exception("Incorrect loop parameters!!");
            }
            if (varString != null) { loop.SetVariable(varString.strip()); }
            if (Range.isRange(boolString)) {
                loop.SetRange(Range.CreateRange(boolString));
            } else if (Inequality.isInequality(boolString)) {
                loop.SetCondition(Inequality.CreateInequality(boolString));
            } else if (Condition.isCondition(boolString)) {
                loop.SetCondition(Condition.CreateCondition(boolString));
            } else if (DotAction.isDotAction(boolString)){
                    ArrayList<String> comp = SWRegex.Compress(boolString);
                    String input = comp.get(0);
                    comp.remove(0);
                    loop.SetCondition(DotAction.CreateDotAction(input, comp));
                }else if (SWRegex.Match(boolString, "~®~")) {
                switch (boolString) {
                    case "true" -> {
                        loop.SetCondition(new Boolean(true));
                    }
                    case "false" -> {
                        loop.SetCondition(new Boolean(false));
                    }
                    default -> {
                        loop.SetCondition(new Variable(boolString));
                    }
                }
            } else {
                throw new Exception(
                        "Repeater input must be a range, condition, inequality, boolean, or boolean variable!!");
            }
            if (actions == null || SWRegex.Match(actions, "\\s*")) {
                loop.SetActions(null);
            } else {
                loop.SetActions(Actions.CreateActionList(actions));
            }
        }
        
        return loop;

        /*block = block.strip(); // Remove unncessary whitespace

        Loop loop = Loop.AddLoop();
        
        // Loop Data
        Matcher matcher = SWRegex.createMatcher(block, SWRegex.createRegex("^(?:repeater!µ!¥!§!)"));
        matcher.find();
        loop.setActions(Actions.CreateActionList(matcher.group(2)));
        String foundMatch = matcher.group(1).strip();

        matcher = SWRegex.createMatcher(foundMatch, SWRegex.createRegex("^(?:(\\w+)?!µ!(.*))"));
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                loop.setVariable(matcher.group(1));
            }
            foundMatch = matcher.group(2).strip();
            if (isRange(foundMatch)) {
                loop.setRange(CreateRange(foundMatch));
            }
            else if (IfCondition.isCondition(foundMatch)) {
                loop.setCondition(IfCondition.CreateCondition(foundMatch));
            }
            else {
                throw new Exception("Repeaters can only utilize ranges or conditions");
            }
        } else{
            throw new Exception("Repeaters can only utilize ranges or conditions");
        }
        
        return loop;*/
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Variable> Interpret(ArrayList<Variable> variables) throws Exception {
        ArrayList<Variable> local = (ArrayList<Variable>)variables.clone();
        Variable iterationVar = null;
        int i;
        if (variable != null) { iterationVar = new Variable(variable); }
        if (range != null) {
            Number range1 = new Number(0);
            Number range2 = new Number(0);
            if (range.GetTerm(0) instanceof DotAction dot) {
                Values val = dot.InterpretDot(local);
                if (val instanceof Number num) {
                    range1 = num;
                } else {
                    throw new Exception("Dot Action in Range does not return number!!");
                }
            }
            if (range.GetTerm(1) instanceof DotAction dot) {
                Values val = dot.InterpretDot(local);
                if (val instanceof Number num) {
                    range2 = num;
                }
                else{throw new Exception("Dot Action in Range does not return number!!");}
            }
            
            i = (int)(range1.GetValue());
            if (iterationVar != null) {
                iterationVar.SetValue(new Number(i));
                Variable.SetVariable(iterationVar, local);
            }
            for (; i < range2.GetValue() + 1; i++) {
                local = Interpreter.Interpret(actions, local);
                if (iterationVar != null) {
                    iterationVar.SetValue(new Number(i + 1));
                    Variable.SetVariable(iterationVar, local);
                }
            }
            return Variable.UpdateList(variables, local);
        } else if (condition != null) { i = 0;
            if (iterationVar != null) {
                iterationVar.SetValue(new Number(i));
                Variable.SetVariable(iterationVar, local); }
            while (IfCondition.GetBooleanValue(condition, local)) {
                local = Interpreter.Interpret(actions, local);
                i++;
                if (iterationVar != null) {
                    iterationVar.SetValue(new Number(i));
                    Variable.SetVariable(iterationVar, local);
                }
            }
            return Variable.UpdateList(variables, local);
        } else {
            throw new Exception("Repeater has no range or condition");
        }
    }

    }
