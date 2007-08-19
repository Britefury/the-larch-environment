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

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTReturn import CVTReturn

from Britefury.CodeView.CVBorderNode import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection



class CVLabel (CVBorderNode):
	treeNode = SheetRefField( CVTNode )



	@FunctionField
	def refreshCell(self):
		pass


	def __init__(self, treeNode, view):
		super( CVLabel, self ).__init__( treeNode, view )
		self._label = DTLabel()
		self.widget.child = self._label
		self.widget.borderColour = None


	def setText(self, text):
		self._label.setText( text )

	def setMarkup(self, markup):
		self._label.setMarkup( markup )

	def setColour(self, colour):
		self._label.setColour( colour )

	def setFont(self, font):
		self._label.setFont( font )
