AutomataLib
===========
[![Travis CI](https://travis-ci.org/LearnLib/automatalib.svg?branch=develop)](https://travis-ci.org/LearnLib/automatalib)
[![Coverage Status](https://coveralls.io/repos/github/LearnLib/automatalib/badge.svg?branch=develop)](https://coveralls.io/github/LearnLib/automatalib?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.automatalib/automata-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.automatalib/automata-parent)

AutomataLib is a free, open source ([Apache License, v2.0][1]) Java library for modeling automata, graphs, and transition systems.

About
-----
AutomataLib is developed at the [Dortmund University of Technology, Germany][2]. Its original purpose is to serve as the automaton framework for the [LearnLib][3]    
active automata learning library. However, it is completely independent of LearnLib and can be used for other projects as well.

Please note that the development of AutomataLib is still in a very early stage. Currently, it mainly focuses on Deterministic Finite Automata (DFA) and Mealy machines. Also please note that many parts of the library have not yet been thoroughly tested.

Build Instructions
------------------
Following are build instructions for IntelliJ IDE with JDK 1.8. 
1- Start IntelliJ. Go to File -> New -> Project from existing sources -> select the project folder.
2- Choose "Import Project from external model" -> Maven -> Next.
3- Check "Import Maven projects automatically". Keep the rest as default and click "Next" until the project is imported.
4- Build the porject. If build fails, then it is probably due to two libraries which we could remove. Go to File -> Project Structure -> Libraries, and remove the following two libraries:  serialization-taf and serialization-saf. Now build again and it should work.
6- To produce the jar file, go to File -> Project Structure -> Artifacts -> Add (the plus sign) -> JAR ->  From modules with dependenceis -> OK -> On the right hand side, check box "Include in project build".
7- Now build the project and it should produce a new directory "out" containing the JAR artifact.

Maintainers
-----------
* [Markus Frohme][5] (2017 - )
* [Malte Isberner][4] (2013 - 2015)

Resources
---------
* **Maven Project Site:** [snapshot](http://learnlib.github.io/automatalib/maven-site/latest-snapshot/) | [latest release](http://learnlib.github.io/automatalib/maven-site/latest-release/)
* **API Documentation:** [snapshot](http://learnlib.github.io/automatalib/maven-site/latest-snapshot/apidocs/) | [latest release](http://learnlib.github.io/automatalib/maven-site/latest-release/apidocs/)

[1]: http://www.apache.org/licenses/LICENSE-2.0
[2]: http://www.cs.tu-dortmund.de
[3]: http://www.learnlib.de
[4]: https://github.com/misberner
[5]: https://github.com/mtf90

