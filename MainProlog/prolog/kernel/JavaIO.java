package prolog.kernel;
import prolog.logic.*;
/*
* Copyright (C) Paul Tarau 1996-2012
*/

import java.io.*;
import java.net.*;
import java.applet.*;

/**
 * Main IO class, providing operations on file,url,string based streams.
 * Called though Reflection from Prolog.
 */
public class JavaIO extends Interact {

  //public static String defaultCharEncoding="Cp1252"; // Windows Latin

  public static boolean showOutput=true;

  public static int showTrace=0; // 1 shows it

  // begin stdin/stdout ******************

  private static PrologReader input=Extender.stdIn();
  private static PrologWriter output=Extender.stdOut();

  public static PrologReader getStdInput() {
    return input;
  }

  public static void setStdInput(InputStream f) {
    input=toReader(f);
  }

  public static PrologWriter getStdOutput() {
    return output;
  }

  public static void setStdOutput(OutputStream f) {
    output=toWriter(f);
  }

  // end stdin/sdout ********************

  public static PrologReader toReader(InputStream f) {
    return new PrologReader(f);
  }

  public static PrologReader toReader(String fname) throws ExistenceException {
    return new PrologReader(url_or_file(fname));
  }

  public static PrologClauseStringReader toClauseReader(String fname) throws ExistenceException {
    return new PrologClauseStringReader(url_or_file(fname));
  }

  public static PrologReader string2PrologReader(String cs) throws ExistenceException {
    return new PrologReader(string2reader(cs));
  }

  public static PrologClauseStringReader
       string2PrologClauseStringReader(String cs) 
        throws ExistenceException {
    Reader stream=string2reader(cs);
    return new PrologClauseStringReader(stream);
  }

  public static final Reader string2reader(String s) {
    return new StringReader(s);
  }

  public static String reader2string(Reader R) {
    StringBuffer buf=new StringBuffer();
    try {
      int c;
      while (PrologReader.EOF!=(c=R.read())) {
        buf.append((char)c);
      }
    }
    catch (EOFException e) {
      // ok
    }
    catch (IOException e) {
      errmes("error in reader2string",e);
      return null;
    }
    return buf.toString();
  }

  public static String url2string(String url) throws ExistenceException {
    return reader2string(toReader(url));
  }

  public static PrologWriter toWriter(OutputStream f) {
    return new PrologWriter(f);
  }

  public static PrologWriter toWriter(String s) {
    PrologWriter f=null;
    //mes("HERE"+s);
    try {
      f=toWriter(new FileOutputStream(s));
    }
    catch (IOException e) {
      warnmes("write error, to: "+s);
    }
    return f;
  }

  synchronized static public final void print(PrologWriter f,String s) {
    f.print(s);
  }

  public static final void println(PrologWriter o,String s) {
    o.println(s);
  }

  static final String readln(PrologReader f) {
    return f.readln();
  }


  // I/O for programs without a Machine

  public static final String readln() {
    return readln(getStdInput());
  }

  public static final void print(String s) {
    print(getStdOutput(),s);
  }

  public static final void println(String s) {
    println(getStdOutput(),s);
  }


  public static final void traceln(String s) {
    if (showTrace>=1) println(s);
  }

  public static final void dump(String s) {
    println(">>>: "+s);
  }



  public static final void assertion(String Mes) {
    errmes("assertion failed",(new Exception(Mes)));
  }

  public static final int system(String cmd) {
    try {
      Runtime.getRuntime().exec(cmd);
    }
    catch (IOException e) {
      errmes("error in system cmd: "+cmd,e);
      return 0;
    }
    return 1;
  }

  /**
   * Opens a stream based on a URL or file or a zipped component in <ZIPSTORE>.zip
   */
  public static final InputStream url_or_file(String s) throws ExistenceException {
    InputStream stream=null;
    String dir;

    if (s.startsWith("http:/")||
       s.startsWith(":",1)||
       s.startsWith("file:/")||
       s.startsWith("ftp:/")
    ) {
      dir="";
    }
    else {
      dir=getPrologHome();
    }

    //System.err.println("dir=<"+dir+"> s="+s);

    if (null==stream&&!isApplet) {
      try {
        File F=new File(s);
        if (F.exists())
          stream=new FileInputStream(F);
      }
      catch (Exception e) { }
    }

    //if(null!=stream) return stream;
    //dump(s+"$=>"+stream);

    if (null==stream&&!isApplet) {
      try {
        File F=new File(dir+s);
        if (F.exists()) {
          stream=new FileInputStream(F);
          //System.err.println("dir=<"+dir+"> s="+s);
        }
      }
      catch (Exception e) { }
    }

    if (null==stream&&!isApplet)
      stream=Extender.zip2stream(Top.ZIPSTORE,s,true); //ends with *.zip or *.jar       
    //if(null==stream && null==applet)
    //    stream=Extender.zip2stream(zip_or_jar(Top.ZIPSTORE),s,true); //ends with *.zip or *.jar  

    if (null==stream) {
      String jarURL="jar:"+Top.ZIPSTORE+"!/"+s;
      // looks like "jar:file:/home/tarau/prolog.jar!/"

      stream=url2stream(jarURL,true);

      //System.err.println("TRYING jarURL===>"+jarURL+"===>"+stream);
    }

    /*
    if(null==stream && null!=applet) {
      try {
        stream=applet.getClass().getResourceAsStream(s);
      }
      catch(Throwable e) {}
    }
    */

    if (null==stream) {
      stream=url2stream(dir+s,true);
      //System.err.println("dir=<"+dir+"> s="+s);
    }

    //System.err.println("ZIPSTORE="+Main.ZIPSTORE+" dir=<"+dir+"> s="+s+"=>"+stream);

    if (null==stream) throw new ExistenceException("error opening for read: "+s);

    traceln("url_or_file found: <"+dir+">"+s);

    return stream;
  }

