##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from copy import deepcopy
import imp

from java.awt import Color

from BritefuryJ.Command import Command, CommandSet

from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *

from BritefuryJ.Controls import *

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.StyleSheet import *

from BritefuryJ.Util.Coroutine import Coroutine

from BritefuryJ.Live import LiveValue

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeWrapSelectedStatementRangeInEmbeddedObjectAction, makeInsertEmbeddedStatementAtCaretAction, chainActions
from LarchCore.Languages.Python25.Python25 import EmbeddedPython25Suite
from LarchCore.Languages.Python25.Execution import Execution



_headerStyle = StyleSheet.style( Primitive.fontBold( True ), Primitive.background( FillPainter( Color(1.0, 0.8, 0.9 ) ) ), Primitive.rowSpacing( 5.0 ) )
_forwardArrow = Arrow( Arrow.Direction.RIGHT, 12.0 )
_codeBorder = SolidBorder( 1.0, 2.0, 8.0, 8.0, Color( 0.75, 0.25, 0.375 ), None )
_stepperBorder = SolidBorder( 2.0, 2.0, 8.0, 8.0, Color( 0.5, 0.15, 0.25 ), None )
_breakPointBorder = SolidBorder( 1.0, 1.0, 5.0, 5.0, Color( 0.3, 0.3, 0.3 ), Color( 0.85, 0.85, 0.85 ) )
_breakPointCurrentBorder = SolidBorder( 1.0, 1.0, 5.0, 5.0, Color( 0.0, 0.4, 0.0 ), Color( 0.85, 1.0, 0.85 ) )
_breakPointStyle = StyleSheet.style( Primitive.fontSize( 10 ) )
_breakPointArrow = Arrow( Arrow.Direction.RIGHT, 8.0 )



class StepperCode (object):
	def __init__(self, stepper, suite):
		self._stepper = stepper
		self._suite = EmbeddedPython25Suite( suite )
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

def _regsiterStepper(coroutine, stepper):
	_stepperRegistry[coroutine] = stepper

def _unregisterStepper(coroutine):
	del _stepperRegistry[coroutine]

def _getStepperForCoroutine(coroutine):
	return _stepperRegistry.get( coroutine )



class Stepper (object):
	def __init__(self, suite=None):
		self._code = StepperCode( self, suite )
		self._stepButton = StepperButtonStep( self )
		self._runButton = StepperButtonRun( self )
		self._co = None
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
		self._stepButton = state['stepButton']
		self._runButton = state['runButton']
		self._co = None
		self.__currentBreakpoint = None
		self.__change_history__ = None


	def __get_trackable_contents__(self):
		return [ self._code ]


	def __initCoroutine(self):
		if self._co is None:
			def _run():
				module = imp.new_module( '<python_stepper>' )
				self._code._suite.executeWithinModule( module )
			self._co = Coroutine( _run )
			_regsiterStepper(self._co, self)
			self._setCurrentBreakpoint( None )


	def __py_replacement__(self):
		return deepcopy( self._code._suite.model['suite'] )


	def __py_exec__(self, globals, locals, codeGen):
		pass


	def reset(self):
		self._co = None
		self.__initCoroutine()


	def _onStep(self):
		self.__initCoroutine()
		bpAndState = self._co.yieldTo( self )
		if bpAndState is not None:
			breakPoint, stateView = bpAndState
			self._setCurrentBreakpoint( breakPoint )
			self._setStateView( stateView )
		else:
			self._setCurrentBreakpoint( None )
			self._setStateView( None )
		if self._co.isFinished():
			_unregisterStepper( self._co )


	def _onRun(self):
		self.__initCoroutine()
		while not self._co.isFinished():
			self._co.yieldTo( self )
		_unregisterStepper( self._co )


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
		header = _headerStyle( Row( [ title.alignVCentre(), self._stepButton, self._runButton ] ) )
		main = Column( [ header, Pres.coerce( self._code ).alignHExpand() ] )
		return _stepperBorder.surround( main ).withCommands( _stepperCommands )





def _newBreakPointAtCaret(caret):
	return StepperBreakpoint()


_breakPointAtCaret = makeInsertEmbeddedStatementAtCaretAction( _newBreakPointAtCaret )


_bpCommand = Command( '&Stepper &Break&point', _breakPointAtCaret )

_stepperCommands = CommandSet( 'LarchTools.PythonTools.Stepper.StepperBreakpoint', [ _bpCommand ] )



def _newStepperAtCaret(caret):
	return Stepper()

def _newStepperAtStatementRange(statements, selection):
	return Stepper( deepcopy( statements ) )


_stepperAtCaret = makeInsertEmbeddedStatementAtCaretAction( _newStepperAtCaret )
_stepperAtSelection = makeWrapSelectedStatementRangeInEmbeddedObjectAction( _newStepperAtStatementRange )


_psCommand = Command( '&Python &S&tepper', chainActions( _stepperAtSelection, _stepperAtCaret ) )

_psCommands = CommandSet( 'LarchTools.PythonTools.Stepper', [ _psCommand ] )

pythonCommands.registerCommandSet( _psCommands )
