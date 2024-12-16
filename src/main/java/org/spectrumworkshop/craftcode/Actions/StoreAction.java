package org.spectrumworkshop.craftcode.Actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.spectrumworkshop.craftcode.Values.Basic.Bool.Boolean;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Condition;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Inequality;
import org.spectrumworkshop.craftcode.Values.Basic.CCString;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Arithmetic;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.VarAddition;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

public class StoreAction extends Actions {

    final String type = "StoreCommand";

    private String variable;
    private Values value;
    private final static String PATTERN = "^store~µ~(~∆~|~∑~)~µ~in~µ~(~®~|~∑~)";
    private final static ArrayList<String> regexSubs = new ArrayList<>(Arrays.asList(
        "∆", "(?:~∂~)|~¶~|~®~|~©~",
             "∑", "\".*?\""
    ));

    public void SetVariable(String string) { variable = string; }

    public void SetValue(Values var) { value = var; }

    @Override
    public String GetType() { return type; }

    private static Values ReturnValue(String string, ArrayList<String> containers) throws Exception {
        // Compares operands to numbers, variables, & arithmetic operations
        if (Condition.isCondition(string)) {
            return Condition.CreateCondition(string);
        }
        else if (Inequality.isInequality(string)) {
            return Inequality.CreateInequality(string);
        }
        else if (VarAddition.isVarAddition(string)) {
            return VarAddition.CreateVarAddition(string, null);
        }
        else if (Arithmetic.isArithmetic(string)) {
            return Arithmetic.CreateArithmetic(string);
        }
        else if (DotAction.isDotAction(string)) {
            return DotAction.CreateDotAction(string, containers);
        }
        else if (CCString.isString(string)) {
            return CCString.CreateString(string);
        }
        else if (SWRegex.Match(string, "©")) {
            return new Number(Float.parseFloat(string));
        }
        else if (SWRegex.Match(string, SWRegex.CleanRegex("®"))) {
            switch (string) {
                case "true" -> {return new Boolean(true);}
                case "false" -> {return new Boolean(false);}
                default -> {return new Variable(string);}
            }
        }
        else if (SWRegex.Match(string, "\\(.*?\\)")) {
            String val = string.substring(1, string.length() - 1).replaceFirst("Container", "");
            String params = containers.get(Integer.parseInt(val)).strip();
            return ReturnValue(params, null);
        }
        else {
            throw new Exception("Condition terms can only be inequalities, variables, or booleans.");
        }
    }

    public static boolean isStoreVariable(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        String storePattern = SWRegex.CleanRegex(PATTERN, regexSubs);
        Matcher match = SWRegex.CreateMatcher(block, storePattern);
        return match.find();
    }

    public static StoreAction CreateStoreVariable(String block, ArrayList<String> containers) throws Exception {
        
        block = block.strip();

        StoreAction variable = new StoreAction();

        SWRegex matcher = new SWRegex(block, PATTERN);

        if (matcher.Find(regexSubs)) {
            ArrayList<String> groups = matcher.AsList();

            variable.SetVariable(groups.get(1).strip());
            String value = groups.get(0).strip();
            variable.SetValue(ReturnValue(value, containers));
        }
        return variable;
    }
    
    @Override
    public ArrayList<Variable> Interpret(ArrayList<Variable> variables) throws Exception {
        Variable var = new Variable(variable);
        Values base = null;
        switch (value) {
            case Condition cond -> {
                base = new Boolean(cond.Evaluate(variables));
            }
            case Inequality ineq -> {
                base = new Boolean(ineq.Evaluate(variables));
            }
            case Arithmetic arith -> {
                base = new Number(arith.Evaluate(variables));
            }
            case Variable vari -> {
                boolean varFound = false;
                for (Variable varia : variables) {
                    if (vari.GetName().equals(varia.GetName())) {
                        base = varia.GetValue();
                        varFound = true;
                    }
                }
                if (!varFound) {
                    throw new Exception("Variable " + vari.GetName() + " not found!!");
                }
            }
            case DotAction dot -> {
                base = dot.InterpretDot(variables);
            }
            default ->{}
        }
        var.SetValue(base);
        Variable.SetVariable(var, variables);
        return variables;
    }
}
