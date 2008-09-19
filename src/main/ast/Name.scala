package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom
import scalaz.OptionW._

// *   <li>An expression like "foo.this" can only be represented as a this
// *   expression (<code>ThisExpression</code>) containing a simple name.
// *   "this" is a keyword, and therefore invalid as an identifier.</li>
// *   <li>An expression like "this.foo" can only be represented as a field
// *   access expression (<code>FieldAccess</code>) containing a this expression
// *   and a simple name. Again, this is because "this" is a keyword, and
// *   therefore invalid as an identifier.</li>
// *   <li>An expression with "super" can only be represented as a super field
// *   access expression (<code>SuperFieldAccess</code>). "super" is a also
// *   keyword, and therefore invalid as an identifier.</li>
// *   <li>An expression like "foo.bar" can be represented either as a
// *   qualified name (<code>QualifiedName</code>) or as a field access
// *   expression (<code>FieldAccess</code>) containing simple names. Either
// *   is acceptable, and there is no way to choose between them without
// *   information about what the names resolve to
// *   (<code>ASTParser</code> may return either).</li>
// *   <li>Other expressions ending in an identifier, such as "foo().bar" can
// *   only be represented as field access expressions
// *   (<code>FieldAccess</code>).</li>

trait Named { 
	val name: dom.Name
	lazy val origName: String = name.origName
	def hasSameOriginalNameAs(other: Named) = name.origName == other.name.origName
}

// NamedDecls are where we keep track of renamings - Names look to their Decl for guidance
trait NamedDecl extends Named with Modifiable
{
	private var _count = 0
	lazy val finalName = referenceName		// becomes fixed once read
	
	// pushes the variable name one further down the rename track
	def incrementName = _count += 1
	def referenceName: String = origName + (if (_count == 0) "" else "_" + _count.toString)
	def hasSameReferenceNameAs(other: NamedDecl) = referenceName == other.referenceName

	private def alternateName: String = "___" + finalName		// alternate name to use when we don't want a full rename
	def emitAlternateName: Emission = emitString(alternateName)		
}

class LocalVariableName(override val node: dom.Name, vb: VBinding) extends VariableName(node, vb)
{
	override def emitDirect: Emission = {
		log.trace("LocalVariableName: %s", segments)
		super.emitDirect
	}
}
class ParameterName(override val node: dom.SimpleName, vb: VBinding) extends VariableName(node, vb)
class FieldName(override val node: dom.SimpleName, vb: VBinding) extends VariableName(node, vb)
class QualifiedVariableName(override val node: dom.QualifiedName, vb: VBinding) extends VariableName(node, vb)
{
	lazy val QualifiedName(qual, _) = node
	private def maxMinOut(s: String) = s match { case "Character" => "CHAR" ; case "Integer" => "INT" ; case _ => allUpper(s) }
	val maxMinIn = List("Byte", "Short", "Character", "Integer", "Long")
	val maxMinNutty = List("Float", "Double")
		
	override def emitDirect: Emission = {
		log.trace("QualifiedVariableName: %s (static = %s) (tb = %s) (sq = %s) ", segments, vb.isStatic, qual.tb.getKey, vb.getStaticQualifier)
		emitScalaMathConstant | 
		(if (vb.isStatic) INVOKE(emitString(vb.getStaticQualifierPkg + "." + vb.getStaticQualifier), simpleName) else super.emitDirect)
	}
	
	private def emitScalaMathConstant: Option[Emission] = segments match {
		case SimpleName(x) :: SimpleName("MAX_VALUE") :: Nil if (maxMinIn ::: maxMinNutty) contains x => emitSome("Math.MAX_" + maxMinOut(x))
		case SimpleName(x) :: SimpleName("MIN_VALUE") :: Nil if maxMinIn contains x => emitSome("Math.MIN_" + maxMinOut(x))
		case SimpleName(x) :: SimpleName("MIN_VALUE") :: Nil if maxMinNutty contains x => emitSome("Math.EPS_" + maxMinOut(x))

		case SimpleName(x) :: SimpleName("NEGATIVE_INFINITY") :: Nil if maxMinNutty contains x => emitSome("Math.NEG_INF_" + maxMinOut(x))
		case SimpleName(x) :: SimpleName("POSITIVE_INFINITY") :: Nil if maxMinNutty contains x => emitSome("Math.POS_INF_" + maxMinOut(x))
		case SimpleName(x) :: SimpleName("NaN") :: Nil if maxMinNutty contains x => emitSome("Math.NaN_" + maxMinOut(x))
	
		case SimpleName("Math") :: SimpleName("PI") :: Nil => emitSome("Math.Pi")
		case _ => None
	}
	
