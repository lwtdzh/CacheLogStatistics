package org.lwt.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

class Analyser {
  // Configurations.
  private static long maxReadTimes = Long.MAX_VALUE;
  private static int blockSize = 300;
  private static int readNoCacheCostPerUnit = 100;
  private static int readCacheCostPerUnit = 5;
  private static int cacheUpdateCostPerTime = 1;
  private static String filePath = "./logfile";
  private static long cacheSize = 1000;
  private static int ignoreLengthSmallerThan = 10;
  private static int ignoreSequentialRead = 0;

  // Inner used objects.
  private static CacheStrategy cs;
  private static RequestMapper rm;
  private static RequestStream rs;
  private static Counter count;

  private static InputStream open(String path) {
    try {
      InputStream is = new FileInputStream(path);
      return is;
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  private static void readConf(String[] args) {
    InputStream is = null;
    for (int i = 0; i < args.length && (is = open(args[i])) == null; ++i) {}
    if (is == null) {
      return;
    }
    Tools.InputStreamLineReader sc = new Tools.InputStreamLineReader(is);
    String line = null;
    while ((line = sc.nextLine()) != null) {
      line = line.trim();
      if (line.length() >= 1 && line.charAt(0) == '#') {
        // Is an annotation line.
        continue;
      }
      String[] splLine = line.split("=");
      if (splLine.length < 2) {
        continue;
      }
      String key = splLine[0].trim();
      String val = splLine[1].trim();
      if ("maxReadTimes".equals(key)) {
        maxReadTimes = Long.valueOf(val);
      } else if ("blockSize".equals(key)) {
        blockSize = Integer.valueOf(val);
      } else if ("readNoCacheCostPerUnit".equals(key)) {
        readNoCacheCostPerUnit = Integer.valueOf(val);
      } else if ("readCacheCostPerUnit".equals(key)) {
        readCacheCostPerUnit = Integer.valueOf(val);
      } else if ("cacheUpdateCostPerTime".equals(key)) {
        cacheUpdateCostPerTime = Integer.valueOf(val);
      } else if ("filePath".equals(key)) {
        filePath = val;
      } else if ("cacheSize".equals(key)) {
        cacheSize = Long.valueOf(val);
      } else if ("ignoreLengthSmallerThan".equals(key)) {
        ignoreLengthSmallerThan = Integer.valueOf(val);
      } else if ("ignoreSequentialRead".equals(key)) {
        ignoreSequentialRead = Integer.valueOf(val);
      } else {
        System.out.println("Undefined configuration entry: " + key + ".");
      }
    }
    try {
      is.close();
    } catch (IOException e) {
      System.out.println("IOException in Conf File.");
    }
  }

  private static void printConf() {
    System.out.println("maxReadTimes: " + maxReadTimes + ".");
    System.out.println("blockSize: " + blockSize + ".");
    System.out.println("readNoCacheCostPerUnit: " + readNoCacheCostPerUnit + ".");
    System.out.println("readCacheCostPerUnit: " + readCacheCostPerUnit + ".");
    System.out.println("cacheUpdateCostPerTime: " + cacheUpdateCostPerTime + ".");
    System.out.println("filePath: " + filePath + ".");
    System.out.println("cacheSize: " + cacheSize + ".");
    System.out.println("ignoreLengthSmallerThan: " + ignoreLengthSmallerThan + ".");
    System.out.println("ignoreSequentialRead: " + ignoreSequentialRead + ".");
  }

  public static void main(String[] args) {
    readConf(args);
    printConf();

    rm = new SimpleRequestMapper(blockSize);
    rs = new SimpleRequestStream();
    cs = new SketchLRUCacheStrategy();
    count = new Counter(readNoCacheCostPerUnit,
        readCacheCostPerUnit, cacheUpdateCostPerTime);
    rs.open(filePath);

    Request req = null;
    long readTimes = 0;
    while ((req = rs.nextReq()) != null && readTimes < maxReadTimes) {
      if (req.getFileLen() < ignoreLengthSmallerThan) {
        continue;
      }
      if (ignoreSequentialRead > 0 &&
          req.getReadType().equals(Request.READ_TYPE_SEQUENTIAL)) {
        continue;
      }

      ++readTimes;
      if (readTimes % 10000 == 0) {
        System.out.println("Read count: " + readTimes + ".");
      }

      List<Tuple<Object, Integer>> keys = rm.mapRequest(req);
      if (keys == null) {
        System.out.println("Found Empty Key.");
        --readTimes;
        continue;
      }

      for (int i = 0; i < keys.size(); ++i) {
        boolean cached = cs.notify(keys.get(i).a) == 1 ? true : false;
        count.record(keys.get(i).b, cached);
      }
    }

    rs.close();

    System.out.println("\n\n\n\n");
    System.out.println("----Result----");
    System.out.println("Total Read Requests Count: " + readTimes + ".");
    System.out.println("Total Read Blocks Count: " + count.getRecordCount() + ".");
    System.out.println("Total Hit Cache Count: " + count.getHitCacheCount() + ".");
    System.out.println("Cache Hit Rate: " + count.getCacheHitRatio() + ".");
    System.out.println("Total Read Lens: " + count.getReadLens() + ".");
    System.out.println("Total Hit Lens: " + count.getHitLens() + ".");
    System.out.println("Total Cost if No Cache: " + count.getCostIfNoCache() + ".");
    System.out.println("Total Cost if Using Cache: " + count.getCostIfCache() + ".");
    System.out.println("Using Cache Save Cost: " +
        (1.0d - (double)count.getCostIfCache() / count.getCostIfNoCache()) + ".");
    System.out.println("\n\n\n------------");
    cs.printRes();
  }
}