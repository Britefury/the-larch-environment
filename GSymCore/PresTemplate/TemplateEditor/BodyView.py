##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakKeyDictionary

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor
from BritefuryJ.Cell import Cell

from BritefuryJ.GSym.PresCom import InnerFragment

from GSymCore.PresTemplate import Schema

from GSymCore.PresTemplate.TemplateEditor.NodeView import NodeView
from GSymCore.PresTemplate.TemplateEditor.ParagraphView import ParagraphView



class BodyView (NodeView):
	def __init__(self, template, model):
		super( BodyView, self ).__init__( template, model )
		self._contentsCell = Cell( self._computeContents )
		
		
	def getContents(self):
		return self._contentsCell.getValue()
	
	
	def appendModel(self, node):
		self._model['contents'].append( node )
	
	def insertModelAfterNode(self, node, model):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		self._model['contents'].insert( index + 1, model )
		return True

	def deleteNode(self, node):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		del self._model['contents'][index]
		return True
		
		
		
	def joinConsecutiveTextNodes(self, firstNode):
		assert isinstance( firstNode, ParagraphView )
		contents = self.getContents()
		
		try:
			index = contents.index( firstNode )
		except ValueError:
			return False
		
		if ( index + 1)  <  len( contents ):
			next = contents[index+1]
			if isinstance( next, ParagraphView ):
				firstNode.setText( firstNode.getText() + next.getText() )
				del self._model['contents'][index+1]
				return True
		return False
	
	def splitTextNodes(self, textNode, textLines):
		style = textNode.getStyle()
		textModels = [ Schema.Paragraph( text=t, style=style )   for t in textLines ]
		try:
			index = self.getContents().index( textNode )
		except ValueError:
			return False
		self._model['contents'][index:index+1] = textModels
		return True
		
		
	def _computeContents(self):
		return [ self._viewOf( x )   for x in self._model['contents'] ]

	
	