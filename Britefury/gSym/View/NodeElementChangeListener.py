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
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Marker import *

from BritefuryJ.DocTree import DocTree
from BritefuryJ.DocTree import DocTreeNode

from BritefuryJ.DocView import DVNode
from BritefuryJ.DocView import DocView

from BritefuryJ.GSym.View import StringDiff

from Britefury.Util.NodeUtil import isListNode, nodeToSXString

from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.MethodDispatch import methodDispatch



DIFF_THRESHOLD = 65536



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
					#assert ( leaf is elementContent )  !=  ( leaf is not elementContent )
					if leaf is elementContent:
						leafOffset = 0
					else:
						leafOffset = leaf.getContentOffsetInSubtree( elementContent )
					leafPosition = newPosition - leafOffset
					
					if leaf.isEditableEntry():
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

