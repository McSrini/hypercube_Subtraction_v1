/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.collection;

import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ONE;
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ZERO;
import ca.mcmaster.hypercube_Subtraction_v1.TestDriver;
import static ca.mcmaster.hypercube_Subtraction_v1.TestDriver.allVariablesInModel;
import static ca.mcmaster.hypercube_Subtraction_v1.Parameters.USE_STRICT_INEQUALITY_IN_MIP;
import ca.mcmaster.hypercube_Subtraction_v1.common.*;
import ilog.concert.IloException;
import java.util.*;

/**
 *
 * @author tamvadss
 */
public class RectangleCollector {
    
    //this is the ubc for which we will collect the best feasible hypercube    
    private UpperBoundConstraint ubc  = null;
    private Rectangle bestVertex = null;
    private List <String> leaf_ZeroFixedVariables ;
    private List <String> leaf_OneFixedVariables;
   
    public RectangleCollector (LeafNode leaf  ) {
        
        List <String> zeroFixings = new ArrayList <String>();
        List <String> oneFixings  = new ArrayList <String>();
        zeroFixings.addAll(  leaf.zeroFixedVariables);
        zeroFixings.addAll( leaf.bestVertex_zeroFixedVariables);
        oneFixings.addAll( leaf.oneFixedVariables );
        oneFixings.addAll(  leaf.bestVertex_oneFixedVariables);
        //now we are ready to construct the best vertex
        this.bestVertex = new Rectangle (zeroFixings,oneFixings ) ;
        
        this.leaf_ZeroFixedVariables= leaf.zeroFixedVariables;
        this.leaf_OneFixedVariables = leaf.oneFixedVariables;        
    }  
    

    
    
    public Rectangle collectBiggest_INFeasibleHyperCube (LowerBoundConstraint lbc) {
    
        //collect feasible hypercube for the flipped ubc
        this.ubc=new UpperBoundConstraint(  lbc.getReducedConstraint(leaf_ZeroFixedVariables, leaf_OneFixedVariables)) ;
        //this is the slack in the UBC at the best vertex
        double remainingSlack = getSlackAtBestVertex ( lbc) ;
                
        //we make note of all the vars which cannot be flipped.  
        List<String> variablesWhichCannotBeFlipped =  new ArrayList<String>();

        
        for (VariableCoefficientTuple tuple : ubc.sortedConstraintExpr){
            //if this var is not already at its best value, see if we can flip it without violating the ubc
            //if it is already at its best value, it can of course be flipped
            //
            //note that best value in this context is w.r.t. the ubc constraint, not the objective
            //we are trying to grow a large feasible hypercube for the ubc , starting from the best objective vertex. 
            // I.E. we try to tighten the constraint as much as we can without violating it.
            //
            //we make note of all the vars which cannot be flipped.  
           
            //note that we try to flip only those vars which are free - this is accounted for by considering the reduced constraint
                        
            boolean isAlreadyAtBestValue = false;
            if (tuple.coeff>ZERO && this.bestVertex.oneFixedVariables.contains (tuple.varName)  ) isAlreadyAtBestValue= true;
            if (tuple.coeff<ZERO && this.bestVertex.zeroFixedVariables.contains(tuple.varName)  ) isAlreadyAtBestValue= true;
            
            if (isAlreadyAtBestValue) {
                //can be flipped, ignore such variables
            }else {
                
                //check if flipping it violates the constraint 
                //else   record its flip, and note the amount by which ubc gets tightened
                boolean isGoingToBeViolated = false;
                if (!USE_STRICT_INEQUALITY_IN_MIP) {
                    isGoingToBeViolated=  ((remainingSlack- Math.abs(tuple.coeff))<= ZERO);
                }else {
                    isGoingToBeViolated=  ((remainingSlack- Math.abs(tuple.coeff))< ZERO);
                }
                
                if (isGoingToBeViolated) {
                    //cannot flip
                    variablesWhichCannotBeFlipped.add( tuple.varName);
                }else {
                    //flip it                   
                    remainingSlack -=  Math.abs(tuple.coeff);
                }
                 
            }//end if else
            
        }    //end for    
        
        //The hypercube we are looking for is defined by all the vars fixed at the best objective vertex (i.e. the ones which cannot be flipped).
        List<String> zeroVarsInBiggestHypercube= new ArrayList<String>();
        List<String> oneVarsInBiggestHypercube = new ArrayList<String>();        
        for (String cannotFlipVar: variablesWhichCannotBeFlipped){
            if (this.bestVertex.zeroFixedVariables.contains(cannotFlipVar )) {
                zeroVarsInBiggestHypercube.add(  cannotFlipVar);
            }else {
                oneVarsInBiggestHypercube.add( cannotFlipVar );
            }
        }
           
        return new Rectangle (zeroVarsInBiggestHypercube, oneVarsInBiggestHypercube) ;
    
    }
        
    //slack for ubc at best vertex
    private double getSlackAtBestVertex (LowerBoundConstraint lbc ) {
        LowerBoundConstraint reduced = lbc.getReducedConstraint(this.bestVertex.zeroFixedVariables ,this.bestVertex.oneFixedVariables  );
        //slack for ubc is = violation of the lbc
        return  reduced.lowerBound;
    }
    
    
}

