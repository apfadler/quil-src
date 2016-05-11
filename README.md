# quil-src
This is the source code for QUIL: a proof of concept for a distributed pricing architecture based on Apache Ignite and QuantLib.

For more info see http://quantlib.org/slides/qlum15/pfadler.pdf.


# Build Instructions

**Prerequisites:**



1.) Java 7 SDK

2.) Maven

3.) npm

4.) A QuanlibJNI.so/.dll compiled for Quantlib 1.7



**Building QUIL:**


0.)  Start shell (Linux bash or Cygwin/git bash)

1.)  git clone https://github.com/apfadler/quil-src.git

2.)  cd quil-src

3.)  mvn clean install


# Running QUIL

**Single node**

Starting one ignite server node with QUIL webapp and rest interface running on localhost:8081/frontend

1.) cd dist/
2.) bin/quil-server-standalone.sh
3.) Goto localhost:8081/frontend for web interface
4.) Try bin/quil-run-examples.sh to upload example data and price basic example trades

Starting quil-server.sh will only start an ignite client node which will try to connect to an ignite grid and then serve the webapp and rest api.

**Starting a QUIL grid**

Additional worker nodes can be started using bin/quil-worker.sh.  This will start another ignite nodes with all neccessary QUIL libs in the classpath. Grid discovery will probably work only with nodes running on the same machine. There are Docker images available that are configured for running a grid on AWS using S3 based grid discovery.


