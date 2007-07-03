##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Util.SignalSlot import ClassSignal


class Command (object):
	def __init__(self):
		self._bFinished = False


	def execute(self):
		"Execute the command"
		assert False, 'abstract'

	def unexecute(self):
		"Unexecute the command"
		assert False, 'abstract'


	def canJoinWith(self, command):
		"Determine if @command can be joined with @self"
		return False

	def joinWith(self, command):
		"Join @command with @self"
		assert False, 'abstract'




class CommandHistory (object):
	changedSignal = ClassSignal()


	def __init__(self):
		self._past = []
		self._future = []
		self._bCommandsBlocked = False
		self._bFrozen = False
		self._freezeCount = 0
		self._trackers = {}
		self._trackedObjects = set()


	def addCommand(self, command):
		"Add a command to the history"
		assert isinstance( command, Command ), 'command is not an instance of UndoStackAction'
		if not self._bCommandsBlocked:
			self._future = []
			top = self.top()
			# Attempt to join @command with @top
			if top is not None  and  top.canJoinWith( command ):
				top.joinWith( command )
			else:
				if self._bFrozen:
					self._past[-1].append( command )
				else:
					self._past.append( command )
			self.changedSignal.emit( self )


	def undo(self):
		"Undo a command"
		if self._bFrozen:
			self.thaw()
		command = self._past.pop()
		if isinstance( command, list ):
			for c in reversed( command ):
				self._p_unexecuteCommand( c )
		else:
			self._p_unexecuteCommand( command )
		self._future.append( command )
		self.changedSignal.emit( self )

	def redo(self):
		"Redo a command"
		if self._bFrozen:
			self.thaw()
		command = self._future.pop()
		if isinstance( command, list ):
			for c in command:
				self._p_executeCommand( c )
		else:
			self._p_executeCommand( command )
		self._past.append( command )
		self.changedSignal.emit( self )


	def clear(self):
		"Clear the command history"
		self._past = []
		self._future = []
		self.changedSignal.emit( self )



	def freeze(self):
		"Freeze the command history"
		if not self._bFrozen:
			if self.top() != []:
				self._past.append( [] )
		self._bFrozen = True
		self._freezeCount += 1

	def freezeJoin(self):
		"Freeze the command history, joining onto previous command"
		if len( self._past ) > 0:
			if not isinstance( self._past[-1], list ):
				self._past[-1] = [ self._past[-1] ]
			self._bFrozen = True
			self._freezeCount += 1
		else:
			self.freeze()

	def thaw(self):
		"Thaw the command history"
		self._freezeCount -= 1
		if self._freezeCount == 0:
			if len( self._past[-1] ) == 0:
				self._past.pop()
			self._bFrozen = False

		self._freezeCount = max( self._freezeCount, 0 )



	def top(self):
		"Get the top command"
		if len( self._past ) > 0:
			top = self._past[-1]
			if isinstance( top, list ):
				if len( top ) > 0:
					return top[-1]
				else:
					return None
			else:
				return top
		else:
			return None


	def finishCommand(self):
		"Disable command joining"
		top = self.top()
		if top is not None:
			top._bFinished = True




	def canUndo(self):
		return len( self._past )  >  0

	def canRedo(self):
		return len( self._future )  >  0

	def getNumUndoCommands(self):
		"Get the number of actions that can be undone"
		return len( self._past )

	def getNumRedoCommands(self):
		"Get the number of actions that can be redone"
		return len( self._future )




	def track(self, obj):
		try:
			trackerClass = obj.trackerClass
		except AttributeError:
			raise TypeError, 'object \'%s\' is not trackable; \'trackerClass\' attribute not found' % ( obj, )

		if trackerClass is None:
			raise TypeError, 'trackable object \'%s\' has no tracker class' % ( obj, )
		if not isinstance( trackerClass, type ):
			raise TypeError, 'tracker class must be a type, it is \'%s\'' % ( trackerClass, )

		if obj not in self._trackedObjects:
			try:
				tracker = self._trackers[obj.trackerClass]
			except KeyError:
				tracker = obj.trackerClass( self )
				self._trackers[obj.trackerClass] = tracker

			self._trackedObjects.add( obj )
			tracker.track( obj )


	def stopTracking(self, obj):
		try:
			trackerClass = obj.trackerClass
		except AttributeError:
			raise TypeError, 'object \'%s\' is not trackable; \'trackerClass\' attribute not found' % ( obj, )

		if trackerClass is None:
			raise TypeError, 'trackable object \'%s\' has no tracker class' % ( obj, )
		if not isinstance( trackerClass, type ):
			raise TypeError, 'tracker class must be a type, it is \'%s\'' % ( trackerClass, )

		if obj in self._trackedObjects:
			try:
				tracker = self._trackers[obj.trackerClass]
			except KeyError:
				assert False, 'no tracker'

			self._trackedObjects.remove( obj )
			tracker.stopTracking( obj )



	def _p_blockCommands(self):
		self._bCommandsBlocked = True

	def _p_unblockCommands(self):
		self._bCommandsBlocked = False



	def _p_executeCommand(self, command):
		self._p_blockCommands()
		command.execute()
		self._p_unblockCommands()

	def _p_unexecuteCommand(self, command):
		self._p_blockCommands()
		command.unexecute()
		self._p_unblockCommands()







