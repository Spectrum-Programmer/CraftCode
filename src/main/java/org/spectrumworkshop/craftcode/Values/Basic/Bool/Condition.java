package org.spectrumworkshop.craftcode.Values.Basic.Bool;

// Java Packages
import java.util.ArrayList;
import java.util.Arrays;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;


/**
 * <strong> Description: </strong> Object that holds boolean expression data - 
 * conditions/booleans and operators - to use during interpretation.
 */
public class Condition extends Boolean {
    
    private final ArrayList<Values> conditions = new ArrayList<>();
    private final ArrayList<String> operators = new ArrayList<>();
    private final static String PATTERN = "^~¡~µ~(~∆~)º(?:~µ~(~•~)~µ~¡~µ~(~∆~))+";
    private final static ArrayList<String> regexSubs = new ArrayList<>(Arrays.asList(
        "∆", "(?:~∂~)|~¶~|~®~|~©~",
            "•", "\\band\\b|\\bor\\b|\\bxor\\b|\\bnand\\b|\\bnor\\b",
            "¡", "(\\bnot\\b)?"
    ));

    /* --------------
       Getter Methods
       -------------- */

    public ArrayList<Values> GetConditions() { return conditions; }
    public ArrayList<String> GetOperators() { return operators; }

    /* -----------
       List Adders
       ----------- */

    public void AddBool(Values item) { conditions.add(item); }
    public void AddOp(String item) { operators.add(item); }
 
    /* --------------
       Parser Methods
       -------------- */

    /**
     * <strong> Description: </strong> Read a string, determine if it is a Number, Variable, or Arithmetic
     * Expression, and then return the corresponding "Value"
     * @param string The string to inspect
     * @return A Values Object, instance of the determined type
     */
    private static Values ReturnBoolValue(String string, ArrayList<String> containers, String inverted) throws Exception {
        // Compares operands to numbers, variables, & arithmetic operations
        Values bool = null;
        if (Condition.isCondition(string)) {
            bool = Condition.CreateCondition(string);
        }
        else if (Inequality.isInequality(string)) {
            bool = Inequality.CreateInequality(string);
        }
        else if (SWRegex.Match(string, "∂")) {
            return DotAction.CreateDotAction(string, containers);
        }
        else if (SWRegex.Match(string, SWRegex.CleanRegex("®"))) {
            bool = switch (string) {
                case "true" -> new Boolean(true);
                case "false" -> new Boolean(false);
                default -> new Variable(string);
            };
        }
        else if (SWRegex.Match(string, "\\(.*?\\)")) {
            String val = string.substring(1, string.length() - 1).replaceFirst("Container", "");
            bool = ReturnBoolValue(containers.get(Integer.parseInt(val)), null, null);
        }
        else {
            throw new Exception("Condition terms can only be inequalities, variables, or booleans.");
        }
        if (bool != null && inverted != null) {
            switch (bool) {
                case Variable var -> { var.SetInverted(true); 
                    return var; }
                case Boolean newBool -> { newBool.SetInverted(true);
                    return newBool; }
                default -> { return null; }
            }
        }
        return bool;
    }

    /**
     * <strong>Description: </strong> Determine if a given string is a correctly-formatted condition
     * @param block The string to compare
     */
    public static boolean isCondition(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        return SWRegex.Match(block, SWRegex.CleanRegex(PATTERN, regexSubs));
    }

    /**
     * <strong> Description: </strong> Create a condition from the given string
     * @param block The string to convert into a condition
     */
    public static Condition CreateCondition(String block) throws Exception {

        block = block.strip();
        Condition condition = new Condition(); // Created condition to return

        SWRegex matcher = new SWRegex(block, PATTERN);

        if (matcher.Find(regexSubs, true)) {
            ArrayList<String> list = matcher.AsList();
            for (int i = 1; i < list.size(); i += 3) {
                Values num = ReturnBoolValue(list.get(i), matcher.GetContainers(), list.get(i-1));
                condition.AddBool(num);
                if (i + 1 < list.size()) {
                    condition.AddOp(list.get(i+1));
                }
            }
        }
        return condition;
    }

    /* -----------------
       Condition Methods
       ----------------- */

    /**
     * <strong> Description: </strong> Evaluate a simple boolean expression.
     */
    public boolean DoCondition(boolean op1, String operator, boolean op2) throws Exception {
        switch (operator) {
            case "and" -> { return op1 && op2; }
            case "or" -> { return op1 || op2; }
            case "xor" -> { return (op1 || op2) && !(op1 && op2); }
            case "nor" -> { return !(op1 || op2); }
            case "nand" -> { return !(op1 && op2); }
            default -> { throw new Exception("Unknown Operator " + operator); }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean Evaluate(ArrayList<Variable> variables) throws Exception {
        ArrayList<Values> tempConditions = (ArrayList<Values>) conditions.clone();
        ArrayList<String> tempOperators = (ArrayList<String>)operators.clone();
        for (int i = 0; i < tempConditions.size(); i++) {
            switch (tempConditions.get(i)) {
                case Condition cond -> tempConditions.set(i, new Boolean(cond.Evaluate(variables)));
                case Inequality ineq -> tempConditions.set(i, new Boolean(ineq.Evaluate(variables)));
                case Variable vari -> {
                    boolean varFound = false;
                    for (Variable var : variables) {
                        if (var.GetName().equals(vari.GetName())) {
                            if (var.GetValue() instanceof Boolean boolVal) {
                                tempConditions.set(i, boolVal);
                                varFound = true;
                                break;
                            }
                            else {
                                throw new Exception("Condition variable is not a boolean!!");
                            }
                        }
                    }
                    if (!varFound) {
                        throw new Exception("Variable " + vari.GetName() + " does not exist");
                    }
                }
                case DotAction dot -> {
                    Values value = dot.InterpretDot(variables);
                    if (!(value instanceof Boolean)) {
                        throw new Exception("Dot action does not return a value of boolean!!");
                    }
                    tempConditions.set(i, (Boolean)(value));
                }
                default -> {
                }
            }
        }
        for (int i = 0; i < tempOperators.size(); i++) {
            Boolean newBool = new Boolean(DoCondition(((Boolean)(tempConditions.get(i))).GetValue(), operators.get(i), ((Boolean)(tempConditions.get(i+1))).GetValue()));
            tempConditions.set(i, newBool);
            tempConditions.remove(i + 1);
            tempOperators.remove(0);
        }
        return ((Boolean)(tempConditions.get(0))).GetValue();
    }
}
