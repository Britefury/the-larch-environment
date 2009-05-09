##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.TreeParser import *

from BritefuryJ.DocModel import DMNode, DMIOReader

from Britefury.Tests.BritefuryJ.TreeParser.TreeParserTestCase import TreeParserTestCase


import unittest



from Britefury.gSym.gSymWorld import GSymWorld

class TestCase_PatternMatcher (TreeParserTestCase):
	def test_lerpRefactor(self):
		oneMinusT = TreeParserExpression.coerce( [ '-', '1.0', Anything().bindTo( 't' ) ] )
		bTimesT = TreeParserExpression.coerce( [ '*', Anything().bindTo( 'b' ), Anything().bindTo( 't' ) ] )
		aTimesOneMinusT = TreeParserExpression.coerce( [ '*', Anything().bindTo( 'a' ), oneMinusT ] )
		lerp = TreeParserExpression.coerce( [ '+', aTimesOneMinusT, bTimesT ] )
		lerpRefactor = lerp.action( lambda input, x, bindings: [ '+', bindings['a'], [ '*', [ '-', bindings['b'], bindings['a'] ], bindings['t'] ] ] )
		
		
		#data = DMIOReader.readFromString( '[+ [* p [- 1.0 x]] [* q x]]', GSymWorld.getInternalResolver() )
		#expected = DMIOReader.readFromString( '[+ p [* [- q p] x]]', GSymWorld.getInternalResolver() )
		#result = lerpRefactor.parseNode( data )
		#self.assert_( DMNode.coerce( result.getValue() )  ==  DMNode.coerce( expected ) )
		
		self._matchTestSX( lerpRefactor, '[+ [* p [- 1.0 x]] [* q x]]', '[+ p [* [- q p] x]]' )
		