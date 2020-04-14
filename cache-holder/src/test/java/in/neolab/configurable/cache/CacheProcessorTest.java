package in.neolab.configurable.cache;

import in.neolab.configurable.cache.processor.CacheProcessor;
import in.neolab.configurable.cache.processor.CacheStrategiesSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class CacheProcessorTest {
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
    public void prioritySystemTest() throws Exception {
        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        assertTrue(
                cacheHolder.getCacheProcessor().getPriority("Key1") <
                        cacheHolder.getCacheProcessor().getPriority("Key2")
        );


        cacheHolder.clear();
        cacheHolder = CacheHolder.CacheBuilder.get()
                .addMemoryCacheHolder(new MemoryCacheImpl(1))
                .addFileSystemCacheHolder(new FIleSystemCacheImpl(1))
                .addCacheProcessor(new CacheProcessor(new CacheStrategiesSelector<>("LFU")))
                .build();

        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        assertEquals(1, (long) cacheHolder.getCacheProcessor().getPriority("Key1"));
        assertEquals(1, (long) cacheHolder.getCacheProcessor().getPriority("Key2"));
        cacheHolder.get("Key1");
        assertEquals(2, (long) cacheHolder.getCacheProcessor().getPriority("Key1"));
        assertEquals(1, (long) cacheHolder.getCacheProcessor().getPriority("Key2"));
    }

    @Test
    public void testRarelyUsedObjects() throws Exception {
        cacheHolder = CacheHolder.CacheBuilder.get()
                .addMemoryCacheHolder(new MemoryCacheImpl(1))
                .addFileSystemCacheHolder(new FIleSystemCacheImpl(1))
                .addCacheProcessor(new CacheProcessor(new CacheStrategiesSelector<>("LFU")))
                .build();

        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        cacheHolder.put("Key3", "Value3");
        cacheHolder.put("Key4", "Value4");
        cacheHolder.get("Key1");
        cacheHolder.get("Key2");
        // Так как ключи 1 и 2 доставались из кэша, то их приоритет будет выше чем у ключей 3 и 4
        assertEquals(2, cacheHolder.getCacheProcessor().getRarelyUsed().size());
        Set expectedSet = new HashSet();
        expectedSet.add("Key3");
        expectedSet.add("Key4");
        assertEquals(expectedSet, cacheHolder.getCacheProcessor().getRarelyUsed());
    }
}
