##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.LSpace import TreeEventListener
from BritefuryJ.Dispatch import DispatchError
from Britefury.Dispatch.MethodDispatch import methodDispatch


		
class TreeEventListenerObjectDispatch (TreeEventListener):
	__dispatch_num_args__ = 2
	
	def onTreeEvent(self, element, sourceElement, event):
		try:
			return methodDispatch( self, event, element, sourceElement )
		except DispatchError:
			return False




