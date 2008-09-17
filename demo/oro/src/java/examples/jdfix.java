/* 
 * $Id: jdfix.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.text.perl.*;


/**
 * This is an example program demonstrating how to use the PerlTools
 * match and substitute methods.
 *
 * @version @version@
 */
public final class jdfix {

  /**
   * This program performs the exact same function as this Perl script.
   * Notice that the Java program is only so much longer because of all
   * of the I/O exception handling and InputStream creation.  The core
   * while loop is EXACTLY the same length as the while loop in the Perl
   * script.  The number of substitutions performed is printed to standard
   * output as additional information.  Note, this is not an efficient way
   * to do this job; it is better to first read the entire file into a
   * character array.
   * <p>
   * This is a simple program that takes a javadoc generated HTML file as
   * input and produces as output the same HTML file, except with a white
   * background color for the body.
   * <p>
   * <pre>
   * #!/usr/bin/perl
   *
   * $#ARGV >= 1 || die "Usage: jdfix input output\n";
   *
   * open(INPUT, $ARGV[0]) || warn "Couldn't open $ARGV[0]\n";
   * open(OUTPUT, ">$ARGV[1]") || warn "Couldn't open $ARGV[1]\n";
   *
   * while(<INPUT>){
   *     s/<body>/<body bgcolor="#ffffff">/;
   *     print OUTPUT;
   * }
   * 
   * close(INPUT);
   * close(OUTPUT);
   * </pre>
   */
  public static final void main(String args[]) {
    String line;
    BufferedReader input = null;
    PrintWriter output    = null;
    Perl5Util perl;
    StringBuffer result = new StringBuffer();
    int numSubs = 0;

    if(args.length < 2) {
      System.err.println("Usage: jdfix input output");
      return;
    }

    try {
      input = 
	new BufferedReader(new FileReader(args[0]));
    } catch(IOException e) {
      System.err.println("Error opening input file: " + args[0]);
      e.printStackTrace();
      return;
    }

    try {
      output =
	new PrintWriter(new FileWriter(args[1]));
    } catch(IOException e) {
      System.err.println("Error opening output file: " + args[1]);
      e.printStackTrace();
      return;
    } 

    perl = new Perl5Util();

    try {
      while((line = input.readLine()) != null) {
	numSubs+=perl.substitute(result,
				 "s/<body>/<body bgcolor=\"#ffffff\">/", line);
	result.append('\n');
      }
      output.print(result.toString());
      System.out.println("Substitutions made: " + numSubs);
    } catch(IOException e) {
      System.err.println("Error reading from input: " + args[1]);
      e.printStackTrace();
      return;
    } finally {
      try {
	input.close();
	output.close();
      } catch(IOException e) {
	System.err.println("Error closing files.");
	e.printStackTrace();
	return;
      }
    }
  }

}
