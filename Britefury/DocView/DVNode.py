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
		
		# HTML node
		self._htmlNode = WDNode( self, self._view.viewContext, self._generateHtml )
		self._htmlNode.disable()
		
		# Unparsed text
		self._text = None
		
		# Focus
		self.focus = None
		
		# Child DVNodes
		self._children = set()
		
		# Reference count; determine if @self is in use
		self._refCount = 0
		

		
		
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
			child.__unref()
			self._view._nodeTable.unrefViewNode( child )
		# And clear the list
		self._children = set()
		
		if self._contentsFactory is not None:
			# Compute the contents
			contents = self._contentsFactory( self, self.treeNode, self._htmlNode )
			
			# The children list will have been re-populated; register the new relationships
			for child in self._children:
				child.__ref()
				self._view._nodeTable.refViewNode( child )
				
			return contents
		else:
			# No contents factory
			return None
		
		
	def _onContentsChanged(self):
		self._htmlNode.contentsChanged()
	

		
		
	#
	# HTML GENERATION
	#
		
	def _generateHtml(self, nodeContext):
		contents = self._contentsCell.getImmutableValue()
		
		if isinstance( contents, tuple ):
			html, text = contents
			
			assert isinstance( html, str )  or  isinstance( html, unicode )  or  isinstance( widget, DVNode )
			assert isinstance( text, UnparsedText )  or  isinstance( text, str )  or  isinstance( text, unicode )
			
			self._text = text
			return html
		else:
			html = contents
			assert isinstance( html, str )  or  isinstance( html, unicode )  or  isinstance( widget, DVNode )
			
			self._text = None
	
			return html
	
	def getHtmlNode(self):
		return self._htmlNode
	
	
	
	
	#
	# REFRESH METHODS
	#
	
	def refresh(self):
		self._generateHtml( self._htmlNode )
			
			
			
			
			
	#
	# CHILD RELATIONSHIP METHODS
	#
	
	def _registerChild(self, child):
		self._children.add( child )
		
	def __ref(self):
		if self._refCount == 0:
			self._htmlNode.enable()
		self._refCount += 1
		
	def __unref(self):
		self._refCount -= 1
		if self._refCount == 0:
			self._htmlNode.disable()
		
		
			
	#
	# CONTENT ACQUISITION METHODS
	#
	
	def reference(self):
		return self._htmlNode.reference()
	
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
	text = property( getText )



