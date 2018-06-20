/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1.cplexRef;

import static ca.mcmaster.hypercube_Subtraction_v1.Constants.MIP_ROOT_ID;
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ONE;
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.TWO; 
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.ZERO;
import ca.mcmaster.hypercube_Subtraction_v1.TestDriver;
import ca.mcmaster.hypercube_Subtraction_v1.collection.BranchingVariableSuggestor;
import ca.mcmaster.hypercube_Subtraction_v1.collection.LeafNode;
import ca.mcmaster.hypercube_Subtraction_v1.collection.Rectangle;
import ca.mcmaster.hypercube_Subtraction_v1.collection.RectangleCollector;
import ca.mcmaster.hypercube_Subtraction_v1.common.LowerBoundConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.NodeId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class RefBranchHandlerWithMap  extends IloCplex.BranchCallback{ 
    
    private BranchingVariableSuggestor branchingVarSuggestor = new BranchingVariableSuggestor();
    private Map<String, IloNumVar> modelVariables = new TreeMap<>();
        
    private double[ ][] bounds ;
    private IloNumVar[][] vars;
    private IloCplex.BranchDirection[ ][]  dirs;
    
    //here is map which substitutes for holding node data inside the cplex nodes
    //we need this so that we can use cplec option to save node files to disk ( i.e. cplex nodes cannot have node data)
    private Map<String, NodePayload> nodeDataMap = new HashMap<String, NodePayload>();
    
     
    public RefBranchHandlerWithMap (IloNumVar[] variables) {
        for (IloNumVar var : variables) {
            modelVariables.put (var.getName(), var);
        }
       
    }

    @Override
    protected void main() throws IloException {
        if ( getNbranches()> ZERO ){  
            
            boolean isMipRoot = (getNodeId().toString()).equals(MIP_ROOT_ID);            
            double lpEstimate = getObjValue();
            
            //get all hypercubes
            
            if (isMipRoot) {
                
                LeafNode thisLeaf = new LeafNode ( new ArrayList <String > (), new ArrayList <String > ()) ; 
                RectangleCollector collector =   new RectangleCollector(thisLeaf);
                
                NodePayload nodeData  = new NodePayload ();
                nodeData.hypercubesList = new ArrayList<Rectangle>();
                for ( LowerBoundConstraint lbc : thisLeaf.getConstraintsViolatedAtBestVertex()){
                                
                    Rectangle hyperCube = collector.collectBiggest_INFeasibleHyperCube(lbc);
                    //logger.debug ("biggest infes rect for " +lbc  + " is " + hyperCube); 
                    nodeData.hypercubesList.add(hyperCube);
                }
                
                this.nodeDataMap.put(MIP_ROOT_ID, nodeData);
                
            }// else if (null ==nodeData.hypercubesList|| nodeData.hypercubesList.size()==ZERO) {                
                //will use cplex default branch variable
            //}// else {
                //non mip root, and non null hypercube list, means we have a node with hypercubes passed down from parent
                //use these hypercubes
            // }
            
            NodePayload nodeData  = this.nodeDataMap.remove(getNodeId().toString() );
            
            // vars nneded for child node creation 
            vars = new IloNumVar[TWO][] ;
            bounds = new double[TWO ][];
            dirs = new  IloCplex.BranchDirection[ TWO][];
             
            //get branching var suggestion from hypercube list, if available
            if (null !=nodeData  ) {   
               
                List<String> excludedVars=new ArrayList<String>();
                excludedVars.addAll( nodeData.zeroFixedVars);
                excludedVars.addAll( nodeData.oneFixedVars);
                
                List<String>  suggestedBranchingVars = this.branchingVarSuggestor.getBranchingVar( nodeData.hypercubesList ,  excludedVars);
                //pick a branching var, and split hypercubes into left and right sections
                String branchingVariable =  getVarWithLargestObjCoeff( suggestedBranchingVars) ; 
                List<Rectangle> zeroChild_hypercubesList = new ArrayList<Rectangle> ();
                List<Rectangle> oneChild_hypercubesList = new ArrayList<Rectangle> ();
                splitHyperCubes (branchingVariable  ,zeroChild_hypercubesList ,oneChild_hypercubesList, nodeData.hypercubesList) ;
                
                getArraysNeededForCplexBranching(branchingVariable);
                
                //create node attachments for left and right child  
                NodePayload zeroChild_payload = getChildPayload (true,  branchingVariable, nodeData, zeroChild_hypercubesList) ;
                NodePayload oneChild_payload =getChildPayload (false,  branchingVariable,nodeData, oneChild_hypercubesList) ;
                
                //create both kids
                NodeId zeroChildID = makeBranch( vars[ZERO],  bounds[ZERO],dirs[ZERO],  lpEstimate    );
                NodeId oneChildID  = makeBranch( vars[ONE],  bounds[ONE],dirs[ONE],   lpEstimate  );
                this.nodeDataMap.put( zeroChildID.toString(), zeroChild_payload);
                this.nodeDataMap.put(oneChildID.toString() , oneChild_payload );

            }else {
                
                //actually this section should never be entered for p6b
                //if no hypercubes for p6b, means that this node is feasible so will not be branched
                
                //use cplex default
                getBranches( vars, bounds,  dirs) ;
               
                //create both kids
                NodeId zeroChildID = makeBranch( vars[ZERO],  bounds[ZERO],dirs[ZERO],  lpEstimate   );
                NodeId oneChildID  = makeBranch( vars[ONE],  bounds[ONE],dirs[ONE],   lpEstimate );
                this.nodeDataMap.put( zeroChildID.toString(), null);
                this.nodeDataMap.put(oneChildID.toString() , null );
            }
            
        }
    }
    
    private NodePayload getChildPayload (boolean isZeroChild,String branchingVariable,  NodePayload parent_nodeData,  List<Rectangle> child_hypercubesList) {
        
        NodePayload payload = new NodePayload ();
        
        payload.zeroFixedVars.addAll( parent_nodeData.zeroFixedVars );
        payload.oneFixedVars.addAll( parent_nodeData.oneFixedVars);
        if (isZeroChild) {
            payload.zeroFixedVars.add(  branchingVariable ) ;
        }else {
            payload.oneFixedVars.add( branchingVariable );
        }
        
        payload.hypercubesList=child_hypercubesList;
        
        return payload;
    }
    
    private void getArraysNeededForCplexBranching (String branchingVar ){
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
       
    private void splitHyperCubes(String    branchingVariable,  List<Rectangle> zeroChild_hypercubesList,   
                                 List<Rectangle> oneChild_hypercubesList,  List<Rectangle> parent_hypercubesList  ){
        
        while (parent_hypercubesList.size()>ZERO) {
            Rectangle rect=parent_hypercubesList.remove(ZERO);
            if (rect.zeroFixedVariables.contains(branchingVariable )) {
                zeroChild_hypercubesList.add(rect );
            }else if (rect.oneFixedVariables.contains( branchingVariable)) {
                oneChild_hypercubesList.add(rect);
            }else {
                //add to both sides
                zeroChild_hypercubesList.add(rect );
                oneChild_hypercubesList.add(rect);
            }
        }
         
    }
    
    private String getVarWithLargestObjCoeff(List<String> suggestedBranchingVars){
        String result = null;
        double highestCoeffMagnitude = -Double.MAX_VALUE;
        for (String var : suggestedBranchingVars) {
            double thisMagnitude = TestDriver.objective.getObjectiveCoeffMagnitude(var );
            if (highestCoeffMagnitude< thisMagnitude) {
                result = var;
                highestCoeffMagnitude =thisMagnitude;
            }
        }
        return result;
    }
}
