##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************


class InvalidDPEventJSon (Exception):
	pass

class InvalidDPEventName (Exception):
	pass



_eventClassNameToClass = {}


class DPEventClass (type):
	def __init__(cls, clsName, clsBases, clsDict):
		super( DPEventClass, cls ).__init__( clsName, clsBases, clsDict )
		_eventClassNameToClass[clsName] = cls
		


class DPEvent (object):
	__metaclass__ = DPEventClass
	
	
	
	
	def json(self):
		return [ self.__class__.__name__, self.jsonContent() ]
		
		
	def jsonContent(self):
		return []
	
	
	
	@classmethod
	def fromJSonContent(cls, content):
		return DPEvent()
	
		
		
	@staticmethod
	def fromJSon(j):
		if isinstance( j, list )   and   len( j ) == 2:
			typeName = j[0]
			if isinstance( typeName, str )  or  isinstance( typeName, unicode ):
				try:
					cls = _eventClassNameToClass[typeName]
				except KeyError:
					raise InvalidDPEventName
				
				return cls.fromJSonContent( j[1] )
		raise InvalidDPEventJSon

	
	
	
import unittest


class TestCase_DPEvent (unittest.TestCase):
	class MyEvent (DPEvent):
		def __init__(self, x):
			self.x = x
			
		def jsonContent(self):
			return self.x
		
		@classmethod
		def fromJSonContent(self, content):
			return TestCase_DPEvent.MyEvent( content )
		
		
		
	def test_to_JSon(self):
		ev = TestCase_DPEvent.MyEvent( 'hi' )
		self.assert_( ev.json()  ==  [ 'MyEvent', 'hi' ] )

	def test_from_JSon(self):
		json = [ 'MyEvent', 'hi' ]
		ev = DPEvent.fromJSon( json )
		self.assert_( isinstance( ev, TestCase_DPEvent.MyEvent ) )
		self.assert_( ev.x == 'hi' )
