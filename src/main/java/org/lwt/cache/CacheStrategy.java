package org.lwt.cache;

interface CacheStrategy {
  /**
   * Tell the cache a new key has come.
   * The cache will try to check if the key is in cache.
   * The cache will also update itself by this key.
   * @param key The key.
   * @return 0 No cache, and do not push it to cash.
   *         1 No cache, and push it to cash. (Optional)
   *         2 Cached.
   */
  int notify(Object key);

  int[] printRes();
}