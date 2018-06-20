/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.cplexRef;
 
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ONE;
import static ca.mcmaster.hypercube_Subtraction_v1.Parameters.*;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.cplex.IloCplex;
import static ilog.cplex.IloCplex.MIPEmphasis.BestBound;

/**
 *
 * @author tamvadss
 */
public class CplexRefTree {
    
    private IloCplex cplex ;
        
    public CplexRefTree () throws IloException {
        cplex = new IloCplex() ;
        cplex.importModel(MIP_FILENAME);
        
        cplex.setParam(IloCplex.Param.Emphasis.MIP, BestBound);
        cplex.setParam( IloCplex.Param.MIP.Strategy.HeuristicFreq , -ONE);
        cplex.setParam(IloCplex.Param.MIP.Limits.CutPasses, -ONE);
        cplex.setParam(IloCplex.Param.Preprocessing.Presolve, false);
        cplex.setParam(IloCplex.Param.MIP.Strategy.File, ONE+ONE+ONE);
                
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        RefBranchHandler  rbh = new RefBranchHandler  ( lpMatrix.getNumVars());
        cplex.use (rbh);
    }
    
    public void solve () throws IloException {
        cplex.solve();
        
    }
    
}
