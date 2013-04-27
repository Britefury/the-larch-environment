##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import types

from BritefuryJ.DocModel import DMSchema, DMObjectClass, DMNode




_stringFormatToPrefix = {
	'' : '',
	'ascii' : '',
	'unicode' : 'u',
	'bytes' : 'b',
	'ascii-regex' : 'r',
	'unicode-regex' : 'ur',
	'bytes-regex' : 'br',
	'regex' : 'r'
}


_stringPrefixToFormat = {
	'' : 'ascii',
        'u' : 'unicode',
	'b' : 'bytes',
	'r' : 'regex',
        'ur' : 'unicode-regex',
        'br' : 'bytes-regex'
}


def stringFormatToPrefix(format):
	try:
		return _stringFormatToPrefix[format]
	except KeyError:
		raise ValueError, 'Unknown string format {0}'.format( format )


def stringPrefixToFormat(prefix):
	try:
		return _stringPrefixToFormat[prefix]
	except KeyError:
		raise ValueError, 'Unknown string prefix {0}'.format( prefix )



def strToStrLiteral(x):
	r = repr( x )
	if r.startswith( 'ur' ):
		format = 'unicode-regex'
		q = 2
	elif r.startswith( 'br' ):
		format = 'bytes-regex'
		q = 2
	elif r.startswith( 'u' ):
		format = 'unicode'
		q = 1
	elif r.startswith( 'b' ):
		format = 'bytes'
		q = 1
	elif r.startswith( 'r' ):
		format = 'regex'
		q = 1
	else:
		format = 'ascii'
		q = 0
	qchar = r[q]
	if qchar == '\'':
		quotation = 'single'
	elif qchar == '\"':
		quotation = 'double'
	value = r[q+1:-1]
	return StringLiteral( format=format, quotation=quotation, value=value )





schema = DMSchema( 'Python2', 'py', 'LarchCore.Languages.Python2', 7 )


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
MultilineStringLiteral = schema.newClass( 'MultilineStringLiteral', Literal, [ 'format', 'value' ] )
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
DictComp = schema.newClass( 'DictComp', Expr, [ 'resultExpr', 'comprehensionItems' ] )
# Set
SetLiteral = schema.newClass( 'SetLiteral', Expr, [ 'values', 'trailingSeparator' ] )
SetComp = schema.newClass( 'SetComp', Expr, [ 'resultExpr', 'comprehensionItems' ] )
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
TupleParam = schema.newClass( 'TupleParam', Node, [ 'params', 'paramsTrailingSeparator' ] )
DefaultValueParam = schema.newClass( 'DefaultValueParam', Node, [ 'param', 'defaultValue' ] )
ParamList = schema.newClass( 'ParamList', Node, [ 'name' ] )
KWParamList = schema.newClass( 'KWParamList', Node, [ 'name' ] )
# Lambda
LambdaExpr = schema.newClass( 'LambdaExpr', Expr, [ 'params', 'expr', 'paramsTrailingSeparator' ] )
# Conditional
ConditionalExpr = schema.newClass( 'ConditionalExpr', Expr, [ 'condition', 'expr', 'elseExpr' ] )


# Quote and Unquote
Quote = schema.newClass( 'Quote', Expr, [ 'value' ] )
Unquote = schema.newClass( 'Unquote', Expr, [ 'value' ] )


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
WithContext = schema.newClass( 'WithContext', Node, [ 'expr', 'target' ] )
WithStmtHeader = schema.newClass( 'WithStmtHeader', CompountStmtHeader, [ 'contexts' ] )
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
WithStmt = schema.newClass( 'WithStmt', CompoundStmt, [ 'contexts', 'suite' ] )
Decorator = schema.newClass( 'Decorator', CompoundComponent, [ 'name', 'args', 'argsTrailingSeparator' ] )
DefStmt = schema.newClass( 'DefStmt', CompoundStmt, [ 'decorators', 'name', 'params', 'paramsTrailingSeparator', 'suite' ] )
ClassStmt = schema.newClass( 'ClassStmt', CompoundStmt, [ 'decorators', 'name', 'bases', 'basesTrailingSeparator', 'suite' ] )



