##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent.Combinators.Primitive import Span


def _numParensRequired(precedence, outerPrecedence):
	if precedence is not None  and  outerPrecedence is not None  and  precedence > outerPrecedence:
		return 1
	else:
		return 0


def applyParens(child, precedence, outerPrecedence, numAdditionalParens, openParen, closeParen):
	required = _numParensRequired( precedence, outerPrecedence )
	numParens = required + numAdditionalParens
	if numParens == 0:
		return child
	else:
		return Span( [ openParen   for i in xrange( 0, numParens ) ]  +  [ child ]  +  [ closeParen   for i in xrange( 0, numParens ) ] )
