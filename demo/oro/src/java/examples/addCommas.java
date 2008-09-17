/* 
 * $Id: addCommas.java 124053 2005-01-04 01:24:35Z dfs $
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

import org.apache.oro.text.perl.*;

/**
 *
 * This is an example program based on a short example from the Camel book.
 * It demonstrates substitutions by adding commas to a the string
 * representation of an integer.
 *
 * @version @version@
 */
public final class addCommas {

  /**
   * This program takes a string as an argument and adds commas to all the
   * integers in the string exceeding 3 digits in length, placing each comma
   * three digits apart.
   */
  public static final void main(String args[]) {
    String number;
    Perl5Util perl;

    if(args.length < 1) {
      System.err.println("Usage: addCommas integer");
      System.exit(1);
    }

    number = args[0];
    perl = new Perl5Util();

    while(perl.match("/[+-]?\\d*\\d{4}/", number))
      number = perl.substitute("s/([+-]?\\d*\\d)(\\d{3})/$1,$2/", number);

    System.out.println(number);
  }

}
