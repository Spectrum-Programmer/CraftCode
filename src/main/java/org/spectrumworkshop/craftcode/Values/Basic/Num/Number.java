package org.spectrumworkshop.craftcode.Values.Basic.Num;

// My Packages
import org.spectrumworkshop.craftcode.Values.Values;


/**
 * <strong> Description: </strong> Object holding a number value (Int or Float)
 */
public class Number implements Values {
        
   protected float value;

   // Constructor
   public Number(float num) { value = num; }
   
   public Number() { value = 0; }

   /* ------------------
      Getters and Setters
      ------------------ */
   
   public void SetValue(float num) { this.value = num; }

   public float GetValue() { return value; }

   /* ---------------
      Integer Methods
      --------------- */

   /** 
   * <strong> Description: </strong> Determine if a Num value is an integer.
   */
   public boolean IsInt(){ return(value % 1 == 0); }

   /**
   * <strong> Description: </strong> Return a num value as an integer.
   * @return 
   */
   public int GetInt(){
      return (int)value;
   }

   /* ---------------
      ToString Method
      --------------- */

   @Override
   public String toString() { return "" + value; }
}
