##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys

from datetime import datetime

import difflib

from copy import copy

from BritefuryJ.Cell import *

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Marker import *

from BritefuryJ.DocTree import DocTree
from BritefuryJ.DocTree import DocTreeNode

from BritefuryJ.DocView import DVNode
from BritefuryJ.DocView import DocView

from BritefuryJ.GSym.View import StringDiff

#from Britefury.DocView.DocView import DocView


from Britefury.Util.NodeUtil import isListNode, nodeToSXString

from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.MethodDispatch import methodDispatch

from Britefury.gSym.View import ListView
from Britefury.gSym.View.gSymStyles import viewError_textStyle

#from Britefury.DocTree.DocTree import DocTree
#from Britefury.DocTree.DocTreeNode import DocTreeNode



DIFF_THRESHOLD = 65536




"""
A brief explanation as to how this module works.


The view is specified as a pattern match expression.
Each match is matched with a view expression that is compiled into Python source which is executed to create the contents of a view node.


The code that is executed to create a view is split into two parts:
  - compiled code
    - match expressions
    - view expressions
  - non-compiled code
    - made available to compiled code
    - prefixed by _runtime_
    
There are 3 levels of operation:
1. View node instance level
  - Handles a view of a specific subtree of the document
2. View instance level
  - Handles the whole document
3. View factory level
  - Creates a view instance
  
  
Refresh policy

DVNode instances have a content refresh system which will allow some level of automation for keeping the view of the document in sync with the document contents.
A DVNode displays its content in a widget hierarchy.
A DVNode has a refresh cell that will result in refreshing its contents
The node can be passed a list of cells that are refreshed in its refresh-cell function.
This means that these cells will be refreshed when the overall node cell is refreshed.
Hierarchy of view-expression is ignored here;
the node refresh cell invoked all cells from the view-expression directly (from a list).
The hierarchy of document view nodes is respected however.
"""



def raiseRuntimeError(exceptionClass, src, reason):
	raise exceptionClass, reason  +  '   ::   '  +  nodeToSXString( src )





def _registerViewNodeRelationship(viewNodeInstance, childNode):
	viewNodeInstance.viewNode.registerChild( childNode )


def _populateBin(viewNodeInstance, bin, child):
	if isinstance( child, Element ):
		bin.setChild( child )
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_populateBin: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _populateContainerSeq(viewNodeInstance, container, children):
	elements = []
	for child in children:
		if isinstance( child, Element ):
			elements.append( child )
		else:
			raiseRuntimeError( TypeError, viewNodeInstance.xs, ' _populateContainerSeq: could not process child of type %s'  %  ( type( child ).__name__, ) )
	container.setChildren( elements )

def _populateScript(viewNodeInstance, script, child, slotIndex):
	if isinstance( child, Element ):
		script.setChild( slotIndex, child )
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_populateScript: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _populateFraction(viewNodeInstance, fraction, child, slotIndex):
	if isinstance( child, Element ):
		fraction.setChild( slotIndex, child )
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_populateFraction: could not process child of type %s'  %  ( type( child ).__name__, ) )

	
	
			
	

#
#
# 'ctx' parameters are _GSymNodeViewInstance instances
#
#



def border(ctx, styleSheet, child):
	viewNodeInstance = ctx
	element = BorderElement( styleSheet )
	_populateBin( viewNodeInstance, element, child )
	return element

def indent(ctx, indentation, child):
	viewNodeInstance = ctx
	styleSheet = viewNodeInstance.viewInstance._indentationStyleSheet( indentation )
	element = BorderElement( styleSheet )
	_populateBin( viewNodeInstance, element, child )
	return element

def text(ctx, styleSheet, txt):
	return TextElement( styleSheet, txt )

def hiddenText(ctx, txt):
	return HiddenContentElement( txt )

def whitespace(ctx, txt, width=0.0):
	return WhitespaceElement( txt, width )



def hbox(ctx, styleSheet, children):
	"""
	Horizontal box container
	"""
	viewNodeInstance = ctx
	element = HBoxElement( styleSheet )
	_populateContainerSeq( viewNodeInstance, element, children )
	return element

