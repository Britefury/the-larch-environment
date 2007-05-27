##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CommandHistory import CommandHistory
from Britefury.CommandHistory import CommandTracker

from Britefury.Sheet.SheetCommandTracker import SheetCommandTracker
import SheetGraphViewDisplayTable







class SheetGraphViewDisplayTableSetCommand (CommandHistory.Command):
	def __init__(self, table, node, oldPosition, newPosition):
		super( SheetGraphViewDisplayTableSetCommand, self ).__init__()
		self._table = table
		self._node = node
		self._oldPosition = oldPosition
		self._newPosition = newPosition


	def execute(self):
		self._table[self._node] = self._newPosition

	def unexecute(self):
		self._table[self._node] = self._oldPosition


	def canJoinWith(self, command):
		return isinstance( command, SheetGraphViewDisplayTableSetCommand )  and  self._table is command._table  and  self._node is command._node  and  not self._bFinished

	def joinWith(self, command):
		self._newPosition = command._newPosition






class SheetGraphViewDisplayTableCommandTracker (CommandTracker.CommandTracker):
	def track(self, table):
		super( SheetGraphViewDisplayTableCommandTracker, self ).track( table )
		assert isinstance( table, SheetGraphViewDisplayTable.SheetGraphViewDisplayTable )


	def stopTracking(self, table):
		assert isinstance( table, SheetGraphViewDisplayTable.SheetGraphViewDisplayTable )
		super( SheetGraphViewDisplayTableCommandTracker, self ).stopTracking( table )



	def _f_onPosSet(self, table, node, oldPosition, newPosition):
		self._commandHistory.addCommand( SheetGraphViewDisplayTableSetCommand( table, node, oldPosition, newPosition ) )






