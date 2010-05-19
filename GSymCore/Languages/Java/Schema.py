##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod, ObjectNodeMethodDispatchMetaClass




schema = DMSchema( 'Java', 'jv', 'GSymCore.Languages.Java' )


#
# Node base classes
#
Node = schema.newClass( 'Node', [] )
Expr = schema.newClass( 'Expr', Node, [ 'parens' ] )
Stmt = schema.newClass( 'Stmt', Node, [] )
CompoundStmt = schema.newClass( 'CompoundStmt', Stmt, [ 'suite' ] )


#
# Module, blank line, comment, unparsed
#
JavaModule = schema.newClass( 'JavaModule', Node, [ 'contents' ] )
BlankLine = schema.newClass( 'BlankLine', Node, [] )
CommentStmt = schema.newClass( 'CommentStmt', Stmt, [ 'comment' ] )
UNPARSED = schema.newClass( 'UNPARSED', Node, [ 'value' ] )


#
# Literals
#
Literal = schema.newClass( 'Literal', Expr, [] )
IntLiteral = schema.newClass( 'IntLiteral', Literal, [ 'format', 'numType', 'value' ] )
FloatLiteral = schema.newClass( 'FloatLiteral', Literal, [ 'value' ] )
CharLiteral = schema.newClass( 'CharLiteral', Literal, [ 'value' ] )
StringLiteral = schema.newClass( 'StringLiteral', Literal, [ 'value' ] )
BooleanLiteral = schema.newClass( 'BooleanLiteral', Literal, [ 'value' ] )
NullLiteral = schema.newClass( 'NullLiteral', Literal, [] )



#
# Types
#
TypeExpression = schema.newClass( 'TypeExpression', Node, [] )

GenericTypeExp = schema.newClass( 'GenericTypeExp', TypeExpression, [ 'target', 'args' ] )
WildCardTypeArgument = schema.newClass( 'WildCardTypeArgument', Node, [ 'extendsOrSuper', 'typeExp' ] )

MemberTypeExp = schema.newClass( 'MemberTypeExp', TypeExpression, [ 'target', 'member' ] )
ArrayTypeExp = schema.newClass( 'ArrayTypeExp', TypeExpression, [ 'itemTypeExp' ] )

TypeRef = schema.newClass( 'TypeRef', TypeExpression, [] )
ClassOrInterfaceTypeRef = schema.newClass( 'ClassOrInterfaceTypeRef', TypeRef, [ 'name' ] )
PrimitiveTypeRef = schema.newClass( 'PrimitiveTypeRef', TypeRef, [] )
BooleanTypeRef = schema.newClass( 'BooleanType', PrimitiveTypeRef, [] )
ByteTypeRef = schema.newClass( 'ByteTypeRef', PrimitiveTypeRef, [] )
ShortTypeRef = schema.newClass( 'ShortTypeRef', PrimitiveTypeRef, [] )
IntTypeRef = schema.newClass( 'IntTypeRef', PrimitiveTypeRef, [] )
LongTypeRef = schema.newClass( 'LongTypeRef', PrimitiveTypeRef, [] )
CharTypeRef = schema.newClass( 'CharTypeRef', PrimitiveTypeRef, [] )
FloatTypeRef = schema.newClass( 'FloatTypeRef', PrimitiveTypeRef, [] )
DoubleTypeRef = schema.newClass( 'DoubleTypeRef', PrimitiveTypeRef, [] )



#
# Primary expressions
#
TypeClassExp = schema.newClass( 'TypeClassExp', Expr, [ 'typeExp' ] )
VoidClassExp = schema.newClass( 'VoidClassExp', Expr, [] )
ThisExp = schema.newClass( 'ThisExp', Expr, [] )
SuperExp = schema.newClass( 'SuperExp', Expr, [] )
ClassInstanceCreation = schema.newClass( 'ClassInstanceCreation', Expr, [ 'classTypeRef', 'args' ] )
ArrayCreation = schema.newClass( 'ArrayCreation', Expr, [ 'itemTypeRef', 'fixedDimensions', 'numUnfixedDimensions' ] )
FieldAccess = schema.newClass( 'FieldAccess', Expr, [ 'target', 'fieldName' ] )
MethodInvocation = schema.newClass( 'MethodInvocation', Expr, [ 'target', 'methodName', 'args' ] )
ArrayAccess = schema.newClass( 'ArrayAccess', Expr, [ 'target', 'index' ] )



#
# Targets
#
Target = schema.newClass( 'Target', Node, [ 'parens' ] )
SingleTarget = schema.newClass( 'SingleTarget', Target, [ 'name' ] )
TupleTarget = schema.newClass( 'TupleTarget', Target, [ 'targets', 'trailingSeparator' ] )
ListTarget = schema.newClass( 'ListTarget', Target, [ 'targets', 'trailingSeparator' ] )


