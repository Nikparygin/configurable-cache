package in.neolab.configurable.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Class that implements {@link Cache} interface at tle file system level
 * @param <K> Type of the key object should implement {@link Serializable} interface
 * @param <V> Type of the value object should implement {@link Serializable} interface
 * @author Nikita Parygin
 */
public class FIleSystemCacheImpl<K extends Serializable, V extends Serializable> implements Cache<K, V> {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(FIleSystemCacheImpl.class);

    /**
     * Maximum count of cache elements
     */
    private int size;

    /**
     * Folder for storing cached objects
     */
    private File tempDir;

    /**
     * File system cache
     */
    private Map<K, String> cache;

    /**
     * The Constructor
     * @param size maximum count of cache elements
     */
    public FIleSystemCacheImpl(int size) {
        this.size = size;
        this.cache = new HashMap<K, String>(size);
        this.tempDir = new File("./temp");
        tempDir.mkdirs();
    }

    @Override
    public void put(K key, V value) throws IOException {
        String fileName = UUID.randomUUID().toString() + ".temp";
        File tempFile = new File(tempDir, fileName);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(value);
            cache.put(key, fileName);
        }
    }

    @Override
    public V get(K key) throws IOException, ClassNotFoundException {
        if (containsKey(key)) {
            String filename = cache.get(key);
            File file = new File(tempDir + "/" +  filename);
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))){
                ObjectInputStream objectInputStream = new ObjectInputStream(in);
                return (V) objectInputStream.readObject();
            }
        } else {
            return null;
        }
    }

    @Override
    public void remove(K key) {
        try {
            if (containsKey(key)) {
                String filename = cache.get(key);
                File file = new File(tempDir + "/" +  filename);
                if(file.delete()) {
                    LOGGER.info("File {} successfully deleted from storage", file.getName());
                } else {
                    LOGGER.warn("Failed to delete file {} it is probably does not exist", file.getName());
                }
                cache.remove(key);
            } else {
                LOGGER.error("Could not find the item with the specified key {}", key.toString());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to remove item from file system cache", e);
            throw e;
        }

    }

    @Override
    public boolean containsKey(K key) {
        try {
            return cache.containsKey(key);
        } catch (Exception e) {
            LOGGER.error(
                    "Unexpected exception is caught while searching item by key {} in file system cache",
                    key.toString(),
                    e
            );
            return false;
        }
    }

    @Override
    public void clear() {
        Arrays.stream(Objects.requireNonNull(tempDir.listFiles())).forEach(file -> {
            try {
                if(file.delete()) {
                    LOGGER.info("File {} successfully deleted from storage", file.getName());
                } else {
                    LOGGER.warn("Failed to delete file {} it is probably does not exist", file.getName());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to delete file {}", file.getName(), e);
            }
        });
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
