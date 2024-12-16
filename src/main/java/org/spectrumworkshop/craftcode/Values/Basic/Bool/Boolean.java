package org.spectrumworkshop.craftcode.Values.Basic.Bool;

// My Packages
import org.spectrumworkshop.craftcode.Values.Values;
import org.spectrumworkshop.tools.SWRegex;


/**
 * <strong> Description: </strong> Object that holds booleans.
 */
public class Boolean implements Values {
    
   protected boolean bool;
   protected boolean inverted;

   public Boolean() {}
   
   public Boolean(boolean bool) {this.bool = bool;}
   
    /* -------------------
       Getters and Setters
       ------------------- */

   public void GetValue(boolean value) { bool = value; }
    
   public boolean GetValue() { return bool;}
    
   public void SetInverted(boolean inverted){this.inverted = inverted;}
    
    /* ---------------
       ToString Method
       --------------- */

   public static boolean isBoolean(String block) {
        block = block.strip(); // Strip block to ensure no whitespace
        //Matcher match = SWRegex.CreateMatcher(block, inequalityPattern);
        return SWRegex.Match(block, "^true|false|(?:∂)|®");
    }

    @Override
    public String toString() { return "" + bool; }
}

