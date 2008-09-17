/* 
 * $Id: prefixExample.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.text.regex.*;
import org.apache.oro.text.awk.*;

/**
 * This is a test program demonstrating an application of the matchesPrefix()
 * methods.  This example program shows how you might tokenize a stream of
 * input using whitespace as a token separator.  Don't forget to use quotes
 * around the input on the command line, e.g.
 *    java prefixExample "Test to see if 1.0 is real and 2 is an integer"
 *
 * If you don't need the power of a full blown lexer generator, you can
 * easily use regular expressions to create your own tokenization and
 * simple parsing classes using similar approaches.  This example is
 * rather sloppy.  If you look at the equivalent example in the OROMatcher
 * distribution, you'll see how to Perl's zero-width look ahead assertion
 * makes correctness easier to achieve.
 *
 * @version @version@
 */
public final class prefixExample {
  public static final int REAL        = 0;
  public static final int INTEGER     = 1;
  public static final int STRING      = 2;

  public static final String[] types = { "Real", "Integer", "String" };
  public static final String whitespace = "[ \t\n\r]+";
  public static final String[] tokens   = {
    "-?[0-9]*\\.[0-9]+([eE]-?[0-9]+)?", "-?[0-9]+", "[^ \t\n\r]+"
  };

  public static final void main(String args[]) {
    int token;
    PatternMatcherInput input;
    PatternMatcher matcher;
    PatternCompiler compiler;
    Pattern[] patterns;
    Pattern tokenSeparator = null;
    MatchResult result;

    if(args.length < 1) {
      System.err.println("Usage: prefixExample <sample input>");
      System.exit(1);
    }

    input    = new PatternMatcherInput(args[0]);
    compiler = new AwkCompiler();
    patterns = new Pattern[tokens.length];

    try {
      tokenSeparator = compiler.compile(whitespace);
      for(token=0; token < tokens.length; token++)
	patterns[token] = compiler.compile(tokens[token]);
    } catch(MalformedPatternException e) {
      System.err.println("Bad pattern.");
      e.printStackTrace();
      System.exit(1);
    }

    matcher  = new AwkMatcher();

  _whileLoop:
    while(!input.endOfInput()) {
      for(token = 0; token < tokens.length; token++)
	if(matcher.matchesPrefix(input, patterns[token])) {
	  int offset;
	  result = matcher.getMatch();
	  offset = input.getCurrentOffset();
	  input.setCurrentOffset(result.endOffset(0));

	  if(matcher.matchesPrefix(input, tokenSeparator)) {
	    input.setCurrentOffset(matcher.getMatch().endOffset(0));
	    System.out.println(types[token] + ": " + result);
	    continue _whileLoop;
	  } else if(input.endOfInput()) {
	    System.out.println(types[token] + ": " + result);
	    break _whileLoop;
	  }

	  input.setCurrentOffset(offset);
	}

      if(matcher.matchesPrefix(input, tokenSeparator))
	input.setCurrentOffset(matcher.getMatch().endOffset(0));
      else {
	System.err.println("Unrecognized token starting at offset: " +
			   input.getCurrentOffset());
	break;
      }
    }

  }
}
