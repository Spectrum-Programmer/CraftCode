package org.spectrumworkshop.craftcode.Values.CraftCode;

import java.util.ArrayList;

import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.craftcode.Values.Variables.Variable;

// Minecraft Packages

/**
 * <strong> Description: </strong> Object that holds Minecraft Event objects, 
 * and carries the methods needed to translate CraftCode instructions to 
 * JavaPlugin instructions.
 */
public interface CCEvent {

    /* ---------------
       ToString Method
       --------------- */
    
    @Override
    public abstract String toString();

    public Values ReturnValue(String value) throws Exception;

    public Values Interpret(ArrayList<String> dots, ArrayList<Values> params, 
            ArrayList<Variable> variables) throws Exception;
}
