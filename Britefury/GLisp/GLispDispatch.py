##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToString
from Britefury.DocTree.DocTreeNode import DocTreeNode



class DispatchTypeError (Exception):
	pass

class DispatchSizeError (Exception):
	pass

class DispatchNameError (Exception):
	pass


def dispatch(target, xs, *args):
	if isGLispList( xs ):
		if len( xs ) < 1:
			raise DispatchSizeError, 'GLisp dispatch: require at least 1 element for dispatch'
		name = xs[0]
		try:
			method = getattr( target, name )
		except AttributeError:
			raise DispatchNameError, 'GLisp dispatch: could not find method named %s in class %s'  %  ( name, type( target ).__name__ )
		return method( *( args + ( xs, ) + tuple( xs[1:] ) ) )
	else:
		if isinstance( xs, DocTreeNode ):
			raise DispatchTypeError, 'GLisp dispatch: can only dispatch on lists; not on %s  (from %s)'  %  ( gLispSrcToString( xs ), gLispSrcToString( xs.getParentTreeNode().getParentTreeNode() ) )
		else:
			raise DispatchTypeError, 'GLisp dispatch: can only dispatch on lists; not on %s'  %  ( gLispSrcToString( xs ) )


		
		
import unittest


class TestCase_GLispDispatch (unittest.TestCase):
	def testDispatch(self):
		class A (object):
			def foo(self, xs, a, b, c):
				return a + b + c
			
		
		d = A()
		
		self.assertRaises( DispatchTypeError, lambda: dispatch( d, 'abc' ) )
		self.assertRaises( DispatchSizeError, lambda: dispatch( d, [] ) )
		self.assertRaises( DispatchNameError, lambda: dispatch( d, [ 'bar' ] ) )
		
		x = dispatch( d, [ 'foo', 'Hello ', 'there ', 'world' ] )
		self.assert_( x == 'Hello there world' )
		
