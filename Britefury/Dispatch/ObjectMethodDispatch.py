##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import copy

from Britefury.Dispatch.Dispatch import DispatchError, DispatchDataError

import inspect




"""
Defines a metaclass for dispatching method calls based on node classes of DMObject instances,
and a dispatch function.
The name of the class is used to choose the method to call.
Rules of inheritance apply; names of superclasses will be used if that is all that is available

Methods should have the form:

@ObjectDispatchMethod( Class1, Class2, ... ClassN )
def methodFor_NodeClass(self, arg0, arg1, ... argM, node):
	pass
	
NodeClass
	Reference to the node class
arg0 ... argM
	Additional arguments passed to method calls. They are passed in a tuple to
	the 'args' parameter of the nodeMethodDispatch() function.
node
	A reference to the node
	
	
A dispatch class is declared like so:
class MyDispatch (object):
	__dispatch_num_args__ = M
	
M
	the number of additional arguments (arg0 ... argM  above)
	
	
To dispatch, call:
	objectMethodDispatch( dispatchInstance, node, args ) -> result_of_method_invocation
		dispatchInstance - the object that is an instance of a dispatch class
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @dispatchInstance that is invoked

	objectMethodDispatchAndGetName( dispatchInstance, node, args ) -> (result_of_method_invocation, method_name)
		dispatchInstance - the object that is an instance of a dispatch class
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @dispatchInstance that is invoked
"""



class ObjectDispatchMethodCannotHaveVarArgs (Exception):
	def __init__(self, className):
		super( ObjectDispatchMethodCannotHaveVarArgs, self ).__init__( 'Object dispatch method \'%s\' should not have variable arguments'  %  className )

class ObjectDispatchMethodCannotHaveVarKWArgs (Exception):
	def __init__(self, methodName):
		super( ObjectDispatchMethodCannotHaveVarKWArgs, self ).__init__( 'Object dispatch method \'%s\' should not have varaible keyword arguments'  %  methodName )

		
		
class ObjectDispatchMethodWrapper (object):		
	def __init__(self, classes, function):
		args, varargs, varkw, defaults = inspect.getargspec( function )
		if varargs is not None:
			raise ObjectNodeDispatchMethodCannotHaveVarArgs( function.__name__ )
		if varkw is not None:
			raise ObjectDispatchMethodCannotHaveVarKWArgs( function.__name__ )
		
		self._function = function
		self._classes = classes

		
	def call(self, object, dispatchInstance, args):
		callArgs = args + ( object, )
		
		return self._function( dispatchInstance, *callArgs )
		
		
	def callNoArgs(self, object, dispatchInstance):
		return self._function( dispatchInstance, object )
		
		
	def getName(self):
		return self._function.__name__
	
	
	
def ObjectDispatchMethod(*classes):
	def decorator(fn):
		return ObjectDispatchMethodWrapper( classes, fn )
	return decorator
		
	
	
		

		
def _initDispatchTableForClass(cls):
	try:
		numArgs = cls.__dispatch_num_args__
	except AttributeError:
		numArgs = 0
	
	# Store two tables for mapping class to method; the method table, and the dispatch table
	# The method table stores entries only for methods that were declared
	# The dispatch table stores those, in addition to mappings for subclasses of the object class
	# The method table is copied from base classes

	# Initialise method table with entries from base classes
	cls.__method_table__ = {}
	for base in cls.mro():
		try:
			cls.__method_table__.update( base.__method_table__ )
		except AttributeError:
			pass

	# Add entries to the method table
	for k, v in cls.__dict__.items():
		if isinstance( v, ObjectDispatchMethodWrapper ):
			method = v
			for c in v._classes:
				cls.__method_table__[c] = method

			
	# Initialise the dispatch table to a copy of the method table
	cls.__dispatch_table__ = copy.copy( cls.__method_table__ )
	return cls.__dispatch_table__

		
def _getMethodForObject(dispatchInstance, obj):
	# Get the class of the dispatch instance
	dispatchClass = type( dispatchInstance )
	
	# Try to get the dispatch table. If it does not exist, initialise it
	try:
		dispatchTable = dispatchClass.__dispatch_table__
	except AttributeError:
		dispatchTable = _initDispatchTableForClass( dispatchClass )
		
	
	# First, try to get a method for the class of @obj
	objClass = type( obj )
	try:
		return dispatchTable[objClass]
	except KeyError:
		# Did not find a suitable method
		# Try looking for one declared for a superclass
		
		# Iterate through mro of class
		method = None
		for superClass in objClass.mro():
			# Try to get a method for cls
			try:
				method = dispatchTable[superClass]
				break
			except KeyError:
				pass
		# Cache the result so that any lookups in the future will be faster
		dispatchTable[objClass] = method
		return method
		
		
def objectMethodDispatch(dispatchInstance, obj, *args):
	method = _getMethodForObject( dispatchInstance, obj )
	if method is None:
		raise DispatchError, 'objectMethodDispatch(): could not find method for objects of type %s in class %s'  %  ( type( obj ).__name__, type( dispatchInstance ).__name__ )
	return method.call( obj, dispatchInstance, args )


		
def objectMethodDispatchAndGetName(dispatchInstance, obj, *args):
	method = _getMethodForObject( dispatchInstance, obj )
	if method is None:
		raise DispatchError, 'objectMethodDispatchAndGetName(): could not find method for objects of type %s in class %s'  %  (type( obj ).__name__, type( dispatchInstance ).__name__ )
	return method.call( obj, dispatchInstance, args ), method.getName()
