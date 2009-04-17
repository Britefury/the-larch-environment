##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.MethodDispatch import methodDispatch, methodDispatchAndGetName
from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeMethodDispatchMetaClass, objectNodeMethodDispatch, objectNodeMethodDispatchAndGetName

from Britefury.gSym.View.gSymStyles import viewError_textStyle

import time


			
		
		
class GSymViewListNodeDispatch (object):
	def __call__(self, xs, ctx, state):
		element = None
		try:
			element, name = methodDispatchAndGetName( self, xs, ctx, state )
			element.setDebugName( name )
		except DispatchError:
			element = ctx.text( viewError_textStyle, '<<ERROR>>' )
		return element
	
		

	


		
class GSymViewObjectNodeDispatch (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = 2
	
	def __call__(self, xs, ctx, state):
		element = None
		try:
			element, name = objectNodeMethodDispatchAndGetName( self, xs, ctx, state )
			element.setDebugName( name )
		except DispatchError:
			element = ctx.text( viewError_textStyle, '<<ERROR>>' )
		return element
	
		

	


