package org.lwt.cache;

import javafx.scene.control.Skin;

import java.lang.reflect.Array;
import java.util.List;

public class Div2Sketch extends Sketch {
  private int divThreshold; // Thresh to div all elements by 2.
  private int putThreshold; // Threshold to push a object to cache.
  private int addTimesLastDiv = 0; // How many add actions after last div action.

  protected Div2Sketch(int d, int w, int divThreshold, int putThreshold) {
    super(d, w);
    this.divThreshold = divThreshold;
    this.putThreshold = putThreshold;
  }

  @Override public boolean update(String key) throws InvalidHashFunctionIndexException {
    boolean put = true;
    ++addTimesLastDiv;
    List<Integer> list = add(key);
    for (int i : list) {
      if (i < putThreshold) {
        put = false;
        break;
      }
    }
    if (addTimesLastDiv >= divThreshold) {
      doDiv();
      addTimesLastDiv = 0;
    }
    return put;
  }

  private void doDiv() {
    for (int i = 0; i < d; ++i) {
      for (int j = 0; j < w; ++j) {
        t[i][j] = t[i][j] >> 1;
      }
    }
  }
}
