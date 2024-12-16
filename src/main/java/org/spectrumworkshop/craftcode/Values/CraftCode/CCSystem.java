package org.spectrumworkshop.craftcode.Values.CraftCode;

import java.util.ArrayList;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Functions.Range;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Boolean;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Condition;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Inequality;
import org.spectrumworkshop.craftcode.Values.Basic.CCString;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Arithmetic;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;

public class CCSystem {
    

    public static Values Interpret(ArrayList<String> dots, ArrayList<Values> params,
            ArrayList<Variable> variables) 
    throws Exception {
        String dot = dots.get(0);
        switch (dot) {
            case "chat" -> {
                Chat(params.get(0), variables);
            }
            case "seed" -> {
                return Seed(params.get(0), variables);
            }
        }
        return null;
    }

    private static void Chat(Values chat, ArrayList<Variable> variables) throws Exception {
        System.out.println("" + EvaluateInput(chat, variables));
    }

    private static Number Seed(Values chat, ArrayList<Variable> variables) throws Exception {
        if (chat instanceof Range range) {
            int num1 = 0;
            int num2 = 0;
            switch (range.GetTerm(0)) {
                case Arithmetic arith -> num1 = (int) arith.Evaluate(variables);
                case DotAction dotAct -> {
                    Values val = dotAct.InterpretDot(variables);
                    if (val instanceof Number num) {
                        num1 = (int) num.GetValue();
                    } else {
                        throw new Exception("Dot Action does not return a number!!");
                    }
                }
                case Number num -> num1 = (int) num.GetValue();
                case Variable vari -> {
                    for (Variable var : variables) {
                        if (var.GetName().equals(vari.GetName())) {
                            Values val = vari.GetValue();
                            if (val instanceof Number num) {
                                num1 = (int) num.GetValue();
                            } else {
                                throw new Exception("Variable is not a number!!");
                            }
                            break;
                        }
                    }
                    throw new Exception("Variable " + chat + " not found!!");
                }
                default -> throw new Exception("Unknown Chat Input!!");
            }
            switch (range.GetTerm(1)) {
                case Arithmetic arith -> num2 = (int) arith.Evaluate(variables);
                case DotAction dotAct -> {
                    Values val = dotAct.InterpretDot(variables);
                    if (val instanceof Number num) {
                        num2 = (int) num.GetValue();
                    } else {
                        throw new Exception("Dot Action does not return a number!!");
                    }
                }
                case Number num -> num2 = (int) num.GetValue();
                case Variable vari -> {
                    for (Variable var : variables) {
                        if (var.GetName().equals(vari.GetName())) {
                            Values val = vari.GetValue();
                            if (val instanceof Number num) {
                                num2 = (int) num.GetValue();
                            } else {
                                throw new Exception("Variable is not a number!!");
                            }
                            break;
                        }
                    }
                    throw new Exception("Variable " + chat + " not found!!");
                }
                default -> throw new Exception("Unknown Chat Input!!");
            }
            int randomNum = (int) (Math.random() * (num2 - num1 + 1)) + num1;
            return new Number(randomNum);
        } else {
            throw new Exception("Seed function only takes in a Range!!");
        }
    }

    public static Values EvaluateInput(Values value, ArrayList<Variable> variables) throws Exception{
        switch (value) {
            case Condition cond -> {
                return new Boolean(cond.Evaluate(variables)); }
            case Inequality ineq -> {
                return new Boolean(ineq.Evaluate(variables));}
            case Arithmetic arith -> { 
                return new Number(arith.Evaluate(variables));}
            case DotAction dotAct -> {
                return dotAct.InterpretDot(variables); }
            case CCString string ->
                { return new CCString(CCString.ReplaceBrackets(string.toString(), variables)); }
            case Number num -> {
                return num; }
            case Boolean bool -> {
                return bool; }
            case Variable vari -> {
                for (Variable var : variables) {
                    if (var.GetName().equals(vari.GetName())) {
                        return var.GetValue();
                    }
                }
                throw new Exception("Variable " + value + " not found!!");
            }
            default -> throw new Exception("Unknown Chat Input!!");
        }
    }
}
