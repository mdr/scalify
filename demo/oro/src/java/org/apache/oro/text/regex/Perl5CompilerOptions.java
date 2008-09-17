/* 
 * $Id: Perl5CompilerOptions.java 124053 2005-01-04 01:24:35Z dfs $
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


package org.apache.oro.text.regex;


/**
 * 
 * @version @version@
 * @since 2.1
 */
public class Perl5CompilerOptions implements PatternCompilerOptions {

  public int getMask(int option) throws UnsupportedOperationException {
    switch(option) {
    case DEFAULT          : return Perl5Compiler.DEFAULT_MASK;
    case CASE_INSENSITIVE : return Perl5Compiler.CASE_INSENSITIVE_MASK;
    case MULTILINE        : return Perl5Compiler.MULTILINE_MASK;
    }

    throw new UnsupportedOperationException();
  }

}
