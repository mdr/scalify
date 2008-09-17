/* 
 * $Id: substituteExample.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.text.regex.*;

/**
 * This is a test program demonstrating the use of the Util.substitute()
 * method.
 *
 * @version @version@
 */
public final class substituteExample {

  /**
   * A good way for you to understand the substitute() method is to play around
   * with it by using this test program.  The program takes 3 to 5 arguments
   * as follows:
   *   java substituteExample
   *     regex substitution input [sub limit] [interpolation limit]
   * regex - A regular expression used to find parts of the input to be
   *         substituted.
   * sub limit - An optional argument limiting the number of substitutions.
   *             If no limit is given, the limit used is Util.SUBSTITUTE_ALL.
   * input - A string to be used as input for substitute().
   * interpolation limit - An optional argument limiting the number of
   *             interpolations performed.
   *
   * Try the following command line for a simple example of subsitute().
   * It changes (2,3) to (3,2) in the input.
   *          java substituteExample '\(2,3\)' '(3, 2)' '(1,2) (2,3) (4,5)'
   *
   * The following command line shows the substitute limit at work.  It
   * changed the first four 1's in the input to 4's.
   *          java substituteExample "1" "4" "381298175 1111" "4"
   *
   * The next command line shows how to use interpolations.  Suppose we
   * want to reverse the coordinates of the first 3 entries in the input
   * and then have all the rest of the coordinates be equal to the new 3rd
   * entry:
java substituteExample '\((\d+),(\d+)\)' '($2,$1)' '(1,2) (2,3) (4,5) (8,8) (9,2)' 5 3
   *
   */
  public static final void main(String args[]) {
    int limit, interps;
    PatternMatcher matcher = new Perl5Matcher();
    Pattern pattern = null;
    PatternCompiler compiler = new Perl5Compiler();
    String regularExpression, input, sub, result;

    // Make sure there are sufficient arguments
    if(args.length < 3) {
      System.err.println("Usage: substituteExample regex substitution " +
			 "input [sub limit] [interpolation limit]");
      System.exit(1);
    }

    limit = Util.SUBSTITUTE_ALL;
    interps = Perl5Substitution.INTERPOLATE_ALL;

    regularExpression = args[0];
    sub               = args[1];
    input             = args[2];

    if(args.length > 3)
      limit = Integer.parseInt(args[3]);

    if(args.length > 4)
      interps = Integer.parseInt(args[4]);

    try {
      pattern = compiler.compile(regularExpression);
      System.out.println("substitute regex: " + regularExpression);
    } catch(MalformedPatternException e){
      System.err.println("Bad pattern.");
      System.err.println(e.getMessage());
      System.exit(1);
    }

    // Perform substitution and print result.
    result = Util.substitute(matcher, pattern,
			     new Perl5Substitution(sub, interps),
			     input, limit);
    System.out.println("result: " + result);
  }
}
