package in.neolab.configurable.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that implements {@link Cache} interface at tle RAM level
 * @param <K> Type of the key object should implement {@link Serializable} interface
 * @param <V> Type of the value object should implement {@link Serializable} interface
 * @author Nikita Parygin
 */
public class MemoryCacheImpl<K extends Serializable, V extends Serializable> implements Cache<K, V> {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(MemoryCacheImpl.class);

    /**
     * File system cache
     */
    private Map<K, V> cache;

    /**
     * Maximum count of cache elements
     */
    private final int size;

    /**
     * The Constructor
     * @param size maximum count of cache elements
     */
    public MemoryCacheImpl(int size) {
        this.size = size;
        cache = new HashMap<>(size);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return containsKey(key) ? cache.get(key) : null;
    }

    @Override
    public void remove(K key) {
      cache.remove(key);
    }

    @Override
    public boolean containsKey(K key) {
        try {
            return cache.containsKey(key);
        } catch (Exception e) {
            LOGGER.error(
                    "Unexpected exception is caught while searching item by key {} in memory cache",
                    key.toString(),
                    e
            );
            return false;
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isNotFull() {
        return (size() < this.size);
    }
}
