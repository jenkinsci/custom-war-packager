package io.jenkins.tools.warpackager.lib.util;

import edu.umd.cs.findbugs.annotations.NonNull;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.IOException;
import java.util.Map;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class CollectionsHelper {

    @CheckForNull
    public static <K,V> V putIfNotNull(@NonNull Map<K, V> dest, @NonNull K key, @CheckForNull V value) {
        if (value != null) {
            return dest.put(key, value);
        }
        return dest.get(key);
    }

    @CheckForNull
    public static <K,V> V getOrFail(@NonNull Map<K, V> src, @NonNull K key, @NonNull String where) throws IOException {
        V value = src.get(key);
        if (value == null) {
            throw new IOException("Cannot find mandatory key " + key + " in " + where);
        }
        return value;
    }
}
