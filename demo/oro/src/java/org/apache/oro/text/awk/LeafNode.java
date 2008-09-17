/* 
 * $Id: LeafNode.java 124053 2005-01-04 01:24:35Z dfs $
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
abstract class LeafNode extends SyntaxNode {
  static final int _NUM_TOKENS                    = 256;
  static final int _END_MARKER_TOKEN              = _NUM_TOKENS;

  protected int _position;
  protected BitSet _positionSet;

  LeafNode(int position){
    _position = position;
    _positionSet = new BitSet(position + 1);
    _positionSet.set(position);
  }

  abstract boolean _matches(char token);
  final boolean _nullable()     { return false; }
  final BitSet _firstPosition() { return _positionSet; }
  final BitSet _lastPosition()  { return _positionSet; }
  final void _followPosition(BitSet[] follow, SyntaxNode[] nodes) {
    nodes[_position] = this;
  }
}
