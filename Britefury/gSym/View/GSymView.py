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
	def createViewFragment(self, xs, ctx, styleSheet, state):
		element = None
		try:
			element, name = methodDispatchAndGetName( self, xs, ctx, styleSheet, state )
			element.setDebugName( name )
		except DispatchError:
			element = ctx.errorElement( '<<VIEW LIST NODE DISPATCH ERROR>>' )
		return element
	
		

	


		
class GSymViewObjectNodeDispatch (GSymViewFragmentFunction):
	__dispatch_num_args__ = 3
	
	def createViewFragment(self, node, ctx, styleSheet, state):
		element = None
		try:
			element, name = dmObjectNodeMethodDispatchAndGetName( self, node, ctx, styleSheet, state )
			element.setDebugName( name )
		except DispatchError:
			print node
			element = ctx.errorElement( '<<VIEW OBJECT NODE DISPATCH ERROR>>' )
		return element
	
		

	
class GSymViewObjectDispatch (GSymViewFragmentFunction):
	__dispatch_num_args__ = 3
	
	def createViewFragment(self, obj, ctx, styleSheet, state):
		element = None
		try:
			element, name = objectMethodDispatchAndGetName( self, obj, ctx, styleSheet, state )
			element.setDebugName( name )
		except DispatchError:
			print obj
			element = ctx.errorElement( '<<VIEW OBJECT DISPATCH ERROR>>' )
		return element




