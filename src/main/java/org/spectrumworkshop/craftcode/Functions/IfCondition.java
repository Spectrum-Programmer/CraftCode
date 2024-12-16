package org.spectrumworkshop.craftcode.Functions;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.spectrumworkshop.craftcode.Actions.Actions;
import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Interpreters.Interpreter;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Boolean;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Condition;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Inequality;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

/**
* <strong>Description: </strong>Class for holding If-Else Statement properties
*/
public class IfCondition extends Actions {

    final String type = "ExecuteCommand";

    private final ArrayList<Values> conditions = new ArrayList<>();
    private final ArrayList<ArrayList<Actions>> actions = new ArrayList<>();
    private final static String PATTERN = "^if~µ~(~¶~)~µ~(~§~)º(?:else~µ~if~µ~(~¶~)~µ~(~§~))*º(?:else~µ~(~§~))?";
    private final static ArrayList<String> regexSubs = null;
    private int containersAmount = 0;

    // Add a new condition to the conditions list
    public void AddCondition(Values item) { conditions.add(item); }
    
    // Add a new action to the actions list 
    public void AddFunction(ArrayList<Actions> item) { actions.add(item); }

    @Override
    public String GetType() { return type; }
    
    public int ContainersAmount() { return containersAmount; }
    
    public void Increment() {
        containersAmount++;
    }


     /**
     * <strong>Description: </strong> Determine if a given string is a correctly-formatted if-else statement
     * @param block The string to compare
     */
    public static boolean isConditional(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        String conditionalPattern = SWRegex.CleanRegex(PATTERN, null);
        Matcher match = SWRegex.CreateMatcher(block, conditionalPattern);
        return match.find();
    }

    /**
     * <strong>Description: </strong> Create an if-ifelse-else statement from the given string
     */
    public static IfCondition CreateConditional(String block, ArrayList<String> containers) throws Exception {

        block = block.strip(); 
        IfCondition conditional = new IfCondition();
        
        SWRegex matcher = new SWRegex(block, PATTERN);

        if (matcher.Find(regexSubs)) {
            ArrayList<String> matcherList = matcher.AsList();
            int barrier = matcherList.size();
            if(barrier % 2 != 0){barrier -= 1;}
            for (int i = 0; i < barrier; i += 2) {
                String cond = matcherList.get(i);
                String val = cond.substring(1, cond.length() - 1).replaceFirst("Container", "");
                String uncomp = containers.get(Integer.parseInt(val)).strip();
                if(uncomp == null){
                    throw new Exception("If-Statement contains no condition!!");
                }
                if (Condition.isCondition(uncomp)){
                    conditional.AddCondition(Condition.CreateCondition(uncomp));
                } 
                else if (Inequality.isInequality(uncomp)) {
                    conditional.AddCondition(Inequality.CreateInequality(uncomp));
                }
                else if (DotAction.isDotAction(uncomp)){
                    ArrayList<String> comp = SWRegex.Compress(uncomp);
                    String input = comp.get(0);
                    comp.remove(0);
                    conditional.AddCondition(DotAction.CreateDotAction(input, comp));
                }
                else if (Boolean.isBoolean(uncomp)) {
                    switch (uncomp) {
                        case "false" -> conditional.AddCondition(new Boolean(false));
                        case "true" -> conditional.AddCondition(new Boolean(true));
                        default -> {
                            conditional.AddCondition(new Variable(uncomp));
                        }
                    }
                }
                else {
                    throw new Exception("If-Statement Condition is invalid!!");
                }
                conditional.Increment();
                if (containers.get(Integer.parseInt(val) + 1) == null || containers.get(Integer.parseInt(val) + 1).matches("\\s*"))
                    conditional.AddFunction(null);
                else {
                    conditional.AddFunction(Actions.CreateActionList(containers.get(Integer.parseInt(val) + 1).strip()));
                }
                conditional.Increment();
            }
            if (barrier < matcherList.size()) {
                String cond = matcherList.get(matcherList.size()-1);
                String val = cond.substring(1, cond.length() - 1).replaceFirst("Container", "");
                String uncomp = containers.get(Integer.parseInt(val)).strip();
                if (uncomp == null || uncomp.matches("\\s*"))
                    conditional.AddFunction(null);
                else {
                    conditional.AddFunction(Actions.CreateActionList(uncomp.strip()));
                }
                conditional.Increment();
            }
        }
        return conditional;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Variable> Interpret(ArrayList<Variable> variables) throws Exception {
        ArrayList<Variable> local = (ArrayList<Variable>) variables.clone();
        for (int i = 0; i < conditions.size(); i++) {
            Values condition = conditions.get(i);
            if (GetBooleanValue(condition, variables)) {
                ArrayList<Actions> actionList = actions.get(i);
                local = Interpreter.Interpret(actionList, local);
                return Variable.UpdateList(variables, local);
            } }
        if (actions.size() > conditions.size()) {
            for (Actions action : actions.get(actions.size() - 1)) {
                local = action.Interpret(local);}
        }
        return Variable.UpdateList(variables, local);
    }

    public static boolean GetBooleanValue(Values value, ArrayList<Variable> variables) 
    throws Exception {
        switch (value) {
            case Condition cond -> { return cond.Evaluate(variables); }
            case Inequality ineq -> { return ineq.Evaluate(variables); }
            case Boolean bool -> { return bool.GetValue(); }
            case Variable var -> {
                for (Variable vari : variables) {
                    if (var.GetName().equals(vari.GetName())) {
                        return GetBooleanValue(vari.GetValue(), variables);
                    }
                }
                throw new Exception("Variable " + var.GetName() + " does not exist!!");
            }
            case DotAction dot -> {
                Values val = dot.InterpretDot(variables);
                if (val instanceof Boolean bool) {
                    return bool.GetValue();
                }
                else {throw new Exception ("Dot action in repeater does not return boolean!!");}
            }
            default -> {
                throw new Exception("Invalid If-Condition parameter");}
        }
    }
}
