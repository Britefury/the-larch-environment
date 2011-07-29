##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from Britefury.ChangeHistory import ChangeHistory
from Britefury.ChangeHistory import CommandTracker




class DMListAppendCommand (ChangeHistory.Command):
	def __init__(self, ls, x):
		super( DMListAppendCommand, self ).__init__()
		self._ls = ls
		self._x = x


	def execute(self):
		self._ls.append( self._x )

	def unexecute(self):
		del self._ls[-1]




class DMListExtendCommand (ChangeHistory.Command):
	def __init__(self, ls, xs):
		super( DMListExtendCommand, self ).__init__()
		self._ls = ls
		self._xs = xs
		self._size = len( xs )


	def execute(self):
		self._ls.extend( self._xs )

	def unexecute(self):
		del self._ls[-self._size:]




class DMListInsertCommand (ChangeHistory.Command):
	def __init__(self, ls, index, x):
		super( DMListInsertCommand, self ).__init__()
		self._ls = ls
		self._index = index
		self._x = x


	def execute(self):
		self._ls.insert( self._index, self._x )

	def unexecute(self):
		del self._ls[self._index]




class DMListRemoveCommand (ChangeHistory.Command):
	def __init__(self, ls, x):
		super( DMListRemoveCommand, self ).__init__()
		self._ls = ls
		self._x = x
		self._index = ls.index( x )


	def execute(self):
		self._ls.remove( self._x )

	def unexecute(self):
		self._ls.insert( self._index, self._x )




class DMListSetCommand (ChangeHistory.Command):
	def __init__(self, ls, oldContents, contents):
		super( DMListSetCommand, self ).__init__()
		self._ls = ls
		self._oldContents = oldContents
		self._contents = contents


	def execute(self):
		self._ls[:] = self._contents

	def unexecute(self):
		self._ls[:] = self._oldContents



	def canJoinWith(self, command):
		"""Determine if @command can be joined with @self"""
		return isinstance( command, DMListSetCommand )  and  self._ls is command._ls  and  not self._bFinished

	def joinWith(self, command):
		"""Join @command with @self, where @command took place after @self, and the result should go into @self"""
		self._contents = command._contents





class DMListCommandTracker (CommandTracker.CommandTracker):
	def track(self, ls):
		super( DMListCommandTracker, self ).track( ls )

		for x in ls:
			if self._changeHistory.canTrack( x ):
				self._changeHistory.track( x )


	def stopTracking(self, ls):
		for x in ls:
			if self._changeHistory.canTrack( x ):
				self._changeHistory.stopTracking( x )

		super( DMListCommandTracker, self ).stopTracking( ls )




	def _f_onAppended(self, ls, x):
		if self._changeHistory.canTrack( x ):
			self._changeHistory.track( x )
		self._changeHistory.addChange( DMListAppendCommand( ls, x ) )


	def _f_onExtended(self, ls, xs):
		self._changeHistory.addChange( DMListExtendCommand( ls, xs ) )
		for x in xs:
			if self._changeHistory.canTrack( x ):
				self._changeHistory.track( x )


	def _f_onInserted(self, ls, index, x):
		if self._changeHistory.canTrack( x ):
			self._changeHistory.track( x )
		self._changeHistory.addChange( DMListInsertCommand( ls, index, x ) )


	def _f_onRemove(self, ls, x):
		self._changeHistory.addChange( DMListRemoveCommand( ls, x ) )
		if self._changeHistory.canTrack( x ):
			self._changeHistory.stopTracking( x )


	def _f_onSet(self, ls, oldContents, contents):
		oldContentsSet = set( oldContents )
		contentsSet = set( contents )
		for x in oldContentsSet - contentsSet:
			if self._changeHistory.canTrack( x ):
				self._changeHistory.stopTracking( x )
		self._changeHistory.addChange( DMListSetCommand( ls, oldContents, contents ) )
		for x in contentsSet - oldContentsSet:
			if self._changeHistory.canTrack( x ):
				self._changeHistory.track( x )


