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
		if self.head == None:
			self.head = node

		if self.tail != None:
			self.tail.next = node

		node.prev = self.tail
		node.next = None
		self.tail = node

	def remove(self, node):
		"Remove connection node from the list"
		if node.prev != None:
			node.prev.next = node.next

		if node.next != None:
			node.next.prev = node.prev

		if self.head == node:
			self.head = node.next

		if self.tail == node:
			self.tail = node.prev

	def clear(self):
		self.head = self.tail = None





class Signal (object):
	"Signal class"
	
	__slots__ = [ '__weakref__', '_connectionListener', '__doc__', '_connections', '_funcToConnection', '_unblockedConnections', '_numSlots' ]

	def __init__(self, doc=''):
		"Constructor"
		super( Signal, self ).__init__()
		self._connectionListener = None
		#self.__doc__ = doc



	def _p_init(self):
		if not hasattr( self, '_connections' ):
			self._connections = []
			self._funcToConnection = {}
			self._unblockedConnections = _ConnectionList()
			self._numSlots = 0

	def _p_deinit(self):
		del self._connections
		del self._funcToConnection
		del self._unblockedConnections
		del self._numSlots



	def connect(self, func):
		"Connect the signal to the function @func"
		self._p_init()

		if self._funcToConnection.get( func ) is None:
			# Create the connection object
			connection = _ConnectionListNode( func )

			# Add to the connection list
			self._connections.append( connection )
			self._funcToConnection[func] = connection


			self._numSlots += 1
			self._unblockedConnections.append( connection )


			if len( self._connections ) == 1:
				# Had 0 connections before adding this one
				if self._connectionListener is not None:
					self._connectionListener( self )
				self._o_ref()



	def disconnect(self, func):
		"Disconnect the signal from the function @func"
		if hasattr( self, '_connections' ):
			# Get the connection
			connection = self._funcToConnection.get( func )

			if connection is not None:
				# Deactivate the connection, so it will be skipped by any emissions currently taking place
				connection.active = False;

				# Remove from the connection list
				self._connections.remove( connection )
				del self._funcToConnection[func]


				self._numSlots -= 1
				self._unblockedConnections.remove( connection )

				if len( self._connections ) == 0:
					if self._connectionListener is not None:
						self._connectionListener( self )
					self._p_deinit()
					self._o_unref()


	def disconnectAll(self):
		"Disconnect the signal from all slots"
		if hasattr( self, '_connections' ):
			bHadConnections = len( self._connections ) > 0

			if bHadConnections:
				for connection in self._connections:
					# Deactivate the connection
					connection.active = False

				self._connections = []
				self._funcToConnection = {}

				self._numSlots = 0


				if self._connectionListener is not None:
					self._connectionListener( self )
				self._o_unref()
				self._p_deinit()




	def hasConnections(self):
		"Determine if the signal has any outgoing connections"
		if hasattr( self, '_connections' ):
			return len( self._connections ) > 0
		else:
			return 0



	def setConnectionListener(self, listener):
		"Add a connection listener"
		if listener is not None  and  self._connectionListener is None:
			self._o_ref()
		elif listener is None  and  self._connectionListener is not None:
			self._o_unref()
		self._connectionListener = listener




	def emit(self, *args, **kwargs):
		"Emit"
		if hasattr( self, '_connections' ):
			connection = self._unblockedConnections.head
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



	def _o_ref(self):
		pass

	def _o_unref(self):
		pass




class _InstanceSignal (Signal):
	__slots__ = [ '_refCount', '_classSignal', '_instance' ]
	
	def __init__(self, classSignal, instance, doc=''):
		super( _InstanceSignal, self ).__init__( doc )
		self._refCount = 0
		self._classSignal = classSignal
		self._instance = instance

	def _o_ref(self):
		if self._refCount == 0:
			self._classSignal._instanceSignalSet.add( self )
		self._refCount += 1

	def _o_unref(self):
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



	def _p_slotBlocked(self, func):
		"Private - notify a signal that one of its connected slots has been blocked"
		pass


	def _p_slotUnblocked(self, func):
		"Private - notify a signal that one of its connected slots has been unblocked"
		pass


	def chainConnect(self, sig):
		"Chain connect to signal @sig"
		pass

	def chainDisconnect(self, sig):
		"Chain disconnect from signal @sig"
		pass






