##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMModule, DMObjectClass




module = DMModule( 'Python25', 'py', 'GSymCore.Languages.Python25.Python25' )


#
# Node base classes
#
Node = module.newClass( 'Node', [] )
Expr = module.newClass( 'Expr', Node, [ 'parens' ] )
Stmt = module.newClass( 'Stmt', Node, [] )


#
# Module
#
PythonModule = module.newClass( 'PythonModule', Node, [ 'suite' ] )


#
# Blank line, commentm unparsed
#
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
Target = module.newClass( 'Target', Node, [ 'parens' ] )
SingleTarget = module.newClass( 'SingleTarget', Target, [ 'name' ] )
TupleTarget = module.newClass( 'TupleTarget', Target, [ 'targets', 'trailingSeparator' ] )
ListTarget = module.newClass( 'ListTarget', Target, [ 'targets', 'trailingSeparator' ] )


#
# Expressions (various)
#
Load = module.newClass( 'Load', Expr, [ 'name' ] )
# Tuple / list
TupleLiteral = module.newClass( 'TupleLiteral', Expr, [ 'values', 'trailingSeparator' ] )
ListLiteral = module.newClass( 'ListLiteral', Expr, [ 'values', 'trailingSeparator' ] )
# List comprehension / generator expression
ComprehensionFor = module.newClass( 'ComprehensionFor', Node, [ 'target', 'source' ] )
ComprehensionIf = module.newClass( 'ComprehensionIf', Node, [ 'condition' ] )
ListComp = module.newClass( 'ListComp', Expr, [ 'resultExpr', 'comprehensionItems' ] )
GeneratorExpr = module.newClass( 'GeneratorExpr', Expr, [ 'resultExpr', 'comprehensionItems' ] )
# Dictionary
DictKeyValuePair = module.newClass( 'DictKeyValuePair', Node, [ 'key', 'value' ] )
DictLiteral = module.newClass( 'DictLiteral', Expr, [ 'values', 'trailingSeparator' ] )
# Yield
YieldExpr = module.newClass( 'YieldExpr', Expr, [ 'value' ] )
# Attribute reference
AttributeRef = module.newClass( 'AttributeRef', Expr, [ 'target', 'name' ] )
# Subscript
SubscriptSlice = module.newClass( 'SubscriptSlice', Node, [ 'lower', 'upper' ] )
SubscriptLongSlice = module.newClass( 'SubscriptLongSlice', Node, [ 'lower', 'upper', 'stride' ] )
SubscriptEllipsis = module.newClass( 'SubscriptEllipsis', Node, [] )
SubscriptTuple = module.newClass( 'SubscriptTuple', Node, [ 'values', 'trailingSeparator' ] )
Subscript = module.newClass( 'Subscript', Expr, [ 'target', 'index' ] )
# Call
CallKWArg = module.newClass( 'CallKWArg', Node, [ 'name', 'value' ] )
CallArgList = module.newClass( 'CallArgList', Node, [ 'value' ] )
CallKWArgList = module.newClass( 'CallKWArgList', Node, [ 'value' ] )
Call = module.newClass( 'Call', Expr, [ 'target', 'args', 'argsTrailingSeparator' ] )
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
LambdaExpr = module.newClass( 'LambdaExpr', Expr, [ 'params', 'expr', 'paramsTrailingSeparator' ] )
# Conditional
ConditionalExpr = module.newClass( 'ConditionalExpr', Expr, [ 'condition', 'expr', 'elseExpr' ] )


#
# Simple statements
#
SimpleStmt = module.newClass( 'SimpleStmt', Stmt, [] )
# Expression statement
ExprStmt = module.newClass( 'ExprStmt', SimpleStmt, [ 'expr' ] )
# Other statements
AssertStmt = module.newClass( 'AssertStmt', SimpleStmt, [ 'condition', 'fail' ] )
AssignStmt = module.newClass( 'AssignStmt', SimpleStmt, [ 'targets', 'value' ] )
AugAssignStmt = module.newClass( 'AugAssignStmt', SimpleStmt, [ 'op', 'target', 'value' ] )
PassStmt = module.newClass( 'PassStmt', SimpleStmt, [] )
DelStmt = module.newClass( 'DelStmt', SimpleStmt, [ 'target' ] )
ReturnStmt = module.newClass( 'ReturnStmt', SimpleStmt, [ 'value' ] )
YieldStmt = module.newClass( 'YieldStmt', SimpleStmt, [ 'value' ] )
RaiseStmt = module.newClass( 'RaiseStmt', SimpleStmt, [ 'excType', 'excValue', 'traceback' ] )
BreakStmt = module.newClass( 'BreakStmt', SimpleStmt, [] )
ContinueStmt = module.newClass( 'ContinueStmt', SimpleStmt, [] )
ExecStmt = module.newClass( 'ExecStmt', SimpleStmt, [ 'source', 'locals', 'globals' ] )
PrintStmt = module.newClass( 'PrintStmt', SimpleStmt, [ 'destination', 'values' ] )
# Import
RelativeModule = module.newClass( 'RelativeModule', Node, [ 'name' ] )
ModuleImport = module.newClass( 'ModuleImport', Node, [ 'name' ] )
ModuleImportAs = module.newClass( 'ModuleImportAs', Node, [ 'name', 'asName' ] )
ModuleContentImport = module.newClass( 'ModuleContentImport', Node, [ 'name' ] )
ModuleContentImportAs = module.newClass( 'ModuleContentImportAs', Node, [ 'name', 'asName' ] )
ImportStmt = module.newClass( 'ImportStmt', SimpleStmt, [ 'modules' ] )
FromImportStmt = module.newClass( 'FromImportStmt', SimpleStmt, [ 'module', 'imports' ] )
FromImportAllStmt = module.newClass( 'FromImportAllStmt', SimpleStmt, [ 'module' ] )
# Global
GlobalVar = module.newClass( 'GlobalVar', Node, [ 'name' ] )
GlobalStmt = module.newClass( 'GlobalStmt', SimpleStmt, [ 'vars' ] )



