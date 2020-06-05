package org.lwt.cache;

public class Request {
  public static final String READ_TYPE_SEQUENTIAL = "READ_TYPE_SEQUENTIONAL";
  public static final String READ_TYPE_POSITIONAL = "READ_TYPE_POSITIONAL";
  public static final String READ_TYPE_UNDEFINED = "READ_TYPE_UNDEFINED";

  private String fileName;
  private long offset;
  private int readLen;
  private long fileLen;
  private long timeStamp;
  private String readType;

  public Request(String fileName, long offset, int readLen,
      long fileLen, long timeStamp, String readType) {
    this.fileName = fileName;
    this.offset = offset;
    this.readLen = readLen;
    this.fileLen = fileLen;
    this.timeStamp = timeStamp;
    this.readType = readType;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public int getReadLen() {
    return readLen;
  }

  public void setReadLen(int readLen) {
    this.readLen = readLen;
  }

  public long getFileLen() {
    return fileLen;
  }

  public void setFileLen(long fileLen) {
    this.fileLen = fileLen;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getReadType() {
    return readType;
  }

  public void setReadType(String readType) {
    this.readType = readType;
  }
}
