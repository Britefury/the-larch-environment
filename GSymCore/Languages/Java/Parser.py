##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


from BritefuryJ.Parser import *
from BritefuryJ.Parser.Utils import *
from BritefuryJ.Parser.Utils.OperatorParser import Prefix, Suffix, InfixLeft, InfixRight, PrecedenceLevel, OperatorTable


from Britefury.Grammar.Grammar import Grammar, Rule


class JavaGrammar (Grammar):
	decimalInteger = RegEx( r"[\-]?[1-9][0-9]*" )  |  Literal( "0" )
	hexInteger = RegEx( r"0[xX][0-9A-Fa-f]+" )
	octalInteger = RegEx( r"0[0-7]+" )
	
	
	
	@Rule
	def identifier(self):
		return Tokens.identifier
	
	@Rule
	def qualifiedIdentifier(self):
		return 
	
	
	
	
import unittest
from Britefury.Tests.BritefuryJ.Parser import ParserTestCase


class TestCase_JavaGrammar (ParserTestCase.ParserTestCase):
	def test_identifier(self):
		g = JavaGrammar()
		self._matchTest( g.identifier(), 'abc', 'abc' )
	
