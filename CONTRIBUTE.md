# Contributing to Debezium Platform

The Debezium community welcomes anyone that wants to help out in any way, whether that includes reporting problems, helping with documentation, or contributing code changes to fix bugs, add tests, or implement new features. 
This document outlines the basic steps required to work with and contribute to the Debezium Platform codebase.

## Talk to us

Before doing any code changes,
it's a good idea to reach out to us,
so as to make sure there's a general consencus on the proposed change and the implementation strategy.
You can reach us here:

* [User chat](https://debezium.zulipchat.com/#narrow/stream/302529-users)
* [Developers chat](https://debezium.zulipchat.com/#narrow/stream/302533-dev) - Only for internal development subjects
* [Google Group](https://groups.google.com/forum/#!forum/debezium)
* [GitHub Issues](https://github.com/debezium/dbz/issues)

## Install the tools

The following software is required to work with the Debezium codebase and build it locally:

* [Git 2.2.1](https://git-scm.com) or later
* JDK 21 or later, e.g. [OpenJDK](http://openjdk.java.net/projects/jdk/)
* [Apache Maven](https://maven.apache.org/index.html) 3.9.8
* [Docker Engine](https://docs.docker.com/engine/install/) or [Docker Desktop](https://docs.docker.com/desktop/) 1.9 or later

See the links above for installation instructions on your platform. You can verify the versions are installed and running:

    $ git --version
    $ javac -version
    $ mvn -version
    $ docker --version

### GitHub account

Debezium uses [GitHub](GitHub.com) for its primary code repository and for pull-requests, so if you don't already have a GitHub account you'll need to [join](https://github.com/join).

Debezium uses the [GitHub Issues](https://github.com/debezium/dbz/issues) instance for issue tracking.
