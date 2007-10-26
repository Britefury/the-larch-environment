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
from Britefury.DocPresent.Toolkit.DTTokenisedEntryLabel import DTTokenisedEntryLabel

from Britefury.DocView.DVBorderNode import DVBorderNode




class DVString (DVBorderNode):
	docNodeClass = str


	#behaviors = [ CVBWrapInAssignmentBehavior(), CVBUnboundRefBehavior() ]


	@FunctionRefField
	def targetNameWidget(self):
		entry = DTTokenisedEntryLabel( self._styleSheet.tokeniser, text=self.docNode )
		entry.keyHandler = self
		self._o_listenToTokenisedEntryLabel( entry )
		return entry


	@FunctionField
	def _refreshTargetName(self):
		self.widget.child = self.targetNameWidget

	def _o_refreshNode(self):
		super( DVString, self )._o_refreshNode()
		self._refreshTargetName


	def _o_reset(self):
		super( DVString, self )._o_reset()
		self.targetNameWidget.text = self.docNode



	def __init__(self, docNode, view, docNodeKey):
		super( DVString, self ).__init__( docNode, view, docNodeKey )
		self._bHandlingModifiedEvent = False



	def isForDocNode(self, docNode):
		return docNode == self.docNode



	def startEditing(self):
		self.targetNameWidget.startEditing()


	def startEditingOnLeft(self):
		self.targetNameWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.targetNameWidget.startEditingOnRight()

	def startEditingAtPosition(self, pos):
		widgetPos = self.widget.getPointRelativeTo( self.targetNameWidget, pos )
		self.targetNameWidget.startEditingAtPositionX( widgetPos.x )



	def finishEditing(self):
		super( DVString, self ).finishEditing()
		self.targetNameWidget.finishEditing()

	def getCursorPosition(self):
		return self.targetNameWidget.getPointRelativeTo( self.widget, self.targetNameWidget.getCursorPosition() )



	def _p_onTokenisedEntryTextModified(self, entry, text, tokens):
		if tokens is not None:
			if len( tokens ) > 1  and  entry.isCursorAtEnd():
				self._bHandlingModifiedEvent = True
				self._f_handleTokenList( tokens, self._parent._styleSheet, False )
				self._bHandlingModifiedEvent = False

	def _p_onTokenisedEntryFinishEditing(self, entry, text, tokens, bUserEvent):
		if not self._bHandlingModifiedEvent:
			if tokens is not None:
				self._f_handleTokenList( tokens, self._parent._styleSheet, bUserEvent )



	def _o_listenToTokenisedEntryLabel(self, entry):
		entry.textModifiedSignal.connect( self._p_onTokenisedEntryTextModified )
		entry.finishEditingSignal.connect( self._p_onTokenisedEntryFinishEditing )



	def _f_handleTokenList(self, tokens, parentStyleSheet, bDirectEvent):
		self._view._f_handleTokenList( self, self._docNodeKey, tokens, parentStyleSheet, bDirectEvent )

