package org.dsa.iot.container;

import org.dsa.iot.container.manager.DSLinkManager;
import org.dsa.iot.container.security.SecMan;
import org.dsa.iot.container.stdin.StdinHandler;
import org.dsa.iot.container.stdin.UserHandler;
import org.dsa.iot.shared.SharedObjects;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Samuel Grenier
 */
public class Main {

    public static void main(String[] args) {
        Args parsed = Args.parse(args);
        if (parsed == null) {
            return;
        }

        // Set the security manager
        System.setSecurityManager(new SecMan());

        // Start up global thread pools on main
        SharedObjects.getDaemonThreadPool().prestartAllCoreThreads();
        SharedObjects.getThreadPool().prestartAllCoreThreads();

        DSLinkManager manager = new DSLinkManager();
        String dslinksFolder = parsed.getDslinksFolder();
        if (dslinksFolder != null) {
            String brokerUrl = parsed.getBrokerUrl();
            Path folder = Paths.get(dslinksFolder);
            manager.loadDirectory(folder, brokerUrl);

            UserHandler handler = new UserHandler(manager, folder, brokerUrl);
            handler.start();
        } else {
            StdinHandler handler = new StdinHandler(manager);
            handler.start();
        }
    }
}
