
package org.improving.scalify

import Scalify._
import org.eclipse.jdt.core.dom


//           AnnotationTypeDeclaration => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                            name: SimpleName
//                                bodyDeclarations: List[BodyDeclaration])
//     AnnotationTypeMemberDeclaration => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                            name: SimpleName
//                                            type: Type
//                                         default: Opt[Expression])
//           AnonymousClassDeclaration => (bodyDeclarations: List[BodyDeclaration])
//                         ArrayAccess => (array: Expression, index: Expression)
//                       ArrayCreation => (type: ArrayType
//                                   dimensions: List[Expression]
//                                  initializer: Opt[ArrayInitializer])
//                    ArrayInitializer => (expressions: List[Expression])
//                           ArrayType => (componentType: Type)
//                     AssertStatement => (expression: Expression, message: Opt[Expression])
//                          Assignment => (leftHandSide: Expression
//                                             operator: Operator
//                                        rightHandSide: Expression)
//                               Block => (statements: List[Statement])
//                      BooleanLiteral => (booleanValue: Boolean)
//                      BreakStatement => (label: Opt[SimpleName])
//                      CastExpression => (type: Type, expression: Expression)
//                         CatchClause => (exception: SingleVariableDeclaration, body: Block)
//                    CharacterLiteral => (escapedValue: String)
//               ClassInstanceCreation => (expression: Opt[Expression]
//                                      typeArguments: List[Type]
//                                               type: Type
//                                          arguments: List[Expression]
//                          anonymousClassDeclaration: Opt[AnonymousClassDeclaration])
//                     CompilationUnit => (package: Opt[PackageDeclaration]
//                                         imports: List[ImportDeclaration]
//                                           types: List[AbstractTypeDeclaration])
//               ConditionalExpression => (expression: Expression
//                                     thenExpression: Expression
//                                     elseExpression: Expression)
//               ConstructorInvocation => (typeArguments: List[Type], arguments: List[Expression])
//                   ContinueStatement => (label: Opt[SimpleName])
//                         DoStatement => (expression: Expression, body: Statement)
//                EnhancedForStatement => (parameter: SingleVariableDeclaration
//                                        expression: Expression
//                                              body: Statement)
//             EnumConstantDeclaration => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                            name: SimpleName
//                                       arguments: List[Expression]
//                       anonymousClassDeclaration: Opt[AnonymousClassDeclaration])
//                     EnumDeclaration => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                            name: SimpleName
//                             superInterfaceTypes: List[Type]
//                                   enumConstants: List[EnumConstantDeclaration]
//                                bodyDeclarations: List[BodyDeclaration])
//                 ExpressionStatement => (expression: Expression)
//                         FieldAccess => (expression: Expression, name: SimpleName)
//                    FieldDeclaration => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                            type: Type
//                                       fragments: List[VariableDeclarationFragment])
//                        ForStatement => (initializers: List[Expression]
//                                           expression: Opt[Expression]
//                                             updaters: List[Expression]
//                                                 body: Statement)
//                         IfStatement => (expression: Expression
//                                      thenStatement: Statement
//                                      elseStatement: Opt[Statement])
//                   ImportDeclaration => (static: Boolean, name: Name, onDemand: Boolean)
//                     InfixExpression => (leftOperand: Expression
//                                            operator: Operator
//                                        rightOperand: Expression
//                                    extendedOperands: List[Expression])
//                         Initializer => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                            body: Block)
//                InstanceofExpression => (leftOperand: Expression, rightOperand: Type)
//                             Javadoc => (tags: List[TagElement])
//                    LabeledStatement => (label: SimpleName, body: Statement)
//                    MarkerAnnotation => (typeName: Name)
//                           MemberRef => (qualifier: Opt[Name], name: SimpleName)
//                     MemberValuePair => (name: SimpleName, value: Expression)
//                   MethodDeclaration => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                     constructor: Boolean
//                                  typeParameters: List[TypeParameter]
//                                     returnType2: Opt[Type]
//                                            name: SimpleName
//                                      parameters: List[SingleVariableDeclaration]
//                                 extraDimensions: Int
//                                thrownExceptions: List[Name]
//                                            body: Opt[Block])
//                    MethodInvocation => (expression: Opt[Expression]
//                                      typeArguments: List[Type]
//                                               name: SimpleName
//                                          arguments: List[Expression])
//                           MethodRef => (qualifier: Opt[Name]
//                                              name: SimpleName
//                                        parameters: List[MethodRefParameter])
//                  MethodRefParameter => (type: Type, varargs: Boolean, name: Opt[SimpleName])
//                            Modifier => (keyword: ModifierKeyword)
//                    NormalAnnotation => (typeName: Name, values: List[MemberValuePair])
//                       NumberLiteral => (token: String)
//                  PackageDeclaration => (javadoc: Opt[Javadoc]
//                                     annotations: List[Annotation]
//                                            name: Name)
//                   ParameterizedType => (type: Type, typeArguments: List[Type])
//             ParenthesizedExpression => (expression: Expression)
//                   PostfixExpression => (operand: Expression, operator: Operator)
//                    PrefixExpression => (operator: Operator, operand: Expression)
//                       PrimitiveType => (primitiveTypeCode: Code)
//                       QualifiedName => (qualifier: Name, name: SimpleName)
//                       QualifiedType => (qualifier: Type, name: SimpleName)
//                     ReturnStatement => (expression: Opt[Expression])
//                          SimpleName => (identifier: String)
//                          SimpleType => (name: Name)
//              SingleMemberAnnotation => (typeName: Name, value: Expression)
//           SingleVariableDeclaration => (modifiers: List[IExtendedModifier]
//                                              type: Type
//                                           varargs: Boolean
//                                              name: SimpleName
//                                   extraDimensions: Int
//                                       initializer: Opt[Expression])
//                       StringLiteral => (escapedValue: String)
//          SuperConstructorInvocation => (expression: Opt[Expression]
//                                      typeArguments: List[Type]
//                                          arguments: List[Expression])
//                    SuperFieldAccess => (qualifier: Opt[Name], name: SimpleName)
//               SuperMethodInvocation => (qualifier: Opt[Name]
//                                     typeArguments: List[Type]
//                                              name: SimpleName
//                                         arguments: List[Expression])
//                          SwitchCase => (expression: Opt[Expression])
//                     SwitchStatement => (expression: Expression, statements: List[Statement])
//               SynchronizedStatement => (expression: Expression, body: Block)
//                          TagElement => (tagName: Opt[String], fragments: List[ASTNode])
//                         TextElement => (text: String)
//                      ThisExpression => (qualifier: Opt[Name])
//                      ThrowStatement => (expression: Expression)
//                        TryStatement => (body: Block, catchClauses: List[CatchClause], finally: Opt[Block])
//                     TypeDeclaration => (javadoc: Opt[Javadoc]
//                                       modifiers: List[IExtendedModifier]
//                                       interface: Boolean
//                                            name: SimpleName
//                                  typeParameters: List[TypeParameter]
//                                  superclassType: Opt[Type]
//                             superInterfaceTypes: List[Type]
//                                bodyDeclarations: List[BodyDeclaration])
//            TypeDeclarationStatement => (declaration: AbstractTypeDeclaration)
//                         TypeLiteral => (type: Type)
//                       TypeParameter => (name: SimpleName, typeBounds: List[Type])
//       VariableDeclarationExpression => (modifiers: List[IExtendedModifier]
//                                              type: Type
//                                         fragments: List[VariableDeclarationFragment])
//         VariableDeclarationFragment => (name: SimpleName
//                              extraDimensions: Int
//                                  initializer: Opt[Expression])
//        VariableDeclarationStatement => (modifiers: List[IExtendedModifier]
//                                              type: Type
//                                         fragments: List[VariableDeclarationFragment])
//                      WhileStatement => (expression: Expression, body: Statement)
//                        WildcardType => (bound: Opt[Type], upperBound: Boolean)

