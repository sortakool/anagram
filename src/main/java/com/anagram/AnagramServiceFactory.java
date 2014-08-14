package com.anagram;

import javax.management.MalformedObjectNameException;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by rmanaloto on 8/13/14.
 */
public class AnagramServiceFactory {

    public static final String DEFAULT_ANAGRAM_SERVICE_MBEAN_NAME = "com.anagram:type=AnagramService";
    public static final String DEFAULT_DICTIONARY_FILENAME = "words.txt";
    public static final String ANAGRAM_SERVICE_JMX_OBJECTNAME = "anagram.service.jmx.objectname";

    public enum Mode {
        LOCAL, JMX, TCPIP;
    }

    private String dictionaryFileName = System.getProperty("anagram.service.factory.dictionary.filename", DEFAULT_DICTIONARY_FILENAME);
    private String jmxHost = System.getProperty("anagram.service.jmx.host", "localhost");
    private int jmxPort = Integer.getInteger("anagram.service.jmx.port", 9999);
    private String jmxObjectName = System.getProperty(ANAGRAM_SERVICE_JMX_OBJECTNAME, DEFAULT_ANAGRAM_SERVICE_MBEAN_NAME);

    public AnagramServiceFactory() {
        this(System.getProperty("anagram.service.factory.dictionary.filename", DEFAULT_DICTIONARY_FILENAME));
    }

    public AnagramServiceFactory(String dictionaryFileName) {
        this.dictionaryFileName = dictionaryFileName;
    }

    public String getDictionaryFileName() {
        return dictionaryFileName;
    }

    public void setDictionaryFileName(String dictionaryFileName) {
        this.dictionaryFileName = dictionaryFileName;
    }

    public String getJmxHost() {
        return jmxHost;
    }

    public void setJmxHost(String jmxHost) {
        this.jmxHost = jmxHost;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public String getJmxObjectName() {
        return jmxObjectName;
    }

    public void setJmxObjectName(String jmxObjectName) {
        this.jmxObjectName = jmxObjectName;
    }

    private static String createJMXURL(String host, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append("service:jmx:rmi://").append(host).append("/jndi/rmi://").append(host).append(":").append(port).append("/jmxrmi");
        return sb.toString();
    }

    public AnagramServiceMBean createAnagramService(Mode mode) {
        AnagramServiceMBean anagramService = null;
        switch(mode) {
            case LOCAL:
                try {
                    anagramService = new AnagramService(1, dictionaryFileName);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                break;
            case JMX:
                final String jmxURL = createJMXURL(jmxHost, jmxPort);
                try {
                    anagramService = new AnagramServiceJMXProxy(jmxURL, jmxObjectName);
                } catch (MalformedObjectNameException|IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case TCPIP:

                break;
        }
        return anagramService;
    }

}
