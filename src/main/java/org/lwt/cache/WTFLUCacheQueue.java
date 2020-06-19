package org.lwt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class WTFLUCacheQueue<K, V> implements CacheQueue<K, V> {

  private Cache<K, V> cache;

  public WTFLUCacheQueue(int maxLen) {
    cache = Caffeine.newBuilder().maximumSize(maxLen).build();
  }

  @Override public V get(K key, boolean refresh) {
    V res = cache.getIfPresent(key);
    if (refresh && res != null) {
      cache.put(key, res);
    }
    return res;
  }

  @Override public Tuple<K, V> put(K key, V val) {
    cache.put(key, val);
    return null;
  }
}
