##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakKeyDictionary

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor
from BritefuryJ.Cell import Cell

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, DMObjectNodeMethodDispatchMetaClass, dmObjectNodeMethodDispatch

from GSymCore.Languages.Python25.Execution import Execution

from GSymCore.Worksheet import Schema





class _Projection (object):
	__metaclass__ = DMObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = 1
	

	def __call__(self, node, worksheet):
		return dmObjectNodeMethodDispatch( self, node, worksheet )

	@DMObjectNodeDispatchMethod( Schema.Worksheet )
	def worksheet(self, worksheet, node):
		return WorksheetView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def paragraph(self, worksheet, node):
		return ParagraphView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def pythonCode(self, worksheet, node):
		return PythonCodeView( worksheet, node )

_projection = _Projection()



class WorksheetNodeView (object):
	def __init__(self, worksheet, model):
		self._worksheet = worksheet
		self._model = model
		
	def getModel(self):
		return self._model


class WorksheetView (WorksheetNodeView):
	def __init__(self, worksheet, model):
		super( WorksheetView, self ).__init__( worksheet, model )
		self._contentsModelToView = WeakKeyDictionary()
		self._contentsCell = Cell( self._computeContents )
		self._executionEnvironment = {}
		self.refreshResults()
		
		
	def refreshResults(self):
		self._executionEnvironment = {}
		for v in self.getContents():
			v._refreshResults( self._executionEnvironment )
		
		
	def getTitle(self):
		return self._model['title']
	
	def setTitle(self, title):
		self._model['title'] = title
		
		
	def getContents(self):
		return self._contentsCell.getValue()
	
	
	def appendContentsNode(self, node):
		self._model['contents'].append( node )
		
		
		
	def _computeContents(self):
		return [ self._viewOf( x )   for x in self._model['contents'] ]
	
	def _viewOf(self, model):
		try:
			return self._contentsModelToView[model]
		except KeyError:
			p = _projection( model, self )
			self._contentsModelToView[model] = p
			return p
		


class ParagraphView (WorksheetNodeView):
	def __init__(self, worksheet, model):
		super( ParagraphView, self ).__init__( worksheet, model )
	
		
	def getText(self):
		return self._model['text']
	
	def setText(self, text):
		self._model['text'] = text
		
	
	def getStyle(self):
		return self._model['style']
	
	def setStyle(self, style):
		self._model['style'] = style
		
		
	def _refreshResults(self, env):
		pass
		
		
		
class PythonCodeView (IncrementalOwner, WorksheetNodeView):
	def __init__(self, worksheet, model):
		WorksheetNodeView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None
		
		
	def getCode(self):
		return self._model['code']
	
	def setCode(self, code):
		self._model['code'] = code
	
	
	def getShowCode(self):
		return self._model['showCode'] == 'True'

	def setShowCode(self, bShowCode):
		self._model['showCode'] = 'True'   if bShowCode   else 'False'
		
		
	def getCodeEditable(self):
		return self._model['codeEditable'] == 'True'

	def setCodeEditable(self, bCodeEditable):
		self._model['codeEditable'] = 'True'   if bCodeEditable   else 'False'
		
		
	def getShowResult(self):
		return self._model['showResult'] == 'True'

	def setShowResult(self, bShowResult):
		self._model['showResult'] = 'True'   if bShowResult   else 'False'
		
		
	def getResult(self):
		self._incr.onAccess()
		return self._result
		
		
		
	def _refreshResults(self, env):
		self._result = Execution.executePythonModule( self.getCode(), '<worksheet>', env, self.getShowResult() )
		self._incr.onChanged()
	