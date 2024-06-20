package com.picobase.cache;

import cn.hutool.core.lang.Assert;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * lazy detect the expiration time
 */
public class LazyCache<K, V> {

    private static final float DEFAULT_BACK_PRESSURE_FACTOR = 0.13f;
    private int maximumSize;
    private int backPressureSize;
    private long expireAfterAccessMillis;
    private long expireCheckIntervalMillis;
    private volatile long minExpireTime;
    private AtomicLong expireCheckTimeAtomic;
    private ConcurrentMap<K, CacheValue<V>> cacheMap;
    private BiConsumer<Pair<K, V>, RemovalCause> removeListener = (pair, removalCause) -> {
    };

    public LazyCache(int maximumSize, long expireAfterAccessMillis, long expireCheckIntervalMillis, BiConsumer<Pair<K, V>, RemovalCause> removeListener) {
        Assert.isTrue(maximumSize > 1);
        Assert.isTrue(expireAfterAccessMillis > 0);
        Assert.isTrue(expireCheckIntervalMillis > 0);
        this.maximumSize = maximumSize;
        this.backPressureSize = Math.max(maximumSize, maximumSize + (int) (maximumSize * DEFAULT_BACK_PRESSURE_FACTOR));
        this.expireAfterAccessMillis = expireAfterAccessMillis;
        this.expireCheckIntervalMillis = Math.max(1, expireCheckIntervalMillis);
        this.minExpireTime = System.currentTimeMillis();
        this.expireCheckTimeAtomic = new AtomicLong(System.currentTimeMillis());
        this.cacheMap = new ConcurrentHashMap<>(Math.max(maximumSize / 16, 512));
        if (removeListener != null) {
            this.removeListener = removeListener;
        }
    }

    /**
     * If the cache previously contained a value associated with the key, the old value is replaced by the new value.
     */
    public void put(K key, V value) {
        var cacheValue = new CacheValue<V>();
        cacheValue.value = value;
        cacheValue.expireTime = System.currentTimeMillis() + expireAfterAccessMillis;
        var oldCacheValue = cacheMap.put(key, cacheValue);
        if (oldCacheValue != null) {
            removeListener.accept(new Pair<>(key, oldCacheValue.value), RemovalCause.REPLACED);
        }
        checkExpire();
        checkMaximumSize();
    }

    public V get(K key) {
        return get(key, null);
    }

    public V get(K key, Supplier<V> supplier) {
        checkExpire();

        var cacheValue = cacheMap.get(key);
        if (cacheValue == null) {
            return getVBySupplier(key, supplier);
        }

        long now = System.currentTimeMillis();
        if (cacheValue.expireTime < now) {
            removeForCause(key, RemovalCause.EXPIRED);
            return getVBySupplier(key, supplier);
        }
        cacheValue.expireTime = now + expireAfterAccessMillis;
        return cacheValue.value;
    }

    private V getVBySupplier(K key, Supplier<V> supplier) {
        if (supplier != null) {
            V v = supplier.get();
            this.put(key, v);
            return v;
        } else {
            return null;
        }
    }


    public void remove(K key) {
        removeForCause(key, RemovalCause.EXPLICIT);
    }

    private void removeForCause(K key, RemovalCause removalCause) {
        if (key == null) {
            return;
        }
        var cacheValue = cacheMap.remove(key);
        if (cacheValue != null) {
            removeListener.accept(new Pair<>(key, cacheValue.value), removalCause);
        }
    }

    public void forEach(BiConsumer<K, V> biConsumer) {
        for (var entry : cacheMap.entrySet()) {
            biConsumer.accept(entry.getKey(), entry.getValue().value);
        }
    }

    public int size() {
        return cacheMap.size();
    }

    // -----------------------------------------------------------------------------------------------------------------
    private void checkMaximumSize() {
        if (cacheMap.size() > backPressureSize) {
            cacheMap.entrySet()
                    .stream()
                    .map(it -> new PairLong<>(it.getValue().expireTime, it.getKey()))
                    .sorted((a, b) -> Long.compare(a.getKey(), b.getKey()))
                    .limit(Math.max(0, cacheMap.size() - maximumSize))
                    .forEach(it -> removeForCause(it.getValue(), RemovalCause.SIZE));
        }
    }

    private void checkExpire() {
        var now = System.currentTimeMillis();
        var expireCheckTime = expireCheckTimeAtomic.get();
        if (now > expireCheckTime) {
            if (expireCheckTimeAtomic.compareAndSet(expireCheckTime, now + expireCheckIntervalMillis)) {
                if (now > this.minExpireTime) {
                    var minTimestamp = Long.MAX_VALUE;
                    var removeList = new ArrayList<K>();
                    for (var entry : cacheMap.entrySet()) {
                        var expireTime = entry.getValue().expireTime;
                        if (expireTime < now) {
                            removeList.add(entry.getKey());
                            continue;
                        }
                        if (expireTime < minTimestamp) {
                            minTimestamp = expireTime;
                        }
                    }
                    removeList.forEach(it -> removeForCause(it, RemovalCause.EXPIRED));
                    if (this.minExpireTime < Long.MAX_VALUE) {
                        this.minExpireTime = minTimestamp;
                    }
                }
            }
        }
    }


    public static enum RemovalCause {
        /**
         * The entry was manually removed by the user. This can result from the user invoking any of the
         * following methods on the cache or map view.
         * remove()
         */
        EXPLICIT,

        /**
         * The entry itself was not actually removed, but its value was replaced by the user. This can
         * result from the user invoking any of the following methods on the cache or map view.
         * put()
         */
        REPLACED,


        /**
         * The entry's expiration timestamp has passed.
         */
        EXPIRED,

        /**
         * The entry was evicted due to size constraints.
         */
        SIZE;
    }

    private static class CacheValue<V> {
        public volatile V value;
        public volatile long expireTime;
    }

}
