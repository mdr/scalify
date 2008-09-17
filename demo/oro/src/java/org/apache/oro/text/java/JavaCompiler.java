/* 
 * $Id: JavaCompiler.java 124053 2005-01-04 01:24:35Z dfs $
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

import java.util.regex.*;

import org.apache.oro.text.regex.*;

/**
 *
 * @version @version@
 * @since 2.1
 */

public final class JavaCompiler implements PatternCompiler {

  public org.apache.oro.text.regex.Pattern compile(String pattern)
    throws MalformedPatternException
  {
    return compile(pattern, 0);
  }


  public org.apache.oro.text.regex.Pattern compile(String pattern, int options)
    throws MalformedPatternException
  {
    try {
      JavaPattern jp = new JavaPattern(pattern);
      return jp;
    } catch(Exception e) {
      // We can't wrap the exception without making MalformedPatternException
      // dependent on J2SE 1.4.
      throw new MalformedPatternException(e.getMessage());
    }
  }


  public org.apache.oro.text.regex.Pattern compile(char[] pattern)
    throws MalformedPatternException 
  {
    return compile(new String(pattern));
  }


  public org.apache.oro.text.regex.Pattern compile(char[] pattern, int options)
       throws MalformedPatternException
  {
    return compile(new String(pattern));
  }

}
