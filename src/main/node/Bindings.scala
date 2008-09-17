package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom
import dom.{ PrimitiveType => PT }
import scalaz.OptionW._

// ***** getDeclaringNode *****
// * <li>package - a <code>PackageDeclaration</code></li>
// * <li>class or interface - a <code>TypeDeclaration</code> or a
// *    <code>AnonymousClassDeclaration</code> (for anonymous classes)</li>
// * <li>primitive type - none</li>
// * <li>array type - none</li>
// * <li>field - a <code>VariableDeclarationFragment</code> in a
// *    <code>FieldDeclaration</code> </li>
// * <li>local variable - a <code>SingleVariableDeclaration</code>, or
// *    a <code>VariableDeclarationFragment</code> in a
// *    <code>VariableDeclarationStatement</code> or
// *    <code>VariableDeclarationExpression</code></li>
// * <li>method - a <code>MethodDeclaration</code> </li>
// * <li>constructor - a <code>MethodDeclaration</code> </li>
// * <li>annotation type - an <code>AnnotationTypeDeclaration</code></li>
// * <li>annotation type member - an <code>AnnotationTypeMemberDeclaration</code></li>
// * <li>enum type - an <code>EnumDeclaration</code></li>
// * <li>enum constant - an <code>EnumConstantDeclaration</code></li>
// * <li>type variable - a <code>TypeParameter</code></li>
// * <li>capture binding - none</li>
// * <li>annotation binding - an <code>Annotation</code></li>
// * <li>member value pair binding - an <code>MemberValuePair</code>,
// *      or <code>null</code> if it represents a default value or a single member value</li>
//
// ***** getJavaElement *****
// * Here are the cases where a <code>null</code> should be expected:
// * <ul>
// * <li>primitive types, including void</li>
// * <li>null type</li>
// * <li>wildcard types</li>
// * <li>capture types</li>
// * <li>array types of any of the above</li>
// * <li>the "length" field of an array type</li>
// * <li>the default constructor of a source class</li>
// * <li>the constructor of an anonymous class</li>
// * <li>member value pairs</li>
// * </ul>

// Bindings => Declarations
// Type => TypeDeclaration
// Variable => VariableDeclaration
// Method => MethodDeclaration
// Package => PackageDeclaration
// Annotation => AnnotationTypeDeclaration

abstract trait Bound extends ASTNodeAdditions with Modifiable
{
	// abstract
	val node: ASTNode
	def binding: IBinding

	// modifiable
	def flags = binding.getModifiers
		
	// concrete
	def bindingName: String = binding.getName
	def jelement: Option[IJavaElement] = onull(binding.getJavaElement)
	def jproject: Option[IJavaProject] = jelement.map(_.getJavaProject)
}

trait TypeBound extends Bound {
	override def binding: IBinding = tb
	override def bindingName: String = tb.getQualifiedName
	def tb: TBinding

	// lazy val hierarchy: Option[ITypeHierarchy] = 
	// 	for (it <- itype ; jp <- jproject) yield it.newSupertypeHierarchy(null)
		// for (it <- itype ; jp <- jproject) yield it.newTypeHierarchy(jp, null)
	def itype: Option[IType] = jelement match { case Some(it: IType) => Some(it) ; case _ => None }
	// def itype: Option[IType] = jelement match { 
	// 	case Some(it: IType) => 
	// 		val op = it.getOpenable
	// 		if (op == null || op.isOpen) return Some(it)
	// 		
	// 		log.trace("!isOpen: %s", it)
	// 		op.open(null)
	// 		Some(it)
	// 	case _ => None
	// }
	def findTypeDeclaration: Option[dom.TypeDeclaration] = tb.findTypeDeclaration
	// itype.flatMap(declaration) match 
	// 	{ case Some(x: dom.TypeDeclaration) => Some(x) ; case _ => None }
	def findAnonTypeDeclaration: Option[dom.AnonymousClassDeclaration] = cu.findDeclaringNode(tb) match 
		{ case x: dom.AnonymousClassDeclaration => Some(x) ; case _ => None }	
}

trait VariableBound extends Bound {
	override def binding: IBinding = vb
	def vb: VBinding

	def findEnumDeclaration: Option[dom.EnumDeclaration] = 
		cu.findDeclaringNode(vb) match { case x: dom.EnumDeclaration => Some(x) ; case _ => None }
}

trait MethodBound extends Bound {
	override def binding: IBinding = mb
	def mb: MBinding
}

