package org.spectrumworkshop.craftcode.Functions;

import java.util.ArrayList;
import java.util.Arrays;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Arithmetic;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;
import org.spectrumworkshop.tools.SWRegex;

public class Range implements Values {
    
    Values term1; Values term2;
    private final static String PATTERN = "^(?:(~∆~)~µ~to~µ~(~∆~))";
    private final static ArrayList<String> regexSubs = new ArrayList<>(Arrays.asList(
        "∆", "(?:~∂~)|~¶~|~®~|~©~"
    ));

    public Range(Values term1, Values term2) {
        this.term1 = term1; this.term2 = term2; }

    public Values GetTerm(int index) {
            switch (index) {
                case 0 -> { return term1; }
                case 1 -> { return term2; }
            } return term1;
        }

    private static Values ReturnNumValue(String string, ArrayList<String> containers) throws Exception {
        // Compares operands to numbers, variables, & arithmetic operations
        if(SWRegex.Match(string, SWRegex.CleanRegex("©"))){  
            return new Number(Float.parseFloat(string)); } // Numbers
        else if (SWRegex.Match(string, SWRegex.CleanRegex("®"))) {
            return new Variable(string); } // Variables
        else if (Arithmetic.isArithmetic(string)) {
            return Arithmetic.CreateArithmetic(string);
        } // Paranthesis
        else if (SWRegex.Match(string, "∂")) {
            ArrayList<String> comp = SWRegex.Compress(string);
            String input = comp.get(0);
            comp.remove(0);
            return DotAction.CreateDotAction(input, comp);
        }
        else if (SWRegex.Match(string, "\\(.*?\\)")) {
            String val = string.substring(1, string.length() - 1).replaceFirst("Container", "");
            return ReturnNumValue(containers.get(Integer.parseInt(val)), null);
        }
        else{ throw new Exception("Arithmetic expressions can only contain numbers, variables, or other arithmetic expressions"); }
    }

    public static boolean isRange(String block) {
        block = block.strip(); 
        return SWRegex.Match(block, SWRegex.CleanRegex(PATTERN, regexSubs));
    }

    public static Range CreateRange(String block) throws Exception {
        block = block.strip();

        Values value1 = new Number(0); Values value2 = new Number(0);

        SWRegex matcher = new SWRegex(block, PATTERN);

        if (matcher.Find(regexSubs)) {
            ArrayList<String> list = matcher.AsList();
            value1 = ReturnNumValue(list.get(0), matcher.GetContainers());
            value2 = ReturnNumValue(list.get(1), matcher.GetContainers());
        }
                    
        return new Range(value1, value2);
    }

}
