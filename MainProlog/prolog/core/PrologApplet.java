package prolog.core;
import java.applet.*;
import java.awt.*;
import prolog.kernel.*;
import prolog.logic.*;

/**
 * Provides an Applet wrapper for  the Prolog GUI
 */
public class PrologApplet extends Applet implements Stateful {
  public static Applet applet;

  public static String getAppletHome() {
    String appletURL = applet.getCodeBase().toString();
    return JavaIO.pathOf(appletURL);
  }
  /**
    Used to initialise applet
  */
  public void init() {
    JavaIO.isApplet = true;
    PrologApplet.applet = this;
    Interact.USER_PATH = new ObjectQueue(Interact.applet_user_path);
    //Prolog.dump("changed to applet path: "+Main.USER_PATH);
    String root = getParameter("root");
    String command = getParameter("command");
    if (null == root || root.length() == 0) {
      root = Top.ZIPSTORE;
    }
    else {
      Top.ZIPSTORE = root; // usually "prolog.jar", defaults to prolog.zip otherwise
    }
    if (null == command || command.length() == 0) {
      command = "applet_console";
    }

    String[] argv = new String[2];
    argv[0] = root;
    argv[1] = command;
    //Machine M=
    try {
      Top.initProlog(argv);
    }
    catch (Throwable e) {
      JavaIO.errmes("ireecoverable Prolog error", e);
      destroy();
    }
  }

  public void start() {
    Prolog.dump("starting...");
  }

  public void stop() {
    Prolog.dump("stopping...");
  }

  public void destroy() {
    Prolog.dump("destroying...");
    super.destroy();
  }

  public boolean action(Event evt, Object arg) {
    if (evt.target instanceof Runnable) {
      ((Runnable)evt.target).run();
    }
    else {
      JavaIO.warnmes("UNEXPECTED  TARGET: " + evt.target);
      return false;
    }
    return true;
  }

  public boolean handleEvent(Event event) {
    if (event.id == Event.WINDOW_DESTROY) {
      Component Cs[] = getComponents();
      for (int i = 0; i < Cs.length; i++) {
        Component C = Cs[i];
        GuiBuiltins.stopComponent(C);
      }
      removeAll();
      destroy();
    }
    return super.handleEvent(event);
  }
}
