##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMModule, DMObjectClass

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod, ObjectNodeMethodDispatchMetaClass




module = DMModule( 'Python25', 'py', 'GSymCore.Languages.Python25.Python25' )


#
# Node base classes
#
Node = module.newClass( 'Node', [] )
Expr = module.newClass( 'Expr', Node, [ 'parens' ] )
Stmt = module.newClass( 'Stmt', Node, [] )
CompoundStmt = module.newClass( 'CompoundStmt', Stmt, [ 'suite' ] )


#
# Module, blank line, comment, unparsed
#
PythonModule = module.newClass( 'PythonModule', Node, [ 'contents' ] )
BlankLine = module.newClass( 'BlankLine', Node, [] )
CommentStmt = module.newClass( 'CommentStmt', Stmt, [ 'comment' ] )
UNPARSED = module.newClass( 'UNPARSED', Node, [ 'value' ] )


#
# Literals
#
Literal = module.newClass( 'Literal', Expr, [] )
StringLiteral = module.newClass( 'StringLiteral', Literal, [ 'format', 'quotation', 'value' ] )
IntLiteral = module.newClass( 'IntLiteral', Literal, [ 'format', 'numType', 'value' ] )
FloatLiteral = module.newClass( 'FloatLiteral', Literal, [ 'value' ] )
ImaginaryLiteral = module.newClass( 'ImaginaryLiteral', Literal, [ 'value' ] )


#
# Targets
#
Target = module.newClass( 'Target', Node, [] )
SingleTarget = module.newClass( 'SingleTarget', Target, [ 'name' ] )
TupleTarget = module.newClass( 'TupleTarget', Target, [ 'targets' ] )
ListTarget = module.newClass( 'ListTarget', Target, [ 'targets' ] )


