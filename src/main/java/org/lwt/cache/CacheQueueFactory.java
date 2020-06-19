package org.lwt.cache;

public class CacheQueueFactory<K, V> {

  private String cacheType;
  private String params = null;

  public CacheQueueFactory(String cache) {
    String[] cacheS = cache.split(":");
    cacheType = cacheS[0];
    if (cacheS.length > 1) {
      params = cacheS[1];
    }
  }

  public CacheQueue<K, V> build() {
    if (cacheType.equals("LRU")) {
      return new Tools.LRU<K, V>(Integer.valueOf(params));
    }
    if (cacheType.equals("WTINYFLU")) {
      return new WTFLUCacheQueue<K, V>(Integer.valueOf(params));
    }
    return null;
  }

}
