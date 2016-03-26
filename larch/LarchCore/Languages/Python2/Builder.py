##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from Britefury.Util.Abstract import abstractmethod

from BritefuryJ.DocModel import DMNode

import LarchCore.Languages.Python2.Schema as Py
from LarchCore.Languages.Python2 import Embedded



def embeddedLiteral(x):
	return Py.EmbeddedObjectLiteral( embeddedValue=DMNode.embedIsolated( x ) )

def embeddedExpression(x):
	return Py.EmbeddedObjectExpr( embeddedValue=DMNode.embedIsolated( x ) )

def embeddedStatement(x):
	return Py.EmbeddedObjectStmt( embeddedValue=DMNode.embedIsolated( x ) )


class _Builder (object):
	@abstractmethod
	def build(self):
		pass


class Target (_Builder):
	pass

class Expr (_Builder):
	pass

class CompItem (_Builder):
	pass

class Stmt (_Builder):
	pass




class TargetBuilt (Target):
	def __init__(self, node):
		self.node = node

	def build(self):
		return self.node


class SingleTarget (Target):
	def __init__(self, name):
		self.name = name

	def build(self):
		return Py.SingleTarget( name=self.name )


class TupleTarget (Target):
	def __init__(self, targets):
		self.targets = [ target( t )   for t in targets ]

	def build(self):
		return Py.TupleTarget( targets=[ t.build()   for t in self.targets ] )


class ListTarget (Target):
	def __init__(self, targets):
		self.targets = [ target( t )   for t in targets ]

	def build(self):
		return Py.ListTarget( targets=[ t.build()   for t in self.targets ] )






class ExprBuilt (Expr):
	def __init__(self, node):
		self.node = node

	def build(self):
		return self.node


class Literal (Expr):
	pass


class StrLit (Literal):
	def __init__(self, value, format=None, quotation='single'):
		if format is None:
			format = 'unicode'   if isinstance( value, unicode )   else 'ascii'
		self.valueString = repr( value )[1:-1]
		self.format = format
		self.quotation = quotation
	
	def build(self):
		return Py.StringLiteral( value=self.valueString, format=self.format, quotation=self.quotation )

	
	
class IntLit (Literal):
	def __init__(self, value, numType=None, format='decimal'):
		if numType is None:
			numType = 'long'   if isinstance( value, long )   else 'int'
		if format == 'hex':
			self.valueString = '%x' % value
		elif format == 'decimal':
			self.valueString = str(value)
		else:
			self.valueString = str(value)
		self.numType = numType
		self.format = format
		
	def build(self):
		return Py.IntLiteral( value=self.valueString, format=self.format, numType=self.numType )


class FloatLit (Literal):
	def __init__(self, value):
		self.value = value
	
	def build(self):
		return Py.FloatLiteral( value=repr( self.value ) )

class ImagLit (Literal):
	def __init__(self, value):
		self.value = value
	
	def build(self):
		return Py.ImaginaryLiteral( value=repr( self.value ) )

class ComplexLit (Literal):
	def __init__(self, real, imag):
		self.real = real
		self.imag = imag
	
	def build(self):
		return Py.Add( x=Py.FloatLiteral( value=repr( self.real ) ), y=Py.ImaginaryLiteral( value=repr( self.imag ) ) )
	
	


class Load (Expr):
	def __init__(self, name):
		self.name = name
	
	def build(self):
		return Py.Load( name=self.name )
	
false_ = Load( 'False' )
true_ = Load( 'True' )
none_ = Load( 'None' )





class TupleLit (Expr):
	def __init__(self, xs):
		self.xs = [ expr(x)   for x in xs ]

	def build(self):
		return Py.TupleLiteral( values=[ x.build()   for x in self.xs ] )


class ListLit (Expr):
	def __init__(self, xs):
		self.xs = [ expr(x)   for x in xs ]

	def build(self):
		return Py.ListLiteral( values=[ x.build()   for x in self.xs ] )



class CompFor (_Builder):
	def __init__(self, tgt, source):
		self.tgt = target( tgt )
		self.src = expr( source )

	def build(self):
		return Py.ComprehensionFor( target=self.tgt.build(), source=self.src.build() )


class CompIf (_Builder):
	def __init__(self, condition):
		self.condition = expr( condition )

	def build(self):
		return Py.ComprehensionIf( condition=self.condition.build() )


class ListComp (Expr):
	def __init__(self, itemExpr, compItems):
		self.itemExpr = expr( itemExpr )
		self.compItems = [ compItem( x )   for x in compItems ]

	def build(self):
		return Py.ListComp( resultExpr=self.itemExpr.build(), comprehensionItems=[ x.build()   for x in self.compItems ] )


class GenExp (Expr):
	def __init__(self, itemExpr, compItems):
		self.itemExpr = expr( itemExpr )
		self.compItems = [ compItem( x )   for x in compItems ]

	def build(self):
		return Py.GeneratorExpr( resultExpr=self.itemExpr.build(), comprehensionItems=[ x.build()   for x in self.compItems ] )









def target(x):
	if isinstance( x, Target ):
		return x
	elif isinstance( x, Embedded.EmbeddedPython2Target ):
		return TargetBuilt( x.target )
	elif isinstance( x, DMNode )  and  x.isInstanceOf( Py.Target ):
		return TargetBuilt( x )
	elif isinstance( x, tuple ):
		return TupleTarget( x )
	elif isinstance( x, list ):
		return ListTarget( x )
	else:
		raise TypeError, 'cannot coerce %s to a target'  %  type( x )