  /*
  private static String zip_or_jar(String s) {
    if(s.endsWith(".zip"))
      s=s.substring(0,s.length()-4)+".jar";
    else if(s.endsWith(".jar")) 
      s=s.substring(0,s.length()-4)+".zip";
    else
      s=null;
    return s;
  }
  */

  /*
   * opens a stream for reading from a URL
  *
  private static final InputStream url2stream(String f) {
    return (url2stream(f,false);
  }
  */

  private static final InputStream url2stream(String f,boolean quiet) {
    //System.err.println("trying URL: "+f);
    InputStream stream=null;
    try {
      URL url=new URL(f);
      stream=url.openStream();
    }
    catch (MalformedURLException e) {
      if (quiet) return null;
      errmes("bad URL: "+f,e);
    }
    catch (Exception e) {
      if (quiet) return null;
      errmes("unable to read URL: "+f,e);
    }

    return stream;
  }

  public static String pathOf(String pf) {
    int split=Math.max(pf.lastIndexOf('/'),pf.lastIndexOf('\\'));
    return (split>0)?pf.substring(0,split+1):"";
  }

  public static String fileOf(String pf) {
    int split=Math.max(pf.lastIndexOf('/'),pf.lastIndexOf('\\'));
    return (split>0)?pf.substring(split+1):pf;
  }

  public static void setPrologHome(String prologHome) {
    if (!isApplet) {
      Top.JINNI_HOME=prologHome;
    }
    else {
      warnmes("cannot override home path for applets");
    }
  }

  public static String getPrologHome() {
    String prologHome=null;
    if (!isApplet) {
      prologHome=Top.JINNI_HOME;
    }
    else {
      prologHome=Extender.getAppletHome();
    }
    //Prolog.dump("getHome()====>"+prologHome+NL);
    return prologHome;
  }

  public static final String runCommand(String cmd) throws SystemException {
    try {
      Process P=Runtime.getRuntime().exec(cmd);
      return runProcess(P);
    }
    catch (Exception e) {
      throw new SystemException("error in OS call: "+e.getMessage()+cmd);
    }
  }

  public static final String runProcess(Process P) throws Exception {
    StringBuffer buf=new StringBuffer();
    PrologReader in=toReader(P.getInputStream());
    PrologReader err=toReader(P.getErrorStream());
    String s;
    while ((s=in.readln())!=null) {
      buf.append(s+NL);
    }
    while ((s=err.readln())!=null) {
      buf.append(s+NL);
    }
    in.close();
    err.close();
    //P.destroy();
    P.waitFor();

    return buf.toString();
  }
}

/*
class VirtualStdOut {
  public static PrologWriter stdout=new PrologWriter(System.out);
}

class VirtualStdIn {
  public static PrologReader stdin=new PrologReader(System.in);
}


class VirtualStdOut extends PrintStream {

  public static PrologWriter stdout=new PrologWriter(new VirtualStdOut());
  //public static PrologWriter stdout=new PrologWriter(System.out);

  VirtualStdOut() {
    super(new VirtualStdOutAdaptor(),true);
  }
}

class VirtualStdOutAdaptor extends OutputStream {

  VirtualStdOutAdaptor() {
  }
  
  public void write(int b) {
    System.out.write(b);//System.out.flush();
  }
  
  public void flush() {
    System.out.flush();
  }
}

class VirtualStdIn extends InputStream {

  public static PrologReader stdin=new PrologReader(new VirtualStdIn());
  //public static PrologReader stdin=new PrologReader(System.in);

  VirtualStdIn() {
  }
  
  public int read() throws IOException {
    return System.in.read();
   }
    
   public int read(byte[] bs, int off, int len) throws IOException {
     return System.in.read(bs); 
   }
}

*/
