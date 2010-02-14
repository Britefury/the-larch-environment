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

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface, DMNode

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction

from BritefuryJ.Parser.ItemStream import ItemStreamBuilder, ItemStream


from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations




from GSymCore.Languages.Python25 import NodeClasses as Nodes
from GSymCore.Languages.Python25.Parser import Python25Grammar
from GSymCore.Languages.Python25.Precedence import *
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator
from GSymCore.Languages.Python25.PythonEditOperations import *


class NotImplementedError (Exception):
	pass


_identity = DefaultIdentityTransformationFunction()
	
	
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


class Python25BufferStream (Python25Buffer):
	def __init__(self, stream):
		self.stream = stream
	



		
class IndentLinearRepresentationEvent (LinearRepresentationEvent):
	def __init__(self, element):
		super( IndentLinearRepresentationEvent, self ).__init__( element )

class DedentLinearRepresentationEvent (LinearRepresentationEvent):
	def __init__(self, element):
		super( DedentLinearRepresentationEvent, self ).__init__( element )

class SelectionLinearRepresentationEvent (LinearRepresentationEvent):
	def __init__(self, element):
		super( SelectionLinearRepresentationEvent, self ).__init__( element )

class IndentSelectionLinearRepresentationEvent (SelectionLinearRepresentationEvent):
	def __init__(self, element):
		super( IndentSelectionLinearRepresentationEvent, self ).__init__( element )

class DedentSelectionLinearRepresentationEvent (SelectionLinearRepresentationEvent):
	def __init__(self, element):
		super( DedentSelectionLinearRepresentationEvent, self ).__init__( element )

class SelectionEditLinearRepresentationEvent (SelectionLinearRepresentationEvent):
	def __init__(self, element):
		super( SelectionEditLinearRepresentationEvent, self ).__init__( element )


		
		

		

