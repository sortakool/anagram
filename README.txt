This application was built and tested on a macbook and Java 8.
A Java 8 JRE will be needed to run the application.

Gradle is the build tool and an embedded Gradle is provided.
An internet connection is needed to download all necessary Gradle files.

Everything is built on pure standard Java and there are not any extra 3rd-party libraries used.

The client and server communication protocol is JMX which uses RMI behind the scenes.

---------------------------------------------------------------------------------------------------------
Installation instructions:
---------------------------------------------------------------------------------------------------------
1.  unzip files into filesystem
2.  Install a Java 8 JRE and set it to be the JAVA_HOME.
2b. Optionally you can skip setting JAVA_HOME and uncomment property 'org.gradle.java.home' in file ./gradle.properties
    and set the JAVA 8 JRE location.
3.  Run embedded Gradle build command (NOTE: requires internet connection):

    (*Nix environment):     ./gradlew clean createAllStartScripts
    (*Windows environment): ./gradle.bat clean createAllStartScripts

---------------------------------------------------------------------------------------------------------
IDE setup instructions:
---------------------------------------------------------------------------------------------------------
1. If using IntelliJ, run embedded Gradle wrapper command:
    (*Nix environment):     ./gradlew idea
    (*Windows environment): ./gradle.bat idea
2. If using Eclipse, run embedded Gradle wrapper command:
    (*Nix environment):     ./gradlew eclipse
    (*Windows environment): ./gradle.bat eclipse

---------------------------------------------------------------------------------------------------------
Run Anagram console application in local mode:
---------------------------------------------------------------------------------------------------------
1. Run generated script:
    (*Nix environment):     ./build/scripts/localClient
    (Windows environment):  ./build/scripts/localClient.bat

NOTES:
By default, the words.txt file attached in the Technical Test.docx file is used as the dictionary file to use.
If a different file needs to be used, it must be set via Java System environment property 'anagram.service.factory.dictionary.filename' which can be overridden via environment variable 'LOCAL_CLIENT_OPTS'.
For example:
export LOCAL_CLIENT_OPTS=-Danagram.service.factory.dictionary.filename=/location/to/dictionarydir/dictionaryfile.txt


---------------------------------------------------------------------------------------------------------
Run Anagram console application in client/server mode:
---------------------------------------------------------------------------------------------------------
1. Start server: Run generated script:
    (*Nix environment):     ./build/scripts/jmxServer
    (Windows environment):  ./build/scripts/jmxServer.bat

NOTES:
By default, the words.txt file attached in the Technical Test.docx file is used as the dictionary file to use.
If a different file needs to be used, it must be set via Java System environment property 'anagram.service.factory.dictionary.filename' which can be overridden via environment variable 'JMX_SERVER_OPTS'.
For example:
export JMX_SERVER_OPTS=-Danagram.service.factory.dictionary.filename=/location/to/dictionarydir/dictionaryfile.txt

Other notable configurable properties (with defaults):
'-Dcom.sun.management.jmxremote.local.only=false'
'-Dcom.sun.management.jmxremote.port=9999'
'-Dcom.sun.management.jmxremote.authenticate=false'
'-Dcom.sun.management.jmxremote.ssl=false'
'-Danagram.service.stripes.count=10'

2. Start client: Run generated script:
    (*Nix environment):     ./build/scripts/jmxClient
    (Windows environment):  ./build/scripts/jmxClient.bat

NOTES:
If the JMX server is on a different server, the JMX hostname and/or port will need to be updated to connect to the JMX MBean on that server.

The 'JMX_CLIENT_OPTS' environment variable must be set with Java environment variables 'anagram.service.jmx.host' and 'anagram.service.jmx.port'

For example:
JMX_CLIENT_OPTS='"-Danagram.service.jmx.host=newhostname" "-Danagram.service.jmx.port=9898"'
