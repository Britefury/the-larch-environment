##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import gtk

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.GLisp.GLispUtil import isGLispList
from Britefury.GLisp.GLispCompiler import GLispCompilerInvalidFormType, GLispCompilerVariableNameMustStartWithAt, compileExpressionListToPyTreeStatements
from Britefury.GLisp.PyCodeGen import PyCodeGenError, PySrc, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyIsInstance, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.View.InteractorEvent import InteractorEvent, InteractorEventKey, InteractorEventTokens


class InvalidKeySpecification (PyCodeGenError):
	pass

class InvalidKeySymbol (PyCodeGenError):
	pass

class InvalidKeyModifier (PyCodeGenError):
	pass

class InvalidTokenSpecifier (PyCodeGenError):
	pass

class InvalidEventType (PyCodeGenError):
	pass



class _EventSpec (object):
	def __init__(self, xs):
		super( _EventSpec, self ).__init__()
		self.srcXs = xs
		
	
	def _p_conditionWrap(self, outerTreeFactory, py_condition):
		return lambda innerTrees: outerTreeFactory( [ py_condition.ifTrue( innerTrees ).debug( self.srcXs ) ] )

	@abstractmethod
	def compileToPyTree(self, py_eventExpr, py_actionStatements, bindings):
		pass




class _KeyEventSpec (_EventSpec):
	_modTable = { 'control' : gtk.gdk.CONTROL_MASK, 
		      'ctrl' : gtk.gdk.CONTROL_MASK, 
		      'shift' : gtk.gdk.SHIFT_MASK, 
		      'alt' : gtk.gdk.MOD1_MASK, 
		      'mod1' : gtk.gdk.MOD1_MASK }
	
	def __init__(self, xs):
		"""
		( $key <key_value> <mods...> )
		"""
		super( _KeyEventSpec, self ).__init__( xs )
		assert isGLispList( xs )
		
		if len( xs ) < 2:
			raise InvalidKeySpecification( xs )
		
		try:
			self.keyValue = getattr( gtk.keysyms, xs[1] )
		except AttributeError:
			raise InvalidKeySymbol( xs )
		
		mods = [ self._modTable.get( mod )   for mod in xs[2:] ]
		mods = [ mod   for mod in mods   if mod is not None ]
		self.mods = reduce( lambda x, y: x | y,  mods,  0 )
		
		
	def compileToPyTreeFactory(self, py_eventExpr, outerTreeFactory, bindings):
		treeFac = outerTreeFactory

		# Check the event type
		treeFac = self._p_conditionWrap( treeFac, py_eventExpr.isinstance_( PyVar( '_InteractorEventKey' ) ) )
		
		# Check the key value and modifiers
		treeFac = self._p_conditionWrap( treeFac, ( py_eventExpr.attr( 'keyValue' )  ==  self.keyValue ).and_( py_eventExpr.attr( 'mods' )  ==  self.mods ) )
		
		return treeFac
		

		
		
class _TokenListEventSpec (_EventSpec):
	class TokenEventSpec (object):
		def __init__(self, xs):
			super( _TokenListEventSpec.TokenEventSpec, self ).__init__()
			
			self.srcXs = xs

			if isGLispList( xs ):
				if xs[0] != ':':
					raise InvalidTokenSpecifier( token )
				if xs[1][0] != '@':
					raise GLispCompilerVariableNameMustStartWithAt( token )
				varName = xs[1][1:]
				self.bindName = varName,
				self.tokenClass = xs[2]
			else:
				self.bindName = None
				self.tokenClass = xs
				
	
			
		def _p_conditionWrap(self, outerTreeFactory, py_condition):
			return lambda innerTrees: outerTreeFactory( [ py_condition.ifTrue( innerTrees ).debug( self.srcXs ) ] )

		def compileToPyTreeFactory(self, py_eventTokenExpr, outerTreeFactory, bindings):
			if self.bindName is not None:
				bindings[self.bindName] = py_eventTokenExpr.attr( 'value' )
			return self._p_conditionWrap( outerTreeFactory, py_eventTokenExpr.attr( 'tokenClass' ) == PyLiteral( self.tokenClass ) )


		
	def __init__(self, xs):
		super( _TokenListEventSpec, self ).__init__( xs )
		
		self.tokenSpecs = [ self.TokenEventSpec( x )   for x in xs[1:] ]
				

	def compileToPyTreeFactory(self, py_eventExpr, outerTreeFactory, bindings):
		treeFac = outerTreeFactory
		
		# Check the event type
		treeFac = self._p_conditionWrap( treeFac, py_eventExpr.isinstance_( PyVar( '_InteractorEventTokenList' ) ) )
		
		# Check the tokens
		for i, tokenSpec in enumerate( self.tokenSpecs ):
			treeFac = tokenSpec.compileToPyTreeFactory( py_eventExpr.attr( 'tokens' )[i], treeFac, bindings )
			
		return treeFac
		
		
		



