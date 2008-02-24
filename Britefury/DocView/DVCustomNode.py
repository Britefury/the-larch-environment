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

from Britefury.DocPresent.Toolkit.DTBin import DTBin

from Britefury.DocView.DVNode import DVNode



class DVCustomNode (DVNode):
	def _o_styleSheetChanged(self):
		# TODO
		pass

		
	def _o_refreshNode(self):
		super( DVCustomNode, self )._o_refreshNode()
		for cell in self._cellsToRefresh:
			cell.getImmutableValue()
		contents = self._contentsCell.getImmutableValue()
		contents.keyHandler = self
		self.widget.child = contents
		




	def __init__(self, docNode, view, docNodeKey, contentsFactory):
		super( DVCustomNode, self ).__init__( docNode, view, docNodeKey )
		self.widget = DTBin()
		
		self._contentsFactory = contentsFactory
		
		self._contentsCell = RefCell()
		self._contentsCell.function = self._p_computeContents
		
		self._cellsToRefresh = []
	
		
	def _p_computeContents(self):
		contents = self._contentsFactory( self, self._docNodeKey )
		return contents
	

	def _f_setRefreshCells(self, cells):
		self._cellsToRefresh = cells
		self._o_resetRefreshCell()
		


