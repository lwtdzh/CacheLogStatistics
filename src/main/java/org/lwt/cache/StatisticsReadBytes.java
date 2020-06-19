package org.lwt.cache;

import java.io.FileInputStream;
import java.util.Scanner;

import static org.lwt.cache.Tools.convertBytes;

public class StatisticsReadBytes {
  static long seqLen = 0;
  static long posLen = 0;

  public static void main(String[] args) {

    System.out.println("File path: ");
    String path = new Scanner(System.in).nextLine();
    try {
      int times = 0;
      RequestStream rs = new SimpleRequestStream(path);
      Request req = rs.nextReq();
      while (req != null) {
        if (req.getFileLen() < 16000000) {
          req = rs.nextReq();
          continue;
        }

        ++times;
        if (req.getReadType().equals(Request.READ_TYPE_SEQUENTIAL)) {
          seqLen += req.getReadLen();
        } else {
          posLen += req.getReadLen();
        }
        if (times % 10000 == 0) {
          System.out.println("Read Lines: " + times + ".");
        }
        req = rs.nextReq();
      }
      rs.close();
    } catch (Exception e) {
      System.out.println("Exception,");
      e.printStackTrace();
    }

    System.out.println(posLen);
    System.out.println("SeqBytes: " + convertBytes(seqLen) + ".");
    System.out.println("PosBytes: " + convertBytes(posLen) + ".");
    System.out.println("TotalBytes: " + convertBytes(seqLen + posLen) + ".");
  }

}
