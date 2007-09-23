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

from Britefury.DocModel.DMList import DMList
from Britefury.DocModel.DMVirtualList import DMVirtualList
from Britefury.DocModel.DMNull import DMNull

from Britefury.DocView.DVNode import *
from Britefury.DocView.DVBorderNode import DVBorderNode

from Britefury.DocViewBehavior.DVBListBehavior import DVBListBehavior
#from Britefury.DocViewBehavior.DVBCreateExpressionBehavior import DVBCreateExpressionBehavior

from Britefury.DocPresent.Toolkit.DTWrappedLineWithSeparators import DTWrappedLineWithSeparators
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel





class DVList (DVBorderNode):
	docNodeClass = DMList, DMVirtualList


#	behaviors = [ DVBListBehavior(), DVBCreateExpressionBehavior() ]
	behaviors = [ DVBListBehavior() ]



	@FunctionField
	def elementNodes(self):
		# Build a dictionary mapping document node to a list of view nodes; (there is nothing preventing a list from containing the same node more than once)
		# Access the list in reverse, so that the lists of view nodes can be popped in the correct order
		docChildToViewNodes = {}
		for viewNode in reversed( self._elementNodes ):
			try:
				viewNodes = docChildToViewNodes[viewNode.docNode]
			except KeyError:
				viewNodes = []
				docChildToViewNodes[viewNode.docNode] = viewNodes
			viewNodes.append( viewNode )

		# Build the new list of view nodes; reuse existing ones
		self._elementNodes = []
		for index, docChildNode in enumerate( self.docNode ):
			try:
				viewNodes = docChildToViewNodes[docChildNode]
				try:
					viewNode = viewNodes.pop()
				except IndexError:
					viewNode = self._view.buildView( docChildNode, self, index )
			except KeyError:
				viewNode = self._view.buildView( docChildNode, self, index )
			viewNode._f_setParentAndIndex( self, self.docNode, index )
			self._elementNodes.append( viewNode )

		return self._elementNodes




	@FunctionField
	def _refreshElements(self):
		self._elementsLine[:] = [ node.widget   for node in self.elementNodes ]


	def _o_refreshNode(self):
		super( DVList, self )._o_refreshNode()
		self._refreshElements




	def _o_styleSheetChanged(self):
		super( DVList, self )._o_styleSheetChanged()
		self._elementsLine = self._styleSheet.elementsContainer()
		self._elementsLine[:] = [ node.widget   for node in self._elementNodes ]
		self._box = self._styleSheet.overallContainer( self._elementsLine )
		self.widget.child = self._box





	def __init__(self, docNode, view, parentDocNode, indexInParent):
		super( DVList, self ).__init__( docNode, view, parentDocNode, indexInParent )
		self._elementNodes = []
		self._elementsLine = DTWrappedLineWithSeparators( spacing=5.0 )
		self._box = DTBox()
		self._box.append( DTLabel( '[', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		self._box.append( self._elementsLine )
		self._box.append( DTLabel( ']', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		self.widget.child = self._box



	def addElement(self):
		element = DMNull()
		self.docNode.append( element )
		self.refresh()
		elemDV = self.elementNodes[-1]
		elemDV.startEditing()


	def deleteChild(self, child, moveFocus):
		if len( self.elementNodes ) <= 1:
			self.makeCurrent()
		else:
			child._o_moveFocus( moveFocus )
		self.docNode.remove( child.docNode )



	def startEditing(self):
		self.widget.grabFocus()



	def horizontalNavigationList(self):
		return self.elementNodes




	def getInsertPosition(self, receivingNodePath):
		if len( receivingNodePath ) > 1:
			child = receivingNodePath[1]
			try:
				return self.elementNodes.index( child )
			except ValueError:
				return 0
		return 0


