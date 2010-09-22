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



class ParagraphView (NodeView):
	def __init__(self, template, model):
		super( ParagraphView, self ).__init__( template, model )
	
		
	def getText(self):
		return self._model['text']
	
	def setText(self, text):
		self._model['text'] = text
		
	
	def getStyle(self):
		return self._model['style']
	
	def setStyle(self, style):
		self._model['style'] = style
		
		
	def partialModel(self):
		return Schema.PartialParagraph( style=self._model['style'] )
		
		
	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
