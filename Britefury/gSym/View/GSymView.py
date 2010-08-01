##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocPresent.Browser import Page

from BritefuryJ.GSym.View import GSymViewFragmentFunction

from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.MethodDispatch import methodDispatch, methodDispatchAndGetName
from Britefury.Dispatch.DMObjectNodeMethodDispatch import dmObjectNodeMethodDispatch, dmObjectNodeMethodDispatchAndGetName
from Britefury.Dispatch.ObjectMethodDispatch import objectMethodDispatch, objectMethodDispatchAndGetName

import time


			
		
		
class GSymViewListNodeDispatch (GSymViewFragmentFunction):
	def createViewFragment(self, xs, ctx, state):
		element = None
		element, name = methodDispatchAndGetName( self, xs, ctx, state )
		element = element.setDebugName( name )
		return element
	
		

	


		
class GSymViewObjectNodeDispatch (GSymViewFragmentFunction):
	__dispatch_num_args__ = 2
	
	def createViewFragment(self, node, ctx, state):
		element = None
		element, name = dmObjectNodeMethodDispatchAndGetName( self, node, ctx, state )
		element = element.setDebugName( name )
		return element
	
		

	
class GSymViewObjectDispatch (GSymViewFragmentFunction):
	__dispatch_num_args__ = 2
	
	def createViewFragment(self, obj, ctx, state):
		element = None
		element, name = objectMethodDispatchAndGetName( self, obj, ctx, state )
		element = element.setDebugName( name )
		return element




