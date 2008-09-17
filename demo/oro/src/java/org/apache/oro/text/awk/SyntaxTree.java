/* 
 * $Id: SyntaxTree.java 124053 2005-01-04 01:24:35Z dfs $
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

/*
 * IMPORTANT!!!!!!!!!!!!!
 * Don't forget to optimize this module.  The calculation of follow can
 * be accelerated by calculating first and last only once for each node and
 * saving instead of doing dynamic calculation every time.
 */

/**
 * @version @version@
 * @since 1.0
 */
final class SyntaxTree {
  int _positions;
  SyntaxNode _root;
  LeafNode[] _nodes;
  BitSet[] _followSet;
  
  SyntaxTree(SyntaxNode root, int positions) {
    _root      = root;
    _positions = positions;
  }

  void _computeFollowPositions() {
    int index;

    _followSet = new BitSet[_positions];
    _nodes     = new LeafNode[_positions];
    index =    _positions;

    while(0 < index--)
      _followSet[index] = new BitSet(_positions);

    _root._followPosition(_followSet, _nodes);
  }

  private void __addToFastMap(BitSet pos, boolean[] fastMap, boolean[] done){
    int token, node;

    for(node = 0; node < _positions; node++){
      if(pos.get(node) && !done[node]){
	done[node] = true;

	for(token=0; token < LeafNode._NUM_TOKENS; token++){
	  if(!fastMap[token])
	    fastMap[token] = _nodes[node]._matches((char)token);
	}
      }
    }
  }

  boolean[] createFastMap(){
    boolean[] fastMap, done;

    fastMap  = new boolean[LeafNode._NUM_TOKENS]; 
    done     = new boolean[_positions];
    __addToFastMap(_root._firstPosition(), fastMap, done);

    return fastMap;
  }
}
