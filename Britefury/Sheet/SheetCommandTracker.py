##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import deepcopy

from Britefury.CommandHistory import CommandHistory
from Britefury.CommandHistory.CommandTracker import CommandTracker




class SheetCellEvaluatorCommand (CommandHistory.Command):
	def __init__(self, sheet, cell, oldEval, newEval):
		super( SheetCellEvaluatorCommand, self ).__init__()
		self._sheet = sheet
		self._valueTable = { cell : [ oldEval, newEval ] }


	def execute(self):
		for cell, ( oldEval, newEval )  in  self._valueTable.items():
			cell.setEvaluator( newEval )

	def unexecute(self):
		for cell, ( oldEval, newEval )  in  self._valueTable.items():
			cell.setEvaluator( oldEval )


	def canJoinWith(self, command):
		return isinstance( command, SheetCellEvaluatorCommand )  and  self._sheet is command._sheet  and  not self._bFinished

	def joinWith(self, command):
		for cell, ( oldEval, newEval )  in  command._valueTable.items():
			if self._valueTable.has_key( cell ):
				self._valueTable[cell][1] = newEval
			else:
				self._valueTable[cell] = [ oldEval, newEval ]






class SheetCommandTracker (CommandTracker):
	def __init__(self, commandHistory):
		super( SheetCommandTracker, self ).__init__( commandHistory )


	def track(self, sheet):
		super( SheetCommandTracker, self ).track( sheet )

		compositeCells = sheet._f_getCompositeCells()
		for cell in compositeCells:
			self._commandHistory.track( cell.getValue() )


	def stopTracking(self, sheet):
		compositeCells = sheet._f_getCompositeCells()
		for cell in compositeCells:
			self._commandHistory.stopTracking( cell.getValue() )

		super( SheetCommandTracker, self ).stopTracking( sheet )



	def _f_onSheetCellEvaluator(self, sheet, cell, oldEval, newEval):
		self._commandHistory.addCommand( SheetCellEvaluatorCommand( sheet, cell, oldEval, newEval ) )




