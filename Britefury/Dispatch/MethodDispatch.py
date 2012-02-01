##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
"""
Defines a decorator for marking dispatch methods and dispatch functions.
Rules of inheritance apply; methods for superclasses will be used if that is all that is available



Dispatch methods for objects should have the form:

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



Dispatch methods for document model objects (DMObject) should have the form:

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
	objectMethodDispatch( dispatchInstance, node, args ) -> result_of_method_invocation
		dispatchInstance - the object that is an instance of a dispatch class
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @dispatchInstance that is invoked

	objectMethodDispatchAndGetName( dispatchInstance, node, args ) -> (result_of_method_invocation, method_name)
		dispatchInstance - the object that is an instance of a dispatch class
		node - the node to use as the dispatch 'key'
		args - additional arguments to be supplied to the method from @dispatchInstance that is invoked
"""


from BritefuryJ.Dispatch import DispatchError, PyMethodDispatch

from Britefury.Dispatch.ObjectMethodDispatch_base import ObjectDispatchMethodWrapper
from Britefury.Dispatch.DMObjectNodeMethodDispatch_base import DMObjectNodeDispatchMethodWrapper




def ObjectDispatchMethod(*classes):
	def deco(method):
		return ObjectDispatchMethodWrapper( classes, method )
	return deco


def DMObjectNodeDispatchMethod(nodeClass):
	def deco(method):
		return DMObjectNodeDispatchMethodWrapper( nodeClass, method )
	return deco


methodDispatch = PyMethodDispatch.methodDispatch
methodDispatchAndGetName = PyMethodDispatch.methodDispatchAndGetName
