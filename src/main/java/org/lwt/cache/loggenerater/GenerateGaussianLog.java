package org.lwt.cache.loggenerater;

import org.lwt.cache.Request;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Random;

public class GenerateGaussianLog {
  private static final String filePath = "/Users/lwtdzh/Desktop/generate_log_gaussian_2GB";
  private static final int fileNum = 10;
  private static final int fileReadNum = (int)3e7;
  private static final int readMaxBytes = 20 * 1000;
  private static final Random r = new Random();

  public static void main(String[] args) {
    try {
      FileOutputStream fs = new FileOutputStream(filePath);
      BufferedOutputStream bos = new BufferedOutputStream(fs);

      for (int i = 0; i < fileReadNum; ++i) {
        String fileName = generateFileName();
        long offset = Math.abs((long)(1024L * 1024L * 2L * r.nextGaussian()));
        int readLen = Math.abs((int)(readMaxBytes * r.nextFloat()));
        readLen = readLen == 0 ? 1 : readLen;
        long fileLen = 50000000;
        long timeStamp = 0;
        String readType = Request.READ_TYPE_POSITIONAL;
        bos.write(String.format("%s|%d|%d|%d|%d|%s\n", fileName, offset,
            readLen, fileLen, timeStamp, readType).getBytes());
        if (i % 1e5 == 0) {
          System.out.println("Printed num: " + i + ".");
        }
      }
      bos.flush();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  private static String generateFileName() {
    return "File: " + String.valueOf((int)(r.nextFloat() * fileNum));
  }
}