trait AnnotationBound extends Bound {
	override def binding: IBinding = ab
	def ab: ABinding
	def findAnnotationDeclaration: Option[dom.AnnotationTypeDeclaration] =
		cu.findDeclaringNode(ab) match { case x: dom.AnnotationTypeDeclaration => Some(x) ; case _ => None }
}

trait PackageBound extends Bound {
	override def binding: IBinding = pb
	def pb: PBinding
	def ipackage = jelement match { case Some(x: IPackageDeclaration) => x ; case _ => abort("ipackage") }
	def findPackageDeclaration: Option[dom.PackageDeclaration] = 
		cu.findDeclaringNode(pb) match { case x: dom.PackageDeclaration => Some(x) ; case _ => None }
}

object Bindings
{	
	// ITypeBinding already offers:
	//
	// isAnnotation(); isAnonymous(); isArray(); isAssignmentCompatible(ITypeBinding variableType);
	// isCapture(); isCastCompatible(ITypeBinding type); isClass(); isEnum(); isFromSource(); isGenericType();
	// isInterface(); isLocal(); isMember(); isNested(); isNullType(); isParameterizedType(); isPrimitive();
	// isRawType(); isSubTypeCompatible(ITypeBinding type); isTopLevel(); isTypeVariable(); isUpperbound();
	// isWildcardType();
	
	class RichITypeBinding(tb: TBinding) extends RichIBinding(tb) {
		def emitCast:	EFilter = (x: Emission) => 
			if (tb.isPrimitive) getAnyValType(tb).emitCast(x)
			else INVOKE(x, ASINSTANCEOF) <~> BRACKETS(tb.emitType)
		def emitBox:	EFilter = (x: Emission) => getAnyValType(tb).emitBoxed(x)
		def emit:		Emission = emitType
		def emitType:	Emission = tb match {
			case JArray(el, dims) => arrayWrap(dims)(el.emitType)
			case JPrimitive(anyVal) => anyVal.emit
			case _ => emitString(ROOTPKG.s + "." + tb.getPackage.getName + "." + tb.getName)		// TODO
		}
		def fqname:		String = tb.getQualifiedName
		
		def isSomeType(code: PT.Code): Boolean = tb.isPrimitive && tb.getName == code.toString
		def isSomeType(name: String): Boolean = getAnyValType(tb) == getAnyValType(name)
		
		def isAnyValType:		Boolean = isAnyValTypeName(tb.getName)
		def isReferenceType:	Boolean = !tb.isPrimitive && !tb.isNullType

		// getting more specific
		def isBoolean:		Boolean = tb.isSomeType(PT.BOOLEAN)
		def isChar:			Boolean = tb.isSomeType(PT.CHAR)
		def isString:		Boolean = tb.getQualifiedName == "java.lang.String"
		def isCharArray:	Boolean = tb.isArray && tb.getElementType.isChar
		def isVoid:			Boolean = tb.isSomeType(PT.VOID)
		
		// thanks for the lhs/rhs consistency
		def isAssignableTo(lhs: TBinding): Boolean =
			tb.getErasure.isAssignmentCompatible(lhs.getErasure)
		def isCastableTo(lhs: TBinding): Boolean =
			lhs.getErasure.isCastCompatible(tb.getErasure)

		def isAssignableTo(lhs: ASTNode): Boolean = 
			lhs.tbinding.map(isAssignableTo(_)) | false
		def isCastableTo(lhs: ASTNode): Boolean =
			lhs.tbinding.map(isCastableTo(_)) | false
			
		def isSameElementType(other: TBinding): Boolean =
			tb.isArray && other.isArray && tb.getElementType.isEqualTo(other.getElementType)

		def itype: Option[IType] = jelement match { case Some(x: IType) => Some(x) ; case _ => None }
		def findTypeDeclaration: Option[dom.TypeDeclaration] = {
			val x = itype.flatMap(declaration)
			// log.trace("findTypeDeclaration: %s %s", x.map(_.getClass), x)
			itype.flatMap(declaration) match 
			{ case Some(x: dom.TypeDeclaration) => Some(x) ; case _ => None }
		}
		override def referenceName = findTypeDeclaration.map(_.referenceName) | tb.getName

		def methods: List[MBinding] = tb.getDeclaredMethods.toList
		def pkgName: String = if (tb.getPackage == null) "" else tb.getPackage.getName
		
