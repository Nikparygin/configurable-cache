package in.neolab.configurable.cache;

import in.neolab.configurable.cache.processor.CacheProcessor;
import in.neolab.configurable.cache.processor.CacheStrategiesSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LfuStrategyTest {
    private CacheHolder cacheHolder;

    @Before
    public void init() {
        cacheHolder = CacheHolder.CacheBuilder.get()
                .addMemoryCacheHolder(new MemoryCacheImpl(2))
                .addFileSystemCacheHolder(new FIleSystemCacheImpl(2))
                .addCacheProcessor(new CacheProcessor(new CacheStrategiesSelector<>("LFU")))
                .build();
    }

    @After
    public void clearCache() {
        cacheHolder.clear();
    }

    @Test
    public void testLfuStrategy() throws Exception {
        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        cacheHolder.put("Key3", "Value3");
        cacheHolder.put("Key4", "Value4");
        cacheHolder.get("Key4");
        cacheHolder.get("Key4");
        cacheHolder.get("Key1");
        cacheHolder.get("Key1");
        cacheHolder.get("Key2");
        cacheHolder.get("Key2");
        cacheHolder.get("Key3");
        cacheHolder.put("Key5", "Value5");

        // Проверяем что из кэша вылетел элемент который был использован 1 раз,
        // а элементы которые были использованны по 2 раза остались
        assertTrue(cacheHolder.containsKey("Key1"));
        assertTrue(cacheHolder.containsKey("Key2"));
        assertTrue(cacheHolder.containsKey("Key4"));
        assertTrue(cacheHolder.containsKey("Key5"));
        assertFalse(cacheHolder.containsKey("Key3"));

    }
}
