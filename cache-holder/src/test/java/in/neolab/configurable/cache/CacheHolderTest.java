package in.neolab.configurable.cache;

import in.neolab.configurable.cache.processor.CacheProcessor;
import in.neolab.configurable.cache.processor.CacheStrategiesSelector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class CacheHolderTest {

    private CacheHolder cacheHolder;

    @Before
    public void init() {
        cacheHolder = CacheHolder.CacheBuilder.get()
                .addMemoryCacheHolder(new MemoryCacheImpl(1))
                .addFileSystemCacheHolder(new FIleSystemCacheImpl(1))
                .addCacheProcessor(new CacheProcessor(new CacheStrategiesSelector<>("LRU")))
                .build();
    }

    @After
    public void clearCache() {
        cacheHolder.clear();
    }

    @Test
    public void replaceObjectTest() throws Exception {
        // Добавляем 3 элемента в кэш
        for (int i = 0; i < 2; i++) {
            cacheHolder.put(String.format("Key%1$s",i), String.format("Value%1$s",i));
        }
        Assert.assertFalse(cacheHolder.isNotFull());
        // Проверяем что последний элемент удалился из кэша
        Assert.assertFalse(cacheHolder.containsKey("Key2"));
        // Добавляем новый элемент
        cacheHolder.put("Key2", "Value2");
        // Прверяем что добавился
        Assert.assertEquals("Value2", cacheHolder.get("Key2"));
        Assert.assertTrue(cacheHolder.containsKey("Key2"));
    }

    @Test
    public void cacheSizeTest() throws Exception {
        assertEquals(0, cacheHolder.size());
        cacheHolder.put("Key1", "Value1");
        assertEquals(1, cacheHolder.size());
        cacheHolder.put("Key2", "Value2");
        assertEquals(2, cacheHolder.size());
    }

    @Test
    public void containsKeyTest() throws Exception {
        assertFalse(cacheHolder.containsKey("Key1"));
        cacheHolder.put("Key1", "Value1");
        assertTrue(cacheHolder.containsKey("Key1"));
    }

    @Test
    public void isNotFullTest() throws Exception {
        assertFalse(cacheHolder.containsKey("Key1"));
        cacheHolder.put("Key1", "Value1");
        assertTrue(cacheHolder.isNotFull());
        cacheHolder.put("Key2", "Value2");
        assertFalse(cacheHolder.isNotFull());
    }

    @Test
    public void clearCacheTest() throws Exception {
        assertEquals(0, cacheHolder.size());
        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        assertEquals(2, cacheHolder.size());
        assertTrue(cacheHolder.containsKey("Key1"));
        assertTrue(cacheHolder.containsKey("Key2"));
        cacheHolder.clear();
        assertEquals(0, cacheHolder.size());
        assertFalse(cacheHolder.containsKey("Key1"));
        assertFalse(cacheHolder.containsKey("Key2"));
    }

    @Test
    public void cacheSwitchTest() throws Exception {
        cacheHolder = CacheHolder.CacheBuilder.get()
                .addMemoryCacheHolder(new MemoryCacheImpl(2))
                .addFileSystemCacheHolder(new FIleSystemCacheImpl(2))
                .addCacheProcessor(new CacheProcessor(new CacheStrategiesSelector<>("LFU")))
                .build();

        cacheHolder.put("Key1", "Value1");
        cacheHolder.put("Key2", "Value2");
        cacheHolder.put("Key3", "Value3");
        cacheHolder.put("Key4", "Value4");
        cacheHolder.get("Key3");
        cacheHolder.get("Key4");
        assertTrue(cacheHolder.getMemoryCache().containsKey("Key3"));
        assertTrue(cacheHolder.getMemoryCache().containsKey("Key4"));
        assertTrue(cacheHolder.getFileSystemCache().containsKey("Key1"));
        assertTrue(cacheHolder.getFileSystemCache().containsKey("Key2"));

    }
}
