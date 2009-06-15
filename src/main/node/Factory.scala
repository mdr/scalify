package org.improving.scalify

import org.eclipse.jdt.core.dom
import scala.collection.mutable.ListBuffer

object NodeFactory
{
	import ScalifySafe._
	
	private[NodeFactory] def isEqualsMethod(node: dom.MethodDeclaration): Boolean = 
		isEqualsMethod(node.resolveBinding, node.getRoot.cu.objectEqualsMB)
	private[NodeFactory] def isEqualsMethod(mb: MBinding, objectEQ: MBinding): Boolean = 
		mb != null && mb.overrides(objectEQ)
	
	object SType {
		def apply(node: dom.Type): Type = node match {
			case x: dom.PrimitiveType		=> new PrimitiveType(x)
			case x: dom.SimpleType			=>
				if (isBoxedTypeName(x.getName.getFullyQualifiedName)) new BoxedType(x)
				else new SimpleType(x)
			case x: dom.ArrayType			=> new ArrayType(x)
			case x: dom.ParameterizedType	=> new ParameterizedType(x)
			case x: dom.QualifiedType		=> new QualifiedType(x)
			case x: dom.WildcardType		=> new WildcardType(x)
			case _							=> GenFactory(node).asInstanceOf[Type]
		}
	}
	
	object SName {		
		def apply(node: dom.Name): Name = node.resolveBinding match {
			case null => createNullBoundName(node)
			case b: MBinding => new MethodName(node, b)
			case b: TBinding => new TypeName(node, b)
			case b: PBinding => new PackageName(node, b)
			case b: ABinding => new AnnotationName(node, b)
			case b: VBinding => createVariableName(node, b)
			case _ => abort("Unknown binding type")
		}
		
		// array .length fields are conveniently special-cased in the jdt
		// if (node.fqname == "length" && b.getDeclaringClass == null) new LocalVariableName(node, b)
		private def createVariableName(node: dom.Name, b: VBinding): Name = 
			node match {
				case x: dom.SimpleName =>
					if (isEqualsParameterRef(x, b)) new LocalVariableName(x, b) {
						import Scalify._
						lazy val default = super.emitDirect
						override def emitDirect: Emission = node.getParent match {
							case _: dom.CastExpression => default
							case _: dom.InstanceofExpression => default
							case _ => INVOKE(super.emitDirect, ASINSTANCEOF) <~> BRACKETS(ANYREF)
						}
					}
					else if (b.isField) new FieldName(x, b)
					else if (b.isParameter) new ParameterName(x, b)
					else new LocalVariableName(x, b)
				case x: dom.QualifiedName => new QualifiedVariableName(x, b)
			}
		
		private def createNullBoundName(node: dom.Name): Name = {
			lazy val default = new UnboundName(node)
			node match {
				case x: dom.QualifiedName => default
				case x: dom.SimpleName => x.getParent match {
					case _:LabeledStatement | _:BreakStatement | _:ContinueStatement => new LabelName(x)
					case _ => default
				}
			}
		}
		
		private def isEqualsParameterRef(node: dom.SimpleName, b: VBinding): Boolean = 
			!node.isDeclaration && isEqualsMethod(b.getDeclaringMethod, node.getRoot.cu.objectEqualsMB) && b.isParameter
	}
			
	object SVariableDeclaration {
		def apply(node: dom.VariableDeclaration): VariableDeclaration = {
			val vb = node.resolveBinding
			val method = node.findEnclosingMethod
			
			if (vb == null) {
				log.debug("Unbound variable in %s: %s", method.get, node.getClass.getName)
				new LocalVariable(node, method.get)
			}
			// abort("Unbound variable: " + node)	// new LocalVariable(node, method.get)
			else node match {
				case x: dom.SingleVariableDeclaration if vb.isParameter => new Parameter(x, x.findEnclosingMethod.get)
				case x: dom.SingleVariableDeclaration => new LocalVariable(x, x.findEnclosingMethod.get)
				case x: dom.VariableDeclarationFragment if vb.isField => new Field(x)
				case x: dom.VariableDeclarationFragment => new LocalVariable(x, x.findEnclosingMethod.get)
				case _							=> GenFactory(node).asInstanceOf[VariableDeclaration]
			}
		}
	}
	
	object SExpr {
		import dom.PrefixExpression.Operator
		def apply(node: dom.Expression): Expression = {
			node match {
				case x: dom.PrefixExpression => x.getOperator match {
					case Operator.INCREMENT => new PrefixExpression(x) with Assigns
					case Operator.DECREMENT => new PrefixExpression(x) with Assigns
					case _ => new PrefixExpression(x)
				}
										
				case _ => GenFactory(node).asInstanceOf[Expression]
			}
		}
	}
	
