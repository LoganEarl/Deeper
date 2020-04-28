package main.java.world.cache;

import java.util.WeakHashMap;

public class WorldSpecificCache<T, U> {
    private String databaseName;

    private WeakHashMap<T, U> cache = new WeakHashMap<>();

    public WorldSpecificCache(String databaseName) {
        this.databaseName = databaseName;
    }

    public void putValue(T key, U value){
        cache.put(key, value);
    }

    public U getValue(T key){
        return cache.get(key);
    }

    public void remove(T key){
        cache.remove(key);
    }

    public void clear(){
        cache.clear();
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
