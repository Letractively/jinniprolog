package prolog.core;
import java.io.*;
import java.net.*;
import prolog.logic.Stateful;
import prolog.kernel.JavaIO;
import prolog.kernel.PrologReader;
import prolog.kernel.PrologWriter;

/**
Lightweight Prolog shell client (that could be made self-contained
if needed) that works like a remote shell allowing users to control
and possible share a Prolog "server".
 */
public class ShellClient implements Stateful {
  public ShellClient(String host, int port) throws Exception {
    Socket socket = new Socket(host, port);
    init_connection(socket);
  }

  private LineNumberReader R;
  private PrintWriter W;


  public LineNumberReader toReader(InputStream inputStream) {
    //return new LineNumberReader(new InputStreamReader(inputStream));
    return JavaIO.toReader(inputStream);
  }

  public static PrintWriter toWriter(OutputStream f) {
    //return new PrintWriter(f,true);
    return JavaIO.toWriter(f);
  }

  public void init_connection(Socket socket) throws Exception {
    R = JavaIO.toReader(socket.getInputStream());
    W = JavaIO.toWriter(socket.getOutputStream());
  }

  public void run() {
    ShellPrinter O = new ShellPrinter(R, ShellClient.toWriter(System.out));
    (new Thread(O,"ShellThread")).start();
    //System.err.println("shell thread started!!!");
    ShellPrinter I = new ShellPrinter(toReader(System.in), W);
    I.run();
  }
}

class ShellPrinter implements Runnable {
  ShellPrinter(LineNumberReader from, PrintWriter to) {
    this.from = from;
    this.to = to;
  }

  private LineNumberReader from;
  private PrintWriter to;

  public void stop() {
    try {
      from.close();
      to.close();
    }
    catch (Exception e) {
    }
  }

  public void run() {
    while (true) {
      int c;
      try {
        c = from.read();
      }
      catch (Exception e) {
        c = -1;
      }
      if (-1 == c) break;
      to.write((char)c); to.flush();
    }
    stop();
  }
}
