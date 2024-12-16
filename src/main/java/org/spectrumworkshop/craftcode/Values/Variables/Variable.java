package org.spectrumworkshop.craftcode.Values.Variables;

// Java Packages
import java.util.ArrayList;

import org.spectrumworkshop.craftcode.Values.Values;

/**
 * <strong> Description: </strong> Variable object, has an identifer and holds
 * a value (Number, Arithmetic, Boolean, Condition, String, VarAddition)
 */
public class Variable implements Values {

    private String name;
    private Values value;
    protected boolean inverted;

    // Constructor
    public Variable(String name) { this.name = name; }

    /* -------------------
       Getters and Setters
       ------------------- */

    public void SetName(String name) { this.name = name; }
    public void SetValue(Values value) { this.value = value; }
    public void SetInverted(boolean inverted) { this.inverted = inverted; }
    
    public String GetName(){return name;}
    public Values GetValue(){return value;}

    public static ArrayList<Variable> SetVariable(Variable value, ArrayList<Variable> variables) throws Exception {
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).GetName().equals(value.GetName())) {
                variables.set(i, value);
                return variables;
            }
        }
        variables.add(value);
        return variables;
    }

    public static ArrayList<Variable> UpdateList(ArrayList<Variable> old, ArrayList<Variable> newOne) {
        for (int i = 0; i < old.size(); i++) { old.set(i, newOne.get(i));}
        return old;
    }

}
