##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************

"""
Python visual regular expression

Inspired by the visual regular expression presentation system in SWYN (Say What You Need), by Alan Blackwell.
"""

import re

from java.awt import Color

from BritefuryJ.Command import Command

from BritefuryJ.DocModel import DMNode

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres.Primitive import Paragraph, HiddenText

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedExpressionAtCaretAction, chainActions
from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.VisualRegex import Schema
from LarchTools.PythonTools.VisualRegex.Parser import VisualRegexGrammar
from LarchTools.PythonTools.VisualRegex.View import perspective as VREPerspective
from LarchTools.PythonTools.VisualRegex.CodeGenerator import VisualRegexCodeGenerator


_vreBorder = SolidBorder( 2.0, 4.0, 10.0, 10.0, Color( 0.7, 0.8, 0.7 ), None )


class VisualPythonRegex (object):
	_codeGen = VisualRegexCodeGenerator()


	def __init__(self, regex=None):
		if regex is None:
			regex = Schema.PythonRegEx( expr= Schema.UNPARSED( value=[ '' ] ) )

		if isinstance( regex, re._pattern_type ):
			# Extract pattern string
			regex = regex.pattern

		if isinstance( regex, str )  or  isinstance( regex, unicode ):
			# Convert to structural form
			g = VisualRegexGrammar()
			x = g.regex().parseStringChars( regex, None )
			regex = Schema.PythonRegEx( expr=x.value )

		if isinstance( regex, DMNode ):
			if not regex.isInstanceOf( Schema.PythonRegEx ):
				if regex.isInstanceOf( Schema.Node ):
					regex = Schema.PythonRegEx( expr=regex )
				else:
					raise TypeError, 'Wrong schema'

			self.regex = regex
		else:
			raise TypeError, 'Invalid regular expression type'


	def __py_evalmodel__(self, codeGen):
		s = self.asString()
		return Py.strToStrLiteral( s )


	def __clipboard_copy__(self, memo):
		return VisualPythonRegex(memo.copy(self.regex))


	def asString(self):
		return self._codeGen( self.regex )


	__embed_hide_frame__ = True


	def __present__(self, fragment, inherited_state):
		#return VREPerspective( self.regex )
		e = Paragraph( [ HiddenText( u'\ue000' ), VREPerspective( self.regex ), HiddenText( u'\ue000' ) ] )
		e = _vreBorder.surround( e )
		return e



@EmbeddedExpressionAtCaretAction
def _newVREAtCaret(caret):
	return VisualPythonRegex()



_vreCommand = Command( '&Visual &Regular &Expression', _newVREAtCaret )

pythonCommandSet( 'LarchTools.PythonTools.VisualRegex', [ _vreCommand ] )

