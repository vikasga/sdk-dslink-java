package org.dsa.iot.dslink.requests;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Samuel Grenier
 */
@AllArgsConstructor
public class CloseRequest extends Request {

    @NonNull
    private final int rid;

    @Override
    public String getName() {
        return "close";
    }

    @Override
    public void addJsonValues(JsonObject obj) {
        obj.removeField("rid");
        obj.putNumber("rid", rid);
    }
}