		override def declaringClassList: List[TBinding] = declaringClassList(tb.getDeclaringClass)
		def emitPackage: Emission = 
			if (tb.getPackage.getName == "") Nil
			else emitString(tb.getPackage.getName) <~> DOT ~ NOS
		
		// true if we had to rewire this type to use factory constructors
		def getFactoryType: Option[STDWithFactory] = findTypeDeclaration.map(_.snode) match {
			case Some(x: STDWithFactory) => Some(x)
			case _ => None
		}
		// true if we split out constants into a separate trait
		def getSplitType: Option[Interface] = findTypeDeclaration.map(_.snode) match {
			case Some(x: Interface) if x.isSplit => Some(x)
			case Some(x) => log.trace("getSplitType but not split: %s %s", x.getClass, x) ; None
			case _ => None
		}
		
		def isFactoryType: Boolean = getFactoryType.isDefined
		def isSplitType: Boolean = getSplitType.isDefined
		
		private def is(cond: Boolean, s: String): String = if (cond) s else ""
		def info: String = 
			if (tb == null) "<null>"
			else "Type " + tb.getQualifiedName + " (" + tb.getName + ") is:" +
					is(tb.isInterface, " interface") +
					is(tb.isAnonymous, " anonymous") +
					is(tb.isStatic, " static") +
					is(tb.isNested, " nested") +
					is(tb.isLocal, " local") +
					is(tb.isTopLevel, " top-level")		
	}
	
	class RichIMethodBinding(mb: MBinding) extends RichIBinding(mb) {
		def imethod: Option[IMethod] = jelement match { case Some(x: IMethod) => Some(x) ; case _ => None }
		def findMethodDeclaration: Option[dom.MethodDeclaration] = imethod.flatMap(declaration)
		
		// override def getStaticQualifiedName: String = {
		// 	val x = super.getStaticQualifiedName + "." + referenceName
		// 	log.trace("mb.name: %s", x)
		// 	x
		// }
		override def declaringClassList: List[TBinding] = declaringClassList(mb.getDeclaringClass)
		override def referenceName = findMethodDeclaration.map(_.referenceName) | mb.getName
	}
	
	class RichIVariableBinding(vb: VBinding) extends RichIBinding(vb) {		
		def declaredInConstructor: Boolean = onull(vb.getDeclaringMethod).map(_.isConstructor) | false		
		override def referenceName = findVariableDeclaration.map(_.referenceName) | vb.getName
		def findVariableDeclaration: Option[dom.VariableDeclaration] = jelement match {
			case Some(x: IField) => declaration(x)
			case Some(x: ILocalVariable) => declaration(x, vb)
			case _ => None
		}
		
		override def declaringClassList: List[TBinding] = declaringClassList(vb.getDeclaringClass)
	}
	
	class RichIBinding(val b: IBinding) extends Modifiable {
		import org.eclipse.jdt.core.BindingKey
		def referenceName: String = b.getName
		
		lazy val node: ASTNode = Global.lookup(b.getKey) | null
		lazy val snode: Node = if (node == null) null else node.snode	
		def jelement: Option[IJavaElement] = onull(b.getJavaElement)		
		def flags = b.getModifiers
		def signature: String = (new BindingKey(b.getKey)).toSignature
		def getOptDeclaringClass: Option[TBinding] = b match {
			case vb: VBinding => Some(vb.getDeclaringClass)
			case mb: MBinding => Some(mb.getDeclaringClass)
			case tb: TBinding => Some(tb)
			case _ => None
		}
		
		// def getStaticQualifiedName: String = declaringClassList.map(_.referenceName).mkString(".")
		def getStaticQualifier: String = declaringClassList.map(_.getName).mkString(".")
		def getStaticQualifierPkg: String = 
			if (declaringClassList.isEmpty) ""
			else declaringClassList.last.getPackage.getName
		
		def declaringClassList: List[TBinding] = Nil
		
		protected def declaringClassList(b: TBinding): List[TBinding] =
			if (b == null) Nil
			else declaringClassList(b.getDeclaringClass) ::: List(b)
		
		def canAccessWithoutQualifying(node: ASTNode): Boolean = {
			val bindingClass = b.getOptDeclaringClass | (return false)
			val nodeClass = node.findEnclosingType.map(_.tb) | (return false)
			
			// sadly, inside constructors the static import hasn't kicked off yet			
			bindingClass.isEqualTo(nodeClass) && !node.isInConstructor
		}		
	}
}

