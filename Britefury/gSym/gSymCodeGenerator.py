##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.GLisp.GLispDispatch import dispatch





class GSymCodeGenerator (object):
	def __init__(self, targetFormat):
		super( GSymCodeGenerator, self ).__init__()
		self._targetFormat = targetFormat
		
		
	def __call__(self, xs):
		return dispatch( self, xs )



class GSymCodeGeneratorCollection (object):
	def __init__(self, codeGenerators):
		super( GSymCodeGeneratorCollection, self ).__init__()
		self.codeGenerators = codeGenerators
		
		
		
		
import unittest
from BritefuryJ.DocModel import DMIORead

class TestCase_GSymCodeGenerator (unittest.TestCase):
	def testGSymCodeGenerator(self):
		class CodeGenerator (GSymCodeGenerator):
			def add(self, node, x, y):
				return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
			
			def sub(self, node, x, y):
				return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
			
			def mul(self, node, x, y):
				return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
			
			def div(self, node, x, y):
				return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
			
			def pow(self, node, x, y):
				return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
			
			def loadLocal(self, node, name):
				return name

			
		c = CodeGenerator( 'ascii' )
		xs = DMIORead.readSX( '(div (add (loadLocal x) (loadLocal y))  (mul (loadLocal a) (loadLocal b)))' )
		
		self.assert_( c( xs )  ==  '( ( x + y ) / ( a * b ) )' )
