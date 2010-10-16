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
Defines a metaclass for dispatching method calls based on node classes of DMObject instances,
and a dispatch function.
The name of the class is used to choose the method to call.
Rules of inheritance apply; names of superclasses will be used if that is all that is available

Methods should have the form:

@DMObjectNodeDispatchMethod( NodeClass )
def methodFor_NodeClass(self, arg0, arg1, ... argM, node, fieldName0, fieldName1, ... fieldNameN):
	pass
	
NodeClass
	Reference to the node class
arg0 ... argM
	Additional arguments passed to method calls. They are passed in a tuple to
	the 'args' parameter of the nodeMethodDispatch() function.
node
	A reference to the node
fieldName0 ... fieldNameN
	The names of fields in the node class
	
	
A dispatch class is declared like so:
class MyDispatch (object):
	__dispatch_num_args__ = M
	
M
	the number of additional arguments (arg0 ... argM  above)
	
	
To dispatch, call:
	dmObjectNodeMethodDispatch( dispatchInstance, node, args ) -> result_of_method_invocation
		dispatchInstance - the object that is an instance of a dispatch class
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @dispatchInstance that is invoked

	dmObjectNodeMethodDispatchAndGetName( dispatchInstance, node, args ) -> (result_of_method_invocation, method_name)
		dispatchInstance - the object that is an instance of a dispatch class
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @dispatchInstance that is invoked
"""



class BadFieldNameException (Exception):
	def __init__(self, className, fieldName):
		super( BadFieldNameException, self ).__init__( 'Could not get field named \'%s\' from class \'%s\''  %  ( fieldName, className ) )

class DMObjectNodeDispatchMethodCannotHaveVarArgs (Exception):
	def __init__(self, methodName):
		super( DMObjectNodeDispatchMethodCannotHaveVarArgs, self ).__init__( 'Object node dispatch method \'%s\' should not have variable arguments'  %  methodName )


		
		
class DMObjectNodeDispatchMethodWrapper (object):		
	def __init__(self, nodeClass, function):
		self._function = function
		self._nodeClass = nodeClass

		
	def _init(self, numArgs):
		args, varargs, varkw, defaults = inspect.getargspec( self._function )
		if varargs is not None:
			raise DMObjectNodeDispatchMethodCannotHaveVarArgs( self._function.__name__ )
		self._indices = [ self._getFieldIndex( name )   for name in args[2+numArgs:] ]
		
		self._varKWTable = None
		if varkw is not None:
			self._varKWTable = []
			for i in xrange( 0, self._nodeClass.getNumFields() ):
				if i not in self._indices:
					self._varKWTable.append( ( self._nodeClass.getField( i ).getName(), i ) )
					
					
	def call(self, object, dispatchSelf, args):
		callArgs = args + tuple( [ object ] + [ object.get( index )   for index in self._indices ] )
		
		if self._varKWTable is None:
			return self._function( dispatchSelf, *callArgs )
		else:
			kwargs = {}
			for name, index in self._varKWTable:
				kwargs[name] = object.get( index )
			return self._function( dispatchSelf, *callArgs, **kwargs )
		
		
	def callNoArgs(self, object, dispatchSelf):
		callArgs = tuple( [ object ] + [ object.get( index )   for index in self._indices ] )
		
		if self._varKWTable is None:
			return self._function( dispatchSelf, *callArgs )
		else:
			kwargs = {}
			for name, index in self._varKWTable:
				kwargs[name] = object.get( index )
			return self._function( dispatchSelf, *callArgs, **kwargs )
		
		
	def getName(self):
		return self._function.__name__
		
		
	def _getFieldIndex(self, name):
		index = self._nodeClass.getFieldIndex( name )
		if index == -1:
			raise BadFieldNameException, ( self._nodeClass.getName(), name )
		return index
	
	
	
def DMObjectNodeDispatchMethod(nodeClass):
	def decorator(fn):
		return DMObjectNodeDispatchMethodWrapper( nodeClass, fn )
	return decorator
		

_methodTables = {}

def _getMethodTableForClass(cls, numArgs):
	try:
		return _methodTables[cls]
	except KeyError:
		# Gather the relevant methods for this class
		methodTable = {}
		# Add entries to the method table
		for k, v in cls.__dict__.items():
			if isinstance( v, DMObjectNodeDispatchMethodWrapper ):
				method = v
				nodeClass = v._nodeClass
				method._init( numArgs )
				methodTable[nodeClass] = method
		_methodTables[cls] = methodTable
		return methodTable
		
	
		
def _initDispatchTableForClass (cls):
	try:
		numArgs = cls.__dispatch_num_args__
	except AttributeError:
		numArgs = 0
	
	# Store two tables for mapping class to method; the method table, and the dispatch table
	# The method table stores entries only for methods that were declared
	# The dispatch table stores those, in addition to mappings for subclasses of the node class
	# The method table is copied from base classes
	
	# Gather methods from base classes
	fullMethodTable = {}
	for base in cls.mro():
		fullMethodTable.update( _getMethodTableForClass( base, numArgs ) )
		
	# Incorporate methods from @cls
	fullMethodTable.update( _getMethodTableForClass( cls, numArgs ) )

	# Initialise the dispatch table
	cls.__dispatch_table__ = fullMethodTable
	return cls.__dispatch_table__
		

		
		
def _getMethodForNode(dispatchInstance, node):
	# Get the class of the dispatch instance
	dispatchClass = type( dispatchInstance )
	
	# Try to get the dispatch table. If it does not exist, initialise it
	try:
		dispatchTable = dispatchClass.__dict__['__dispatch_table__']
	except KeyError:
		dispatchTable = _initDispatchTableForClass( dispatchClass )

	# First, try to get a method for the class of @node
	try:
		return dispatchTable[node.getDMNodeClass()]
	except KeyError:
		# Did not find a suitable method
		# Try looking for one declared for a superclass
		nodeClass = node.getDMNodeClass()
		
		# Iterate over all superclasses of @nodeClass, until we hit one that has an entry
		superClass = nodeClass.getSuperclass()
		method = None
		while superClass is not None:
			# Try to get a method for a superclass of @node
			try:
				method = dispatchTable[superClass]
				break
			except KeyError:
				pass
			superClass = superClass.getSuperclass()
		# Cache the result so that any lookups in the future will be faster
		dispatchTable[nodeClass] = method
		return method
		
		

def dmObjectNodeMethodDispatch(dispatchInstance, node, *args):
	if isObjectNode( node ):
		method = _getMethodForNode( dispatchInstance, node )
		if method is None:
			raise DispatchError, 'dmObjectNodeMethodDispatch(): could not find method for nodes of type %s in class %s'  %  ( node.getDMNodeClass().getName(), type( dispatchInstance ).__name__ )
		return method.call( node, dispatchInstance, args )
	else:
		raise DispatchDataError, 'dmObjectNodeMethodDispatch(): can only dispatch on objects; not on %s'  %  ( nodeToSXString( node ) )


		
def dmObjectNodeMethodDispatchAndGetName(dispatchInstance, node, *args):
	if isObjectNode( node ):
		method = _getMethodForNode( dispatchInstance, node )
		if method is None:
			raise DispatchError, 'dmObjectNodeMethodDispatchAndGetName(): could not find method for nodes of type %s in class %s'  %  ( node.getDMNodeClass().getName(), type( dispatchInstance ).__name__ )
		return method.call( node, dispatchInstance, args ), method.getName()
	else:
		raise DispatchDataError, 'dmObjectNodeMethodDispatchAndGetName(): can only dispatch on objects; not on %s'  %  ( nodeToSXString( node ) )
