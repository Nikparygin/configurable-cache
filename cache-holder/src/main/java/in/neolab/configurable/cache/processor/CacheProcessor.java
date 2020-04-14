package in.neolab.configurable.cache.processor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The cache processor adjusts the key priority system depending on the extrusion strategy being implemented.
 * It decides which keys to remove from the cache when it is full.
 * @param <K> Type of the key object should implement {@link Serializable} interface
 * @author Nikita Parygin
 */
public class CacheProcessor<K> {

    /**
     * Map that contains key-priority pair
     */
    private Map<K, Long> priorityTable;
    /**
     * Function that implements extrusion strategy
     */
    private BiConsumer<K, Map> implStrategy;

    /**
     * The Constructor
     * @param implStrategy Function that implements extrusion strategy
     */
    public CacheProcessor(BiConsumer<K, Map> implStrategy) {
        this.priorityTable = new HashMap<>();
        this.implStrategy = implStrategy;
    }

    /**
     * Add key to the priority system or update priority value if key is already exists
     * @param key key
     */
    public void put(K key) {
        // Реализация процесса обновления таблицы приоритетов представлена в классе
        // CacheStrategiesSelector. Этот класс содержит функцию-консьюмер, осуществляющую добавление или обновление ключей
        implStrategy.accept(key, priorityTable);
    }
    /**
     * Searches for key in the priority system
     * @param key key
     * @return  <code>true</code> - if key is in the system.<br>
     *          <code>false</code> - otherwise
     */
    public boolean containsKey(K key) {
        return priorityTable.containsKey(key);
    }

    /**
     * Remove key from the priority system
     * @param key key
     */
    public void remove(K key) {
        priorityTable.remove(key);
    }

    /**
     * Delete all keys from priority system
     */
    public void clearPriorityTable() {
        priorityTable.clear();
    }

    /**
     * Return one rarest used key
     * @return {@link K} object
     */
    public K getKeyForReplace() {
        // Тут можно не делать никаких проверок, т.к. если в таблице приоритетов отсутствуют элементы мы по коду сюда не должны попасть
        return priorityTable
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .findFirst()
                .get()
                .getKey();
    }

    /**
     * Returns collection of rarely used keys in cache processor priority system
     * @return Set of {@link K} objects
     */
    public Set<K> getRarelyUsed() {
        // Решение, конечно не очень, но зато работает, более изящное пока не придумал
        Set<K> result = new HashSet<>();
        // Получаем список ключей системы приоритетов
        Set<K> keys = priorityTable.keySet();
        for (K firstKey: keys) {
            // Добавляем в результирующий список наименьшие ключи
            boolean needToAdd = true;
            for (K secondKey: keys) {
                if (priorityTable.get(firstKey) > priorityTable.get(secondKey)) {
                    needToAdd = false;
                    break;
                }
            }
            if (needToAdd) {
                result.add(firstKey);
            }
        }
        return result;
    }

    /**
     * Returns priority of the single key in cache processor
     * @param key requested key
     * @return priority
     */
    public Long getPriority(K key) {
        return priorityTable.get(key);
    }


}
