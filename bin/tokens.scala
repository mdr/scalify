import scala.collection.immutable._

// scala token names
val tokens = List(
	("NL" -> """\\n"""),
	("LBRACKET" -> "["),
	("RBRACKET" -> "]"),
	("LBRACE" -> "{"),
	("RBRACE" -> "}"),
	("LPAREN" -> "("),
	("RPAREN" -> ")"),
	("EMPTYBRACES" -> "{ }"),

	("ARROWLEFT" -> "<-"),
	("ARROWRIGHT" -> "->"),
	("BOUNDUPPER" -> "<:"),
	("BOUNDLOWER" -> ">:"),
	("COLON" -> ":"),
	("COMMA" -> ","),
	("COMPLEMENT" -> "~"),
	("DOT" -> "."),
	("EQUALS" -> "="),
	("FUNARROW" -> "=>"),
	("ISEQUALTO" -> "=="),
	("ISEQUALTOREF" -> "eq"),
	("LOGICALAND" -> "&&"),
	("LOGICALOR" -> "||"),
	("MINUS" -> "-"),
	("MINUSEQUALS" -> "-="),
	("NOT" -> "!"),
	("PIPE" -> "|"),
	("PLUS" -> "+"),
	("PLUSEQUALS" -> "+="),
	("QUOTE" -> "\\\\\""),
	("SEMICOLON" -> ";"),
	("UNDERSCORE" -> "_"),

	("ABSTRACT" -> "abstract"),
	("APPLY" -> "apply"),
	("ASINSTANCEOF" -> "asInstanceOf"),
	("ASSERT" -> "assert"),
	("BOOLEAN" -> "Boolean"),
	("BYTE" -> "Byte"), 
	("CASE" -> "case"),
	("CATCH" -> "catch"),
	("CHAR" -> "Char"),
	("CHARACTER" -> "Character"),
	("CLASS" -> "class"),
	("CLASSOF" -> "classOf"),
	("DEF" -> "def"),
	("DO" -> "do"),
	("DOUBLE" -> "Double"),
	("ELSE" -> "else"),
	("EXTENDS" -> "extends"),
	("FALSE" -> "false"),
	("FINAL" -> "final"),
	("FINALLY" -> "finally"),
	("FLOAT" -> "Float"),
	("FOR" -> "for"),
	("IF" -> "if"),
	("IMPORT" -> "import"),
	("INT" -> "Int"),
	("INTEGER" -> "Integer"),
	("ISINSTANCEOF" -> "isInstanceOf"),
	("LONG" -> "Long"),
	("MATCH" -> "match"),
	("NEW" -> "new"),
	("NATIVE" -> "@native"),
	("NULL" -> "null"),
	("OBJECT" -> "object"),
	("OVERRIDE" -> "override"),
	("PACKAGE" -> "package"),
	("PRIVATE" -> "private"),
	("PROTECTED" -> "protected"),
	("RETURN" -> "return"),
	("SHORT" -> "Short"),
	("SUPER" -> "super"),
	("SYNCHRONIZED" -> "synchronized"),
	("THIS" -> "this"),
	("THROW" -> "throw"),
	("TRAIT" -> "trait"),
	("TRANSIENT" -> "@transient"),
	("TRUE" -> "true"),
	("TRY" -> "try"),
	("VAL" -> "val"),
	("VAR" -> "var"),
	("VOLATILE" -> "@volatile"),
	("WHILE" -> "while"),
	("WITH" -> "with"),
	("YIELD" -> "yield"),

	// not language keywords but needed nonetheless
	("ANY" -> "Any"),
	("ANYREF" -> "AnyRef"),
	("ARRAY" -> "Array"),
	("BY" -> "by"),
	("ENUMCLASS" -> "scala.Enumeration"),
	("GETORELSE" -> "getOrElse"),
	("INTERNALRETURN" -> "InternalReturnException"),
	("JAVALANG" -> "java.lang"),
	("LPARENDOUBLE" -> "(("),
	("MINUSONE" -> "-1"),
	("NONE" -> "None"),
	("ONE" -> "1"),
	("OPTION" -> "Option"),
	("PRODUCT" -> "Product"),
	("RPARENDOUBLE" -> "))"),
	("ROOTPKG" -> "_root_"),
	("SOME" -> "Some"),
	("TO" -> "to"),
	("TUPLE1" -> "Tuple1"),
	("UNIT" -> "Unit"),
	("UNTIL" -> "until"),
	("VALUE" -> "Value"),
	("VALUEOF" -> "valueOf"),
	("X" -> "x"),
	("ZERO" -> "0"),
	
	// control structures
	("BREAK" -> "Break"),
	("CONTINUE" -> "Continue"),
	("LBREAK" -> "LabeledBreak"),
	("LCONTINUE" -> "LabeledContinue"),
	("RETCLASS" -> "ReturnBox"),
	("RETVAL" -> "Return"),

	// java semantics
	("SETEQ" -> "seteq"),			// assignment that yields result
	("SETEQLHS" -> "seteqlhs"),	// like seteq but result of expr is original lhs
	("UPDATEFUN" -> "update")
)
	
val output =
"""package org.improving.scalify

trait Tokens
{
##TOKENS##
}
"""

val txt = tokens.map(x => "    val " + x._1 + " = Emit(\"" + x._2 + "\")").mkString("\n")
println(output.replaceAll("##TOKENS##", txt))
