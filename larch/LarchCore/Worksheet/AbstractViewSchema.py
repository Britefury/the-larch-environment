##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from weakref import WeakValueDictionary

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveFunction

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Projection import SubjectPath

from LarchCore.Kernel.python import inproc_kernel


class NodeAbstractView (object):
	def __init__(self, worksheet, model):
		self._worksheet = worksheet
		self._model = model
		
	def getModel(self):
		return self._model
	
	def isVisible(self):
		return True
	
	def __present__(self, fragment, inheritedState):
		return Pres.coerce( self._model )
	
	def _viewOf(self, model):
		return self._worksheet._viewOf( model )

	


class WorksheetAbstractView (NodeAbstractView):
	_projection = None

	def __init__(self, worksheet, model, importName, kernel_source):
		super( WorksheetAbstractView, self ).__init__( worksheet, model )
		self._modelToView = WeakValueDictionary()
		self._importName = importName
		self._kernel_source = kernel_source
		self._module = None
		self.refreshResults()
		
		
	def __init_module(self, module_init_callback):
		name = self._importName   if self._importName is not None   else 'worksheet'
		def _on_kernel_created(kernel):
			self._module = kernel.new_module(name)
			module_init_callback(self._module)
		self._kernel_source(_on_kernel_created)

		
	def refreshResults(self):
		body = self.getBody()
		body.clear_results()
		def on_module_initialised(module):
			body.refreshResults(module)
		self.__init_module(on_module_initialised)

		
	def getBody(self):
		return self._viewOf( self._model['body'] )
	
	
	def _viewOf(self, model):
		if model is self._model:
			return self
		else:
			key = id( model )
			try:
				return self._modelToView[key]
			except KeyError:
				# Try putting None in first, in case the view of a model is being created more than once
				self._modelToView[key] = None
				p = self._projection( model, self )
				self._modelToView[key] = p
				return p
		
		


class BodyAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		super( BodyAbstractView, self ).__init__( worksheet, model )
		self._contentsLive = LiveFunction( self._computeContents )


	def clear_results(self):
		for v in self.getContents():
			v._clear_results()

		
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


	def _clear_results(self):
		for x in self.getText():
			if not isinstance( x, str )  and  not isinstance( x, unicode ):
				x._clear_results()

	def _refreshResults(self, module):
		for x in self.getText():
			if not isinstance( x, str )  and  not isinstance( x, unicode ):
				x._refreshResults( module )


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

		
		
class TextSpanAbstractView (_TextAbstractView):
	def __init__(self, worksheet, model):
		super( TextSpanAbstractView, self ).__init__( worksheet, model )
		self._styleMapLive = LiveFunction( self.__compute_style_map )

		
	def getStyleAttrs(self):
		return self._styleMapLive.getValue()[0]


	def getStyleSheet(self):
		return self._styleMapLive.getValue()[1]



	_styleMap = {}
	_styleMap['italic'] = lambda value: Primitive.fontItalic( bool( value ) )
	_styleMap['bold'] = lambda value: Primitive.fontBold( bool( value ) )
	_styleMap['underline'] = lambda value: Primitive.fontUnderline( bool( value ) )
	_styleMap['strikethrough'] = lambda value: Primitive.fontStrikethrough( bool( value ) )

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



class LinkAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		super( LinkAbstractView, self ).__init__( worksheet, model )


	@property
	def text(self):
		return self._model['text']


	def _clear_results(self):
		pass

	def _refreshResults(self, module):
		pass


	def getSubject(self, docSubject):
		path = self._model['path']
		assert isinstance( path, SubjectPath )
		return path.followFrom( docSubject )




	
