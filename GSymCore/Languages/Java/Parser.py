##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


from BritefuryJ.Parser import *
from BritefuryJ.Parser.Utils import *
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, UnaryOperator, BinaryOperator, OperatorTable


from Britefury.Grammar.Grammar import Grammar, Rule


from GSymCore.Languages.Java import Keywords
from GSymCore.Languages.Java import NodeClasses as Nodes



class JavaGrammar (Grammar):
	decimalInteger = RegEx( r"[\-]?[1-9][0-9]*" )  |  Literal( "0" )
	hexInteger = RegEx( r"0[xX][0-9A-Fa-f]+" )
	octalInteger = RegEx( r"0[0-7]+" )
	
	
	
	@Rule
	def identifier(self):
		return Tokens.javaIdentifier
	
	@Rule
	def qualifiedIdentifier(self):
		return ( self.identifier()  +  ( Literal( '.' )  +  self.identifier() ).zeroOrMore() ).action( lambda input, begin, end, x, bindings: [ x[0] ] + [ a[1]   for a in x[1] ] )
	
	
	# Integer literal
	@Rule
	def decimalIntLiteral(self):
		return Tokens.decimalIntegerNoOctal.action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='decimal', numType='int', value=x ) )

	@Rule
	def decimalLongLiteral(self):
		return ( Tokens.decimalIntegerNoOctal + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='decimal', numType='long', value=x[0] ) )

	@Rule
	def hexIntLiteral(self):
		return Tokens.hexInteger.action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='hex', numType='int', value=x ) )

	@Rule
	def hexLongLiteral(self):
		return ( Tokens.hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='hex', numType='long', value=x[0] ) )

	@Rule
	def octIntLiteral(self):
		return Tokens.octalInteger.action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='oct', numType='int', value=x ) )

	@Rule
	def octLongLiteral(self):
		return ( Tokens.octalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='oct', numType='long', value=x[0] ) )

	@Rule
	def integerLiteral(self):
		return self.hexLongLiteral() | self.hexIntLiteral() | self.octLongLiteral() | self.octIntLiteral() | self.decimalLongLiteral() | self.decimalIntLiteral()
	




	# Float literal
	@Rule
	def floatLiteral(self):
		return Tokens.floatingPoint.action( lambda input, begin, end, x, bindings: Nodes.FloatLiteral( value=x ) )

	
	
	# Character literal
	@Rule
	def charLiteral(self):
		return Tokens.javaCharacterLiteral.action( lambda input, begin, end, x, bindings: Nodes.CharLiteral( value=x[1:-1] ) )

	
	
	# String literal
	@Rule
	def stringLiteral(self):
		return Tokens.javaStringLiteral.action( lambda input, begin, end, x, bindings: Nodes.StringLiteral( value=x[1:-1] ) )
	
	

	# Boolean Literal
	@Rule
	def booleanLiteral(self):
		return ( Keyword( Keywords.falseKeyword ) | Keyword( Keywords.trueKeyword ) ).action( lambda input, begin, end, x, bindings: Nodes.BooleanLiteral( value=x ) )
	
	
	
	# Null Literal
	@Rule
	def nullLiteral(self):
		return Keyword( Keywords.nullKeyword ).action( lambda input, begin, end, x, bindings: Nodes.NullLiteral() )
	
	
	# Java literal
	@Rule
	def literal(self):
		return self.floatLiteral() | self.integerLiteral() | self.charLiteral() | self.stringLiteral() | self.booleanLiteral() | self.nullLiteral()
	
	
	
	
	
	# Type reference
	@Rule
	def typeExpression(self):
		return self.basicTypeRef() | self.complexTypeExp()
	
	
	@Rule
	def complexTypeExp(self):
		def _action(input, begin, end, x, bindings):
			typeExp = x[0]
			for g in x[1]:
				typeExp = Nodes.MemberTypeExp( target=typeExp, member=g[1]  )
			for a in x[2]:
				typeExp = Nodes.ArrayTypeExp( itemTypeExp=typeExp )
			return typeExp
		return ( self.simpleTypeExp()  +  ( Literal( '.' ) + self.simpleTypeExp() ).zeroOrMore() +  ( Literal( '[' ) + Literal( ']' ) ).zeroOrMore() ).action( _action )
	
	
	@Rule
	def simpleTypeExp(self):
		return self.genericTypeExp()  |  self.classOrInterfaceTypeRef()
	
	
	@Rule
	def genericTypeExp(self):
		return ( self.classOrInterfaceTypeRef() + Literal( '<' ) + SeparatedList( self.genericTypeArgument(), 0, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( '>' ) ).action( \
		        lambda input, begin, end, x, bindings: Nodes.GenericTypeExp( target=x[0], args=x[2] ) )
	
	@Rule
	def genericTypeArgument(self):
		return self.typeExpression()  |  self.genericWildcardArgument()
	
	@Rule
	def genericWildcardArgument(self):
		return ( Literal( '?' )  +  ( Keyword( Keywords.superKeyword ) | Keyword( Keywords.extendsKeyword ) )  +  self.typeExpression ).action(
		        lambda input, begin, end, x, bindings: Nodes.WildCardTypeArgument( extendsOrSuper=( 'extends'   if x[1] == Keywords.extendsKeyword   else 'super' ), typeExp=x[2] ) )
	
	
	
	# Type reference
	@Rule
	def typeRef(self):
		return self.arrayTypeRef()  |  self.classOrInterfaceTypeRef()  |  self.basicTypeRef()
	
	@Rule
	def basicTypeRef(self):
		return self.byteTypeRef() | self.shortTypeRef() | self.intTypeRef() | self.longTypeRef() | self.charTypeRef() | self.floatTypeRef() | self.doubleTypeRef()  |  self.booleanTypeRef()
	
	@Rule
	def booleanTypeRef(self):
		return Keyword( Keywords.booleanKeyword ).action( lambda input, begin, end, x, bindings: Nodes.BooleanTypeRef() )
	
	@Rule
	def byteTypeRef(self):
		return Keyword( Keywords.byteKeyword ).action( lambda input, begin, end, x, bindings: Nodes.ByteTypeRef() )
	
	@Rule
	def shortTypeRef(self):
		return Keyword( Keywords.shortKeyword ).action( lambda input, begin, end, x, bindings: Nodes.ShortTypeRef() )
	
	@Rule
	def intTypeRef(self):
		return Keyword( Keywords.intKeyword ).action( lambda input, begin, end, x, bindings: Nodes.IntTypeRef() )
	
	@Rule
	def longTypeRef(self):
		return Keyword( Keywords.longKeyword ).action( lambda input, begin, end, x, bindings: Nodes.LongTypeRef() )
	
	@Rule
	def charTypeRef(self):
		return Keyword( Keywords.charKeyword ).action( lambda input, begin, end, x, bindings: Nodes.CharTypeRef() )
	
	@Rule
	def floatTypeRef(self):
		return Keyword( Keywords.floatKeyword ).action( lambda input, begin, end, x, bindings: Nodes.FloatTypeRef() )
	
	@Rule
	def doubleTypeRef(self):
		return Keyword( Keywords.doubleKeyword ).action( lambda input, begin, end, x, bindings: Nodes.DoubleTypeRef() )
	
	@Rule
	def classOrInterfaceTypeRef(self):
		return self.simpleName().action( lambda input, begin, end, x, bindings: Nodes.ClassOrInterfaceTypeRef( name=x ) )
	
	
	
	# Name
	@Rule
	def name(self):
		return ( self.simpleName()  +  ( Literal( '.' )  +  self.simpleName() ).zeroOrMore() ).action( lambda input, begin, end, x, bindings: [ x[0] ] + [ a[1]   for a in x[1] ] )
	
	@Rule
	def simpleName(self):
		return Tokens.javaIdentifier  &  ( lambda input, begin, end, x, bindings: x not in Keywords.keywordsSet )
	
	
	
	
	# This
	@Rule
	def thisExp(self):
		return Keyword( Keywords.thisKeyword ).action( lambda input, begin, end, x, bindings: Nodes.ThisExp() )
	
	
	# Super
	@Rule
	def superExp(self):
		return Keyword( Keywords.superKeyword ).action( lambda input, begin, end, x, bindings: Nodes.SuperExp() )
	
	
	# Parentheses
	@Rule
	def parenForm(self):
		return ( Literal( '(' ) + self.expression() + ')' ).action( lambda input, begin, end, xs, bindings: _incrementParens( xs[1] ) )
	
	
	
	
	# Primary
	@Rule
	def primary(self):
		return self.primaryNoNewArray() | self.arrayCreationExpression()
	
	@Rule
	def primaryNoNewArray(self):
		return self.literal() | self.thisExp() | self.parenForm() | self.classInstanceCreationExpression() | self.fieldAccess() | self.methodInvocation() | self.arrayAccess()
	
	
	
	@Rule
	def classInstanceCreationExpression(self):
		return ( Keyword( Keywords.newKeyword ) + self.classOrInterfaceTypeRef() + Literal( '(' ) + SeparatedList( self.expression(), 0, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( ')' ) ).action( \
		        lambda input, begin, end, xs, bindings: Nodes.ClassInstanceCreation( classTypeRef=xs[1], args=xs[3] ) )


	@Rule
	def arrayCreationExpression(self):
		return ( Keyword( Keywords.newKeyword ) + ( self.classOrInterfaceTypeRef() | self.primitiveTypeRef() ) + self.dimExpr().oneOrMore() ).action( \
		        lambda input, begin, end, xs, bindings: Nodes.ClassInstanceCreation( classTypeRef=xs[1], args=xs[3] ) )

	@Rule
	def dimExpr(self):
		return ( Literal( '[' ) + self.expression() + Literal( ']' ) ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def fieldAccess(self):
		return ( ( self.primary() | self.superExp() )  +  Literal( '.' )  +  self.simpleName() ).action( lambda input, begin, end, xs, bindings: Nodes.ClassInstanceCreation( target=xs[0], fieldName=xs[2] ) )
	
	
	@Rule
	def methodInvoation(self):
		return ( ( self.name() | self.primary() | self.superExp() )  +  Literal( '.' )  +  self.simpleName() ).action( lambda input, begin, end, xs, bindings: Nodes.ClassInstanceCreation( target=xs[0], fieldName=xs[2] ) )



import unittest
from Britefury.Tests.BritefuryJ.Parser import ParserTestCase


class TestCase_JavaGrammar (ParserTestCase.ParserTestCase):
	def test_identifier(self):
		g = JavaGrammar()
		self._parseStringTest( g.identifier(), 'abc', 'abc' )
	
	def test_qualifiedIdentifier(self):
		g = JavaGrammar()
		self._parseStringTest( g.qualifiedIdentifier(), 'abc', [ 'abc' ] )
		self._parseStringTest( g.qualifiedIdentifier(), 'abc.xyz', [ 'abc', 'xyz' ] )
		self._parseStringTest( g.qualifiedIdentifier(), 'abc.xyz.pqr', [ 'abc', 'xyz', 'pqr' ] )
		self._parseStringFailTest( g.qualifiedIdentifier(), 'abc.xyz.pqr.' )
		self._parseStringFailTest( g.qualifiedIdentifier(), '.abc.xyz.pqr' )
		self._parseStringFailTest( g.qualifiedIdentifier(), 'abc..xyz.pqr' )
		
	def test_integerLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), '123', Nodes.IntLiteral( format='decimal', numType='int', value='123' ) )
		self._parseStringTest( g.literal(), '123l', Nodes.IntLiteral( format='decimal', numType='long', value='123' ) )
		self._parseStringTest( g.literal(), '123L', Nodes.IntLiteral( format='decimal', numType='long', value='123' ) )
		self._parseStringFailTest( g.literal(), '12a' )
		self._parseStringTest( g.literal(), '0x123abc', Nodes.IntLiteral( format='hex', numType='int', value='0x123abc' ) )
		self._parseStringTest( g.literal(), '0x123abcl', Nodes.IntLiteral( format='hex', numType='long', value='0x123abc' ) )
		self._parseStringTest( g.literal(), '0x123ABCL', Nodes.IntLiteral( format='hex', numType='long', value='0x123ABC' ) )
		self._parseStringFailTest( g.literal(), '0x12g' )
		self._parseStringTest( g.literal(), '0123', Nodes.IntLiteral( format='oct', numType='int', value='0123' ) )
		self._parseStringTest( g.literal(), '0123l', Nodes.IntLiteral( format='oct', numType='long', value='0123' ) )
		self._parseStringTest( g.literal(), '0123L', Nodes.IntLiteral( format='oct', numType='long', value='0123' ) )
		self._parseStringFailTest( g.literal(), '012a' )
		self._parseStringFailTest( g.literal(), '0128' )

	def test_floatLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), '123.45', Nodes.FloatLiteral( value='123.45' ) )

	def test_charLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), '\'a\'', Nodes.CharLiteral( value='a' ) )
		self._parseStringTest( g.literal(), '\'\\n\'', Nodes.CharLiteral( value='\\n' ) )

	def test_stringLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), '"x"', Nodes.StringLiteral( value='x' ) )

	def test_booleanLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), 'false', Nodes.BooleanLiteral( value='false' ) )

	def test_nullLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), 'null', Nodes.NullLiteral() )
	
		
		
	def test_primitiveTypeRef(self):
		g = JavaGrammar()
		self._parseStringTest( g.typeRef(), 'boolean', Nodes.BooleanTypeRef() )
		self._parseStringTest( g.typeRef(), 'byte', Nodes.ByteTypeRef() )
		self._parseStringTest( g.typeRef(), 'short', Nodes.ShortTypeRef() )
		self._parseStringTest( g.typeRef(), 'int', Nodes.IntTypeRef() )
		self._parseStringTest( g.typeRef(), 'long', Nodes.LongTypeRef() )
		self._parseStringTest( g.typeRef(), 'char', Nodes.CharTypeRef() )
		self._parseStringTest( g.typeRef(), 'float', Nodes.FloatTypeRef() )
		self._parseStringTest( g.typeRef(), 'double', Nodes.DoubleTypeRef() )
		
	def test_classOrInterfaceTypeRef(self):
		g = JavaGrammar()
		self._parseStringTest( g.typeRef(), 'ArrayList', Nodes.ClassOrInterfaceTypeRef( name=[ 'ArrayList' ] ) )
		self._parseStringTest( g.typeRef(), 'java.util.List', Nodes.ClassOrInterfaceTypeRef( name=[ 'java', 'util', 'List' ] ) )
		
	def test_arrayTypeRef(self):
		g = JavaGrammar()
		self._parseStringTest( g.typeRef(), 'float []', Nodes.ArrayTypeRef( itemTypeRef=Nodes.FloatTypeRef() ) )
		self._parseStringTest( g.typeRef(), 'float [][]', Nodes.ArrayTypeRef( itemTypeRef=Nodes.ArrayTypeRef( itemTypeRef=Nodes.FloatTypeRef() ) ) )
		self._parseStringTest( g.typeRef(), 'ArrayList []', Nodes.ArrayTypeRef( itemTypeRef=Nodes.ClassOrInterfaceTypeRef( name=[ 'ArrayList' ] ) ) )
		self._parseStringTest( g.typeRef(), 'java.util.List []', Nodes.ArrayTypeRef( itemTypeRef=Nodes.ClassOrInterfaceTypeRef( name=[ 'java', 'util', 'List' ] ) ) )
		
		
		

	def test_thisExp(self):
		g = JavaGrammar()
		self._parseStringTest( g.thisExp(), 'this', Nodes.ThisExp() )
		