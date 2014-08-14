package com.anagram;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.Set;

/**
 * Created by rmanaloto on 8/13/14.
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

    @Override
    public boolean add(String word) {
        return anagramService.add(word);
    }

    @Override
    public boolean delete(String word) {
        return anagramService.delete(word);
    }

    @Override
    public Set<String> getAnagrams(String word) {
        return anagramService.getAnagrams(word);
    }
}