#
# Expressions (various)
#
Load = schema.newClass( 'Load', Expr, [ 'name' ] )
# Tuple / list
TupleLiteral = schema.newClass( 'TupleLiteral', Expr, [ 'values', 'trailingSeparator' ] )
ListLiteral = schema.newClass( 'ListLiteral', Expr, [ 'values', 'trailingSeparator' ] )
# List comprehension / generator expression
ComprehensionFor = schema.newClass( 'ComprehensionFor', Node, [ 'target', 'source' ] )
ComprehensionIf = schema.newClass( 'ComprehensionIf', Node, [ 'condition' ] )
ListComp = schema.newClass( 'ListComp', Expr, [ 'resultExpr', 'comprehensionItems' ] )
GeneratorExpr = schema.newClass( 'GeneratorExpr', Expr, [ 'resultExpr', 'comprehensionItems' ] )
# Dictionary
DictKeyValuePair = schema.newClass( 'DictKeyValuePair', Node, [ 'key', 'value' ] )
DictLiteral = schema.newClass( 'DictLiteral', Expr, [ 'values', 'trailingSeparator' ] )
# Yield
YieldAtom = schema.newClass( 'YieldAtom', Expr, [ 'value' ] )
# Attribute reference
AttributeRef = schema.newClass( 'AttributeRef', Expr, [ 'target', 'name' ] )
# Subscript
SubscriptSlice = schema.newClass( 'SubscriptSlice', Node, [ 'lower', 'upper' ] )
SubscriptLongSlice = schema.newClass( 'SubscriptLongSlice', Node, [ 'lower', 'upper', 'stride' ] )
SubscriptEllipsis = schema.newClass( 'SubscriptEllipsis', Node, [] )
SubscriptTuple = schema.newClass( 'SubscriptTuple', Node, [ 'values', 'trailingSeparator' ] )
Subscript = schema.newClass( 'Subscript', Expr, [ 'target', 'index' ] )
# Call
CallKWArg = schema.newClass( 'CallKWArg', Node, [ 'name', 'value' ] )
CallArgList = schema.newClass( 'CallArgList', Node, [ 'value' ] )
CallKWArgList = schema.newClass( 'CallKWArgList', Node, [ 'value' ] )
Call = schema.newClass( 'Call', Expr, [ 'target', 'args', 'argsTrailingSeparator' ] )
# Mathematical / bitwise operators
UnaryOp = schema.newClass( 'UnaryOp', Expr, [ 'x' ] )
BinOp = schema.newClass( 'BinOp', Expr, [ 'x', 'y' ] )
Pow = schema.newClass( 'Pow', BinOp, [] )
Invert = schema.newClass( 'Invert', UnaryOp, [] )
Negate = schema.newClass( 'Negate', UnaryOp, [] )
Pos = schema.newClass( 'Pos', UnaryOp, [] )
Mul = schema.newClass( 'Mul', BinOp, [] )
Div = schema.newClass( 'Div', BinOp, [] )
Mod = schema.newClass( 'Mod', BinOp, [] )
Add = schema.newClass( 'Add', BinOp, [] )
Sub = schema.newClass( 'Sub', BinOp, [] )
LShift = schema.newClass( 'LShift', BinOp, [] )
RShift = schema.newClass( 'RShift', BinOp, [] )
BitAnd = schema.newClass( 'BitAnd', BinOp, [] )
BitXor = schema.newClass( 'BitXor', BinOp, [] )
BitOr = schema.newClass( 'BitOr', BinOp, [] )
# Comparison
Cmp = schema.newClass( 'Cmp', Expr, [ 'x', 'ops' ] )
CmpOp = schema.newClass( 'CmpOp', Node, [ 'y' ] )
CmpOpLte = schema.newClass( 'CmpOpLte', CmpOp, [] )
CmpOpLt = schema.newClass( 'CmpOpLt', CmpOp, [] )
CmpOpGte = schema.newClass( 'CmpOpGte', CmpOp, [] )
CmpOpGt = schema.newClass( 'CmpOpGt', CmpOp, [] )
CmpOpEq = schema.newClass( 'CmpOpEq', CmpOp, [] )
CmpOpNeq = schema.newClass( 'CmpOpNeq', CmpOp, [] )
CmpOpIsNot = schema.newClass( 'CmpOpIsNot', CmpOp, [] )
CmpOpIs = schema.newClass( 'CmpOpIs', CmpOp, [] )
CmpOpNotIn = schema.newClass( 'CmpOpNotIn', CmpOp, [] )
CmpOpIn = schema.newClass( 'CmpOpIn', CmpOp, [] )
# Tests
NotTest = schema.newClass( 'NotTest', UnaryOp, [] )
AndTest = schema.newClass( 'AndTest', BinOp, [] )
OrTest = schema.newClass( 'OrTest', BinOp, [] )
# Parameters for lambda / function definition
SimpleParam = schema.newClass( 'SimpleParam', Node, [ 'name' ] )
DefaultValueParam = schema.newClass( 'DefaultValueParam', Node, [ 'name', 'defaultValue' ] )
ParamList = schema.newClass( 'ParamList', Node, [ 'name' ] )
KWParamList = schema.newClass( 'KWParamList', Node, [ 'name' ] )
# Lambda
LambdaExpr = schema.newClass( 'LambdaExpr', Expr, [ 'params', 'expr', 'paramsTrailingSeparator' ] )
# Conditional
ConditionalExpr = schema.newClass( 'ConditionalExpr', Expr, [ 'condition', 'expr', 'elseExpr' ] )


