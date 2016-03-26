##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from copy import deepcopy
import imp

from java.awt import Color

from BritefuryJ.Command import Command, CommandSet

from BritefuryJ.Graphics import SolidBorder, FillPainter

from BritefuryJ.Controls import Button

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Arrow, Spacer, Box, Row, Column
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Util.Coroutine import Coroutine

from BritefuryJ.Live import LiveValue

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, WrapSelectedStatementRangeInEmbeddedObjectAction, EmbeddedStatementAtCaretAction, chainActions
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Suite



_headerStyle = StyleSheet.style( Primitive.fontBold( True ), Primitive.background( FillPainter( Color(1.0, 0.95, 0.8 ) ) ), Primitive.rowSpacing( 5.0 ) )
_forwardArrow = Arrow( Arrow.Direction.RIGHT, 12.0 )
_codeBorder = SolidBorder( 1.0, 2.0, 8.0, 8.0, Color( 0.75, 0.737, 0.675 ), None )
_stepperBorder = SolidBorder( 2.0, 2.0, 8.0, 8.0, Color( 0.55, 0.52, 0.4 ), None )
_breakPointBorder = SolidBorder( 1.0, 1.0, 5.0, 5.0, Color( 0.3, 0.3, 0.3 ), Color( 0.85, 0.85, 0.85 ) )
_breakPointCurrentBorder = SolidBorder( 1.0, 1.0, 5.0, 5.0, Color( 0.0, 0.4, 0.0 ), Color( 0.85, 1.0, 0.85 ) )
_breakPointStyle = StyleSheet.style( Primitive.fontSize( 10 ) )
_breakPointArrow = Arrow( Arrow.Direction.RIGHT, 8.0 )



class StepperCode (object):
	def __init__(self, stepper, suite):
		self._stepper = stepper
		self._suite = EmbeddedPython2Suite( suite )
		self.__change_history__ = None


	def __getstate__(self):
		return { 'stepper' : self._stepper, 'suite' : self._suite }

	def __setstate__(self, state):
		self._stepper = state['stepper']
		self._suite = state['suite']
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._suite ]



	def __present__(self, fragment, inheritedState):
		return _codeBorder.surround( self._suite )




class StepperButton (object):
	def __init__(self, stepper):
		self._stepper = stepper


	def __getstate__(self):
		return { 'stepper' : self._stepper }

	def __setstate__(self, state):
		self._stepper = state['stepper']



class StepperButtonStep (StepperButton):
	def __present__(self, fragment, inheritedState):
		def _onClick(button, event):
			self._stepper._onStep()

		return Button( Row( [ Box( 4.0, 0.0 ).alignVExpand(), Spacer( 4.0, 0.0 ), _forwardArrow ] ).pad( 2.0, 2.0 ), _onClick ).alignVCentre()



class StepperButtonRun (StepperButton):
	def __present__(self, fragment, inheritedState):
		def _onClick(button, event):
			self._stepper._onRun()

		return Button( Row( [ _forwardArrow, Spacer( 2.0, 0.0 ), _forwardArrow ] ).pad( 2.0, 2.0 ), _onClick ).alignVCentre()




class StepperBreakpoint (object):
	def __init__(self):
		self.__change_history__ = None
		self._isCurrent = LiveValue( False )

	def __getstate__(self):
		return { None : None }

	def __setstate__(self, state):
		self.__change_history__ = None
		self._isCurrent = LiveValue( False )


	def __get_trackable_contents__(self):
		return []


	def __py_exec__(self, globals, locals, codeGen):
		co = Coroutine.getCurrent()
		stepper = _getStepperForCoroutine( co )
		if stepper is not None:
			stateView = self._createStateView()
			sourceStepper = stepper._co.yieldToParent( (self, stateView) )



	def _setAsNotCurrent(self):
		self._isCurrent.setLiteralValue( False )


	def _setAsCurrent(self):
		self._isCurrent.setLiteralValue( True )


	def _createStateView(self):
		print 'StepperBreakpoint._createStateView: not implemented'
		return None


	def __present__(self, fragment, inheritedState):
		current = self._isCurrent.getValue()
		label = _breakPointStyle( Label( 'BREAKPOINT' ) )
		if current:
			return _breakPointCurrentBorder.surround( Row( [ _breakPointArrow, Spacer( 3.0, 0.0, ), label ] ).alignVCentre() )
		else:
			return _breakPointBorder.surround( label )





