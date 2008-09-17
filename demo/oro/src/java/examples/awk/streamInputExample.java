/* 
 * $Id: streamInputExample.java 124053 2005-01-04 01:24:35Z dfs $
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


package examples.awk;

import java.io.*;
import org.apache.oro.text.regex.*;
import org.apache.oro.text.awk.*;

/**
 * This is a test program demonstrating how to search an input stream
 * with the AwkTools regular expression classes.
 *
 * @version @version@
 */
public final class streamInputExample {

  /**
   * This program extracts sentences containing the word C++ from 
   * the sample file streamInputExample.txt  The regular expression
   * used is not perfect, so focus on AwkStreamInput and not the
   * ability of the regular expression to handle all normal sentences.
   * For those not familiar with the OROMatcher Util class, a use of
   * the Util.substitute method is included.
   */
  public static final void main(String args[]) {

    // A regular expression to extract sentences containing the word C++.
    // We assume sentences can only end in . ! ? and start with a word
    // character \w
    String regex = "(\\w[^\\.?!]*C\\+\\+|C\\+\\+)[^\\.?!]*[\\.?!]";
    String sentence;
    AwkMatcher matcher;
    AwkCompiler compiler;
    Pattern pattern = null, newline = null;
    AwkStreamInput input;
    MatchResult result;
    Reader file = null;

    // Create AwkCompiler and AwkMatcher instances.
    compiler = new AwkCompiler();
    matcher  = new AwkMatcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      pattern
	= compiler.compile(regex, AwkCompiler.CASE_INSENSITIVE_MASK);
      // Compile a pattern representing a string of newlines with other
      // whitespace stuck around the newlines
      newline = compiler.compile("(\\s*[\n\r]\\s*)+");
    } catch(MalformedPatternException e) {
      System.err.println("Bad pattern.");
      System.err.println(e.getMessage());
      System.exit(1);
    }


    // Open input file.
    try {
      file = new FileReader("streamInputExample.txt");
    } catch(IOException e) {
      System.err.println("Error opening streamInputExample.txt.");
      System.err.println(e.getMessage());
      System.exit(1);
    }

    // Create an AwkStreamInput instance to search the input stream.
    input   = new AwkStreamInput(file);

    // We need to put the search loop in a try block because when searching
    // an AwkStreamInput instance, an IOException may occur, and it must be
    // caught.
    try {
      // Loop until there are no more matches left.
      while(matcher.contains(input, pattern)) {
	// Since we're still in the loop, fetch match that was found.
	result = matcher.getMatch();  
	
	// Substitute all newlines in the match with spaces.
	sentence = Util.substitute(matcher, newline,
				   new StringSubstitution(" "),
				   result.toString(), Util.SUBSTITUTE_ALL);
	System.out.println("\nMatch:\n" + sentence);
      }
    } catch(IOException e) {
      System.err.println("Error occurred while reading file.");
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
