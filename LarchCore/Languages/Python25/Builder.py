##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from Britefury.Util.Abstract import abstractmethod

from BritefuryJ.DocModel import DMNode

import LarchCore.Languages.Python25.Schema as Py
from LarchCore.Languages.Python25 import Embedded



def embeddedExpression(x):
	return Py.EmbeddedObjectExpr( embeddedValue=DMNode.embedIsolated( x, False ) )

def embeddedStatement(x):
	return Py.EmbeddedObjectStmt( embeddedValue=DMNode.embedIsolated( x, False ) )


class _Builder (object):
	@abstractmethod
	def build(self):
		pass


class Target (_Builder):
	@staticmethod
	def coerce(x):
		if isinstance( x, Target ):
			return x
		elif isinstance( x, Embedded.EmbeddedPython25Target ):
			return TargetBuilt( x.target )
		elif isinstance( x, DMNode )  and  x.isInstanceOf( Py.Target ):
			return TargetBuilt( x )
		elif isinstance( x, tuple ):
			return TupleTarget( x )
		elif isinstance( x, list ):
			return ListTarget( x )
		else:
			raise TypeError, 'cannot coerce %s to a target'  %  type( x )







class Expr (_Builder):
	@staticmethod
	def coerce(x):
		if x is None:
			return none_
		elif isinstance( x, Expr ):
			return x
		elif isinstance( x, Embedded.EmbeddedPython25Expr ):
			return ExprBuild( x.expression )
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
		self.targets = [ Target.coerce( t )   for t in targets ]

	def build(self):
		return Py.TupleTarget( targets=[ t.build()   for t in self.targets ] )


class ListTarget (Target):
	def __init__(self, targets):
		self.targets = [ Target.coerce( t )   for t in targets ]

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
	def __init__(self, value, format='ascii', quotation='single'):
		self.value = value
		self.format = format
		self.quotation = quotation
	
	def build(self):
		r = repr( self.value )
		try:
			start = r.index( '\'' )
		except ValueError:
			start = r.index( "\"" )
		r = r[start+1:-1]
		return Py.StringLiteral( value=r, format=self.format, quotation=self.quotation )

	
	
class IntLit (Literal):
	def __init__(self, value, numType='int', format='decimal'):
		self.value = value
		self.numType = numType
		self.format = format
		
	def build(self):
		return Py.IntLiteral( value=repr( self.value ), format=self.format, numType=self.numType )


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
		return Py.Add( x=Py.FloatLiteral( value=self.real ), y=Py.ImaginaryLiteral( value=self.imag ) )
	
	


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
		self.xs = [ Expr.coerce(x)   for x in xs ]

	def build(self):
		return Py.TupleLiteral( values=[ x.build()   for x in self.xs ] )


class ListLit (Expr):
	def __init__(self, xs):
		self.xs = [ Expr.coerce(x)   for x in xs ]

	def build(self):
		return Py.ListLiteral( values=[ x.build()   for x in self.xs ] )


