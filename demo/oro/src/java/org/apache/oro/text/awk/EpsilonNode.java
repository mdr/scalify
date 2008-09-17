/* 
 * $Id: EpsilonNode.java 124053 2005-01-04 01:24:35Z dfs $
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
final class EpsilonNode extends SyntaxNode {
  BitSet _positionSet = new BitSet(1);

  boolean _nullable()     { return true; }
  BitSet _firstPosition() { return _positionSet; }
  BitSet _lastPosition()  { return _positionSet; }
  void _followPosition(BitSet[] follow, SyntaxNode[] nodes) {
  }

  SyntaxNode _clone(int pos[]){
    return new EpsilonNode();
  }
}
