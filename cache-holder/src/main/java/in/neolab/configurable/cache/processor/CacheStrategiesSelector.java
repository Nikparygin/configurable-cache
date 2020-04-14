package in.neolab.configurable.cache.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Class that implements cache extrusion strategies
 * @param <K> Type of the key object should implement {@link Serializable} interface
 * @author Nikita Parygin
 */
public class CacheStrategiesSelector<K> implements BiConsumer<K, HashMap<K, Long>> {
    /**
     * Extrusion strategies
     */
    private enum CacheStrategyType {
        /**
         * LFU cache strategy
         */
        LFU,
        /**
         * LRU cache strategy (default)
         */
        LRU
    }

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(CacheStrategiesSelector.class);

    /**
     * Selected extrusion strategy
     */
    private CacheStrategyType selectedType;

    /**
     * The Constructor
     * @param selectedType Selected extrusion strategy
     */
    public CacheStrategiesSelector(String selectedType) {
        try {
            this.selectedType = CacheStrategyType.valueOf(selectedType);
        } catch (Exception e) {
            LOGGER.error("Failed to get cache erasure strategy from configuration file." +
                    "Received value is {}. Value should be \"LFU\" or \"LRU\"." +
                    "Application will use default \"LRU\" value", selectedType, e);
            this.selectedType = CacheStrategyType.LRU;
        }

    }

    @Override
    public void accept(K key, HashMap<K, Long> map) {
        /* TODO: Если количество имплементируемых стратегий увеличится, желательно создать отдельного консьюмера для каждой */
        switch (selectedType) {
            case LFU:
                long frequency = 1;
                if (map.containsKey(key)) {
                    frequency = map.get(key) + 1;
                }
                map.put(key, frequency);
                break;
            // По умолчанию используем стратегию LRU
            case LRU:
            default:
                map.put(key, System.nanoTime());
                break;
        }
    }
}