#
# Compound statement headers
#
CompountStmtHeader = module.newClass( 'CompountStmtHeader', Stmt, [] )
IfStmtHeader = module.newClass( 'IfStmtHeader', CompountStmtHeader, [ 'condition' ] )
ElifStmtHeader = module.newClass( 'ElifStmtHeader', CompountStmtHeader, [ 'condition' ] )
ElseStmtHeader = module.newClass( 'ElseStmtHeader', CompountStmtHeader, [] )
WhileStmtHeader = module.newClass( 'WhileStmtHeader', CompountStmtHeader, [ 'condition' ] )
ForStmtHeader = module.newClass( 'ForStmtHeader', CompountStmtHeader, [ 'target', 'source' ] )
TryStmtHeader = module.newClass( 'TryStmtHeader', CompountStmtHeader, [] )
ExceptStmtHeader = module.newClass( 'ExceptStmtHeader', CompountStmtHeader, [ 'exception', 'target' ] )
FinallyStmtHeader = module.newClass( 'FinallyStmtHeader', CompountStmtHeader, [] )
WithStmtHeader = module.newClass( 'WithStmtHeader', CompountStmtHeader, [ 'expr', 'target' ] )
DecoStmtHeader = module.newClass( 'DecoStmtHeader', CompountStmtHeader, [ 'name', 'args', 'argsTrailingSeparator' ] )
DefStmtHeader = module.newClass( 'DefStmtHeader', CompountStmtHeader, [ 'name', 'params', 'paramsTrailingSeparator' ] )
ClassStmtHeader = module.newClass( 'ClassStmtHeader', CompountStmtHeader, [ 'name', 'bases', 'basesTrailingSeparator' ] )



#
# Compound statements
#
CompoundStmt = module.newClass( 'CompoundStmt', Stmt, [] )
CompoundComponent = module.newClass( 'CompoundComponent', Node, [] )

IfStmt = module.newClass( 'IfStmt', CompoundStmt, [ 'condition', 'suite', 'elifBlocks', 'elseSuite' ] )
ElifBlock = module.newClass( 'ElifBlock', CompoundComponent, [ 'condition', 'suite' ] )
WhileStmt = module.newClass( 'WhileStmt', CompoundStmt, [ 'condition', 'suite', 'elseSuite' ] )
ForStmt = module.newClass( 'ForStmt', CompoundStmt, [ 'target', 'source', 'suite', 'elseSuite' ] )
TryStmt = module.newClass( 'TryStmt', CompoundStmt, [ 'suite', 'exceptBlocks', 'elseSuite', 'finallySuite' ] )
ExceptBlock = module.newClass( 'ExceptBlock', CompoundComponent, [ 'exception', 'target', 'suite' ] )
WithStmt = module.newClass( 'WithStmt', CompoundStmt, [ 'expr', 'target', 'suite' ] )
Decorator = module.newClass( 'Decorator', CompoundComponent, [ 'name', 'args', 'argsTrailingSeparator' ] )
DefStmt = module.newClass( 'DefStmt', CompoundStmt, [ 'decorators', 'name', 'params', 'paramsTrailingSeparator', 'suite' ] )
ClassStmt = module.newClass( 'ClassStmt', CompoundStmt, [ 'name', 'bases', 'basesTrailingSeparator', 'suite' ] )



#
# Structure nodes
#
Indent = module.newClass( 'Indent', Node, [] )
Dedent = module.newClass( 'Dedent', Node, [] )

IndentedBlock = module.newClass( 'IndentedBlock', CompoundStmt, [ 'suite' ] )