	 // * The name specified in a non-static single-type import can resolve
	 // * to a type (only). The name specified in a non-static on-demand
	 // * import can itself resolve to either a package or a type.
	 // * For static imports (introduced in JLS3), the name specified in a
	 // * static on-demand import can itself resolve to a type (only).
	 // * The name specified in a static single import can resolve to a
	 // * type, field, or method; in cases where the name could be resolved
	 // * to more than one element with that name (for example, two
	 // * methods both named "max", or a method and a field), this method
	 // * returns one of the plausible bindings.
	object SMisc {
		def apply(node: dom.ASTNode): MiscNode = node match {
			case x: dom.ImportDeclaration => x.resolveBinding match {
				case b: MBinding => new ImportDeclaration(x) with MethodBound { val mb = b }
				case b: TBinding => new ImportDeclaration(x) with TypeBound { val tb = b }
				case b: PBinding => new ImportDeclaration(x) with PackageBound { val pb = b }
				case b: VBinding => new ImportDeclaration(x) with VariableBound { val vb = b }
				case _ => GenFactory(node).asInstanceOf[MiscNode]
			}
			case _ => GenFactory(node).asInstanceOf[MiscNode]
		}
	}
	
	object SStatement {
		def apply(node: dom.Statement): Statement = GenFactory(node).asInstanceOf[Statement]
	}
	
	object SBodyDeclaration {
		import Implicits.enrichITB
		
		def apply(node: dom.BodyDeclaration): BodyDeclaration = node match {
			case x: dom.TypeDeclaration => createType(x)
			case x: dom.MethodDeclaration   => createMethod(x)				
			case _ => GenFactory(node).asInstanceOf[BodyDeclaration]
		}
		
		def createType(node: dom.TypeDeclaration): TypeDeclaration = {
			val methods: List[dom.MethodDeclaration] = node.getMethods.toList
			val constructors = methods.filter(_.isConstructor)
			val indCons = constructors.filter(isIndependent)
			val tb = node.resolveBinding
			// println(tb.info)
			
			if (node.isInterface) new Interface(node)
			else if (constructors.size == 0) new STDNoConstructor(node)
			else if (indCons.size == 1) new STDWithPrimary(node, indCons.head)
			else new STDWithFactory(node)
		}
		
		def createMethod(node: dom.MethodDeclaration): MethodDeclaration = {
			if (isEqualsMethod(node))		new EqualsMethod(node)
			else if (!node.isConstructor)	new MethodDeclaration(node)
			else if (isIndependent(node))	new IndependentConstructor(node)
			else							new DependentConstructor(node)
		}
		
		private def isIndependent(node: dom.MethodDeclaration): Boolean = {
			val body = onull(node.getBody)
			val stmts: List[ASTNode] = body.map(_.statements.toList) getOrElse Nil				
			stmts match {
				case (x: dom.ConstructorInvocation) :: _ => false
				case _ => true
			}
		}
	}
	
	object SAnnotation {
		def apply(node: dom.Annotation): Annotation = GenFactory(node).asInstanceOf[Annotation]
	}
	object SComment {
		def apply(node: dom.Comment): Comment = GenFactory(node).asInstanceOf[Comment]
	}

	def evaluate(node: ASTNode): Option[Node] = {
		implicit def toOpt[T](x: T): Option[T] = Some(x)
		node match {
			case x: dom.Annotation			=> SAnnotation(x)				// Annotation ext. Expression
			case x: dom.Name				=> SName(x)						// Name ext. Expression
			case x: dom.Comment             => SComment(x)
			case x: dom.BodyDeclaration		=> SBodyDeclaration(x)
			case x: dom.Expression          => SExpr(x)
			case x: dom.Type                => SType(x)
			case x: dom.VariableDeclaration => SVariableDeclaration(x)
			case x: dom.Statement			=> SStatement(x)
			case _							=> SMisc(node)
	    }
	}	
}

object LoopInfo
{
	def useFunction(root: dom.Statement): Boolean = {
		val lc = new LoopClassifier(root)
		return lc.useFunction
	}
	
	class LoopClassifier(root: dom.Statement) extends dom.ASTVisitor
	{
		private var _useFunction = false
		lazy val useFunction = _useFunction
		
		root.accept(this)
		
		override def visit(node: dom.BreakStatement): Boolean = { _useFunction = true ; false }
		override def visit(node: dom.ContinueStatement): Boolean = { _useFunction = true ; false }
		override def visit(node: dom.ReturnStatement): Boolean =  { _useFunction = true ; false }
	}
}
