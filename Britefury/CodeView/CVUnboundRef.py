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

from Britefury.Math.Math import Colour3f

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTUnboundRef import CVTUnboundRef

from Britefury.CodeView.CVExpression import *
from Britefury.CodeView.MoveFocus import *

from Britefury.CodeViewBehavior.CVBWrapInAssignmentBehavior import *
from Britefury.CodeViewBehavior.CVBUnboundRefBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel




class CVUnboundRef (CVExpression):
	keywordToMethod =	\
	{
		'return' : CVTUnboundRef.replaceWithReturn,
		'if' : CVTUnboundRef.replaceWithIf,
		'while' : CVTUnboundRef.replaceWithWhile,
		'break' : CVTUnboundRef.replaceWithBreak,
		'continue' : CVTUnboundRef.replaceWithContinue,
		'def' : CVTUnboundRef.replaceWithDef,
		'class' : CVTUnboundRef.replaceWithClass,
		'import' : CVTUnboundRef.replaceWithImport,
	}



	treeNodeClass = CVTUnboundRef


	treeNode = SheetRefField( CVTUnboundRef )


	behaviors = [ CVBWrapInAssignmentBehavior(), CVBUnboundRefBehavior() ]


	@FunctionRefField
	def targetNameWidget(self):
		entry = DVCStringCellEditEntryLabel( regexp=RegExpStrings.identifier )
		entry.entry.textColour = Colour3f( 0.5, 0.0, 0.5 )
		entry.entry.highlightedTextColour = Colour3f( 1.0, 0.75, 1.0 )
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.targetName )
		entry.finishSignal.connect( self._p_onEntryFinish )
		return entry.entry


	@FunctionField
	def _refreshTargetName(self):
		self.widget.child = self.targetNameWidget

	@FunctionField
	def refreshCell(self):
		self._refreshTargetName



	def __init__(self, treeNode, view):
		super( CVUnboundRef, self ).__init__( treeNode, view )
		self._bReplaced = False



	def startEditing(self):
		self.targetNameWidget.startEditing()


	def startEditingOnLeft(self):
		self.targetNameWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.targetNameWidget.startEditingOnRight()


	def keywordCheck(self):
		text = self.targetNameWidget.text

		try:
			method = self.keywordToMethod[text]
		except KeyError:
			return False
		else:
			self._bReplaced = True
			self._f_commandHistoryFreeze()
			stmtCVT = method( self.treeNode )
			self.refreshFromParent()
			self._o_getViewNode( stmtCVT ).startEditing()
			self._f_commandHistoryThaw()
			return True




	def _p_onEntryFinish(self, entry, text, bUserEvent):
		if bUserEvent  and  not self._bReplaced:
			if self.keywordCheck():
				return
		self._f_commandHistoryFreeze()
		if bUserEvent:
			self.cursorRight()
		if text == '':
			self.deleteNode( MoveFocus.RIGHT )
		else:
			if not self._bReplaced:
				self._bReplaced = True
				self._replaceWithRef()
		self._f_commandHistoryThaw()


	def _replaceWithRef(self):
		refCVT = self.treeNode.replaceWithRef()
		if refCVT is not None:
			self._view.refresh()
			refCV = self._view.getViewNodeForTreeNode( refCVT )
			return refCV
		else:
			return None

