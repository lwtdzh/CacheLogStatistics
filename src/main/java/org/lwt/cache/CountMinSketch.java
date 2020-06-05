package org.lwt.cache;

public class CountMinSketch extends Sketch {
  private int threshold; // When should all counters be divided by 2.

  public CountMinSketch(int d, int w, int threshold) {
    super(d, w);
    this.threshold = threshold;
  }

  @Override
  public boolean update(String key) {
    try {
      boolean shouldDivide = false;
      for (int i = 0; i < d; ++i) {
        int res = (++t[i][calcHash(key, i)]);
        if (res > threshold) {
          shouldDivide = true;
        }
      }
      if (shouldDivide) {
        divide();
      }
    } catch (InvalidHashFunctionIndexException e) {
      return false;
    }
    return true;
  }

  @Override
  public int estimate(String key) {
    return 0;
  }

  /**
   * Divide all counters by 2.
   */
  private void divide() {
    for (int i = 0; i < d; ++i) {
      for (int j = 0; j < w; ++j) {
        t[i][j] = t[i][j] >> 1;
      }
    }
  }
}
