
Early release notes:

* I'm the only one who has ever used this so there could be unintentional dependencies.
* I only use OS X so if it works on other platforms at this point that's a lucky accident.
* It lacks serious polish but that might be for the best since it doesn't exactly work yet.
* I learned scala while I wrote this and there's plenty of evidence of it.  I'm sure there
    are far superior ways to do any number of things - I'm all ears!
* I haven't bothered translating comments over yet, coming soon.
* Generics are not yet supported, though they definitely will be.  I'd like to get 1.4 translations working and bulletproof before I resume work on java 5.
* If eclipse can't compile the java source you give it, the translation will not work - at all.  You must satisfy eclipse.
* I've had the translated source looking far more attractive in the past than it does now, but after running into a few too many weird cases involving punctuation I decided aesthetics were a premature optimization.  It'll be beautiful in the end.

Additional software you will need:

* Scala, of course - I doubt it will run on 2.7.1, use a 2.7.2 candidate
		http://www.scala-lang.org/downloads/
* Eclipse - a typical ganymede installation should work fine.  A stripped down one may not.
		http://www.eclipse.org/downloads/
* Scala eclipse plugin (install from within eclipse)
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

*** TESTING ***

% bin/scalify
"Scalify service started"

osgi> scademo
< blah blah it must be working ... all done >
osgi> exit

% cd demo/oroScala
% rake
% scala examples.grep
Usage: grep <pattern> <filename> ...

[Seeing "Usage" is as much of a thrill as you're likely to receive, because at this moment all the examples I've tried go into infinite loops, although some of those have worked in the past.]

*** BUGS ***

Many.  I will change the content of this readme when I'm more interested in bug reports.  More helpful right now would be:

* build system improvements
* identification of massive fails wrt the best way to do something in scala
* well constructed test cases
* pointers to open source java projects that don't use features from java 5 and beyond

Email me at paulp@improving.org.