#
# Expressions (various)
#
Load = module.newClass( 'Load', Expr, [ 'name' ] )
# Tuple / list
TupleLiteral = module.newClass( 'TupleLiteral', Expr, [ 'values' ] )
ListLiteral = module.newClass( 'ListLiteral', Expr, [ 'values' ] )
# List comprehension / generator expression
ComprehensionFor = module.newClass( 'ComprehensionFor', Node, [ 'target', 'source' ] )
ComprehensionIf = module.newClass( 'ComprehensionIf', Node, [ 'condition' ] )
ListComp = module.newClass( 'ListComp', Expr, [ 'resultExpr', 'comprehensionItems' ] )
GeneratorExpr = module.newClass( 'GeneratorExpr', Expr, [ 'resultExpr', 'comprehensionItems' ] )
# Dictionary
DictKeyValuePair = module.newClass( 'DictKeyValuePair', Node, [ 'key', 'value' ] )
DictLiteral = module.newClass( 'DictLiteral', Expr, [ 'values' ] )
# Yield
YieldAtom = module.newClass( 'YieldAtom', Expr, [ 'value' ] )
# Attribute reference
AttributeRef = module.newClass( 'AttributeRef', Expr, [ 'target', 'name' ] )
# Subscript
SubscriptSlice = module.newClass( 'SubscriptSlice', Node, [ 'lower', 'upper' ] )
SubscriptLongSlice = module.newClass( 'SubscriptLongSlice', Node, [ 'lower', 'upper', 'stride' ] )
SubscriptEllipsis = module.newClass( 'SubscriptEllipsis', Node, [] )
SubscriptTuple = module.newClass( 'SubscriptTuple', Node, [ 'values' ] )
Subscript = module.newClass( 'Subscript', Expr, [ 'target', 'index' ] )
# Call
CallKWArg = module.newClass( 'CallKWArg', Node, [ 'name', 'value' ] )
CallArgList = module.newClass( 'CallArgList', Node, [ 'value' ] )
CallKWArgList = module.newClass( 'CallKWArgList', Node, [ 'value' ] )
Call = module.newClass( 'Call', Expr, [ 'target', 'args' ] )
# Mathematical / bitwise operators
UnaryOp = module.newClass( 'UnaryOp', Expr, [ 'x' ] )
BinOp = module.newClass( 'BinOp', Expr, [ 'x', 'y' ] )
Pow = module.newClass( 'Pow', BinOp, [] )
Invert = module.newClass( 'Invert', UnaryOp, [] )
Negate = module.newClass( 'Negate', UnaryOp, [] )
Pos = module.newClass( 'Pos', UnaryOp, [] )
Mul = module.newClass( 'Mul', BinOp, [] )
Div = module.newClass( 'Div', BinOp, [] )
Mod = module.newClass( 'Mod', BinOp, [] )
Add = module.newClass( 'Add', BinOp, [] )
Sub = module.newClass( 'Sub', BinOp, [] )
LShift = module.newClass( 'LShift', BinOp, [] )
RShift = module.newClass( 'RShift', BinOp, [] )
BitAnd = module.newClass( 'BitAnd', BinOp, [] )
BitXor = module.newClass( 'BitXor', BinOp, [] )
BitOr = module.newClass( 'BitOr', BinOp, [] )
# Comparison
Cmp = module.newClass( 'Cmp', Expr, [ 'x', 'ops' ] )
CmpOp = module.newClass( 'CmpOp', Node, [ 'y' ] )
CmpOpLte = module.newClass( 'CmpOpLte', CmpOp, [] )
CmpOpLt = module.newClass( 'CmpOpLt', CmpOp, [] )
CmpOpGte = module.newClass( 'CmpOpGte', CmpOp, [] )
CmpOpGt = module.newClass( 'CmpOpGt', CmpOp, [] )
CmpOpEq = module.newClass( 'CmpOpEq', CmpOp, [] )
CmpOpNeq = module.newClass( 'CmpOpNeq', CmpOp, [] )
CmpOpIsNot = module.newClass( 'CmpOpIsNot', CmpOp, [] )
CmpOpIs = module.newClass( 'CmpOpIs', CmpOp, [] )
CmpOpNotIn = module.newClass( 'CmpOpNotIn', CmpOp, [] )
CmpOpIn = module.newClass( 'CmpOpIn', CmpOp, [] )
# Tests
NotTest = module.newClass( 'NotTest', UnaryOp, [] )
AndTest = module.newClass( 'AndTest', BinOp, [] )
OrTest = module.newClass( 'OrTest', BinOp, [] )
# Parameters for lambda / function definition
SimpleParam = module.newClass( 'SimpleParam', Node, [ 'name' ] )
DefaultValueParam = module.newClass( 'DefaultValueParam', Node, [ 'name', 'defaultValue' ] )
ParamList = module.newClass( 'ParamList', Node, [ 'name' ] )
KWParamList = module.newClass( 'KWParamList', Node, [ 'name' ] )
# Lambda
LambdaExpr = module.newClass( 'LambdaExpr', Expr, [ 'params', 'expr' ] )
# Conditional
ConditionalExpr = module.newClass( 'ConditionalExpr', Expr, [ 'condition', 'expr', 'elseExpr' ] )


