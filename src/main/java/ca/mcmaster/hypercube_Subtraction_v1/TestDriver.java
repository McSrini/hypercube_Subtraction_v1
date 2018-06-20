/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_Subtraction_v1;

import ca.mcmaster.hypercube_Subtraction_v1.common.Objective;
import ca.mcmaster.hypercube_Subtraction_v1.common.LowerBoundConstraint;
import static ca.mcmaster.hypercube_Subtraction_v1.Constants.*;
import static ca.mcmaster.hypercube_Subtraction_v1.Parameters.*;  
import ca.mcmaster.hypercube_Subtraction_v1.cplexRef.CplexRefTree;
import ca.mcmaster.hypercube_Subtraction_v1.utils.MIP_Reader;
import ilog.cplex.IloCplex;
import static java.lang.System.exit; 
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 * 
 * June 8 , 2018
 * 
 * main driver class for hypercube subtraction
 * 
 */
public class TestDriver {
        
    private static Logger logger=Logger.getLogger(TestDriver.class);
    
    //constraints in this mip
    public  static List<LowerBoundConstraint> mipConstraintList ;
    public static  Objective objective ;
    public static  List<String> allVariablesInModel ;
    
    static {
        logger.setLevel(Level.OFF);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            logger.addAppender(new  RollingFileAppender(layout,LOG_FOLDER+TestDriver.class.getSimpleName()+ LOG_FILE_EXTENSION));
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    }
    
    public static void main(String[] args) throws Exception {
         
        try {
            IloCplex mip =  new IloCplex();
            mip.importModel(MIP_FILENAME);
            mip.exportModel(MIP_FILENAME+ ".lp");
           
            allVariablesInModel = MIP_Reader.getVariables(mip) ;
            objective= MIP_Reader.getObjective(mip);            
            mipConstraintList= MIP_Reader.getConstraints(mip);

            logger.debug ("Collected objective and constraints" ) ;

            CplexRefTree cplexRefTree = new CplexRefTree () ;
            cplexRefTree.solve();
             
        }catch (Exception ex){
            System.err.println(ex) ;
            System.err.println(ex.getMessage()) ;
            ex.printStackTrace();
            exit(ONE);
        }
        
    }//end main method
    
}//end class
