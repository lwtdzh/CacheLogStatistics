package org.lwt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class TestDraft {
  public static void main(String[] args) {
    Cache<Object, Boolean> cache = Caffeine.newBuilder().maximumSize(11).build();
    for (int i = 0; i < 5000; ++i) {
      cache.put(String.valueOf(i), true);
    }
    int num = 0;
    for (int i = 0; i < 5000; ++i) {
      int c = cache.getIfPresent(String.valueOf(i)) != null ? ++num: num;
    }
    System.out.println(num);
  }
}
