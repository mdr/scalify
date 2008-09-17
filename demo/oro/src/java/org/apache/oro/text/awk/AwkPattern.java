/* 
 * $Id: AwkPattern.java 124053 2005-01-04 01:24:35Z dfs $
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

import java.io.Serializable;
import java.util.*;

import org.apache.oro.text.regex.*;


final class DFAState {
  int _stateNumber;
  BitSet _state;

  DFAState(BitSet s, int num){
    _state = s;
    _stateNumber = num;
  }
}

/**
 * An implementation of the Pattern interface for Awk regular expressions.
 * This class is compatible with the AwkCompiler and AwkMatcher
 * classes.  When an AwkCompiler instance compiles a regular expression
 * pattern, it produces an AwkPattern instance containing internal
 * data structures used by AwkMatcher to perform pattern matches.
 * This class cannot be subclassed and cannot be directly instantiated
 * by the programmer as it would not make sense.  It is however serializable
 * so that pre-compiled patterns may be saved to disk and re-read at a later
 * time.  AwkPattern instances should only be created through calls to an
 * AwkCompiler instance's compile() methods
 * 
 * @version @version@
 * @since 1.0
 * @see AwkCompiler
 * @see AwkMatcher
 */
public final class AwkPattern implements Pattern, Serializable {
  final static int _INVALID_STATE = -1, _START_STATE = 1; 

  int _numStates, _endPosition, _options;
  String _expression;
  Vector _Dtrans, _nodeList[], _stateList;
  BitSet _U, _emptySet, _followSet[], _endStates;
  Hashtable _stateMap;
  boolean _matchesNullString, _fastMap[];
  boolean _hasBeginAnchor = false, _hasEndAnchor = false;

  AwkPattern(String expression, SyntaxTree tree){
    int token, node, tstateArray[];
    DFAState dfaState;

    _expression = expression;

    // Assume endPosition always occurs at end of parse.
    _endPosition = tree._positions - 1;
    _followSet   = tree._followSet;

    _Dtrans    = new Vector();
    _stateList = new Vector();
    _endStates = new BitSet();

    _U        = new BitSet(tree._positions);
    _U.or(tree._root._firstPosition());

    tstateArray = new int[LeafNode._NUM_TOKENS];
    _Dtrans.addElement(tstateArray); // this is a dummy entry because we
                                     // number our states starting from 1
    _Dtrans.addElement(tstateArray);

    _numStates = _START_STATE;
    if(_U.get(_endPosition))
      _endStates.set(_numStates);
    dfaState = new DFAState((BitSet)_U.clone(), _numStates);
    _stateMap = new Hashtable();
    _stateMap.put(dfaState._state, dfaState);
    _stateList.addElement(dfaState); // this is a dummy entry because we
                                     // number our states starting from 1
    _stateList.addElement(dfaState);
    _numStates++;

    _U.xor(_U);  // clear bits
    _emptySet = new BitSet(tree._positions);

    _nodeList = new Vector[LeafNode._NUM_TOKENS];
    for(token = 0; token < LeafNode._NUM_TOKENS; token++){
      _nodeList[token] = new Vector();
      for(node=0; node < tree._positions; node++)
	if(tree._nodes[node]._matches((char)token))
	  _nodeList[token].addElement(tree._nodes[node]);
    }

    _fastMap = tree.createFastMap();
    _matchesNullString = _endStates.get(_START_STATE);
  }

  // tstateArray is assumed to have been set before calling this method
  void _createNewState(int current, int token, int[] tstateArray) {
    int node, pos;
    DFAState T, dfaState;

    T    = (DFAState)_stateList.elementAt(current);
    node = _nodeList[token].size();
    _U.xor(_U);  // clear bits
    while(node-- > 0){
      pos = ((LeafNode)_nodeList[token].elementAt(node))._position;
      if(T._state.get(pos))
	_U.or(_followSet[pos]);
    }

    if(!_stateMap.containsKey(_U)){
      dfaState = new DFAState((BitSet)_U.clone(), _numStates++);
      _stateList.addElement(dfaState);
      _stateMap.put(dfaState._state, dfaState);
      _Dtrans.addElement(new int[LeafNode._NUM_TOKENS]);

      if(!_U.equals(_emptySet)){
	tstateArray[token] = _numStates - 1;

	if(_U.get(_endPosition))
	  _endStates.set(_numStates - 1);
      } else
	tstateArray[token] = _INVALID_STATE;
    } else {
      if(_U.equals(_emptySet))
	tstateArray[token] = _INVALID_STATE;
      else 
	tstateArray[token] = ((DFAState)_stateMap.get(_U))._stateNumber;
    }
  }

  int[] _getStateArray(int state) { return ((int[])_Dtrans.elementAt(state)); }


  /**
   * This method returns the string representation of the pattern.
   * <p>
   * @return The original string representation of the regular expression
   *         pattern.
   */
  public String getPattern() { return _expression; }


  /**
   * This method returns an integer containing the compilation options used
   * to compile this pattern.
   * <p>
   * @return The compilation options used to compile the pattern.
   */
  public int getOptions()    { return _options; }
}

