package org.lwt.cache;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Sketch is a two-dimensional array T[i][j] with d rows, i = 1,....,d, and w columns,
 * j = 1,......w. Each of the d * w array buckets is a counter that is initially set
 * to zero. The rows of T are associated with d pairwise independent hash functions:
 * h1,....,hd from keys to 1,.....w.
 * Sketch supports following procedures:
 * 1. Update procedure. An incoming key Kt is hashed with each of the hash functions
 * h1,....,hd, and the value Ut is added to the counters T[i][hi(Kt)], i = 1,....d.
 * 2. Estimation Procedure. Given a query for key kt, the sketch returns the minimum
 * of the counters to which kt hashes, so that the estimated count for a key
 * kt becomes min(T[i][hi(Kt)]), i = 1,.....d.
 * The difference of different types of Sketch is that they have different value
 * calculate method for Ut. And they decrease the counter on regular basis with
 * different kinds of strategies.
 * For more detailed introduction of Sketch, see <reference>Xenofontas Dimitropoulos,
 * Marc Stoecklin, Paul Hurley, and Andreas Kind. The eternal sunshine of the sketch
 * data structure. Comput. Netw., 52(17):3248–3257, December 2008.</reference>
 */
public abstract class Sketch {
  public class InvalidHashFunctionIndexException extends Exception {
    public InvalidHashFunctionIndexException(String msg) {
      super(msg);
    }
  }

  protected static final int HASH_JAVA = 0;
  protected static final int HASH_ROTATE = 1;
  protected static final int HASH_ONEBYONE = 2;
  protected static final int HASH_BERNSTEIN = 3;
  protected static final int HASH_JS = 4;
  protected static final int HASH_FNV = 5;
  protected static final int HASH_REFNV = 6;
  protected static final int HASH_RS = 7;
  protected static final int HASH_PJW = 8;
  protected static final int HASH_ELF = 9;
  protected static final int HASH_ADDITIVE = 10;

  protected int d;
  protected int w;
  protected int[][] t;

  protected Sketch(int d, int w) {
    this.d = d;
    this.w = w;
    t = new int[d][w];
    for (int i = 0; i < d; ++i) {
      for (int j = 0; j < w; ++j) {
        t[i][j] = 0;
      }
    }
  }

  /**
   *
   * @param key
   * @return True means this key should be put into cache.
   * @throws InvalidHashFunctionIndexException
   */
  public abstract boolean update(String key)
      throws InvalidHashFunctionIndexException;

  public int estimate(String key) throws InvalidHashFunctionIndexException {
    int res = Integer.MAX_VALUE;
    for (int i = 0; i < d; ++i) {
      int curVal = t[i][calcHash(key, i)];
      if (curVal < res) {
        res = curVal;
      }
    }
    return res;
  }

  protected List<Integer> add(String key) throws InvalidHashFunctionIndexException {
    return add(key, 1);
  }

  protected List<Integer> add(String key, int addend) throws InvalidHashFunctionIndexException {
    List<Integer> num = new ArrayList<Integer>();
    for (int i = 0; i < d; ++i) {
      int j = calcHash(key, i);
      t[i][j] += addend;
      num.add(t[i][j]);
    }
    return num;
  }

  protected int calcHash(String key, int hashFunctionIndex)
      throws InvalidHashFunctionIndexException{
    switch (hashFunctionIndex) {
    case HASH_JAVA:
      return Math.abs(JAVAHash(key));
    case HASH_ONEBYONE:
      return Math.abs(oneByOneHash(key));
    case HASH_BERNSTEIN:
      return Math.abs(bernsteinHash(key));
    case HASH_JS:
      return Math.abs(JSHash(key));
    case HASH_FNV:
      return Math.abs(FNVHash(key));
    case HASH_REFNV:
      return Math.abs(reFNVHash(key));
    case HASH_RS:
      return Math.abs(RSHash(key));
    case HASH_PJW:
      return Math.abs(PJWHash(key));
    case HASH_ELF:
      return Math.abs(ELFHash(key));
    case HASH_ADDITIVE:
      return Math.abs(additiveHash(key));
    case HASH_ROTATE:
      return Math.abs(rotatingHash(key));
    }
    throw new InvalidHashFunctionIndexException("Index: " + hashFunctionIndex + ".");
  }

