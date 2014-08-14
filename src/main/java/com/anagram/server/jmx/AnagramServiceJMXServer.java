package com.anagram.server.jmx;

import com.anagram.AnagramServiceFactory;
import com.anagram.AnagramServiceMBean;
import com.anagram.util.ConsolePrinter;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by rmanaloto on 8/13/14.
 */
public class AnagramServiceJMXServer {

    public static final String EXIT = "X";

    private final AnagramServiceMBean anagramService;
    private final AnagramServiceFactory anagramServiceFactory;
    private final String mbeanName;
    private final ObjectName objectName;
    private final ObjectInstance registeredMBean;

    public AnagramServiceJMXServer(String mbeanName) throws MalformedObjectNameException, NotCompliantMBeanException,
            InstanceAlreadyExistsException, MBeanRegistrationException {
        this.mbeanName = mbeanName;
        Objects.nonNull(this.mbeanName);
        this.objectName = new ObjectName(this.mbeanName);

        this.anagramServiceFactory = new AnagramServiceFactory();
        this.anagramService = this.anagramServiceFactory.createAnagramService(AnagramServiceFactory.Mode.LOCAL);

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        this.registeredMBean = mBeanServer.registerMBean(anagramService, this.objectName);
        Objects.nonNull(this.registeredMBean);
        System.out.println("Registered mbean " + registeredMBean.getObjectName());
    }

    private void readConsoleInput() {
        Scanner scanner = new Scanner(System.in);
        final StringBuilder sb = new StringBuilder(1024);
        String word;
        Set<String> anagrams;
        while (true) {
            sb.setLength(0);
            sb.append("Enter any command below:\n");
            sb.append("[X]\tExit:");
            ConsolePrinter.println(System.out, sb);
            sb.setLength(0);
            String command = scanner.nextLine();
            switch (command) {
                case EXIT:
                    sb.append("Exiting...");
                    ConsolePrinter.println(System.out, sb);
                    return;
                default:
                    sb.append("Invalid command '").append(command).append("'");
                    ConsolePrinter.println(System.out, sb);
                    break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String mbeanName = System.getProperty(AnagramServiceFactory.ANAGRAM_SERVICE_JMX_OBJECTNAME, AnagramServiceFactory.DEFAULT_ANAGRAM_SERVICE_MBEAN_NAME);
        AnagramServiceJMXServer server = new AnagramServiceJMXServer(mbeanName);
        server.readConsoleInput();
    }
}
