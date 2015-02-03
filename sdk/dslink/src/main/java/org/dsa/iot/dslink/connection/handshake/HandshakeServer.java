package org.dsa.iot.dslink.connection.handshake;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.UrlBase64;
import org.dsa.iot.core.SyncHandler;
import org.dsa.iot.core.URLInfo;
import org.dsa.iot.core.Utils;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;

import java.util.concurrent.TimeUnit;

/**
 * Holds handshake information about a server.
 * @author Samuel Grenier
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HandshakeServer {

    private final String dsId;
    private final String publicKey;
    private final String wsUri;
    private final String httpUri;
    private final byte[] sharedSecret;
    private final String salt;
    private final String saltS;
    private final Integer updateInterval;

    public static HandshakeServer perform(String url, HandshakeClient hc) {
        return perform(URLInfo.parse(url), hc);
    }

    public static HandshakeServer perform(URLInfo url, HandshakeClient hc) {
        return perform(url, hc, true);
    }

    public static HandshakeServer perform(URLInfo url,
                                          HandshakeClient hc,
                                          boolean verifySsl) {
        HttpClient client = Utils.VERTX.createHttpClient();
        client.setHost(url.host).setPort(url.port);
        if (url.secure) {
            client.setSSL(true);
            client.setVerifyHost(verifySsl);
        }

        SyncHandler<HttpClientResponse> reqHandler = new SyncHandler<>();
        HttpClientRequest req = client.post(url.path + "?dsId=" + hc.getDsId(), reqHandler);

        String encoded = hc.toJson().encode();
        req.putHeader("Content-Length", String.valueOf(encoded.length()));
        req.write(encoded);
        req.end();

        HttpClientResponse resp = reqHandler.get(2, TimeUnit.SECONDS);
        if (resp == null)
            throw new NullPointerException("resp");
        SyncHandler<Buffer> bufHandler = new SyncHandler<>();
        resp.bodyHandler(bufHandler);

        Buffer buf = bufHandler.get(30, TimeUnit.SECONDS);
        if (buf == null)
            throw new NullPointerException("buf (Failed to receive any data)");
        JsonObject obj = new JsonObject(buf.toString());

        String dsId = obj.getString("dsId");
        String publicKey = obj.getString("publicKey");
        String wsUri = obj.getString("wsUri");
        String httpUri = obj.getString("httpUri");
        byte[] sharedSecret = decryptSharedSecret(hc, obj.getString("tempKey"));
        String salt = obj.getString("salt");
        String saltS = obj.getString("saltS");
        Integer updateInterval = obj.getInteger("updateInterval");

        return new HandshakeServer(dsId, publicKey, wsUri, httpUri,
                                    sharedSecret, salt, saltS, updateInterval);
    }

    private static byte[] decryptSharedSecret(HandshakeClient client,
                                                String tempKey) {
        tempKey = Utils.addPadding(tempKey, true);
        byte[] decoded = UrlBase64.decode(tempKey);
        ECParameterSpec params = client.getPrivKeyInfo().getParameters();
        ECPoint point = params.getCurve().decodePoint(decoded);
        ECPublicKeySpec spec = new ECPublicKeySpec(point, params);
        point = spec.getQ().multiply(client.getPrivKeyInfo().getD());
        return point.getAffineXCoord().toBigInteger().toByteArray();
    }
}
