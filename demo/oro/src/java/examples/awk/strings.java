/* 
 * $Id: strings.java 124053 2005-01-04 01:24:35Z dfs $
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
 * with the jakarta-oro awk package regular expression classes.  It
 * performs a function similar to the Unix <code>strings</code> command,
 * but is intended to show how matching on a stream is affected by its
 * character encoding.  The most important thing to remember is that
 * AwkMatcher only matches on 8-bit values.  If your input contains
 * Java characters containing values greater than 255, the pattern
 * matching process will result in an ArrayIndexOutOfBoundsException.
 * Therefore, if you want to search a binary file containing arbitrary
 * bytes, you have to make sure you use an 8-bit character encoding
 * like ISO-8859-1, so that the mapping between byte-values and character
 * values will be one to one.  Otherwise, the file will be interpreted
 * as UTF-8 by default, and you will probably wind up with character 
 * values outside of the 8-bit range.
 *
 * @version @version@
 */
public final class strings {

  public static final class StringFinder {
    /**
     * Default string expression.  Looks for at least 4 contiguous
     * printable characters.  Differs slightly from GNU strings command
     * in that any printable character may start a string.
     */
    public static final String DEFAULT_PATTERN =
      "[\\x20-\\x7E]{3}[\\x20-\\x7E]+";

    Pattern pattern;
    AwkMatcher matcher;

    public StringFinder(String regex) throws MalformedPatternException {
      AwkCompiler compiler = new AwkCompiler();
      pattern = compiler.compile(regex, AwkCompiler.CASE_INSENSITIVE_MASK);
      matcher = new AwkMatcher();
    }

    public StringFinder() throws MalformedPatternException {
      this(DEFAULT_PATTERN);
    }

    public void search(Reader input, PrintWriter output) throws IOException {
      MatchResult result;
      AwkStreamInput in = new AwkStreamInput(input);

      while(matcher.contains(in, pattern)) {
        result = matcher.getMatch();  
        output.println(result);
      }
      output.flush();
    }
  }


  public static final String DEFAULT_ENCODING = "ISO-8859-1";

  public static final void main(String args[]) {
    String regex = StringFinder.DEFAULT_PATTERN;
    String filename, encoding = DEFAULT_ENCODING;
    StringFinder finder;
    Reader file = null;

    // Some users thought it would be useful to use the default pattern
    // and just pass the encoding as the second parameter.  Therefore,
    // when two arguments are given and the second argument is not a valid
    // encoding, it is interpreted as a pattern.  This means you can't
    // use a valid encoding name as a pattern without also specifying
    // an encoding as a third argument.
    if(args.length < 1) {
      System.err.println("usage: strings file [pattern|encoding] [encoding]");
      return;
    } else if(args.length > 2) {
      regex = args[1];
      encoding = args[2];
    } else if(args.length > 1)
      encoding = args[1];

    filename = args[0];

    try {
      InputStream fin = new FileInputStream(filename);

      try {
        file = new InputStreamReader(fin, encoding);
      } catch(UnsupportedEncodingException uee) {
        if(args.length == 2) {
          regex    = encoding;
	  encoding = DEFAULT_ENCODING;
	  file     = new InputStreamReader(fin, encoding);
	} else
	  throw uee;
      }

      finder = new StringFinder(regex);
      finder.search(file, new PrintWriter(new OutputStreamWriter(System.out)));
      file.close();
    } catch(Exception e) {
      e.printStackTrace();
      return;
    }
  }
}
