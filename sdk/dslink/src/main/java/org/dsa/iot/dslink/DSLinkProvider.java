package org.dsa.iot.dslink;

import org.dsa.iot.dslink.connection.Endpoint;
import org.dsa.iot.dslink.connection.NetworkClient;
import org.dsa.iot.dslink.connection.RemoteEndpoint;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.serializer.SerializationManager;
import org.vertx.java.core.Handler;

import java.io.File;

/**
 * Provides DSLinks as soon as a client connects to the server or vice versa.
 * @author Samuel Grenier
 */
public class DSLinkProvider {

    private final Endpoint endpoint;
    private final DSLinkHandler handler;

    public DSLinkProvider(Endpoint endpoint, DSLinkHandler handler) {
        if (endpoint == null)
            throw new NullPointerException("endpoint");
        else if (handler == null)
            throw new NullPointerException("handler");
        this.endpoint = endpoint;
        this.handler = handler;
        handler.preInit();
    }

    /**
     * Sets the default endpoint handler. Override if a custom DSLink
     * implementation needs to be provided.
     * @see DSLinkHandler#onRequesterConnected
     * @see DSLinkHandler#onResponderConnected
     */
    public void setDefaultEndpointHandler() {
        endpoint.setClientConnectedHandler(new Handler<NetworkClient>() {
            @Override
            public synchronized void handle(NetworkClient event) {
                DSLink link = null;
                if (event.isRequester()) {
                    link = new DSLink(handler, event);
                    link.setDefaultDataHandlers();
                    handler.onRequesterConnected(link);
                } else if (event.isResponder()) {
                    link = new DSLink(handler, event);

                    File path = handler.getConfig().getSerializationPath();
                    if (path != null) {
                        NodeManager man = link.getNodeManager();
                        SerializationManager manager = new SerializationManager(path, man);
                        manager.deserialize();
                        manager.start();
                    }

                    link.setDefaultDataHandlers();
                    handler.onResponderConnected(link);
                }
                if (link != null) {
                    handler.init(link);
                }
            }
        });
    }

    /**
     * @see RemoteEndpoint#activate()
     */
    public void start() {
        endpoint.activate();
    }

    /**
     * @see RemoteEndpoint#deactivate()
     */
    public void stop() {
        endpoint.deactivate();
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
            while (endpoint.isBecomingActive() || endpoint.isActive()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
