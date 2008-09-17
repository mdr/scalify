package org.improving.scalify

import Scalify._
import Bindings._
import org.eclipse.jdt.core._
import org.eclipse.jdt.core.dom

//
// Keep all the implicits in one place to minimize surprises
//

trait Implicits extends SafeImplicits with WrapperImplicits
{		
	// Pimping Emits and Emissions to enable custom operators
	implicit def emitEmissionExtras(xs: Emission) = EmissionExtras(xs) 
	implicit def emitExtras(x: Emit) = EmissionExtras(List(x): Emission)
	
	// These let us work on Emits & ASTNodes directly
	implicit def emitToEmission(x: Emit): Emission = Emission(x)									// Emit -> Emission

	// bindings
	implicit def enrichIB(x: IBinding): RichIBinding = new RichIBinding(x)	
	implicit def enrichITB(x: TBinding): RichITypeBinding = new RichITypeBinding(x)
	implicit def enrichIMB(x: MBinding): RichIMethodBinding = new RichIMethodBinding(x)
	implicit def enrichIVB(x: VBinding): RichIVariableBinding = new RichIVariableBinding(x)

	// Bound nodes to their enriched bindings
	implicit def enrichIBound(x: Bound): RichIBinding = new RichIBinding(x.binding)
	implicit def enrichTBound1(x: TypeBound): TBinding = x.tb
	implicit def enrichTBound2(x: TypeBound): RichITypeBinding = new RichITypeBinding(x.tb)
	implicit def enrichMBound(x: MethodBound): RichIMethodBinding = new RichIMethodBinding(x.mb)
	implicit def enrichVBound(x: VariableBound): RichIVariableBinding = new RichIVariableBinding(x.vb)
	
	// java model
	implicit def enrichJE(x: IJavaElement): RichIJavaElement = new RichIJavaElement(x)
	implicit def enrichIMEM(x: IMember): RichIMember = new RichIMember(x)
	implicit def enrichITYPE(x: IType): RichIType = new RichIType(x)
	implicit def enrichIMETH(x: IMethod): RichIMethod = new RichIMethod(x)
		
	// XXX should use OptionW uniformly
	implicit def optionToBoolean(x: Option[Boolean]): Boolean = x getOrElse false
	
    // nodes to emissions
	implicit def nodeToEmission[T <: ASTNode](x: T): Emission = enrichNode(x).emit				// ASTNode -> Emission
	implicit def nodesToEmission[T <: ASTNode](xs: List[T]): List[Emission] =
		xs.map(nodeToEmission[T])
	implicit def scalifyNodesToEmissions[T <: ASTNode](xs: List[Node]): List[Emission] =
		nodesToEmission(xs.map(_.node))

	// These de-option the option, which is usually what we want - if not, be explicit
	implicit def optionToEmission(x: Option[Emission]) = if (x.isEmpty) Nil else x.get
	implicit def optionEmitToEmission(x: Option[Emit]) = if (x.isEmpty) Nil else List(x)
	implicit def optionASTNodeToEmission[T <: ASTNode](x: Option[T]) = if (x.isEmpty) Nil else nodeToEmission(x.get)
}
object Implicits extends Implicits

trait WrapperImplicits
{
	// custom implicits on these classes 
	implicit def enrichNode(x: dom.ASTNode): Node = Scalify.lookup(x)
	implicit def enrichTypeDeclaration(x: dom.TypeDeclaration): TypeDeclaration =
		Scalify.lookup(x).asInstanceOf[TypeDeclaration]
		
	implicit def enrichName(x: dom.Name): Name = Scalify.lookup(x).asInstanceOf[Name]
	implicit def enrichVariableDeclaration(x: dom.VariableDeclaration): VariableDeclaration =
		Scalify.lookup(x).asInstanceOf[VariableDeclaration]
	implicit def enrichType(x: dom.Type): Type = Scalify.lookup(x).asInstanceOf[Type]
}
