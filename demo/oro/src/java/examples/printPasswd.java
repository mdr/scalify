/* 
 * $Id: printPasswd.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.text.perl.*;

/**
 * This is an example program based on a short example from the Camel book.
 * It demonstrates splits by reading the /etc/passwd file (assuming you're
 * on a Unix system) and printing out the formatted entries.
 *
 * @version @version@
 */
public final class printPasswd {
  public static final String[] fieldNames = {
    "Login: ", "Encrypted password: ", "UID: ", "GID: ", "Name: ",
    "Home: ", "Shell: "
  };

  public static final void main(String args[]) {
    BufferedReader input = null;
    int field, record;
    String line;
    Perl5Util perl;
    ArrayList fields;
    Iterator it;

    try {
      input = new BufferedReader(new FileReader("/etc/passwd"));
    } catch(IOException e) {
      System.err.println("Could not open /etc/passwd.");
      e.printStackTrace();
      System.exit(1);
    }

    perl = new Perl5Util();
    record = 0;

    try {
      fields = new ArrayList();

      while((line = input.readLine()) != null) {
	fields.clear();
	perl.split(fields, "/:/", line);

	it = fields.iterator();
	field = 0;

	System.out.println("Record " + record++); 

	while(it.hasNext() && field < fieldNames.length)
	  System.out.println(fieldNames[field++] + 
			     (String)it.next());

	System.out.print("\n\n");
      }
    } catch(IOException e) {
      System.err.println("Error reading /etc/passwd.");
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
	input.close();
      } catch(IOException e) {
	System.err.println("Could not close /etc/passwd.");
	e.printStackTrace();
	System.exit(1);
      }
    }

  }

}
