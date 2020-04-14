package in.neolab.configurable.cache;

import com.typesafe.config.Config;

import in.neolab.configurable.cache.processor.CacheProcessor;
import in.neolab.configurable.cache.processor.CacheStrategiesSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Cache holder class
 * @param <K> Type of the key object should implement {@link Serializable} interface
 * @param <V> Type of the value object should implement {@link Serializable} interface
 * @author Nikita Parygin
 */
public class CacheHolder<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(CacheHolder.class);

    /**
     * memory cache object. Should implement {@link Cache} interface
     */
    private Cache<K, V> memoryCache;

    /**
     * file system cache object. Should implement {@link Cache} interface
     */
    private Cache<K, V> fileSystemCache;

    /**
     * cache processor object
     */
    private CacheProcessor<K> cacheProcessor;

    /**
     * The constructor
     * @param memoryCache memory cache object. Should implement {@link Cache} interface
     * @param fileSystemCache file system cache object. Should implement {@link Cache} interface
     * @param cacheProcessor cache processor object
     */
    public CacheHolder(Cache<K, V> memoryCache, Cache<K, V> fileSystemCache, CacheProcessor<K> cacheProcessor) {
        this.memoryCache = memoryCache;
        this.fileSystemCache = fileSystemCache;
        this.cacheProcessor = cacheProcessor;
    }

    /**
     * Default empty constructor
     */
    public CacheHolder() {

    }

    public Cache<K, V> getMemoryCache() {
        return memoryCache;
    }

    public Cache<K, V> getFileSystemCache() {
        return fileSystemCache;
    }

    public CacheProcessor<K> getCacheProcessor() {
        return cacheProcessor;
    }

    @Override
    public void put(K key, V value) throws Exception {
        try {
            // Если есть место в  кэше оперативной памяти или объект с таким ключом уже существует то пихаем объект в кэш оперативной памяти
            if (memoryCache.containsKey(key) || memoryCache.isNotFull()) {
                memoryCache.put(key, value);
                // Иначе если есть место в кэше файловой системы или объект с таким ключом уже есть там - пихаем туда
            } else if (fileSystemCache.containsKey(key) || fileSystemCache.isNotFull()) {
                fileSystemCache.put(key, value);
            } else {
                // Если места нет ни там ни там - заменяем, в соответствии с выбранной стратегией
                recache(key, value);
            }
            if (!cacheProcessor.containsKey(key)) {
                // Также добавляем новый ключ в систему приоритетов кэш-процессора
                cacheProcessor.put(key);
            }
            LOGGER.info("Object {} was successfully cached with key: {}", value.toString(), key.toString());
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to put element with key: {} and value: {} into the cache",
                    key.toString(),
                    value.toString(),
                    e
            );
            throw e;
        }

    }

    @Override
    public V get(K key) throws Exception {
        V object = null;
        // Сперва ищем объект в оперативной памяти
        if (memoryCache.containsKey(key)) {
            object = memoryCache.get(key);
            // Каждый раз когда запрашиваем объект из кэша - увеличиваем/обновляем приоритет запрашиваемого ключа
            cacheProcessor.put(key);
            // Если не нашили в оперативной памяти - ищем в файловой системе
        } else if (fileSystemCache.containsKey(key)) {
            object = fileSystemCache.get(key);
            cacheProcessor.put(key);
            // Если объекты из файловой системы запрашиваются слишком часто - имеет смысл переместить их в оперативную память
            // Для этого сравниваем приоритет текущего ключа из файловой системы с наименьшим из оперативной памяти
            Long priority = cacheProcessor.getPriority(key);
            K memoryKey = getRarelyUsedFromMemoryCache().stream().findFirst().orElse(null);
            if (memoryKey == null) {
                return object;
            }
            Long memoryKeyPriority = cacheProcessor.getPriority(memoryKey);
            // Если приоритет ключа из файловой системы выше - меням его местами с любым ключом из списка ключей
            // с низким приоритетом из оперативной памяти.
            if (priority > memoryKeyPriority) {
                try {
                    swap(memoryKey, key);
                    LOGGER.info(
                            "Object with key {} was transferred from file system cache to memory cache. " +
                                    "It replaced object with key {}",
                            key.toString(),
                            memoryKey.toString()
                    );
                } catch (Exception e) {
                    LOGGER.error(
                            "Failed to transfer object with key {} from file system cache to memory cache",
                            key.toString(),
                            e
                    );
                    throw e;
                }

            }
        }
        if (object != null) {
            LOGGER.info("Object {} with key {} was successfully requested", object.toString(), key.toString());
        } else {
            LOGGER.warn("Could not find object by key {} in cache", key.toString());
        }

        return object;
    }

    @Override
    public void remove(K key) throws Exception {
        try {
            // Удаляем из оперативной памяти (если он там есть)
            if (memoryCache.containsKey(key)) {
                memoryCache.remove(key);
            }
            // Из файловой системы
            if (fileSystemCache.containsKey(key)) {
                fileSystemCache.remove(key);
            }
            // Из таблицы приоритетов кэш-процессора
            cacheProcessor.remove(key);
        } catch (Exception e) {
            LOGGER.error("Failed to remove element with key {} from cache", key.toString(), e);
            throw e;
        }
        LOGGER.info("Object with key {} was successfully removed from cache", key.toString());
    }

    @Override
    public boolean containsKey(K key) {
        return (memoryCache.containsKey(key) || fileSystemCache.containsKey(key));
    }

    @Override
    public void clear() {
        // Тут просто чистим кэш оперативной памяти, файловой систем и таблицу приоритетов кэш-процессора
        try {
            memoryCache.clear();
            fileSystemCache.clear();
            cacheProcessor.clearPriorityTable();
            LOGGER.info("Cache was successfully cleared");
        } catch (Exception e) {
            LOGGER.error("Failed to clear cache", e);
        }
    }

    @Override
    public int size() {
        // Текущий размер кэша (количество элементов)
        return memoryCache.size() + fileSystemCache.size();
    }

    @Override
    public boolean isNotFull() {
        return (memoryCache.isNotFull() || fileSystemCache.isNotFull());
    }

    /**
     * Ыwaps two objects. An object that was in the memory cache is transferred to the file system cache.
     * An object that was in the file system cache is transferred to memory
     * @param memoryKey key of memory cache object
     * @param fileSystemKey key of file system cache object
     * @throws Exception if any exception is caught
     */
    private void swap(K memoryKey, K fileSystemKey) throws Exception {
        // Сперва достаем объекты из оперативной памяти
        V memoryObject = memoryCache.get(memoryKey);
        // И из файловой системы
        V fileSystemObject = fileSystemCache.get(fileSystemKey);
        // Дальше удаляем оттуда и оттуда
        memoryCache.remove(memoryKey);
        fileSystemCache.remove(fileSystemKey);
        // В конце записываем по новой, просто меняя местами =))
        memoryCache.put(fileSystemKey, fileSystemObject);
        fileSystemCache.put(memoryKey, memoryObject);
    }

    /**
     * Transfer object between to cache levels
     * @param key key
     * @param val value
     * @throws IOException if any input or output error is occurred
     * @throws ClassNotFoundException  if the requested class is not in classpath
     */
    private void recache(K key,  V val) throws Exception {
        // Тут мы получаем ключ с наименьшим приоритетом.
        // Таких ключей может быть несколько - мы берем последний из отсортированного списка
        K keyForReplace = cacheProcessor.getKeyForReplace();
        if (memoryCache.containsKey(keyForReplace)) {
            memoryCache.remove(keyForReplace);
            cacheProcessor.remove(keyForReplace);
            memoryCache.put(key, val);
        } else {
            fileSystemCache.remove(keyForReplace);
            cacheProcessor.remove(keyForReplace);
            fileSystemCache.put(key, val);
        }
    }

    /**
     * Returns collection of most rarely used keys in cache processor priority system
     * @return Set of {@link K} objects
     */
    private Set<K> getRarelyUsedFromMemoryCache() {
        Set<K> result = new HashSet<>();
        Set<K> rarelyUsed = cacheProcessor.getRarelyUsed();
        for (K key: rarelyUsed) {
            if (memoryCache.containsKey(key)) {
                result.add(key);
            }
        }
        return result;
    }

    /**
     * Cache builder class. Return instance of {@link CacheHolder} object
     */
    public static final class CacheBuilder {
        private CacheHolder cacheHolder = new CacheHolder();

        /**
         * Set value to the {@link CacheHolder#memoryCache} field
         * @param memoryCache memory cache object. Should implement {@link Cache} interface
         * @return instance of {@link CacheBuilder} object used for building
         */
        public CacheBuilder addMemoryCacheHolder(Cache memoryCache) {
            cacheHolder.memoryCache = memoryCache;
            return this;
        }
        /**
         * Set value to the {@link CacheHolder#fileSystemCache} field
         * @param fileSystemCache file system cache object. Should implement {@link Cache} interface
         * @return instance of {@link CacheBuilder} object used for building
         */
        public  CacheBuilder addFileSystemCacheHolder(Cache fileSystemCache) {
            cacheHolder.fileSystemCache = fileSystemCache;
            return this;
        }
        /**
         * Set value to the {@link CacheHolder#memoryCache} field
         * @param cacheProcessor cache processor object
         * @return instance of {@link CacheBuilder} object used for building
         */
        public CacheBuilder addCacheProcessor(CacheProcessor cacheProcessor) {
            cacheHolder.cacheProcessor = cacheProcessor;
            return this;
        }

        /**
         * Build cache holder
         * @return {@link CacheHolder} object
         */
        public CacheHolder build() {
            return cacheHolder;
        }

        /**
         * Get cache builder
         * @return {@link CacheBuilder} object
         */
        public static CacheBuilder get() {
            return new CacheBuilder();
        }

        /**
         * Return cache holder object built using the {@link Config} object.<br>
         * This object should have 3 mandatory properties:<br>
         *     <ul>
         *         <li><code>size.memory</code> - maximum number of items that memory cache can store </li>
         *         <li><code>size.file-system</code> - maximum number of items that file system cache can store</li>
         *         <li><code>strategy</code> - implemented cache erasure strategy (Should be "LFU" or "LRU")</li>
         *     </ul>
         * @param config configuration object implements {@link Config} interface
         * @return {@link CacheBuilder} object
         */
        public CacheHolder buildFromConfig(Config config) {
            cacheHolder.memoryCache = new MemoryCacheImpl(config.getInt("size.memory"));
            cacheHolder.fileSystemCache = new FIleSystemCacheImpl(config.getInt("size.file-system"));
            cacheHolder.cacheProcessor = new CacheProcessor(
                    new CacheStrategiesSelector(config.getString("strategy"))
            );
            return cacheHolder;
        }
    }
}
