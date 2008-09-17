/* 
 * $Id: Perl5FilenameFilter.java 124053 2005-01-04 01:24:35Z dfs $
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


package org.apache.oro.io;

import java.io.*;

import org.apache.oro.text.regex.*;
import org.apache.oro.text.*;


/**
 * Perl5FilenameFilter is a convenience class that subclasses
 * RegexFilenameFilter and filters on Perl5 regular expressions as
 * implemented by the org.apache.oro.text.regex package, which is
 * required to use this class.
 *
 * @version @version@
 * @since 1.0
 * @see RegexFilenameFilter
 * @see AwkFilenameFilter
 * @see GlobFilenameFilter
 */
public class Perl5FilenameFilter extends RegexFilenameFilter {
  private static final PatternMatcher __MATCHER = new Perl5Matcher();
  private static final PatternCache __CACHE     = new PatternCacheLRU();

  /**
   * Construct a filter initialized with the indicated regular expression
   * and accompanying compilation options conforming to those used by
   * <code> org.apache.oro.text.regex.Perl5Compiler </code>
   * <p>
   * @param regex  The regular expression on which to filter.
   * @param options A set of compilation options.
   * @exception MalformedCachePatternException  If there is an error in
   *     compiling the regular expression.  This need not be caught if
   *     you are using a hard-coded expression that you know is correct.
   *     But for robustness and reliability you should catch this exception
   *     for dynamically entered expressions determined at runtime.
   */
  public Perl5FilenameFilter(String regex, int options) {
    super(__CACHE, __MATCHER, regex, options);
  }

  /** Same as Perl5FilenameFilter(regex, Perl5Compiler.DEFAULT_MASK); */
  public Perl5FilenameFilter(String regex) {
    super(__CACHE, __MATCHER, regex);
  }

  /** Same as Perl5FilenameFilter(""); */
  public Perl5FilenameFilter() {
    super(__CACHE, __MATCHER);
  }
}
