##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Dispatch import ObjectDispatchPyMethodInvoker

from BritefuryJ.Util import PolymorphicMap

import inspect




class ObjectDispatchMethodCannotHaveVarArgs (Exception):
	def __init__(self, className):
		super( ObjectDispatchMethodCannotHaveVarArgs, self ).__init__( 'Object dispatch method \'%s\' should not have variable arguments'  %  className )

class ObjectDispatchMethodCannotHaveVarKWArgs (Exception):
	def __init__(self, methodName):
		super( ObjectDispatchMethodCannotHaveVarKWArgs, self ).__init__( 'Object dispatch method \'%s\' should not have varaible keyword arguments'  %  methodName )

		
		
class ObjectDispatchMethodWrapper (object):		
	def __init__(self, classes, function):
		self._function = function
		self._classes = classes

		
	def _makeInvoker(self):
		unwrapped = self._function
		while True:
			try:
				unwrapped = unwrapped.__dispatch_unwrapped_method__
			except AttributeError:
				break

		args, varargs, varkw, defaults = inspect.getargspec( unwrapped )
		
		if varargs is not None:
			raise ObjectDispatchMethodCannotHaveVarArgs( unwrapped.__name__ )
		if varkw is not None:
			raise ObjectDispatchMethodCannotHaveVarKWArgs( unwrapped.__name__ )
		
		return ObjectDispatchPyMethodInvoker( self._function )

		
	def getName(self):
		return self._function.__name__
	
	
	
def ObjectDispatchMethod(*classes):
	def decorator(fn):
		return ObjectDispatchMethodWrapper( classes, fn )
	return decorator
		
	
	
		

		
_methodTables = {}

def _getMethodTableForClass(cls):
	try:
		return _methodTables[cls]
	except KeyError:
		# Gather the relevant methods for this class
		methodTable = PolymorphicMap()
		# Add entries to the method table
		for k, v in cls.__dict__.items():
			if isinstance( v, ObjectDispatchMethodWrapper ):
				method = v
				invoker = method._makeInvoker()
				for c in v._classes:
					methodTable.put( c, invoker )
		_methodTables[cls] = methodTable
		return methodTable



def createDispatchTableForClass(cls):
	# Gather methods from base classes
	fullMethodTable = PolymorphicMap()
	for base in cls.mro():
		fullMethodTable.update( _getMethodTableForClass( base ) )
		
	# Incorporate methods from @cls
	fullMethodTable.update( _getMethodTableForClass( cls ) )

	return fullMethodTable
