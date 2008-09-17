/* 
 * $Id: semicolon.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.text.*;
import org.apache.oro.text.regex.*;

/**
 * This is a simple example program showing how to use the MatchActionProcessor
 * class.  It reads the provided semi-colon delimited file semicolon.txt and
 * outputs only the second column to standard output.
 *
 * @version @version@
 */
public final class semicolon {

  public static final void main(String[] args) {
    MatchActionProcessor processor = new MatchActionProcessor();

    try {
      processor.setFieldSeparator(";");
      // Using a null pattern means to perform the action for every line.
      processor.addAction(null, new MatchAction() {
	public void processMatch(MatchActionInfo info) {
	  // We assume the second column exists
	  info.output.println(info.fields.get(1));
	}
      });
    } catch(MalformedPatternException e) {
      e.printStackTrace();
      System.exit(1);
    }

    try {
      processor.processMatches(new FileInputStream("semicolon.txt"),
			       System.out);
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
