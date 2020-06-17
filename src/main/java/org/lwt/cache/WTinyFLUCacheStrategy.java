package org.lwt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class WTinyFLUCacheStrategy implements CacheStrategy {
  Cache<Object, Boolean> cache;

  public WTinyFLUCacheStrategy(long maxSize) {
    cache = Caffeine.newBuilder().maximumSize(maxSize).build();
  }

  @Override
  public int notify(Object key) {
    int res = cache.getIfPresent(key) != null ? 1 : -1;
    cache.put(key, true);
    return res;
  }

  @Override public void printRes() {
    return;
  }
}
