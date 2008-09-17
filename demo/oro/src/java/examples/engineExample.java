/* 
 * $Id: engineExample.java 124053 2005-01-04 01:24:35Z dfs $
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

import java.io.*;
import java.util.*;

import org.apache.oro.text.*;
import org.apache.oro.text.regex.*;

/**
 * This is a no-frills implementation of grep that demos the use of
 * PatternMatchingEngineFactory to choose different
 * regular expression engines.  It performs case insensitive matching
 * to demonstrate the use of the PatternCompilerOptions interface.
 *
 * @version @version@
 */
public final class engineExample {
  static int _file = 0;

  static final String[] _preferences = {
    PatternMatchingEngineFactory.JAVA_KEY,
    PatternMatchingEngineFactory.PERL5_KEY, 
    PatternMatchingEngineFactory.POSIX_KEY,
    PatternMatchingEngineFactory.AWK_KEY, 
    PatternMatchingEngineFactory.GLOB_KEY
  };

  // args[] is declared final so that Inner Class may reference it.
  public static final void main(final String[] args) {
    PatternMatchingEngineFactory factory;
    PatternMatchingEngine engine = null;
    PatternCompiler compiler;
    PatternCompilerOptions options;
    PatternMatcher matcher;
    MatchActionProcessor processor;
    int mask;

    if(args.length < 2) {
      System.err.println("Usage: grep <pattern> <filename> ...");
      System.exit(1);
    }

    factory = new PatternMatchingEngineFactory();

    // Demonstration of choosing engine based on preferences.
    for(int i = 0; i < _preferences.length; ++i) {
      if(factory.containsKey(_preferences[i])) {
        engine = factory.get(_preferences[i]);
        break;
      }
    }

    if(engine == null)
      engine = factory.get(PatternMatchingEngineFactory.DEFAULT_KEY);

    compiler = engine.createCompiler();
    matcher  = engine.createMatcher();
    options  = engine.getOptions();
    mask     = options.getMask(PatternCompilerOptions.CASE_INSENSITIVE);
    processor = new MatchActionProcessor(compiler, matcher);

    try {
      if(args.length > 2) {
	// Print filename before line if more than one file is specified.
	// Rely on file to point to current file being processed.
	processor.addAction(args[0], mask, new MatchAction() {
	  public void processMatch(MatchActionInfo info) {
	    info.output.println(args[_file] + ":" + info.line);
	  }
        });
      } else {
	// We rely on the default action of printing matched 
	// lines to the given OutputStream
	processor.addAction(args[0], mask);
      }

      for(_file = 1; _file < args.length; _file++)
	processor.processMatches(new FileInputStream(args[_file]), System.out);

    } catch(MalformedPatternException e) {
      System.err.println("Bad pattern.");
      e.printStackTrace();
      System.exit(1);
    } catch(IOException e) {
      System.err.println("Error opening or reading " + args[_file]);
      e.printStackTrace();
      System.exit(1);
    }
  }

}
