package io.jenkins.tools.warpackager.lib.util;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class CollectionsHelper {

    @CheckForNull
    public static <K,V> V putIfNotNull(@Nonnull Map<K, V> dest, @Nonnull K key, @CheckForNull V value) {
        if (value != null) {
            return dest.put(key, value);
        }
        return dest.get(key);
    }

    @CheckForNull
    public static <K,V> V getOrFail(@Nonnull Map<K, V> src, @Nonnull K key, @Nonnull String where) throws IOException {
        V value = src.get(key);
        if (value == null) {
            throw new IOException("Cannot find mandatory key " + key + " in " + where);
        }
        return value;
    }
}
