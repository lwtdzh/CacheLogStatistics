package org.lwt.cache;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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

  public static class LRU<K, V> implements CacheQueue<K, V> {
    // Class Node.
    private static class Node<K, V> {
      public Node(K key, V val) {
        this.key = key;
        this.val = val;
      }
      public K key;
      public V val;
      public Node<K, V> next = null;
      public Node<K, V> pre = null;
    } // End class Node.

    private Map<K, Node<K, V>> map = new HashMap<K, Node<K, V>>();
    private Node<K, V> head = null;
    private Node<K, V> tail = null;
    private int maxLen;

    /**
     * Construct.
     * @param maxLen
     */
    public LRU(int maxLen) {
      this.maxLen = maxLen;
    }

    public int length() {
      return map.size();
    }

    public void changeCapacity(int maxLen) {
      this.maxLen = maxLen;
    }

    /**
     * PUT.
     * @param key
     * @param val
     * @return The eliminated value. Or null if no element was eliminated.
     */
    public Tuple<K, V> put(K key, V val) {
      if (maxLen <= 0) {
        return null;
      }

      // Already existing node. Move it to head.
      Node<K, V> newNode = map.get(key);
      if (newNode != null) {
        moveToHead(newNode);
        return null;
      }

      Tuple<K, V> eliminated = null;

      // If Full.
      if (map.size() >= maxLen) {
        eliminated = eliminateTail();
      }

      newNode = new Node<K, V>(key, val);
      newNode.next = head;
      if (head != null) {
        head.pre = newNode;
      }
      head = newNode;
      map.put(key, head);
      // If it is the only element.
      if (tail == null) {
        tail = head;
      }
      return eliminated;
    }

    /**
     * GET.
     * @param key
     * @param refresh If true, move this key to the head of the LRU queue.
     * @return Null if no such key.
     */
    public V get(K key, boolean refresh) {
      Node<K, V> node = map.get(key);
      if (node == null) {
        return null;
      }
      if (refresh) {
        moveToHead(node);
      }
      return node.val;
    }

    /**
     *
     * @return The eliminated value.
     */
    private Tuple<K, V> eliminateTail() {
      if (head == null || tail == null) {
        // Empty queue.
        return null;
      }
      Node<K, V> eliminated = tail;
      if (tail == head) {
        // The only element.
        tail = null;
        head = null;
      } else {
        tail = tail.pre;
        tail.next = null;
      }
      map.remove(eliminated.key);
      return new Tuple<K, V>(eliminated.key, eliminated.val);
    }

    /**
     *
     * @param node
     */
    private void moveToHead(Node<K, V> node) {
      if (node == null || node == head) {
        return;
      }
      node.pre.next = node.next;
      if (node.next != null) {
        node.next.pre = node.pre;
      }
      head.pre = node;
      node.next = head;
      node.pre = null;
      head = node;
    }

  }

  public static String convertBytes(long bytes) {
    double data = bytes;
    int unit = 0; // 0: B, 1: KB, 2: MB, 3: GB, 4: TB.
    while (unit <= 3) {
      if (data > 1024.0d) {
        data /= 1024.0d;
        ++unit;
      } else {
        break;
      }
    }
    String unitStr = null;
    switch (unit) {
    case 0:
      unitStr = "B";
      break;
    case 1:
      unitStr = "KB";
      break;
    case 2:
      unitStr = "MB";
      break;
    case 3:
      unitStr = "GB";
      break;
    case 4:
      unitStr = "TB";
      break;
    }
    return String.format("%.2f", data) + unitStr;
  }
}
