# AutomataLib

[![Travis CI](https://travis-ci.org/LearnLib/automatalib.svg?branch=develop)](https://travis-ci.org/LearnLib/automatalib)
[![Coverage Status](https://coveralls.io/repos/github/LearnLib/automatalib/badge.svg?branch=develop)](https://coveralls.io/github/LearnLib/automatalib?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.automatalib/automata-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.automatalib/automata-parent)

AutomataLib is a free, open source ([Apache License, v2.0][1]) Java library for modeling automata, graphs, and transition systems.


## About

AutomataLib is developed at the [Dortmund University of Technology, Germany][2].
Its original purpose is to serve as the automaton framework for the [LearnLib][3] active automata learning library.
However, it is completely independent of LearnLib and can be used for other projects as well.

Please note that the development of AutomataLib is still in a very early stage.
Currently, it mainly focuses on Deterministic Finite Automata (DFA) and Mealy machines.
Also please note that many parts of the library have not yet been thoroughly tested.

## Build Instructions

For simply using AutomataLib, you may use the Maven artifacts which are available in the [Maven Central repository][maven-central].
It is also possible to download a bundled [distribution artifact][maven-central-distr], if you want to use AutomataLib without Maven support.
Note, that AutomataLib requires Java 8.

#### Building development versions

If you intend to use development versions of AutomataLib, simply clone the development branch of the repository

```
git clone -b develop --single-branch https://github.com/LearnLib/automatalib.git
```

and run a single `mvn clean install`.
This will build all the required maven artifacts and will install them in your local Maven repository, so that you can reference them in other projects.

If you plan to use a development version of AutomataLib in an environment where no Maven support is available, simply run `mvn clean package -Pbundles`.
The respective JARs are then available under `distribution/target/bundles`.

#### Developing AutomataLib

For developing the code base of AutomataLib, it is suggested to use one of the major Java IDEs, which come with out-of-the-box Maven support.

* For [IntelliJ IDEA][intellij]:
  1. Select `File` -> `New` -> `Project from existing sources` and select the folder containing the development checkout.
  1. Choose "Import Project from external model", select "Maven" and click `Next`.
  1. Configure the project to your liking, but make sure to check "Import Maven projects automatically" and have "Generated sources folders" set to "Detect automatically".
  1. Click `Next` until the project is imported (no Maven profile needs to be selected).

* For [Eclipse][eclipse]:
  1. **Note**: AutomataLib uses annotation processing on several occasions throughout the build process.
  This is usually handled correctly by Maven, however, for Eclipse you need to install the [m2e-apt-plugin](https://marketplace.eclipse.org/content/m2e-apt) and activate annotation processing afterwards (see the [LearnLib issue #32](https://github.com/LearnLib/learnlib/issues/32)).
  1. Select `File` -> `Import...` and select "Existing Maven Projects".
  1. Select the folder containing the development checkout as the root directory and click `Finish`.


## Documentation

* **Maven Project Site:** [snapshot](http://learnlib.github.io/automatalib/maven-site/latest-snapshot/) | [latest release](http://learnlib.github.io/automatalib/maven-site/latest-release/)
* **API Documentation:** [snapshot](http://learnlib.github.io/automatalib/maven-site/latest-snapshot/apidocs/) | [latest release](http://learnlib.github.io/automatalib/maven-site/latest-release/apidocs/)


## Mailing Lists

  * [Q&A @ Google Groups][automatalib-qa] -- General questions regarding the usage of Automatalib.
  * [Discussion @ Google Groups][automatalib-discussion] -- Discussions about the internals of AutomataLib.
  * [Devel (private) @ Google Groups][automatalib-devel] -- Discussions about future development plans.


## Maintainers

* [Markus Frohme][5] (2017 - )
* [Malte Isberner][4] (2013 - 2015)

[1]: http://www.apache.org/licenses/LICENSE-2.0
[2]: http://www.cs.tu-dortmund.de
[3]: http://www.learnlib.de
[4]: https://github.com/misberner
[5]: https://github.com/mtf90

[automatalib-qa]: https://groups.google.com/d/forum/automatalib-qa
[automatalib-discussion]: https://groups.google.com/d/forum/automatalib-discussion
[automatalib-devel]: https://groups.google.com/d/forum/automatalib-devel

[maven-central]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.automatalib%22
[maven-central-distr]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.automatalib.distribution%22
[intellij]: https://www.jetbrains.com/idea/
[eclipse]: https://www.eclipse.org/
