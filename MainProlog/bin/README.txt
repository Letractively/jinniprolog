Jinni uses a fast Java-based  emulator.
Morever, a variant of Jinni runs on .NET
(to run the .NET executable, netjinni.exe, you will need to download
the (free) .NET framework and the (free) J# redistributable from 
http://www.microsoft.com .

After installing the required .NET components or Java 1.1.x or later,
you can run Jinni by typing 

  netjinni.exe (on a .NET platform)

or
  
  jinni.bat (on a Java platform)
  
or

  jinni

Try out a set of benchmarks with nbm.bat (.NET) or bm.bat (JAVA).

What's new in Jinni:

Jinni  is implemented using very fast, reentrant, WAM based engines
and a flexible Reflection based Java interface giving to Prolog
programmers access to zillions of lines of Java and .NET
components.

Jinni  provides a powerful Reflection based Java interface,
supports multiple Prolog databases and an elegant Object 
Oriented Prolog layer. A GUI, multi-threading, blackboards and
networking operations make Jinni a complete
solution for building client-server and 3-tier rule based 
program components.

Still, the the look and feel of basic Prolog and its syntax 
are preserved. Jinni gives a glimpse at how a next 
generation knowledge processing languages can look - despite
its compact design it  has features going far beyond ISO Prolog.
Jinni contains embedded Web server and Web tunelling
for quick deployment of you Prolog solution as Web services.

Jinni sees the Web as if it were a local files system.
It can read files directly from URLs and zipped directories.
It can read and write directly from Java strings launch multiple
threads, handles multiple dynamic databases etc.
 
For more information on Jinni, take a look at the
JinniUserGuide.html our Web based demos, the 
bin/JinniUserGude.html file.

Look in build/build.pl and various batchfiles and project files
for work with the sources. 
You can regenerate everything
by running 

  makejinni
  
This assumes you have Java in your PATH.

On Windows, you might want to use/adapt some of the scripts in directory
OLDscripts.

  jcompile.bat will regenerate the Java classes only

To have Jinni recompile its own Prolog kernel wam.bp
go in psrc and run mjc.bat or jc.bat, then type jboot.

Try out appletgo.bat (for applets) and bm.bat (for benchmarking with jview).

Enjoy,

Paul Tarau
