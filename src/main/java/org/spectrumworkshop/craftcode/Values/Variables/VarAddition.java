package org.spectrumworkshop.craftcode.Values.Variables;

// Java Packages
import java.util.ArrayList;

import org.spectrumworkshop.craftcode.Actions.DotAction;
import org.spectrumworkshop.craftcode.Values.Basic.CCString;
import org.spectrumworkshop.craftcode.Values.Basic.Num.Number;
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.tools.SWRegex;

/**
 * <strong> Description: </strong> Holds variable addition expressions until
 * the necessary operation (String Concatenation or Number Addition) is known.
 */
public class VarAddition implements Values {
    
   private ArrayList<Values> vars = new ArrayList<>();
   private final static String PATTERN = "^((?:~∂~)|~®~)º(?:~µ~\\+~µ~((?:~∂~)|~®~))+";

    /* -----------
       List Adders
       ----------- */

    public VarAddition(ArrayList<Values> vars) { this.vars = vars; }

    public void AddVar(Values var) { vars.add(var); }
    public void SetVars(ArrayList<Values> vars) { this.vars = vars; }

    /* --------------
       Getter Methods
       -------------- */

    public ArrayList<Values> GetVars() { return vars; }
    

    public static boolean isVarAddition(String string) {
        string = string.strip();
        String stringPattern = SWRegex.CleanRegex(PATTERN, null);
        return SWRegex.Match(string, stringPattern);
    }
    
    public static VarAddition CreateVarAddition(String string, ArrayList<String> containers) throws Exception {
        string = string.strip();
        SWRegex matcher = new SWRegex(string, PATTERN);
        if (matcher.Find(null)) {
           ArrayList<String> list = matcher.AsList();
           ArrayList<Values> returnList = new ArrayList<>();
           for (String item : list) {
              if (SWRegex.Match(item, "∂")) {
               returnList.add(DotAction.CreateDotAction(item, containers));
              }
              else if (SWRegex.Match(item, "®")) {
                 returnList.add(new Variable(item));
              }
           }
           return new VarAddition(returnList);
        }
        return null;
    }

    public Values Evaluate(ArrayList<Variable> variables) throws Exception {
       ArrayList<Values> values = new ArrayList<>();
       boolean concat = false;
       for (Values val : vars) {
          if (val instanceof Variable var) {
             boolean varFound = false;
             for (Variable vari : variables) {
                if (vari.GetName().equals(var.GetName())) {
                   varFound = true;
                   values.add(vari.GetValue());
                   if (vari.GetValue() instanceof CCString) {
                      concat = true;
                      break;
                   } else if (!(vari.GetValue() instanceof Number)) {
                      throw new Exception("Variable addition can only work between numbers or strings");
                   }
                }
             }
             if (!varFound) {
               throw new Exception("Variable " + var.GetName() + " not found!!");
           }
          } /*else if (val instanceof DotAction dot) {

          }*/
       }
       Values returnVal;
       if (concat) {
          String returnString = "";
          for (Values val : values) {
             CCString string = (CCString) val;
             returnString += string;
          }
          returnVal = new CCString(returnString);
       } else {
          float returnFloat = 0;
          for (Values val : values) {
             Number num = (Number) val;
             returnFloat += num.GetValue();
          }
          returnVal = new Number(returnFloat);
       }
       return returnVal;
    } 
}
