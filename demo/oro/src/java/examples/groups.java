/* 
 * $Id: groups.java 124053 2005-01-04 01:24:35Z dfs $
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
 * This is a sample program mimicking the Unix groups command.  It assumes
 * the /etc/group file exists.
 *
 * @version @version@
 */
public final class groups {

  public static final void main(String[] args) {
    int user;
    MatchActionProcessor processor = new MatchActionProcessor();
    final Hashtable groups = new Hashtable();
    Vector users = new Vector();
    Enumeration usersElements;
    MatchAction action = new MatchAction() {
      public void processMatch(MatchActionInfo info) {
	// Add group name to hashtable entry
	((Vector)groups.get(info.match.toString())).addElement(
				       info.fields.get(0));
      }
    };

    if(args.length == 0) {
      // No arguments assumes calling user
      args = new String[1];
      args[0] = System.getProperty("user.name");
    }

    try {
      processor.setFieldSeparator(":");
      for(user = 0; user < args.length; user++) {
	// Screen out duplicates
	if(!groups.containsKey(args[user])) {
	  groups.put(args[user], new Vector());
	  // We assume usernames contain no special characters
	  processor.addAction(args[user], action);
	  // Add username to Vector to preserve argument order when printing
	  users.addElement(args[user]);
	}
      }
    } catch(MalformedPatternException e) {
      e.printStackTrace();
      System.exit(1);
    }

    try {
      processor.processMatches(new FileInputStream("/etc/group"),
			       System.out);
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    usersElements = users.elements();

    while(usersElements.hasMoreElements()) {
      String username;
      Enumeration values;

      username = (String)usersElements.nextElement();
      values = ((Vector)groups.get(username)).elements();

      System.out.print(username + " :");
      while(values.hasMoreElements()) {
	System.out.print(" " + values.nextElement());
      }
      System.out.println();
    }

    System.out.flush();
  }

}
