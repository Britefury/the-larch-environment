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
from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeMethodDispatchMetaClass, objectNodeMethodDispatch, objectNodeMethodDispatchAndGetName

import time


			
		
		
class GSymViewListNodeDispatch (object):
	def __call__(self, xs, ctx, styleSheet, state):
		element = None
		try:
			element, name = methodDispatchAndGetName( self, xs, ctx, styleSheet, state )
			element.setDebugName( name )
		except DispatchError:
			element = ctx.errorElement( '<<VIEW LIST NODE DISPATCH ERROR>>' )
		return element
	
		

	


		
class GSymViewObjectNodeDispatch (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = 3
	
	def __call__(self, node, ctx, styleSheet, state):
		element = None
		try:
			element, name = objectNodeMethodDispatchAndGetName( self, node, ctx, styleSheet, state )
			element.setDebugName( name )
		except DispatchError:
			print node
			element = ctx.errorElement( '<<VIEW OBJECT NODE DISPATCH ERROR>>' )
		return element
	
		

	
class GSymViewPage (Page):
	def __init__(self, title, commandHistory):
		self._title = title
		self._commandHistory = commandHistory
		
		
	def setContentsElement(self, element):
		self._element = element
		
		
	def getTitle(self):
		return self._title
	
	def getContentsElement(self):
		return self._element
	
	def getCommandHistoryController(self):
		return self._commandHistory
	
	def setCommandHistoryListener(self, listener):
		self._commandHistory.setCommandHistoryListener( listener )