if __name__ == '__main__':
	s0 = Signal()


	def f0(x):
		print '>f0', x
		s0.disconnect( f1 )
		print '<f0', x

	def f1(x):
		print '>f1', x
		print '<f1', x

	def f2(x):
		print '>f2', x
		s0.disconnect( f2 )
		s0.emit(x+1)
		print '<f2', x

	def f3(x):
		print '>f3', x
		s0.connect( f4 )
		print '<f3', x

	def f4(x):
		print '>f4', x
		print '<f4', x


	s0.connect( f0 )
	s0.connect( f1 )
	s0.connect( f2 )
	s0.connect( f3 )

	s0.emit(0)


	print '=================='



	names = {}

	def makeFun(name):
		def f():
			print '> %s' % ( name, )
		names[f] = name
		print 'FUNCTION %s' % ( name, )
		return f

	def makeSignal(name):
		s = Signal()
		names[s] = name
		print 'SIGNAL %s' % ( name, )
		return s

	def emit(s):
		print '%s.emit' % ( names[s], )
		s.emit()

	def connect(s, f):
		print '%s -> %s' % ( names[s], names[f] )
		s.connect( f )

	def disconnect(s, f):
		print '%s <- %s' % ( names[s], names[f] )
		s.disconnect( f )

	def chainConnect(s, d):
		print '%s :=> %s' % ( names[s], names[d] )
		s.chainConnect( d )

	def chainDisconnect(s, d):
		print '%s <=: %s' % ( names[s], names[d] )
		s.chainDisconnect( d )

	def block(f):
		print '|| %s' % ( names[f], )
		blockSlot( f )

	def unblock(f):
		print '-- %s' % ( names[f], )
		unblockSlot( f )

	p = makeSignal( 'p' )
	q = makeSignal( 'q' )
	r = makeSignal( 'r' )

	f0 = makeFun( 'f0' )
	f1 = makeFun( 'f1' )
	f2 = makeFun( 'f2' )
	f3 = makeFun( 'f3' )
	f4 = makeFun( 'f4' )

	emit( p )
	connect( p, f0 )
	emit( p )
	connect( p, f1 )
	emit( p )
	connect( q, f2 )
	emit( q )
	connect( q, f3 )
	emit( q )
	chainConnect( p, q )
	chainConnect( q, r )
	connect( r, f4 )
	emit( p )
	disconnect( p, f0 )
	emit( p )
	disconnect( p, f1 )
	emit( p )
	connect( p, f0 )
	connect( p, f1 )
	emit( p )
	block( f0 )
	emit( p )
	block( f1 )
	emit( p )
	unblock( f0 )
	unblock( f1 )
	emit( p )
	block( f2 )
	emit( p )
	block( f3 )
	emit( p )
	unblock( f3 )
	emit( p )


	print '=================='


	class C (object):
		sig = Signal()


	def f0():
		print 'f0'

	def f1():
		print 'f1'

	def f2():
		print 'f2'

	def f3():
		print 'f3'


	a = C()
	b = C()
	c = C()
	d = C()

	a.sig.connect( f0 )
	b.sig.connect( f1 )
	c.sig.connect( f2 )
	d.sig.connect( f3 )


	print 'a.sig.emit()'
	a.sig.emit()
	print 'b.sig.emit()'
	b.sig.emit()
	print 'c.sig.emit()'
	c.sig.emit()
	print 'd.sig.emit()'
	d.sig.emit()


	print '=================='


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


	# Performance: 224k emissions per second, 215k with psyco
