package org.spectrumworkshop.craftcode.Values.Basic.Bool;

// Java Packages
import java.util.ArrayList;
import java.util.Arrays;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Values.Basic.CCString;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Arithmetic;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

/**
 * <strong> Description: </strong> Object that holds inequality expression data - 
 * operands and inequality operators - to use during interpretation.
 */
public class Inequality extends Boolean {
    private final ArrayList<Values> values = new ArrayList<>();
    private final ArrayList<String> inequalities = new ArrayList<>();
    private final static String PATTERN = "^(~∆~)º(?:~µ~(~•~)~µ~(~∆~))+";
    private final static ArrayList<String> regexSubs = new ArrayList<>(Arrays.asList(
        "∆", "(?:~∂~)|~¶~|~®~|~©~",
            "•", "<|>|≤|≥|<=|>=|==|\\bis\\b|!=|\\b!is\\b"
    ));

    public static String GetPattern(){return SWRegex.CleanRegex(PATTERN, regexSubs);}

    /* -----------
       List Adders
       ----------- */

    public void AddValue(Values value) { values.add(value); }
    public void AddSymbol(String value) { inequalities.add(value); }

    /**
     * <strong> Description: </strong> Read a string, determine if it is a Number, Variable, or Arithmetic
     * Expression, and then return the corresponding "Value"
     * @param string The string to inspect
     * @return A Values Object, instance of the determined type
     */
    private static Values ReturnNumValue(String string, ArrayList<String> containers) throws Exception {
        // Compares operands to numbers, variables, & arithmetic operations
        if(SWRegex.Match(string, SWRegex.CleanRegex("©"))){  
            return new Number(Float.parseFloat(string)); } // Numbers
        else if (SWRegex.Match(string, SWRegex.CleanRegex("®"))) {
            return switch (string) {
                case "true" -> new Number(1);
                case "false" -> new Number(0);
                default -> new Variable(string);
            }; // Variables
        } else if (Arithmetic.isArithmetic(string)) {
            return Arithmetic.CreateArithmetic(string);
        } else if (Inequality.isInequality(string)) {
            return Inequality.CreateInequality(string);
        } else if (Condition.isCondition(string)) {
            return Condition.CreateCondition(string);
        }else if (CCString.isString(string)) {
            return CCString.CreateString(string);
        }else if (SWRegex.Match(string, "∂")) {
            return DotAction.CreateDotAction(string, containers);
        }
        else if (SWRegex.Match(string, "\\(.*?\\)")) {
            String val = string.substring(1, string.length() - 1).replaceFirst("Container", "");
            return ReturnNumValue(containers.get(Integer.parseInt(val)), null);
        }
        else {
            throw new Exception(
                    "Arithmetic expressions can only contain numbers, variables, or other arithmetic expressions");
        }
    }
    
    /**
     * <strong>Description: </strong> Determine if a given string is a correctly-formatted inequality
     * @param block The string to compare
     * @return True if the the string is an inequality
     */
    public static boolean isInequality(String block) {
        block = block.strip(); 
        return SWRegex.Match(block, SWRegex.CleanRegex(PATTERN, regexSubs));
    }

     /**
     * <strong> Description: </strong> Create an inequality expression from
     * a given string.
     * @param block The string to convert into an inequality expression
     * @return An Inequality Object containg the expression's data
     */
     public static Inequality CreateInequality(String block) throws Exception {
        block = block.strip(); 
        Inequality inequality = new Inequality();

        SWRegex matcher = new SWRegex(block, PATTERN);

        if (matcher.Find(regexSubs, true)) {
            ArrayList<String> list = matcher.AsList();
            for (int i = 0; i < list.size(); i += 2) {
                Values num = ReturnNumValue(list.get(i), matcher.GetContainers());
                inequality.AddValue(num);
                if (i + 1 < list.size()) {
                    inequality.AddSymbol(list.get(i+1));
                }
            }
        }
        return inequality;    
    }

