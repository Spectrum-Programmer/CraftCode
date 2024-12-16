package org.spectrumworkshop.craftcode.Interpreters;

import java.util.ArrayList;

import org.spectrumworkshop.craftcode.Actions.Actions;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;

public class Interpreter {
    
    @SuppressWarnings("unchecked")
    public static ArrayList<Variable> Interpret(ArrayList<Actions> actions, ArrayList<Variable> variables)
            throws Exception {
        ArrayList<Variable> local = new ArrayList<>();
        if (variables != null) {
            local = (ArrayList<Variable>)variables.clone();
        }
        for (Actions action : actions) {
            local = action.Interpret(local);
        }
        if(variables != null){
            return Variable.UpdateList(variables, local);
        } else {
            return local;
        }
        
    }

}
