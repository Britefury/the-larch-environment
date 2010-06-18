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

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from GSymCore.Languages.Python25 import Python25
from GSymCore.Languages.Python25.Execution import Execution

from GSymCore.Worksheet import Schema





class _Projection (object):
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
	
	def isVisible(self):
		return True
	
	def __present__(self, fragment, styleSheet, inheritedState):
		return fragment.presentFragmentWithGenericPerspective( self._model, styleSheet )


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
	
	
	def prependContentsNode(self, node):
		self._model['contents'].insert( 0, node )

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
	STYLE_MINIMAL_RESULT = 0
	STYLE_RESULT = 1
	STYLE_CODE_AND_RESULT = 2
	STYLE_CODE = 3
	STYLE_EDITABLE_CODE_AND_RESULT = 4
	STYLE_EDITABLE_CODE = 5
	STYLE_HIDDEN = 6
	
	_styleToName  = { STYLE_MINIMAL_RESULT : 'minimal_result',
	                    STYLE_RESULT : 'result',
	                    STYLE_CODE_AND_RESULT : 'code_result',
	                    STYLE_CODE : 'code',
	                    STYLE_EDITABLE_CODE_AND_RESULT : 'editable_code_result',
	                    STYLE_EDITABLE_CODE : 'editable_code',
	                    STYLE_HIDDEN : 'hidden' }
	
	_nameToStyle  = { 'minimal_result' : STYLE_MINIMAL_RESULT,
	                  'result' : STYLE_RESULT,
	                  'code_result' : STYLE_CODE_AND_RESULT,
	                  'code' : STYLE_CODE,
	                  'editable_code_result' : STYLE_EDITABLE_CODE_AND_RESULT,
	                  'editable_code' : STYLE_EDITABLE_CODE,
	                  'hidden' : STYLE_HIDDEN }
	
	
	def __init__(self, worksheet, model):
		WorksheetNodeView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None
		
		
	def getCode(self):
		return self._model['code']
	
	def setCode(self, code):
		self._model['code'] = code
		
		
		
	def getStyle(self):
		name = self._model['style']
		try:
			return self._nameToStyle[name]
		except KeyError:
			return self.STYLE_CODE_AND_RESULT
	
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name
		
		
	def isCodeVisible(self):
		style = self.getStyle()
		return style == self.STYLE_CODE  or  style == self.STYLE_CODE_AND_RESULT  or  style == self.STYLE_EDITABLE_CODE  or  style == self.STYLE_EDITABLE_CODE_AND_RESULT
		
	def isCodeEditable(self):
		style = self.getStyle()
		return style == self.STYLE_EDITABLE_CODE  or  style == self.STYLE_EDITABLE_CODE_AND_RESULT
	
	def isResultVisible(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL_RESULT  or  style == self.STYLE_RESULT  or  style == self.STYLE_CODE_AND_RESULT  or  style == self.STYLE_EDITABLE_CODE_AND_RESULT
		
	def isResultMinimal(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL_RESULT
	
	def isVisible(self):
		style = self.getStyle()
		return style != self.STYLE_HIDDEN
		
		
		
	def getResult(self):
		self._incr.onAccess()
		return self._result
		
		
		
	def _refreshResults(self, env):
		self._result = Execution.executePythonModule( self.getCode(), '<worksheet>', env, self.isResultVisible() )
		self._incr.onChanged()
		
		
	@staticmethod
	def newPythonCodeNode():
		return Schema.PythonCode( style='code_result', code=Python25.py25NewModule() )
	