package in.neolab.configurable.cache;

import in.neolab.configurable.cache.processor.CacheProcessor;
import in.neolab.configurable.cache.processor.CacheStrategiesSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LruStrategyTest {
    private CacheHolder cacheHolder;

    @Before
    public void init() {
        cacheHolder = CacheHolder.CacheBuilder.get()
                .addMemoryCacheHolder(new MemoryCacheImpl(2))
                .addFileSystemCacheHolder(new FIleSystemCacheImpl(2))
                .addCacheProcessor(new CacheProcessor(new CacheStrategiesSelector<>("LRU")))
                .build();
    }

    @After
    public void clearCache() {
        cacheHolder.clear();
    }

    @Test
    public void testLruStrategy() throws Exception {

        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        cacheHolder.put("Key3", "Value3");
        cacheHolder.put("Key4", "Value4");
        cacheHolder.put("Key5", "Value5");
        // Проверяем что из кэша вылетел самый старый элемент
        assertFalse(cacheHolder.containsKey("Key1"));

        cacheHolder.clear();
        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        cacheHolder.put("Key3", "Value3");
        cacheHolder.put("Key4", "Value4");
        cacheHolder.get("Key1");
        cacheHolder.put("Key5", "Value5");
        // Проверяем что объект который уже доставался из кеша не был выброшен
        assertTrue(cacheHolder.containsKey("Key1"));
        assertFalse(cacheHolder.containsKey("Key2"));
    }

}
