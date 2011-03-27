##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************


def _onSetContents(commandHistory, ls, oldContents, newContents, description):
	if commandHistory is not None:
		def execute():
			ls._setContents( newContents )
		def unexecute():
			ls._setContents( oldContents )
		for x in oldContents:
			commandHistory.stopTracking( x )
		commandHistory.addCommand( execute, unexecute, description )
		for x in newContents:
			commandHistory.track( x )


def _onAppend(commandHistory, ls, x, listAttrName):
	if commandHistory is not None:
		commandHistory.addCommand( lambda: ls.append( x ), lambda: ls.__delitem__( -1 ), 'Tracked list \'%s\' append' % listAttrName )
		commandHistory.track( x )

def _onExtend(commandHistory, ls, xs, listAttrName):
	if commandHistory is not None:
		n = len( xs )
		def _del():
			del ls[-n:]
		commandHistory.addCommand( lambda: ls.extend( xs ), _del, 'Tracked list \'%s\' extend' % listAttrName )
		for x in xs:
			commandHistory.track( x )

def _onInsert(commandHistory, ls, i, x, listAttrName):
	if commandHistory is not None:
		commandHistory.addCommand( lambda: ls.insert( i, x ), lambda: ls.__delitem__( i ), 'Tracked list \'%s\' insert at %d' % ( listAttrName, i ) )
		commandHistory.track( x )

def _onPop(commandHistory, ls, x, listAttrName):
	if commandHistory is not None:
		commandHistory.addCommand( lambda: ls.pop(), lambda: ls.append( x ), 'Tracked list \'%s\' pop' % listAttrName )
		commandHistory.stopTracking( x )

def _onRemove(commandHistory, ls, i, x, listAttrName):
	if commandHistory is not None:
		commandHistory.addCommand( lambda: ls.__delitem__( i ), lambda: ls.insert( i, x ), 'Tracked list \'%s\' remove' % listAttrName )
		commandHistory.stopTracking( x )

def _onReverse(commandHistory, ls, listAttrName):
	if commandHistory is not None:
		commandHistory.addCommand( lambda: ls.reverse(), lambda: ls.reverse(), 'Tracked list \'%s\' reverse' % listAttrName )


class _TrackedListWrapper (object):
	__slots__ = [ '_instance', '_prop', '_ls' ]
	
	def __init__(self, instance, prop, ls):
		self._instance = instance
		self._prop = prop
		self._ls = ls
		
	def __iter__(self):
		return iter( self._ls )
	
	def __contains__(self, x):
		return x in self._ls
	
	def __add__(self, xs):
		return self._ls + xs
	
	def __mul__(self, x):
		return self._ls * x
	
	def __rmul__(self, x):
		return x * self._ls
	
	def __getitem__(self, index):
		return self._ls[index]
	
	def __len__(self):
		return len( self._ls )
	
	def index(self, x, i=None, j=None):
		if i is None:
			return self._ls.index( x )
		elif j is None:
			return self._ls.index( x, i )
		else:
			return self._ls.index( x, i, j )

	def count(self, x):
		return self._ls.count( x )
	
	def __setitem__(self, index, x):
		ch = self._prop._getCommandHistory( self._instance )
		oldContents = self._ls[:]
		self._ls[index] = x
		newContents = self._ls[:]
		_onSetContents( ch, self, oldContents, newContents, 'Tracked list \'%s\' set item' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def __delitem__(self, index):
		ch = self._prop._getCommandHistory( self._instance )
		oldContents = self._ls[:]
		del self._ls[index]
		newContents = self._ls[:]
		_onSetContents( ch, self, oldContents, newContents, 'Tracked list \'%s\' del item' % self._prop._attrName )
		self._prop._onChange( self._instance )
		
	def append(self, x):
		ch = self._prop._getCommandHistory( self._instance )
		self._ls.append( x )
		_onAppend( ch, self, x, self._prop._attrName )
		self._prop._onChange( self._instance )

	def extend(self, xs):
		ch = self._prop._getCommandHistory( self._instance )
		self._ls.extend( xs )
		_onExtend( ch, self, xs, self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def insert(self, i, x):
		ch = self._prop._getCommandHistory( self._instance )
		self._ls.insert( i, x )
		_onInsert( ch, self, i, x, self._prop._attrName )
		self._prop._onChange( self._instance )

	def pop(self):
		ch = self._prop._getCommandHistory( self._instance )
		x = self._ls.pop()
		_onPop( ch, self, x, self._prop._attrName )
		self._prop._onChange( self._instance )
		return x
		
	def remove(self, x):
		ch = self._prop._getCommandHistory( self._instance )
		i = self._ls.index( x )
		xFromList = self._ls[i]
		del self._ls[i]
		_onRemove( ch, self, i, xFromList, self._prop._attrName )
		self._prop._onChange( self._instance )
		
	def reverse(self):
		ch = self._prop._getCommandHistory( self._instance )
		self._ls.reverse()
		_onReverse( ch, self, self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def sort(self, cmp=None, key=None, reverse=None):
		ch = self._prop._getCommandHistory( self._instance )
		oldContents = self._ls[:]
		self._ls.sort( cmp, key, reverse )
		newContents = self._ls[:]
		_onSetContents( ch, self, oldContents, newContents, 'Tracked list \'%s\' sort' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def _setContents(self, xs):
		ch = self._prop._getCommandHistory( self._instance )
		oldContents = self._ls[:]
		self._ls[:] = xs
		_onSetContents( ch, self, oldContents, xs, 'Tracked list \'%s\' set contents' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	
	def trackContents(self, history):
		for x in self._ls:
			history.track( x )
	
	def stopTrackingContents(self, history):
		for x in self._ls:
			history.stopTracking( x )



class TrackedListProperty (object):
	def __init__(self, attrName, commandHistoryAttrName, onChangeMethod=None):
		self._attrName = intern( attrName )
		self._commandHistoryAttrName = intern( commandHistoryAttrName )
		self._onChangeMethod = onChangeMethod
	
		
	def __get__(self, instance, owner):
		if instance is None:
			return self
		else:
			return _TrackedListWrapper( instance, self, getattr( instance, self._attrName ) )
	
	def __set__(self, instance, value):
		setattr( instance, self._attrName, value )
	
	def __delete__(self, instance):
		delattr( instance, self._attrName )
	
	
	def _getCommandHistory(self, instance):
		return getattr( instance, self._commandHistoryAttrName )

	def _onChange(self, instance):
		if self._onChangeMethod is not None:
			self._onChangeMethod( instance )

	
	
	