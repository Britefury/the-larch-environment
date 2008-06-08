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

from Britefury.DocPresent.Toolkit.DTBin import DTBin

from Britefury.DocView.DVNode import DVNode



class DVCustomNode (DVNode):
	def _o_refreshNode(self):
		super( DVCustomNode, self )._o_refreshNode()
		for cell in self._cellsToRefresh:
			cell.getImmutableValue()
		contents = self._contentsCell.getImmutableValue()
		self.widget.child = contents
		




	def __init__(self, docNode, view, docNodeKey):
		super( DVCustomNode, self ).__init__( docNode, view, docNodeKey )
		self.widget = DTBin()
		
		self._contentsFactory = None
		
		self._contentsCell = RefCell()
		self._contentsCell.function = self._p_computeContents
		
		self._cellsToRefresh = []
		
		self.focus = None
		
	
		
	def _p_computeContents(self):
		if self._contentsFactory is not None:
			return self._contentsFactory( self, self._docNodeKey )
		else:
			return None
	

	def _f_setRefreshCells(self, cells):
		self._cellsToRefresh = cells
		self._o_resetRefreshCell()
		
		
	def _f_setContentsFactory(self, contentsFactory):
		if contentsFactory is not self._contentsFactory:
			self._contentsFactory = contentsFactory
			self._contentsCell.function = self._p_computeContents
			
		