def _compileInteractorMatch(srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree, bMatchedName, py_eventExpr, bFirst):
	eventSpecXs = srcXs[0]
	actionXs = srcXs[1:]
	bindings = {}
	

	# Compile the event conditions
	
	# Build the spec
	if not isGLispList( eventSpecXs ):
		raise GLispCompilerInvalidFormType( eventSpecXs )
	
	if eventSpecXs[0] == '$key':
		spec = _KeyEventSpec( eventSpecXs )
	elif eventSpecXs[0] == '$tokens':
		spec = _TokenListEventSpec( eventSpecXs )
	else:
		raise InvalidEventType( eventSpecXs )
	
	matchTreeFac = spec.compileToPyTreeFactory( py_eventExpr, lambda innerTrees: innerTrees, bindings )
		
	
	
	# Compile the action statements
	actionContext = context.innerContext()

	# Action function name
	actionFnName = actionContext.temps.allocateTempName( 'interactor_fn' )

	# Build the action tree
	actionFnContext = context.innerContext()
	# Bind variables (in alphabetical order)
	bindingPairs = bindings.items()
	bindingPairs.sort( lambda x, y: cmp( x[0], y[0] ) )
		
	# Action expression code
	py_actionStmts, py_actionResultStore = compileExpressionListToPyTreeStatements( actionXs, actionFnContext, False, compileSpecial )
	actionFnContext.body.extend( py_actionStmts )
	
	# Make a function define
	py_actionFn = PyDef( actionFnName, [ pair[0]   for pair in bindingPairs ], actionFnContext.body, dbgSrc=srcXs )
	py_actionFnCall = PyVar( actionFnName, dbgSrc=srcXs )( *[ pair[1].debug( srcXs )   for pair in bindingPairs ] ).debug( srcXs )
	
	# Matched
	py_matchedTrue = pyt_coerce( True ).assignTo_sideEffects( PyVar( bMatchedName )[0] ).debug( srcXs )
	
	py_action = [ py_actionFn,  py_actionFnCall,  py_matchedTrue ]
	
	
	py_match = matchTreeFac( py_action )
	
	if not bFirst:
		py_match = [ PySimpleIf( PyVar( bMatchedName )[0].not_(), py_match ).debug( srcXs ) ]
		
	return py_match




def compileInteractor(srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
	#
	#( $interactor <interactor_specs...> )
	#
	# interactor_spec:
	#    ( <event_spec>  <actions...> )
	#
	# event_spec:
	# key:
	#    ( $key <key_value> <mods...> )    - key event
	# tokens:
	#    ( $tokens <token_specs...> )     - token list; consumes the tokens specified in the list
	#
	# token_spec:
	#    token_class        - the token class name
	# or:
	#    (: @var token_class)     - look for a token with the specified class, and bind the value to a variable called 'var'
	assert srcXs[0] == '$interactor'
	
	onEventFnName = context.temps.allocateTempName( 'interactor_on_event_fn' )
	eventName = context.temps.allocateTempName( 'interactor_event' )
	
	py_interactor = []

	
	bMatchedName = context.temps.allocateTempName( 'interactor_bMatched' )
	py_initBMatched = PyVar( bMatchedName ).assign_sideEffects( [ False ] ).debug( matchXs )
	py_interactor.append( py_initBMatched )

	bFirst = True
	for xs in srcXs[1:]:
		py_match = _compileInteractorMatch( xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree, bMatchedName, PyVar( eventName ).debug( srcXS ), bFirst )
		py_interactor.extend( py_match )
		bFirst = False
	
	py_interactorOnEventFn = PyDef( onEventFnName, [ eventName ], py_interactor ).debug( srcXs )
	

	interactorFactoryFnName = context.temps.allocateTempName( 'interactor_factory_fn' )
	py_interactorFactoryFn = PyDef( interactorFactoryFnName, [], [ py_interactorOnEventFn, PyVar( onEventFnName ).return_().debug( srcXs ) ] ).debug( srcXs )
	
	context.body.append( py_interactorFactoryFn )
	
	if bNeedResult:
		return PyVar( interactorFactoryFnName )().debug( srcXs )
	else:
		return None

	
