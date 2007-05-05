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

from Britefury.CodeViewTree.CVTLocalAssignment import CVTLocalAssignment

from Britefury.CodeView.CVBorderNode import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVLocalAssignment (CVBorderNode):
	treeNodeClass = CVTLocalAssignment


	treeNode = SheetRefField( CVTLocalAssignment )


	def _nameWidget(self):
		return DTLabel( self.treeNode.varName )

	def _valueWidget(self):
		return self._view.buildView( self.treeNode.valueNode, self ).widget

	def _refreshName(self):
		self._box[0] = self.nameWidget

	def _refreshValue(self):
		self._box[2] = self.valueWidget

	def _refreshCell(self):
		self.refreshName
		self.refreshValue

	nameWidget = FunctionField( _nameWidget )
	valueWidget = FunctionField( _valueWidget )
	refreshName = FunctionField( _refreshName )
	refreshValue = FunctionField( _refreshValue )
	refreshCell = FunctionField( _refreshCell )




	def __init__(self, treeNode, view):
		super( CVLocalAssignment, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=10.0 )
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( DTLabel( '=' ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box
