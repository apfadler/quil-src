# quil-src
This is the source code for QUIL: a proof of concept for a distributed pricing architecture based on Apache Ignite and QuantLib. Alongside QuantLib it also includes OpenGamma's Strata Library.

For more info see http://quantlib.org/slides/qlum15/pfadler.pdf.

License: Apache 2.0

(c) 2016 Andreas Pfadler 

# Quickstart using Docker

Follow the instructions at https://github.com/apfadler/quil-docker

# Build Instructions

**Prerequisites:**


1.) Java 8 SDK

2.) Maven

3.) npm

4.) A QuanlibJNI.so/.dll compiled for Quantlib 1.7


**Building QUIL:**


0.)  Start shell (Linux bash or Cygwin/git bash)

1.)  git clone https://github.com/apfadler/quil-src.git

2.)  cd quil-src

3.)  mvn clean install


# Running QUIL

**Single node (standalone mode)**

Starting one ignite server node with QUIL webapp and rest interface running on localhost:8081/frontend

1.) cd dist/

2.) bin/quil-server-standalone.sh

3.) Goto localhost:8081/frontend for web interface

4.) Try bin/quil-run-examples.sh to upload example data and price basic example trades (uses curl to upload data and start pricing)


**Quil Grid with one master node and several worker nodes**

Master Node:

1.) cd dist/

2.) bin/quil-server.sh 

This will start a quil master node.  It provides the webapp and rest interface, but does not execute any tasks. Instead, it will schedule all tasks to be executed on one of the available worker nodes.


Worker Nodes:

1.) cd dist/

2.) bin/quil-worker.sh 

**Quil Scala Console**


1.) bin/quil-console.sh

2.) Wait until the scala prompt appears. Now you are able to run scala scripts using quil/quantlib/ignite APIs. Useful for experimentation and testing...

**Notes**

Environment Variables: 

1.) QUIL_HOME: Should contain the path to the root of the QUIL distribution (i.e. what's in the dist/ directory)

2.) QUIL_WORKER, QUIL_SERVER_STANDALONE: These variables control whether we want to start a master or worker node.

3.) QUIL_SERVER, QUIL_PORT: Only relevant for the quil-client.sh shell script 

4.) IGNITE_H2_DEBUG_CONSOLE: If set to "true" (without quotes) this will open up a H2 web interface in your browser. Useful for looking at the various caches and running SQL queries.

5.) QUIL_WARPATH: Path to .war file for QUIL web app or path to unpacked .war file (for development purposes)

Windows:

There are some .bat files in bin/ that may be used start QUIL master and worker nodes. There is, however, no equivalent for the quil-client.sh script.
