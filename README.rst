joblauncher
===========

Web service to submit jobs via a Octopus supported scheduler.

Submit job to web service using a HTTP POST request.
Reports status of job to submitter using a callback url.

Requirements
------------

- JDK 7 (http://www.java.com)
- Maven 3 (http://maven.apache.org)
- Octopus (https://github.com/NLeSC/octopus)

Install
-------

1. Build Octopus

.. code-block:: bash

   git clone https://github.com/NLeSC/octopus.git
   cd octopus
   ant build -Dversion=1.0

2. Add Octopus jar to local maven repository, so it can be used as maven dependencies

.. code-block:: bash

   mvn install:install-file -Dfile=dist/octopus-1.0.jar -DartifactId=octopus -Dversion=1.0 -DgroupId=nl.esciencecenter.octopus -Dpackaging=jar -DgeneratePom=true

3. Make copy of 'joblauncher.yml-dist' to 'joblauncher.yml'
3.1 Configure Octopus scheduler
3.2 Configure Octopus sandbox root directory
3.3 Configure Octopus adaptor directory to dist/ subdirectory of octopus clone.
3.4 Configure optional MAC id/key
4. Build uber-jar or execute from maven.
4.1. Uber-jar, to start on other machine the `joblauncher-2.0.jar`, `joblauncher.yml` and `dist/octpus-adaptor-*.jar` files must be copied.

.. code-block:: bash

   mvn package
   java -jar target/joblauncher-2.0.jar server joblauncher.yml

4.2 Execute from maven

.. code-block:: bash

   mvn compile exec:java

Usage
-----

Web service listens on http://localhost:9998 .

Create a directory with an input file and script

.. code-block:: bash

   mkdir myjob
   cd myjob
   echo 'Lorem ipsum' > input_file
   echo 'hostname;date;wc -l input_file > output_file' > runme.sh

Create a json file (query.json)

.. code-block:: json

   {
      "jobdir": "<absolute path to myjob directory>/",
      "executable": "/bin/sh",
      "prestaged": ["runme.sh", "input_file"],
      "poststaged": ["output_file"],
      "stderr": "stderr.txt",
      "stdout": "stdout.txt",
      "arguments": ["runme.sh"],
      "status_callback_url": "http://localhost/job/myjob/status"
   }

Then submit it

.. code-block:: bash

   curl -H "Content-Type: application/json" -H 'Accept: application/json' -X POST -d @query.json http://localhost:9998/job

After a while `output_file`, `stderr.txt` and `stdout.txt` file appear in `myjob` directory.
"http://localhost/job/myjob/status" will have several PUT HTTP requests send to it.
The PUT requestes contain job statuses like PRE_STAGING, RUNNING, POST_STAGING, STOPPED.

Callback authentication
^^^^^^^^^^^^^^^^^^^^^^^

The status callbacks uses MAC Access Authentication.
The MAC key indentifier and MAC key must be obtained from the provider.

Documentation
-------------

A maven site can be generated with

.. code-block:: bash

   mvn site
   firefox target/site/index.html