# *************************** UNIT TESTING ***************************

if __name__ == '__main__':
	import unittest

	class TestCommand (Command):
		def __init__(self, target, value):
			super( TestCommand, self ).__init__()
			self._target = target
			self._value = value
			self._oldValue = None

		def execute(self):
			self._oldValue = self._target[0]
			self._target[0] = self._value

		def unexecute(self):
			self._target[0] = self._oldValue


	class CommandHistoryTest (unittest.TestCase):
		def testStack(self):
			target = [ 0 ]
			stack = CommandHistory()

			self.assertEqual( target[0], 0 )
			self.assertEqual( stack.getNumUndoCommands(), 0 )
			self.assertEqual( stack.getNumRedoCommands(), 0 )

			stack.addCommandAndExecute( TestCommand( target, 1 ) )
			self.assertEqual( target[0], 1 )
			self.assertEqual( stack.getNumUndoCommands(), 1 )
			self.assertEqual( stack.getNumRedoCommands(), 0 )

			stack.addCommandAndExecute( TestCommand( target, 2 ) )
			self.assertEqual( target[0], 2 )
			self.assertEqual( stack.getNumUndoCommands(), 2 )
			self.assertEqual( stack.getNumRedoCommands(), 0 )

			stack.addCommandAndExecute( TestCommand( target, 3 ) )
			self.assertEqual( target[0], 3 )
			self.assertEqual( stack.getNumUndoCommands(), 3 )
			self.assertEqual( stack.getNumRedoCommands(), 0 )

			stack.addCommandAndExecute( TestCommand( target, 4 ) )
			self.assertEqual( target[0], 4 )
			self.assertEqual( stack.getNumUndoCommands(), 4 )
			self.assertEqual( stack.getNumRedoCommands(), 0 )

			stack.undo()
			self.assertEqual( target[0], 3 )
			self.assertEqual( stack.getNumUndoCommands(), 3 )
			self.assertEqual( stack.getNumRedoCommands(), 1 )

			stack.undo()
			self.assertEqual( target[0], 2 )
			self.assertEqual( stack.getNumUndoCommands(), 2 )
			self.assertEqual( stack.getNumRedoCommands(), 2 )

			stack.redo()
			self.assertEqual( target[0], 3 )
			self.assertEqual( stack.getNumUndoCommands(), 3 )
			self.assertEqual( stack.getNumRedoCommands(), 1 )

			stack.addCommandAndExecute( TestCommand( target, -1 ) )
			self.assertEqual( target[0], -1 )
			self.assertEqual( stack.getNumUndoCommands(), 4 )
			self.assertEqual( stack.getNumRedoCommands(), 0 )

			stack.undo()
			self.assertEqual( target[0], 3 )
			self.assertEqual( stack.getNumUndoCommands(), 3 )
			self.assertEqual( stack.getNumRedoCommands(), 1 )

			stack.undo()
			self.assertEqual( target[0], 2 )
			self.assertEqual( stack.getNumUndoCommands(), 2 )
			self.assertEqual( stack.getNumRedoCommands(), 2 )



	unittest.main()






