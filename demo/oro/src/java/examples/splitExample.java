/* 
 * $Id: splitExample.java 124053 2005-01-04 01:24:35Z dfs $
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


package examples;

import java.util.*;
import org.apache.oro.text.regex.*;

/**
 * This is a test program demonstrating the use of the Util.split() method.
 *
 * @version @version@
 */
public final class splitExample {

  /**
   * A good way for you to understand the split() method is to play around
   * with it by using this test program.  The program takes 2 or 3 arguments
   * as follows:
   *      java splitExample regex input [split limit]
   * regex - A regular expression used to split the input.
   * input - A string to be used as input for split().
   * split limit - An optional argument limiting the size of the list returned
   *               by split().  If no limit is given, the limit used is
   *               Util.SPLIT_ALL.  Setting the limit to 1 generally doesn't
   *               make any sense.
   *
   * Try the following two command lines to see how split limit works:
   *          java splitExample '[:|]' '1:2|3:4'
   *          java splitExample '[:|]' '1:2|3:4' 3
   *
   */
  public static final void main(String args[]) {
    int limit, i;
    String regularExpression, input;
    List results = new ArrayList();
    Pattern pattern = null;
    PatternMatcher matcher;
    PatternCompiler compiler;
    Iterator elements;

    // Make sure there are sufficient arguments
    if(args.length < 2) {
      System.err.println("Usage: splitExample regex input [split limit]");
      System.exit(1);
    }

    regularExpression = args[0];
    input = args[1];

    if(args.length > 2)
      limit = Integer.parseInt(args[2]);
    else
      limit = Util.SPLIT_ALL;

    // Create Perl5Compiler and Perl5Matcher instances.
    compiler = new Perl5Compiler();
    matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      pattern = compiler.compile(regularExpression);
      System.out.println("split regex: " + regularExpression);
    } catch(MalformedPatternException e){
      System.err.println("Bad pattern.");
      System.err.println(e.getMessage());
      System.exit(1);
    }

    // Split the input and print the resulting list.
    System.out.println("split results: ");
    Util.split(results, matcher, pattern, input, limit);
    elements = results.iterator();

    i = 0;
    while(elements.hasNext())
      System.out.println("item " + i++ + ": " + (String)elements.next());

  }
}
