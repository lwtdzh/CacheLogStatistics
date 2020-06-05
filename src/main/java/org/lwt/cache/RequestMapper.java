package org.lwt.cache;

import java.util.List;

public interface RequestMapper {
  /**
   * Map a read request to several storage blocks and the read length of each block.
   * A block here is a section in a file, e.g, in fileName: "file1",
   * position [100~150] is a block, position [151~200] is another block.
   * @param req The request.
   * @return Several reading blocks and the length of these block.
   *         Format: ([block-id1, readLen1], [block-id2, readLen2] ... ).
   *         E.g., return ([1999, 10], [2001, 15]) means
   *         this request read 10 units from block-id: 1999,
   *         and read 15 units from block-id: 2001.
   *         The block-id can be arbitrary type. You need to guarantee that
   *         each block have an unique block-id.
   */
  List<Tuple<Object, Integer>> mapRequest(Request req);
}