    /* ------------------
       Inequality Methods
       ------------------ */

    /**
     * <strong> Description: </strong> Evaluates a simple inequality
     */
    public boolean DoInequality(Values op1, String operator, Values op2) throws Exception {
        if (op1 instanceof Number num1 && op2 instanceof Number num2) {
            float oper1 = num1.GetValue(); float oper2 = num2.GetValue();
            switch (operator) {
                case "<" -> { return oper1 < oper2; }
                case ">" -> { return oper1 > oper2; }
                case "≤", "<=" -> { return oper1 <= oper2; }
                case "≥", ">=" -> { return oper1 >= oper2; }
                case "==", "is" -> { return oper1 == oper2; }
            }
            throw new Exception("Operator " + operator + " not recognized!");
        }
        else if (op1 instanceof CCString string1 && op2 instanceof CCString string2) {
            String opString1 = string1.GetValue(); String opString2 = string2.GetValue();
            switch (operator) {
                case "<" -> {
                    return (opString1.compareTo(opString2)) < 0;
                }
                case ">" -> {
                    return (opString1.compareTo(opString2)) > 0;
                }
                case "≤", "<=" -> {
                    return opString1.compareTo(opString2) < 0 ||
                            opString1.equals(opString2);
                }
                case "≥", ">=" -> {
                    return opString1.compareTo(opString2) > 0 ||
                            opString1.equals(opString2);
                }
                case "==", "is" -> {
                    return opString1.equals(opString2);
                }
            }
            throw new Exception("Operator " + operator + " not recognized!");
        }
        else {
            switch (operator) {
                case "<" -> {
                    return false;
                }
                case ">" -> {
                    return false;
                }
                case "≤", "<=" -> {
                    return false;
                }
                case "≥", ">=" -> {
                    return false;
                }
                case "==", "is" -> {
                    return op1.equals(op2);
                }
            }
        }
        throw new Exception("Operator " + operator + " not recognized!");
    }

    @SuppressWarnings("unchecked")
    public boolean Evaluate(ArrayList<Variable> variables) throws Exception {
        ArrayList<Values> tempValues = (ArrayList<Values>) values.clone();
        ArrayList<String> tempOperators = (ArrayList<String>) inequalities.clone();
        for (int i = 0; i < tempValues.size(); i++) {
            switch (tempValues.get(i)) {
                case Arithmetic arith -> tempValues.set(i, new Number(arith.Evaluate(variables)));
                case Variable vari -> {
                    boolean varFound = false;
                    for (Variable var : variables) {
                        if (var.GetName().equals(vari.GetName())) {
                            if (var.GetValue() instanceof Number num) {
                                tempValues.set(i, num);
                                varFound = true;
                                break;
                            }
                            else {
                                throw new Exception("Inequality variable is not a number");
                            }
                        }
                    }
                    if (!varFound) {
                        throw new Exception("Variable " + vari.GetName() + " does not exist");
                    }
                }
                case DotAction dot -> {
                    Values value = dot.InterpretDot(variables);
                    if (!(value instanceof Number)) {
                        throw new Exception("Dot Action in inequality does not return a number!!");
                    }
                    tempValues.set(i, (Number)(value));
                }
                case Condition cond -> {
                    boolean thisBool = cond.Evaluate(variables);
                    if(thisBool == true){tempValues.set(i, new Number(1));}
                    else{tempValues.set(i, new Number(0));}
                }
                case Inequality ineq -> {
                    boolean thisBool = ineq.Evaluate(variables);
                    if(thisBool == true){tempValues.set(i, new Number(1));}
                    else{tempValues.set(i, new Number(0));}
                }
                default -> {
                }
            }
        }
        for (int i = 0; i < tempValues.size() - 1; i++) {
            if (!DoInequality((Number)(tempValues.get(i)), tempOperators.get(i), (Number)(tempValues.get(i + 1)))) {
                return false;
            }
        }
        return true;
    }
}
 