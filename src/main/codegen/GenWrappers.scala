
package org.improving.scalify

import org.eclipse.jdt.core.dom
import org.eclipse.jdt.core.dom.ASTNode
import GenWrappers._

trait GenImplicits
{
    protected def get(n: ASTNode): Node = Scalify.lookup(n)
	
    implicit def enrichAbstractTypeDeclaration(n: dom.AbstractTypeDeclaration): AbstractTypeDeclaration = get(n).asInstanceOf[AbstractTypeDeclaration]
    implicit def enrichAnnotation(n: dom.Annotation): Annotation = get(n).asInstanceOf[Annotation]
    implicit def enrichAnnotationTypeDeclaration(n: dom.AnnotationTypeDeclaration): AnnotationTypeDeclaration = get(n).asInstanceOf[AnnotationTypeDeclaration]
    implicit def enrichAnnotationTypeMemberDeclaration(n: dom.AnnotationTypeMemberDeclaration): AnnotationTypeMemberDeclaration = get(n).asInstanceOf[AnnotationTypeMemberDeclaration]
    implicit def enrichAnonymousClassDeclaration(n: dom.AnonymousClassDeclaration): AnonymousClassDeclaration = get(n).asInstanceOf[AnonymousClassDeclaration]
    implicit def enrichArrayAccess(n: dom.ArrayAccess): ArrayAccess = get(n).asInstanceOf[ArrayAccess]
    implicit def enrichArrayCreation(n: dom.ArrayCreation): ArrayCreation = get(n).asInstanceOf[ArrayCreation]
    implicit def enrichArrayInitializer(n: dom.ArrayInitializer): ArrayInitializer = get(n).asInstanceOf[ArrayInitializer]
    // ??? public class ArrayType extends Type
    implicit def enrichAssertStatement(n: dom.AssertStatement): AssertStatement = get(n).asInstanceOf[AssertStatement]
    implicit def enrichAssignment(n: dom.Assignment): Assignment = get(n).asInstanceOf[Assignment]
    implicit def enrichBlock(n: dom.Block): Block = get(n).asInstanceOf[Block]
    implicit def enrichBlockComment(n: dom.BlockComment): BlockComment = get(n).asInstanceOf[BlockComment]
    implicit def enrichBodyDeclaration(n: dom.BodyDeclaration): BodyDeclaration = get(n).asInstanceOf[BodyDeclaration]
    implicit def enrichBooleanLiteral(n: dom.BooleanLiteral): BooleanLiteral = get(n).asInstanceOf[BooleanLiteral]
    implicit def enrichBreakStatement(n: dom.BreakStatement): BreakStatement = get(n).asInstanceOf[BreakStatement]
    implicit def enrichCastExpression(n: dom.CastExpression): CastExpression = get(n).asInstanceOf[CastExpression]
    implicit def enrichCatchClause(n: dom.CatchClause): CatchClause = get(n).asInstanceOf[CatchClause]
    implicit def enrichCharacterLiteral(n: dom.CharacterLiteral): CharacterLiteral = get(n).asInstanceOf[CharacterLiteral]
    implicit def enrichClassInstanceCreation(n: dom.ClassInstanceCreation): ClassInstanceCreation = get(n).asInstanceOf[ClassInstanceCreation]
    implicit def enrichComment(n: dom.Comment): Comment = get(n).asInstanceOf[Comment]
    implicit def enrichCompilationUnit(n: dom.CompilationUnit): CompilationUnit = get(n).asInstanceOf[CompilationUnit]
    implicit def enrichConditionalExpression(n: dom.ConditionalExpression): ConditionalExpression = get(n).asInstanceOf[ConditionalExpression]
    implicit def enrichConstructorInvocation(n: dom.ConstructorInvocation): ConstructorInvocation = get(n).asInstanceOf[ConstructorInvocation]
    implicit def enrichContinueStatement(n: dom.ContinueStatement): ContinueStatement = get(n).asInstanceOf[ContinueStatement]
    implicit def enrichDoStatement(n: dom.DoStatement): DoStatement = get(n).asInstanceOf[DoStatement]
    implicit def enrichEmptyStatement(n: dom.EmptyStatement): EmptyStatement = get(n).asInstanceOf[EmptyStatement]
    implicit def enrichEnhancedForStatement(n: dom.EnhancedForStatement): EnhancedForStatement = get(n).asInstanceOf[EnhancedForStatement]
    implicit def enrichEnumConstantDeclaration(n: dom.EnumConstantDeclaration): EnumConstantDeclaration = get(n).asInstanceOf[EnumConstantDeclaration]
    implicit def enrichEnumDeclaration(n: dom.EnumDeclaration): EnumDeclaration = get(n).asInstanceOf[EnumDeclaration]
    implicit def enrichExpression(n: dom.Expression): Expression = get(n).asInstanceOf[Expression]
    implicit def enrichExpressionStatement(n: dom.ExpressionStatement): ExpressionStatement = get(n).asInstanceOf[ExpressionStatement]
    implicit def enrichFieldAccess(n: dom.FieldAccess): FieldAccess = get(n).asInstanceOf[FieldAccess]
    implicit def enrichFieldDeclaration(n: dom.FieldDeclaration): FieldDeclaration = get(n).asInstanceOf[FieldDeclaration]
    implicit def enrichForStatement(n: dom.ForStatement): ForStatement = get(n).asInstanceOf[ForStatement]
    implicit def enrichIfStatement(n: dom.IfStatement): IfStatement = get(n).asInstanceOf[IfStatement]
    implicit def enrichImportDeclaration(n: dom.ImportDeclaration): ImportDeclaration = get(n).asInstanceOf[ImportDeclaration]
    implicit def enrichInfixExpression(n: dom.InfixExpression): InfixExpression = get(n).asInstanceOf[InfixExpression]
    implicit def enrichInitializer(n: dom.Initializer): Initializer = get(n).asInstanceOf[Initializer]
    implicit def enrichInstanceofExpression(n: dom.InstanceofExpression): InstanceofExpression = get(n).asInstanceOf[InstanceofExpression]
    implicit def enrichJavadoc(n: dom.Javadoc): Javadoc = get(n).asInstanceOf[Javadoc]
    implicit def enrichLabeledStatement(n: dom.LabeledStatement): LabeledStatement = get(n).asInstanceOf[LabeledStatement]
    implicit def enrichLineComment(n: dom.LineComment): LineComment = get(n).asInstanceOf[LineComment]
    implicit def enrichMarkerAnnotation(n: dom.MarkerAnnotation): MarkerAnnotation = get(n).asInstanceOf[MarkerAnnotation]
    implicit def enrichMemberRef(n: dom.MemberRef): MemberRef = get(n).asInstanceOf[MemberRef]
    implicit def enrichMemberValuePair(n: dom.MemberValuePair): MemberValuePair = get(n).asInstanceOf[MemberValuePair]
    implicit def enrichMethodDeclaration(n: dom.MethodDeclaration): MethodDeclaration = get(n).asInstanceOf[MethodDeclaration]
    implicit def enrichMethodInvocation(n: dom.MethodInvocation): MethodInvocation = get(n).asInstanceOf[MethodInvocation]
    implicit def enrichMethodRef(n: dom.MethodRef): MethodRef = get(n).asInstanceOf[MethodRef]
    implicit def enrichMethodRefParameter(n: dom.MethodRefParameter): MethodRefParameter = get(n).asInstanceOf[MethodRefParameter]
    implicit def enrichModifier(n: dom.Modifier): Modifier = get(n).asInstanceOf[Modifier]
    // ??? public abstract class Name extends Expression implements IDocElement
    implicit def enrichNormalAnnotation(n: dom.NormalAnnotation): NormalAnnotation = get(n).asInstanceOf[NormalAnnotation]
    implicit def enrichNullLiteral(n: dom.NullLiteral): NullLiteral = get(n).asInstanceOf[NullLiteral]
    implicit def enrichNumberLiteral(n: dom.NumberLiteral): NumberLiteral = get(n).asInstanceOf[NumberLiteral]
    implicit def enrichPackageDeclaration(n: dom.PackageDeclaration): PackageDeclaration = get(n).asInstanceOf[PackageDeclaration]
    // ??? public class ParameterizedType extends Type
    implicit def enrichParenthesizedExpression(n: dom.ParenthesizedExpression): ParenthesizedExpression = get(n).asInstanceOf[ParenthesizedExpression]
    implicit def enrichPostfixExpression(n: dom.PostfixExpression): PostfixExpression = get(n).asInstanceOf[PostfixExpression]
    implicit def enrichPrefixExpression(n: dom.PrefixExpression): PrefixExpression = get(n).asInstanceOf[PrefixExpression]
    // ??? public class PrimitiveType extends Type
    // ??? public class QualifiedName extends Name
    // ??? public class QualifiedType extends Type
    implicit def enrichReturnStatement(n: dom.ReturnStatement): ReturnStatement = get(n).asInstanceOf[ReturnStatement]
    // ??? public class SimpleName extends Name
    // ??? public class SimpleType extends Type
    implicit def enrichSingleMemberAnnotation(n: dom.SingleMemberAnnotation): SingleMemberAnnotation = get(n).asInstanceOf[SingleMemberAnnotation]
    // ??? public class SingleVariableDeclaration extends VariableDeclaration
    implicit def enrichStatement(n: dom.Statement): Statement = get(n).asInstanceOf[Statement]
    implicit def enrichStringLiteral(n: dom.StringLiteral): StringLiteral = get(n).asInstanceOf[StringLiteral]
    implicit def enrichSuperConstructorInvocation(n: dom.SuperConstructorInvocation): SuperConstructorInvocation = get(n).asInstanceOf[SuperConstructorInvocation]
    implicit def enrichSuperFieldAccess(n: dom.SuperFieldAccess): SuperFieldAccess = get(n).asInstanceOf[SuperFieldAccess]
    implicit def enrichSuperMethodInvocation(n: dom.SuperMethodInvocation): SuperMethodInvocation = get(n).asInstanceOf[SuperMethodInvocation]
    implicit def enrichSwitchCase(n: dom.SwitchCase): SwitchCase = get(n).asInstanceOf[SwitchCase]
    implicit def enrichSwitchStatement(n: dom.SwitchStatement): SwitchStatement = get(n).asInstanceOf[SwitchStatement]
    implicit def enrichSynchronizedStatement(n: dom.SynchronizedStatement): SynchronizedStatement = get(n).asInstanceOf[SynchronizedStatement]
    implicit def enrichTagElement(n: dom.TagElement): TagElement = get(n).asInstanceOf[TagElement]
    implicit def enrichTextElement(n: dom.TextElement): TextElement = get(n).asInstanceOf[TextElement]
    implicit def enrichThisExpression(n: dom.ThisExpression): ThisExpression = get(n).asInstanceOf[ThisExpression]
    implicit def enrichThrowStatement(n: dom.ThrowStatement): ThrowStatement = get(n).asInstanceOf[ThrowStatement]
    implicit def enrichTryStatement(n: dom.TryStatement): TryStatement = get(n).asInstanceOf[TryStatement]
    // ??? public abstract class Type extends ASTNode
    // ??? public class TypeDeclaration extends AbstractTypeDeclaration
    implicit def enrichTypeDeclarationStatement(n: dom.TypeDeclarationStatement): TypeDeclarationStatement = get(n).asInstanceOf[TypeDeclarationStatement]
    implicit def enrichTypeLiteral(n: dom.TypeLiteral): TypeLiteral = get(n).asInstanceOf[TypeLiteral]
    implicit def enrichTypeParameter(n: dom.TypeParameter): TypeParameter = get(n).asInstanceOf[TypeParameter]
    // ??? public abstract class VariableDeclaration extends ASTNode
    implicit def enrichVariableDeclarationExpression(n: dom.VariableDeclarationExpression): VariableDeclarationExpression = get(n).asInstanceOf[VariableDeclarationExpression]
    // ??? public class VariableDeclarationFragment extends VariableDeclaration
    implicit def enrichVariableDeclarationStatement(n: dom.VariableDeclarationStatement): VariableDeclarationStatement = get(n).asInstanceOf[VariableDeclarationStatement]
    implicit def enrichWhileStatement(n: dom.WhileStatement): WhileStatement = get(n).asInstanceOf[WhileStatement]
    // ??? public class WildcardType extends Type
}

