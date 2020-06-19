package org.lwt.cache;

import java.util.HashMap;
import java.util.Map;

public class SketchCacheStrategy implements CacheStrategy {
  private int d;
  private int w;
  private int cacheMaxLen;
  private int divThreshold;
  private int putThreshold;

  private class StatInfo {
    public int totalReadTimes = 0;
    public int cacheHitTimes = 0;
    public int cacheElimTimes = 0;
  }

  CacheQueue<String, StatInfo> cache;
  Sketch ske;
  Map<String, StatInfo> map;

  public SketchCacheStrategy(int d, int w, int cacheMaxLen,
      int divThreshold, int putThreshold, String cache) {
    this.cache = new CacheQueueFactory<String, StatInfo>(cache + ":" + String.valueOf(cacheMaxLen)).build();
    ske = new Div2Sketch(d, w, divThreshold, putThreshold);
    map = new HashMap<String, StatInfo>();
  }

  /**
   *
   * @param key The key.
   * @return 0 No cache, and do not push it to cash.
   *         1 No cache, and push it to cash.
   *         2 Cached.
   */
  @Override
  public int notify(Object key) {
    int ret = 0;
    String k = (String)key;
    StatInfo si = map.get(k);
    if (si != null) {
      ++si.totalReadTimes;
    }

    try {
      if (cache.get(k, true) != null) {
        ++si.cacheHitTimes;
        ret = 2;
      }

      if (ske.update(k) && ret != 2) {
        if (si == null) {
          si = new StatInfo();
          map.put(k, si);
        }

        Tuple<String, StatInfo> elim = cache.put(k, si);
        if (elim != null) {
          ++elim.b.cacheElimTimes;
        }
        ret = 1;
      }
    } catch (Sketch.InvalidHashFunctionIndexException e) {
      System.out.println("eeeeeeeeeeeeeeeeeee.");
    }

    return ret;
  }

  @Override public int[] printRes() {
    int rt = 0;
    int cht = 0;
    int cet = 0;
    for (Map.Entry<String, StatInfo> e : map.entrySet()) {
//      System.out.print(e.getValue().totalReadTimes);
//      System.out.print(" ");
//      System.out.print(e.getValue().cacheHitTimes);
//      System.out.print(" ");
//      System.out.print(e.getValue().cacheElimTimes);
//      System.out.println();
      rt += e.getValue().totalReadTimes;
      cht += e.getValue().cacheHitTimes;
      cet += e.getValue().cacheElimTimes;
    }
//    System.out.println("-----");
//    System.out.println("readTimesAfterFirstCache: " + rt + ".");
//    System.out.println("cacheTimesAfterFirstCache: " + cht + ".");
//    System.out.println("elimTimesAfterFirstCache: " + cet + ".");
    int[] res = {rt, cht, cet};
    return res;
  }
}