trait GenExtractors
{
    // Reduce null suffering
    def Opt[T](x: T): Option[T] = if (x == null) None else Some(x)

    object AnnotationTypeDeclaration {
        def unapply(node: dom.AnnotationTypeDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val name: dom.SimpleName = node.getName
            val bodyDeclarations: List[dom.BodyDeclaration] = node.bodyDeclarations
            
            Some(Opt(javadoc), modifiers, name, bodyDeclarations)
        }
    }
  
    object AnnotationTypeMemberDeclaration {
        def unapply(node: dom.AnnotationTypeMemberDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val name: dom.SimpleName = node.getName
            val `type`: dom.Type = node.getType
            val default: dom.Expression = node.getDefault
            
            Some(Opt(javadoc), modifiers, name, `type`, Opt(default))
        }
    }
  
    object AnonymousClassDeclaration {
        def unapply(node: dom.AnonymousClassDeclaration) = {
            val bodyDeclarations: List[dom.BodyDeclaration] = node.bodyDeclarations
            
            Some(bodyDeclarations)
        }
    }
  
    object ArrayAccess {
        def unapply(node: dom.ArrayAccess) = {
            val array: dom.Expression = node.getArray
            val index: dom.Expression = node.getIndex
            
            Some(array, index)
        }
    }
  