object GenFactory
{	
	def apply(n: dom.ASTNode): Node = n match {
    // skipping abstract class AbstractTypeDeclaration
    // skipping abstract class Annotation
        case x: dom.AnnotationTypeDeclaration => new AnnotationTypeDeclaration(x)
        case x: dom.AnnotationTypeMemberDeclaration => new AnnotationTypeMemberDeclaration(x)
        case x: dom.AnonymousClassDeclaration => new AnonymousClassDeclaration(x)
        case x: dom.ArrayAccess => new ArrayAccess(x)
        case x: dom.ArrayCreation => new ArrayCreation(x)
        case x: dom.ArrayInitializer => new ArrayInitializer(x)
    // ??? public class ArrayType extends Type
        case x: dom.AssertStatement => new AssertStatement(x)
        case x: dom.Assignment => new Assignment(x)
        case x: dom.Block => new Block(x)
        case x: dom.BlockComment => new BlockComment(x)
    // skipping abstract class BodyDeclaration
        case x: dom.BooleanLiteral => new BooleanLiteral(x)
        case x: dom.BreakStatement => new BreakStatement(x)
        case x: dom.CastExpression => new CastExpression(x)
        case x: dom.CatchClause => new CatchClause(x)
        case x: dom.CharacterLiteral => new CharacterLiteral(x)
        case x: dom.ClassInstanceCreation => new ClassInstanceCreation(x)
    // skipping abstract class Comment
        case x: dom.CompilationUnit => new CompilationUnit(x)
        case x: dom.ConditionalExpression => new ConditionalExpression(x)
        case x: dom.ConstructorInvocation => new ConstructorInvocation(x)
        case x: dom.ContinueStatement => new ContinueStatement(x)
        case x: dom.DoStatement => new DoStatement(x)
        case x: dom.EmptyStatement => new EmptyStatement(x)
        case x: dom.EnhancedForStatement => new EnhancedForStatement(x)
        case x: dom.EnumConstantDeclaration => new EnumConstantDeclaration(x)
        case x: dom.EnumDeclaration => new EnumDeclaration(x)
    // skipping abstract class Expression
        case x: dom.ExpressionStatement => new ExpressionStatement(x)
        case x: dom.FieldAccess => new FieldAccess(x)
        case x: dom.FieldDeclaration => new FieldDeclaration(x)
        case x: dom.ForStatement => new ForStatement(x)
        case x: dom.IfStatement => new IfStatement(x)
        case x: dom.ImportDeclaration => new ImportDeclaration(x)
        case x: dom.InfixExpression => new InfixExpression(x)
        case x: dom.Initializer => new Initializer(x)
        case x: dom.InstanceofExpression => new InstanceofExpression(x)
        case x: dom.Javadoc => new Javadoc(x)
        case x: dom.LabeledStatement => new LabeledStatement(x)
        case x: dom.LineComment => new LineComment(x)
        case x: dom.MarkerAnnotation => new MarkerAnnotation(x)
        case x: dom.MemberRef => new MemberRef(x)
        case x: dom.MemberValuePair => new MemberValuePair(x)
        case x: dom.MethodDeclaration => new MethodDeclaration(x)
        case x: dom.MethodInvocation => new MethodInvocation(x)
        case x: dom.MethodRef => new MethodRef(x)
        case x: dom.MethodRefParameter => new MethodRefParameter(x)
        case x: dom.Modifier => new Modifier(x)
    // skipping abstract class Name
        case x: dom.NormalAnnotation => new NormalAnnotation(x)
        case x: dom.NullLiteral => new NullLiteral(x)
        case x: dom.NumberLiteral => new NumberLiteral(x)
        case x: dom.PackageDeclaration => new PackageDeclaration(x)
    // ??? public class ParameterizedType extends Type
        case x: dom.ParenthesizedExpression => new ParenthesizedExpression(x)
        case x: dom.PostfixExpression => new PostfixExpression(x)
        case x: dom.PrefixExpression => new PrefixExpression(x)
    // ??? public class PrimitiveType extends Type
    // ??? public class QualifiedName extends Name
    // ??? public class QualifiedType extends Type
        case x: dom.ReturnStatement => new ReturnStatement(x)
    // ??? public class SimpleName extends Name
    // ??? public class SimpleType extends Type
        case x: dom.SingleMemberAnnotation => new SingleMemberAnnotation(x)
    // ??? public class SingleVariableDeclaration extends VariableDeclaration
    // skipping abstract class Statement
        case x: dom.StringLiteral => new StringLiteral(x)
        case x: dom.SuperConstructorInvocation => new SuperConstructorInvocation(x)
        case x: dom.SuperFieldAccess => new SuperFieldAccess(x)
        case x: dom.SuperMethodInvocation => new SuperMethodInvocation(x)
        case x: dom.SwitchCase => new SwitchCase(x)
        case x: dom.SwitchStatement => new SwitchStatement(x)
        case x: dom.SynchronizedStatement => new SynchronizedStatement(x)
        case x: dom.TagElement => new TagElement(x)
        case x: dom.TextElement => new TextElement(x)
        case x: dom.ThisExpression => new ThisExpression(x)
        case x: dom.ThrowStatement => new ThrowStatement(x)
        case x: dom.TryStatement => new TryStatement(x)
    // skipping abstract class Type
    // ??? public class TypeDeclaration extends AbstractTypeDeclaration
        case x: dom.TypeDeclarationStatement => new TypeDeclarationStatement(x)
        case x: dom.TypeLiteral => new TypeLiteral(x)
        case x: dom.TypeParameter => new TypeParameter(x)
    // skipping abstract class VariableDeclaration
        case x: dom.VariableDeclarationExpression => new VariableDeclarationExpression(x)
    // ??? public class VariableDeclarationFragment extends VariableDeclaration
        case x: dom.VariableDeclarationStatement => new VariableDeclarationStatement(x)
        case x: dom.WhileStatement => new WhileStatement(x)
    // ??? public class WildcardType extends Type
    }
}

