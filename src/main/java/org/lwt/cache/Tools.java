package org.lwt.cache;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

public class Tools {
  public static class StringLineReader {
    int curPos;
    String str;
    public StringLineReader(String s) {
      curPos = 0;
      str = s;
    }
    private boolean isLineBreak(char c) {
      return c == '\n' || c == '\r' || c == '\t';
    }
    public String nextLine() {
      if (str == null || str.length() == 0 || curPos >= str.length()) {
        return null;
      }
      while (curPos < str.length() && isLineBreak(str.charAt(curPos))) {
        ++curPos;
      }
      if (curPos >= str.length()) {
        return null;
      }
      int endPos = curPos + 1;
      while (endPos < str.length() && !isLineBreak(str.charAt(endPos))) {
        ++endPos;
      }
      String res = str.substring(curPos, endPos);
      curPos = endPos;
      return res;
    }
  }

  public static class InputStreamLineReader {
    InputStream is;
    Scanner sc;

    public InputStreamLineReader() {
      is = null;
      sc = null;
    }

    public InputStreamLineReader(InputStream is) {
      if (is == null) {
        is = null;
        sc = null;
        return;
      }

      this.is = is;
      this.sc = new Scanner(is);
    }

    public String nextLine() {
      String res = null;
      try {
        res = sc.nextLine();
      } catch (Exception e) {
        res = null;
      }
      return res;
    }
  }
}