#
# Structure nodes
#
Indent = schema.newClass( 'Indent', Node, [] )
Dedent = schema.newClass( 'Dedent', Node, [] )

IndentedBlock = schema.newClass( 'IndentedBlock', CompoundStmt, [ 'suite' ] )


# Embedded object
EmbeddedObjectLiteral = schema.newClass( 'EmbeddedObjectLiteral', Expr, [ 'embeddedValue' ] )
EmbeddedObjectExpr = schema.newClass( 'EmbeddedObjectExpr', Expr, [ 'embeddedValue' ] )
EmbeddedObjectStmt = schema.newClass( 'EmbeddedObjectStmt', SimpleStmt, [ 'embeddedValue' ] )



#
# Top level nodes
#
TopLevel = schema.newClass( 'TopLevel', Node, [] )
PythonModule = schema.newClass( 'PythonModule', TopLevel, [ 'suite' ] )
PythonSuite = schema.newClass( 'PythonSuite', TopLevel, [ 'suite' ] )
PythonExpression = schema.newClass( 'PythonExpression', TopLevel, [ 'expr' ] )
PythonTarget = schema.newClass( 'PythonTarget', TopLevel, [ 'target' ] )





#
#
# Embedded object utilities
#
#

def getEmbeddedObjectModelType(value):
	if hasattr( value, '__py_execmodel__' )  or  hasattr( value, '__py_exec__' )  or  ( hasattr( value, '__py_localnames__' ) and hasattr( value, '__py_localvalues__' ) ):
		# Statement methods
		return Stmt
	elif hasattr( value, '__py_evalmodel__' )  or  hasattr( value, '__py_eval__' ):
		# Expression methods
		return Expr
	else:
		# Fallback - use as value
		return Expr





#
#
# Python value coercion
#
#

def coerceToModel(x, embedClass=EmbeddedObjectExpr):
	# Literal values
	t = type(x)
	handler = _primitiveToMethodTypeMap.get(t)
	if handler is not None:
		return handler(x, embedClass)

	return embedClass(embeddedValue=DMNode.embed(x))




_primitiveToMethodTypeMap = {}


def _handleNone(x, embedClass):
	return Load(name='None')

_primitiveToMethodTypeMap[types.NoneType] = _handleNone


def _handleBool(x, embedClass):
	if x:
		return Load(name='True')
	else:
		return Load(name='False')

_primitiveToMethodTypeMap[bool] = _handleBool


def _handleInt(x, embedClass):
	return IntLiteral(format='decimal', numType='int', value=repr(x))

_primitiveToMethodTypeMap[int] = _handleInt


def _handleLong(x, embedClass):
	return IntLiteral(format='decimal', numType='long', value=repr(x)[:-1])

_primitiveToMethodTypeMap[long] = _handleLong


def _handleFloat(x, embedClass):
	return FloatLiteral(value=repr(x))

_primitiveToMethodTypeMap[float] = _handleFloat


def _handleComplex(x, embedClass):
	return Add(x=FloatLiteral(value=repr(x.real)), y=ImaginaryLiteral(value=repr(x.imag)+'j'))

_primitiveToMethodTypeMap[complex] = _handleComplex


def _handleStrOrUnicode(x, embedClass):
	return strToStrLiteral(x)

_primitiveToMethodTypeMap[str] = _handleStrOrUnicode
_primitiveToMethodTypeMap[unicode] = _handleStrOrUnicode



def _handleTuple(x, embedClass):
	return TupleLiteral(values=[coerceToModel(a, embedClass)   for a in x])

_primitiveToMethodTypeMap[tuple] = _handleTuple


def _handleList(x, embedClass):
	return ListLiteral(values=[coerceToModel(a, embedClass)   for a in x])

