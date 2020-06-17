package org.lwt.cache;

import sun.swing.plaf.synth.DefaultSynthStyle;

import java.util.HashMap;
import java.util.Map;

public class SketchLRUCacheStrategy implements CacheStrategy {
  private static final int d = 4;
  private static final int w = 10000;
  private static final int lruLen = 500;
  private static final int divThreshold = 7;
  private static final int putThreshold = 3;

  private class StatInfo {
    public int totalReadTimes = 0;
    public int cacheHitTimes = 0;
    public int cacheElimTimes = 0;
  }

  Tools.LRU<String, StatInfo> lru = new Tools.LRU<String, StatInfo>(lruLen);
  Sketch ske = new Div2Sketch(d, w, divThreshold, putThreshold);
  Map<String, StatInfo> map = new HashMap<String, StatInfo>();

  @Override
  public int notify(Object key) {
    boolean hit = false;
    String k = (String)key;
    StatInfo si = map.get(k);
    if (si != null) {
      ++si.totalReadTimes;
    }

    try {
      if (lru.get(k, true) != null) {
        ++si.cacheHitTimes;
        hit = true;
      }

      if (ske.update(k)) {
        if (si == null) {
          si = new StatInfo();
          map.put(k, si);
        }

        if (!hit) {
          Tuple<String, StatInfo> elim = lru.put(k, si);
          if (elim != null) {
            ++elim.b.cacheElimTimes;
          }
        }
      }
    } catch (Sketch.InvalidHashFunctionIndexException e) {
      System.out.println("eeeeeeeeeeeeeeeeeee.");
    }

    return hit ? 1 : -1;
  }

  @Override public void printRes() {
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
    System.out.println("-----");
    System.out.println("readTimesAfterFirstCache: " + rt + ".");
    System.out.println("cacheTimesAfterFirstCache: " + cht + ".");
    System.out.println("elimTimesAfterFirstCache: " + cet + ".");
  }
}