#
# Statements
#
AssertStmt = module.newClass( 'AssertStmt', Stmt, [ 'condition', 'fail' ] )
AssignStmt = module.newClass( 'AssignStmt', Stmt, [ 'targets', 'value' ] )
AugAssignStmt = module.newClass( 'AugAssignStmt', Stmt, [ 'op', 'target', 'value' ] )
PassStmt = module.newClass( 'PassStmt', Stmt, [] )
DelStmt = module.newClass( 'DelStmt', Stmt, [ 'target' ] )
ReturnStmt = module.newClass( 'ReturnStmt', Stmt, [ 'value' ] )
YieldStmt = module.newClass( 'YieldStmt', Stmt, [ 'value' ] )
RaiseStmt = module.newClass( 'RaiseStmt', Stmt, [ 'excType', 'excValue', 'traceback' ] )
BreakStmt = module.newClass( 'BreakStmt', Stmt, [] )
ContinueStmt = module.newClass( 'ContinueStmt', Stmt, [] )
ExecStmt = module.newClass( 'ExecStmt', Stmt, [ 'source', 'locals', 'globals' ] )
# Import
RelativeModule = module.newClass( 'RelativeModule', Node, [ 'name' ] )
ModuleImport = module.newClass( 'ModuleImport', Node, [ 'name' ] )
ModuleImportAs = module.newClass( 'ModuleImportAs', Node, [ 'name', 'asName' ] )
ModuleContentImport = module.newClass( 'ModuleContentImport', Node, [ 'name' ] )
ModuleContentImportAs = module.newClass( 'ModuleContentImportAs', Node, [ 'name', 'asName' ] )
ImportStmt = module.newClass( 'ImportStmt', Stmt, [ 'modules' ] )
FromImportStmt = module.newClass( 'FromImportStmt', Stmt, [ 'module', 'imports' ] )
FromImportAllStmt = module.newClass( 'FromImportAllStmt', Stmt, [ 'module' ] )
# Global
GlobalVar = module.newClass( 'GlobalVar', Node, [ 'name' ] )
GlobalStmt = module.newClass( 'GlobalStmt', Stmt, [ 'vars' ] )


#
# Compound statements
#
IfStmt = module.newClass( 'IfStmt', CompoundStmt, [ 'condition' ] )
ElifStmt = module.newClass( 'ElifStmt', CompoundStmt, [ 'condition' ] )
ElseStmt = module.newClass( 'ElseStmt', CompoundStmt, [] )
WhileStmt = module.newClass( 'WhileStmt', CompoundStmt, [ 'condition' ] )
ForStmt = module.newClass( 'ForStmt', CompoundStmt, [ 'target', 'source' ] )
TryStmt = module.newClass( 'TryStmt', CompoundStmt, [] )
ExceptStmt = module.newClass( 'ExceptStmt', CompoundStmt, [ 'exception', 'target' ] )
FinallyStmt = module.newClass( 'FinallyStmt', CompoundStmt, [] )
WithStmt = module.newClass( 'WithStmt', CompoundStmt, [ 'expr', 'target' ] )
DefStmt = module.newClass( 'DefStmt', CompoundStmt, [ 'name', 'params' ] )
DecoStmt = module.newClass( 'DecoStmt', CompoundStmt, [ 'name', 'args' ] )
ClassStmt = module.newClass( 'ClassStmt', CompoundStmt, [ 'name', 'bases' ] )









PRECEDENCE_NONE = None

PRECEDENCE_STMT = 200

PRECEDENCE_COMPREHENSIONELEMENT = 175
PRECEDENCE_CONDITIONALELEMENT = 175

PRECEDENCE_CONTAINEREXPR = 150
PRECEDENCE_TUPLEELEMENT = 150
PRECEDENCE_TUPLE = 150

PRECEDENCE_SEQUENCEELEMENT = 100
PRECEDENCE_YIELDVALUE = 100
PRECEDENCE_SUBSCRIPTINDEX = 100
PRECEDENCE_ELLIPSIS = 100
PRECEDENCE_ARG = 100
PRECEDENCE_PARAM = 100

PRECEDENCE_LAMBDAEXPR = 50

PRECEDENCE_OR = 14
PRECEDENCE_AND = 13
PRECEDENCE_NOT = 12
PRECEDENCE_CMP = 9
PRECEDENCE_BITOR = 8
PRECEDENCE_BITXOR = 7
PRECEDENCE_BITAND = 6
PRECEDENCE_SHIFT = 5
PRECEDENCE_ADDSUB = 4
PRECEDENCE_MULDIVMOD = 3
PRECEDENCE_INVERT_NEGATE_POS = 2
PRECEDENCE_POW = 1
PRECEDENCE_CALL = 0
PRECEDENCE_SUBSCRIPT = 0
PRECEDENCE_ATTR = 0

