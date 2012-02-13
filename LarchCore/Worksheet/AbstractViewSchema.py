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
from BritefuryJ.Live import LiveFunction

from BritefuryJ.Pres import InnerFragment
from BritefuryJ.Pres.Primitive import Primitive
from BritefuryJ.StyleSheet import StyleSheet


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

	def __init__(self, worksheet, model, importName):
		super( WorksheetAbstractView, self ).__init__( worksheet, model )
		self._modelToView = WeakValueDictionary()
		self._importName = importName
		self.refreshResults()
		
		
	def _initModule(self):
		name = self._importName   if self._importName is not None   else 'worksheet'
		self._module = imp.new_module( name )
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
		self._contentsLive = LiveFunction( self._computeContents )
		
		
	def refreshResults(self, module):
		for v in self.getContents():
			v._refreshResults( module )
		
		
	def getContents(self):
		return self._contentsLive.getValue()
	
	
	def _computeContents(self):
		return [ self._viewOf( x )   for x in self._model['contents'] ]
	
		


class _TextAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		super( _TextAbstractView, self ).__init__( worksheet, model )
		self._textLive = LiveFunction( self._computeText )


	def getText(self):
		return self._textLive.getValue()


	def _refreshResults(self, module):
		pass


	def _computeText(self):
		return [ x   if isinstance( x, str ) or isinstance( x, unicode )   else self._viewOf( x )   for x in self._model['text'] ]


	@staticmethod
	def _textToModel(text):
		return [ ( x   if isinstance( x, str ) or isinstance( x, unicode )   else x.getModel() )   for x in text ]



class ParagraphAbstractView (_TextAbstractView):
	def __init__(self, worksheet, model):
		super( ParagraphAbstractView, self ).__init__( worksheet, model )

		
	def getStyle(self):
		return self._model['style']
	

	def _refreshResults(self, module):
		pass

		
		
class TextSpanAbstractView (_TextAbstractView):
	def __init__(self, worksheet, model):
		super( TextSpanAbstractView, self ).__init__( worksheet, model )
		self._styleMapLive = LiveFunction( self.__compute_style_map )

		
	def getStyleAttrs(self):
		return self._styleMapLive.getValue()[0]


	def getStyleSheet(self):
		return self._styleMapLive.getValue()[1]



	def _refreshResults(self, module):
		pass

		
	_styleMap = {}
	_styleMap['italic'] = lambda value: Primitive.fontItalic( bool( value ) )
	_styleMap['bold'] = lambda value: Primitive.fontBold( bool( value ) )

	def __compute_style_map(self):
		attrs = self._model['styleAttrs']

		m = {}
		styles = []

		for a in attrs:
			key = a['name']
			value = a['value']

			m[key] = value
			styles.append( self._styleMap[key]( value ) )

		return m, StyleSheet.style( *styles )



	
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
		self._result = Execution.getResultOfExecutionWithinModule( self.getCode(), module, self.isResultVisible() )
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




class InlineEmbeddedObjectAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		NodeAbstractView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None


	def getValue(self):
		return self._model['embeddedValue'].getValue()


	def _refreshResults(self, module):
		pass




class ParagraphEmbeddedObjectAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		NodeAbstractView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None


	def getValue(self):
		return self._model['embeddedValue'].getValue()


	def _refreshResults(self, module):
		pass
