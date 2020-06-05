package org.lwt.cache;

import java.io.InputStream;

/**
 * The interface provides a possibility to retrieve many requests from a file or
 * an InputStream object.
 */
interface RequestStream {
  boolean open(String str);
  boolean open(InputStream is);

  /**
   * Return null means the input has been completely read.
   * If dirty data occurred, this method must skip it and return the next
   * legal request, it shouldn't return null, since null means completion.
   */
  Request nextReq();
  void close();
}