PRECEDENCE_EXPR = 0
PRECEDENCE_LOADLOCAL = 0
PRECEDENCE_LISTLITERAL = 0
PRECEDENCE_LITERALVALUE = 0
PRECEDENCE_LISTCOMPREHENSION = 0
PRECEDENCE_GENERATOREXPRESSION = 0
PRECEDENCE_CONDITIONALEXPRESSION = 0
PRECEDENCE_DICTLITERAL = 0
PRECEDENCE_YIELDEXPR = 0
PRECEDENCE_IMPORTCONTENT = 0


PRECEDENCE_TARGET = 0




class NodePrecedence (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_module__ = module
	__dispatch_num_args__ = 0
	
	
	@ObjectNodeDispatchMethod
	def Stmt(self, node):
		return PRECEDENCE_STMT
	
	@ObjectNodeDispatchMethod
	def Expr(self, node):
		return PRECEDENCE_EXPR
	
	@ObjectNodeDispatchMethod
	def Pow(self, node):
		return PRECEDENCE_POW
	
	@ObjectNodeDispatchMethod
	def Invert(self, node):
		return PRECEDENCE_INVERT_NEGATE_POS
	
	@ObjectNodeDispatchMethod
	def Negate(self, node):
		return PRECEDENCE_INVERT_NEGATE_POS

	@ObjectNodeDispatchMethod
	def Pos(self, node):
		return PRECEDENCE_INVERT_NEGATE_POS
	
	@ObjectNodeDispatchMethod
	def Mul(self, node):
		return PRECEDENCE_MULDIVMOD
	
	@ObjectNodeDispatchMethod
	def Div(self, node):
		return PRECEDENCE_MULDIVMOD
	
	@ObjectNodeDispatchMethod
	def Mod(self, node):
		return PRECEDENCE_MULDIVMOD
	
	@ObjectNodeDispatchMethod
	def Add(self, node):
		return PRECEDENCE_ADDSUB
	
	@ObjectNodeDispatchMethod
	def Sub(self, node):
		return PRECEDENCE_ADDSUB
	
	@ObjectNodeDispatchMethod
	def LShift(self, node):
		return PRECEDENCE_SHIFT
	
	@ObjectNodeDispatchMethod
	def RShift(self, node):
		return PRECEDENCE_SHIFT
	
	@ObjectNodeDispatchMethod
	def BitAnd(self, node):
		return PRECEDENCE_BITAND

	@ObjectNodeDispatchMethod
	def BitXor(self, node):
		return PRECEDENCE_BITXOR

	@ObjectNodeDispatchMethod
	def BitOr(self, node):
		return PRECEDENCE_BITOR
	
	@ObjectNodeDispatchMethod
	def Cmp(self, node):
		return PRECEDENCE_CMP
	
	@ObjectNodeDispatchMethod
	def NotTest(self, node):
		return PRECEDENCE_NOT
	
	@ObjectNodeDispatchMethod
	def AndTest(self, node):
		return PRECEDENCE_AND
	
	@ObjectNodeDispatchMethod
	def OrTest(self, node):
		return PRECEDENCE_OR

	@ObjectNodeDispatchMethod
	def LambdaExpr(self, node):
		return PRECEDENCE_LAMBDAEXPR

	
	
	
	
	
	
class NodeContainmentPrecedence (NodePrecedence):
	@ObjectNodeDispatchMethod
	def TupleLiteral(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def ListLiteral(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def ComprehensionFor(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def ComprehensionIf(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def ListComp(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def GeneratorExpr(self, node):
		return PRECEDENCE_CONTAINEREXPR

	@ObjectNodeDispatchMethod
	def DictKeyValuePair(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def DictLiteral(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def YieldAtom(self, node):
		return PRECEDENCE_CONTAINEREXPR
	
	@ObjectNodeDispatchMethod
	def Call(self, node):
		return PRECEDENCE_CONTAINEREXPR
	