package org.lwt.cache;

import java.util.Objects;

public class Tuple<A, B> {

  public A a;
  public B b;

  public Tuple(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public Tuple(Tuple<A, B> rhs) {
    this.a = rhs.a;
    this.b = rhs.b;
  }

  public Tuple() {
    this.a = null;
    this.b = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Tuple<?, ?> tuple = (Tuple<?, ?>) o;
    return Objects.equals(a, tuple.a) && Objects.equals(b, tuple.b);
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b);
  }
}
