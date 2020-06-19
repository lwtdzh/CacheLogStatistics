package org.lwt.cache;

public interface CacheQueue<K, V> {

  /**
   *
   * @param key
   * @param refresh If update the key to the cache head.
   * @return
   */
  V get(K key, boolean refresh);

  /**
   *
   * @param key
   * @param val
   * @return The eliminated element. Null if no element was eliminated.
   */
  Tuple<K, V> put(K key, V val);
}
