##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import weakref
from copy import copy

"Signals and slots module"




class _ConnectionListNode (object):
	__slots__ = [ 'func', 'active', 'prev', 'next' ]

	def __init__(self, func):
		super( _ConnectionListNode, self ).__init__()
		self.func = func
		self.active = True
		self.prev = None
		self.next = None




class _ConnectionList (object):
	__slots__ = [ 'head', 'tail' ]

	def __init__(self):
		super( _ConnectionList, self ).__init__()
		self.head = None
		self.tail = None


	def append(self, node):
		"Append connection node to the list"
		if self.head is None:
			self.head = node

		if self.tail is not None:
			self.tail.next = node

		node.prev = self.tail
		node.next = None
		self.tail = node

	def remove(self, node):
		"Remove connection node from the list"
		if node.prev is not None:
			node.prev.next = node.next

		if node.next is not None:
			node.next.prev = node.prev

		if self.head == node:
			self.head = node.next

		if self.tail == node:
			self.tail = node.prev

	def clear(self):
		self.head = self.tail = None
		
		
	def isEmpty(self):
		return self.head is None
	
	def hasOneElement(self):
		return self.head is not None   and   self.tail is self.head
	
	
	def __iter__(self):
		x = self.head
		while x is not None:
			yield x
			x = x.next





class Signal (object):
	"Signal class"
	
	__slots__ = [ '__weakref__', '_connectionListener', '_funcToConnection', '_connections' ]

	def __init__(self):
		"Constructor"
		super( Signal, self ).__init__()
		self._connectionListener = None



	def __init(self):
		if not hasattr( self, '_connections' ):
			self._funcToConnection = {}
			self._connections = _ConnectionList()

	def __deinit(self):
		del self._funcToConnection
		del self._connections



	def connect(self, func):
		"Connect the signal to the function @func"
		self.__init()

		if func not in self._funcToConnection:
			# Create the connection object
			connection = _ConnectionListNode( func )

			# Add to the connection list
			self._funcToConnection[func] = connection
			self._connections.append( connection )


			if self._connections.hasOneElement():
				# Had 0 connections before adding this one
				if self._connectionListener is not None:
					self._connectionListener( self )
				self._ref()



	def disconnect(self, func):
		"Disconnect the signal from the function @func"
		if hasattr( self, '_connections' ):
			# Get the connection
			connection = self._funcToConnection.get( func )

			if connection is not None:
				# Deactivate the connection, so it will be skipped by any emissions currently taking place
				connection.active = False

				# Remove from the connection list
				del self._funcToConnection[func]
				self._connections.remove( connection )

				if self._connections.isEmpty():
					if self._connectionListener is not None:
						self._connectionListener( self )
					self.__deinit()
					self._unref()


	def disconnectAll(self):
		"Disconnect the signal from all slots"
		if hasattr( self, '_connections' ):
			bHadConnections = not self._connections.isEmpty()

			if bHadConnections:
				# Deactivate all connections
				for connection in self._connections:
					connection.active = False

				self._funcToConnection = {}


				if self._connectionListener is not None:
					self._connectionListener( self )
				self.__deinit()
				self._unref()




	def hasConnections(self):
		"Determine if the signal has any outgoing connections"
		if hasattr( self, '_connections' ):
			return not self._connections.isEmpty()
		else:
			return 0



	def setConnectionListener(self, listener):
		"Add a connection listener"
		if listener is not None  and  self._connectionListener is None:
			self._ref()
		elif listener is None  and  self._connectionListener is not None:
			self._unref()
		self._connectionListener = listener




	def emit(self, *args, **kwargs):
		"Emit"
		if hasattr( self, '_connections' ):
			connection = self._connections.head
			while connection is not None:
				if connection.active:
					connection.func( *args, **kwargs )

				connection = connection.next




	def chainConnect(self, sig):
		"Chain connect to signal @sig"
		self.connect( sig.emit )


	def chainDisconnect(self, sig):
		"Chain disconnect from signal @sig"
		self.disconnect( sig.emit )



	def _ref(self):
		pass

	def _unref(self):
		pass




class _InstanceSignal (Signal):
	__slots__ = [ '_refCount', '_classSignal', '_instance' ]
	
	def __init__(self, classSignal, instance, doc=''):
		super( _InstanceSignal, self ).__init__( doc )
		self._refCount = 0
		self._classSignal = classSignal
		self._instance = instance

	def _ref(self):
		if self._refCount == 0:
			self._classSignal._instanceSignalSet.add( self )
		self._refCount += 1

	def _unref(self):
		self._refCount -= 1
		if self._refCount == 0:
			self._classSignal._instanceSignalSet.remove( self )