class Python25EditHandler (EditHandler):
	def __init__(self, viewContext):
		self._viewContext = viewContext
		self._grammar = Python25Grammar()
		
		
			
	def indent(self, element, context, node):
		selection = self._viewContext.getSelection()
		
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
		selection = self._viewContext.getSelection()
		
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
		element.setStructuralPrefixObject( Nodes.Indent() )
		element.setStructuralSuffixObject( Nodes.Dedent() )
		bSuccess = element.sendLinearRepresentationModifiedEventToParent( IndentLinearRepresentationEvent( element ) )
		if not bSuccess:
			print 'Python25EditHandler._indentLine(): INDENT LINE FAILED'
			element.clearStructuralPrefix()
			element.clearStructuralSuffix()
			
	
	
	def _dedentLine(self, element, context, node):
		suite = node.getValidParents()[0]
		suiteParent = suite.getValidParents()[0]
		if not suiteParent.isInstanceOf( Nodes.PythonModule ):
			# This statement is not in the root node
			element.setStructuralPrefixObject( Nodes.Dedent() )
			element.setStructuralSuffixObject( Nodes.Indent() )
			bSuccess = element.sendLinearRepresentationModifiedEventToParent( DedentLinearRepresentationEvent( element ) )
			if not bSuccess:
				print 'Python25EditHandler._dedentLine(): DEDENT LINE FAILED'
				element.clearStructuralPrefix()
				element.clearStructuralSuffix()
				
				
				
				
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
		
		rootElement = root.getViewNodeElement()
				
		startStmtElement.setStructuralPrefixObject( Nodes.Indent() )
		endStmtElement.setStructuralSuffixObject( Nodes.Dedent() )
		
		startContext.getViewNodeElement().clearStructuralRepresentationUpTo( rootElement )
		endContext.getViewNodeElement().clearStructuralRepresentationUpTo( rootElement )
		
		bSuccess = root.getViewNodeContentElement().sendLinearRepresentationModifiedEvent( IndentSelectionLinearRepresentationEvent( rootElement ) )
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
		startStmtElement = startContext.getViewNodeContentElement()
		endStmtElement = endContext.getViewNodeContentElement()

		# Get paths to start and end nodes, from the common root statement
		path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
		root = path0[0]
		
		rootElement = root.getViewNodeElement()
				
		startStmtElement.setStructuralPrefixObject( Nodes.Dedent() )
		endStmtElement.setStructuralSuffixObject( Nodes.Indent() )
		
		startContext.getViewNodeElement().clearStructuralRepresentationUpTo( rootElement )
		endContext.getViewNodeElement().clearStructuralRepresentationUpTo( rootElement )
		
		bSuccess = rootElement.sendLinearRepresentationModifiedEvent( DedentSelectionLinearRepresentationEvent( rootElement ) )
		if not bSuccess:
			print 'Python25EditHandler._dedentSelection(): DEDENT SELECTION FAILED'
			startStmtElement.clearStructuralPrefix()
			endStmtElement.clearStructuralSuffix()

			
			
			
	def deleteSelection(self):
		self.replaceSelection( None )
	
	
	def replaceSelection(self, replacement):
		selection = self._viewContext.getSelection()
		
		if not selection.isEmpty():
			startMarker = selection.getStartMarker()
			endMarker = selection.getEndMarker()
			
			# Get the statements that contain the start and end markers
			startContext = getStatementContextFromElement( startMarker.getElement() )
			endContext = getStatementContextFromElement( endMarker.getElement() )
			# Get the statement elements
			startStmtElement = startContext.getViewNodeContentElement()
			endStmtElement = endContext.getViewNodeContentElement()
			
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
					
					rootElement = root.getViewNodeElement()
				
					before = rootElement.getLinearRepresentationFromStartToMarker( startMarker )
					after = rootElement.getLinearRepresentationFromMarkerToEnd( endMarker )
					
					stream = joinStreamsForInsertion( root, before, replacement, after )
	
					rootElement.setStructuralValueStream( stream )
					selection.clear()
					rootElement.sendLinearRepresentationModifiedEvent( SelectionEditLinearRepresentationEvent( rootElement ) )
			else:
				# Now, insert the parsed text into the document		
				if startContext is endContext:
					builder = ItemStreamBuilder()
					builder.extend( startStmtElement.getLinearRepresentationFromStartToMarker( startMarker ) )
					builder.extend( endStmtElement.getLinearRepresentationFromMarkerToEnd( endMarker ) )
					startStmtElement.setStructuralValueStream( builder.stream() )
					selection.clear()
					startStmtElement.sendLinearRepresentationModifiedEvent( SelectionEditLinearRepresentationEvent( startStmtElement ) )
				else:
					# Get paths to start and end nodes, from the common root statement
					path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
					root = path0[0]
					
					rootElement = root.getViewNodeElement()
				
					before = rootElement.getLinearRepresentationFromStartToMarker( startMarker )
					after = rootElement.getLinearRepresentationFromMarkerToEnd( endMarker )
					stream = joinStreamsAroundDeletionPoint( before, after )
					rootElement.setStructuralValueStream( stream )
					selection.clear()
					rootElement.sendLinearRepresentationModifiedEvent( SelectionEditLinearRepresentationEvent( rootElement ) )
				
	
	
	
	
	def _insertBufferAtMarker(self, marker, b):
		markerStmtContext = getStatementContextFromElement( marker.getElement() )
		markerStmtElement = markerStmtContext.getViewNodeContentElement()

		if isinstance( b, Python25BufferStream ):
			before = markerStmtElement.getLinearRepresentationFromStartToMarker( marker )
			after = markerStmtElement.getLinearRepresentationFromMarkerToEnd( marker )
					
			stream = joinStreamsForInsertion( markerStmtContext, before, b.stream, after )
	
			markerStmtElement.setStructuralValueStream( stream )
			markerStmtElement.sendLinearRepresentationModifiedEvent( SelectionEditLinearRepresentationEvent( markerStmtElement ) )
			

			
			
	
	
	
	
	
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
	
			# Get the statement elements
			startStmtElement = startContext.getViewNodeContentElement()
			endStmtElement = endContext.getViewNodeContentElement()
			# Get the text between the selection start and the end of the start line, and the start of the end line and the selection end
			textInFirstLine = startStmtElement.getTextRepresentationFromMarkerToEnd( startMarker )
			textInLastLine = endStmtElement.getTextRepresentationFromStartToMarker( endMarker )
			
			path0, path1 = getStatementContextPathsFromCommonRoot( startContext, endContext )
				
			commonRoot = path0[0]
				
			presArea = startStmtElement.getPresentationArea()
			stream = presArea.getLinearRepresentationInSelection( selection )
			
			builder = ItemStreamBuilder()
			for item in stream.getItems():
				if isinstance( item, ItemStream.StructuralItem ):
					builder.appendStructuralValue( _identity.apply( item.getStructuralValue(), _identity ) )
				elif isinstance( item, ItemStream.TextItem ):
					builder.appendTextValue( item.getTextValue() )
			
			return Python25Transferable( Python25BufferStream( builder.stream() ) )

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
		
