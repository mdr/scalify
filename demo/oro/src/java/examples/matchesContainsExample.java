/* 
 * $Id: matchesContainsExample.java 124053 2005-01-04 01:24:35Z dfs $
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
 * This is a test program demonstrating the difference between the
 * matches() and contains() methods.
 *
 * @version @version@
 */
public final class matchesContainsExample {

  /**
   * A common mistake is to confuse the behavior of the matches() and
   * contains() methods.  matches() tests to see if a string exactly
   * matches a pattern whereas contains() searches for the first pattern
   * match contained somewhere within the string.  When used with a
   * PatternMatcherInput instance, the contains() method allows you to
   * search for every pattern match within a string by using a while loop.
   */
  public static final void main(String args[]) {
    int matches = 0;
    String numberExpression = "\\d+";
    String exactMatch = "2010";
    String containsMatches =
   "  2001 was the movie before 2010, which takes place before 2069 the book ";
    Pattern pattern   = null;
    PatternMatcherInput input;
    PatternCompiler compiler;
    PatternMatcher matcher;
    MatchResult result;

    // Create Perl5Compiler and Perl5Matcher instances.
    compiler = new Perl5Compiler();
    matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      pattern = compiler.compile(numberExpression);
    } catch(MalformedPatternException e) {
      System.err.println("Bad pattern.");
      System.err.println(e.getMessage());
      System.exit(1);
    }

    // Here we show the difference between the matches() and contains()
    // methods().  Compile the program and study the output to reinforce
    // in your mind what the methods do.

    System.out.println("Input: " + exactMatch);

    // The following should return true because exactMatch exactly matches
    // numberExprssion.

    if(matcher.matches(exactMatch, pattern))
      System.out.println("matches() Result: TRUE, EXACT MATCH");
    else
      System.out.println("matches() Result: FALSE, NOT EXACT MATCH");

    System.out.println("\nInput: " + containsMatches);

    // The following should return false because containsMatches does not
    // exactly match numberExpression even though its subparts do.

    if(matcher.matches(containsMatches, pattern))
      System.out.println("matches() Result: TRUE, EXACT MATCH");
    else
      System.out.println("matches() Result: FALSE, NOT EXACT MATCH");


    // Now we call the contains() method.  contains() should return true
    // for both strings.

    System.out.println("\nInput: " + exactMatch);

    if(matcher.contains(exactMatch, pattern)) {
      System.out.println("contains() Result: TRUE");

      // Fetch match and print.
      result = matcher.getMatch();
      System.out.println("Match: " + result);
    } else
      System.out.println("contains() Result: FALSE");

    System.out.println("\nInput: " + containsMatches);

    if(matcher.contains(containsMatches, pattern)) {
      System.out.println("contains() Result: TRUE");
      // Fetch match and print.
      result = matcher.getMatch();
      System.out.println("Match: " + result);
    } else
      System.out.println("contains() Result: FALSE");


    // In the previous example, notice how contains() will fetch only first 
    // match in a string.  If you want to search a string for all of the
    // matches it contains, you must create a PatternMatcherInput object
    // to keep track of the position of the last match, so you can pick
    // up a search where the last one left off.

    input   = new PatternMatcherInput(containsMatches);

    System.out.println("\nPatternMatcherInput: " + input);
    // Loop until there are no more matches left.
    while(matcher.contains(input, pattern)) {
      // Since we're still in the loop, fetch match that was found.
      result = matcher.getMatch();  

      ++matches;

      System.out.println("Match " + matches + ": " + result);
    }
  }
}
