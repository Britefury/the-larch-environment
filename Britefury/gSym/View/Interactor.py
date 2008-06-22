##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import gtk

from Britefury.gSym.View.InteractorEvent import InteractorEvent, InteractorEventKey, InteractorEventText, InteractorEventTokenList




class NoEventMatch (Exception):
	pass


def keyEventMethod(keyString):
	def decorate(method):
		def decoratedMethod(self, event, *args, **kwargs):
			if isinstance( event, InteractorEventKey ):
				if event.keyString == keyString:
					nodeToSelect = method( self, event.keyString, *args, **kwargs )
					return nodeToSelect, None
			raise NoEventMatch
		return decoratedMethod
	return decorate
	


def accelEventMethod(accelString):
	keyValue, mods = gtk.accelerator_parse( accelString )
	def decorate(method):
		def decoratedMethod(self, event, *args, **kwargs):
			if isinstance( event, InteractorEventKey ):
				if event.keyValue == keyValue  and  event.mods == mods:
					nodeToSelect = method( self, event.keyValue, event.mods, *args, **kwargs )
					return nodeToSelect, None
			raise NoEventMatch
		return decoratedMethod
	return decorate
	


def textEventMethod():
	def decorate(method):
		def decoratedMethod(self, event, *args, **kwargs):
			if isinstance( event, InteractorEventText ):
				nodeToSelect = method( self, event.bUserEvent, event.bChanged, event.text, *args, **kwargs )
				return nodeToSelect, None
			raise NoEventMatch
		return decoratedMethod
	return decorate
	


def tokenListEventMethod(*tokenClasses):
	def decorate(method):
		def decoratedMethod(self, event, *args, **kwargs):
			if isinstance( event, InteractorEventTokenList ):
				if len( event.tokens )  >=  len( tokenClasses ):
					bEventMatched = True
					for token, tokenClass  in  zip( event.tokens, tokenClasses ):
						if token.tokenClass != tokenClass:
							bEventMatched = False
							break
					if bEventMatched:
						tokenValues = [ token.value   for token in event.tokens[:len(tokenClasses)] ]
						nodeToSelect = method( self, event.bUserEvent, event.bChanged, *(tokenValues + list(args)), **kwargs )
						return nodeToSelect, event.tailEvent( len( tokenClasses ) )
			raise NoEventMatch
		return decoratedMethod
	return decorate
	




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
	



