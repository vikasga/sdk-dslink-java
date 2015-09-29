package org.dsa.iot.dslink.util.json;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Samuel Grenier
 */
public class JsonArray implements Iterable<Object> {

    private final List<Object> list;

    public JsonArray() {
        this(new LinkedList<>());
    }

    public JsonArray(String content) {
        this((List) Json.decode(content, List.class));
    }

    @SuppressWarnings("unchecked")
    public JsonArray(List list) {
        if (list == null) {
            throw new NullPointerException("list");
        }
        this.list = new LinkedList<>();
        for (Object obj : list) {
            add(obj);
        }
    }

    public String encode() {
        return Json.encode(this);
    }

    @SuppressWarnings("unused")
    public String encodePrettily() {
        return Json.encodePrettily(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        return (T) list.get(index);
    }

    public JsonArray add(Object value) {
        value = Json.checkAndUpdate(value);
        list.add(value);
        return this;
    }

    public int size() {
        return list.size();
    }

    public List<Object> getList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public Iterator<Object> iterator() {
        return list.iterator();
    }
}