_primitiveToMethodTypeMap[list] = _handleList


def _handleSet(x, embedClass):
	return SetLiteral(values=[coerceToModel(a, embedClass)   for a in x])

_primitiveToMethodTypeMap[set] = _handleSet


def _handleDict(x, embedClass):
	return DictLiteral(values=[DictKeyValuePair(key=coerceToModel(k, embedClass), value=coerceToModel(v, embedClass))    for k, v in x.items()])

_primitiveToMethodTypeMap[dict] = _handleDict






#
#
# Version 1 backwards compatibility
#
#

def _readClassStmtHeader_v1(fieldValues):
	# Version 1 did not have a decorators field; initialise it to []
	return ClassStmtHeader( decorators=[], name=fieldValues['name'], bases=fieldValues['bases'], basesTrailingSeparator=fieldValues['basesTrailingSeparator'] )

def _readClassStmt_v1(fieldValues):
	# Version 1 did not have a decorators field; initialise it to []
	return ClassStmt( decorators=[], name=fieldValues['name'], bases=fieldValues['bases'], basesTrailingSeparator=fieldValues['basesTrailingSeparator'], suite=fieldValues['suite'] )

schema.registerReader( 'ClassStmtHeader', 1, _readClassStmtHeader_v1 )
schema.registerReader( 'ClassStmt', 1, _readClassStmt_v1 )




#
#
# Version 2 backwards compatibility
#
#

def _readInlineObjectExpr_v2(fieldValues):
	# Version 2 called the field 'resource', rather than 'embeddedValue'
	embeddedValue = fieldValues['resource']
	embeddedValue = DMNode.embed( None )
	return EmbeddedObjectExpr( embeddedValue=embeddedValue )

def _readInlineObjectStmt_v2(fieldValues):
	# Version 2 called the field 'resource', rather than 'embeddedValue'
	embeddedValue = fieldValues['resource']
	embeddedValue = DMNode.embed( None )
	return EmbeddedObjectStmt( embeddedValue=embeddedValue )

schema.registerReader( 'InlineObjectExpr', 2, _readInlineObjectExpr_v2 )
schema.registerReader( 'InlineObjectStmt', 2, _readInlineObjectStmt_v2 )




#
#
# Version 3 backwards compatibility
#
#

def _readInlineObjectExpr_v3(fieldValues):
	# Version 3 used the class name 'InlineObjectExpr'
	embeddedValue = fieldValues['embeddedValue']
	return EmbeddedObjectExpr( embeddedValue=embeddedValue )

def _readInlineObjectStmt_v3(fieldValues):
	# Version 3 used the class name 'InlineObjectStmt'
	embeddedValue = fieldValues['embeddedValue']
	return EmbeddedObjectStmt( embeddedValue=embeddedValue )

schema.registerReader( 'InlineObjectExpr', 3, _readInlineObjectExpr_v3 )
schema.registerReader( 'InlineObjectStmt', 3, _readInlineObjectStmt_v3 )




#
#
# Version 4 backwards compatibility
#
#

def _readDefaultValueParam_v4(fieldValues):
	# Version 4 stored a name and defaultValue
	name = fieldValues['name']
	defaultValue = fieldValues['defaultValue']
	return DefaultValueParam( param=SimpleParam( name=name ), defaultValue=defaultValue )

schema.registerReader( 'DefaultValueParam', 4, _readDefaultValueParam_v4 )




#
#
# Version 6 backwards compatibility
#
#

def _read_temp_SpecialFormStmtWrapper_v6(fieldValues):
	# Version 6; convert temporary special form statement wrapper to an embedded object literal
	value = fieldValues['value']
	if value.isInstanceOf( EmbeddedObjectStmt ):
		return EmbeddedObjectLiteral( embeddedValue=value['embeddedValue'] )
	else:
		print 'WARNING: Unable to load _temp_SpecialFormStmtWrapper node; converted to None'
		return Load( name='None' )

