package org.dsa.iot.broker.node;

import org.dsa.iot.broker.server.client.Client;

import java.util.Random;

/**
 * @author Samuel Grenier
 */
public class Downstream extends BrokerNode<DSLinkNode> {

    private static final Random RANDOM = new Random();
    private static final String ALPHABET;

    public Downstream(BrokerNode parent, String name) {
        super(parent, name, "node");
    }

    public String init(String name, String dsId) {
        synchronized (this) {
            DSLinkNode node = getChild(name);
            if (node != null && node.dsId().equals(dsId)) {
                if (node.client() != null) {
                    return null;
                }
                return name;
            }
            if (node != null && hasChild(name)) {
                StringBuilder tmp = new StringBuilder(name);
                tmp.append("-");
                tmp.append(randomChar());

                while (hasChild(tmp.toString())) {
                    tmp.append(randomChar());
                }
                name = tmp.toString();
            }

            node = new DSLinkNode(this, name);
            node.accessible(false);
            addChild(node);
        }
        return name;
    }

    @Override
    public void connected(Client client) {
        super.connected(client);
        DSLinkNode node = getChild(client.handshake().name());
        node.connected(client);
    }

    @Override
    public void propagateConnected(Client client) {
    }

    private static char randomChar() {
        return ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()));
    }

    static {
        String tmp = "";
        tmp += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        tmp += "abcdefghijklmnopqrstuvwxyz";
        tmp += "0123456789";
        ALPHABET = tmp;
    }
}
