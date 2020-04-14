package in.neolab.rest.service.controller;

import in.neolab.configurable.cache.CacheHolder;
import in.neolab.configurable.cache.config.SystemHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Serializable;

/**
 * REST-controller providing functionality to use {@link in.neolab.configurable.cache.Cache} interface methods
 * @author Nikita Parygin
 */
@RestController
@RequestMapping("cache")
@Api(value = "Configurable cache API", tags = {"Cache API"})
public class CacheController<K extends Serializable, V extends Serializable> {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

    /**
     * {@link CacheHolder} instance
     */
    private CacheHolder cache = CacheHolder.CacheBuilder.get().buildFromConfig(SystemHolder.getConfig());

    /**
     * Adding an item to the cache
     * @param key item key used for searching elements in cache
     * @param value item value
     * @throws Exception if any error is occurred
     */
    @ApiOperation(value = "Put key-value pair into cache", tags = {"Cache API"})
    @RequestMapping(value = "/put", method = RequestMethod.POST)
    public void put(@RequestParam(value = "key") final K key, @RequestBody final Object value) throws Exception {
        try {
            GsonJsonParser gson = new GsonJsonParser();
            cache.put(key, (Serializable) gson.parseMap(String.valueOf(value)));
        } catch (Exception e) {
            LOGGER.error("Failed to put received value into cache", e);
            throw e;
        }
    }

    /**
     * Retrieving an item from the cache by key
     * @param key item key used for searching elements in cache
     * @return item value received from cache
     * @throws Exception if any error is occurred
     */
    @ApiOperation(value = "Get value from cache by key", tags = {"Cache API"})
    @RequestMapping(value = "/get/{key}", method= RequestMethod.GET)
    public Object get(@PathVariable("key") K key) throws Exception {
        try {
            return cache.get(key);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Failed to get value with key {} from cache", key.toString(), e);
            throw e;
        }
    }

    /**
     * Removes all items from the cache
     */
    @ApiOperation(value = "Clear cache", tags = {"Cache API"})
    @RequestMapping(value = "/clear", method= RequestMethod.DELETE)
    public String clear() {
        try {
            cache.clear();
            return "Cache cleared";
        } catch (Exception e) {
            LOGGER.error("Failed to clear cache", e);
            throw e;
        }
    }

    /**
     * Removing an item from the cache by key
     * @param key item key used for searching elements in cache
     * @throws Exception if any error is occurred
     */
    @ApiOperation(value = "Remove object from cache by key", tags = {"Cache API"})
    @RequestMapping(value = "/remove/{key}", method= RequestMethod.DELETE)
    public String remove(@PathVariable("key") K key) throws Exception {
        try {
            if (cache.containsKey(key)) {
                cache.remove(key);
                return String.format("Object with key %1$s was successfully removed", key);
            } else {
                return String.format("Could not find the item with the specified key %1$s", key);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to remove object with key {}", key.toString(), e);
            throw e;
        }
    }

    /**
     * Return size of the cache
     * @return current count of elements stored int the cache
     */
    @ApiOperation(value = "Get cache size", tags = {"Cache API"})
    @RequestMapping(value = "/size", method= RequestMethod.GET)
    public int size() throws Exception {
        try {
            return cache.size();
        } catch (Exception e) {
            LOGGER.error("Failed to get cache size", e);
            throw e;
        }
    }
}
