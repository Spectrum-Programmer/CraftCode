package org.spectrumworkshop.craftcode.Values.Basic.Num;

// Java Packages
import java.util.ArrayList;
import java.util.Arrays;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

/**
 * <strong> Description: </strong> Object that holds mathematical expression data - 
 * operands and operators - to utilize during interpretation. 
 */
public class Arithmetic extends Number {

    private final ArrayList<Values> operands = new ArrayList<>();
    private final ArrayList<String> operators = new ArrayList<>();
    private final static String PATTERN = "^(~∆~)º(?:~µ~(~•~)~µ~(~∆~))+";
    private final static ArrayList<String> regexSubs = new ArrayList<>(Arrays.asList(
        "∆", "(?:~∂~)|~¶~|~®~|~©~",
            "•", "\\+|\\-|\\*|/|÷|\\^|%"
    ));

    /* ---------------
       Getter Methods
       --------------- */

    public ArrayList<Values> GetOperands(){return operands;}
    public ArrayList<String> GetOperators(){return operators;}
    public static String GetPattern(){return SWRegex.CleanRegex(PATTERN, regexSubs);}
    
    /* -----------
       List Adders
       ----------- */

    public void AddOperand(Values item) { operands.add(item); }
    public void AddOperator(String item) { operators.add(item); }

    /* --------------
       Parser Methods
       -------------- */

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
            return new Variable(string); } // Variables
        else if (isArithmetic(string)) {
            return CreateArithmetic(string);
        } // Paranthesis
        else if (SWRegex.Match(string, "∂")) {
            return DotAction.CreateDotAction(string, containers);
        }
        else if (SWRegex.Match(string, "\\(.*?\\)")) {
            String val = string.substring(1, string.length() - 1).replaceFirst("Container", "");
            return ReturnNumValue(containers.get(Integer.parseInt(val)), null);
        }
        else{ throw new Exception("Arithmetic expressions can only contain numbers, variables, or other arithmetic expressions"); }
    }

    /**
     * <strong> Description: </strong> Determine if a given string is in the 
     * form of an Arithmetic Expression
     */
    public static boolean isArithmetic(String block) {
        block = block.strip(); 
        return SWRegex.Match(block, SWRegex.CleanRegex(PATTERN, regexSubs));
    }

    /**
     * <strong> Description: </strong> Create an arithmetic expression from
     * a given string.
     * @param block The string to convert into an arithmetic expression
     * @return An Arithmetic Object containg the expression's data
     */
    public static Arithmetic CreateArithmetic(String block) throws Exception {
        block = block.strip(); 
        Arithmetic arithmetic = new Arithmetic(); 

        SWRegex matcher = new SWRegex(block, PATTERN);

        if (matcher.Find(regexSubs, true)) {
            ArrayList<String> list = matcher.AsList();
            for (int i = 0; i < list.size(); i += 2) {
                Values num = ReturnNumValue(list.get(i), matcher.GetContainers());
                arithmetic.AddOperand(num);
                if (i + 1 < list.size()) {
                    arithmetic.AddOperator(list.get(i+1));
                }
            }
        }
        return arithmetic;
    }

    /* ------------------
       Arithmetic Methods
       ------------------ */

    /**
     * <strong> Description: </strong> Evaluate a simple math expression.
     */
    private static float DoMath(float operand1, String operator, float operand2) throws Exception {
        switch (operator) {
            case "^" -> { return (float) Math.pow(operand1, operand2); }
            case "%" -> { return operand1 % operand2; }
            case "/", "÷" -> { return operand1 / operand2; }
            case "*" -> { return operand1 * operand2; }
            case "-" -> { return operand1 - operand2; }
            case "+" -> { return operand1 + operand2; }
        }
        throw new Exception("Unknown arithmetic operator!!");
    }

    @SuppressWarnings("unchecked")
    public float Evaluate(ArrayList<Variable> variables) throws Exception {
        ArrayList<Values> tempOperands = (ArrayList<Values>) operands.clone();
        ArrayList<String> tempOperators = (ArrayList<String>) operators.clone();
        for (int i = 0; i < tempOperands.size(); i++) {
            switch (tempOperands.get(i)) {
                case Arithmetic arith -> tempOperands.set(i, new Number(arith.Evaluate(variables)));
                case Variable vari -> {
                    boolean varFound = false;
                    for (Variable var : variables) {
                        if (var.GetName().equals(vari.GetName())) {
                            if (var.GetValue() instanceof Number num) {
                                tempOperands.set(i, num);
                                varFound = true;
                                break;
                            }
                            else {
                                throw new Exception("Arithmetic variable is not a number");
                            }
                        }
                    }
                    if (!varFound) {
                        throw new Exception("Variable " + vari.GetName() + " does not exist");
                    }
                }
                case DotAction dot -> {
                    Values thisValue = dot.InterpretDot(variables);
                    if (!(thisValue instanceof Number)) {
                        throw new Exception("Dot Action in Arithmetic Expression does not return a number!!");
                    }
                    tempOperands.set(i, (Number)(thisValue));
                }
                default -> {
                }
            }
        }
        while (!tempOperators.isEmpty()) {
            int i;
            boolean breakOut = false;
            int savedIndex = -1;
            for (i = 0; i < tempOperators.size(); i++) {
                switch (tempOperators.get(i)) {
                    case "^" -> {
                        savedIndex = -1;
                        breakOut = true;
                    }
                    case "\\", "÷", "*", "%" -> {
                        if (!(savedIndex > -1)) {
                            savedIndex = i;
                        }
                    }
                }
                if (breakOut)
                    break;
            }
            if (savedIndex == -1) {
                savedIndex = i;
                if (!breakOut) {
                    savedIndex--;
                }
            }
            Number operator1 = (Number) tempOperands.get(savedIndex);
            Number operator2 = (Number) tempOperands.get(savedIndex + 1);
            float result = DoMath(operator1.GetValue(), tempOperators.get(savedIndex),
                    operator2.GetValue());
                    tempOperators.remove(savedIndex);
            tempOperands.remove(savedIndex + 1);
            tempOperands.set(savedIndex, new Number(result));
        }
        return ((Number)(tempOperands.get(0))).GetValue();
    }
}


