##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
__author__ = 'Geoff'

from copy import deepcopy

from java.awt import Color

from BritefuryJ.Command import Command, CommandSet

from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *

from BritefuryJ.Controls import *

from BritefuryJ.Pres.Primitive import *
from BritefuryJ.StyleSheet import *

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeWrapSelectedStatementRangeInEmbeddedObjectAction, makeInsertEmbeddedStatementAtCaretAction, chainActions
from LarchCore.Languages.Python25.Python25 import EmbeddedPython25Suite



_headerStyle = StyleSheet.style( Primitive.fontBold( True ), Primitive.background( FillPainter( Color(1.0, 0.8, 0.9 ) ) ), Primitive.rowSpacing( 5.0 ) )
_forwardArrow = Arrow( Arrow.Direction.RIGHT, 12.0 )
_codeBorder = SolidBorder( 1.0, 2.0, 8.0, 8.0, Color( 0.75, 0.25, 0.375 ), None )
_stepperBorder = SolidBorder( 2.0, 2.0, 8.0, 8.0, Color( 0.5, 0.15, 0.25 ), None )



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




class Stepper (object):
	def __init__(self, suite=None):
		self._code = StepperCode( self, suite )
		self._stepButton = StepperButtonStep( self )
		self._runButton = StepperButtonRun( self )


	def __getstate__(self):
		return { 'stepper' : self._stepper, 'suite' : self._suite }

	def __setstate__(self, state):
		self._stepper = state['stepper']
		self._suite = state['suite']
		self.__change_history__ = None


	def _onStep(self):
		print 'Step'


	def _onRun(self):
		print 'Run'


	def __present__(self, fragment, inheritedState):
		title = Label( 'Code stepper' )
		header = _headerStyle( Row( [ title.alignVCentre(), self._stepButton, self._runButton ] ) )
		main = Column( [ header, self._code.alignHExpand() ] )
		return _stepperBorder.surround( main )




def _newStepperAtCaret(caret):
	return Stepper()

def _newStepperAtStatementRange(statements, selection):
	return Stepper( deepcopy( statements ) )


_suiteAtCaret = makeInsertEmbeddedStatementAtCaretAction( _newStepperAtCaret )
_suiteAtSelection = makeWrapSelectedStatementRangeInEmbeddedObjectAction( _newStepperAtStatementRange )


_psCommand = Command( '&Python &S&tepper', chainActions( _suiteAtSelection, _suiteAtCaret ) )

_psCommands = CommandSet( 'LarchTools.PythonTools.Stepper', [ _psCommand ] )

pythonCommands.registerCommandSet( _psCommands )
