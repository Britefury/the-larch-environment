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


from Britefury.Math.Math import Colour3f

from Britefury.Sheet.Sheet import *

from Britefury.DocModel.DMAtom import DMAtom

from Britefury.DocView.DVBorderNode import *



class DVAtom (DVBorderNode):
	docNodeClass = DMAtom



	def _p_onAtomEntryTextModified(self, entry, text, parseResult):
		if parseResult is not None:
			xs = parseResult.asList()
			if len( xs ) > 1:
				self._styleSheet._f_handleTokenList( self, xs, self._parentDocNode, self._indexInParent )

	def _p_onAtomEntryFinishEditing(self, entry, text, parseResult, bUserEvent):
		self._styleSheet._f_handleText( self, text, self._parentDocNode, self._indexInParent, bUserEvent )



	def _o_listenToParsedEntryLabel(self, entry):
		entry.textModifiedSignal.connect( self._p_onAtomEntryTextModified )
		entry.finishEditingSignal.connect( self._p_onAtomEntryFinishEditing )

