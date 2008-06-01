##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.GLisp.GLispDispatch import dispatch





class GSymCompiler (object):
	def __init__(self, targetFormat):
		super( GSymCompiler, self ).__init__()
		self._targetFormat = targetFormat
		
		
	def __call__(self, xs):
		return dispatch( self, xs )



class GSymCompilerCollection (object):
	def __init__(self, compilers):
		super( GSymCompilerCollection, self ).__init__()
		self.compilers = compilers
		
		
		
		
import unittest
from Britefury.DocModel.DMIO import readSX

class TestCase_GSymCompiler (unittest.TestCase):
	def testGSymCompiler(self):
		class Compiler (GSymCompiler):
			def add(self, x, y):
				return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
			
			def sub(self, x, y):
				return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
			
			def mul(self, x, y):
				return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
			
			def div(self, x, y):
				return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
			
			def pow(self, x, y):
				return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
			
			def loadLocal(self, name):
				return name

			
		c = Compiler( 'ascii' )
		xs = readSX( '(div (add (loadLocal x) (loadLocal y))  (mul (loadLocal a) (loadLocal b)))' )
		
		self.assert_( c( xs )  ==  '( ( x + y ) / ( a * b ) )' )
