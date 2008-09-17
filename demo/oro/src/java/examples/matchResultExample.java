/* 
 * $Id: matchResultExample.java 124053 2005-01-04 01:24:35Z dfs $
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
 * This is a test program demonstrating the methods of the OROMatcher
 * MatchResult class.
 *
 * @version @version@
 */
public final class matchResultExample {

  /**
   * Takes a regular expression and string as input and reports all the
   * pattern matches in the string.
   * <p>
   * @param args[]  The array of arguments to the program.  The first
   *    argument should be a Perl5 regular expression, and the second
   *    should be an input string.
   */
  public static final void main(String args[]) {
    int groups;
    PatternMatcher matcher;
    PatternCompiler compiler;
    Pattern pattern = null;
    PatternMatcherInput input;
    MatchResult result;

    // Must have at least two arguments, else exit.
    if(args.length < 2) {
      System.err.println("Usage: matchResult pattern input");
      return;
    }

    // Create Perl5Compiler and Perl5Matcher instances.
    compiler = new Perl5Compiler();
    matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      pattern = compiler.compile(args[0]);
    } catch(MalformedPatternException e) {
      System.err.println("Bad pattern.");
      System.err.println(e.getMessage());
      return;
    }

    // Create a PatternMatcherInput instance to keep track of the position
    // where the last match finished, so that the next match search will
    // start from there.  You always create a PatternMatcherInput instance
    // when you want to search a string for all of the matches it contains,
    // and not just the first one.
    input   = new PatternMatcherInput(args[1]);


    // Loop until there are no more matches left.
    while(matcher.contains(input, pattern)) {
      // Since we're still in the loop, fetch match that was found.
      result = matcher.getMatch();  

      // Perform whatever processing on the result you want.
      // Here we just print out all its elements to show how the
      // MatchResult methods are used.
  
      // The toString() method is provided as a convenience method.
      // It returns the entire match.  The following are all equivalent:
      //     System.out.println("Match: " + result);
      //     System.out.println("Match: " + result.toString());
      //     System.out.println("Match: " + result.group(0));
      System.out.println("Match: " + result.toString());

      // Print the length of the match.  The length() method is another
      // convenience method.  The lengths of subgroups can be obtained
      // by first retrieving the subgroup and then calling the string's
      // length() method.
      System.out.println("Length: " + result.length());

      // Retrieve the number of matched groups.  A group corresponds to
      // a parenthesized set in a pattern.
      groups = result.groups();
      System.out.println("Groups: " + groups);

      // Print the offset into the input of the beginning and end of the
      // match.  The beinOffset() and endOffset() methods return the
      // offsets of a group relative to the beginning of the input.  The
      // begin() and end() methods return the offsets of a group relative
      // the to the beginning of a match.
      System.out.println("Begin offset: " + result.beginOffset(0));
      System.out.println("End offset: " + result.endOffset(0));
      System.out.println("Groups: ");

      // Print the contents of each matched subgroup along with their
      // offsets relative to the beginning of the entire match.

      // Start at 1 because we just printed out group 0
      for(int group = 1; group < groups; group++) {
	System.out.println(group + ": " + result.group(group));
	System.out.println("Begin: " + result.begin(group));
	System.out.println("End: " + result.end(group));
      }
    }
  }
}
