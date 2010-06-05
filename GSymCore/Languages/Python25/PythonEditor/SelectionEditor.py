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
from java.awt.datatransfer import UnsupportedFlavorException, StringSelection, Transferable

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface, DMNode

from BritefuryJ.Parser.ItemStream import ItemStreamBuilder, ItemStream


from BritefuryJ.DocPresent.Clipboard import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *




class NotImplementedError (Exception):
	pass


class Python25Buffer (Object):
	pass


class Python25BufferStream (Python25Buffer):
	def __init__(self, stream):
		self.stream = stream
	



_python25BufferDataFlavor = LocalDataFlavor( Python25Buffer )



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
	


	


		
class PythonIndentationTreeEvent (object):
	pass

class PythonIndentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonDedentTreeEvent (PythonIndentationTreeEvent):
	pass

class PythonSelectionTreeEvent (object):
	def __init__(self, sourceElement):
		self.sourceElement = sourceElement

class IndentPythonSelectionTreeEvent (PythonSelectionTreeEvent):
	def __init__(self, sourceElement):
		super( IndentPythonSelectionTreeEvent, self ).__init__( sourceElement )

class DedentPythonSelectionTreeEvent (PythonSelectionTreeEvent):
	def __init__(self, sourceElement):
		super( DedentPythonSelectionTreeEvent, self ).__init__( sourceElement )

class SelectionEditLinearRepresentationEvent (PythonSelectionTreeEvent):
	def __init__(self, sourceElement):
		super( SelectionEditLinearRepresentationEvent, self ).__init__( sourceElement )


		
		

		

