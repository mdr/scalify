package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom

// @Retention(RetentionPolicy.RUNTIME)
// @Target({ElementType.METHOD, ElementType.TYPE})
// public @interface Ignore {
// 	/**
// 	 * The optional reason why the test is ignored.
// 	 */
// 	String value() default ""; 
// }
//

// @interface @RequestForEnhancement {
//   val URL = "foo",
//   val mail = "bar"
// } class MyClass

// @RequestForEnhancement {
// 	val URL = "foo",
// 	val mail = "bar"
// } class MyClass

// @Source { val URL = "http://coders.com/" ,
//           val mail = "support@coders.com" }
// class MyScalaClass ...

// ** Three types of annotations ** 
//
//   1) Marker:
//
// public @interface MyAnnotation { }
// 
// @MyAnnotation
// public void mymethod() { ... }
//
//   2) Single Element:
//
// public @interface MyAnnotation { String doSomething(); }
// 
// @MyAnnotation ("What to do")
// public void mymethod() { ... }
//
//   3) Full-value or multi-value
//
// public @interface MyAnnotation {
//    String doSomething();
//    int count; String date();
// }
// 
// @MyAnnotation (doSomething="What to do", count=1,
//                date="09-09-2005")
// public void mymethod() { ... }
//
// ** Four types of meta-annotations **
//  Target, Retention, Documented, Inherited
//
// * @Target(ElementType.TYPE)—can be applied to any element of a class
// * @Target(ElementType.FIELD)—can be applied to a field or property
// * @Target(ElementType.METHOD)—can be applied to a method level annotation
// * @Target(ElementType.PARAMETER)—can be applied to the parameters of a method
// * @Target(ElementType.CONSTRUCTOR)—can be applied to constructors
// * @Target(ElementType.LOCAL_VARIABLE)—can be applied to local variables
// * @Target(ElementType.ANNOTATION_TYPE)—indicates that the declared type itself is an annotation type
//
// * RetentionPolicy.SOURCE—Annotations with this type will be by retained only at the source level and will be ignored by the compiler
// * RetentionPolicy.CLASS—Annotations with this type will be by retained by the compiler at compile time, but will be ignored by the VM
// * RetentionPolicy.RUNTIME—Annotations with this type will be retained by the VM so they can be read only at run-time

// Basic Annotations extend Annotation (class)
// For static type checking: StaticAnnotation (trait)  (sort of RetentionPolicy.CLASS)
// For classfile storage: ClassfileAnnotation (trait)  (RetentionPolicy.RUNTIME)

class Annotation(override val node: dom.Annotation) extends Node(node) with AnnotationBound
{
	def ab = node.resolveAnnotationBinding
	
	import Extractors._
	// http://www.scala-lang.org/intro/annotations.html
	
	def emitDirect = node match {
		case SingleMemberAnnotation(name, expr) => expr <~> COLON ~ name
		case MarkerAnnotation(name) => name
		case NormalAnnotation(name, values) => name ~ REP(values)

		//		MemberValuePair(name, value)
	}
	
	//       AnnotationTypeDeclaration => (javadoc: Opt[Javadoc]
	//                                   modifiers: List[IExtendedModifier]
	//                                        name: SimpleName
	//                            bodyDeclarations: List[BodyDeclaration])
	// AnnotationTypeMemberDeclaration => (javadoc: Opt[Javadoc]
	//                                   modifiers: List[IExtendedModifier]
	//                                        name: SimpleName
	//                                        type: Type
	//                                     default: Opt[Expression])

	// these are Body node types but make more sense here
	//
	// Body Declarations can be:
	// *   AnnotationTypeMemberDeclaration
	// *   FieldDeclaration
	// *   TypeDeclaration
	// *   EnumDeclaration
	// *   AnnotationTypeDeclaration
	//
	// public @interface Ignore  => class Ignore extends scala.Annotation
	
	// def emitAnnotationTypeMemberDeclaration(mods: List[Modifier], name: SimpleName, jtype: Type, expr: Opt[Expression]) = {
	// }	
	
	// 	Emit("// ATD:") ~ REP(mods: List[Annotation]) ~ name ~ REP(bodyDecls) ~ NL
	// case AnnotationTypeMemberDeclaration(javadoc, mods, name, jtype, expr) =>
	// 	Emit("// ATMD:") ~ REP(mods: List[Annotation]) ~ name ~ jtype ~ emitOpt(expr) ~ NL
					
	
}

//   AnnotationTypeDeclaration => /**  ... [25 lines] (<No Binding>)
//     Javadoc => /**  ... [20 lines] (<No Binding>)
//     SingleMemberAnnotation => @Retention(RetentionPolicy.RUNTIME) (<No Binding>)
//       SimpleName => Retention (Retention)
//       QualifiedName => RetentionPolicy.RUNTIME (RUNTIME)
//         SimpleName => RetentionPolicy (RetentionPolicy)
//         SimpleName => RUNTIME (RUNTIME)
//     SingleMemberAnnotation => @Target({ElementType.METHOD,ElementType.TYPE}) (<No Binding>)
//       SimpleName => Target (Target)
//       ArrayInitializer => {ElementType.METHOD,ElementType.TYPE} (<No Binding>)
//         QualifiedName => ElementType.METHOD (METHOD)
//           SimpleName => ElementType (ElementType)
//           SimpleName => METHOD (METHOD)
//         QualifiedName => ElementType.TYPE (TYPE)
//           SimpleName => ElementType (ElementType)
//           SimpleName => TYPE (TYPE)
//     Modifier => public (<No Binding>)
//     SimpleName => Ignore (Ignore)
//     AnnotationTypeMemberDeclaration => /**  ... [3 lines] (<No Binding>)
//       Javadoc => /**  ... [2 lines] (<No Binding>)
//         TagElement =>  (<No Binding>)
//           TextElement => The optional reason why the test is ignored. (<No Binding>)
//       SimpleType => String (String)
//         SimpleName => String (String)
//       SimpleName => value (value)
//       StringLiteral => "" (<No Binding>)

