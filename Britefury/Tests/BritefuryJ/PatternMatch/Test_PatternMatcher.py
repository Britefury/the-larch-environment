##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.PatternMatch import *

from BritefuryJ.DocModel import DMNode, DMIOReader


import unittest



from Britefury.gSym.gSymWorld import GSymWorld

class TestCase_PatternMatcher (unittest.TestCase):
	def test_lerpRefactor(self):
		oneMinusT = MatchExpression.toMatchExpression( [ '-', '1.0', 't' << Anything() ] )
		bTimesT = MatchExpression.toMatchExpression( [ '*', 'b' << Anything(), 't' << Anything() ] )
		aTimesOneMinusT = MatchExpression.toMatchExpression( [ '*', 'a' << Anything(), oneMinusT ] )
		lerp = MatchExpression.toMatchExpression( [ '+', aTimesOneMinusT, bTimesT ] )
		lerpRefactor = lerp.action( lambda input, begin, x, bindings: [ '+', bindings['a'], [ '*', [ '-', bindings['b'], bindings['a'] ], bindings['t'] ] ] )
		
		data = DMIOReader.readFromString( '(+ (* p (- 1.0 x)) (* q x))', GSymWorld.getInternalResolver() )
		expected = DMIOReader.readFromString( '(+ p (* (- q p) x))', GSymWorld.getInternalResolver() )
		result = lerpRefactor.parseNode( data )
		self.assert_( DMNode.coerce( result.getValue() )  ==  DMNode.coerce( expected ) )
		