class ClassSignal (object):
	__slots__ = [ '_instanceSignals', '_instanceSignalSet', '__doc__' ]
	def __init__(self, doc=''):
		super( ClassSignal, self ).__init__()
		self._instanceSignals = weakref.WeakValueDictionary()
		self._instanceSignalSet = set()
		self.__doc__ = doc


	def __get__(self, instance, owner):
		if instance is None:
			return self
		else:
			key = id( instance )
			try:
				instanceSignal = self._instanceSignals[key]
			except KeyError:
				instanceSignal = _InstanceSignal( self, instance, self.__doc__ )
				self._instanceSignals[key] = instanceSignal
			return instanceSignal

	def __set__(self, instance, value):
		raise TypeError, 'cannot set signal'

	def __delete__(self, instance):
		raise TypeError, 'cannot delete signal'





class DummySignal (object):
	"Dummy signal class"
	def connect(self, func):
		"Connect the signal to the function @func"
		pass

	def disconnect(self, func):
		"Disconnect the signal from the function @func"
		pass


	def hasConnections(self):
		"Determine if the signal has any outgoing connections"
		return False


	def addConnectionListener(self, listener):
		"Add a connection listener"
		pass


	def removeConnectionListener(self, listener):
		"Remove a connection listener"
		pass



	def chainConnect(self, sig):
		"Chain connect to signal @sig"
		pass

	def chainDisconnect(self, sig):
		"Chain disconnect from signal @sig"
		pass





import unittest

class TestCase_Signal (unittest.TestCase):
	def testConnectDisconnect(self):
		s0 = Signal()
		
		log = []
	
	
		def f0(x):
			log.append( '>f0%d' % x )
			s0.disconnect( f1 )
			log.append( '<f0%d' % x )
	
		def f1(x):
			log.append( '>f1%d' % x )
			log.append( '<f1%d' % x )
	
		def f2(x):
			log.append( '>f2%d' % x )
			s0.disconnect( f2 )
			s0.emit(x+1)
			log.append( '<f2%d' % x )
	
		def f3(x):
			log.append( '>f3%d' % x )
			s0.connect( f4 )
			log.append( '<f3%d' % x )
	
		def f4(x):
			log.append( '>f4%d' % x )
			log.append( '<f4%d' % x )
	
	
		s0.connect( f0 )
		s0.connect( f1 )
		s0.connect( f2 )
		s0.connect( f3 )
	
		s0.emit(0)
		
		self.assert_( log == [ '>f00', '<f00', '>f20', '>f01', '<f01', '>f31', '<f31', '>f41', '<f41', '<f20', '>f30', '<f30', '>f40', '<f40' ] )
		
		
	def testChainConnect(self):
		s0 = Signal()
		s1 = Signal()
		
		log = []
		def f(x):
			log.append( 'f%d' % x )
			
		s1.connect( f )

		s0.emit()
		self.assert_( log == [] )

		s0.chainConnect( s1 )
		s0.emit( 0 )
		self.assert_( log == [ 'f0' ] )
		
		s0.emit( 1 )
		self.assert_( log == [ 'f0', 'f1' ] )

		s0.chainDisconnect( s1 )
		s0.emit( 2 )
		self.assert_( log == [ 'f0', 'f1' ] )
		
		
	def testConnectionListner(self):
		log = []
		
		def cl(sig):
			log.append( sig.hasConnections() )
		
		s0 = Signal()
		s0.setConnectionListener( cl )
		
		def f0():
			pass
		
		def f1():
			pass
		
		self.assert_( log == [] )
		s0.connect( f0 )
		self.assert_( log == [ True ] )
		s0.connect( f1 )
		self.assert_( log == [ True ] )
		s0.disconnect( f1 )
		self.assert_( log == [ True ] )
		s0.disconnect( f0 )
		self.assert_( log == [ True, False ] )
		


		
		
if __name__ == '__main__':
	import datetime

	class A (object):
		def p0(self, x):
			pass

		def p1(self, x):
			pass

	x = A()

	s1 = Signal()
	s1.connect( x.p0 )
	s1.connect( x.p1 )




	numEmissions = 1 << 17
	startTime = datetime.datetime.now()

	for i in xrange( 0, numEmissions ):
		s1.emit( i )

	endTime = datetime.datetime.now()

	elapsed = endTime - startTime
	seconds = elapsed.days * 86400.0  +  elapsed.seconds  +  elapsed.microseconds / 1000000.0
	print '%d emissions took %f seconds; %f emissions per second' % ( numEmissions, seconds, numEmissions / seconds )
