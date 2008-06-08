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

import traceback

from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel import KMeta

from Britefury.Cell.Cell import RefCell

from Britefury.DocView.DocView import DocView
from Britefury.DocView.DocViewEvent import DocViewEventKey, DocViewEventEmpty, DocViewEventToken

from Britefury.DocPresent.Toolkit.DTWidget import *
from Britefury.DocPresent.Toolkit.DTBorder import *
from Britefury.DocPresent.Toolkit.DTLabel import *




class DVNode (object):
	class _CouldNotFindNextChildError (Exception):
		pass


	def _o_refreshNode(self):
		for cell in self._cellsToRefresh:
			cell.getImmutableValue()
		contents = self._contentsCell.getImmutableValue()
		self.widget.child = contents


	def _o_reset(self):
		pass




	def __init__(self, docNode, view, docNodeKey):
		super( DVNode, self ).__init__()
		self.docNode = docNode
		self._view = view
		self._bDeleting = False
		self._parent = None
		self._docNodeKey = docNodeKey
		self.refreshCell = RefCell()
		self.refreshCell.function = self._o_refreshNode
		
		self.widget = DTBin()
		self._contentsFactory = None
		self._contentsCell = RefCell()
		self._contentsCell.function = self._p_computeContents
		self._cellsToRefresh = []
		self.focus = None
		


	#
	# DOCUMENT VIEW METHODS
	#
	def getDocView(self):
		return self._view


	
	
	#
	# NODE KEY METHODS
	#
	def _f_setParentAndKey(self, parent, docNodeKey):
		if parent is not self._parent  or  docNodeKey != self._docNodeKey:
			self._parent = parent
			oldKey = self._docNodeKey
			self._docNodeKey = docNodeKey
			self._view._f_nodeChangeKey( self, oldKey, docNodeKey )
			# Force refreshCell to require recomputation due to potential style sheet change
			# self.refreshCell.function = self._o_refreshNode
		self._o_reset()
		
		
	def getDocNodeKey(self):
		return self._docNodeKey



	
	#
	# REFRESH METHODS
	#
	
	def _o_resetRefreshCell(self):
		self.refreshCell.function = self._o_refreshNode


	def refresh(self):
		self.refreshCell.immutableValue


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
			
			
			
			
	#
	# NODE TREE METHODS
	#
	
	def getParentNodeView(self):
		return self._parent

	def isDescendantOf(self, node):
		n = self
		while n is not None:
			if n is node:
				return True
			n = n._parent
		return False




	def getChildViewNodeForChildDocNode(self, childDocNode):
		if childDocNode is not None:
			raise KeyError
		else:
			return None


	def isForDocNode(self, docNode):
		return docNode is self.docNode




	def _f_commandHistoryFreeze(self):
		self._view._f_commandHistoryFreeze()


	def _f_commandHistoryThaw(self):
		self._view._f_commandHistoryThaw()






	parentNodeView = property( getParentNodeView )
	docView = property( getDocView )
	docNodeKey = property( getDocNodeKey )
