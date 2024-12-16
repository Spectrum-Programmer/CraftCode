package org.spectrumworkshop.craftcode.Actions;

import java.util.ArrayList;

import org.spectrumworkshop.craftcode.Events.BlockPlace;
import org.spectrumworkshop.craftcode.Events.PlayerJoin;
import org.spectrumworkshop.craftcode.Functions.Range;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Boolean;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Condition;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Inequality;
import org.spectrumworkshop.craftcode.Values.Basic.CCString;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Arithmetic;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.CraftCode.CCSystem;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.VarAddition;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

public class DotAction extends Actions implements Values {
    
    private final ArrayList<String> dots = new ArrayList<>();
    private final ArrayList<Values> params = new ArrayList<>();
    private final static String PATTERN = "^(~®~)º(?:\\.(?:(~®~)(~¶~)?)+)";

    private int paramsAmount;

    public void AddDot(String line) { dots.add(line); }

    public void AddParam(Values line) { params.add(line); }

    public void Increment(){paramsAmount++;}

    public int GetParamsSize() { return paramsAmount; }

    private static Values ReturnValue(String string) throws Exception {
        if (Condition.isCondition(string)) {
            return Condition.CreateCondition(string);
        }
        else if (Inequality.isInequality(string)) {
            return Inequality.CreateInequality(string);
        }
        else if (Arithmetic.isArithmetic(string)) {
            return Arithmetic.CreateArithmetic(string);
        }
        else if (Range.isRange(string)){
            return Range.CreateRange(string);
        } else if (DotAction.isDotAction(string)){
            ArrayList<String> comp = SWRegex.Compress(string);
            String input = comp.get(0);
            comp.remove(0);
            return DotAction.CreateDotAction(input, comp);
        }
        else if (VarAddition.isVarAddition(string)) {
            return VarAddition.CreateVarAddition(string, null);
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
        } else {
            throw new Exception("Invalid Parameters");
        }
    }

    public static boolean isDotAction(String line) {
        line = line.strip();
        return SWRegex.Match(line, SWRegex.CleanRegex(PATTERN, null));
    }

    public static DotAction CreateDotAction(String line, ArrayList<String> containers) throws Exception {
        line = line.strip();
        DotAction action = new DotAction();

        SWRegex matcher = new SWRegex(line, PATTERN);
        if (matcher.Find(null)) {
            ArrayList<String> list = matcher.AsList();
            action.AddDot(list.get(0));
            for (int i = 1; i < list.size(); i += 2) {
                action.AddDot(list.get(i));
                String param = list.get(i + 1);
                if (param != null) {
                    String val = param.substring(1, param.length() - 1).replaceFirst("Container", "");
                    action.AddParam(ReturnValue(containers.get(Integer.parseInt(val))));
                    action.Increment();
                } else {
                    action.AddParam(null);
                }

            }
        }
        return action;
    }

    public static String CreateBracketForm(String line, ArrayList<String> containers) throws Exception {
        String pattern = "^~®~º(?:\\.(?:~®~\\(Container(~©~)\\)?)+)";
        SWRegex matcher = new SWRegex(line, pattern);
        while (matcher.Find(null)) {
            int number = Integer.parseInt(matcher.AsList().get(0));
            line = line.replaceFirst(SWRegex.CleanRegex("Container~©~"), containers.get(number));
            matcher = new SWRegex(line, pattern);
        }
        return line;
    }
    
    @SuppressWarnings("unchecked")
    public Values InterpretDot(ArrayList<Variable> variables) throws Exception {
        ArrayList<String> tempList = (ArrayList<String>) dots.clone();
        tempList.remove(0);
        if (dots.get(0).equals("System")) {
            return CCSystem.Interpret(tempList, params, variables);
        } else {
            for (Variable var : variables) {
                if (var.GetName().equals(dots.get(0))) {
                    switch (var.GetValue()) {
                        case PlayerJoin pJ -> {
                            return pJ.Interpret(tempList, params, variables); }
                        case BlockPlace bP -> {
                            return bP.Interpret(tempList, params, variables); }
                        default -> { }
                    }
                }
            }
        }

        throw new Exception("Variable " + dots.get(0));
    }

    @Override
    public ArrayList<Variable> Interpret(ArrayList<Variable> variables) throws Exception{
        InterpretDot(variables);
        return variables;
    }
}
