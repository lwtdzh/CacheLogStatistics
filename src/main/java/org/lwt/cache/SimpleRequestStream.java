package org.lwt.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class SimpleRequestStream implements RequestStream {

  private InputStream is;
  private Scanner sc;

  public SimpleRequestStream() {
    is = null;
    sc = null;
  }

  public SimpleRequestStream(InputStream is) {
    open(is);
  }

  public SimpleRequestStream(String str) {
    open(str);
  }

  @Override
  public boolean open(String str) {
    if (str == null) {
      return false;
    }
    try {
      is = new FileInputStream(str);
    } catch (FileNotFoundException e) {
      is = null;
      System.out.println("No Such File: " + str + ".");
      return false;
    }
    sc = new Scanner(is);
    return true;
  }

  @Override
  public boolean open(InputStream is) {
    if (is == null) {
      return false;
    }
    this.is = is;
    sc = new Scanner(this.is);
    return true;
  }

  @Override
  public Request nextReq() {
    if (sc == null || is == null) {
      return null;
    }
    try {
      String[] reqStr = sc.nextLine().split("\\|");
      if (reqStr == null || reqStr.length != 6) {
        return nextReq();
      }
      String fileName = reqStr[0];
      long offset = Long.valueOf(reqStr[1]);
      int readLen = Integer.valueOf(reqStr[2]);
      long fileLen = Long.valueOf(reqStr[3]);
      long timeStamp = Long.valueOf(reqStr[4]);
      String readType = reqStr[5];
      return new Request(fileName, offset, readLen,
          fileLen, timeStamp, readType);
    } catch (NoSuchElementException e) {
      return null;
    } catch (IllegalStateException e) {
      return null;
    }
  }

  @Override
  public void close() {
    if (is == null) {
      return;
    }
    try {
      is.close();
    } catch (IOException e) {
      System.out.println("IOException.");
    } finally {
      is = null;
      sc = null;
    }
  }
}
