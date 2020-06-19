package org.lwt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import sun.swing.plaf.synth.DefaultSynthStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class WTinyFLUCacheStrategy implements CacheStrategy {

  private class StatInfo {
    public int totalReadTimes = 0;
    public int cacheHitTimes = 0;
  }
  Cache<Object, StatInfo> cache;
  Map<String, StatInfo> cacheRecords;
  StatisticsRemovalListener rl;

  public WTinyFLUCacheStrategy(long maxSize) {
    rl = new StatisticsRemovalListener();
    cache = Caffeine.newBuilder().maximumSize(maxSize).removalListener(rl).build();
    cacheRecords = new HashMap<String, StatInfo>();
  }

  @Override
  public int notify(Object key) {
    int ret = 2;
    StatInfo cacheInfo = cacheRecords.get(key);
    if (cacheInfo != null) {
      ++cacheInfo.totalReadTimes;
    } else {
      cacheInfo = new StatInfo();
      cacheRecords.put((String)key, cacheInfo);
    }

    StatInfo resCache = cache.getIfPresent(key);
    if (resCache == null) { // Not in cache.
      ret = 1;
    } else {
      ++resCache.cacheHitTimes;
    }

    cache.put(key, cacheInfo);
    return ret;
  }

  @Override public int[] printRes() {
    int cht = 0;
    int trt = 0;
    int rt = (int)rl.getRemovalTimes();
    for (Map.Entry<String, StatInfo> e : cacheRecords.entrySet()) {
      cht += e.getValue().cacheHitTimes;
      trt += e.getValue().totalReadTimes;
    }
    int[] ret = {trt, cht, rt};
    return ret;
  }
}
