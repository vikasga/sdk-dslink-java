package org.dsa.iot.dslink;

import org.dsa.iot.dslink.connection.ConnectionManager;
import org.dsa.iot.dslink.connection.DataHandler;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.serializer.SerializationManager;
import org.dsa.iot.dslink.util.Objects;
import org.vertx.java.core.Handler;

import java.io.File;

import static org.dsa.iot.dslink.connection.ConnectionManager.ClientConnected;

/**
 * Provides DSLinks as soon as a client connects to the server or vice versa.
 * @author Samuel Grenier
 */
public class DSLinkProvider {

    private final ConnectionManager manager;
    private final DSLinkHandler handler;
    private boolean running;

    public DSLinkProvider(ConnectionManager manager, DSLinkHandler handler) {
        if (manager == null)
            throw new NullPointerException("manager");
        else if (handler == null)
            throw new NullPointerException("handler");
        this.manager = manager;
        this.handler = handler;
        handler.preInit();
    }

    public void start() {
        running = true;

        manager.setPreInitHandler(new Handler<ClientConnected>() {
            @Override
            public void handle(final ClientConnected event) {
                final DataHandler h = event.getHandler();
                Objects.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (event.isRequester()) {
                            DSLink link = new DSLink(handler, h, true, true);
                            link.setDefaultDataHandlers(true, false);
                            handler.onRequesterInitialized(link);
                        }
                    }
                });

                Objects.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (event.isResponder()) {
                            DSLink link = new DSLink(handler, h, false, true);

                            File path = handler.getConfig().getSerializationPath();
                            if (path != null) {
                                NodeManager man = link.getNodeManager();
                                SerializationManager manager = new SerializationManager(path, man);
                                manager.deserialize();
                                manager.start();
                            }

                            link.setDefaultDataHandlers(false, true);
                            handler.onResponderInitialized(link);
                        }
                    }
                });
            }
        });

        manager.start(null);
    }

    public void stop() {
        running = false;
        manager.stop();
    }

    /**
     * Blocks the thread indefinitely while the endpoint is active or connected
     * to a host. This will automatically unblock when the endpoint becomes
     * inactive or disconnects, allowing the thread to proceed execution. Typical
     * usage is to call {@code sleep} in the main thread to prevent the application
     * from terminating abnormally.
     */
    public void sleep() {
        try {
            while (running) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
