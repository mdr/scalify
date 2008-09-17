/* 
 * $Id: SyntaxNode.java 124053 2005-01-04 01:24:35Z dfs $
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
abstract class SyntaxNode {
  abstract boolean _nullable(); 
  abstract BitSet  _firstPosition();
  abstract BitSet  _lastPosition();
  abstract void    _followPosition(BitSet[] follow, SyntaxNode[] nodes);

  /**
   * This method is designed specifically to accommodate the expansion of
   * an interval into its subparts.
   * <p>
   * @param pos  A single element array containing a variable representing
   *             the current position.  It is made an array to cause it
   *             to be passed by reference to allow incrementing.
   */ 
  abstract SyntaxNode _clone(int pos[]);
}
