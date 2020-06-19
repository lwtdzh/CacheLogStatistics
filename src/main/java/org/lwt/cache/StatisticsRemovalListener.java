package org.lwt.cache;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StatisticsRemovalListener implements RemovalListener {
  private long removalTimes = 0;

  @Override public void onRemoval(@Nullable Object o, @Nullable Object o2,
      @NonNull RemovalCause removalCause) {
    if (o != null && o2 != null) {
      ++removalTimes;
    }
  }

  public long getRemovalTimes() {
    return this.removalTimes;
  }
}
