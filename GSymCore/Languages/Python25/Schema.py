##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass




schema = DMSchema( 'Python25', 'py', 'GSymCore.Languages.Python25', 2 )


#
# Node base classes
#
Node = schema.newClass( 'Node', [] )
Expr = schema.newClass( 'Expr', Node, [ 'parens' ] )
Stmt = schema.newClass( 'Stmt', Node, [] )


#
# Blank line, blank expr, comment, unparsed
#
BlankLine = schema.newClass( 'BlankLine', Stmt, [] )
CommentStmt = schema.newClass( 'CommentStmt', Stmt, [ 'comment' ] )
UNPARSED = schema.newClass( 'UNPARSED', Node, [ 'value' ] )


#
# Literals
#
Literal = schema.newClass( 'Literal', Expr, [] )
StringLiteral = schema.newClass( 'StringLiteral', Literal, [ 'format', 'quotation', 'value' ] )
IntLiteral = schema.newClass( 'IntLiteral', Literal, [ 'format', 'numType', 'value' ] )
FloatLiteral = schema.newClass( 'FloatLiteral', Literal, [ 'value' ] )
ImaginaryLiteral = schema.newClass( 'ImaginaryLiteral', Literal, [ 'value' ] )


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
YieldExpr = schema.newClass( 'YieldExpr', Expr, [ 'value' ] )
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


# Quote and Unquote
Quote = schema.newClass( 'Quote', Expr, [ 'value' ] )
Unquote = schema.newClass( 'Unquote', Expr, [ 'value' ] )


# Special externally provided expression
ExternalExpr = schema.newClass( 'ExternalExpr', Expr, [ 'expr' ] )


#
# Simple statements
#
# Unparsed statement
UnparsedStmt = schema.newClass( 'UnparsedStmt', Stmt, [ 'value' ] )
# Simple statement
SimpleStmt = schema.newClass( 'SimpleStmt', Stmt, [] )
# Expression statement
ExprStmt = schema.newClass( 'ExprStmt', SimpleStmt, [ 'expr' ] )
# Other statements
AssertStmt = schema.newClass( 'AssertStmt', SimpleStmt, [ 'condition', 'fail' ] )
AssignStmt = schema.newClass( 'AssignStmt', SimpleStmt, [ 'targets', 'value' ] )
AugAssignStmt = schema.newClass( 'AugAssignStmt', SimpleStmt, [ 'op', 'target', 'value' ] )
PassStmt = schema.newClass( 'PassStmt', SimpleStmt, [] )
DelStmt = schema.newClass( 'DelStmt', SimpleStmt, [ 'target' ] )
ReturnStmt = schema.newClass( 'ReturnStmt', SimpleStmt, [ 'value' ] )
YieldStmt = schema.newClass( 'YieldStmt', SimpleStmt, [ 'value' ] )
RaiseStmt = schema.newClass( 'RaiseStmt', SimpleStmt, [ 'excType', 'excValue', 'traceback' ] )
BreakStmt = schema.newClass( 'BreakStmt', SimpleStmt, [] )
ContinueStmt = schema.newClass( 'ContinueStmt', SimpleStmt, [] )
ExecStmt = schema.newClass( 'ExecStmt', SimpleStmt, [ 'source', 'globals', 'locals' ] )
PrintStmt = schema.newClass( 'PrintStmt', SimpleStmt, [ 'destination', 'values' ] )
# Import
RelativeModule = schema.newClass( 'RelativeModule', Node, [ 'name' ] )
ModuleImport = schema.newClass( 'ModuleImport', Node, [ 'name' ] )
ModuleImportAs = schema.newClass( 'ModuleImportAs', Node, [ 'name', 'asName' ] )
ModuleContentImport = schema.newClass( 'ModuleContentImport', Node, [ 'name' ] )
ModuleContentImportAs = schema.newClass( 'ModuleContentImportAs', Node, [ 'name', 'asName' ] )
ImportStmt = schema.newClass( 'ImportStmt', SimpleStmt, [ 'modules' ] )
FromImportStmt = schema.newClass( 'FromImportStmt', SimpleStmt, [ 'module', 'imports' ] )
FromImportAllStmt = schema.newClass( 'FromImportAllStmt', SimpleStmt, [ 'module' ] )
# Global
GlobalVar = schema.newClass( 'GlobalVar', Node, [ 'name' ] )
GlobalStmt = schema.newClass( 'GlobalStmt', SimpleStmt, [ 'vars' ] )



