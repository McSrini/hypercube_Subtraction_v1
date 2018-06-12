/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.cplex;

import static ca.mcmaster.hypercube_Subtraction_v1.Parameters.MIP_FILENAME;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.cplex.IloCplex;
import static ilog.cplex.IloCplex.MIPEmphasis.BestBound;

/**
 *
 * @author tamvadss
 */
public class CplexTree {
    
    private IloCplex cplex ;
    private BranchHandler bh;
    
    public CplexTree () throws IloException {
        cplex = new IloCplex() ;
        cplex.importModel(MIP_FILENAME);
        
        cplex.setParam(IloCplex.Param.Emphasis.MIP, BestBound);
        
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        bh = new BranchHandler(lpMatrix.getNumVars());
        cplex.use (bh);
    }
    
    public void solve () throws IloException {
        cplex.solve();
        System.out.println("\n Number of times cplex was overruled = "+bh.numBranchingDecisionsOverruled) ;
    }
    
}
