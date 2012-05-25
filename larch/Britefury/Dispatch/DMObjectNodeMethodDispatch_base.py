##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import Britefury

import inspect

from BritefuryJ.Dispatch import DMObjectNodeDispatchPyMethodInvoker

from BritefuryJ.DocModel import DMPolymorphicMap




class BadFieldNameException (Exception):
	def __init__(self, className, fieldName):
		super( BadFieldNameException, self ).__init__( 'Could not get field named \'%s\' from class \'%s\''  %  ( fieldName, className ) )

class DMObjectNodeDispatchMethodCannotHaveVarArgs (Exception):
	def __init__(self, methodName):
		super( DMObjectNodeDispatchMethodCannotHaveVarArgs, self ).__init__( 'Object node dispatch method \'%s\' should not have variable arguments'  %  methodName )

class DMObjectNodeDispatchMethodCannotHaveVarKWArgs (Exception):
	def __init__(self, methodName):
		super( DMObjectNodeDispatchMethodCannotHaveVarKWArgs, self ).__init__( 'Object node dispatch method \'%s\' should not have variable keyword arguments'  %  methodName )


		
		
class DMObjectNodeDispatchMethodWrapper (object):		
	def __init__(self, nodeClass, function):
		self._function = function
		self._nodeClass = nodeClass

		
	def _makeInvoker(self, numArgs):
		unwrapped = self._function
		while True:
			try:
				unwrapped = unwrapped.__dispatch_unwrapped_method__
			except AttributeError:
				break
			
		args, varargs, varkw, defaults = inspect.getargspec( unwrapped )

		if varargs is not None:
			raise DMObjectNodeDispatchMethodCannotHaveVarArgs( self._function.__name__ )
		if varkw is not None:
			raise DMObjectNodeDispatchMethodCannotHaveVarKWArgs( self._function.__name__ )

		indices = [ self._getFieldIndex( name )   for name in args[2+numArgs:] ]
		
		return DMObjectNodeDispatchPyMethodInvoker( self._function, indices )
		
					
					
	def getName(self):
		return self._function.__name__
		
		
	def _getFieldIndex(self, name):
		index = self._nodeClass.getFieldIndex( name )
		if index == -1:
			raise BadFieldNameException, ( self._nodeClass.getName(), name )
		return index




_methodTables = {}

def _getMethodTableForClass(cls, numArgs):
	try:
		return _methodTables[cls]
	except KeyError:
		# Gather the relevant methods for this class
		methodTable = DMPolymorphicMap()
		# Add entries to the method table
		for k, v in cls.__dict__.items():
			if isinstance( v, DMObjectNodeDispatchMethodWrapper ):
				method = v
				nodeClass = v._nodeClass
				invoker = method._makeInvoker( numArgs )
				methodTable.put( nodeClass, invoker )
		_methodTables[cls] = methodTable
		return methodTable
		
	
		
def createDispatchTableForClass(cls):
	try:
		numArgs = cls.__dispatch_num_args__
	except AttributeError:
		numArgs = 0
	
	# Gather methods from base classes
	fullMethodTable = DMPolymorphicMap()
	for base in cls.mro():
		fullMethodTable.update( _getMethodTableForClass( base, numArgs ) )
		
	# Incorporate methods from @cls
	fullMethodTable.update( _getMethodTableForClass( cls, numArgs ) )

	return fullMethodTable
