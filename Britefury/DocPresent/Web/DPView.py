##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Web.DPEvent import DPEvent

#

class InvalidDPEventList (Exception):
	pass


class DPView (object):
	def __init__(self, title):
		self._eventQueue = []
		self._rootElement = None
		self._title = title
		
		
	def sendEventsAsJSon(self):
		eventJSon = [ event.json()   for event in self._eventQueue ]
		self._eventQueue = []
		return eventJSon
	
	
	def receiveEventsAsJSon(self, eventsJSon):
		if isinstance( eventsJSon, list ):
			events = [ DPEvent.fromJSon( j )   for j in eventsJSon ]
			return
		raise InvalidDPEventList
		
	
	
	def queueEvent(self, event):
		self._eventQueue.append( event )
		
	
		
	def scripts(self):
		return ''
	
	
	def title(self):
		return self._title
	
	
	def htmlBody(self):
		return '<h1>gSym test</h2>'
	
	
	
	
	def _onElementRealise(self, element):
		pass
	
	def _onElementUnrealise(self, element):
		pass
	
	