#
# Compound statement headers
#
CompountStmtHeader = schema.newClass( 'CompountStmtHeader', Stmt, [] )
IfStmtHeader = schema.newClass( 'IfStmtHeader', CompountStmtHeader, [ 'condition' ] )
ElifStmtHeader = schema.newClass( 'ElifStmtHeader', CompountStmtHeader, [ 'condition' ] )
ElseStmtHeader = schema.newClass( 'ElseStmtHeader', CompountStmtHeader, [] )
WhileStmtHeader = schema.newClass( 'WhileStmtHeader', CompountStmtHeader, [ 'condition' ] )
ForStmtHeader = schema.newClass( 'ForStmtHeader', CompountStmtHeader, [ 'target', 'source' ] )
TryStmtHeader = schema.newClass( 'TryStmtHeader', CompountStmtHeader, [] )
ExceptStmtHeader = schema.newClass( 'ExceptStmtHeader', CompountStmtHeader, [ 'exception', 'target' ] )
FinallyStmtHeader = schema.newClass( 'FinallyStmtHeader', CompountStmtHeader, [] )
WithStmtHeader = schema.newClass( 'WithStmtHeader', CompountStmtHeader, [ 'expr', 'target' ] )
DecoStmtHeader = schema.newClass( 'DecoStmtHeader', CompountStmtHeader, [ 'name', 'args', 'argsTrailingSeparator' ] )
DefStmtHeader = schema.newClass( 'DefStmtHeader', CompountStmtHeader, [ 'name', 'params', 'paramsTrailingSeparator' ] )
ClassStmtHeader = schema.newClass( 'ClassStmtHeader', CompountStmtHeader, [ 'name', 'bases', 'basesTrailingSeparator' ] )



#
# Compound statements
#
CompoundStmt = schema.newClass( 'CompoundStmt', Stmt, [] )
CompoundComponent = schema.newClass( 'CompoundComponent', Node, [] )

IfStmt = schema.newClass( 'IfStmt', CompoundStmt, [ 'condition', 'suite', 'elifBlocks', 'elseSuite' ] )
ElifBlock = schema.newClass( 'ElifBlock', CompoundComponent, [ 'condition', 'suite' ] )
WhileStmt = schema.newClass( 'WhileStmt', CompoundStmt, [ 'condition', 'suite', 'elseSuite' ] )
ForStmt = schema.newClass( 'ForStmt', CompoundStmt, [ 'target', 'source', 'suite', 'elseSuite' ] )
TryStmt = schema.newClass( 'TryStmt', CompoundStmt, [ 'suite', 'exceptBlocks', 'elseSuite', 'finallySuite' ] )
ExceptBlock = schema.newClass( 'ExceptBlock', CompoundComponent, [ 'exception', 'target', 'suite' ] )
WithStmt = schema.newClass( 'WithStmt', CompoundStmt, [ 'expr', 'target', 'suite' ] )
Decorator = schema.newClass( 'Decorator', CompoundComponent, [ 'name', 'args', 'argsTrailingSeparator' ] )
DefStmt = schema.newClass( 'DefStmt', CompoundStmt, [ 'decorators', 'name', 'params', 'paramsTrailingSeparator', 'suite' ] )
ClassStmt = schema.newClass( 'ClassStmt', CompoundStmt, [ 'decorators', 'name', 'bases', 'basesTrailingSeparator', 'suite' ] )



#
# Structure nodes
#
Indent = schema.newClass( 'Indent', Node, [] )
Dedent = schema.newClass( 'Dedent', Node, [] )

IndentedBlock = schema.newClass( 'IndentedBlock', CompoundStmt, [ 'suite' ] )


# Inline object
InlineObjectExpr = schema.newClass( 'InlineObjectExpr', Expr, [ 'resource' ] )
InlineObjectStmt = schema.newClass( 'InlineObjectStmt', SimpleStmt, [ 'resource' ] )



#
# Top level nodes
#
TopLevel = schema.newClass( 'TopLevel', Node, [] )
PythonModule = schema.newClass( 'PythonModule', TopLevel, [ 'suite' ] )
PythonSuite = schema.newClass( 'PythonSuite', TopLevel, [ 'suite' ] )
PythonExpression = schema.newClass( 'PythonExpression', TopLevel, [ 'expr' ] )





#
#
# Version 1 backwards compatibility
#
#

def _readClassStmtHeader_v1(fieldValues):
	# V1 did not have a decorators field; initialise it to []
	return ClassStmtHeader( decorators=[], name=fieldValues['name'], bases=fieldValues['bases'], basesTrailingSeparator=fieldValues['basesTrailingSeparator'] )

def _readClassStmt_v1(fieldValues):
	# V1 did not have a decorators field; initialise it to []
	return ClassStmt( decorators=[], name=fieldValues['name'], bases=fieldValues['bases'], basesTrailingSeparator=fieldValues['basesTrailingSeparator'], suite=fieldValues['suite'] )

schema.registerReader( 'ClassStmtHeader', 1, _readClassStmtHeader_v1 )
schema.registerReader( 'ClassStmt', 1, _readClassStmt_v1 )






def getInlineObjectModelType(value):
	try:
		modelType = value.__py_model_type__
	except AttributeError:
		modelType = Expr
	else:
		if isinstance( modelType, str )  or  isinstance( modelType, unicode ):
			if modelType == 'stmt'  or  modelType == 'statement':
				modelType = Stmt
			elif modelType == 'expr'  or  modelType == 'expression':
				modelType = Expr
			else:
				raise TypeError, '__py_model_type__ should be \'stmt\', \'statement\', \'expr\', or \'expression\''
	if not isinstance( modelType, DMObjectClass ):
		raise TypeError, '__py_model_type__ should be a string, or a Python node class'
	if modelType is not Expr  and  modelType is not Stmt:
		raise TypeError, '__py_model_type__ should be Expr or Stmt'
	return modelType
