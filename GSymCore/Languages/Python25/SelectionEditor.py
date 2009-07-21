##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakValueDictionary

import cPickle


from java.lang import Object
from java.io import IOException
from java.util import List
from java.awt.event import KeyEvent
from java.awt.datatransfer import UnsupportedFlavorException, DataFlavor, StringSelection, Transferable

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction

from BritefuryJ.DocTree import DocTreeNode, DocTreeList, DocTreeObject


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent import *


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations




from GSymCore.Languages.Python25 import NodeClasses as Nodes
from GSymCore.Languages.Python25.Parser import Python25Grammar
from GSymCore.Languages.Python25.Precedence import *
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator
from GSymCore.Languages.Python25.StructureUnparser import Python25StructureUnparser
from GSymCore.Languages.Python25.StructureParser import Python25StructureGrammar
from GSymCore.Languages.Python25.PythonEditOperations import *


class NotImplementedError (Exception):
	pass



	
	
class Python25BufferFlavor (DataFlavor):
	def __init__(self):
		super( Python25BufferFlavor, self ).__init__( Object, DataFlavor.javaJVMLocalObjectMimeType )


_python25BufferDataFlavor = Python25BufferFlavor()
	


class Python25Transferable (Transferable):
	def __init__(self, data):
		self.data = data

	def getTransferData(self, flavor):
		if flavor is _python25BufferDataFlavor:
			return self.data
		else:
			raise UnsupportedFlavorException
	
	def getTransferDataFlavors(self):
		return [ _python25BufferDataFlavor ]
	
	def isDataFlavorSupported(self, flavor):
		return flavor is _python25BufferDataFlavor
	


	
class Python25Buffer (object):
	pass


class Python25BufferString (Python25Buffer):
	def __init__(self, text):
		self.text = text
		
		
class Python25BufferLinePair (Python25Buffer):
	def __init__(self, line1, line2):
		self.line1 = line1
		self.line2 = line2
		
		
class Python25BufferSubtree (Python25Buffer):
	def __init__(self, prefix, lineList, suffix):
		self.prefix = prefix
		self.lineList = lineList
		self.suffix = suffix
	
	



