##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.SequentialEditor import SequentialEditor, SelectionEditTreeEvent


class WorksheetSelectionEditTreeEvent (SelectionEditTreeEvent):
	pass


class WorksheetSequentialEditor (SequentialEditor):
	def getSelectionEditTreeEventClass(self):
		return WorksheetSelectionEditTreeEvent
	
	def createSelectionEditTreeEvent(self, sourceElement):
		return WorksheetSelectionEditTreeEvent( self, sourceElement )

	
	def copyStructuralValue(self, x):
		return x.deepCopy()


WorksheetSequentialEditor.instance = WorksheetSequentialEditor()