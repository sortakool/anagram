package com.anagram;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Created by rmanaloto on 8/13/14.
 *
 * Proxy implementation of {@link com.anagram.AnagramServiceMBean} that delegates all work to a JMX-backed service running on different JVM (and/or different server).
 */
public class AnagramServiceJMXProxy implements AnagramServiceMBean {

    private final String jmxURL;
    private final String objectName;
    private final ObjectName mbeanName;
    private final AnagramServiceMBean anagramService;
    private final MBeanServerConnection mBeanServerConnection;
    private final JMXConnector jmxConnector;
    private final JMXServiceURL jmxServiceURL;

    public AnagramServiceJMXProxy(String jmxURL, String objectName) throws MalformedObjectNameException, IOException {
        this.jmxURL = jmxURL;
        this.objectName = objectName;
        Objects.nonNull(this.jmxURL);
        Objects.nonNull(this.objectName);

        this.mbeanName = new ObjectName(objectName);

        this.jmxServiceURL = new JMXServiceURL(jmxURL);
        this.jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);
        this.mBeanServerConnection = jmxConnector.getMBeanServerConnection();

        this.anagramService = JMX.newMBeanProxy(mBeanServerConnection, mbeanName, AnagramServiceMBean.class, true);
        Objects.nonNull(anagramService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addWord(String word) {
        return anagramService.addWord(word);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteWord(String word) {
        return anagramService.deleteWord(word);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAnagrams(String word) {
        return anagramService.getAnagrams(word);
    }

    @Override
    public void close() {

    }
}
