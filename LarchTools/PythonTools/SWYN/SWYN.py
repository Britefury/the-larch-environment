##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import re

from BritefuryJ.Command import *

from BritefuryJ.DocModel import DMNode

from BritefuryJ.Pres.Primitive import *

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedExpressionAtCaretAction, chainActions

from LarchTools.PythonTools.SWYN import Schema
from LarchTools.PythonTools.SWYN.Parser import SWYNGrammar
from LarchTools.PythonTools.SWYN.View import perspective as SWYNPerspective
from LarchTools.PythonTools.SWYN.CodeGenerator import SWYNCodeGenerator



class SWYN (object):
	_codeGen = SWYNCodeGenerator()


	def __init__(self, regex=None):
		if regex is None:
			regex = Schema.SWYNRegEx( expr= Schema.UNPARSED( value=[ '' ] ) )

		if isinstance( regex, re._pattern_type ):
			# Extract pattern string
			regex = regex.pattern

		if isinstance( regex, str )  or  isinstance( regex, unicode ):
			# Convert to structural form
			g = SWYNGrammar()
			x = g.regex().parseStringChars( regex, None )
			regex = Schema.SWYNRegEx( expr=x.value )

		if isinstance( regex, DMNode ):
			if not regex.isInstanceOf( Schema.SWYNRegEx ):
				if regex.isInstanceOf( Schema.Node ):
					regex = Schema.SWYNRegEx( expr=regex )
				else:
					raise TypeError, 'Wrong schema'

			self.regex = regex
		else:
			raise TypeError, 'Invalid regular expression type'


	def __py_eval__(self, _globals, _locals, codeGen):
		return self._codeGen( self.regex )


	def __present__(self, fragment, inherited_state):
		#return SWYNPerspective( self.regex )
		return Paragraph( [ HiddenText( u'\ue000' ), SWYNPerspective( self.regex ), HiddenText( u'\ue000' ) ] )



def _newSWYNAtCaret(caret):
	return SWYN()

_exprAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newSWYNAtCaret )


_swynCommand = Command( '&S&W&Y&N', _exprAtCaret )

_swynCommands = CommandSet( 'LarchTools.PythonTools.SWYN', [ _swynCommand ] )

pythonCommands.registerCommandSet( _swynCommands )
