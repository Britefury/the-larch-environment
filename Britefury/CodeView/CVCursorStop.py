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

from Britefury.CodeView.CVNode import *

from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder



class CVCursorStop (CVNode):
	def __init__(self, view, parent):
		super( CVCursorStop, self ).__init__( None, view )
		self.widget = DTActiveBorder()
		self.widget.keyHandler = self
		self.widget.leftMargin = self.widget.rightMargin = 20.0
		self.widget.topMargin = self.widget.bottomMargin = 4.0
		self.widget.borderColour = None
		self.widget.prelitColour = Colour3f( 0.5, 0.5, 0.5 )
		self._parent = parent



