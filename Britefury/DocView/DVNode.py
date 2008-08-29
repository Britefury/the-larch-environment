##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import traceback

from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *


from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel import KMeta

from Britefury.Cell.Cell import RefCell

from Britefury.DocView.DocView import DocView

from Britefury.gSym.View.UnparsedText import UnparsedText



#_defaultBinStyleSheet = ContainerStyleSheet()
_defaultParagraphStyleSheet = ParagraphStyleSheet()


_dbg_computeContentsStack = []


class DVNode (object):
	class _CouldNotFindNextChildError (Exception):
		pass


	def __init__(self, view, treeNode):
		super( DVNode, self ).__init__()
		self.treeNode = treeNode
		self.docNode = treeNode.node
		self._view = view
		self._parent = None
		self.refreshCell = RefCell()
		self.refreshCell.function = self._o_refreshNode
		
		#self._element = BinElement( _defaultBinStyleSheet )
		self._element = ParagraphElement( _defaultParagraphStyleSheet )
		self._metadata = None
		self._contentsFactory = None
		self._contentsCell = RefCell()
		self._contentsCell.function = self._p_computeContents
		self._cellsToRefresh = []
		self.focus = None
		
		self._children = set()
		
		

		
		
	def _changeTreeNode(self, treeNode):
		assert treeNode.node is self.docNode, 'DVNode._changeTreeNode(): doc-node must remain the same'
		self.treeNode = treeNode
		
		

	#
	# DOCUMENT VIEW METHODS
	#
	def getDocView(self):
		return self._view


	
	
	
	#
	# REFRESH METHODS
	#
	
	def _o_refreshNode(self):
		if len( _dbg_computeContentsStack ) == 1:
			print '###'
			print '###'
			print '###'
			print '###'
			print 'FOUND'
			traceback.print_stack()
			print '...'
			print '...'
			print '...'
			print '...'
		contents = self._contentsCell.getImmutableValue()
		for cell in self._cellsToRefresh:
			cell.getImmutableValue()
		if isinstance( contents, tuple ):
			element, metadata = contents
		else:
			element = contents
			metadata = None
			
		assert isinstance( element, Element )  or  isinstance( element, DVNode )
		
		# If the contents is a DVNode, get its widget
		if isinstance( element, DVNode ):
			element = element.getElement()
		elif isinstance( element, Element ):
			pass
		else:
			raise TypeError, 'contents should be an Element or a DVNode'
		
		#self._element.setChild( element )
		self._element.setChildren( [ element ] )
		self._metadata = metadata


		
	def _o_resetRefreshCell(self):
		self.refreshCell.function = self._o_refreshNode


	def refresh(self):
		self.refreshCell.getImmutableValue()


	def _p_computeContents(self):
		_dbg_computeContentsStack.append( self )
		for child in self._children:
			self._view._nodeTable.unrefViewNode( child )
		self._children = set()
		if self._contentsFactory is not None:
			contents = self._contentsFactory( self, self.treeNode )
			for child in self._children:
				self._view._nodeTable.refViewNode( child )
			_dbg_computeContentsStack.pop()
			return contents
		else:
			_dbg_computeContentsStack.pop()
			return None
	

	def _f_setRefreshCells(self, cells):
		self._cellsToRefresh = cells
		
	def _registerChild(self, child):
		self._children.add( child )
		
		
	def _f_setContentsFactory(self, contentsFactory):
		if contentsFactory is not self._contentsFactory:
			self._contentsFactory = contentsFactory
			self._contentsCell.function = self._p_computeContents
			
			
			
	#
	# CONTENT ACQUISITION METHODS
	#
	
	def getElementNoRefresh(self):
		return self._element
	
	def getElement(self):
		self.refresh()
		return self._element
	
	def getMetadata(self):
		self.refresh()
		return self._metadata	
			
			
			
			
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


	def _f_commandHistoryFreeze(self):
		self._view._f_commandHistoryFreeze()


	def _f_commandHistoryThaw(self):
		self._view._f_commandHistoryThaw()






	parentNodeView = property( getParentNodeView )
	docView = property( getDocView )
	metadata = property( getMetadata )



