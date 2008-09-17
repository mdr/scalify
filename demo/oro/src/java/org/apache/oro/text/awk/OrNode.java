/* 
 * $Id: OrNode.java 124053 2005-01-04 01:24:35Z dfs $
 *
 * Copyright 2000-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oro.text.awk;

import java.util.*;

/**
 * @version @version@
 * @since 1.0
 */
class OrNode extends SyntaxNode {
  SyntaxNode _left, _right;

  OrNode(SyntaxNode left, SyntaxNode right) {
    _left  = left;
    _right = right;
  }

  boolean _nullable() {
    return (_left._nullable() || _right._nullable());
  }

  BitSet _firstPosition() {
    BitSet ls, rs, bs;

    ls = _left._firstPosition();
    rs = _right._firstPosition();
    bs = new BitSet(Math.max(ls.size(), rs.size()));
    bs.or(rs);
    bs.or(ls);

    return bs;
  }

  BitSet _lastPosition()  {
    BitSet ls, rs, bs;

    ls = _left._lastPosition();
    rs = _right._lastPosition();
    bs = new BitSet(Math.max(ls.size(), rs.size()));
    bs.or(rs);
    bs.or(ls);

    return bs;
  }

  void _followPosition(BitSet[] follow, SyntaxNode[] nodes) {
    _left._followPosition(follow, nodes);
    _right._followPosition(follow, nodes);
  }

  SyntaxNode _clone(int pos[]) {
    return new OrNode(_left._clone(pos), _right._clone(pos));
  }
}
