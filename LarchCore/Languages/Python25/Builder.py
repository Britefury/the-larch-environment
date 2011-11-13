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


def embeddedExpression(x):
	return Py.EmbeddedObjectExpr( embeddedValue=DMNode.embedIsolated( x, False ) )

def embeddedStatement(x):
	return Py.EmbeddedObjectStmt( embeddedValue=DMNode.embedIsolated( x, False ) )


class _Builder (object):
	@abstractmethod
	def build(self):
		pass
	
	
def expr(x):
	if x is None:
		return none_
	elif isinstance( x, _Expr ):
		return x
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
	

def target(x):
	if isinstance( x, _Target ):
		return x
	elif isinstance( x, str )  or  isinstance( x, unicode ):
		return SingleTarget( x )
	elif isinstance( x, tuple ):
		return TupleTarget( x )
	elif isinstance( x, list ):
		return ListTarget( x )
	else:
		raise TypeError, 'cannot coerce %s to a target'  %  type( x )
		
	
	
	
class _Expr (_Builder):
	pass

class _Stmt (_Builder):
	pass





class _Literal (_Expr):
	pass


class StrLit (_Literal):
	def __init__(self, value, format='ascii', quotation='single'):
		self.value = value
		self.format = format
		self.quotation = quotation
	
	def build(self):
		return Py.StringLiteral( value=self.value, format=self.format, quotation=self.quotation )

	
	
class IntLit (_Literal):
	def __init__(self, value, numType='int', format='decimal'):
		self.value = value
		self.numType = numType
		self.format = format
		
	def build(self):
		return Py.IntLiteral( value=repr( self.value ), format=self.format, numType=self.numType )


class FloatLit (_Literal):
	def __init__(self, value):
		self.value = value
	
	def build(self):
		return Py.FloatLiteral( value=repr( self.value ) )

class ImagLit (_Literal):
	def __init__(self, value):
		self.value = value
	
	def build(self):
		return Py.ImaginaryLiteral( value=repr( self.value ) )

class ComplexLit (_Literal):
	def __init__(self, real, imag):
		self.real = real
		self.imag = imag
	
	def build(self):
		return Py.Add( x=Py.FloatLiteral( value=self.real ), y=Py.ImaginaryLiteral( value=self.imag ) )
	
	
	
class _Target (_Builder):
	pass



class SingleTarget (_Target):
	def __init__(self, name):
		self.name = name
	
	def build(self):
		return Py.SingleTarget( name=self.name )
	
	
class TupleTarget (_Target):
	def __init__(self, targets):
		self.targets = [ target( t )   for t in targets ]
	
	def build(self):
		return Py.TupleTarget( targets=[ t.build()   for t in self.targets ] )
	
	
class ListTarget (_Target):
	def __init__(self, targets):
		self.targets = [ target( t )   for t in targets ]
	
	def build(self):
		return Py.ListTarget( targets=[ t.build()   for t in self.targets ] )
	
	
	
	
	
class Load (_Expr):
	def __init__(self, name):
		self.name = name
	
	def build(self):
		return Py.Load( name=self.name )
	
false_ = Load( 'False' )
true_ = Load( 'True' )
none_ = Load( 'None' )



