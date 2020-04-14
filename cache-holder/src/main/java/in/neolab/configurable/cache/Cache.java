package in.neolab.configurable.cache;

import java.io.Serializable;

/**
 * Cache interface. provides functionality for
 * adding, receiving, deleting items from the cache,
 * completely clearing the cache, etc.
 * @param <K> Type of the key object should implement {@link Serializable} interface
 * @param <V> Type of the value object should implement {@link Serializable} interface
 * @author Nikita Parygin
 */
public interface  Cache<K, V> {
    /**
     * Adding an item to the cache
     * @param key item key used for searching elements in cache
     * @param value item value
     * @throws Exception if any error is occurred
     */
    void put(K key, V value) throws Exception;

    /**
     * Retrieving an item from the cache by key
     * @param key item key used for searching elements in cache
     * @return item value received from cache
     * @throws Exception if any error is occurred
     */
    V get(K key) throws Exception;

    /**
     * Removing an item from the cache by key
     * @param key item key used for searching elements in cache
     * @throws Exception if any error is occurred
     */
    void remove(K key) throws Exception;

    /**
     * Searches for an item in the cache that matches the specified key
     * @param key key by which the item is searched in the cache
     * @return  <code>true</code> - if the item with the specified key is in the cache.<br>
     *          <code>false</code> - otherwise
     */
    boolean containsKey(K key);

    /**
     * Removes all items from the cache
     */
    void clear();

    /**
     * Return size of the cache
     * @return current count of elements stored int the cache
     */
    int size();

    /**
     * Search for free space left in the cache
     * @return  <code>true</code> - if there is free space left in the cache<br>
     *          <code>false</code> - otherwise
     */
    boolean isNotFull();
}
