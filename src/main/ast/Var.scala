package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom

case class ParameterList(val svds: List[dom.SingleVariableDeclaration]) {
	val params: List[Parameter] = svds.map(_.snode.asInstanceOf[Parameter])
	def emitList: List[Emission] = params.map(_.emit)
	def emitOriginalList: List[Emission] = params.map(_.emitOriginalName)
	def emitPrimaryList: List[Emission] = params.map(_.emitPrimary)
	def emitRenamings: List[Emission] = {
		val renamings = params.map(_.emitRenaming).filter(_ != Nil)
		if (renamings == Nil) Nil
		else renamings ::: List(List(NL))
	}
}

class Parameter(override val node: dom.SingleVariableDeclaration, val method: dom.MethodDeclaration) extends VariableDeclaration(node)
{
	require(vb.isParameter)
	override def needsType: Boolean = true
	lazy val SingleVariableDeclaration(modifiers, jtype, isVarargs, name, dims, init) = node
	
	def collisionSearchScope = method
	// def collisionSearchScope = if (method.isConstructor) method.dtype else method
	def useAlternateName = 
		if (method.isConstructor && method.isPrimary) false
		else isUsedInAssignment(collisionSearchScope)
		
	def emitRenaming: Emission = if (useAlternateName) VAR ~ name ~ EQUALS ~ emitAlternateName ~ NL else Nil
	def isVar: Emission = {
		if (method.isConstructor && method.isPrimary) VAR else Nil
		// val mb = vb.getDeclaringMethod
		// mb.findMethodDeclaration match {
		// 	case Some(x) if x.isConstructor => x.snode match {
		// 		case x: Constructor if x.isPrimary => VAR
		// 		case _ => Nil
		// 	}
		// 	case _ => Nil
		// }
	}
	
	override def emitDirect: Emission = {
		val aName = if (useAlternateName) emitAlternateName else name.emit	
		emitWithName(aName)
	}
	def emitOriginalName: Emission = emitWithName(name.emit)
	def emitPrimary: Emission = VAR ~ emitDirect
	
	private def emitWithName(aName: Emission): Emission = 
		aName ~ COLON ~ arrayWrap(dims)(jtype.emitDirect(node)) ~ emitCond(isVarargs, NOS ~ Emit("*"))
}

class Field(override val node: dom.VariableDeclarationFragment) extends VariableDeclaration(node)
{
	require(vb.isField)
	override def emitDefaultInitializer: Emission = ifDims(UNDERSCORE)

	lazy val VariableDeclarationFragment(name, dims, init) = node
	lazy val FieldDeclaration(_, modifiers, jtype, _) = parent	
}

class LocalVariable(node: dom.VariableDeclaration, val method: dom.MethodDeclaration) extends VariableDeclaration(node)
{
	require (!vb.isField && !vb.isParameter)
	override def emitDefaultInitializer: Emission = ifDims(jtype.emitDefaultValue)
	
	lazy val (modifiers, jtype, name, dims, init) = node match {
		case SingleVariableDeclaration(m, j, _, n, d, i) => (m, j, n, d, i)
		case VariableDeclarationFragment(n, d, i) => parent match {
			case VariableDeclarationExpression(m, j, _) => (m, j, n, d, i)
			case VariableDeclarationStatement(m, j, _) => (m, j, n, d, i)
			case FieldDeclaration(_, m, j, _) => (m, j, n, d, i)
		}
	}
	
	override def emitDirect: Emission = {
		log.trace("LocalVariable emitDirect: %s %s", name, modifiers)
		super.emitDirect
	}
}

//         VariableDeclarationStatement => char[] modifierFlags, posFlags={0}, negFlags={0};
//           VariableDeclarationFragment => negFlags={0}
//             ArrayInitializer => {0}
//               NumberLiteral => 0
//             SimpleName => negFlags
//           VariableDeclarationFragment => posFlags={0}
//             ArrayInitializer => {0}
//               NumberLiteral => 0
//             SimpleName => posFlags


abstract class VariableDeclaration(override val node: dom.VariableDeclaration)
extends Node(node) with VariableBound with NamedDecl
{
	override def toString: String = name.fqname + (if (vb.isField) "" else " (local)")
	override def ppString = Some(toString)
	
	def vb = node.resolveBinding
	def emitDefaultInitializer: Emission = Nil
	// require(vb != null)
	
	val modifiers: List[dom.IExtendedModifier]
	val jtype: dom.Type
	val name: dom.SimpleName
	val dims: Int
	val init: Option[dom.Expression]
	
	// so far the logic is: if there's no initializer, it must be a var because
	// unlike java we can't declare a final and assign to it later.
	// Otherwise, if it's final it's a val.
	private def isInitializedVal: Boolean = isFinal && !init.isEmpty
	private def isUninitializedVal: Boolean = isFinal && init.isEmpty
	def isVolatileVar: Boolean = isFinal && timesUsedInAssignment(findEnclosingScope) > 1
	def isDeferredVal: Boolean = isUninitializedVal && timesUsedInAssignment(findEnclosingScope) <= 1
	
	def emitValOrVar: Emission =
		if (isVolatileVar) VOLATILE ~ VAR
		else if (isFinal) VAL
		else VAR
	
	protected def ifDims(x: Emission): Emission = if (dims > 0) NULL else x
	protected def needsType: Boolean = {
		val retVal = 
			init.isEmpty || 
			!init.get.tb.isEqualTo(jtype.tb) ||
			init.get.tb.isFactoryType ||
			(jtype.tb.isArray && jtype.tb.getElementType.getName == "char")

		// if (retVal) log.trace("Annotating type in %s: %s != %s", 
		// 				toString, init.map(_.tb.getKey).getOrElse("<none>"), jtype.tb.getKey)
			
		retVal
	}
	
	// looks under the supplied root for assignments to this name
	def isUsedInAssignment(root: ASTNode): Boolean = timesUsedInAssignment(root) > 0
	def timesUsedInAssignment(root: ASTNode): Int = {		
		// log.trace("Testing if %s is assigned to under %s", name, root.id)
		def opDoesModify(op: String) = (op == "++" || op == "--")

		val assigns = root.descendantExprs filter {
			case Assignment(lhs: dom.SimpleName, _, _) if compareSimpleNames(lhs, name) => true
			case PostfixExpression(lhs: dom.SimpleName, _) if compareSimpleNames(lhs, name) => true
			case PrefixExpression(JavaOp(op), lhs: dom.SimpleName) if compareSimpleNames(lhs, name) && opDoesModify(op) => true
			case _ => false
		}
		
		assigns.size
	}

	override def emitDirect: Emission = if (isDeferredVal) Nil else emitNameDeclaration ~ emitInitDeclaration
	private def emitNameDeclaration: Emission = name ~ (if (needsType) COLON ~ arrayWrap(dims)(jtype.emitDirect(node)) else Nil)
	private def emitInitDeclaration: Emission = EQUALS ~ (if (init.isDefined) init.get else emitDefaultInitializer)
	
	// this is called when we had to move the declaration site - the context is the assignment node
	override def emitDirect(context: ASTNode): Emission = context match {
		case x: dom.Assignment if isDeferredVal => VAL ~ emitNameDeclaration
		case _ => abort("Var declaration proposed at non-assignment node ")
	}
}

