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

from Britefury.Cell.Cell import RefCell

from Britefury.Sheet.Sheet import Sheet, SheetClass, FunctionRefField, FunctionField

from Britefury.DocView.DVNode import DVNode

from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder



class DVCustomNode (DVNode):
	def _o_styleSheetChanged(self):
		# TODO
		pass


		
		
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
					viewNode = self._view._f_buildView( docChildNode, self, index )
			except KeyError:
				viewNode = self._view._f_buildView( docChildNode, self, index )
			viewNode._f_setParentAndKey( self, DocNodeKey( docChildNode, self.docNode, index ) )
			self._elementNodes.append( viewNode )

		return self._elementNodes

	
	
	
	@FunctionField
	def _refreshElements(self):
		for node in self.elementNodes:
			node.refresh()
		self._elementsLine[:] = [ node.widget   for node in self.elementNodes ]


	def _o_refreshNode(self):
		super( DVList, self )._o_refreshNode()
		for cell in self._cellsToRefresh:
			cell.getImmutableValue()




	def _o_styleSheetChanged(self):
		super( DVList, self )._o_styleSheetChanged()
		self._elementsLine = self._styleSheet.elementsContainer()
		self._elementsLine[:] = [ node.widget   for node in self._elementNodes ]
		self._box = self._styleSheet.overallContainer( self._elementsLine )
		self.widget.child = self._box


	

	def __init__(self, docNode, view, docNodeKey, contentsFactory):
		super( DVBorderNode, self ).__init__( docNode, view, docNodeKey )
		self.widget = DTBin()
		
		self._elementNodes = []
		self._elementsLine = DTWrappedLineWithSeparators( spacing=5.0 )
		self._box = DTBox()
		self._box.append( DTLabel( '[', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		self._box.append( self._elementsLine )
		self._box.append( DTLabel( ']', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		self.widget.child = self._box
		
		self._contentsFactory = contentsFactory
		
		self._contentsCell = RefCell()
		self._contentsCell.function = self._p_computeContents
		
		self._cellsToRefresh = []
	
		
	def _p_computeContents(self):
		return self._contentsFactory( self, self._docNodeKey )
	

	def _f_setRefreshCells(self, cells):
		self._cellsToRefresh = cells
		self._o_resetRefreshCell()
		


	def getChildViewNodeForChildDocNode(self, childDocNode):
		if childDocNode is not None:
			for node in self._elementNodes:
				if node.isForDocNode( childDocNode ):
					return node
			raise KeyError
		else:
			return None

		
	def _p_onContext(self, widget):
		if self._pieMenu is not None:
			# Get the default display, and the pointer position
			display = gtk.gdk.display_get_default()
			screen, x, y, mods = display.get_pointer()
			self._pieMenu.popup( x, y, True )


	def _o_createPieMenu(self):
		return None



	def _dndBeginCallback(self, dndSource, localpos, button, state):
		return self


	def _dndDragToCallback(self, dndSource, dndDest, localPos, button, state):
		return self