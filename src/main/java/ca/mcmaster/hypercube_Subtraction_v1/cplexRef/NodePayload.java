/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.cplexRef;

import ca.mcmaster.hypercube_Subtraction_v1.collection.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 */
public class NodePayload {
    
    public List<String > zeroFixedVars = new ArrayList <String > ();
    public List<String > oneFixedVars = new ArrayList <String > ();
    
    List<Rectangle> hypercubesList = null;
    
}
