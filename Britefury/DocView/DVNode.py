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

from Britefury.gSym.View.UnparsedText import UnparsedText



class DVNode (object):
	class _CouldNotFindNextChildError (Exception):
		pass


	def __init__(self, docNode, view, docNodeKey):
		super( DVNode, self ).__init__()
		self.docNode = docNode
		self._view = view
		self._parent = None
		self._docNodeKey = docNodeKey
		self.refreshCell = RefCell()
		self.refreshCell.function = self._o_refreshNode
		
		self._widget = DTBin()
		self._text = None
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
		
		
	def getDocNodeKey(self):
		return self._docNodeKey



	
	#
	# REFRESH METHODS
	#
	
	def _o_refreshNode(self):
		for cell in self._cellsToRefresh:
			cell.getImmutableValue()
		contents = self._contentsCell.getImmutableValue()
		if isinstance( contents, tuple ):
			widget, text = contents
			assert isinstance( widget, DTWidget )  or  isinstance( widget, DVNode )
			assert isinstance( text, UnparsedText )  or  isinstance( text, str )  or  isinstance( text, unicode )
			
			# If the contents is a DVNode, get its widget
			if isinstance( widget, DVNode ):
				widget = widget.widget
			
			self._widget.child = widget
			self._text = contents[1]
		else:
			# Contents is just a widget
			if isinstance( contents, DTWidget ):
				self._widget.child = contents
			elif isinstance( contents, DVNode ):
				self._widget.child = contents.widget
			else:
				raise TypeError, 'contents should be a DTWidget or a DVNode'
			self._text = None


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
	# CONTENT ACQUISITION METHODS
	#
	
	def getWidget(self):
		self.refresh()
		return self._widget
	
	def getText(self):
		self.refresh()
		return self._text
	
			
			
			
			
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
	widget = property( getWidget )
	text = property( getText )



