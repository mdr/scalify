/* 
 * $Id: JavaMatcher.java 124053 2005-01-04 01:24:35Z dfs $
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


package org.apache.oro.text.java;

import java.nio.*;
import java.util.regex.Matcher;

import org.apache.oro.text.regex.*;

/**
 *
 * @version @version@
 * @since 2.1
 */
public final class JavaMatcher implements PatternMatcher {

  private Matcher __matcher;

  public JavaMatcher() {
    __matcher = null;
  }

  /** Currently throws an UnsupportedOperationException. */
  public boolean matchesPrefix(char[] input, Pattern pattern, int offset) {
    throw new UnsupportedOperationException();
  }

  /** Currently throws an UnsupportedOperationException. */
  public boolean matchesPrefix(String input, Pattern pattern) {
    throw new UnsupportedOperationException();
  }

  /** Currently throws an UnsupportedOperationException. */
  public boolean matchesPrefix(char[] input, Pattern pattern) {
    throw new UnsupportedOperationException();
  }

  /** Currently throws an UnsupportedOperationException. */
  public boolean matchesPrefix(PatternMatcherInput input, Pattern pattern) {
    throw new UnsupportedOperationException();
  }


  private boolean __matches(Matcher matcher) {
    boolean matched;

    matched = matcher.matches();

    if(matched)
      __matcher = matcher;
    else
      __matcher = null;

    return matched;
  }


  public boolean matches(String input, Pattern pattern) {
    JavaPattern jp = (JavaPattern)pattern;
    return __matches(jp._matcher(input));
  }


  public boolean matches(char[] input, Pattern pattern) {
    JavaPattern jp = (JavaPattern)pattern;
    return __matches(jp._matcher(CharBuffer.wrap(input)));
  }


  public boolean matches(PatternMatcherInput input, Pattern pattern) {
    JavaPattern jp    = (JavaPattern)pattern;
    CharBuffer buffer = 
      CharBuffer.wrap(input.getBuffer(), input.getBeginOffset(),
                      input.getEndOffset());
    Matcher matcher = jp._matcher(buffer);
    return __matches(matcher);
  }


  private boolean __contains(Matcher matcher) {
    boolean matched;

    matched = matcher.find();

    if(matched)
      __matcher = matcher;
    else
      __matcher = null;

    return matched;
  }

  // These wrappers are not the height of efficiency (java.util.regex.Matcher
  // instances are not reused) because of the quirkiness of java.util.regex.
  // Some time should be spent on improving their efficiency in the future.
  public boolean contains(String input, Pattern pattern) {
    JavaPattern jp = (JavaPattern)pattern;
    return __contains(jp._matcher(input));
  }


  public boolean contains(char[] input, Pattern pattern) {
    JavaPattern jp = (JavaPattern)pattern;
    return __contains(jp._matcher(CharBuffer.wrap(input)));
  }


  public boolean contains(PatternMatcherInput input, Pattern pattern) {
    JavaPattern jp    = (JavaPattern)pattern;
    CharBuffer buffer = 
      CharBuffer.wrap(input.getBuffer(), input.getCurrentOffset(),
                      input.getEndOffset() - input.getCurrentOffset());
    Matcher matcher = jp._matcher(buffer);
    boolean matched = __contains(matcher);

    if(matched) {
      input.setCurrentOffset(matcher.end());
      input.setMatchOffsets(matcher.start(), matcher.end());
    } else {
      input.setCurrentOffset(input.getEndOffset());
      input.setMatchOffsets(-1, -1);
    }

    return matched;
  }


  public MatchResult getMatch() {
    if(__matcher == null)
      return null;
    return new JavaMatchResult(__matcher);
  }
}
