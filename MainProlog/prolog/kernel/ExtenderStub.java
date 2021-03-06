package prolog.kernel;
import prolog.logic.*;
import java.io.InputStream;
/**
Class that switches off some of the engine extensions which break
automatic conversion to C# for alternative .NET implementation.
Note that the Prolog engine runs at full functionality as a J# application under .NET.
 */
  public class ExtenderStub {
    ExtenderStub(Machine M) {
      this.M=M;
    }
    
    private Machine M;
  
    static PrologReader toTokenReader(String fname) throws ExistenceException {
      return null;
    }
  
    static PrologReader string2TokenReader(String cs) throws ExistenceException {
      return null;
    }
  
    static boolean activateBytecode(CodeStore codeStore) throws Exception {
      return false; 
    }
  
    static void turnOff() {
    }
  
    static InputStream zip2stream(String jarname,String fname,boolean quiet) {
      return null;
    }

    /* reflection related */
    
    final public Object getTerm(int xref) throws PrologException {
      JavaIO.errmes("reflection disabled: getTerm="+M.dumpCell(xref));
      return null;
    }
    
    public final int new_java_class(int xref) {
      JavaIO.errmes("reflection disabled: new_java_class="+M.dumpCell(xref));
      return 0;
    }
    
    public final int new_java_object(int xref,int xargs) {
      JavaIO.errmes("reflection disabled: new_java_object="+M.dumpCell(xref));
      return 0;
    }
    
    public final int invoke_java_method(int classref,int xref,int xmeth,int xargs) {
      JavaIO.errmes("reflection disabled: invoke_java_method="+M.dumpCell(xmeth));
      return 0;
    }
    
    public final boolean delete_java_class(int xref) {
      JavaIO.errmes("reflection disabled: delete_java_class="+M.dumpCell(xref));
      return false;
    }
    
    public final boolean delete_java_object(int xref) {
      JavaIO.errmes("reflection disabled: delete_java_object="+M.dumpCell(xref));
      return false;
    }
    
    public final int get_java_field_handle(int xref,int xfield) {   
        JavaIO.errmes("reflection disabled: invoke_java_method="+M.dumpCell(xfield));
        return 0;
    }
    
    public static PrologReader stdIn() {
      try {
        return JavaIO.toReader("stdin.txt");
      }
      catch(Exception e) {
        return null;
      }
    }   
    
    public static PrologWriter stdOut()  {
      return JavaIO.toWriter("stdout.txt");
    }   
    
    public static String getAppletHome() {
      return "";
    } 
    
    public static boolean toFile(String f,Object O) {
      return false;
    }
    
    static public Object fromFile(String f) {
      return null;
    }
  }