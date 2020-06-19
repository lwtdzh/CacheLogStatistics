package org.lwt.cache;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class TestLogTotalBlocks {
  static int blockSize = 1000 * 1000;
  static String filePath = "/Users/lwtdzh/Desktop/generate_log_gaussian_2GB";
  static Set<String> set = new HashSet<>();

  public static void main(String[] args) {
    try {
      FileInputStream fs = new FileInputStream(filePath);
      BufferedInputStream bis = new BufferedInputStream(fs);
      Scanner sc = new Scanner(bis);

      int lineNum = 0;

      try {
        String line = sc.nextLine();
        while (line != null) {

          String[] lines = line.split("\\|");
          String blockName = lines[0] + getBlockNum(Long.valueOf(lines[1]));
          set.add(blockName);

          line = sc.nextLine();

          ++lineNum;
          if (lineNum % 100000 == 0) {
            System.out.println("Lines: " + lineNum + ".");
          }
        }
      } catch (NoSuchElementException e) {}

      sc.close();
      bis.close();
      fs.close();
    } catch (Exception e) {
      System.out.println("Exception.");
      e.printStackTrace();
    }

    System.out.println("Total blocks: " + set.size() + ".");
  }

  static long getBlockNum(long offset) {
    return offset / blockSize;
  }
}
