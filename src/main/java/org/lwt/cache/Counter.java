package org.lwt.cache;

public class Counter {
  private final int readNoCacheCostPerUnit;
  private final int readCacheCostPerUnit;
  private final int cacheUpdateCostPerTime;

  private long recordCount;
  private long cachedRecordCount;
  private long costIfNoCache;
  private long costIfCache;

  private long readLens;
  private long hitLens;

  public Counter(int readNoCacheCostPerUnit,
      int readCacheCostPerUnit, int cacheUpdateCostPerTime) {
    this.readNoCacheCostPerUnit = readNoCacheCostPerUnit;
    this.readCacheCostPerUnit = readCacheCostPerUnit;
    this.cacheUpdateCostPerTime = cacheUpdateCostPerTime;
    recordCount = 0;
    cachedRecordCount = 0;
    costIfCache = 0;
    costIfNoCache = 0;
    readLens = 0;
    hitLens = 0;
  }

  public void record(int readLen, boolean ifCached) {
    ++recordCount;
    readLens += readLen;
    costIfNoCache += (readLen * readNoCacheCostPerUnit);
    costIfCache += cacheUpdateCostPerTime;
    if (ifCached) {
      ++cachedRecordCount;
      hitLens += readLen;
      costIfCache += (readLen * readCacheCostPerUnit);
    } else {
      costIfCache += (readLen * readNoCacheCostPerUnit);
    }
  }

  public double getCacheHitRatio() {
    if (recordCount == 0) {
      return 1.0d;
    }
    return (double)cachedRecordCount / recordCount;
  }

  public long getCostIfNoCache() {
    return costIfNoCache;
  }

  public long getCostIfCache() {
    return costIfCache;
  }

  public long getRecordCount() {
    return recordCount;
  }

  public long getHitCacheCount() {
    return cachedRecordCount;
  }

  public long getReadLens() {
    return readLens;
  }

  public long getHitLens() {
    return hitLens;
  }
}
