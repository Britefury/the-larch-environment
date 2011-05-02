##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocPresent import TreeEventListener
from BritefuryJ.Dispatch import DispatchError
from Britefury.Dispatch.ObjectMethodDispatch import objectMethodDispatch, objectMethodDispatchAndGetName, ObjectDispatchMethod


		
class TreeEventListenerObjectDispatch (TreeEventListener):
	__dispatch_num_args__ = 2
	
	def onTreeEvent(self, element, sourceElement, event):
		try:
			return objectMethodDispatch( self, event, element, sourceElement )
		except DispatchError:
			return False




