package org.jenkinsci.plugins.globalEventsPlugin.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds Maps.
 */
public final class MapBuilder {

    private MapBuilder() {
    }

    public static MapBuilderInstance put(final Object key, final Object value) {
        MapBuilderInstance inst = new MapBuilderInstance();
        inst.put(key, value);
        return inst;
    }

    public static final class MapBuilderInstance {

        private final Map<Object, Object> delegate = new HashMap<Object, Object>();

        public MapBuilderInstance put(final Object key, final Object value) {
            delegate.put(key, value);
            return this;
        }

        public Map<Object, Object> build() {
            return delegate;
        }

    }


}