trait GenWrappers
{	
    import GenFactory._
    // skipping abstract class AbstractTypeDeclaration
    // skipping abstract class Annotation
    class AnnotationTypeDeclaration(override val node: dom.AnnotationTypeDeclaration) extends AbstractTypeDeclaration(node)
    class AnnotationTypeMemberDeclaration(override val node: dom.AnnotationTypeMemberDeclaration) extends BodyDeclaration(node)
    class AnonymousClassDeclaration(override val node: dom.AnonymousClassDeclaration) extends MiscNode(node)
    class ArrayAccess(override val node: dom.ArrayAccess) extends Expression(node)
    class ArrayCreation(override val node: dom.ArrayCreation) extends Expression(node)
    class ArrayInitializer(override val node: dom.ArrayInitializer) extends Expression(node)
    // ??? public class ArrayType extends Type
    class AssertStatement(override val node: dom.AssertStatement) extends Statement(node)
    class Assignment(override val node: dom.Assignment) extends Expression(node)
    class Block(override val node: dom.Block) extends Statement(node)
    class BlockComment(override val node: dom.BlockComment) extends Comment(node)
    // skipping abstract class BodyDeclaration
    class BooleanLiteral(override val node: dom.BooleanLiteral) extends Expression(node)
    class BreakStatement(override val node: dom.BreakStatement) extends Statement(node)
    class CastExpression(override val node: dom.CastExpression) extends Expression(node)
    class CatchClause(override val node: dom.CatchClause) extends MiscNode(node)
    class CharacterLiteral(override val node: dom.CharacterLiteral) extends Expression(node)
    class ClassInstanceCreation(override val node: dom.ClassInstanceCreation) extends Expression(node)
    // skipping abstract class Comment
    class CompilationUnit(override val node: dom.CompilationUnit) extends MiscNode(node)
    class ConditionalExpression(override val node: dom.ConditionalExpression) extends Expression(node)
    class ConstructorInvocation(override val node: dom.ConstructorInvocation) extends Statement(node)
    class ContinueStatement(override val node: dom.ContinueStatement) extends Statement(node)
    class DoStatement(override val node: dom.DoStatement) extends Statement(node)
    class EmptyStatement(override val node: dom.EmptyStatement) extends Statement(node)
    class EnhancedForStatement(override val node: dom.EnhancedForStatement) extends Statement(node)
    class EnumConstantDeclaration(override val node: dom.EnumConstantDeclaration) extends BodyDeclaration(node)
    class EnumDeclaration(override val node: dom.EnumDeclaration) extends AbstractTypeDeclaration(node)
    // skipping abstract class Expression
    class ExpressionStatement(override val node: dom.ExpressionStatement) extends Statement(node)
    class FieldAccess(override val node: dom.FieldAccess) extends Expression(node)
    class FieldDeclaration(override val node: dom.FieldDeclaration) extends BodyDeclaration(node)
    class ForStatement(override val node: dom.ForStatement) extends Statement(node)
    class IfStatement(override val node: dom.IfStatement) extends Statement(node)
    class ImportDeclaration(override val node: dom.ImportDeclaration) extends MiscNode(node)
    class InfixExpression(override val node: dom.InfixExpression) extends Expression(node)
    class Initializer(override val node: dom.Initializer) extends BodyDeclaration(node)
    class InstanceofExpression(override val node: dom.InstanceofExpression) extends Expression(node)
    class Javadoc(override val node: dom.Javadoc) extends Comment(node)
    class LabeledStatement(override val node: dom.LabeledStatement) extends Statement(node)
    class LineComment(override val node: dom.LineComment) extends Comment(node)
    class MarkerAnnotation(override val node: dom.MarkerAnnotation) extends Annotation(node)
    class MemberRef(override val node: dom.MemberRef) extends MiscNode(node)
    class MemberValuePair(override val node: dom.MemberValuePair) extends MiscNode(node)
    class MethodDeclaration(override val node: dom.MethodDeclaration) extends BodyDeclaration(node)
    class MethodInvocation(override val node: dom.MethodInvocation) extends Expression(node)
    class MethodRef(override val node: dom.MethodRef) extends MiscNode(node)
    class MethodRefParameter(override val node: dom.MethodRefParameter) extends MiscNode(node)
    class Modifier(override val node: dom.Modifier) extends MiscNode(node)
    // skipping abstract class Name
    class NormalAnnotation(override val node: dom.NormalAnnotation) extends Annotation(node)
    class NullLiteral(override val node: dom.NullLiteral) extends Expression(node)
    class NumberLiteral(override val node: dom.NumberLiteral) extends Expression(node)
    class PackageDeclaration(override val node: dom.PackageDeclaration) extends MiscNode(node)
    // ??? public class ParameterizedType extends Type
    class ParenthesizedExpression(override val node: dom.ParenthesizedExpression) extends Expression(node)
    class PostfixExpression(override val node: dom.PostfixExpression) extends Expression(node)
    class PrefixExpression(override val node: dom.PrefixExpression) extends Expression(node)
    // ??? public class PrimitiveType extends Type
    // ??? public class QualifiedName extends Name
    // ??? public class QualifiedType extends Type
    class ReturnStatement(override val node: dom.ReturnStatement) extends Statement(node)
    // ??? public class SimpleName extends Name
    // ??? public class SimpleType extends Type
    class SingleMemberAnnotation(override val node: dom.SingleMemberAnnotation) extends Annotation(node)
    // ??? public class SingleVariableDeclaration extends VariableDeclaration
    // skipping abstract class Statement
    class StringLiteral(override val node: dom.StringLiteral) extends Expression(node)
    class SuperConstructorInvocation(override val node: dom.SuperConstructorInvocation) extends Statement(node)
    class SuperFieldAccess(override val node: dom.SuperFieldAccess) extends Expression(node)
    class SuperMethodInvocation(override val node: dom.SuperMethodInvocation) extends Expression(node)
    class SwitchCase(override val node: dom.SwitchCase) extends Statement(node)
    class SwitchStatement(override val node: dom.SwitchStatement) extends Statement(node)
    class SynchronizedStatement(override val node: dom.SynchronizedStatement) extends Statement(node)
    class TagElement(override val node: dom.TagElement) extends MiscNode(node)
    class TextElement(override val node: dom.TextElement) extends MiscNode(node)
    class ThisExpression(override val node: dom.ThisExpression) extends Expression(node)
    class ThrowStatement(override val node: dom.ThrowStatement) extends Statement(node)
    class TryStatement(override val node: dom.TryStatement) extends Statement(node)
    // skipping abstract class Type
    // ??? public class TypeDeclaration extends AbstractTypeDeclaration
    class TypeDeclarationStatement(override val node: dom.TypeDeclarationStatement) extends Statement(node)
    class TypeLiteral(override val node: dom.TypeLiteral) extends Expression(node)
    class TypeParameter(override val node: dom.TypeParameter) extends MiscNode(node)
    // skipping abstract class VariableDeclaration
    class VariableDeclarationExpression(override val node: dom.VariableDeclarationExpression) extends Expression(node)
    // ??? public class VariableDeclarationFragment extends VariableDeclaration
    class VariableDeclarationStatement(override val node: dom.VariableDeclarationStatement) extends Statement(node)
    class WhileStatement(override val node: dom.WhileStatement) extends Statement(node)
    // ??? public class WildcardType extends Type
}

object GenWrappers extends GenWrappers

