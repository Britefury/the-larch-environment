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

@ObjectNodeDispatchMethod( NodeClass )
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
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = M
	
M
	the number of additional arguments (arg0 ... argM  above)
	
	
To dispatch, call:
	objectNodeMethodDispatch( target, node, args ) -> result_of_method_invocation
		target - the object that is an instance of a class which uses ObjectNodeMethodDispatchMetaClass
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @target that is invoked

	objectNodeMethodDispatchAndGetName( target, node, args ) -> (result_of_method_invocation, method_name)
		target - the object that is an instance of a class which uses ObjectNodeMethodDispatchMetaClass
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @target that is invoked
"""



class BadFieldNameException (Exception):
	def __init__(self, className, fieldName):
		super( BadFieldNameException, self ).__init__( 'Could not get field named \'%s\' from class \'%s\''  %  ( fieldName, className ) )

class ObjectNodeDispatchMethodCannotHaveVarArgs (Exception):
	def __init__(self, className):
		super( ObjectNodeDispatchMethodCannotHaveVarArgs, self ).__init__( 'Object node dispatch method \'%s\' should not have varargs'  %  className )


		
		
class ObjectNodeDispatchMethodWrapper (object):		
	def __init__(self, nodeClass, function):
		self._function = function
		self._nodeClass = nodeClass

		
	def _init(self, numArgs):
		args, varargs, varkw, defaults = inspect.getargspec( self._function )
		if varargs is not None:
			raise ObjectNodeDispatchMethodCannotHaveVarArgs( self._nodeClass.getName() )
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
	
	
	
def ObjectNodeDispatchMethod(nodeClass):
	def decorator(fn):
		return ObjectNodeDispatchMethodWrapper( nodeClass, fn )
	return decorator
		
	
	
		
class ObjectNodeMethodDispatchMetaClass (type):
	def __init__(cls, name, bases, clsDict):
		super( ObjectNodeMethodDispatchMetaClass, cls ).__init__( name, bases, clsDict )
		
		try:
			numArgs = cls.__dispatch_num_args__
		except AttributeError:
			numArgs = 0
		
		# Store two tables for mapping class to method; the method table, and the dispatch table
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
			if isinstance( v, ObjectNodeDispatchMethodWrapper ):
				method = v
				nodeClass = v._nodeClass
				method._init( numArgs )
				cls.__method_table__[nodeClass] = method

				
		# Initialise the dispatch table to a copy of the method table
		cls.__dispatch_table__ = copy.copy( cls.__method_table__ )
		clsDict['__dispatch_table__'] = cls.__dispatch_table__
		
				
		
				
	def _getMethodForNode(cls, node):
		# First, try to get a method for the class of @node
		try:
			return cls.__dispatch_table__[node.getDMNodeClass()]
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
					method = cls.__dispatch_table__[superClass]
					break
				except KeyError:
					pass
				superClass = superClass.getSuperclass()
			# Cache the result so that any lookups in the future will be faster
			cls.__dispatch_table__[nodeClass] = method
			return method

		

def objectNodeMethodDispatch(target, node, *args):
	if isObjectNode( node ):
		method = type( target )._getMethodForNode( node )
		if method is None:
			raise DispatchError, 'objectNodeMethodDispatch(): could not find method for nodes of type %s in class %s'  %  ( node.getDMNodeClass().getName(), type( target ).__name__ )
		return method.call( node, target, args )
	else:
		raise DispatchDataError, 'objectNodeMethodDispatch(): can only dispatch on objects; not on %s'  %  ( nodeToSXString( node ) )


		
def objectNodeMethodDispatchAndGetName(target, node, *args):
	if isObjectNode( node ):
		method = type( target )._getMethodForNode( node )
		if method is None:
			raise DispatchError, 'objectNodeMethodDispatchAndGetName(): could not find method for nodes of type %s in class %s'  %  ( node.getDMNodeClass().getName(), type( target ).__name__ )
		return method.call( node, target, args ), method.getName()
	else:
		raise DispatchDataError, 'objectNodeMethodDispatchAndGetName(): can only dispatch on objects; not on %s'  %  ( nodeToSXString( node ) )
