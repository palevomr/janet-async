package io.techery.janet;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

abstract class AsyncActionsRosterBase {

    protected final ConcurrentHashMap<String, List<Class>> map = new ConcurrentHashMap<String, List<Class>>();

    List<Class> getActionClasses(String event) {
        return map.get(event);
    }

    boolean containsEvent(String event) {
        return map.containsKey(event);
    }

    Enumeration<String> getRegisteredEvents() {
        return map.keys();
    }
}