class Python25EditHandler (EditHandler):
	def __init__(self):
		self._grammar = Python25Grammar()
		
		
			
	def indent(self, element, context, node):
		viewContext = context.getView()
		selection = viewContext.getSelection()
		
		if selection.isEmpty():
			self._indentLine( element, context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._indentLine( element, context, node )
			else:
				self._indentSelection( selection )
			
			
			
	def dedent(self, element, context, node):
		viewContext = context.getView()
		selection = viewContext.getSelection()
		
		if selection.isEmpty():
			self._dedentLine( element, context, node )
		else:
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			
			if startContext is endContext:
				self._dedentLine( element, context, node )
			else:
				self._dedentSelection( selection )
				
			
			
	def _indentLine(self, element, context, node):
		element.setStructuralPrefixObject( Schema.Indent() )
		element.setStructuralSuffixObject( Schema.Dedent() )
		bSuccess = element.postTreeEventToParent( PythonIndentTreeEvent() )
		if not bSuccess:
			print 'Python25EditHandler._indentLine(): INDENT LINE FAILED'
			element.clearStructuralPrefix()
			element.clearStructuralSuffix()
			
	
	
	def _dedentLine(self, element, context, node):
		suite = node.getValidParents()[0]
		suiteParent = suite.getValidParents()[0]
		if not suiteParent.isInstanceOf( Schema.PythonModule ):
			# This statement is not in the root node
			element.setStructuralPrefixObject( Schema.Dedent() )
			element.setStructuralSuffixObject( Schema.Indent() )
			bSuccess = element.postTreeEventToParent( PythonDedentTreeEvent() )
			if not bSuccess:
				print 'Python25EditHandler._dedentLine(): DEDENT LINE FAILED'
				element.clearStructuralPrefix()
				element.clearStructuralSuffix()
		else:
			print 'Python25EditHandler._dedentLine(): Attempted to dedent line in top-level module'
			
				
				
				
	def _indentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = getStatementContextFromElement( startMarker.getElement() )
		endContext = getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getFragmentContentElement()
		endStmtElement = endContext.getFragmentContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
		root = path0[0]
		
		# Get the content element, not the fragment itself, otherwise editing operations that involve the module (top level) will trigger events that will NOT be caught
		rootElement = root.getFragmentContentElement()
				
		startContext.getFragmentContentElement().clearStructuralRepresentationsOnPathUpTo( rootElement )
		endContext.getFragmentContentElement().clearStructuralRepresentationsOnPathUpTo( rootElement )
		
		startStmtElement.setStructuralPrefixObject( Schema.Indent() )
		endStmtElement.setStructuralSuffixObject( Schema.Dedent() )
		
		bSuccess = root.getFragmentContentElement().postTreeEvent( IndentPythonSelectionTreeEvent( rootElement ) )
		if not bSuccess:
			print 'Python25EditHandler._indentSelection(): INDENT SELECTION FAILED'
			startStmtElement.clearStructuralPrefix()
			endStmtElement.clearStructuralSuffix()
			
				
	
	
	def _dedentSelection(self, selection):
		startMarker = selection.getStartMarker()
		endMarker = selection.getEndMarker()
		
		# Get the statements that contain the start and end markers
		startContext = getStatementContextFromElement( startMarker.getElement() )
		endContext = getStatementContextFromElement( endMarker.getElement() )
		# Get the statement elements
		startStmtElement = startContext.getFragmentContentElement()
		endStmtElement = endContext.getFragmentContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
		root = path0[0]
		
		# Get the content element, not the fragment itself, otherwise editing operations that involve the module (top level) will trigger events that will NOT be caught
		rootElement = root.getFragmentContentElement()
				
		startContext.getFragmentContentElement().clearStructuralRepresentationsOnPathUpTo( rootElement )
		endContext.getFragmentContentElement().clearStructuralRepresentationsOnPathUpTo( rootElement )
		
		startStmtElement.setStructuralPrefixObject( Schema.Dedent() )
		endStmtElement.setStructuralSuffixObject( Schema.Indent() )
		
		bSuccess = rootElement.postTreeEvent( DedentPythonSelectionTreeEvent( rootElement ) )
		if not bSuccess:
			print 'Python25EditHandler._dedentSelection(): DEDENT SELECTION FAILED'
			startStmtElement.clearStructuralPrefix()
			endStmtElement.clearStructuralSuffix()

			
			
			
	def deleteSelection(self, selection):
		self.replaceSelection( selection, None )
		
		
	def replaceSelectionWithText(self, selection, replacement):
		self.replaceSelection( selection, replacement )
	
	
	def replaceSelection(self, selection, replacement):
		if not selection.isEmpty():
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			# Get the statement elements
			startStmtElement = startContext.getFragmentContentElement()
			endStmtElement = endContext.getFragmentContentElement()
			
			if replacement is not None:
				if isinstance( replacement, Python25BufferStream ):
					replacement = replacement.stream
				elif isinstance( replacement, str )  or  isinstance( replacement, unicode ):
					builder = ItemStreamBuilder()
					builder.appendTextValue( replacement )
					replacement = builder.stream()
				else:
					replacement = None
					
				if replacement is not None:
					# Get paths to start and end nodes, from the common root statement
					path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
					root = path0[0]
					
					# Get the content element, not the fragment itself, otherwise editing operations that involve the module (top level) will trigger events that will NOT be caught
					rootElement = root.getFragmentContentElement()
				
					before = rootElement.getLinearRepresentationFromStartToMarker( startMarker )
					after = rootElement.getLinearRepresentationFromMarkerToEnd( endMarker )
					
					stream = joinStreamsForInsertion( root, before, replacement, after )
	
					rootElement.setStructuralValueStream( stream )
					selection.clear()
					rootElement.postTreeEvent( SelectionEditLinearRepresentationEvent( rootElement ) )
			else:
				# Now, insert the parsed text into the document		
				if startContext is endContext:
					builder = ItemStreamBuilder()
					builder.extend( startStmtElement.getLinearRepresentationFromStartToMarker( startMarker ) )
					builder.extend( endStmtElement.getLinearRepresentationFromMarkerToEnd( endMarker ) )
					startStmtElement.setStructuralValueStream( builder.stream() )
					selection.clear()
					startStmtElement.postTreeEvent( SelectionEditLinearRepresentationEvent( startStmtElement ) )
				else:
					# Get paths to start and end nodes, from the common root statement
					path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
					root = path0[0]
					
					# Get the content element, not the fragment itself, otherwise editing operations that involve the module (top level) will trigger events that will NOT be caught
					rootElement = root.getFragmentContentElement()
				
					before = rootElement.getLinearRepresentationFromStartToMarker( startMarker )
					after = rootElement.getLinearRepresentationFromMarkerToEnd( endMarker )
					stream = joinStreamsAroundDeletionPoint( before, after )
					rootElement.setStructuralValueStream( stream )
					selection.clear()
					rootElement.postTreeEvent( SelectionEditLinearRepresentationEvent( rootElement ) )
				
	
	
	
	
	def _insertBufferAtMarker(self, marker, b):
		markerStmtContext = getStatementContextFromElement( marker.getElement() )
		markerStmtElement = markerStmtContext.getFragmentContentElement()

		if isinstance( b, Python25BufferStream ):
			before = markerStmtElement.getLinearRepresentationFromStartToMarker( marker )
			after = markerStmtElement.getLinearRepresentationFromMarkerToEnd( marker )
					
			stream = joinStreamsForInsertion( markerStmtContext, before, b.stream, after )
	
			markerStmtElement.setStructuralValueStream( stream )
			markerStmtElement.postTreeEvent( SelectionEditLinearRepresentationEvent( markerStmtElement ) )
			

			
			
	
	
	
	
	
	def getExportActions(self, selection):					# -> int
		return self.COPY_OR_MOVE
	
	
	
	def createExportTransferable(self, selection):					# -> Transferable
		if not selection.isEmpty():
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
	
			# Get the statement elements
			startStmtElement = startContext.getFragmentContentElement()
			endStmtElement = endContext.getFragmentContentElement()
			
			rootElement = startStmtElement.getRootElement()
			stream = rootElement.getLinearRepresentationInSelection( selection )
			
			builder = ItemStreamBuilder()
			for item in stream.getItems():
				if isinstance( item, ItemStream.StructuralItem ):
					builder.appendStructuralValue( item.getStructuralValue().deepCopy() )
				elif isinstance( item, ItemStream.TextItem ):
					builder.appendTextValue( item.getTextValue() )
			
			return Python25Transferable( Python25BufferStream( builder.stream() ) )

		return None

				
				
				
	def exportDone(self, selection, transferable, action):				# -> None,   transferable <- Transferable, action <- int
		if action == self.MOVE:
			self.deleteSelection( selection )
		
			
			

	def canImport(self, caret, selection, dataTransfer):					# -> bool,   dataTransfer <- DataTransfer
		return dataTransfer.isDataFlavorSupported( _python25BufferDataFlavor )

		
		
	def importData(self, caret, selection, dataTransfer):					# -> bool,   dataTransfer <- DataTransfer
		if not self.canImport( caret, selection, dataTransfer ):
			return False
		try:
			data = dataTransfer.getTransferData( _python25BufferDataFlavor )
		except UnsupportedFlavorException:
			return False
		except IOException:
			return False
		
		# Paste
		if not selection.isEmpty():
			self.replaceSelection( selection, data )
			return True
		else:
			caretMarker = caret.getMarker()
			if caretMarker.isValid():
				self._insertBufferAtMarker( caretMarker, data )
				return True
			return False
		
