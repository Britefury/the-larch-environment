##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Util import RegExpStrings

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTLocalRef import CVTLocalRef

from Britefury.CodeView.CVExpression import *
from Britefury.CodeView.MoveFocus import *

from Britefury.CodeViewBehavior.CVBWrapInAssignmentBehavior import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTEntryLabel import DTEntryLabel




class CVLocalRef (CVExpression):
	treeNodeClass = CVTLocalRef


	treeNode = SheetRefField( CVTLocalRef )


	behaviors = [ CVBWrapInAssignmentBehavior() ]


	@FunctionRefField
	def nameWidget(self):
		self._entry.text = self.treeNode.varName
		return self._entry

	@FunctionField
	def refreshCell(self):
		self.nameWidget




	def __init__(self, treeNode, view):
		super( CVLocalRef, self ).__init__( treeNode, view )
		self._entry = DTEntryLabel( regexp=RegExpStrings.identifier )
		self._entry.keyHandler = self
		self._entry.finishEditingSignal.connect( self._p_onEntryFinish )
		self.widget.child = self._entry



	def startEditing(self):
		self.nameWidget.startEditing()


	def startEditingOnLeft(self):
		self.nameWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.nameWidget.startEditingOnRight()

	def startEditingAtPosition(self, pos):
		widgetPos = self.widget.getPointRelativeTo( self.nameWidget, pos )
		self.nameWidget.startEditingAtPositionX( widgetPos.x )

	def getCursorPosition(self):
		return self.nameWidget.getPointRelativeTo( self.widget, self.nameWidget.getCursorPosition() )


	def _p_onEntryFinish(self, entry, text, bUserEvent):
		self._f_commandHistoryFreeze()
		if bUserEvent:
			self.cursorRight()
		if text == '':
			self.deleteNode( MoveFocus.RIGHT )
		else:
			self._rebind( text )
		self._f_commandHistoryThaw()



	def _rebind(self, varName):
		refCVT = self.treeNode.rebind( varName )
		if refCVT is not None:
			self._view.refresh()
			refCV = self._view.getViewNodeForTreeNode( refCVT )
			return refCV
		else:
			return None
