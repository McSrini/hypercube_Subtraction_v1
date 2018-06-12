/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.collection;

import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ZERO;
import ca.mcmaster.hypercube_Subtraction_v1.TestDriver;
import static ca.mcmaster.hypercube_Subtraction_v1.Parameters.USE_STRICT_INEQUALITY_IN_MIP;
import ca.mcmaster.hypercube_Subtraction_v1.common.LowerBoundConstraint;
import ca.mcmaster.hypercube_Subtraction_v1.common.VariableCoefficientTuple;
import java.util.*;

/**
 *
 * @author tamvadss
 * 
 * leaf node of BNB tree
 * 
 */
public class LeafNode extends Rectangle{
    
    public List<String> bestVertex_zeroFixedVariables = new ArrayList<String>();
    public List<String> bestVertex_oneFixedVariables = new ArrayList<String>();
    
    public LeafNode(List<String> zeroFixedVariables, List<String> oneFixedVariables) {
        super(zeroFixedVariables, oneFixedVariables);
    }
    
    //finds     vars fixings to get  best vertex
    public void findVarFixingsAtBestVertex () {
        for (VariableCoefficientTuple tuple : TestDriver.objective.objectiveExpr){
            String thisVar = tuple.varName;
            if (this.zeroFixedVariables.contains(thisVar) || this.oneFixedVariables.contains(thisVar )) {
                //already fixed, do nothing
            }else {
                //choose fixing so that objective becomes lowest possible
                if (tuple.coeff < ZERO){
                    bestVertex_oneFixedVariables.add(thisVar);
                } else {
                    bestVertex_zeroFixedVariables.add(thisVar);
                }
            }
        }
    }
    
    public List<LowerBoundConstraint> getConstraintsViolatedAtBestVertex(){
        List<LowerBoundConstraint> violatedConstraints = new ArrayList<LowerBoundConstraint>();
        
        for (LowerBoundConstraint lbc : TestDriver.mipConstraintList){
            LowerBoundConstraint reducedConstraint = lbc.getReducedConstraint( this.zeroFixedVariables, this.oneFixedVariables  );
            reducedConstraint = reducedConstraint.getReducedConstraint(bestVertex_zeroFixedVariables ,  bestVertex_oneFixedVariables);
            if (! reducedConstraint.isGauranteedFeasible( USE_STRICT_INEQUALITY_IN_MIP)) violatedConstraints.add(lbc);
        }
        
        return violatedConstraints;
    }
    
    
    
}