_stepperRegistry = {}

def _registerStepper(coroutine, stepper):
	_stepperRegistry[coroutine] = stepper

def _unregisterStepper(coroutine):
	del _stepperRegistry[coroutine]

def _getStepperForCoroutine(coroutine):
	return _stepperRegistry.get( coroutine )



class Stepper (object):
	def __init__(self, suite=None):
		self._code = StepperCode( self, suite )
		self._compiledCode = None
		self._scope = {}
		self._stepButton = StepperButtonStep( self )
		self._runButton = StepperButtonRun( self )
		self._co = None
		self._finished = False
		self.__currentBreakpoint = None
		self.__change_history__ = None


	def __del__(self):
		if self._co is not None:
			self._co.terminate()
			_unregisterStepper( self._co )
			self._co = None


	def __getstate__(self):
		return { 'code' : self._code, 'stepButton' : self._stepButton, 'runButton' : self._runButton }

	def __setstate__(self, state):
		self._code = state['code']
		self._compiledCode = None
		self._scope = {}
		self._stepButton = state['stepButton']
		self._runButton = state['runButton']
		self._co = None
		self._finished = False
		self.__currentBreakpoint = None
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._code ]


	def __initCoroutine(self):
		if self._co is None:
			def _run():
				module = imp.new_module( '<python_stepper>' )
				module.__dict__.update( self._scope )
				exec self._compiledCode in module.__dict__
			self._co = Coroutine( _run )
			_registerStepper( self._co, self )
			self._setCurrentBreakpoint( None )


	def __py_replacement__(self):
		return deepcopy( self._code._suite.model['suite'] )


	def __py_compile_visit__(self, codeGen):
		self._compiledCode = codeGen.compileForExecution( self._code._suite.model )


	def __py_exec__(self, globalVars, localVars, codeGen):
		self.reset()
		self._scope = {}
		self._scope.update( globalVars )
		self._scope.update( localVars )


	def reset(self):
		self._co = None
		self._finished = False
		self.__initCoroutine()


	def _onStep(self):
		if not self._finished:
			self.__initCoroutine()
			bpAndState = self._co.yieldTo( self )
			if bpAndState is not None:
				breakPoint, stateView = bpAndState
				self._setCurrentBreakpoint( breakPoint )
				self._setStateView( stateView )
			else:
				self._setCurrentBreakpoint( None )
				self._setStateView( None )
			if self._co.isFinished()  and  not self._finished:
				_unregisterStepper( self._co )
				self._finished = True


	def _onRun(self):
		if not self._finished:
			self.__initCoroutine()
			while not self._co.isFinished():
				self._co.yieldTo( self )
			_unregisterStepper( self._co )
			self._finished = True


	def _setCurrentBreakpoint(self, breakPoint):
		if self.__currentBreakpoint is not None:
			self.__currentBreakpoint._setAsNotCurrent()

		self.__currentBreakpoint = breakPoint

		if self.__currentBreakpoint is not None:
			self.__currentBreakpoint._setAsCurrent()


	def _setStateView(self, stateView):
		print 'Stepper._setStateView: not implemented'


	def __present__(self, fragment, inheritedState):
		title = Label( 'Code stepper' )
		header = _headerStyle( Row( [ title.alignVCentre().alignHPack(), Pres.coerce( self._stepButton ).alignHPack(), Pres.coerce( self._runButton ).alignHPack() ] ) ).alignHExpand()
		main = Column( [ header, Pres.coerce( self._code ).alignHExpand() ] )
		return _stepperBorder.surround( main ).withCommands( _stepperCommands )




@EmbeddedStatementAtCaretAction
def _newBreakPointAtCaret(caret):
	return StepperBreakpoint()



_bpCommand = Command( '&Stepper &Break&point', _newBreakPointAtCaret )

_stepperCommands = CommandSet( 'LarchTools.PythonTools.Stepper.StepperBreakpoint', [ _bpCommand ] )



@EmbeddedStatementAtCaretAction
def _newStepperAtCaret(caret):
	return Stepper()

@WrapSelectedStatementRangeInEmbeddedObjectAction
def _newStepperAtStatementRange(statements, selection):
	return Stepper( deepcopy( statements ) )



_psCommand = Command( '&Python &S&tepper', chainActions( _newStepperAtStatementRange, _newStepperAtCaret ) )

pythonCommandSet( 'LarchTools.PythonTools.Stepper', [ _psCommand ] )