    object ArrayCreation {
        def unapply(node: dom.ArrayCreation) = {
            val `type`: dom.ArrayType = node.getType
            val dimensions: List[dom.Expression] = node.dimensions
            val initializer: dom.ArrayInitializer = node.getInitializer
            
            Some(`type`, dimensions, Opt(initializer))
        }
    }
  
    object ArrayInitializer {
        def unapply(node: dom.ArrayInitializer) = {
            val expressions: List[dom.Expression] = node.expressions
            
            Some(expressions)
        }
    }
  
    object ArrayType {
        def unapply(node: dom.ArrayType) = {
            val componentType: dom.Type = node.getComponentType
            
            Some(componentType)
        }
    }
  
    object AssertStatement {
        def unapply(node: dom.AssertStatement) = {
            val expression: dom.Expression = node.getExpression
            val message: dom.Expression = node.getMessage
            
            Some(expression, Opt(message))
        }
    }
  
    object Assignment {
        import org.eclipse.jdt.core.dom.Assignment.Operator
        def unapply(node: dom.Assignment) = {
            val leftHandSide: dom.Expression = node.getLeftHandSide
            val operator: dom.Assignment.Operator = node.getOperator
            val rightHandSide: dom.Expression = node.getRightHandSide
            
            Some(leftHandSide, operator, rightHandSide)
        }
    }
  
    object Block {
        def unapply(node: dom.Block) = {
            val statements: List[dom.Statement] = node.statements
            
            Some(statements)
        }
    }
  
    object BooleanLiteral {
        def unapply(node: dom.BooleanLiteral) = {
            val booleanValue: Boolean = node.booleanValue
            
            Some(booleanValue)
        }
    }
  
    object BreakStatement {
        def unapply(node: dom.BreakStatement) = {
            val label: dom.SimpleName = node.getLabel
            
            Some(Opt(label))
        }
    }
  
    object CastExpression {
        def unapply(node: dom.CastExpression) = {
            val `type`: dom.Type = node.getType
            val expression: dom.Expression = node.getExpression
            
            Some(`type`, expression)
        }
    }
  
    object CatchClause {
        def unapply(node: dom.CatchClause) = {
            val exception: dom.SingleVariableDeclaration = node.getException
            val body: dom.Block = node.getBody
            
            Some(exception, body)
        }
    }
  
    object CharacterLiteral {
        def unapply(node: dom.CharacterLiteral) = {
            val escapedValue: String = node.getEscapedValue
            
            Some(escapedValue)
        }
    }
  
    object ClassInstanceCreation {
        def unapply(node: dom.ClassInstanceCreation) = {
            val expression: dom.Expression = node.getExpression
            val typeArguments: List[dom.Type] = node.typeArguments
            val `type`: dom.Type = node.getType
            val arguments: List[dom.Expression] = node.arguments
            val anonymousClassDeclaration: dom.AnonymousClassDeclaration = node.getAnonymousClassDeclaration
            
            Some(Opt(expression), typeArguments, `type`, arguments, Opt(anonymousClassDeclaration))
        }
    }
  
    object CompilationUnit {
        def unapply(node: dom.CompilationUnit) = {
            val `package`: dom.PackageDeclaration = node.getPackage
            val imports: List[dom.ImportDeclaration] = node.imports
            val types: List[dom.AbstractTypeDeclaration] = node.types
            
            Some(Opt(`package`), imports, types)
        }
    }
  
