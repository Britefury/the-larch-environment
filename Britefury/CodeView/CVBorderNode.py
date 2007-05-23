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

from Britefury.CodeViewBehavior.CVBMovementBehavior import *

from Britefury.CodeView.CVNode import *

from Britefury.DocView.Toolkit.DTActiveBorder import DTActiveBorder



class CVBorderNode (CVNode):
	behaviors = [ CVBMovementBehavior() ]


	def __init__(self, treeNode, view):
		super( CVBorderNode, self ).__init__( treeNode, view )
		self.widget = DTActiveBorder()
		self.widget.keyHandler = self



	def getChildToLeft(self, child):
		return None

	def getChildToRight(self, child):
		return None


	def moveLeft(self, fromChild):
		child = self.getChildToLeft( fromChild )
		if child is not None:
			child.makeCurrent()
			return True
		else:
			return False

	def moveRight(self, fromChild):
		child = self.getChildToRight( fromChild )
		if child is not None:
			child.makeCurrent()
			return True
		else:
			return False
