##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Util.NodeUtil import isListNode, nodeToSXString
#from Britefury.DocTree.DocTreeNode import DocTreeNode
from BritefuryJ.DocTree import DocTreeNode

from Britefury.Dispatch.Dispatch import DispatchError, DispatchDataError



def methodDispatch(target, node, *args):
	if isListNode( node ):
		if len( node ) < 1:
			raise DispatchDataError, 'methodDispatch(): require at least 1 element for dispatch'
		name = node[0]
		try:
			method = getattr( target, name )
		except AttributeError:
			raise DispatchError, 'methodDispatch(): could not find method named %s in class %s'  %  ( name, type( target ).__name__ )
		return method( *( args + ( node, ) + tuple( node[1:] ) ) )
	else:
		if isinstance( node, DocTreeNode ):
			raise DispatchDataError, 'methodDispatch(): can only dispatch on lists; not on %s:%s  (from %s)'  %  ( node.getClass().getName(), nodeToSXString( node ), nodeToSXString( node.getParentTreeNode().getParentTreeNode() ) )
		else:
			raise DispatchDataError, 'methodDispatch(): can only dispatch on lists; not on %s'  %  ( nodeToSXString( node ) )


		
def methodDispatchAndGetName(target, node, *args):
	if isListNode( node ):
		if len( node ) < 1:
			raise DispatchDataError, 'methodDispatch(): require at least 1 element for dispatch'
		name = node[0]
		try:
			method = getattr( target, name )
		except AttributeError:
			raise DispatchError, 'methodDispatch(): could not find method named %s in class %s'  %  ( name, type( target ).__name__ )
		#return method( *( args + ( node, ) + tuple( node[1:] ) ) ),   name
		try:
			lastArgs = tuple( node[1:] )
		except TypeError:
			print node[1:]
			lastArgs = ()
		methodArgs = args + ( node, ) + lastArgs
		return method( *methodArgs ),   name
	else:
		if isinstance( node, DocTreeNode ):
			raise DispatchDataError, 'methodDispatch(): can only dispatch on lists; not on %s:%s  (from %s)'  %  ( node.getClass().getName(), nodeToSXString( node ), nodeToSXString( node.getParentTreeNode().getParentTreeNode() ) )
		else:
			raise DispatchDataError, 'methodDispatch(): can only dispatch on lists; not on %s'  %  ( nodeToSXString( node ) )


		
		
import unittest


class TestCase_MethodDispatch (unittest.TestCase):
	class A (object):
		def foo(self, node, a, b, c):
			return a + b + c

	def testDispatchTypeError(self):
		d = self.A()
		self.assertRaises( DispatchDataError, lambda: methodDispatch( d, 'abc' ) )
		
		
	def testDispatchSizeError(self):
		d = self.A()
		self.assertRaises( DispatchDataError, lambda: methodDispatch( d, [] ) )
		
		
	def testDispatchNameError(self):
		d = self.A()
		self.assertRaises( DispatchError, lambda: methodDispatch( d, [ 'bar' ] ) )
		
		
	def testDispatch(self):
		d = self.A()
		x = methodDispatch( d, [ 'foo', 'Hello ', 'there ', 'world' ] )
		self.assert_( x == 'Hello there world' )
		
		
