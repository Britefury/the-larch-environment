##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import difflib

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Marker import *


from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel import KMeta

from Britefury.Cell.Cell import RefCell

from Britefury.DocView.DocView import DocView

from Britefury.gSym.View.UnparsedText import UnparsedText



#_defaultBinStyleSheet = ContainerStyleSheet()
_defaultParagraphStyleSheet = ParagraphStyleSheet()




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
		self._elementContent = None
		self._metadata = None
		self._contentsFactory = None
		self._contentsCell = RefCell()
		self._contentsCell.function = self._p_computeContents
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
		if self._elementContent is not None:
			startContent = self._elementContent.getContent()
		else:
			startContent = ''
		position, bias, contentString = self._getCursorPositionBiasAndContentString( self._elementContent )
		print 'Node: ', self.docNode[0], position, self._elementContent

		# Set the caret node to self
		self._view._caretNode = self

		contents = self._contentsCell.getImmutableValue()
		for child in self._children:
			child.refresh()

		self._updateElementAndMetadata( contents )
		
		#if self._view._caretNode is self:
		if True:
			# Invoking child.refresh() above can cause this method to be invoked on another node; recursively;
			# Ensure that only the inner-most recursion level handles the caret
			if position is not None  and  bias is not None  and  self._elementContent is not None:
				index = position + 1   if bias == Marker.Bias.END   else   position
	
				newContentString = self._elementContent.getContent()
				
				newPosition = position
	
				# Compute the difference
				matcher = difflib.SequenceMatcher( lambda x: x in ' \t', contentString, newContentString )
				for tag, i1, i2, j1, j2 in matcher.get_opcodes():
					if tag == 'replace':
						if newPosition >= i2:
							newPosition += ( j2 - j1 ) - ( i2 - i1 )
					elif tag == 'delete':
						if newPosition >= i1:
							newPosition = max( newPosition - ( i2 - i1 ), i1 )
					elif tag == 'insert':
						if newPosition >= i1:
							newPosition += ( j2 - j1 )
					elif tag == 'equal':
						pass
					else:
						raise ValueError, 'unreckognised tag'
				elementTree = self._elementContent.getElementTree()
				caret = elementTree.getCaret()
				leaf = self._elementContent.getLeafAtContentPosition( newPosition )
				if leaf is not None:
					leafOffset = leaf.getContentOffsetInSubtree( self._elementContent )
					leafPosition = newPosition - leafOffset
					print 'Node "%s"; content was "%s" now "%s"'  %  ( self.docNode[0], startContent, self._elementContent.getContent() )
					print 'Position was %d, now is %d; leaf (%s) offset is %d, moving to %d in leaf'  %  ( position, newPosition, leaf.getContent(), leafOffset, leafPosition )
					leaf.moveMarker( caret.getMarker(), leafPosition, bias )

				
				
		
	def _o_resetRefreshCell(self):
		self.refreshCell.function = self._o_refreshNode


	def refresh(self):
		self.refreshCell.getImmutableValue()


	def _getCursorPositionBiasAndContentString(self, element):
		if element is not None:
			contentString = element.getContent()
			elementTree = element.getElementTree()
			caret = elementTree.getCaret()
			try:
				position = caret.getMarker().getPositionInSubtree( self._elementContent )
			except DPWidget.IsNotInSubtreeException:
				return None, None, contentString
			return position, caret.getMarker().getBias(), contentString
		else:
			return None, None, ''
		
	
		
					

	def _p_computeContents(self):
		# Unregister existing child relationships
		for child in self._children:
			self._view._nodeTable.unrefViewNode( child )
		self._children = set()
		
		if self._contentsFactory is not None:
			contents = self._contentsFactory( self, self.treeNode )
			
			# Register new child relationships
			for child in self._children:
				self._view._nodeTable.refViewNode( child )
			
			return contents
		else:
			return None
		
		
	def _updateElementAndMetadata(self, contents):
		if contents is not None:
			if isinstance( contents, tuple ):
				element, metadata = contents
			else:
				element = contents
				metadata = None
				
			assert isinstance( element, Element )  or  isinstance( element, DVNode )
			
			# If the contents is a DVNode, get its widget
			if isinstance( element, DVNode ):
				element = element.getElementNoRefresh()
			elif isinstance( element, Element ):
				pass
			else:
				raise TypeError, 'contents should be an Element or a DVNode'
			
			#self._element.setChild( element )
			self._elementContent = element
			self._element.setChildren( [ element ] )
			self._metadata = metadata
		else:
			self._elementContent = None
			self._element.setChildren( [] )
			self._metadata = None
	

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