def expr(x):
	if x is None:
		return none_
	elif isinstance( x, Expr ):
		return x
	elif isinstance( x, Embedded.EmbeddedPython2Expr ):
		return ExprBuilt( x.expression )
	elif isinstance( x, DMNode )  and  x.isInstanceOf( Py.Expr ):
		return ExprBuilt( x )
	elif isinstance( x, str ):
		return StrLit( x )
	elif isinstance( x, unicode ):
		return StrLit( x, format='unicode' )
	elif isinstance( x, bool ):
		if x:
			return true_
		else:
			return false_
	elif isinstance( x, int ):
		return IntLit( x )
	elif isinstance( x, long ):
		return IntLit( x, numType='long' )
	elif isinstance( x, float ):
		return FloatLit( x )
	elif isinstance( x, complex ):
		return ComplexLit( x.real, x.imag )
	else:
		raise TypeError, 'cannot coerce %s to an expression'  %  type( x )


def compItem(x):
	if isinstance( x, CompItem ):
		return x
	else:
		raise TypeError, 'cannot coerce %s to a comprehension item'  %  type( x )








import unittest

class TestCase_Builder (unittest.TestCase):
	def _buildTest(self, expected, builder):
		result = builder.build()
		self.assertEqual( expected, result )


	def test_TargetBuilt(self):
		self._buildTest( Py.SingleTarget( name='t' ), TargetBuilt( Py.SingleTarget( name='t' ) ) )

	def test_SingleTarget(self):
		self._buildTest( Py.SingleTarget( name='t' ), SingleTarget( 't' ) )

	def test_TupleTarget(self):
		self._buildTest( Py.TupleTarget( targets=[ Py.SingleTarget( name='t' ), Py.SingleTarget( name='v' ) ] ), TupleTarget( [ SingleTarget( 't' ), SingleTarget( 'v' ) ] ) )

	def test_ListTarget(self):
		self._buildTest( Py.ListTarget( targets=[ Py.SingleTarget( name='t' ), Py.SingleTarget( name='v' ) ] ), ListTarget( [ SingleTarget( 't' ), SingleTarget( 'v' ) ] ) )

	def test_Target_coerce(self):
		t = SingleTarget( 't' )
		self.assert_( t is target( t ) )
		self._buildTest( Py.SingleTarget( name='t' ), target( Embedded.EmbeddedPython2Target( Py.PythonTarget( target=Py.SingleTarget( name='t' ) ) ) ) )
		self._buildTest( Py.SingleTarget( name='t' ), target( Py.SingleTarget( name='t' ) ) )
		self._buildTest( Py.TupleTarget( targets=[ Py.SingleTarget( name='t' ), Py.SingleTarget( name='v' ) ] ), target( ( Py.SingleTarget( name='t' ), Py.SingleTarget( name='v' ) ) ) )
		self._buildTest( Py.ListTarget( targets=[ Py.SingleTarget( name='t' ), Py.SingleTarget( name='v' ) ] ), target( [ Py.SingleTarget( name='t' ), Py.SingleTarget( name='v' ) ] ) )


	def test_ExprBuilt(self):
		self._buildTest( Py.Load( name='x' ), ExprBuilt( Py.Load( name='x' ) ) )

	def test_StrLit(self):
		self._buildTest( Py.StringLiteral( value='abc\\n', format='ascii', quotation='single' ), StrLit( 'abc\n' ) )
		self._buildTest( Py.StringLiteral( value='abc\'\\n', format='ascii', quotation='single' ), StrLit( 'abc\'\n' ) )
		self._buildTest( Py.StringLiteral( value='abc\\n', format='unicode', quotation='double' ), StrLit( 'abc\n', format='unicode', quotation='double' ) )

	def test_IntLit(self):
		self._buildTest( Py.IntLiteral( value='123', numType='int', format='decimal' ), IntLit( 123 ) )
		self._buildTest( Py.IntLiteral( value='123', numType='long', format='decimal' ), IntLit( 123L ) )
		self._buildTest( Py.IntLiteral( value='40', numType='int', format='hex' ), IntLit( 64, format='hex' ) )

	def test_IntLit(self):
		self._buildTest( Py.FloatLiteral( value='3.14' ), FloatLit( 3.14 ) )

	def test_ComplexLit(self):
		self._buildTest( Py.Add( x=Py.FloatLiteral( value='3.14' ), y=Py.ImaginaryLiteral( value='2.18' ) ), ComplexLit( 3.14, 2.18 ) )

	def test_Load(self):
		self._buildTest( Py.Load( name='x' ), Load( 'x' ) )

	def test_TupleLiteral(self):
		self._buildTest( Py.TupleLiteral( values=[ Py.Load( name='t' ), Py.Load( name='v' ) ] ), TupleLit( [ Load( 't' ), Load( 'v' ) ] ) )

	def test_ListLiteral(self):
		self._buildTest( Py.ListLiteral( values=[ Py.Load( name='t' ), Py.Load( name='v' ) ] ), ListLit( [ Load( 't' ), Load( 'v' ) ] ) )
