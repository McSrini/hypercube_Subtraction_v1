/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.cplex;

import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ONE;
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.TWO;
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ZERO;
import ca.mcmaster.hypercube_Subtraction_v1.collection.BranchingVariableSuggestor;
import ca.mcmaster.hypercube_Subtraction_v1.collection.LeafNode;
import ca.mcmaster.hypercube_Subtraction_v1.collection.Rectangle;
import ca.mcmaster.hypercube_Subtraction_v1.collection.RectangleCollector;
import ca.mcmaster.hypercube_Subtraction_v1.common.LowerBoundConstraint;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BranchCallback;
import java.util.*;

/**
 *
 * @author tamvadss
 * 
 * for this leaf, get best vertex and constraints violated by this best vertex
 * 
 * 
 */
public class BranchHandler extends BranchCallback{
    
    
    private BranchingVariableSuggestor branchingVarSuggestor = new BranchingVariableSuggestor();
    
    private double[ ][] bounds ;
    private IloNumVar[][] vars;
    private IloCplex.BranchDirection[ ][]  dirs;
    
    private Map<String, IloNumVar> modelVariables = new TreeMap<>();
    
    public long numBranchingDecisionsOverruled = ZERO;
    
    public BranchHandler (IloNumVar[] variables) {
        for (IloNumVar var : variables) {
            modelVariables.put (var.getName(), var);
        }
       
    }
 
    protected void main() throws IloException {
        if ( getNbranches()> ZERO ){  
            
                       
            //get the node attachment for this node, any child nodes will accumulate the branching conditions
            if (null==getNodeData()){
                //root of mip
                NodeAttachment data = new NodeAttachment (  );
                setNodeData(data);                
            } 
            
            NodeAttachment nodeData = (NodeAttachment) getNodeData();
            
            List<String> myZeroFixings = nodeData.zeroFixedVars;
            List<String> myOneFixings = nodeData.oneFixedVars;
            
            LeafNode thisLeaf = new LeafNode ( myZeroFixings ,myOneFixings) ; 
            thisLeaf.findVarFixingsAtBestVertex();
            
            List<Rectangle> hypercubesList = new ArrayList<Rectangle>();
            for ( LowerBoundConstraint lbc : thisLeaf.getConstraintsViolatedAtBestVertex()){
                RectangleCollector collector = new RectangleCollector(thisLeaf);
                Rectangle hyperCube = collector.collectBiggest_INFeasibleHyperCube(lbc);
                //logger.debug ("biggest infes rect for " +lbc  + " is " + hyperCube); 
                hypercubesList.add(hyperCube);
            }
            List<String> suggestedBranchingVars = this.branchingVarSuggestor.getBranchingVar(hypercubesList);
            
            // branches about to be created by CPLEX
            vars = new IloNumVar[TWO][] ;
            bounds = new double[TWO ][];
            dirs = new  IloCplex.BranchDirection[ TWO][];
            getBranches( vars, bounds,  dirs) ;
            String branchingVariable  = vars[ZERO][ZERO].getName();
            
            if (! suggestedBranchingVars.contains(vars[ZERO][ZERO].getName() )) {
                
                //overrule CPLEX
                numBranchingDecisionsOverruled ++;
                //if many suggestions , take first one in alphabetic order
                branchingVariable = Collections.min( suggestedBranchingVars);
                //System.out.println("Cplex decsion was " + vars[ZERO][ZERO].getName() + " overruled with "+ branchingVariable);
                
                this.getVarsNeededForCplexBranching(branchingVariable  );
                
            }else {
                //System.out.println("Cplex decsion was accepted " + vars[ZERO][ZERO].getName());
            }
            
            //
 
            //attach node data to both child nodes and execute the 2 branches
            createChildNodes (  nodeData,   branchingVariable   );
 
            
        }
    }//end main
    
    private void getVarsNeededForCplexBranching (String branchingVar ){
        //get var with given name, and create up and down branch conditions
        vars[ZERO] = new IloNumVar[ONE];
        vars[ZERO][ZERO]= this.modelVariables.get(branchingVar );
        bounds[ZERO]=new double[ONE ];
        bounds[ZERO][ZERO]=ZERO;
        dirs[ZERO]= new IloCplex.BranchDirection[ONE];
        dirs[ZERO][ZERO]=IloCplex.BranchDirection.Down;

        vars[ONE] = new IloNumVar[ONE];
        vars[ONE][ZERO]= this.modelVariables.get(branchingVar );
        bounds[ONE]=new double[ONE ];
        bounds[ONE][ZERO]=ONE;
        dirs[ONE]= new IloCplex.BranchDirection[ONE];
        dirs[ONE][ZERO]=IloCplex.BranchDirection.Up;
    }
    
    private void createChildNodes (NodeAttachment nodeData, String branchingVar ) throws IloException {
        for (int childNum = ZERO ;childNum<TWO;  childNum++) {  
            
            NodeAttachment thisChild  =  new NodeAttachment ( ); 
            thisChild.zeroFixedVars.addAll( nodeData.zeroFixedVars );
            thisChild.oneFixedVars.addAll( nodeData.oneFixedVars);
            if (ZERO==childNum) {
                thisChild.zeroFixedVars.add(  branchingVar ) ;
            }else {
                thisChild.oneFixedVars.add( branchingVar );
            }
            
            makeBranch( vars[childNum],  bounds[childNum],dirs[childNum],   getObjValue() , thisChild);
            
        }
    }
    
}
