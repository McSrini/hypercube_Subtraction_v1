/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.collection;
 
import ilog.concert.IloException;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 * 
 * this class represents a collected infeasible hypercube
 * 
 */
public class Rectangle {
    
    
    //note that some vars can be free
    public List <String> zeroFixedVariables = new ArrayList <String>();
    public List <String> oneFixedVariables = new ArrayList <String>();
     
    
    public Rectangle (List <String> zeroFixedVariables , List <String> oneFixedVariables ){
        this.zeroFixedVariables .addAll(zeroFixedVariables);
        this.oneFixedVariables  .addAll( oneFixedVariables);      
    }
    
    /*public String toString (){
        
        String result=" "; 
        result += " --- Zero fixed vars :";
        for (String str: zeroFixedVariables){
            result += str + ",";
        }
        result += "  -- One fixed vars :";
        for (String str: oneFixedVariables){
            result += str + ",";
        }
        return result;

    }*/
    
     
}
