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

@ObjectDispatchMethod( Class )
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
	__metaclass__ = ObjectMethodDispatchMetaClass
	__dispatch_num_args__ = M
	
M
	the number of additional arguments (arg0 ... argM  above)
	
	
To dispatch, call:
	objectMethodDispatch( target, node, args ) -> result_of_method_invocation
		target - the object that is an instance of a class which uses ObjectMethodDispatchMetaClass
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @target that is invoked

	objectMethodDispatchAndGetName( target, node, args ) -> (result_of_method_invocation, method_name)
		target - the object that is an instance of a class which uses ObjectMethodDispatchMetaClass
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @target that is invoked
"""



class ObjectDispatchMethodCannotHaveVarArgs (Exception):
	def __init__(self, className):
		super( ObjectDispatchMethodCannotHaveVarArgs, self ).__init__( 'Object dispatch method \'%s\' should not have variable arguments'  %  className )

class ObjectDispatchMethodCannotHaveVarKWArgs (Exception):
	def __init__(self, methodName):
		super( ObjectDispatchMethodCannotHaveVarKWArgs, self ).__init__( 'Object dispatch method \'%s\' should not have varaible keyword arguments'  %  methodName )

		
		
class ObjectDispatchMethodWrapper (object):		
	def __init__(self, cls, function):
		args, varargs, varkw, defaults = inspect.getargspec( function )
		if varargs is not None:
			raise ObjectNodeDispatchMethodCannotHaveVarArgs( function.__name__ )
		if varkw is not None:
			raise ObjectDispatchMethodCannotHaveVarKWArgs( function.__name__ )
		
		self._function = function
		self._cls = cls

		
	def call(self, object, dispatchSelf, args):
		callArgs = args + ( object, )
		
		return self._function( dispatchSelf, *callArgs )
		
		
	def callNoArgs(self, object, dispatchSelf):
		return self._function( dispatchSelf, object )
		
		
	def getName(self):
		return self._function.__name__
	
	
	
def ObjectDispatchMethod(cls):
	def decorator(fn):
		return ObjectDispatchMethodWrapper( cls, fn )
	return decorator
		
	
	
		
class ObjectMethodDispatchMetaClass (type):
	def __init__(cls, name, bases, clsDict):
		super( ObjectMethodDispatchMetaClass, cls ).__init__( name, bases, clsDict )
		
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
		for base in bases:
			try:
				cls.__method_table__.update( base.__method_table__ )
			except AttributeError:
				pass
		clsDict['__method_table__'] = cls.__method_table__

		# Add entries to the method table
		for k, v in clsDict.items():
			if isinstance( v, ObjectDispatchMethodWrapper ):
				method = v
				cls.__method_table__[v._cls] = method

				
		# Initialise the dispatch table to a copy of the method table
		cls.__dispatch_table__ = copy.copy( cls.__method_table__ )
		clsDict['__dispatch_table__'] = cls.__dispatch_table__
		
				
		
				
	def _getMethodForObject(cls, obj):
		# First, try to get a method for the class of @obj
		objClass = type( obj )
		try:
			return cls.__dispatch_table__[objClass]
		except KeyError:
			# Did not find a suitable method
			# Try looking for one declared for a superclass
			
			# Iterate through mro of class
			method = None
			for superClass in objClass.mro():
				# Try to get a method for cls
				try:
					method = cls.__dispatch_table__[superClass]
					break
				except KeyError:
					pass
			# Cache the result so that any lookups in the future will be faster
			cls.__dispatch_table__[objClass] = method
			return method

		

def objectMethodDispatch(target, obj, *args):
	method = type( target )._getMethodForObject( obj )
	if method is None:
		raise DispatchError, 'objectMethodDispatch(): could not find method for objects of type %s in class %s'  %  ( type( obj ).__name__, type( target ).__name__ )
	return method.call( obj, target, args )


		
def objectMethodDispatchAndGetName(target, obj, *args):
	method = type( target )._getMethodForObject( obj )
	if method is None:
		raise DispatchError, 'objectMethodDispatchAndGetName(): could not find method for objects of type %s in class %s'  %  (type( obj ).__name__, type( target ).__name__ )
	return method.call( obj, target, args ), method.getName()