class Python25EditHandler (EditHandler):
	def __init__(self, viewContext):
		self._viewContext = viewContext
		self._grammar = Python25Grammar()
		
		
			
	def indent(self, context, node):
		selection = self._viewContext.getSelection()
		
		if selection.isEmpty():
			self._indentLine( context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._indentLine( context, node )
			else:
				self._indentSelection( selection )
			
			
			
	def dedent(self, context, node):
		selection = self._viewContext.getSelection()
		
		if selection.isEmpty():
			self._dedentLine( context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._indentLine( context, node )
			else:
				self._dedentSelection( selection )
				
			
			
	def _indentLine(self, context, node):
		# If @node comes after a compunt statement, move it to the end of that statement
		suite = node.getParentTreeNode()
		index = suite.indexOfById( node )
		next = suite[index+1]   if index < len( suite ) - 1   else None
		if index > 0:
			prev = suite[index-1]
			if isCompoundStmt( prev ):
				prevSuite = prev['suite']
				
				nodes = [ node ]
				
				# If @node is followed by an indented block, join the contents of the indented block onto @prevSuite
				if next is not None   and   isIndentedBlock( next ):
					nodes.extend( next['suite'] )
					EditOperations.remove( context, next )
						
				del suite[index]
				prevSuite.extend( nodes )
				
				# Now, 
				return
			

		if next is not None   and   isIndentedBlock( next ):
			# If @node is followed by an indented block, insert into the indented block
			EditOperations.remove( context, node )
			next['suite'].insert( 0, node )
		else:
			# Else, move @node into its own indented block
			indentedNode = Nodes.IndentedBlock( suite=[ node ] )
			EditOperations.replace( context, node, indentedNode )
			
	
	
	def _dedentLine(self, context, node):
		suite = node.getParentTreeNode()
		compStmt = suite.getParentTreeNode()
		outerSuite = compStmt.getParentTreeNode()   if compStmt is not None   else None
		outerIndex = outerSuite.indexOfById( compStmt )   if outerSuite is not None   else None

		# If @outerSuite is None, then this suite is from the module node, in which case we cannot dedent
		if outerSuite is not None:
			index = suite.indexOfById( node )
			if index == len( suite ) - 1:
				# If @node is at the end of a suite, move it into the parent suite
				outerIndex = outerSuite.indexOfById( compStmt )
				
				del suite[-1]
				outerSuite.insert( outerIndex + 1, node )
				
				# If @node was inside an indented block, which will now be empty: remove the indented block
				if len( suite ) == 0   and   isIndentedBlock( compStmt ):
					EditOperations.remove( context, compStmt )
				return
			elif index == 0  and  isIndentedBlock( compStmt ):
				# @node is the first node in an indented block; move it out.
				# The indented block has 2 or more child statements, otherwise the previous if-statment
				# would have been taken
				EditOperations.remove( context, node )
				outerSuite.insert( outerIndex, node )
			else:
				# Remove @node and all subsequent nodes from @suite.
				# Place @node into @outerSuite
				# Place all subsequent nodes into an indented block
				subsequentNodes = suite[index+1:]
				del suite[index:]
				outerSuite.insert( outerIndex + 1, Nodes.IndentedBlock( suite=subsequentNodes ) )
				outerSuite.insert( outerIndex + 1, node )
				
				
				
				
	def _indentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = getStatementContextFromElement( startMarker.getElement() )
		endContext = getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getViewNodeContentElement()
		endStmtElement = endContext.getViewNodeContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
		root = path0[0]
				
		if len( path0 ) == 1:
			# Start of selection is in the header of a compound statement; we need to work from one node up
			r = getParentStatementContext( root )
			if r is not None:
				root = r
			
		rootDoc = root.getDocNode()
		
		# Convert to a line list
		lineList = PyLineList( rootDoc['suite'] )
		
		# Replace the lines in the range startIndex->endIndex with the new parsed line
		startIndex = lineList.indexOf( startContext.getDocNode() )
		endIndex = lineList.indexOf( endContext.getDocNode() )
		lineList.indentRange( startIndex, endIndex + 1 )
		
		# Parse to ASTs
		newRootASTs = lineList.parse( self._grammar.statement() )
		
		# Insert into document
		rootDoc['suite'] = newRootASTs
			
				
	
	
	def _dedentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = getStatementContextFromElement( startMarker.getElement() )
		endContext = getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getViewNodeContentElement()
		endStmtElement = endContext.getViewNodeContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
		commonRoot = path0[0]
		
		if len( path0 ) == 1:
			# Start marker is in the header of a compound statement
			# Move up one
			commonRoot = getParentStatementContext( commonRoot )
			if commonRoot is None:
				return
		
		rootParent = getParentStatementContext( commonRoot )
		
		if rootParent is not None:
			rootParentDoc = rootParent.getDocNode()
			
			# Convert to a line list
			lineList = PyLineList( rootParentDoc['suite'] )
			
			# Replace the lines in the range startIndex->endIndex with the new parsed line
			startIndex = lineList.indexOf( startContext.getDocNode() )
			endIndex = lineList.indexOf( endContext.getDocNode() )
			lineList.dedentRange( startIndex, endIndex + 1 )
			
			# Parse to ASTs
			newRootASTs = lineList.parse( self._grammar.statement() )
			
			# Insert into document
			rootParentDoc['suite'] = newRootASTs

			
			
			
	def deleteSelection(self):
		self.replaceSelection( None )
	
	
	def replaceSelection(self, replacement):
		selection = self._viewContext.getSelection()
		
		if not selection.isEmpty():
			if replacement is not None   and   isinstance( replacement, Python25BufferSubtree ):
				raise NotImplementedError
			else:
				startMarker = selection.getStartMarker()
				endMarker = selection.getEndMarker()
				
				# Get the statements that contain the start and end markers
				startContext = getStatementContextFromElement( startMarker.getElement() )
				endContext = getStatementContextFromElement( endMarker.getElement() )
				# Get the statement elements
				startStmtElement = startContext.getViewNodeContentElement()
				endStmtElement = endContext.getViewNodeContentElement()
				# Get the text before and after the selection
				textBefore = startStmtElement.getTextRepresentationFromStartToMarker( startMarker )
				textAfter = endStmtElement.getTextRepresentationFromMarkerToEnd( endMarker )
				
				if replacement is None:
					# Compose a new line of text, and parse it
					line = textBefore + textAfter
				
					lineDocs = [ self._parseLine( line.strip( '\n' ) ) ]
				elif isinstance( replacement, str )   or   isinstance( replacement, unicode ):
					# Compose a new line of text, and parse it
					line = textBefore + replacement + textAfter
				
					lineDocs = [ self._parseLine( line.strip( '\n' ) ) ]
				elif isinstance( replacement, Python25BufferString ):
					# Compose a new line of text, and parse it
					line = textBefore + replacement.text + textAfter
				
					lineDocs = [ self._parseLine( line.strip( '\n' ) ) ]
				elif isinstance( replacement, Python25BufferLinePair ):
					# Compose two new lines of text, and parse
					line1 = textBefore + replacement.line2
					line2 = replacement.line2 + textAfter
				
					lineDocs = [ self._parseLine( line1.strip( '\n' ) ),  self._parseLine( line2.strip( '\n' ) ) ]
					
					
						
						
						
				# Now, insert the parsed text into the document		
				if startContext is endContext:
					# Selection is within a single statement
					pyReplaceStatementWithRange( startContext, startContext.getTreeNode(), lineDocs )
					selection.clear()
				else:
					# Get paths to start and end nodes, from the common root statement
					path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
					commonRoot = path0[0]
					selection.clear()
					
					if len( path0 ) == 1:
						# The path to the start node has only 1 entry; this means that the only statement
						# on the path is the common root.
						# The only way this can happen is if the start marker is within the bounds of the header
						# of a compound statement
						rootDoc = commonRoot.getDocNode()
						
						# Convert to a line list
						lineList = PyLineList( [ rootDoc ] )
						
						# Replace the lines in the range startIndex->endIndex with the new parsed line
						startIndex = lineList.indexOf( startContext.getDocNode() )
						endIndex = lineList.indexOf( endContext.getDocNode() )
						lineList.replaceRangeWithAST( startIndex, endIndex + 1, lineDocs )
						
						# Parse to ASTs
						newRootASTs = lineList.parse( self._grammar.statement() )
						
						# Insert into document
						EditOperations.replaceWithRange( commonRoot, commonRoot.getTreeNode(), newRootASTs )
					else:
						# Get the suite from the common root statement
						suite = commonRoot.getDocNode()['suite']
						
						# Get the indices of the child statements that contain the start and end markers respectively
						startStmtIndex = suite.indexOfById( path0[1].getDocNode() )
						endStmtIndex = suite.indexOfById( path1[1].getDocNode() )
						assert startStmtIndex != -1  and  endStmtIndex != -1
						
						# Convert to a line list
						lineList = PyLineList( suite[startStmtIndex:endStmtIndex+1] )
						
						# Replace the lines in the range startIndex->endIndex with the new parsed line
						startIndex = lineList.indexOf( startContext.getDocNode() )
						endIndex = lineList.indexOf( endContext.getDocNode() )
						lineList.replaceRangeWithAST( startIndex, endIndex + 1, lineDocs )
						
						# Parse to ASTs
						newASTs = lineList.parse( self._grammar.statement() )
						
						suite[startStmtIndex:endStmtIndex+1] = newASTs 
				
				
	
	
	
	
	def _insertBufferAtMarker(self, marker, b):
		markerStmtContext = getStatementContextFromElement( marker.getElement() )
		markerStmtElement = markerStmtContext.getViewNodeContentElement()
		textBefore = markerStmtElement.getTextRepresentationFromStartToMarker( marker )
		textAfter = markerStmtElement.getTextRepresentationFromMarkerToEnd( marker )
		if isinstance( b, Python25BufferString )  or  isinstance( b, Python25BufferLinePair )  or  isinstance( b, str )  or  isinstance( b, unicode ):
			if isinstance( b, str )  or  isinstance( b, unicode ):
				line = textBefore + b + textAfter
				lineDoc = self._parseLine( line.strip() )
				pyReplaceStatement( markerStmtContext, markerStmtContext.getTreeNode(), lineDoc )
			elif isinstance( b, Python25BufferString ):
				line = textBefore + b.text + textAfter
				lineDoc = self._parseLine( line.strip() )
				pyReplaceStatement( markerStmtContext, markerStmtContext.getTreeNode(), lineDoc )
			elif isinstance( b, Python25BufferLinePair ):
				# Even if the second line has indentation, its not enough to determine what to apply, so don't
				line1, line2 = b.line1, b.line2
				if not line2.startswith( line2.strip() ):
					stripped = line2.strip()
					contentIndex = line2.index( stripped )
					line2 = line2[contentIndex:]
				
				line1 = textBefore + line1
				line2 = line2 + textAfter
				lineDocs = [ self._parseLine( line1.strip() ), self._parseLine( line2.strip() ) ]
				pyReplaceStatementWithRange( markerStmtContext, markerStmtContext.getTreeNode(), lineDocs )
		elif isinstance( b, Python25BufferSubtree ):
			# Compute the text and ASTs for the lines that surround the marker / insertion point
			preLine = ( textBefore + b.prefix ).strip( '\n' )
			postLine = ( b.suffix + textAfter ).strip( '\n' )
			preDoc = self._parseLine( preLine )   if preLine != ''   else None
			postDoc = self._parseLine( postLine )   if postLine != ''   else None

			# Get the indentation of the first line of the source data
			destIndent = b.lineList.lines[0].indent   if len( b.lineList.lines ) > 0   else 0

			# Recurse up the destination document from the caret statement, the number of levels
			# necessary to fit the source block, so that indentation matches
			rootContext = markerStmtContext
			for i in xrange( 0, destIndent+1 ):
				parentContext = getParentStatementContext( rootContext )
				if parentContext is None:
					break
				rootContext = parentContext
			rootDoc = rootContext.getDocNode()
			
			# Convert the destination data to a line list, and get the position of the caret
			lineList = PyLineList( rootDoc['suite'] )
			index = lineList.indexOf( markerStmtContext.getDocNode() )
			indentAtCaret = lineList.lines[index].indent
			
			# Compute the indentation offset that should be applied to the source data so that
			# the first line matches the caret in the destination
			indentOffset = indentAtCaret - destIndent
			
			lineList.deleteLine( index )
			if postDoc is not None:
				postIndent = b.lineList.lines[-1].indent + indentOffset   if len( b.lineList.lines ) > 0   else indentAtCaret
				lineList.insertASTLine( index, postDoc, postIndent )
			lineList.insertLineList( index, b.lineList, indentOffset )
			if preDoc is not None:
				lineList.insertASTLine( index, preDoc, indentAtCaret )
			
			# Parse to ASTs
			newASTs = lineList.parse( self._grammar.statement() )
			
			rootDoc['suite'] = newASTs
			

			
			
	
	
	
	
	
	def getSourceActions(self):					# -> int
		return self.COPY_OR_MOVE
	
	
	
	def createTransferable(self):					# -> Transferable
		selection = self._viewContext.getSelection()
		
		if not selection.isEmpty():
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
	
			if startContext is endContext:
				# Selection within a single statement
				text = self._viewContext.getElementTree().getTextRepresentationInSelection( selection )
				return Python25Transferable( Python25BufferString( text ) )
			else:
				# Get the statement elements
				startStmtElement = startContext.getViewNodeContentElement()
				endStmtElement = endContext.getViewNodeContentElement()
				# Get the text between the selection start and the end of the start line, and the start of the end line and the selection end
				textInFirstLine = startStmtElement.getTextRepresentationFromMarkerToEnd( startMarker )
				textInLastLine = endStmtElement.getTextRepresentationFromStartToMarker( endMarker )
				
				path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
				
				if len( path0 ) == 2  and  len( path1 ) == 2:
					# Both statements fromt the same suite
					commonRootCtx = path0[0]
					suite = commonRootCtx.getDocNode()['suite']
					startIndex = suite.indexOfById( startContext.getDocNode() )
					endIndex = suite.indexOfById( endContext.getDocNode() )
					if endIndex == startIndex + 1:
						return Python25Transferable( Python25BufferLinePair( textInFirstLine, textInLastLine ) )


				#
				# Handle the subtree case
				#
				path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
				commonRoot = path0[0]
				
				rootDoc = commonRoot.getDocNode()
				
				# Convert to a line list
				lineList = PyLineList( [ rootDoc ] )

				# Compute the range of the line list
				startIndex = lineList.indexOf( startContext.getDocNode() )
				endIndex = lineList.indexOf( endContext.getDocNode() )
				
				# Determine if the start marker is at the start of the start statement
				if textInFirstLine == startStmtElement.getTextRepresentation():
					textInFirstLine = ''
				else:
					startIndex += 1
					
				if textInLastLine == endStmtElement.getTextRepresentation():
					textInLastLine = ''
				else:
					endIndex -= 1
					
				subList = lineList.subList( startIndex, endIndex + 1 )
				
				return Python25Transferable( Python25BufferSubtree( textInFirstLine, subList, textInLastLine ) )
					

		return None

				
				
				
	def exportDone(self, data, action):				# -> None,   data <- Transferable, action <- int
		if action == self.MOVE:
			self.deleteSelection()
		
			
			

	def canImport(self, support):					# -> bool,   support <- TransferHandler.TransferSupport
		if support.isDataFlavorSupported( _python25BufferDataFlavor ):
			return True
		else:
			return False

		
		
	def importData(self, info):					# -> bool,   info <- TransferHandler.TransferSupport
		if not self.canImport( info ):
			return False
		try:
			data = info.getTransferable().getTransferData( _python25BufferDataFlavor )
		except UnsupportedFlavorException:
			return False
		except IOException:
			return False
		
		if info.isDrop():
			# Drop
			return False
		else:
			# Paste
			selection = self._viewContext.getSelection()
			if not selection.isEmpty():
				self.replaceSelection( data )
				return True
			else:
				caret = self._viewContext.getCaret()
				caretMarker = caret.getMarker()
				if caretMarker.isValid():
					self._insertBufferAtMarker( caretMarker, data )
					return True
				return False
	

	def _parseLine(self, line):
		if line.strip() == '':
			# Blank line
			return Nodes.BlankLine()
		else:
			# Parse
			parsed = parseText( self._grammar.statement(), line )
			if parsed is None:
				# Parse failure; unparsed text
				return Nodes.UNPARSED( value=line )
			else:
				# Parsed
				return parsed
		
