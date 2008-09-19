package org.improving.scalify

import org.eclipse.jdt.core.dom
import scala.collection.immutable
import scala.collection.mutable

trait Constants
{
	// when these names are used in java code they must be fully qualified
	val scalaReservedTypes: immutable.Set[String] = immutable.HashSet(("""
Annotation Application Array Array0 ArrayLike ArrowAssoc Attribute BigDecimal BigInt
BufferedIterator ByNameFunction Cell ClassfileAnnotation ClassfileAttribute Collection
CollectionProxy Console CountedIterator Default Definite Either Ensuring Enumeration Equiv Function
Function0 Function1 Function10 Function11 Function12 Function13 Function14 Function15 Function16
Function17 Function18 Function19 Function2 Function20 Function21 Function22 Function3 Function4
Function5 Function6 Function7 Function8 Function9 FunctionFile GetClassWrapper Inclusive Iterable
IterableProxy Iterator Left LeftProjection List MatchError Math Mutable MutableProjection Nil None
NotDefinedError NotNull Option Ordered Ordering Pair PartialFunction PartialOrdering
PartiallyOrdered Predef PredicatedIterator Product Product1 Product10 Product11 Product12 Product13
Product14 Product15 Product16 Product17 Product18 Product19 Product2 Product20 Product21 Product22
Product3 Product4 Product5 Product6 Product7 Product8 Product9 ProductFile Projection Proxy PutBack
Random RandomAccessSeq RandomAccessSeqProxy Range Responder Right RightProjection RoundingMode
ScalaObject Seq SeqProxy SerialVersionUID Set32 Set64 SetXX Some StaticAnnotation StaticAttribute
Stream StringBuilder Symbol TakeWhileIterator Triple Tuple1 Tuple10 Tuple11 Tuple12 Tuple13 Tuple14
Tuple15 Tuple16 Tuple17 Tuple18 Tuple19 Tuple2 Tuple20 Tuple21 Tuple22 Tuple3 Tuple4 Tuple5 Tuple6
Tuple7 Tuple8 Tuple9 TupleFile TypeConstraint UninitializedError UninitializedFieldError Val Value
cloneable cons deprecated genprodAdvanced inline native noinline remote serializable singleton
throws transient unchecked uncheckedStable volatileArray
	""".trim.split("""\s+""").toList ++
	// the types imported by Predef that aren't in java.lang
	List("NoSuchElementException", "Function", "Map", "Set", "Pair", "Triple")) : _*)
	
	// legal java ids with special meaning in scala
	val scalaReservedWords = """apply def class forSome implicit lazy match mixin object override 
		requires sealed trait type val var with yield _""".split("""\s+""")
	
	// type aliases
	type ASTNode  = dom.ASTNode
	// type PT       = dom.PrimitiveType				// why doesn't this work?
	
	type IBinding = dom.IBinding
	type ABinding = dom.IAnnotationBinding
	type MBinding = dom.IMethodBinding
	type PBinding = dom.IPackageBinding
	type TBinding = dom.ITypeBinding
	type VBinding = dom.IVariableBinding
	
	type ICU = org.eclipse.jdt.core.ICompilationUnit
	type ModifierKeyword = dom.Modifier.ModifierKeyword
	
	type Emission = List[Emit]
	type EFilter = (Emission) => Emission
	type NFilter = (ASTNode) => Emission

	type NodeMapping = immutable.Map[ASTNode, Node]
	type MuNodeMapping = mutable.HashMap[ASTNode, Node]
	
	// imports for every file
	val commonImports = List(
		"scalify.Compat" -> true,
		"scala.collection.jcl.Conversions" -> true
	)
}