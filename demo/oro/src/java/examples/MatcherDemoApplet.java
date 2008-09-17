/* 
 * $Id: MatcherDemoApplet.java 124053 2005-01-04 01:24:35Z dfs $
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

import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;

import org.apache.oro.text.*;
import org.apache.oro.text.awk.*;
import org.apache.oro.text.regex.*;

/**
 * This is a quickly hacked together demo of regular expression
 * matching with three different regular expression syntaxes.
 * It was originally written in JDK 1.0.2 days and hasn't changed
 * much.  It should be refactored into classes for a general purpose
 * interactive testing interface that can be run as a standalone
 * AWT application or embedded in an applet.
 * 
 * @version @version@
 */
public final class MatcherDemoApplet extends Applet {
  static int CONTAINS_SEARCH  = 0, MATCHES_SEARCH = 1;
  static int CASE_SENSITIVE = 0, CASE_INSENSITIVE = 1;

  static int PERL5_EXPRESSION = 0;
  static int AWK_EXPRESSION   = 1;
  static int GLOB_EXPRESSION  = 2;

  static String[] expressionType = {
    "Perl5 Expression:", "AWK Expression:", "Glob Expression:"
  };

  static int[] CASE_MASK[] = {
    { Perl5Compiler.DEFAULT_MASK,
      Perl5Compiler.CASE_INSENSITIVE_MASK },
    { AwkCompiler.DEFAULT_MASK,
      AwkCompiler.CASE_INSENSITIVE_MASK },
    { GlobCompiler.DEFAULT_MASK,
      GlobCompiler.CASE_INSENSITIVE_MASK }
  };

  TextField expressionField; 
  Label resultLabel, inputLabel;
  TextArea resultArea, inputArea;
  Choice expressionChoice, searchChoice, caseChoice;
  Button searchButton, resetButton;
  PatternCompiler compiler[];
  PatternMatcher matcher[];

  public MatcherDemoApplet() {
    setFont(new Font("Helvetica", Font.PLAIN, 14));
    setBackground(new Color(210, 180, 140));

    expressionChoice = new Choice();

    for(int i = 0; i < expressionType.length; ++i)
      expressionChoice.addItem(expressionType[i]);

    compiler = new PatternCompiler[expressionType.length];
    matcher  = new PatternMatcher[expressionType.length];

    compiler[PERL5_EXPRESSION] = new Perl5Compiler();
    matcher[PERL5_EXPRESSION]  = new Perl5Matcher();

    compiler[AWK_EXPRESSION]   = new AwkCompiler();
    matcher[AWK_EXPRESSION]    = new AwkMatcher();

    compiler[GLOB_EXPRESSION]  = new GlobCompiler();
    matcher[GLOB_EXPRESSION]   = matcher[PERL5_EXPRESSION];

    expressionField = new TextField(10);

    searchChoice   = new Choice();
    searchChoice.addItem("contains()");
    searchChoice.addItem("matches()");
    caseChoice     = new Choice();
    caseChoice.addItem("Case Sensitive");
    caseChoice.addItem("Case Insensitive");
    searchButton   = new Button("Search");
    resetButton    = new Button("Reset");

    resultArea    = new TextArea(20, 80);
    inputArea     = new TextArea(5, 80);
    inputLabel    = new Label("Search Input", Label.CENTER);
    resultLabel   = new Label("Search Results", Label.CENTER);
    resultArea.setEditable(false);
  }


  public void init(){
    String param;
    GridBagLayout layout;
    GridBagConstraints constraints;

    if((param = getParameter("background")) != null) {
      try {
	setBackground(new Color(Integer.parseInt(param, 16)));
      } catch(NumberFormatException e) {
	// do nothing, don't set color
      }
    }

    if((param = getParameter("fontSize")) != null) {
      Font font;

      font = getFont();

      try {
	setFont(new Font(font.getFamily(), font.getStyle(),
			 Integer.parseInt(param)));
      } catch(NumberFormatException e) {
	// do nothing, don't set font size
      }
    }

    setLayout(layout = new GridBagLayout());

    constraints = new GridBagConstraints();
    constraints.fill   = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.EAST;
    layout.setConstraints(expressionChoice, constraints);
    add(expressionChoice);

    constraints.weightx    = 1.0;
    constraints.anchor     = GridBagConstraints.WEST;
    constraints.gridwidth  = GridBagConstraints.REMAINDER;
    layout.setConstraints(expressionField, constraints);
    add(expressionField);


    constraints.gridwidth  = 1;
    layout.setConstraints(searchChoice, constraints);
    add(searchChoice);

    layout.setConstraints(caseChoice, constraints);
    add(caseChoice);

    layout.setConstraints(searchButton, constraints);
    add(searchButton);

    constraints.gridwidth  = GridBagConstraints.REMAINDER;
    layout.setConstraints(resetButton, constraints);
    add(resetButton);


    constraints.gridwidth  = GridBagConstraints.REMAINDER;
    layout.setConstraints(inputLabel, constraints);
    add(inputLabel);


    constraints.gridwidth  = GridBagConstraints.REMAINDER;
    constraints.fill   = GridBagConstraints.BOTH;
    constraints.weighty    = 0.25;
    layout.setConstraints(inputArea, constraints);
    add(inputArea);

    constraints.weighty = 0.0;
    constraints.fill   = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(resultLabel, constraints);
    add(resultLabel);

    constraints.weighty    = 1.0;
    constraints.fill   = GridBagConstraints.BOTH;
    constraints.gridheight = GridBagConstraints.REMAINDER;
    layout.setConstraints(resultArea, constraints);
    add(resultArea);
  }


  public void search(){
    int matchNum, group, caseMask, exprChoice, search;
    String text;
    MatchResult result;
    Pattern pattern;
    PatternMatcherInput input;

    resultArea.setText("");
    text       = expressionField.getText();
    exprChoice = expressionChoice.getSelectedIndex();
    caseMask   = CASE_MASK[exprChoice][caseChoice.getSelectedIndex()];

    resultArea.appendText("Compiling regular expression.\n");

    try {
      pattern = compiler[exprChoice].compile(text, caseMask);
    } catch(MalformedPatternException e){
      resultArea.appendText("\nMalformed Regular Expression:\n" +
			  e.getMessage());
      return;
    }

    search   = searchChoice.getSelectedIndex();
    text     = inputArea.getText();
    matchNum = 0;

    resultArea.appendText("\nSearching\n\n");


    if(search == MATCHES_SEARCH) {
      if(matcher[exprChoice].matches(text, pattern))
	resultArea.appendText("The input IS an EXACT match.\n");
      else
	resultArea.appendText("The input IS NOT an EXACT match.\n");
    } else {
      input    = new PatternMatcherInput(text);

      while(matcher[exprChoice].contains(input, pattern)) {
	int groups;

	result = matcher[exprChoice].getMatch();
	++matchNum;

	resultArea.appendText("Match " + matchNum + ": " +
			      result.group(0)+ "\n");
	groups = result.groups();

	if(groups > 1){
	  resultArea.appendText("    Subgroups:\n");
	  for(group=1; group < groups; group++){
	    resultArea.appendText("    " + group + ": " +
				  result.group(group) + "\n");
	  }
	}
      }

      resultArea.appendText("\nThe input contained " + matchNum + " matches.");
    }
 
  }

  public boolean action(Event event, Object arg) {
    if(event.target == searchButton){
      search();
      return true;
    } else if(event.target == resetButton) {
      resultArea.setText("");
      inputArea.setText("");
      expressionField.setText("");
      return true;
    }

    return false;
  }
}
