##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.gSymCodeGenerator import GSymCodeGenerator


class Python25CodeGenerator (GSymCodeGenerator):
	# STRING LITERALS
	def stringLiteral(self, node, format, quotation, value):
		return repr( value )
	
	def intLiteral(self, node, format, numType, value):
		return repr( value )
	
	def floatLiteral(self, node, value):
		return repr( value )
	
	def imaginaryLiteral(self, node, value):
		return repr( value )
	

	def kwParam(self, node, name, value):
		return name + '=' + self( value )
	
	def call(self, node, target, *params):
		return self( target ) + '( ' + ', '.join( [ self( p )   for p in params ] ) + ' )'
	
	def subscript(self, node, target, index):
		return self( target ) + '[' + self( index ) + ']'
	
	def slice(self, node, target, first, second):
		return self( target ) + '[' + self( first ) + ':' + self( second ) + ']'
	
	def attr(self, node, target, name):
		return self( target ) + '.' + name

	
	def add(self, node, x, y):
		return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
	
	def sub(self, node, x, y):
		return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
	
	def mul(self, node, x, y):
		return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
	
	def div(self, node, x, y):
		return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
	
	def mod(self, node, x, y):
		return '( '  +  self( x )  +  ' % '  +  self( y )  +  ' )'
	
	def pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	def var(self, node, name):
		return name
	
	def nilExpr(self, node):
		return '<NIL>'
	
	def listDisplay(self, node, *x):
		return '[ '  +  ', '.join( [ self( i )   for i in x ] )  +  ' ]'
