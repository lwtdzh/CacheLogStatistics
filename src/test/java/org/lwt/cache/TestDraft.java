package org.lwt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class TestDraft {
  public static void main(String[] args) {
    int[] a = new int[15];
    for (int i = 0; i < 15; ++i) {
      a[i] = -i;
      System.out.println(a[i] % 5);
    }

  }
}
