/* 
 * $Id: MatchAction.java 124053 2005-01-04 01:24:35Z dfs $
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


package org.apache.oro.text;


/**
 * The MatchAction interface provides the callback interface for actions
 * bound to patterns in
 * {@link MatchActionProcessor}.  More often than not, you will want to
 * create MatchAction instances as anonymous classes when adding pattern
 * action pairs to a MatchActionProcessor instance.
 *
 * @version @version@
 * @since 1.0
 * @see MatchActionProcessor
 * @see MatchActionInfo
 */
public interface MatchAction {
  /**
   * This method is called by MatchActionProcessor when it finds an associated
   * pattern in a line of input.  Information pertaining to the matched
   * line is included in the MatchActionInfo parameter.
   * <p>
   * @see MatchActionProcessor
   * @see MatchActionInfo
   * @param matchInfo  The match information associated with the line
   *                   matched by MatchActionProcessor.
   */
  public void processMatch(MatchActionInfo matchInfo);
}
