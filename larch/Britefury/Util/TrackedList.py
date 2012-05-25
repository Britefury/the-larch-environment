##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************


def onTrackedListSetContents(changeHistory, ls, oldContents, newContents, description):
	if changeHistory is not None:
		for x in oldContents:
			changeHistory.stopTracking( x )
		changeHistory.addChange( lambda: ls._setContents( newContents ), lambda: ls._setContents( oldContents ), description )
		for x in newContents:
			changeHistory.track( x )

def onTrackedListSetItem(changeHistory, ls, i, oldX, x, description):
	if changeHistory is not None:
		changeHistory.stopTracking( oldX )
		changeHistory.addChange( lambda: ls.__setitem__( i, x ), lambda: ls.__setitem__( i, oldX ), description )
		changeHistory.track( x )


def onTrackedListAppend(changeHistory, ls, x, description):
	if changeHistory is not None:
		changeHistory.addChange( lambda: ls.append( x ), lambda: ls.__delitem__( -1 ), description )
		changeHistory.track( x )

def onTrackedListExtend(changeHistory, ls, xs, description):
	if changeHistory is not None:
		n = len( xs )
		def _del():
			del ls[-n:]
		changeHistory.addChange( lambda: ls.extend( xs ), _del, description )
		for x in xs:
			changeHistory.track( x )

def onTrackedListInsert(changeHistory, ls, i, x, description):
	if changeHistory is not None:
		changeHistory.addChange( lambda: ls.insert( i, x ), lambda: ls.__delitem__( i ), description )
		changeHistory.track( x )

def onTrackedListPop(changeHistory, ls, x, description):
	if changeHistory is not None:
		changeHistory.addChange( lambda: ls.pop(), lambda: ls.append( x ), description )
		changeHistory.stopTracking( x )

def onTrackedListRemove(changeHistory, ls, i, x, description):
	if changeHistory is not None:
		changeHistory.addChange( lambda: ls.__delitem__( i ), lambda: ls.insert( i, x ), description )
		changeHistory.stopTracking( x )

def onTrackedListReverse(changeHistory, ls, description):
	if changeHistory is not None:
		changeHistory.addChange( lambda: ls.reverse(), lambda: ls.reverse(), description )


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
		ch = self._prop._getChangeHistory( self._instance )
		if isinstance( index, int )  or  isinstance( index, long ):
			oldX = self._ls[index]
			self._ls[index] = x
			onTrackedListSetItem( ch, self, index, oldX, x, 'Tracked list \'%s\' set item' % self._prop._attrName )
		else:
			oldContents = self._ls[:]
			self._ls[index] = x
			newContents = self._ls[:]
			onTrackedListSetContents( ch, self, oldContents, newContents, 'Tracked list \'%s\' set item' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def __delitem__(self, index):
		ch = self._prop._getChangeHistory( self._instance )
		oldContents = self._ls[:]
		del self._ls[index]
		newContents = self._ls[:]
		onTrackedListSetContents( ch, self, oldContents, newContents, 'Tracked list \'%s\' del item' % self._prop._attrName )
		self._prop._onChange( self._instance )
		
	def append(self, x):
		ch = self._prop._getChangeHistory( self._instance )
		self._ls.append( x )
		onTrackedListAppend( ch, self, x, 'Tracked list \'%s\' append' % self._prop._attrName )
		self._prop._onChange( self._instance )

	def extend(self, xs):
		ch = self._prop._getChangeHistory( self._instance )
		self._ls.extend( xs )
		onTrackedListExtend( ch, self, xs, 'Tracked list \'%s\' extend' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def insert(self, i, x):
		ch = self._prop._getChangeHistory( self._instance )
		self._ls.insert( i, x )
		onTrackedListInsert( ch, self, i, x, 'Tracked list \'%s\' insert' % self._prop._attrName )
		self._prop._onChange( self._instance )

	def pop(self):
		ch = self._prop._getChangeHistory( self._instance )
		x = self._ls.pop()
		onTrackedListPop( ch, self, x, 'Tracked list \'%s\' pop' % self._prop._attrName )
		self._prop._onChange( self._instance )
		return x
		
	def remove(self, x):
		ch = self._prop._getChangeHistory( self._instance )
		i = self._ls.index( x )
		xFromList = self._ls[i]
		del self._ls[i]
		onTrackedListRemove( ch, self, i, xFromList, 'Tracked list \'%s\' remove' % self._prop._attrName )
		self._prop._onChange( self._instance )
		
	def reverse(self):
		ch = self._prop._getChangeHistory( self._instance )
		self._ls.reverse()
		onTrackedListReverse( ch, self, 'Tracked list \'%s\' reverse' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def sort(self, cmp=None, key=None, reverse=None):
		ch = self._prop._getChangeHistory( self._instance )
		oldContents = self._ls[:]
		self._ls.sort( cmp, key, reverse )
		newContents = self._ls[:]
		onTrackedListSetContents( ch, self, oldContents, newContents, 'Tracked list \'%s\' sort' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	def _setContents(self, xs):
		ch = self._prop._getChangeHistory( self._instance )
		oldContents = self._ls[:]
		self._ls[:] = xs
		onTrackedListSetContents( ch, self, oldContents, xs, 'Tracked list \'%s\' set contents' % self._prop._attrName )
		self._prop._onChange( self._instance )
	
	
	def __get_trackable_contents__(self):
		return self._ls



class TrackedListProperty (object):
	def __init__(self, attrName, changeHistoryAttrName='__change_history__', onChangeMethod=None):
		self._attrName = intern( attrName )
		self._changeHistoryAttrName = intern( changeHistoryAttrName )
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
	
	
	def _getChangeHistory(self, instance):
		return getattr( instance, self._changeHistoryAttrName )

	def _onChange(self, instance):
		if self._onChangeMethod is not None:
			self._onChangeMethod( instance )

	
	
	