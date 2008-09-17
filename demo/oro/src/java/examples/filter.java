/* 
 * $Id: filter.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.io.*;
import org.apache.oro.text.*;

/**
 * This is a sample program demonstrating how to use the regular expression
 * filename filter classes.
 *
 * @version @version@
 */
public final class filter {

  public static void printList(String[] list) {
    System.out.println();
    for(int i=0; i < list.length; i++)
      System.out.println(list[i]);
    System.out.println();
  }

  public static final void main(String[] args) {
    File dir;
    FilenameFilter filter;

    dir = new File(System.getProperty("user.dir"));

    // List all files ending in .java
    filter = new GlobFilenameFilter("*.java",
			    GlobCompiler.STAR_CANNOT_MATCH_NULL_MASK);
    System.out.println("Glob: *.java");
    printList(dir.list(filter));

    // List all files ending in .class
    filter = new AwkFilenameFilter(".+\\.class");
    System.out.println("Awk: .+\\.class");
    printList(dir.list(filter));

    // List all files ending in .java or .class
    filter = new Perl5FilenameFilter(".+\\.(?:java|class)");
    System.out.println("Perl5: .+\\.(?:java|class)");
    printList(dir.list(filter));
  }

}
