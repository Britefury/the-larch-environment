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
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTDefDoc import CVTDefDoc

from Britefury.CodeView.CVNode import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel



class CVDefDoc (CVNode):
	treeNodeClass = CVTDefDoc


	treeNode = SheetRefField( CVTDefDoc )



	@FunctionRefField
	def nameWidget(self):
		return DTLabel( self.treeNode.functionName, font='Sans bold 11' )


	@FunctionRefField
	def paramsNode(self):
		return self._view.buildView( self.treeNode.paramsDocNode, self )

	@FunctionRefField
	def paramsWidget(self):
		return self.paramsNode.widget


	@FunctionRefField
	def functionDocWidget(self):
		entry = DPCStringCellEditEntryLabel()
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.functionDoc )
		return entry.entry




	@FunctionField
	def _refreshName(self):
		self._functionBox[1] = ( self.nameWidget, False, False, True, DTBox.ALIGN_EXPAND, 0.0 )

	@FunctionField
	def _refreshParams(self):
		self.widget[1] = self.paramsWidget

	@FunctionField
	def _refreshFunctionDoc(self):
		self._functionDocBox[1] = ( self.functionDocWidget, True, True, True, DTBox.ALIGN_EXPAND, 0.0 )


	@FunctionField
	def refreshCell(self):
		self._refreshName
		self._refreshParams
		self._refreshFunctionDoc




	def __init__(self, treeNode, view):
		super( CVDefDoc, self ).__init__( treeNode, view )
		self._functionBox = DTBox( spacing=25.0 )
		self._functionBox.append( DTLabel( markup=_( 'F<span size="small">UNCTION</span>' ), font='Sans 11' ), False, False )
		self._functionBox.append( DTLabel( '<nil>' ), True, True )
		self._functionDocBox = DTBox( spacing=10.0 )
		self._functionDocBox.append( DTLabel( 'DESCRIPTION:' ) )
		self._functionDocBox.append( DTLabel( '<nil>' ), True, True )
		self.widget = DTBox( direction=DTDirection.TOP_TO_BOTTOM, spacing=10.0, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self.widget.append( self._functionBox )
		self.widget.append( DTLabel( '<nil>' ) )
		self.widget.append( self._functionDocBox )
		self.widget.backgroundColour = Colour3f( 0.9, 0.9, 0.9 )



	def startEditing(self):
		self.paramsNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.paramsNode ]

	def verticalNavigationList(self):
		return [ self.paramsNode ]