class PythonCodeAbstractView (NodeAbstractView):
	STYLE_MINIMAL_RESULT = 0
	STYLE_RESULT = 1
	STYLE_CODE_AND_RESULT = 2
	STYLE_CODE = 3
	STYLE_EDITABLE_CODE_AND_RESULT = 4
	STYLE_EDITABLE_CODE = 5
	STYLE_ERRORS = 6
	STYLE_HIDDEN = 7
	
	_styleToName  = { STYLE_MINIMAL_RESULT : 'minimal_result',
	                    STYLE_RESULT : 'result',
	                    STYLE_CODE_AND_RESULT : 'code_result',
	                    STYLE_CODE : 'code',
	                    STYLE_EDITABLE_CODE_AND_RESULT : 'editable_code_result',
	                    STYLE_EDITABLE_CODE : 'editable_code',
			    STYLE_ERRORS : 'errors',
	                    STYLE_HIDDEN : 'hidden' }
	
	_nameToStyle  = { 'minimal_result' : STYLE_MINIMAL_RESULT,
	                  'result' : STYLE_RESULT,
	                  'code_result' : STYLE_CODE_AND_RESULT,
	                  'code' : STYLE_CODE,
	                  'editable_code_result' : STYLE_EDITABLE_CODE_AND_RESULT,
	                  'editable_code' : STYLE_EDITABLE_CODE,
			  'errors' : STYLE_ERRORS,
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

	def isMinimal(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL_RESULT  or  style == self.STYLE_ERRORS

	def isVisible(self):
		style = self.getStyle()
		if style == self.STYLE_HIDDEN:
			return False
		elif style == self.STYLE_ERRORS:
			result = self.getResult()
			return result.hasErrors()
		else:
			return True

		
		
	def getResult(self):
		self._incr.onAccess()
		return self._result
		
		
		
	def _clear_results(self):
		self._result = None
		self._incr.onChanged()

	def _refreshResults(self, module):
		def result_callback(result):
			self._result = result
			self._incr.onChanged()

		module.execute( self.getCode(), self.isResultVisible(), result_callback )




class InlinePythonCodeAbstractView (NodeAbstractView):
	STYLE_MINIMAL_RESULT = 0
	STYLE_RESULT = 1
	STYLE_CODE_AND_RESULT = 2
	STYLE_EDITABLE_CODE_AND_RESULT = 3

	_styleToName  = { STYLE_MINIMAL_RESULT : 'minimal_result',
			  STYLE_RESULT : 'result',
			  STYLE_CODE_AND_RESULT : 'code_result',
			  STYLE_EDITABLE_CODE_AND_RESULT : 'editable_code_result' }

	_nameToStyle  = { 'minimal_result' : STYLE_MINIMAL_RESULT,
			  'result' : STYLE_RESULT,
			  'code_result' : STYLE_CODE_AND_RESULT,
			  'editable_code_result' : STYLE_EDITABLE_CODE_AND_RESULT }


	def __init__(self, worksheet, model):
		NodeAbstractView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None


	def getExpr(self):
		return self._model['expr']



	def getStyle(self):
		name = self._model['style']
		try:
			return self._nameToStyle[name]
		except KeyError:
			return self.STYLE_CODE_AND_RESULT


	def isCodeVisible(self):
		style = self.getStyle()
		return style == self.STYLE_CODE_AND_RESULT  or  style == self.STYLE_EDITABLE_CODE_AND_RESULT

	def isCodeEditable(self):
		style = self.getStyle()
		return style == self.STYLE_EDITABLE_CODE_AND_RESULT

	def isResultMinimal(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL_RESULT



	def getResult(self):
		self._incr.onAccess()
		return self._result



	def _clear_results(self):
		self._result = None
		self._incr.onChanged()

	def _refreshResults(self, module):
		self._result = inproc_kernel.getResultOfEvaluationWithinModule( self.getExpr(), module )
		self._incr.onChanged()




class InlineEmbeddedObjectAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		NodeAbstractView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )


	@property
	def value(self):
		return self._model['embeddedValue'].getValue()


	def _clear_results(self):
		pass

	def _refreshResults(self, module):
		pass




class ParagraphEmbeddedObjectAbstractView (NodeAbstractView):
	def __init__(self, worksheet, model):
		NodeAbstractView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )


	@property
	def value(self):
		return self._model['embeddedValue'].getValue()


	def _clear_results(self):
		pass

	def _refreshResults(self, module):
		pass
