package org.lwt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Random;

public class Test {

  public static void main(String[] args) {
    Random r = new Random();
    for (int i = 0; i < 40; ++i) {
      System.out.println((long)(1024L * 1024L * 1024L * 20L * r.nextGaussian()));
    }
  }
}
