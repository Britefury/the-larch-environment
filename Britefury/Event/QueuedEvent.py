##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gobject


from Britefury.Util.SignalSlot import Signal



class _EventQueue (object):
	def __init__(self):
		self._queuedEvents = set()
		self._queuedEventIdleHandle = None


	def queueEvent(self, func):
		if len( self._queuedEvents ) == 0:
			self._queuedEventAddIdle()
		self._queuedEvents.add( func )


	def dequeueEvent(self, func):
		self._queuedEvents.discard( func )
		if len( self._queuedEvents ) == 0:
			self._queuedEventRemoveIdle()



	def _queuedEventAddIdle(self):
		self._queuedEventIdleHandle = gobject.idle_add( self._queuedEventIdleFunction, priority=gobject.PRIORITY_HIGH_IDLE )

	def _queuedEventRemoveIdle(self):
		if self._queuedEventIdleHandle is not None:
			gobject.source_remove( self._queuedEventIdleHandle )
			self._queuedEventIdleHandle = None


	def _queuedEventIdleFunction(self):
		while len( self._queuedEvents ) > 0:
			eventsToRun = self._queuedEvents
			self._queuedEvents = set()
			for event in eventsToRun:
				event()
		self._queuedEventIdleHandle = None
		return False



_normalEventQueue = _EventQueue()

def queueEvent(func):
	_normalEventQueue.queueEvent( func )


def dequeueEvent(func):
	_normalEventQueue.dequeueEvent( func )