#
# Statements
#
AssertStmt = schema.newClass( 'AssertStmt', Stmt, [ 'condition', 'fail' ] )
AssignStmt = schema.newClass( 'AssignStmt', Stmt, [ 'targets', 'value' ] )
AugAssignStmt = schema.newClass( 'AugAssignStmt', Stmt, [ 'op', 'target', 'value' ] )
PassStmt = schema.newClass( 'PassStmt', Stmt, [] )
DelStmt = schema.newClass( 'DelStmt', Stmt, [ 'target' ] )
ReturnStmt = schema.newClass( 'ReturnStmt', Stmt, [ 'value' ] )
YieldStmt = schema.newClass( 'YieldStmt', Stmt, [ 'value' ] )
RaiseStmt = schema.newClass( 'RaiseStmt', Stmt, [ 'excType', 'excValue', 'traceback' ] )
BreakStmt = schema.newClass( 'BreakStmt', Stmt, [] )
ContinueStmt = schema.newClass( 'ContinueStmt', Stmt, [] )
ExecStmt = schema.newClass( 'ExecStmt', Stmt, [ 'source', 'locals', 'globals' ] )
# Import
RelativeModule = schema.newClass( 'RelativeModule', Node, [ 'name' ] )
ModuleImport = schema.newClass( 'ModuleImport', Node, [ 'name' ] )
ModuleImportAs = schema.newClass( 'ModuleImportAs', Node, [ 'name', 'asName' ] )
ModuleContentImport = schema.newClass( 'ModuleContentImport', Node, [ 'name' ] )
ModuleContentImportAs = schema.newClass( 'ModuleContentImportAs', Node, [ 'name', 'asName' ] )
ImportStmt = schema.newClass( 'ImportStmt', Stmt, [ 'modules' ] )
FromImportStmt = schema.newClass( 'FromImportStmt', Stmt, [ 'module', 'imports' ] )
FromImportAllStmt = schema.newClass( 'FromImportAllStmt', Stmt, [ 'module' ] )
# Global
GlobalVar = schema.newClass( 'GlobalVar', Node, [ 'name' ] )
GlobalStmt = schema.newClass( 'GlobalStmt', Stmt, [ 'vars' ] )


#
# Compound statements
#
IfStmt = schema.newClass( 'IfStmt', CompoundStmt, [ 'condition' ] )
ElifStmt = schema.newClass( 'ElifStmt', CompoundStmt, [ 'condition' ] )
ElseStmt = schema.newClass( 'ElseStmt', CompoundStmt, [] )
WhileStmt = schema.newClass( 'WhileStmt', CompoundStmt, [ 'condition' ] )
ForStmt = schema.newClass( 'ForStmt', CompoundStmt, [ 'target', 'source' ] )
TryStmt = schema.newClass( 'TryStmt', CompoundStmt, [] )
ExceptStmt = schema.newClass( 'ExceptStmt', CompoundStmt, [ 'exception', 'target' ] )
FinallyStmt = schema.newClass( 'FinallyStmt', CompoundStmt, [] )
WithStmt = schema.newClass( 'WithStmt', CompoundStmt, [ 'expr', 'target' ] )
DefStmt = schema.newClass( 'DefStmt', CompoundStmt, [ 'name', 'params', 'paramsTrailingSeparator' ] )
DecoStmt = schema.newClass( 'DecoStmt', CompoundStmt, [ 'name', 'args', 'argsTrailingSeparator' ] )
ClassStmt = schema.newClass( 'ClassStmt', CompoundStmt, [ 'name', 'bases', 'basesTrailingSeparator' ] )



