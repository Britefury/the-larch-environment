##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import gtk

from Britefury.gSym.View.InteractorEvent import InteractorEvent, InteractorEventKey, InteractorEventText, InteractorEventBackspaceStart, InteractorEventDeleteEnd




class NoEventMatch (Exception):
	pass




def eventMethodDecorator(eventClass, invokeMethod, eventTestFn=None):
	def decorate(method):
		def decoratedMethod(self, event, *args, **kwargs):
			if isinstance( event, eventClass ):
				if eventTestFn is None   or   eventTestFn( event ):
					nodeToSelect = invokeMethod( method, self, event, args, kwargs )
					return nodeToSelect, None
			raise NoEventMatch
		return decoratedMethod
	return decorate


def keyEventMethod(keyString):
	return eventMethodDecorator( InteractorEventKey, lambda method, target, event, args, kwargs: method( target, event.keyString, *args, **kwargs ), lambda event: event.keyString == keyString )
	


def accelEventMethod(accelString):
	keyValue, mods = gtk.accelerator_parse( accelString )
	return eventMethodDecorator( InteractorEventKey, lambda method, target, event, args, kwargs: method( target, event.keyValue, event.mods, *args, **kwargs ), lambda event: event.keyValue == keyValue  and  event.mods == mods )
	


def textEventMethod():
	return eventMethodDecorator( InteractorEventText, lambda method, target, event, args, kwargs: method( target, event.bUserEvent, event.bChanged, event.text, *args, **kwargs ), None )



def backspaceStartMethod():
	return eventMethodDecorator( InteractorEventBackspaceStart, lambda method, target, event, args, kwargs: method( target, *args, **kwargs ), None )
	
def deleteEndMethod():
	return eventMethodDecorator( InteractorEventDeleteEnd, lambda method, target, event, args, kwargs: method( target, *args, **kwargs ), None )
	




class InteractorClass (type):
	def __init__(cls, clsName, clsBases, clsDict):
		eventMethods = []
		
		for base in clsBases:
			try:
				methods = base.eventMethods
			except AttributeError:
				pass
			else:
				for m in methods:
					if m not in eventMethods:
						eventMethods.append( m )
		
		try:
			methods = clsDict['eventMethods']
		except KeyError:
			pass
		else:
			for m in methods:
				if m not in eventMethods:
					eventMethods.append( m )
					
		clsDict['eventMethods'] = eventMethods
		cls.eventMethods = eventMethods

		super( InteractorClass, cls ).__init__( clsName, clsBases, clsDict )


		
		
	

class Interactor (object):
	__metaclass__ = InteractorClass
	
	
	def __init__(self, *args, **kwargs):
		super( Interactor, self ).__init__()
		self._args = args
		self._kwargs = kwargs
		
	
	def handleEvent(self, event):
		"""
		Handle an event
		handleEvent( event )  ->  result, processedEvent
		"""
		for m in self.eventMethods:
			try:
				return m( self, event, *self._args, **self._kwargs )
			except NoEventMatch:
				pass
			
		return None, event
	



