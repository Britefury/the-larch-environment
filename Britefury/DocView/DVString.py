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

#from Britefury.DocViewBehavior.DVBCreateExpressionBehavior import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.Toolkit.DTParsedEntryLabel import DTParsedEntryLabel




class DVString (DVAtom):
	docNodeClass = DMString


	#behaviors = [ CVBWrapInAssignmentBehavior(), CVBUnboundRefBehavior() ]


	@FunctionRefField
	def targetNameWidget(self):
		entry = DTParsedEntryLabel( self._styleSheet.parser, text=self.docNode.name )
		entry.keyHandler = self
		self._o_listenToParsedEntryLabel( entry )
		return entry


	@FunctionField
	def _refreshTargetName(self):
		self.widget.child = self.targetNameWidget

	def _o_refreshNode(self):
		super( DVString, self )._o_refreshNode()
		self._refreshTargetName



	def startEditing(self):
		self.targetNameWidget.startEditing()


	def startEditingOnLeft(self):
		self.targetNameWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.targetNameWidget.startEditingOnRight()

	def startEditingAtPosition(self, pos):
		widgetPos = self.widget.getPointRelativeTo( self.targetNameWidget, pos )
		self.targetNameWidget.startEditingAtPositionX( widgetPos.x )

	def getCursorPosition(self):
		return self.targetNameWidget.getPointRelativeTo( self.widget, self.targetNameWidget.getCursorPosition() )



	def _p_onEntryFinish(self, entry, text, bUserEvent):
		self._f_commandHistoryFreeze()
		if bUserEvent:
			self.cursorRight()
		if text == '':
			self.deleteNode( MoveFocus.RIGHT )
		else:
			self._parentDocNode[self._indexInParent] = DMString( text )
		self._f_commandHistoryThaw()