	private def emitSome(s: String): Option[Emission] = Some(emitString(s))
	
	private def staticTypeRef: Option[Type] =
		if (!vb.isStatic) None
		else vb.findVariableDeclaration.map(_.jtype)
		
	private def isStaticBinaryRef = vb.isStatic && (staticTypeRef.flatMap(_.itype).map(_.isBinary) | false)	
	
	override def isStaticReference = isStaticBinaryRef || super.isStaticReference		
	override def emitNameAsStaticRef: Emission = staticTypeRef.map(x => emitString(x.tb.fqname + "." + currentName)) | super.emitNameAsStaticRef
}

abstract class VariableName(node: dom.Name, val vb: VBinding) extends Name(node) with VariableBound
{
	override def binding = vb
	override def isStaticReference = 
		!isDeclaration &&
		vb.isStatic &&
		((findEnclosingType.map(_.tb.isFactoryType) | false) || (findEnclosingMethod.map(_.mb.isConstructor) | false))

	override def emitNameAsStaticRef: Emission = emitString(vb.getStaticQualifier + "." + currentName)
	override def currentName: String = 
		if (varSegs.size > 1) varSegs.map(_.currentName).mkString(".")
		else super.currentName
}

class MethodName(node: dom.Name, val mb: MBinding) extends Name(node) with MethodBound
{
	override def binding = mb
	override def isStaticReference: Boolean = mb.isStatic 	// !isDeclaration && mb.isStatic
	override def emitNameAsStaticRef: Emission = emitString(mb.getStaticQualifier + "." + currentName)
	override def emitDirect: Emission = emitString(node.currentName)
}

class TypeName(node: dom.Name, t: TBinding) extends Name(node)
{
	require (tb.isEqualTo(t))		// sanity check
	override def binding = t
	override def isStaticReference: Boolean = !isDeclaration && tb.isMember
	override def emitNameAsStaticRef: Emission = 
		if (tb.isStatic || tb.isInterface) emitString(tb.getStaticQualifier + "." + currentName)
		else if (node.isInStatic) emitString(tb.getStaticQualifier + "#" + currentName)
		else emitString(currentName)
}

// Annoyingly, we can count on tb being non-null in (maybe) all TypeBound classes except PackageName
class PackageName(node: dom.Name, val pb: PBinding) extends Name(node) with PackageBound
{
	override def binding = pb
	override def emitDirect: Emission = emitString(pb.getName)
}

class AnnotationName(node: dom.Name, val ab: ABinding) extends Name(node) with AnnotationBound
{
	override def binding = ab
}

// if tb == null for unknown reasons
class UnboundName(node: dom.Name) extends Name(node) { 
	override def nameDeclaration: Option[NamedDecl] = None
}

// as far as we know right now, resolveBinding in Name comes back null only on label names
class LabelName(node: dom.Name) extends Name(null) {
	override def nameDeclaration: Option[NamedDecl] = None
}


// all uses of names create Name objects of an appropriate kind.  If the name
// is a declaration, the object it is declaring will have a NamedDecl trait mixed in.
class Name(override val node: dom.Name) extends Expression(node)
with NameInfo
{
	val name = node
	def declaringPkg = if (tb == null || tb.getPackage == null) "" else tb.getPackage.getName
	def emitDeclaringPkg: Emission = emitString(declaringPkg)
	
	override def emitDirect: Emission = 
		if (!isStaticReference) emitNameAsOrig
		else emitRootPkg ~ emitCurrentPkg ~ emitNameAsStaticRef
				
	def isStaticReference: Boolean = false	
	def emitNameAsStaticRef: Emission = Nil

	override def toString = segmentsString
	override def ppString: Option[String] = Some(segmentsString)
	def emitUnqualified: Emission = emitString(node.origName)
	
	// all bindings should lead back to a declared name
	def nameDeclaration: Option[NamedDecl] = binding.snode match {
		case x: NamedDecl => Some(x)
		case _ => None		
	}
	
	def currentName: String = name.nameDeclaration match {
		case Some(x) => x.referenceName
		case None => origName		
	}
	
	def unqualify: String = node match {
		case SimpleName(a) => a
		case QualifiedName(q, a) => q.unqualify + "." + a
	}
}