    object ConditionalExpression {
        def unapply(node: dom.ConditionalExpression) = {
            val expression: dom.Expression = node.getExpression
            val thenExpression: dom.Expression = node.getThenExpression
            val elseExpression: dom.Expression = node.getElseExpression
            
            Some(expression, thenExpression, elseExpression)
        }
    }
  
    object ConstructorInvocation {
        def unapply(node: dom.ConstructorInvocation) = {
            val typeArguments: List[dom.Type] = node.typeArguments
            val arguments: List[dom.Expression] = node.arguments
            
            Some(typeArguments, arguments)
        }
    }
  
    object ContinueStatement {
        def unapply(node: dom.ContinueStatement) = {
            val label: dom.SimpleName = node.getLabel
            
            Some(Opt(label))
        }
    }
  
    object DoStatement {
        def unapply(node: dom.DoStatement) = {
            val expression: dom.Expression = node.getExpression
            val body: dom.Statement = node.getBody
            
            Some(expression, body)
        }
    }
  
    object EnhancedForStatement {
        def unapply(node: dom.EnhancedForStatement) = {
            val parameter: dom.SingleVariableDeclaration = node.getParameter
            val expression: dom.Expression = node.getExpression
            val body: dom.Statement = node.getBody
            
            Some(parameter, expression, body)
        }
    }
  
    object EnumConstantDeclaration {
        def unapply(node: dom.EnumConstantDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val name: dom.SimpleName = node.getName
            val arguments: List[dom.Expression] = node.arguments
            val anonymousClassDeclaration: dom.AnonymousClassDeclaration = node.getAnonymousClassDeclaration
            
            Some(Opt(javadoc), modifiers, name, arguments, Opt(anonymousClassDeclaration))
        }
    }
  
    object EnumDeclaration {
        def unapply(node: dom.EnumDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val name: dom.SimpleName = node.getName
            val superInterfaceTypes: List[dom.Type] = node.superInterfaceTypes
            val enumConstants: List[dom.EnumConstantDeclaration] = node.enumConstants
            val bodyDeclarations: List[dom.BodyDeclaration] = node.bodyDeclarations
            
            Some(Opt(javadoc), modifiers, name, superInterfaceTypes, enumConstants, bodyDeclarations)
        }
    }
  
    object ExpressionStatement {
        def unapply(node: dom.ExpressionStatement) = {
            val expression: dom.Expression = node.getExpression
            
            Some(expression)
        }
    }
  
    object FieldAccess {
        def unapply(node: dom.FieldAccess) = {
            val expression: dom.Expression = node.getExpression
            val name: dom.SimpleName = node.getName
            
            Some(expression, name)
        }
    }
  
    object FieldDeclaration {
        def unapply(node: dom.FieldDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val `type`: dom.Type = node.getType
            val fragments: List[dom.VariableDeclarationFragment] = node.fragments
            
            Some(Opt(javadoc), modifiers, `type`, fragments)
        }
    }
  
    object ForStatement {
        def unapply(node: dom.ForStatement) = {
            val initializers: List[dom.Expression] = node.initializers
            val expression: dom.Expression = node.getExpression
            val updaters: List[dom.Expression] = node.updaters
            val body: dom.Statement = node.getBody
            
            Some(initializers, Opt(expression), updaters, body)
        }
    }
  
    object IfStatement {
        def unapply(node: dom.IfStatement) = {
            val expression: dom.Expression = node.getExpression
            val thenStatement: dom.Statement = node.getThenStatement
            val elseStatement: dom.Statement = node.getElseStatement
            
            Some(expression, thenStatement, Opt(elseStatement))
        }
    }
  
    object ImportDeclaration {
        def unapply(node: dom.ImportDeclaration) = {
            val static: Boolean = node.isStatic
            val name: dom.Name = node.getName
            val onDemand: Boolean = node.isOnDemand
            
            Some(static, name, onDemand)
        }
    }
  
    object InfixExpression {
        import org.eclipse.jdt.core.dom.InfixExpression.Operator
        def unapply(node: dom.InfixExpression) = {
            val leftOperand: dom.Expression = node.getLeftOperand
            val operator: dom.InfixExpression.Operator = node.getOperator
            val rightOperand: dom.Expression = node.getRightOperand
            val extendedOperands: List[dom.Expression] = node.extendedOperands
            
            Some(leftOperand, operator, rightOperand, extendedOperands)
        }
    }
  
