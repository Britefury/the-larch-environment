##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMNode
from BritefuryJ.DocModel import DMIOReader, DMIOWriter, DMModuleResolver

import string
import unittest
import cStringIO

import re

_whitespaceRegex = '[' + re.escape( string.whitespace ) + ']*'

def _deepList(xs):
	if isinstance( xs, java.util.List )  or  isinstance( xs, list ):
		return [ _deepList( x )   for x in xs ]
	else:
		return xs

class TreeParserTestCase (unittest.TestCase):
	class Resolver (DMModuleResolver):
		def __init__(self):
			self.modules = {}
			
		def getModule(self, location):
			return self.modules[location]
		
	resolver = Resolver()
	
	
	def _matchTest(self, parser, input, expected):
		result = parser.parseNode( input )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expected
		self.assert_( result.isValid() )
		
		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print value
		self.assert_( bSame )



	def _matchTestSX(self, parser, inputSX, expectedSX):
		expected = DMIOReader.readFromString( expectedSX, self.resolver )
		input = DMIOReader.readFromString( inputSX, self.resolver )
		
		result = parser.parseNode( input )

		if not result.isValid():
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
		self.assert_( result.isValid() )

		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
		self.assert_( bSame )

		
	def _matchFailTest(self, parser, input):
		result = parser.parseNode( input )
		
		if result.isValid():
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
		self.assert_( not result.isValid() )

		
	def _matchFailTestSX(self, parser, inputSX):
		input = DMIOReader.readFromString( inputSX, self.resolver )

		result = parser.parseNode( input )
		
		if result.isValid():
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
		self.assert_( not result.isValid() )
	