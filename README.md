# quil-src
This is the source code for QUIL: a proof of concept for a distributed pricing architecture based on Apache Ignite and QuantLib.

For more info see http://quantlib.org/slides/qlum15/pfadler.pdf.

# Quickstart using Docker

Follow the instructions at https://github.com/apfadler/quil-docker

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


You can start as many worker nodes as you like using bin/quil-worker.sh. Automatic grid discovery using TCP Multicast should work out of the box. If you run into problems please check the Apache Ignite docs and adjust conf/quil-common.xml accordingly.  There are also Docker images available that are configured for running a grid on AWS using S3 based grid discovery.


