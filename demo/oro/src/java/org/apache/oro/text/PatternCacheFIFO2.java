/* 
 * $Id: PatternCacheFIFO2.java 124053 2005-01-04 01:24:35Z dfs $
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

import java.util.*;

import org.apache.oro.text.regex.*;
import org.apache.oro.util.*;

/**
 * This class is a GenericPatternCache subclass implementing a second
 * chance FIFO (First In First Out) cache replacement policy.  In other
 * words, patterns are added to the cache until the cache becomes full.
 * Once the cache is full, when a new pattern is added to the cache, it 
 * replaces the first of the current patterns in the cache to have been
 * added, unless that pattern has been used recently (generally
 * between the last cache replacement and now).
 * If the pattern to be replaced has been used, it is given
 * a second chance, and the next pattern in the cache is tested for
 * replacement in the same manner.  If all the patterns are given a
 * second chance, then the original pattern selected for replacement is
 * replaced.
 *
 * @version @version@
 * @since 1.0
 * @see GenericPatternCache
 */
public final class PatternCacheFIFO2 extends GenericPatternCache {

  /**
   * Creates a PatternCacheFIFO2 instance with a given cache capacity,
   * initialized to use a given PatternCompiler instance as a pattern compiler.
   * <p>
   * @param capacity  The capacity of the cache.
   * @param compiler  The PatternCompiler to use to compile patterns.
   */
  public PatternCacheFIFO2(int capacity, PatternCompiler compiler) {
    super(new CacheFIFO2(capacity), compiler);
  }


  /**
   * Same as:
   * <blockquote><pre>
   * PatternCacheFIFO2(GenericPatternCache.DEFAULT_CAPACITY, compiler);
   * </pre></blockquote>
   */
  public PatternCacheFIFO2(PatternCompiler compiler) {
    this(GenericPatternCache.DEFAULT_CAPACITY, compiler);
  }


  /**
   * Same as:
   * <blockquote><pre>
   * PatternCacheFIFO2(capacity, new Perl5Compiler());
   * </pre></blockquote>
   */
  public PatternCacheFIFO2(int capacity) {
    this(capacity, new Perl5Compiler());
  }


  /**
   * Same as:
   * <blockquote><pre>
   * PatternCacheFIFO2(GenericPatternCache.DEFAULT_CAPACITY);
   * </pre></blockquote>
   */
  public PatternCacheFIFO2() {
    this(GenericPatternCache.DEFAULT_CAPACITY);
  }

}



