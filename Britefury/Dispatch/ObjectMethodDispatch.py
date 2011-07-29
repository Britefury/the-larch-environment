##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
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


from BritefuryJ.Dispatch import ObjectPyMethodDispatch

from Britefury.Dispatch.ObjectMethodDispatch_base import ObjectDispatchMethodWrapper





def ObjectDispatchMethod(*classes):
	def deco(method):
		return ObjectDispatchMethodWrapper( classes, method )
	return deco
		
	
objectMethodDispatch = ObjectPyMethodDispatch.objectMethodDispatch
objectMethodDispatchAndGetName = ObjectPyMethodDispatch.objectMethodDispatchAndGetName