    object Initializer {
        def unapply(node: dom.Initializer) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val body: dom.Block = node.getBody
            
            Some(Opt(javadoc), modifiers, body)
        }
    }
  
    object InstanceofExpression {
        def unapply(node: dom.InstanceofExpression) = {
            val leftOperand: dom.Expression = node.getLeftOperand
            val rightOperand: dom.Type = node.getRightOperand
            
            Some(leftOperand, rightOperand)
        }
    }
  
    object Javadoc {
        def unapply(node: dom.Javadoc) = {
            val tags: List[dom.TagElement] = node.tags
            
            Some(tags)
        }
    }
  
    object LabeledStatement {
        def unapply(node: dom.LabeledStatement) = {
            val label: dom.SimpleName = node.getLabel
            val body: dom.Statement = node.getBody
            
            Some(label, body)
        }
    }
  
    object MarkerAnnotation {
        def unapply(node: dom.MarkerAnnotation) = {
            val typeName: dom.Name = node.getTypeName
            
            Some(typeName)
        }
    }
  
    object MemberRef {
        def unapply(node: dom.MemberRef) = {
            val qualifier: dom.Name = node.getQualifier
            val name: dom.SimpleName = node.getName
            
            Some(Opt(qualifier), name)
        }
    }
  
    object MemberValuePair {
        def unapply(node: dom.MemberValuePair) = {
            val name: dom.SimpleName = node.getName
            val value: dom.Expression = node.getValue
            
            Some(name, value)
        }
    }
  
    object MethodDeclaration {
        def unapply(node: dom.MethodDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val constructor: Boolean = node.isConstructor
            val typeParameters: List[dom.TypeParameter] = node.typeParameters
            val returnType2: dom.Type = node.getReturnType2
            val name: dom.SimpleName = node.getName
            val parameters: List[dom.SingleVariableDeclaration] = node.parameters
            val extraDimensions: Int = node.getExtraDimensions
            val thrownExceptions: List[dom.Name] = node.thrownExceptions
            val body: dom.Block = node.getBody
            
            Some(Opt(javadoc), modifiers, constructor, typeParameters, Opt(returnType2), name, parameters, extraDimensions, thrownExceptions, Opt(body))
        }
    }
  
    object MethodInvocation {
        def unapply(node: dom.MethodInvocation) = {
            val expression: dom.Expression = node.getExpression
            val typeArguments: List[dom.Type] = node.typeArguments
            val name: dom.SimpleName = node.getName
            val arguments: List[dom.Expression] = node.arguments
            
            Some(Opt(expression), typeArguments, name, arguments)
        }
    }
  
    object MethodRef {
        def unapply(node: dom.MethodRef) = {
            val qualifier: dom.Name = node.getQualifier
            val name: dom.SimpleName = node.getName
            val parameters: List[dom.MethodRefParameter] = node.parameters
            
            Some(Opt(qualifier), name, parameters)
        }
    }
  
    object MethodRefParameter {
        def unapply(node: dom.MethodRefParameter) = {
            val `type`: dom.Type = node.getType
            val varargs: Boolean = node.isVarargs
            val name: dom.SimpleName = node.getName
            
            Some(`type`, varargs, Opt(name))
        }
    }
  
    object Modifier {
        import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword
        def unapply(node: dom.Modifier) = {
            val keyword: dom.Modifier.ModifierKeyword = node.getKeyword
            
            Some(keyword)
        }
    }
  
    object NormalAnnotation {
        def unapply(node: dom.NormalAnnotation) = {
            val typeName: dom.Name = node.getTypeName
            val values: List[dom.MemberValuePair] = node.values
            
            Some(typeName, values)
        }
    }
  
    object NumberLiteral {
        def unapply(node: dom.NumberLiteral) = {
            val token: String = node.getToken
            
            Some(token)
        }
    }
  
    object PackageDeclaration {
        def unapply(node: dom.PackageDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val annotations: List[dom.Annotation] = node.annotations
            val name: dom.Name = node.getName
            
            Some(Opt(javadoc), annotations, name)
        }
    }
  
    object ParameterizedType {
        def unapply(node: dom.ParameterizedType) = {
            val `type`: dom.Type = node.getType
            val typeArguments: List[dom.Type] = node.typeArguments
            
            Some(`type`, typeArguments)
        }
    }
  
    object ParenthesizedExpression {
        def unapply(node: dom.ParenthesizedExpression) = {
            val expression: dom.Expression = node.getExpression
            
            Some(expression)
        }
    }
  
    object PostfixExpression {
        import org.eclipse.jdt.core.dom.PostfixExpression.Operator
        def unapply(node: dom.PostfixExpression) = {
            val operand: dom.Expression = node.getOperand
            val operator: dom.PostfixExpression.Operator = node.getOperator
            
            Some(operand, operator)
        }
    }
  
    object PrefixExpression {
        import org.eclipse.jdt.core.dom.PrefixExpression.Operator
        def unapply(node: dom.PrefixExpression) = {
            val operator: dom.PrefixExpression.Operator = node.getOperator
            val operand: dom.Expression = node.getOperand
            
            Some(operator, operand)
        }
    }
  
    object PrimitiveType {
        import org.eclipse.jdt.core.dom.PrimitiveType.Code
        def unapply(node: dom.PrimitiveType) = {
            val primitiveTypeCode: dom.PrimitiveType.Code = node.getPrimitiveTypeCode
            
            Some(primitiveTypeCode)
        }
    }
  
    object QualifiedName {
        def unapply(node: dom.QualifiedName) = {
            val qualifier: dom.Name = node.getQualifier
            val name: dom.SimpleName = node.getName
            
            Some(qualifier, name)
        }
    }
  
    object QualifiedType {
        def unapply(node: dom.QualifiedType) = {
            val qualifier: dom.Type = node.getQualifier
            val name: dom.SimpleName = node.getName
            
            Some(qualifier, name)
        }
    }
  
    object ReturnStatement {
        def unapply(node: dom.ReturnStatement) = {
            val expression: dom.Expression = node.getExpression
            
            Some(Opt(expression))
        }
    }
  
    object SimpleName {
        def unapply(node: dom.SimpleName) = {
            val identifier: String = node.getIdentifier
            
            Some(identifier)
        }
    }
  
    object SimpleType {
        def unapply(node: dom.SimpleType) = {
            val name: dom.Name = node.getName
            
            Some(name)
        }
    }
  
    object SingleMemberAnnotation {
        def unapply(node: dom.SingleMemberAnnotation) = {
            val typeName: dom.Name = node.getTypeName
            val value: dom.Expression = node.getValue
            
            Some(typeName, value)
        }
    }
  
    object SingleVariableDeclaration {
        def unapply(node: dom.SingleVariableDeclaration) = {
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val `type`: dom.Type = node.getType
            val varargs: Boolean = node.isVarargs
            val name: dom.SimpleName = node.getName
            val extraDimensions: Int = node.getExtraDimensions
            val initializer: dom.Expression = node.getInitializer
            
            Some(modifiers, `type`, varargs, name, extraDimensions, Opt(initializer))
        }
    }
  
    object StringLiteral {
        def unapply(node: dom.StringLiteral) = {
            val escapedValue: String = node.getEscapedValue
            
            Some(escapedValue)
        }
    }
  
    object SuperConstructorInvocation {
        def unapply(node: dom.SuperConstructorInvocation) = {
            val expression: dom.Expression = node.getExpression
            val typeArguments: List[dom.Type] = node.typeArguments
            val arguments: List[dom.Expression] = node.arguments
            
            Some(Opt(expression), typeArguments, arguments)
        }
    }
  
    object SuperFieldAccess {
        def unapply(node: dom.SuperFieldAccess) = {
            val qualifier: dom.Name = node.getQualifier
            val name: dom.SimpleName = node.getName
            
            Some(Opt(qualifier), name)
        }
    }
  
    object SuperMethodInvocation {
        def unapply(node: dom.SuperMethodInvocation) = {
            val qualifier: dom.Name = node.getQualifier
            val typeArguments: List[dom.Type] = node.typeArguments
            val name: dom.SimpleName = node.getName
            val arguments: List[dom.Expression] = node.arguments
            
            Some(Opt(qualifier), typeArguments, name, arguments)
        }
    }
  
    object SwitchCase {
        def unapply(node: dom.SwitchCase) = {
            val expression: dom.Expression = node.getExpression
            
            Some(Opt(expression))
        }
    }
  
    object SwitchStatement {
        def unapply(node: dom.SwitchStatement) = {
            val expression: dom.Expression = node.getExpression
            val statements: List[dom.Statement] = node.statements
            
            Some(expression, statements)
        }
    }
  
    object SynchronizedStatement {
        def unapply(node: dom.SynchronizedStatement) = {
            val expression: dom.Expression = node.getExpression
            val body: dom.Block = node.getBody
            
            Some(expression, body)
        }
    }
  
    object TagElement {
        def unapply(node: dom.TagElement) = {
            val tagName: String = node.getTagName
            val fragments: List[ASTNode] = node.fragments
            
            Some(Opt(tagName), fragments)
        }
    }
  
    object TextElement {
        def unapply(node: dom.TextElement) = {
            val text: String = node.getText
            
            Some(text)
        }
    }
  
    object ThisExpression {
        def unapply(node: dom.ThisExpression) = {
            val qualifier: dom.Name = node.getQualifier
            
            Some(Opt(qualifier))
        }
    }
  
    object ThrowStatement {
        def unapply(node: dom.ThrowStatement) = {
            val expression: dom.Expression = node.getExpression
            
            Some(expression)
        }
    }
  
    object TryStatement {
        def unapply(node: dom.TryStatement) = {
            val body: dom.Block = node.getBody
            val catchClauses: List[dom.CatchClause] = node.catchClauses
            val `finally`: dom.Block = node.getFinally
            
            Some(body, catchClauses, Opt(`finally`))
        }
    }
  
    object TypeDeclaration {
        def unapply(node: dom.TypeDeclaration) = {
            val javadoc: dom.Javadoc = node.getJavadoc
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val interface: Boolean = node.isInterface
            val name: dom.SimpleName = node.getName
            val typeParameters: List[dom.TypeParameter] = node.typeParameters
            val superclassType: dom.Type = node.getSuperclassType
            val superInterfaceTypes: List[dom.Type] = node.superInterfaceTypes
            val bodyDeclarations: List[dom.BodyDeclaration] = node.bodyDeclarations
            
            Some(Opt(javadoc), modifiers, interface, name, typeParameters, Opt(superclassType), superInterfaceTypes, bodyDeclarations)
        }
    }
  
    object TypeDeclarationStatement {
        def unapply(node: dom.TypeDeclarationStatement) = {
            val declaration: dom.AbstractTypeDeclaration = node.getDeclaration
            
            Some(declaration)
        }
    }
  
    object TypeLiteral {
        def unapply(node: dom.TypeLiteral) = {
            val `type`: dom.Type = node.getType
            
            Some(`type`)
        }
    }
  
    object TypeParameter {
        def unapply(node: dom.TypeParameter) = {
            val name: dom.SimpleName = node.getName
            val typeBounds: List[dom.Type] = node.typeBounds
            
            Some(name, typeBounds)
        }
    }
  
    object VariableDeclarationExpression {
        def unapply(node: dom.VariableDeclarationExpression) = {
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val `type`: dom.Type = node.getType
            val fragments: List[dom.VariableDeclarationFragment] = node.fragments
            
            Some(modifiers, `type`, fragments)
        }
    }
  
    object VariableDeclarationFragment {
        def unapply(node: dom.VariableDeclarationFragment) = {
            val name: dom.SimpleName = node.getName
            val extraDimensions: Int = node.getExtraDimensions
            val initializer: dom.Expression = node.getInitializer
            
            Some(name, extraDimensions, Opt(initializer))
        }
    }
  
    object VariableDeclarationStatement {
        def unapply(node: dom.VariableDeclarationStatement) = {
            val modifiers: List[dom.IExtendedModifier] = node.modifiers
            val `type`: dom.Type = node.getType
            val fragments: List[dom.VariableDeclarationFragment] = node.fragments
            
            Some(modifiers, `type`, fragments)
        }
    }
  
    object WhileStatement {
        def unapply(node: dom.WhileStatement) = {
            val expression: dom.Expression = node.getExpression
            val body: dom.Statement = node.getBody
            
            Some(expression, body)
        }
    }
  
    object WildcardType {
        def unapply(node: dom.WildcardType) = {
            val bound: dom.Type = node.getBound
            val upperBound: Boolean = node.isUpperBound
            
            Some(Opt(bound), upperBound)
        }
    }
  }
