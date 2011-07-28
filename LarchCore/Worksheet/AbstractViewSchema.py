##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from weakref import WeakValueDictionary

import imp

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.IncrementalUnit import Unit

from BritefuryJ.Pres import InnerFragment


from Britefury import LoadBuiltins

from LarchCore.Languages.Python25.Execution import Execution





class NodeAbstractView (object):
	def __init__(self, worksheet, model):
		self._worksheet = worksheet
		self._model = model
		
	def getModel(self):
		return self._model
	
	def isVisible(self):
		return True
	
	def __present__(self, fragment, inheritedState):
		return InnerFragment( self._model )
	
	def _viewOf(self, model):
		return self._worksheet._viewOf( model )

	


class WorksheetAbstractView (NodeAbstractView):
	_projection = None

	def __init__(self, worksheet, model):
		super( WorksheetAbstractView, self ).__init__( worksheet, model )
		self._modelToView = WeakValueDictionary()
		self.refreshResults()
		
		
	def _initModule(self):
		self._module = imp.new_module( 'worksheet' )
		LoadBuiltins.loadBuiltins( self._module )
		
		
	def refreshResults(self):
		self._initModule()
		body = self.getBody()
		body.refreshResults( self._module )
	
		
	def getModule(self):
		return self._module
		
		
	def getBody(self):
		return self._viewOf( self._model['body'] )
	
	
	def _viewOf(self, model):
		key = id( model )
		try:
			return self._modelToView[key]
		except KeyError:
			p = self._projection( model, self )
			self._modelToView[key] = p
			return p
		
		


class BodyAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		super( BodyAbstractView, self ).__init__( worksheet, model )
		self._contentsUnit = Unit( self._computeContents )
		
		
	def refreshResults(self, module):
		for v in self.getContents():
			v._refreshResults( module )
		
		
	def getContents(self):
		return self._contentsUnit.getValue()
	
	
	def _computeContents(self):
		return [ self._viewOf( x )   for x in self._model['contents'] ]
	
		


class ParagraphAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		super( ParagraphAbstractView, self ).__init__( worksheet, model )
	
		
	def getText(self):
		return self._model['text']
	

	def getStyle(self):
		return self._model['style']
	

	def _refreshResults(self, module):
		pass

		
		
class TextSpanAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		super( TextSpanAbstractView, self ).__init__( worksheet, model )
	
		
	def getText(self):
		return self._model['text']

	
	def getStyleAttrs(self):
		return self._model['styleAttrs']

		
	def _refreshResults(self, module):
		pass

		
		
class PythonCodeAbstractView (NodeAbstractView):
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
		NodeAbstractView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None
		
		
	def getCode(self):
		return self._model['code']

		
		
	def getStyle(self):
		name = self._model['style']
		try:
			return self._nameToStyle[name]
		except KeyError:
			return self.STYLE_CODE_AND_RESULT

		
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
		
		
		
	def _refreshResults(self, module):
		self._result = Execution.executePythonModule( self.getCode(), module, self.isResultVisible() )
		self._incr.onChanged()

	
	
class QuoteLocationAbstractView (NodeAbstractView):
	STYLE_MINIMAL = 0
	STYLE_NORMAL = 1
	
	_styleToName  = { STYLE_MINIMAL : 'minimal',
	                    STYLE_NORMAL : 'normal' }
	
	_nameToStyle  = { 'minimal' : STYLE_MINIMAL,
	                  'normal' : STYLE_NORMAL }
	
	
	def __init__(self, worksheet, model):
		NodeAbstractView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None
		
		
	def getLocation(self):
		return self._model['location']
	

		
	def getStyle(self):
		name = self._model['style']
		try:
			return self._nameToStyle[name]
		except KeyError:
			return self.STYLE_NORMAL
	

	def isMinimal(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL
		
		
	def _refreshResults(self, module):
		pass