_ahboxStyleSheet = HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, False, 0.0 )
def ahbox(ctx, children):
	"""
	Baseline-aligned horizontal box container
	"""
	return hbox( ctx, _ahboxStyleSheet, children )

def vbox(ctx, styleSheet, children):
	"""
	Vertical box container
	"""
	viewNodeInstance = ctx
	element = VBoxElement( styleSheet )
	_populateContainerSeq( viewNodeInstance, element, children )
	return element

def paragraph(ctx, styleSheet, children):
	"""
	Paragraph container
	"""
	viewNodeInstance = ctx
	element = ParagraphElement( styleSheet )
	_populateContainerSeq( viewNodeInstance, element, children )
	return element

def lineBreak(ctx, styleSheet, child):
	"""
	Line break container
	"""
	viewNodeInstance = ctx
	element = LineBreakElement( styleSheet )
	_populateBin( viewNodeInstance, element, child )
	return element

def segment(ctx, styleSheet, caretStopFactory, child):
	"""
	Segment container
	"""
	viewNodeInstance = ctx
	element = SegmentElement( styleSheet, caretStopFactory )
	_populateBin( viewNodeInstance, element, child )
	return element


def script(ctx, styleSheet, mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	element = ScriptElement( styleSheet )
	
	_populateScript( viewNodeInstance, element, mainChild, DPScript.MAIN )
	if leftSuperChild is not None:
		_populateScript( viewNodeInstance, element, leftSuperChild, DPScript.LEFTSUPER )
	if leftSubChild is not None:
		_populateScript( viewNodeInstance, element, leftSubChild, DPScript.LEFTSUB )
	if rightSuperChild is not None:
		_populateScript( viewNodeInstance, element, rightSuperChild, DPScript.RIGHTSUPER )
	if rightSubChild is not None:
		_populateScript( viewNodeInstance, element, rightSubChild, DPScript.RIGHTSUB )
	return element

def scriptLSuper(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, scriptChild, None, None, None )

def scriptLSub(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, None, scriptChild, None, None )

def scriptRSuper(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, None, None, scriptChild, None )

def scriptRSub(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, None, None, None, scriptChild )


def fraction(ctx, styleSheet, numerator, denominator):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	element = FractionElement( styleSheet )
	
	_populateFraction( viewNodeInstance, element, numerator, DPFraction.NUMERATOR )
	_populateFraction( viewNodeInstance, element, denominator, DPFraction.DENOMINATOR )
	return element





def listView(ctx, layout, beginDelim, endDelim, separatorFactory, children):
	"""
	Builds a list view.
	@layout controls the layout
	"""
	viewNodeInstance = ctx
	element = ListView.listView( viewNodeInstance.xs, layout, beginDelim, endDelim, separatorFactory, children )
	return element


listViewStrToElementFactory = ListView.listViewStrToElementFactory



_contentListenerParaStyle = ParagraphStyleSheet()




def _applyContentListener(ctx, child, listener):
	viewNodeInstance = ctx

	if isinstance( child, Element ):
		child.setContentListener( listener )
		return child
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_applyContentListener: could not process child of type %s'  %  ( type( child ).__name__, ) )

		
def contentListener(ctx, child, listener):
	"""
	Sets a content listener
	"""
	
	if isinstance( child, list )  or  isinstance( child, tuple ):
		return [ _applyContentListener( ctx, c, listener )   for c in child ]
	else:
		return _applyContentListener( ctx, child, listener )
		



def viewEval(ctx, content, nodeViewFunction=None, state=None):
	"""Build a view for a document subtree (@content)"""
	viewNodeInstance = ctx
	
	if not isinstance( content, DocTreeNode ):
		raise TypeError, 'buildView: content is not a DocTreeNode'
		
	# A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
	viewInstance = viewNodeInstance.viewInstance
	viewNode = viewNodeInstance.view.buildNodeView( content )
	#viewNode._f_setContentsFactory( viewNodeInstance.viewInstance._f_makeNodeContentsFactory( nodeViewFunction, state ) )
	viewNode.setNodeElementFactory( viewNodeInstance.viewInstance._f_makeNodeElementFactory( nodeViewFunction, state ) )
	
	
	# Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
	# and refresh the view node
	# Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
	# is up to date and available.
	# Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
	# refresh.
	accessList = CellInterface.blockAccessTracking()
	viewNode.refresh()
	CellInterface.unblockAccessTracking( accessList )
	
	_registerViewNodeRelationship( viewNodeInstance, viewNode )

	return viewNode.getElementNoRefresh()


def mapViewEval(ctx, content, nodeViewFunction=None, state=None):
	return [ viewEval( ctx, x, nodeViewFunction, state )   for x in content ]







class _GSymNodeViewInstance (object):
	"""
	Manages state that concerns a view of a specific sub-tree of a document
	"""
	
	__slots__ = [ 'xs', 'view', 'viewInstance', 'viewNode' ]

	def __init__(self, xs, view, viewInstance, viewNode):
		self.xs = xs
		self.view = view
		self.viewInstance = viewInstance
		self.viewNode = viewNode

		


	
	
	
	
class _GSymViewInstanceNodeContentsFactory (DVNode.NodeElementFactory):
	__slots__ = [ '_nodeViewFunction', '_state', '_viewInstance' ]
	
	def __init__(self, viewInstance, nodeViewFunction, state):
		self._viewInstance = viewInstance
		self._nodeViewFunction = nodeViewFunction
		self._state = state
		
	
	def createNodeElement(self, viewNode, treeNode):
		# Create the node view instance
		nodeViewInstance = _GSymNodeViewInstance( treeNode.node, self._viewInstance.view, self._viewInstance, viewNode )
		## HACK ##
		# Build the contents
		return self._viewInstance._p_buildNodeViewContents( nodeViewInstance, treeNode, self._nodeViewFunction, self._state )
	
	
	
class _GSymViewInstance (object):
	"""
	Manages state concerning a view of a specific document
	"""
	def __init__(self, tree, xs, viewFactory, commandHistory):
		class RootInitialiser (DocView.RootNodeInitialiser):
			def initRootNode(initialiserSelf, viewNode, treeNode):
				viewNode.setNodeElementFactory( self._f_makeNodeElementFactory( None, None ) )
			
		
		
		self.tree = tree
		self.xs = xs
		self.generalNodeViewFunction = viewFactory.createViewFunction()
		# self._p_buildDVNode is a factory that builds DVNode instances for document subtrees
		self.view = DocView( self.tree, self.xs, RootInitialiser(), _NodeElementChangeListener() )
		
		self._indentationStyleSheets = {}
		
		self._nodeContentsFactories = {}
		
		
		
	def _indentationStyleSheet(self, indentation):
		try:
			return self._indentationStyleSheets[indentation]
		except KeyError:
			styleSheet = BorderStyleSheet( indentation, 0.0, 0.0, 0.0 )
			self._indentationStyleSheets[indentation] = styleSheet
			return styleSheet
	
	
		
	def _f_makeNodeElementFactory(self, nodeViewFunction, state):
		#Memoise the contents factory; keyed by @nodeViewFunction and @state
		key = nodeViewFunction, state
		try:
			return self._nodeContentsFactories[key]
		except KeyError:
			factory = _GSymViewInstanceNodeContentsFactory( self, nodeViewFunction, state )
			self._nodeContentsFactories[key] = factory
			return factory
		
	
	def _p_buildNodeViewContents(self, nodeViewInstance, content, nodeViewFunction, state):
		"""Runtime - build the contents of a view node"""
		if nodeViewFunction is None:
			nodeViewFunction = self.generalNodeViewFunction

		return nodeViewFunction( content, nodeViewInstance, state )
	
	
	
	
class _NodeElementChangeListener (DVNode.NodeElementChangeListener):
	def __init__(self):
		self._caretNode = None
		self._posBiasContent = None
	
		
	def elementChangeFrom(self, node, element):
		elementContent = node.getInnerElementNoRefresh()
		if elementContent is not None:
			startContent = elementContent.getContent()
		else:
			startContent = ''
		position, bias, contentString = self._getCursorPositionBiasAndContentString( node, elementContent )
		#print 'Node: ', node.getDocNode()[0], position, elementContent

		# Set the caret node to node
		if position is not None  and  bias is not None  and  elementContent is not None:
			if isListNode( node.getDocNode() ):
				#print 'Node: %s, position=%d'  %  ( node.getDocNode()[0], position )
				pass
			self._caretNode = node
			self._posBiasContent = position, bias, contentString
		
		
	def elementChangeTo(self, node, element):
		if self._caretNode is node:
			elementContent = node.getInnerElementNoRefresh()
			position, bias, contentString = self._posBiasContent
			# Invoking child.refresh() above can cause this method to be invoked on another node; recursively;
			# Ensure that only the inner-most recursion level handles the caret
			if position is not None  and  bias is not None  and  elementContent is not None:
				print 'CURSOR POSITION CHANGE'
				newContentString = elementContent.getContent()
				
				newPosition = position
				newBias = bias
	
				oldIndex = position  +  ( 1  if bias == Marker.Bias.END  else  0 )

				# Compute the difference between the old content and the new content in order to update the cursor position
				
				# String differencing is a O(mn) algorithm, where m and n are the lengths of the source and destination strings respectively
				# In order to prevent awful performance, string differencing must be applied to as little text as possible.
				# In most cases, the edit operation affects part of the content in the middle of the string; large parts of the beginning and end
				# will remain unchanged
				# By computing the common prefix and common suffix of the two strings, we can narrow down the window to which differencing
				# is applied.
				# Limiting the scope can cause the differencing algorithm to produce a different result, than if it was applied to the whole string.
				# In order to keeps the result consistent with that of a complete string difference, an extra character at the start and end of the
				# change region is included.
				
				# Get the size of the common prefix and suffix
				prefixLen = StringDiff.getCommonPrefixLength( contentString, newContentString )
				suffixLen = StringDiff.getCommonSuffixLength( contentString, newContentString )
				# Include 1 extra character at each end if available
				prefixLen = max( 0, prefixLen - 1 )
				suffixLen = max( 0, suffixLen - 1 )
				# Compute the lengths of the change region in the original content and the new content
				origChangeRegionLength = len( contentString ) - prefixLen - suffixLen
				newChangeRegionLength = len( newContentString ) - prefixLen - suffixLen
				
				# If the m*n > DIFF_THRESHOLD, use a simpler method; this prevents slow downs
				if ( origChangeRegionLength * newChangeRegionLength )  >  DIFF_THRESHOLD:
					# HACK HACK HACK
					if position > prefixLen:
						rel = position - prefixLen
						if rel > origChangeRegionLength:
							rel += newChangeRegionLength - origChangeRegionLength
						else:
							rel = min( rel, newChangeRegionLength )
						position = rel + prefixLen
					# HACK HACK HACK
				else:
					# Cannot simply use contentString[prefixLen:-suffixLen], since suffixLen may be 0
					origChangeRegion = contentString[prefixLen:len(contentString)-suffixLen]
					newChangeRegion = newContentString[prefixLen:len(newContentString)-suffixLen]
					matcher = difflib.SequenceMatcher( lambda x: x in ' \t', origChangeRegion, newChangeRegion )
					opcodes = matcher.get_opcodes()
					for tag, i1, i2, j1, j2 in matcher.get_opcodes():
						# Apply the prefix offset
						i1 += prefixLen
						i2 += prefixLen
						j1 += prefixLen
						j2 += prefixLen
						if ( position > i1  or  ( position == i1  and  bias == Marker.Bias.END ) )   and   position < i2:
							# Caret is in the range of this opcode
							if tag == 'delete':
								# Range deleted; move to position in destination; bias:START
								newPosition = j1
								newBias = Marker.Bias.START
							elif tag == 'replace'  or  tag == 'equal'  or  tag == 'insert':
								# Range replaced or equal; move position by delta
								newPosition += j1 - i1
							else:
								raise ValueError, 'unreckognised tag'
				elementTree = elementContent.getElementTree()
				caret = elementTree.getCaret()
				
				
				newIndex = newPosition  +  ( 1  if newBias == Marker.Bias.END  else  0 )
				
				#if bias == Marker.Bias.START:
					#print contentString[:oldIndex].replace( '\n', '\\n' ) + '>|' + contentString[oldIndex:].replace( '\n', '\\n' )
				#else:
					#print contentString[:oldIndex].replace( '\n', '\\n' ) + '|<' + contentString[oldIndex:].replace( '\n', '\\n' )

				#if bias == Marker.Bias.START:
					#print newContentString[:newIndex].replace( '\n', '\\n' ) + '>|' + newContentString[newIndex:].replace( '\n', '\\n' )
				#else:
					#print newContentString[:newIndex].replace( '\n', '\\n' ) + '|<' + newContentString[newIndex:].replace( '\n', '\\n' )
				
				newPosition = max( 0, newPosition )
				if newPosition >= elementContent.getContentLength():
					newPosition = elementContent.getContentLength() - 1
					newBias = Marker.Bias.END
				
				leaf = elementContent.getLeafAtContentPosition( newPosition )
				if leaf is not None:
					#print leaf, "'" + leaf.getContent().replace( '\n', '\\n' ) + "'"
					leafOffset = leaf.getContentOffsetInSubtree( elementContent )
					leafPosition = newPosition - leafOffset
					
					if leaf.isEditable():
						#print 'Node "%s"; content was "%s" now "%s"'  %  ( node.getDocNode()[0], startContent, elementContent.getContent() )
						#print 'Position was %d, now is %d; leaf (%s) offset is %d, moving to %d in leaf'  %  ( position, newPosition, leaf.getContent(), leafOffset, leafPosition )
						leaf.moveMarker( caret.getMarker(), leafPosition, newBias )
					else:
						segFilter = SegmentElement.SegmentFilter( leaf.getSegment() )
						elemFilter = LeafElement.LeafFilterEditable()
						
						if leafPosition < leaf.getContentLength()/2:
							left = leaf.getPreviousLeaf( segFilter, None, elemFilter )
							if left is not None:
								#print left, "'" + left.getContent().replace( '\n', '\\n' ) + "'", left.getSegment() is leaf.getSegment()
								left.moveMarkerToEnd( caret.getMarker() )
							else:
								right = leaf.getNextLeaf( segFilter, None, elemFilter )
								if right is not None:
									#print right, "'" + right.getContent().replace( '\n', '\\n' ) + "'"
									right.moveMarkerToStart( caret.getMarker() )
								else:
									leaf.moveMarker( caret.getMarker(), leafPosition, newBias )
						else:
							right = leaf.getNextLeaf( segFilter, None, elemFilter )
							if right is not None:
								#print right, "'" + right.getContent().replace( '\n', '\\n' ) + "'"
								right.moveMarkerToStart( caret.getMarker() )
							else:
								left = leaf.getPreviousLeaf( segFilter, None, elemFilter )
								if left is not None:
									#print left, "'" + left.getContent().replace( '\n', '\\n' ) + "'"
									left.moveMarkerToEnd( caret.getMarker() )
								else:
									leaf.moveMarker( caret.getMarker(), leafPosition, newBias )

		
	def _getCursorPositionBiasAndContentString(self, node, element):
		if element is not None:
			contentString = element.getContent()
			elementTree = element.getElementTree()
			caret = elementTree.getCaret()
			try:
				position = caret.getMarker().getPositionInSubtree( node.getInnerElementNoRefresh() )
			except DPWidget.IsNotInSubtreeException:
				return None, None, contentString
			return position, caret.getMarker().getBias(), contentString
		else:
			return None, None, ''

		
		
		
class GSymView (object):
	def __call__(self, xs, ctx, state):
		try:
			return methodDispatch( self, xs, ctx, state )
		except DispatchError:
			return ctx.text( viewError_textStyle, '<<ERROR>>' )
	
		
		
		
class GSymViewFactory (object):
	"""
	Used to manufacture document views
	Manages state concerning a view that has been compiled.
	"""
	def __init__(self, viewClass):
		super( GSymViewFactory, self ).__init__()
		self.viewClass = viewClass
		
		
	def createViewFunction(self):
		return self.viewClass()
		
		
	def createDocumentView(self, xs, commandHistory):
		tree = DocTree()
		txs = tree.treeNode( xs )
		viewInstance = _GSymViewInstance( tree, txs, self, commandHistory )
		return viewInstance.view


	


