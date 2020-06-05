package org.lwt.cache;

interface CacheStrategy {
  /**
   * Tell the cache a new key has come.
   * The cache will try to check if the key is in cache.
   * The cache will also update itself by this key.
   * @param key The key.
   * @return -1 This key is not in cache.
   *          1 This key is in cache.
   */
  int notify(Object key);
}