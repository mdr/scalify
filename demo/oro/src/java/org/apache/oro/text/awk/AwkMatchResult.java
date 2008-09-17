/* 
 * $Id: AwkMatchResult.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.text.regex.*;

/**
 * A class used to store and access the results of an AwkPattern match.
 * It is important for you to remember that AwkMatcher does not save
 * parenthesized sub-group information.  Therefore the number of groups
 * saved in an AwkMatchResult will always be 1.
 *
 * @version @version@
 * @since 1.0
 * @see org.apache.oro.text.regex.PatternMatcher
 * @see AwkMatcher
 * @see AwkCompiler
 */

final class AwkMatchResult implements MatchResult {
  /**
   * The character offset into the line or stream where the match
   * begins.  Pattern matching methods that look for matches a line at
   * a time should use this field as the offset into the line
   * of the match.  Methods that look for matches independent of line
   * boundaries should use this field as the offset into the entire
   * text stream.
   */
  private int __matchBeginOffset;


  /**
   * The length of the match.  Stored as a convenience to avoid calling
   * the String length().  Since groups  aren't saved, all we need is the
   * length and the offset into the stream.
   */
  private int __length;


  /**
   * The entire string that matched the pattern.
   */
  private String __match;


  /**
   * Default constructor given default access to prevent instantiation
   * outside the package.
   */
  AwkMatchResult(String match, int matchBeginOffset){
    __match            = match;
    __length           = match.length();
    __matchBeginOffset = matchBeginOffset;
  }


  /**
   * Adjusts the relative offset where the match begins to an absolute
   * value.  Only used by AwkMatcher to adjust the offset for stream
   * matches.
   */
  void _incrementMatchBeginOffset(int streamOffset) {
    __matchBeginOffset+=streamOffset;
  }

  /**
   * @return The length of the match.
   */
  public int length(){ return __length; }


  /**
   * @return The number of groups contained in the result.  This number
   *         includes the 0th group.  In other words, the result refers
   *         to the number of parenthesized subgroups plus the entire match
   *         itself.  Because Awk doesn't save parenthesized groups, this
   *         always returns 1.
   */
  public int groups(){ return 1; }


  /**
   * @param group The pattern subgroup to return.
   * @return A string containing the indicated pattern subgroup.  Group
   *         0 always refers to the entire match.  If a group was never
   *         matched, it returns null.  This is not to be confused with
   *         a group matching the null string, which will return a String
   *         of length 0.
   */
  public String group(int group){ return (group == 0 ? __match : null); }


  /**
   * @param group The pattern subgroup.
   * @return The offset into group 0 of the first token in the indicated
   *         pattern subgroup.  If a group was never matched or does
   *         not exist, returns -1.
   */
  public int begin(int group){ return (group == 0 ? 0 : -1); }

  /**
   * @param group The pattern subgroup.
   * @return Returns one plus the offset into group 0 of the last token in
   *         the indicated pattern subgroup.  If a group was never matched
   *         or does not exist, returns -1.  A group matching the null
   *         string will return its start offset.
   */
  public int end(int group){ return (group == 0 ? __length : -1); }


  /**
   * Returns an offset marking the beginning of the pattern match
   * relative to the beginning of the input.
   * <p>
   * @param group The pattern subgroup.
   * @return The offset of the first token in the indicated
   *         pattern subgroup.  If a group was never matched or does
   *         not exist, returns -1.
   */
  public int beginOffset(int group){
    return (group == 0 ? __matchBeginOffset : -1);
  }

  /**
   * Returns an offset marking the end of the pattern match 
   * relative to the beginning of the input.
   * <p>
   * @param group The pattern subgroup.
   * @return Returns one plus the offset of the last token in
   *         the indicated pattern subgroup.  If a group was never matched
   *         or does not exist, returns -1.  A group matching the null
   *         string will return its start offset.
   */
  public int endOffset(int group){
    return (group == 0 ? __matchBeginOffset + __length : -1);
  }


  /**
   * The same as group(0).
   *
   * @return A string containing the entire match.
   */
  public String toString() { return group(0); }

}






