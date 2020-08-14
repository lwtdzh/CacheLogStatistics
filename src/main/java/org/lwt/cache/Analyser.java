package org.lwt.cache;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class Analyser implements Runnable {
  // Configurations.
  private static int threadMaxNum = 12;
  private static long maxReadTimes = Long.MAX_VALUE;
  private static int blockSize = 300;
  private static int readNoCacheCostPerUnit = 100;
  private static int readCacheCostPerUnit = 5;
  private static int cacheUpdateCostPerTime = 1;
  private static String filePath = "./logfile";
  private static long cacheSize = 1000;
  private static int ignoreLengthSmallerThan = 10;
  private static int ignoreSequentialRead = 0;
  private static AtomicInteger threadNum = new AtomicInteger(0);

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

  public Map<String, Object> main2(Map<String, Object> params) {
    // printConf();

    // Inner used objects.
    CacheStrategy cs;
    RequestMapper rm;

    Counter count;
    rm = new SimpleRequestMapper(blockSize);

    cs = new SketchCacheStrategy((Integer)params.get("d"),
        (Integer)params.get("w"), (Integer)params.get("cacheMaxLen"),
        (Integer)params.get("divThreshold"), (Integer)params.get("putThreshold"),
        "WTINYFLU");
//    cs = new WTinyFLUCacheStrategy(cacheMaxLen);
    count = new Counter(readNoCacheCostPerUnit,
        readCacheCostPerUnit, cacheUpdateCostPerTime);

    long noCacheReadTimes = 0;
    long cacheReadTimes = 0;
    long noCacheReadBytes = 0;
    long cacheReadBytes = 0;
    long writeCacheTimes = 0;

    long readTimes = 0;
    for (Request req : reqs) {
      if (readTimes > maxReadTimes) {
        System.out.println("Read times overflow.");
        break;
      }
      if (req.getFileLen() < ignoreLengthSmallerThan) {
        continue;
      }
      if (ignoreSequentialRead > 0 &&
          req.getReadType().equals(Request.READ_TYPE_SEQUENTIAL)) {
        continue;
      }

      ++readTimes;
//      if (readTimes % 10000 == 0) {
//        System.out.println("Read count: " + readTimes + ".");
//      }

      List<Tuple<Object, Integer>> keys = rm.mapRequest(req);
      if (keys == null) {
        System.out.println("Found Empty Key.");
        --readTimes;
        continue;
      }

      noCacheReadBytes += req.getReadLen();
      noCacheReadTimes += 1;
      boolean allCached = true;
      for (int i = 0; i < keys.size(); ++i) {
        int ret = cs.notify(keys.get(i).a);
        if (ret == 0) { // No cache, do not put into cache.
          count.record(keys.get(i).b, false);
          cacheReadBytes += keys.get(i).b;
          allCached = false;
        } else if (ret == 1) { // No cache, put it into cache.
          count.record(keys.get(i).b, false);
          cacheReadBytes += blockSize;
          allCached = false;
          ++writeCacheTimes;
        } else { // cached.
          count.record(keys.get(i).b, true);
          cacheReadBytes += keys.get(i).b;
        }
      }
      if (!allCached) {
        ++cacheReadTimes;
      } else {
        cacheReadBytes -= req.getReadLen();
      }
    } //  End While.


//    System.out.println("\n\n\n\n");
//    System.out.println("----Result----");
//    System.out.println("Total Read Requests Count: " + readTimes + ".");
//    System.out.println("Total Read Blocks Count: " + count.getRecordCount() + ".");
//    System.out.println("Total Hit Cache Count: " + count.getHitCacheCount() + ".");
//    System.out.println("Cache Hit Rate: " + count.getCacheHitRatio() + ".");
//    System.out.println("Total Read Lens: " + count.getReadLens() + ".");
//    System.out.println("Total Hit Lens: " + count.getHitLens() + ".");
//    System.out.println("Total Cost if No Cache: " + count.getCostIfNoCache() + ".");
//    System.out.println("Total Cost if Using Cache: " + count.getCostIfCache() + ".");
//    System.out.println("Using Cache Save Cost: " +
//        (1.0d - (double)count.getCostIfCache() / count.getCostIfNoCache()) + ".");
//    System.out.println("Cache Read Bytes: " + cacheReadBytes + ".");
//    System.out.println("No Cache Read Bytes: " + noCacheReadBytes + ".");
//    System.out.println("No / YES Bytes: "+ (double)noCacheReadBytes / cacheReadBytes + ".");
//    System.out.println("Cache Read Times: " + cacheReadTimes + ".");
//    System.out.println("No Cache Read Times: " + noCacheReadTimes + ".");
//    System.out.println("No / YES Times: " + (double)noCacheReadTimes / cacheReadTimes + ".");
//    System.out.println("\n\n\n------------");
    int[] csres = cs.printRes();
    Map<String, Object> res = new HashMap<String, Object>();
    res.put("cacheHitRate", count.getCacheHitRatio());
    res.put("readTimesAfterFirstCache", csres[0]);
    res.put("cacheTimesAfterFirstCache", csres[1]);
    res.put("elimTimesAfterFirstCache", csres[2]);
    res.put("no/yesBytes", (double)noCacheReadBytes / cacheReadBytes);
    res.put("no/yesTimes", (double)noCacheReadTimes / cacheReadTimes);
    res.put("writeCacheTimes", (int)writeCacheTimes);
    return res;
  }

  public static void main(String[] args) {
    readConf(args);
    try {
      RequestStream rs = new SimpleRequestStream();
      rs.open(filePath);
      List<Request> reqs = new ArrayList<Request>(10000000);
      Request nextReq = rs.nextReq();
      long readNum = 0;
      while (nextReq != null) {
        if (ignoreSequentialRead < 0 || nextReq.getReadType() != Request.READ_TYPE_SEQUENTIAL) {
          if (ignoreLengthSmallerThan <= nextReq.getFileLen()) {
            reqs.add(nextReq);
            ++readNum;
          }
        }
        nextReq = rs.nextReq();
        if (readNum % 10000 == 0) {
          System.out.println("Read Lines: " + readNum + ".");
        }
      }
      FileOutputStream fos = new FileOutputStream("/Users/lwtdzh/Desktop/rloganaout.txt");
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      List<Thread> threadList = new ArrayList<Thread>();
      for (int lruLen = 500; lruLen <= 2000; lruLen += 500) {
        for (int divThreshold = 2; divThreshold <= 2; divThreshold += 3) {
          for (int putThreshold = 1; putThreshold <= 1; putThreshold += 1) {
            if (divThreshold < putThreshold) {
              continue;
            }
             Thread t =new Thread(new Analyser(lruLen, divThreshold, putThreshold, bos, args, reqs));
            t.start();
            threadList.add(t);
             while (threadNum.get() >= threadMaxNum) {
               Thread.sleep(2000);
             }
          } // putThreshold.
        } // divThreshold.
      } // lruLen.
      for (Thread t : threadList) {
        t.join();
      }
//      Thread t = new Thread(new Analyser(1500, 1, 1, bos, args, reqs));
//      Thread t2 = new Thread(new Analyser(2000, 1, 1, bos, args, reqs));
//      t.start();
//      t.join();
//      t2.start();
//      t2.join();

      // User modified params.
//     List<int[]> p = new ArrayList<int[]>();
//     int[] a1 = {500, 2, 2}; p.add(a1);
//      int[] a2 = {500, 5, 2}; p.add(a2);
//      int[] a3 = {500, 5, 3}; p.add(a3);
//      int[] a4 = {500, 5, 4}; p.add(a4);
//      int[] a5 = {500, 5, 5}; p.add(a5);
//      int[] a6 = {500, 11, 2}; p.add(a6);
//      for (int j = 2; j <=8 ;++j) {
//        int[] aaa = {500, 8 , j};
//        p.add(aaa);
//      }
//     List<Thread> tc = new ArrayList<Thread>();
//     for (int[] curP : p) {
//       Thread t = new Thread(new Analyser(curP[0], curP[1], curP[2], bos, args, reqs));
//       t.start();
//       tc.add(t);
//     }
//     for (Thread curT : tc) {
//       curT.join();
//     }
      bos.flush();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }


  int cacheMaxLen;
  int divThreshold;
  int putThreshold;
  BufferedOutputStream bos;
  String[] args;
  List<Request> reqs;

  public Analyser(int cacheMaxLen, int divThreshold, int putThreshold,
      BufferedOutputStream bos, String[] args, List<Request> reqs) {
    this.cacheMaxLen = cacheMaxLen;
    this.divThreshold = divThreshold;
    this.putThreshold = putThreshold;
    this.bos = bos;
    this.args = args;
    this.reqs = reqs;
  }

  @Override public void run() {
    threadNum.incrementAndGet();
    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("d", 4);
      params.put("w", 10000);
      System.out.println(String.valueOf(cacheMaxLen) + " " + divThreshold + " " + putThreshold);
      params.put("putThreshold", putThreshold);
      params.put("divThreshold", divThreshold);
      params.put("cacheMaxLen", cacheMaxLen);
      Map<String, Object> res = main2(params);
      double cacheHitRate = (Double) res.get("cacheHitRate");
//      int readTimesAfterFirstCache = (Integer) res.get("readTimesAfterFirstCache");
      int cacheTimesAfterFirstCache = (Integer) res.get("cacheTimesAfterFirstCache");
      int elimTimesAfterFirstCache = (Integer) res.get("elimTimesAfterFirstCache");
      double noYesBytes = (Double) res.get("no/yesBytes");
      double noYesTimes = (Double) res.get("no/yesTimes");
      int writeCacheTimes = (Integer)res.get("writeCacheTimes");
      synchronized (bos) {
        bos.write(Tools.convertBytes(cacheMaxLen * blockSize).getBytes());
        bos.write(" ".getBytes());
        bos.write(String.valueOf(divThreshold).getBytes());
        bos.write(" ".getBytes());
        bos.write(String.valueOf(putThreshold).getBytes());
        bos.write(" ".getBytes());
        bos.write(String.format("%.4f", cacheHitRate).getBytes());
//        bos.write(" ".getBytes());
//        bos.write(String.valueOf(readTimesAfterFirstCache).getBytes());
        bos.write(" ".getBytes());
        bos.write(String.valueOf(cacheTimesAfterFirstCache).getBytes());
        bos.write(" ".getBytes());
        bos.write(String.valueOf(writeCacheTimes).getBytes());
        bos.write(" ".getBytes());
        bos.write(String.format("%.4f", noYesBytes).getBytes());
        bos.write(" ".getBytes());
        bos.write(String.format("%.4f", noYesTimes).getBytes());
        bos.write("\n".getBytes());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    threadNum.decrementAndGet();
  }
}