def _readEmbeddedObjectExpr_v6(fieldValues):
	# Version 6 embedded object expressions had an asLiteral field which determined if they acted as literals; convert
	embeddedValue = fieldValues['embeddedValue']
	asLiteral = fieldValues['asLiteral']
	if asLiteral is not None:
		return EmbeddedObjectLiteral( embeddedValue=embeddedValue )
	else:
		return EmbeddedObjectExpr( embeddedValue=embeddedValue)

schema.registerReader( '_temp_SpecialFormStmtWrapper', 6, _read_temp_SpecialFormStmtWrapper_v6 )
schema.registerReader( 'EmbeddedObjectExpr', 6, _readEmbeddedObjectExpr_v6 )






# Test coercion

import unittest



class TestCase_coercion (unittest.TestCase):
	def _testCoerce(self, valueToCoerce, expectedModel, embedClass=EmbeddedObjectExpr):
		result = coerceToModel(valueToCoerce, embedClass)

		if result != expectedModel:
			print 'UNEXPECTED RESULT'
			print 'INPUT:'
			print valueToCoerce
			print 'EXPECTED:'
			print expectedModel
			print 'RESULT:'
			print result

		self.assert_( result == expectedModel )


	def test_coercion_primitives(self):
		self._testCoerce(None, Load(name='None'))
		self._testCoerce(False, Load(name='False'))
		self._testCoerce(True, Load(name='True'))
		self._testCoerce(1, IntLiteral(format='decimal', numType='int', value='1'))
		self._testCoerce(1L, IntLiteral(format='decimal', numType='long', value='1'))
		self._testCoerce(1.0, FloatLiteral(value='1.0'))
		self._testCoerce(1.0+2.0j, Add(x=FloatLiteral(value='1.0'), y=ImaginaryLiteral(value='2.0j')))
		self._testCoerce('x', StringLiteral(format='ascii', quotation='single', value='x'))
		self._testCoerce(u'x', StringLiteral(format='unicode', quotation='single', value='x'))

		self._testCoerce((1.0, 2.0, (3.0, 4.0)), TupleLiteral(values=[FloatLiteral(value='1.0'), FloatLiteral(value='2.0'), TupleLiteral(values=[FloatLiteral(value='3.0'), FloatLiteral(value='4.0')])]))
		self._testCoerce([1.0, 2.0, (3.0, 4.0), [5.0, 6.0]], ListLiteral(values=[FloatLiteral(value='1.0'), FloatLiteral(value='2.0'), TupleLiteral(values=[FloatLiteral(value='3.0'), FloatLiteral(value='4.0')]), ListLiteral(values=[FloatLiteral(value='5.0'), FloatLiteral(value='6.0')])]))
		self._testCoerce({1.0, 2.0, (3.0, 4.0)}, SetLiteral(values=[FloatLiteral(value='2.0'), TupleLiteral(values=[FloatLiteral(value='3.0'), FloatLiteral(value='4.0')]), FloatLiteral(value='1.0')]))
		self._testCoerce({1.0:2.0, 3.0:(3.0, 4.0), (5.0, 6.0): 7.0}, DictLiteral(values=[
			DictKeyValuePair(key=TupleLiteral(values=[FloatLiteral(value='5.0'), FloatLiteral(value='6.0')]), value=FloatLiteral(value='7.0')),
			DictKeyValuePair(key=FloatLiteral(value='3.0'), value=TupleLiteral(values=[FloatLiteral(value='3.0'), FloatLiteral(value='4.0')])),
			DictKeyValuePair(key=FloatLiteral(value='1.0'), value=FloatLiteral(value='2.0')),
		]))

	def test_coercion_objects(self):
		class MyObject (object):
			def __init__(self):
				pass


		q = MyObject()

		self._testCoerce({1.0: q}, DictLiteral(values=[
			DictKeyValuePair(key=FloatLiteral(value='1.0'), value=EmbeddedObjectExpr(embeddedValue=DMNode.embed(q)))
		]))
