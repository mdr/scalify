/* 
 * $Id: didNotMatch.java 124053 2005-01-04 01:24:35Z dfs $
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
import org.apache.oro.text.perl.*;

/**
 * This is a trivial example program demonstrating the preMatch()
 * and postMatch() methods of Perl5Util.
 *
 * @version @version@
 */
public final class didNotMatch {

  /**
   * This program takes a Perl5 pattern and an input string as arguments.
   * It prints the parts of the input surrounding the first occurrence
   * of the pattern in the input.
   */
  public static final void main(String args[]) {
    String pattern, input;
    Perl5Util perl;

    if(args.length < 2) {
      System.err.println("Usage: didNotMatch pattern input");
      System.exit(1);
    }

    pattern = args[0];
    input   = args[1];
    perl    = new Perl5Util();

    // Use a try block because we have no idea if the user will enter a valid
    // pattern.
    try {
      if(perl.match(pattern, input)) {
	System.out.println("Pre : " + perl.preMatch());
	System.out.println("Post: " + perl.postMatch());
      } else
	System.err.println("There was no match.");
    } catch(MalformedPerl5PatternException e) {
      System.err.println("You entered an invalid pattern.");
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }

  }

}