// A name is a list of segments arranged like so:
//   pkg.pkg.type.type.var.var.method
//

trait NameInfo
{
	val name: ASTNode

	def emitRootPkg: Emission = if (needsRoot) ROOTPKG <~> DOT ~ NOS else Nil
	def emitCurrentPkg: Emission = if (currentPkg == "") Nil else emitString(currentPkg) <~> DOT ~ NOS
	def emitNonPkgSegs: Emission = emitString(origNonPkg)
	def emitNameAsOrig: Emission = emitRootPkg ~ emitCurrentPkg ~ emitNonPkgSegs
		
	lazy val pkgSegs: List[PackageName] = segments.map(_.snode) flatMap
		{ case x: PackageName => List(x) ; case _ => Nil } filter(_.origName != "")
	lazy val typeSegs: List[TypeName] = segments.map(_.snode) flatMap
		{ case x: TypeName => List(x) ; case _ => Nil }
	lazy val varSegs: List[VariableName] = segments.map(_.snode) flatMap
		{ case x: VariableName => List(x) ; case _ => Nil }
	lazy val methodSegs: List[MethodName] = segments.map(_.snode) flatMap
		{ case x: MethodName => List(x) ; case _ => Nil }
		
	lazy val segments: List[dom.SimpleName] = getSegments(name)
	lazy val nonPkgSegments: List[dom.SimpleName] = 
		segments.filter { _.snode match { case _: PackageName => false ; case _ => true } }
	
	lazy val simpleName: dom.SimpleName = segments.last
	lazy val isDeclaration: Boolean = simpleName.isDeclaration
	lazy val origName: String = simpleName.getIdentifier
	lazy val origPkg: String = if (pkgSegs.isEmpty) "" else pkgSegs.last.pb.getName
	lazy val origNonPkg: String = Scalify.join(nonPkgSegments.map(_.currentName).filter(_ != ""), ".")
	lazy val currentPkg: String = 
		if (origPkg != "") origPkg
		else name.cu.needsPkg(origName) match {
			case Some(pkg) => pkg
			case None => ""
		}
	lazy val needsRoot: Boolean = name.cu.needsRoot(currentPkg)
	
	protected def getSegments(node: ASTNode): List[dom.SimpleName] = node match {
		case x: dom.SimpleName => List(x)
		case SimpleType(x: dom.SimpleName) => List(x)
		case SimpleType(x: dom.QualifiedName) => getSegments(x)
		case QualifiedType(x, y) => getSegments(x) ::: getSegments(y)
		case QualifiedName(x, y) => getSegments(x) ::: getSegments(y)
	}
	
	protected def segmentsString: String = Scalify.join(segments.map(segmentInfo), ".")
	private def segmentInfo(x: dom.SimpleName): String = (x.snode match {
		case x: VariableName => "var"
		case x: MethodName => "method"
		case x: PackageName => "pkg"
		case x: AnnotationName => "ann"
		case x: TypeName => "type"
		case x: LabelName => "label"
		case x => "unknown"
	}) + "(" + x.getIdentifier + ")"
}

object SToken
{
	// puts backticks around identifiers when necessary - this must be the last transformation in all cases
	def esc(s: String): String = if (scalaReservedWords contains s) "`" + s + "`" else s	
	def emitString(s: String): Emission = {
		if (s == "") return Nil
		val segments = s.split('.')
		val dot = if (s endsWith ".") "." else ""
		Emit(segments.map(esc).mkString(".") + dot)
	}
	def emitLiteral(s: String): Emission = Emit(s)
}
