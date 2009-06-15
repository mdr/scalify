
Release notes updated Jun 15 2009: 

* I stopped working on this in the days of scala 2.71 to devote my energies
  to the scala compiler, and it took some tweaking to get working even back then.
* I've just brought it up to date to work with scala 2.7.5 but it will still require
  some fiddling.
* The code is horrible.  This was my learning scala project.

* If eclipse can't compile the java source you give it, the translation will not work - at all.  You must satisfy eclipse.

Additional software you will need:

* Scala, of course - version 2.7.5.
		http://www.scala-lang.org/downloads/
* Eclipse - a typical installation should work fine.  A stripped down one may not.
		http://www.eclipse.org/downloads/
* Scala OSGI bundles in the eclipse plugins dir, and possibly some other bundles that
  don't come with eclipse as well.  Look in lib/ for the ones I use.
		http://www.scala-lang.org/node/94
* Rake, if you want to use my sad build setup
		http://rake.rubyforge.org/
		
Recommended:

* rlwrap to make using the OSGI console tolerable
		http://utopia.knoware.nl/~hlub/rlwrap/

Scalify utilizes eclipse plugins but you don't have to run eclipse to use it.

*** BUILDING ***

% rake

*** RUNNING ***

% bin/scalify
osgi> scalify /tmp/some/src -o /tmp/output

*** "DID IT WORK?" / DEMONSTRATION ***

% bin/scalify
"Scalify service started"

osgi> scademo
< blah blah hey it must be working ... all done >
osgi> exit

% cd demo/oroScala
% rake
// Note Jun 15 2009 - this used to compile and run far enough to print "Usage" but no longer.
% scala examples.grep
Usage: grep <pattern> <filename> ...

*** BUGS ***

Many. 

I can be emailed at paulp@improving.org but help making this work is unlikely.
