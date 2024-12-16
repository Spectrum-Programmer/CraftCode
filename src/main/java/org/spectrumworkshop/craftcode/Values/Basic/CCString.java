package org.spectrumworkshop.craftcode.Values.Basic;

// Java Packages
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Condition;
import org.spectrumworkshop.craftcode.Values.Basic.Bool.Inequality;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Arithmetic;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

/** <strong> Description: </strong> Holds String objects and methods. */
public class CCString implements Values {

    private String string;
    private final static String PATTERN =  
    "^(?:(~∆~)~µ~\\+~µ~)*º(~∑~)º(?:~µ~\\+~µ~(~∆~|~∑~))*" ;
    private final static ArrayList<String> regexSubs = new ArrayList<>(Arrays.asList(
            "∆", "(?:~∂~)|~¶~|~®~|~©~",
                 "∑", "\".*?\""
    ));

    /* ------------------
       Getters and Setters
       ------------------ */

    public void SetValue(String string) { this.string = string; }

    public String GetValue() { return string; }

    /* ---------------
       ToString Method
       --------------- */

    public CCString(String string) {
        this.string = string;
    }

    @Override
    public String toString() { return "" + string.replaceAll("\"", ""); }

    public static String ReturnAsString(String string, ArrayList<String> containers) throws Exception {
        string = string.strip();
        if (Condition.isCondition(string) || Inequality.isInequality(string)
                || Arithmetic.isArithmetic(string)) {
            return "[" + string + "]";
        }
        else if (SWRegex.Match(string, "∂")) {
            return "[" + DotAction.CreateBracketForm(string, containers) + "]";
        }
        else if (CCString.isString(string)) {
            Matcher shortMatcher = SWRegex.CreateMatcher(string, "\\(Container(~©~)\\)");
            while (shortMatcher.find()) {
                int val = Integer.parseInt(shortMatcher.group(1));
                string = string.replaceFirst(SWRegex.CleanRegex("Container(~©~)"), containers.get(val));
                shortMatcher = SWRegex.CreateMatcher(string, "\\(Container(~©~)\\)");
            }
            return string.substring(1, string.length() - 1);
        }
        else if (SWRegex.Match(string, "©")) {
            return string;
        }
        else if (SWRegex.Match(string, SWRegex.CleanRegex("®"))) {
            switch (string) {
                case "true", "false" -> {
                    return string;
                }
                default -> {
                    return "[" + string + "]";
                }
            }
        }
        else if (SWRegex.Match(string, "\\(.*?\\)")) {
            String val = string.substring(1, string.length() - 1).replaceFirst("Container", "");
            return ReturnAsString(containers.get(Integer.parseInt(val)), null);
        }
        else {
            throw new Exception("Invalid String Construction!!");
        }
    }

    public static boolean isString(String string) {
        string = string.strip();
        String stringPattern = SWRegex.CleanRegex(PATTERN, regexSubs);
        return SWRegex.Match(string, stringPattern);
    }
    
    public static CCString CreateString(String string) throws Exception {
        string = string.strip();
        SWRegex matcher = new SWRegex(string, PATTERN);
        String returnString = "";
        if (matcher.Find(regexSubs, true)) {
            ArrayList<String> strings = matcher.AsList();
            ArrayList<String> containers = matcher.GetContainers();
            
            for (String value : strings) {
                returnString += ReturnAsString(value, containers);
            }
        }
        return new CCString(returnString);
    }

    /* ----------------
       String Modifiers
       ---------------- */

    public static String ReplaceBrackets(String string, ArrayList<Variable> variables) 
    throws Exception {
        String[] list = string.split("\\[.*?\\]");
        String newString = list[0];
        Matcher matcher = SWRegex.CreateMatcher(string, "\\[(.*?)\\]");
        int i = 1;
        while (matcher.find()) {
            String found = matcher.group(1).strip();
            if (Arithmetic.isArithmetic(found)){
                newString += Arithmetic.CreateArithmetic(found).Evaluate(variables);
                if (i < list.length) { newString += list[i]; }
            } else if (Inequality.isInequality(found)) {
                newString += Inequality.CreateInequality(found).Evaluate(variables);
                if (i < list.length) { newString += list[i]; }
            } else if (Condition.isCondition(found)) {
                newString += Condition.CreateCondition(found).Evaluate(variables);
                if (i < list.length) { newString += list[i]; }
            }
            else if (SWRegex.Match(found, "∂")) {
                ArrayList<String> dotStringComp = SWRegex.Compress(found);
                String dotString = dotStringComp.remove(0);
                newString += DotAction.CreateDotAction(dotString, dotStringComp).InterpretDot(variables);
                if (i < list.length) { newString += list[i]; }
            }
            else if (SWRegex.Match(found, "®")) {
                boolean foundVar = false;
                for (Variable var : variables) {
                    if (var.GetName().equals(found)) {
                        foundVar = true;
                        newString += var.GetValue();
                        if (i < list.length) {
                            newString += list[i];
                        }
                        break;
                    }
                }
                if (!foundVar) {
                    throw new Exception("Variable " + found + " not found!");
                }
            } else {
                throw new Exception("String [] value could not be computed");
            }
            i++;
        }
        return newString;
    }
}
