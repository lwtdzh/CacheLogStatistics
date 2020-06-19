package org.lwt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

class A {
  void ss() {
    System.out.println(1);
  }
  void sss() {
    ss();
  }
}

class B extends A {
  @Override void ss() {
    System.out.println(2);
  }
}

public class TestDraft {
  public static void main(String[] args) {
    A a = new B();
    a.sss();
  }
}
