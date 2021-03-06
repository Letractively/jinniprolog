package prolog.core;
import prolog.kernel.*;
import prolog.logic.*;
/**
   BinProlog to Java interface adaptor
*/
public class BPAdaptor implements Stateful {
  public static String[] args=null;
  
  public static boolean initArgs(String[] myargs) {
    boolean notYetDone=(null==args);
    args=myargs;
    return notYetDone;
  }

  public static Machine initMachine(String jarName) {
    if(null==args) args=new String[1];
    args[0]=jarName;
    return Top.initProlog(args);
  }

  public static Machine M=null;

  public static String callj(String query) {
    if(null==args) M=initMachine("/bin/prolog.jar");
  
    //System.out.println("callj CALLED with:"+query);
    //return "the("+query+")";
    
    
    if(null==M) Top.new_machine(); // restarts if machine damaged
    
    String answer=null;
    try {
      M.load_engine(query);
      int v=M.ask();
      if(0!=v) {
        answer=M.canonicalTermToString(v); //canonical term
      }
    }
    catch (Exception e) {
      JavaIO.errmes("error in machine.run()",e);
      answer="exception('"+e+"')";
      M=null;
    }
    if(null==answer) answer="no";
    else answer="the("+answer+")";
    return answer;
  }
}