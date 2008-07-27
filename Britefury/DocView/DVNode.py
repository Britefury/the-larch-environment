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

from Britefury.Cell.CellInterface import CellInterface
from Britefury.Cell.Cell import RefCell

from Britefury.DocView.DocView import DocView

from Britefury.DocPresent.Web.WDNode import WDNode

from Britefury.gSym.View.UnparsedText import UnparsedText



class DVNode (object):
	class _CouldNotFindNextChildError (Exception):
		pass


	def __init__(self, view, treeNode):
		super( DVNode, self ).__init__()
		
		# Tree node and document node
		self.treeNode = treeNode
		self.docNode = treeNode.node
		
		# View and parent DVNOde
		self._view = view
		self._parent = None
		
		# Contents factory and contents cell
		self._contentsFactory = None
		self._contentsCell = RefCell()
		self._contentsCell.function = self._computeContents
		self._contentsCell.changedSignal.connect( self._onContentsChanged )
		self._contents = None
		
		# HTML node
		self._htmlNode = WDNode( self._view.viewContext, self._generateHtml )
		
		# Unparsed text
		self._text = None
		
		# Focus
		self.focus = None
		
		# Child DVNodes
		self._children = set()
		

		
		
	def _changeTreeNode(self, treeNode):
		"""
		Change the tree node used by this DVNode
		"""
		assert treeNode.node is self.docNode, 'DVNode._changeTreeNode(): doc-node must remain the same'
		self.treeNode = treeNode
		
		

	#
	# DOCUMENT VIEW METHODS
	#
	def getDocView(self):
		"""
		Get the document view
		"""
		return self._view


	
	
	
	#
	# CONTENTS METHODS
	#
	
	def setContentsFactory(self, contentsFactory):
		"""
		Set the contents factory
		"""
		if contentsFactory is not self._contentsFactory:
			# Change @self._contentsFactory, and reset @self._contentsCell
			self._contentsFactory = contentsFactory
			self._contentsCell.function = self._computeContents
			
			
			
	def _computeContents(self):
		"""
		Compute the contents of this node
		"""
		# Note that the contents are stored in the @_contents attribute. This is to avoid the contents
		# being cached by the contents cell and therefore taking up memory.
		
		# The list of children may change:
		# Unregister all the existing parent->child relationships
		for child in self._children:
			self._view._nodeTable.unrefViewNode( child )
		# And clear the list
		self._children = set()
		
		if self._contentsFactory is not None:
			# Compute the contents
			self._contents = self._contentsFactory( self, self.treeNode )
			
			# The children list will have been re-populated; register the new relationships
			for child in self._children:
				self._view._nodeTable.refViewNode( child )
		else:
			# No contents factory
			self._contents = None
		
		return None
		
		
	def _onContentsChanged(self):
		self._htmlNode.contentsChanged()
	

		
		
	#
	# HTML GENERATION
	#
		
	def _generateHtml(self, nodeContext):
		accesses = CellInterface.blockAccessTracking()
		self._contentsCell.getImmutableValue()
		CellInterface.unblockAccessTracking( accesses )
		contents = self._contents
		self._contents = None
		html, self._text = contents
		return html
	
	
	def getHtmlNode(self):
		return self._htmlNode
			
			
			
			
			
	#
	# REFRESH METHODS
	#
	
	def _refreshNode(self):
		"""
		Refresh the node
		"""
		
		# Invoke getImmutableValue() on each of the refresh cells; this will ensure that the dependency relationships on child nodes are
		# discovered by the Cell system
		for cell in self._cellsToRefresh:
			cell.getImmutableValue()
			
		# Get the contents
		contents = self._contentsCell.getImmutableValue()
		
		if isinstance( contents, tuple ):
			html, text = contents
			assert isinstance( html, str )  or  isinstance( html, unicode )  or  isinstance( widget, DVNode )
			assert isinstance( text, UnparsedText )  or  isinstance( text, str )  or  isinstance( text, unicode )
			
			# If the contents is a DVNode, get its widget
			if isinstance( html, DVNode ):
				html = html.getHtml()
			
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


	def _resetRefreshCell(self):
		"""
		Reset the refresh cell; causes a refresh to be queued
		"""
		self.refreshCell.function = self._refreshNode


	def refresh(self):
		"""
		Refresh this node
		"""
		self.refreshCell.immutableValue


		

	#
	# CHILD RELATIONSHIP METHODS
	#
	
	def _f_setRefreshCells(self, cells):
		self._cellsToRefresh = cells
		self._resetRefreshCell()
		
	def _registerChild(self, child):
		self._children.add( child )
		
		
			
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


	def _commandHistoryFreeze(self):
		"""
		Freeze the command history
		"""
		self._view._commandHistoryFreeze()


	def _commandHistoryThaw(self):
		"""
		Thaw the command history
		"""
		self._view._commandHistoryThaw()






	parentNodeView = property( getParentNodeView )
	docView = property( getDocView )
	widget = property( getWidget )
	text = property( getText )