  protected int additiveHash(String key) {
    int hash, i;
    for (hash = key.length(), i = 0; i < key.length(); ++i) {
      hash += key.charAt(i);
    }
    return (hash % w);
  }

  protected int rotatingHash(String key) {
    int hash, i;
    for (hash = key.length(), i = 0; i < key.length(); ++i) {
      hash = (hash << 4) ^ (hash >> 28) ^ key.charAt(i);
    }
    return (hash % w);
  }

  protected int oneByOneHash(String key) {
    int hash, i;
    for (hash = 0, i = 0; i < key.length(); ++i) {
      hash += key.charAt(i);
      hash += (hash << 10);
      hash ^= (hash >> 6);
    }
    hash += (hash << 3);
    hash ^= (hash >> 11);
    hash += (hash << 15);
    return hash % w;
  }

  protected int bernsteinHash(String key) {
    int hash = 0;
    int i = 0;
    for (; i < key.length(); ++i) {
      hash = 33 * hash + key.charAt(i);
    }
    return hash % w;
  }

  protected int FNVHash(String key) {
    int hash = (int)2166136261L;
    for(int i = 0; i < key.length(); ++i) {
      hash = (hash * 16777619) ^ key.charAt(i);
    }
    return hash % w;
  }

  protected int reFNVHash(String key) {
    final int p = 16777619;
    int hash = (int)2166136261L;
    for(int i = 0; i < key.length(); ++i) {
      hash = (hash ^ key.charAt(i)) * p;
    }
    hash += hash << 13;
    hash ^= hash >> 7;
    hash += hash << 3;
    hash ^= hash >> 17;
    hash += hash << 5;
    return hash % w;
  }

  protected int RSHash(String key) {
    int a  = 63689;
    int b  = 378551;
    int hash = 0;
    for (int i = 0; i < key.length(); ++i) {
      hash = hash * a + key.charAt(i);
      a = a * b;
    }
    return (hash & 0x7FFFFFFF) % w;
  }

  protected int JSHash(String key) {
    int hash = 1315423911;
    for (int i = 0; i < key.length(); ++i) {
      hash ^= ((hash << 5) + key.charAt(i) + (hash >> 2));
    }
    return (hash & 0x7FFFFFFF) % w;
  }

  protected int PJWHash(String key) {
    int BitsInUnsignedInt = 32;
    int ThreeQuarters = (BitsInUnsignedInt * 3) / 4;
    int OneEighth = BitsInUnsignedInt / 8;
    int HighBits = 0xFFFFFFFF << (BitsInUnsignedInt - OneEighth);
    int hash = 0;
    int test = 0;
    for (int i = 0; i < key.length(); ++i)
    {
      hash = (hash << OneEighth) + key.charAt(i);
      if ((test = hash & HighBits) != 0) {
        hash = (( hash ^ (test >> ThreeQuarters)) & (~HighBits));
      }
    }
    return (hash & 0x7FFFFFFF) % w;
  }

  protected int ELFHash(String key) {
    int hash = 0;
    int x = 0;
    for (int i = 0; i < key.length(); ++i) {
      hash = (hash << 4) + key.charAt(i);
      if ((x = (int)(hash & 0xF0000000L)) != 0) {
        hash ^= (x >> 24);
        hash &= ~x;
      }
    }
    return (hash & 0x7FFFFFFF) % w;
  }

  protected int JAVAHash(String key) {
    int h = 0;
    int off = 0;
    int len = key.length();
    for (int i = 0; i < len; ++i) {
      h = 31 * h + key.charAt(off++);
    }
    return h % w;
  }
}
