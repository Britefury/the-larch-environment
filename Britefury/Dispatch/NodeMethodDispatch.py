##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import copy

from Britefury.Util.NodeUtil import isObjectNode, nodeToSXString

from Britefury.Dispatch.Dispatch import DispatchError, DispatchDataError

import inspect




"""
Defines a metaclass for dispatching method calls based on node classes of DMObject instances.
The name of the class is used to choose the method to call.
Rules of inheritance apply; names of superclasses will be used if that is all that is available

Methods should have the form:

def NodeClass(self, arg0, arg1, ... argM, node, fieldName0, fieldName1, ... fieldNameN):
	pass
	
arg0 ... argM
	Additional arguments passed to method calls. They are passed in a tuple to
	the 'args' parameter of the nodeMethodDispatch() function.
node
	A reference to the node
fieldName0 ... fieldNameN
	The names of fields in the node class
	
	
A dispatch class is declared like so:
class MyDispatch (object):
	__metaclass__ = NodeMethodDispatchMetaClass
	__module__ = MyModule
	__num_args__ = M
	
MyModule
	a reference to the DMModule in which all the node classes are defined
M
	the number of additional arguments (arg0 ... argM  above)
"""



class BadFieldNameException (Exception):
	pass


class NodeMethodDispatchMetaClass (type):
	class _Method (object):
		def __init__(self, dmClass, function, numArgs):
			self._function = function
			args, varargs, varkw, defaults = inspect.getargspec( function )
			assert varargs is None
			self._indices = [ self._getFieldIndex( dmClass, name )   for name in args[2+numArgs:] ]
			
			self._varKWTable = None
			if varkw is not None:
				self._varKWTable = []
				for i in xrange( 0, dmClass.getNumFields() ):
					if i not in self._indices:
						self._varKWTable.append( ( dmClass.getField( i ).getName(), i ) )
						
						
		def call(self, object, dispatchSelf, args):
			callArgs = args + tuple( [ object ] + [ object.get( index )   for index in self._indices ] )
			
			if self._varKWTable is None:
				return self._function( dispatchSelf, *callArgs )
			else:
				kwargs = {}
				for name, index in self._varKWTable:
					kwargs[name] = object.get( index )
				return self._function( dispatchSelf, *callArgs, **kwargs )
			
			
		def getName(self):
			return self._function.__name__
			
			
		@staticmethod
		def _getFieldIndex(dmClass, name):
			index = dmClass.getFieldIndex( name )
			if index == -1:
				raise BadFieldNameException
			return index
			

			
	def __init__(cls, name, bases, clsDict):
		super( NodeMethodDispatchMetaClass, cls ).__init__( name, bases, clsDict )
		
		module = cls.__module__
		numArgs = clsDict.get( '__num_args__', 0 )
		
		# Store two tables for mapping class name to method; the method table, and the dispatch table
		# The method table stores entries only for methods that were declared
		# The dispatch table stores those, in addition to mappings for subclasses of the node class
		# The method table is copied from base classes

		# Initialise method table with entries from base classes
		cls.__method_table__ = {}
		for base in bases:
			try:
				cls.__method_table__.update( base.__method_table__ )
			except AttributeError:
				pass
		clsDict['__method_table__'] = cls.__method_table__

		# Add entries to the method table
		for k, v in clsDict.items():
			if inspect.isfunction( v ):
				dmClass = module.get( k )
				method = cls._Method( dmClass, v, numArgs )
				cls.__method_table__[dmClass.getName()] = method

				
		# Initialise the dispatch table to a copy of the method table
		cls.__dispatch_table__ = copy.copy( cls.__method_table__ )
		clsDict['__dispatch_table__'] = cls.__dispatch_table__
		
				
		
				
	def _getMethodForNode(cls, node):
		# First, try to get a method for the class of @node
		try:
			return cls.__dispatch_table__[node.getDMClass().getName()]
		except KeyError:
			# Did not find a suitable method
			# Try looking for one declared for a superclass
			nodeClass = node.getDMClass()
			
			# Iterate over all superclasses of @nodeClass, until we hit one that has an entry
			nodeClass = nodeClass.getSuperclass()
			method = None
			while nodeClass is not None:
				# Try to get a method for a superclass of @node
				try:
					method = cls.__dispatch_table__[nodeClass.getName()]
					break
				except KeyError:
					pass
				nodeClass = nodeClass.getSuperclass()
			# Cache the result so that any lookups in the future will be faster
			cls.__dispatch_table__[node.getDMClass().getName()] = method
			return method

		

def nodeMethodDispatch(target, node, *args):
	if isObjectNode( node ):
		method = type( target )._getMethodForNode( node )
		if method is None:
			raise DispatchError, 'nodeMethodDispatch(): could not find method for nodes of type %s in class %s'  %  ( node.getDMClass().getName(), type( target ).__name__ )
		return method.call( node, target, args )
	else:
		if isinstance( node, DocTreeNode ):
			raise DispatchDataError, 'nodeMethodDispatch(): can only dispatch on objects; not on %s:%s  (from %s)'  %  ( node.getClass().getName(), nodeToSXString( node ), nodeToSXString( node.getParentTreeNode().getParentTreeNode() ) )
		else:
			raise DispatchDataError, 'nodeMethodDispatch(): can only dispatch on objects; not on %s'  %  ( nodeToSXString( node ) )


		
def methodDispatchAndGetName(target, node, *args):
	if isListNode( node ):
		method = type( target )._getMethodForNode( node )
		if method is None:
			raise DispatchError, 'methodDispatchAndGetName(): could not find method for nodes of type %s in class %s'  %  ( node.getDMClass().getName(), type( target ).__name__ )
		return method.call( node, target, args ), method.getName()
	else:
		if isinstance( node, DocTreeNode ):
			raise DispatchDataError, 'methodDispatchAndGetName(): can only dispatch on objects; not on %s:%s  (from %s)'  %  ( node.getClass().getName(), nodeToSXString( node ), nodeToSXString( node.getParentTreeNode().getParentTreeNode() ) )
		else:
			raise DispatchDataError, 'methodDispatchAndGetName(): can only dispatch on objects; not on %s'  %  ( nodeToSXString( node ) )
