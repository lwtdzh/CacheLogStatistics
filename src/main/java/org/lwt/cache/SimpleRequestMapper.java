package org.lwt.cache;

import java.util.ArrayList;
import java.util.List;

public class SimpleRequestMapper implements RequestMapper {
  private int blockSize;
  public SimpleRequestMapper(int blockSize) {
    this.blockSize = blockSize;
  }

  @Override
  public List<Tuple<Object, Integer>> mapRequest(Request req) {
    if (blockSize <= 0) {
      return null;
    }

    String fileName = req.getFileName();
    long offset = req.getOffset();
    int readLen = req.getReadLen();

    if (fileName == null) {
      return null;
    }
    if (readLen == 0) {
      return null;
    }

    long tailPos = offset + readLen;
    long curPos = offset;
    List<Tuple<Object, Integer>> list = new ArrayList<Tuple<Object, Integer>>();
    while (curPos < tailPos) {
      long lastPosOfBlock = blockTailPos(curPos);
      String hash = fileName + "-Block-Index-" + getBlockIndex(curPos);
      if (lastPosOfBlock >= tailPos) {
        // This is the last block required to read.
        list.add(new Tuple<Object, Integer>(hash, (int)(tailPos - curPos)));
        break;
      }
      // Read the whole block begin with curPos (Included).
      list.add(new Tuple<Object, Integer>(hash, (int)(lastPosOfBlock - curPos + 1)));
      // Set the pos to the first position of the next block.
      curPos = lastPosOfBlock + 1;
    }

    return list;
  }

  public List<Tuple<Object, Integer>> mapRequest(String reqStr) {
    if (reqStr == null) {
      return null;
    }
    String[] splStr = reqStr.split("|");
    if (splStr.length != 6) {
      return null;
    }

    String fileName = splStr[0];
    long offset = Long.valueOf(splStr[1]);
    int readLen = Integer.valueOf(splStr[2]);
    long fileLen = Long.valueOf(splStr[3]);
    long timeStamp = Long.valueOf(splStr[4]);
    String readType = splStr[5];

    return mapRequest(new Request(fileName, offset, readLen,
        fileLen, timeStamp, readType));
  }

  /**
   * Calculate the block tail position of the block containing <code>pos</code>.
   * @param pos Offset position.
   * @return The last position of the block containing <code>pos</code>.
   */
  private long blockTailPos(long pos) {
    if (blockSize == 0) {
      return -1;
    }
    return (pos / blockSize + 1) * blockSize - 1;
  }

  /**
   * Return which block is the <code>pos</code> belongs to.
   * Index starts from 0.
   */
  private long getBlockIndex(long pos) {
    if (blockSize == 0) {
      return -1;
    }
    return (pos / blockSize);
  }

  private long generateHash(String key) {
    long arraySize = 92233720368547757L; // A prime number.
    long hashCode = 0;
    for (int i = 0; i < key.length(); ++i) {
      int letterValue = key.charAt(i) - 96;
      hashCode = ((hashCode << 5) + letterValue) % arraySize;
    }
    return hashCode;
  }
}
