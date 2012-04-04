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


from LarchCore.Languages.Java import Keywords
from LarchCore.Languages.Java import Schema



class JavaGrammar (Grammar):
	__junk_regex__ = '[ ]*'

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
		return Tokens.decimalIntegerNoOctal.action( lambda input, begin, end, x, bindings: Schema.IntLiteral( format='decimal', numType='int', value=x ) )

	@Rule
	def decimalLongLiteral(self):
		return ( Tokens.decimalIntegerNoOctal + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Schema.IntLiteral( format='decimal', numType='long', value=x[0] ) )

	@Rule
	def hexIntLiteral(self):
		return Tokens.hexInteger.action( lambda input, begin, end, x, bindings: Schema.IntLiteral( format='hex', numType='int', value=x ) )

	@Rule
	def hexLongLiteral(self):
		return ( Tokens.hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Schema.IntLiteral( format='hex', numType='long', value=x[0] ) )

	@Rule
	def octIntLiteral(self):
		return Tokens.octalInteger.action( lambda input, begin, end, x, bindings: Schema.IntLiteral( format='oct', numType='int', value=x ) )

	@Rule
	def octLongLiteral(self):
		return ( Tokens.octalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Schema.IntLiteral( format='oct', numType='long', value=x[0] ) )

	@Rule
	def integerLiteral(self):
		return self.hexLongLiteral() | self.hexIntLiteral() | self.octLongLiteral() | self.octIntLiteral() | self.decimalLongLiteral() | self.decimalIntLiteral()
	




	# Float literal
	@Rule
	def floatLiteral(self):
		return Tokens.floatingPoint.action( lambda input, begin, end, x, bindings: Schema.FloatLiteral( value=x ) )

	
	
	# Character literal
	@Rule
	def charLiteral(self):
		return Tokens.javaCharacterLiteral.action( lambda input, begin, end, x, bindings: Schema.CharLiteral( value=x[1:-1] ) )

	
	
	# String literal
	@Rule
	def stringLiteral(self):
		return Tokens.javaStringLiteral.action( lambda input, begin, end, x, bindings: Schema.StringLiteral( value=x[1:-1] ) )
	
	

	# Boolean Literal
	@Rule
	def booleanLiteral(self):
		return ( Keyword( Keywords.falseKeyword ) | Keyword( Keywords.trueKeyword ) ).action( lambda input, begin, end, x, bindings: Schema.BooleanLiteral( value=x ) )
	
	
	
	# Null Literal
	@Rule
	def nullLiteral(self):
		return Keyword( Keywords.nullKeyword ).action( lambda input, begin, end, x, bindings: Schema.NullLiteral() )
	
	
	# Java literal
	@Rule
	def literal(self):
		return self.floatLiteral() | self.integerLiteral() | self.charLiteral() | self.stringLiteral() | self.booleanLiteral() | self.nullLiteral()
	
	
	
	
	
	
	# Type reference
	@Rule
	def typeExpression(self):
		return self.referenceTypeExp()  |  self.primitiveTypeRef()
	
	
	@Rule
	def referenceTypeExp(self):
		def _action(input, begin, end, x, bindings):
			typeExp = x[0]
			for a in x[1]:
				typeExp = Schema.ArrayTypeExp( itemTypeExp=typeExp )
			return typeExp
		return ( self.classOrInterfaceTypeExp()  +  ( Literal( '[' ) + Literal( ']' ) ).zeroOrMore() ).action( _action )  |  \
		       ( self.primitiveTypeRef()  +  ( Literal( '[' ) + Literal( ']' ) ).oneOrMore() ).action( _action )
		
	
	
	@Rule
	def classOrInterfaceTypeExp(self):
		def _action(input, begin, end, x, bindings):
			typeExp = x[0]
			for g in x[1]:
				typeExp = Schema.MemberTypeExp( target=typeExp, member=g[1]  )
			return typeExp
		return ( self.classOrInterfaceTypeExpComponent()  +  ( Literal( '.' ) + self.classOrInterfaceTypeExpComponent() ).zeroOrMore() ).action( _action )
	
	
	@Rule
	def classOrInterfaceTypeExpComponent(self):
		return self.genericTypeCall()  |  self.classOrInterfaceTypeRef()
	
	
	@Rule
	def genericTypeCall(self):
		return ( self.simpleName() + Literal( '<' ) + SeparatedList( self.genericTypeArgument(), 0, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( '>' ) ).action( \
		        lambda input, begin, end, x, bindings: Schema.GenericTypeExp( target=x[0], args=x[2] ) )
	
	@Rule
	def genericTypeArgument(self):
		return self.referenceTypeExp()  |  self.genericWildcardArgument()
	
	@Rule
	def genericWildcardArgument(self):
		return ( Literal( '?' )  +  ( Keyword( Keywords.superKeyword ) | Keyword( Keywords.extendsKeyword ) )  +  self.referenceTypeExp() ).action(
		        lambda input, begin, end, x, bindings: Schema.WildCardTypeArgument( extendsOrSuper=( 'extends'   if x[1] == Keywords.extendsKeyword   else 'super' ), typeExp=x[2] ) )
	
	
	
	@Rule
	def primitiveTypeRef(self):
		return self.byteTypeRef() | self.shortTypeRef() | self.intTypeRef() | self.longTypeRef() | self.charTypeRef() | self.floatTypeRef() | self.doubleTypeRef()  |  self.booleanTypeRef()
	
	@Rule
	def booleanTypeRef(self):
		return Keyword( Keywords.booleanKeyword ).action( lambda input, begin, end, x, bindings: Schema.BooleanTypeRef() )
	
	@Rule
	def byteTypeRef(self):
		return Keyword( Keywords.byteKeyword ).action( lambda input, begin, end, x, bindings: Schema.ByteTypeRef() )
	
	@Rule
	def shortTypeRef(self):
		return Keyword( Keywords.shortKeyword ).action( lambda input, begin, end, x, bindings: Schema.ShortTypeRef() )
	
	@Rule
	def intTypeRef(self):
		return Keyword( Keywords.intKeyword ).action( lambda input, begin, end, x, bindings: Schema.IntTypeRef() )
	
	@Rule
	def longTypeRef(self):
		return Keyword( Keywords.longKeyword ).action( lambda input, begin, end, x, bindings: Schema.LongTypeRef() )
	
	@Rule
	def charTypeRef(self):
		return Keyword( Keywords.charKeyword ).action( lambda input, begin, end, x, bindings: Schema.CharTypeRef() )
	
	@Rule
	def floatTypeRef(self):
		return Keyword( Keywords.floatKeyword ).action( lambda input, begin, end, x, bindings: Schema.FloatTypeRef() )
	
	@Rule
	def doubleTypeRef(self):
		return Keyword( Keywords.doubleKeyword ).action( lambda input, begin, end, x, bindings: Schema.DoubleTypeRef() )
	
	@Rule
	def classOrInterfaceTypeRef(self):
		return self.simpleName().action( lambda input, begin, end, x, bindings: Schema.ClassOrInterfaceTypeRef( name=x ) )
	
	
	
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
		return Keyword( Keywords.thisKeyword ).action( lambda input, begin, end, x, bindings: Schema.ThisExp() )
	
	
	# Super
	@Rule
	def superExp(self):
		return Keyword( Keywords.superKeyword ).action( lambda input, begin, end, x, bindings: Schema.SuperExp() )
	
	
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
		return self.literal() | self.typeClassExp() | self.voidClassExp() | self.thisExp() | self.parenForm() | self.classInstanceCreationExpression() | self.fieldAccess() | self.methodInvocation() | self.arrayAccess()
	
	
	@Rule
	def typeClassExp(self):
		return ( self.typeExpression() + Literal( '.' ) + Keyword( Keywords.classKeyword ) ).action( lambda input, begin, end, xs, bindings: Schema.TypeClassExp( typeExp=xs[0] ) )
	
	@Rule
	def voidClassExp(self):
		return ( Keyword( Keywords.voidKeyword ) + Literal( '.' ) + Keyword( Keywords.classKeyword ) ).action( lambda input, begin, end, xs, bindings: Schema.VoidClassExp() )
	
	
	@Rule
	def classInstanceCreationExpression(self):
		return ( Keyword( Keywords.newKeyword ) + self.classOrInterfaceTypeRef() + Literal( '(' ) + SeparatedList( self.expression(), 0, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( ')' ) ).action( \
		        lambda input, begin, end, xs, bindings: Schema.ClassInstanceCreation( classTypeRef=xs[1], args=xs[3] ) )


	@Rule
	def dimExpr(self):
		return ( Literal( '[' ) + self.expression() + Literal( ']' ) ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def fieldAccess(self):
		return ( ( self.primary() | self.superExp() )  +  Literal( '.' )  +  self.simpleName() ).action( lambda input, begin, end, xs, bindings: Schema.ClassInstanceCreation( target=xs[0], fieldName=xs[2] ) )
	
	
	@Rule
	def methodInvocation(self):
		return ( ( self.name() | self.primary() | self.superExp() )  +  Literal( '.' )  +  self.simpleName() ).action( lambda input, begin, end, xs, bindings: Schema.MethodInvocation( target=xs[0], fieldName=xs[2] ) )


	@Rule
	def arrayAccess(self):
		def _action(input, begin, end, x, bindings):
			exp = x[0]
			for i in x[1]:
				exp = Schema.ArrayAccess( target=exp, index=i[1] )
			return exp
		return ( self.primaryNoNewArray()  +  ( Literal( '[' ) + self.expression() + Literal( ']' ) ).oneOrMore() ).action( _action )
	
	
	@Rule
	def arrayCreationExpression(self):
		return ( Keyword( Keywords.newKeyword ) + ( self.classOrInterfaceTypeRef() | self.primitiveTypeRef() ) + self.dimExpr().oneOrMore() ).action( \
		        lambda input, begin, end, xs, bindings: Schema.ClassInstanceCreation( classTypeRef=xs[1], args=xs[3] ) )


	
	
	
	@Rule
	def expression(self):
		return self.primary()
	
	
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
		self._parseStringTest( g.literal(), '123', Schema.IntLiteral( format='decimal', numType='int', value='123' ) )
		self._parseStringTest( g.literal(), '123l', Schema.IntLiteral( format='decimal', numType='long', value='123' ) )
		self._parseStringTest( g.literal(), '123L', Schema.IntLiteral( format='decimal', numType='long', value='123' ) )
		self._parseStringFailTest( g.literal(), '12a' )
		self._parseStringTest( g.literal(), '0x123abc', Schema.IntLiteral( format='hex', numType='int', value='0x123abc' ) )
		self._parseStringTest( g.literal(), '0x123abcl', Schema.IntLiteral( format='hex', numType='long', value='0x123abc' ) )
		self._parseStringTest( g.literal(), '0x123ABCL', Schema.IntLiteral( format='hex', numType='long', value='0x123ABC' ) )
		self._parseStringFailTest( g.literal(), '0x12g' )
		self._parseStringTest( g.literal(), '0123', Schema.IntLiteral( format='oct', numType='int', value='0123' ) )
		self._parseStringTest( g.literal(), '0123l', Schema.IntLiteral( format='oct', numType='long', value='0123' ) )
		self._parseStringTest( g.literal(), '0123L', Schema.IntLiteral( format='oct', numType='long', value='0123' ) )
		self._parseStringFailTest( g.literal(), '012a' )
		self._parseStringFailTest( g.literal(), '0128' )

	
	def test_floatLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), '123.45', Schema.FloatLiteral( value='123.45' ) )

		
	def test_charLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), '\'a\'', Schema.CharLiteral( value='a' ) )
		self._parseStringTest( g.literal(), '\'\\n\'', Schema.CharLiteral( value='\\n' ) )

		
	def test_stringLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), '"x"', Schema.StringLiteral( value='x' ) )

		
	def test_booleanLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), 'false', Schema.BooleanLiteral( value='false' ) )

		
	def test_nullLiteral(self):
		g = JavaGrammar()
		self._parseStringTest( g.literal(), 'null', Schema.NullLiteral() )
	
		
		
		
	def test_primitiveTypeRef(self):
		g = JavaGrammar()
		self._parseStringTest( g.typeExpression(), 'boolean', Schema.BooleanTypeRef() )
		self._parseStringTest( g.typeExpression(), 'byte', Schema.ByteTypeRef() )
		self._parseStringTest( g.typeExpression(), 'short', Schema.ShortTypeRef() )
		self._parseStringTest( g.typeExpression(), 'int', Schema.IntTypeRef() )
		self._parseStringTest( g.typeExpression(), 'long', Schema.LongTypeRef() )
		self._parseStringTest( g.typeExpression(), 'char', Schema.CharTypeRef() )
		self._parseStringTest( g.typeExpression(), 'float', Schema.FloatTypeRef() )
		self._parseStringTest( g.typeExpression(), 'double', Schema.DoubleTypeRef() )
		
		
	def test_classOrInterfaceTypeRef(self):
		g = JavaGrammar()
		self._parseStringTest( g.typeExpression(), 'ArrayList', Schema.ClassOrInterfaceTypeRef( name='ArrayList' ) )
		self._parseStringTest( g.typeExpression(), 'java.util.List', Schema.MemberTypeExp( target=Schema.MemberTypeExp( target=Schema.ClassOrInterfaceTypeRef( name='java' ), member=Schema.ClassOrInterfaceTypeRef( name='util' ) ), member=Schema.ClassOrInterfaceTypeRef( name='List' ) ) )
		
		
	def test_genericTypeExp(self):
		g = JavaGrammar()
		self._parseStringTest( g.typeExpression(), 'ArrayList<Integer>', Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ClassOrInterfaceTypeRef( name='Integer' ) ] ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<Integer,Double>', Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ClassOrInterfaceTypeRef( name='Integer' ), Schema.ClassOrInterfaceTypeRef( name='Double' ) ] ) )
		self._parseStringTest( g.typeExpression(), 'X.ArrayList<Integer>', Schema.MemberTypeExp( target=Schema.ClassOrInterfaceTypeRef( name='X' ), member=Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ClassOrInterfaceTypeRef( name='Integer' ) ] ) ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<Integer>.X', Schema.MemberTypeExp( target=Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ClassOrInterfaceTypeRef( name='Integer' ) ] ), member=Schema.ClassOrInterfaceTypeRef( name='X' ) ) )
		self._parseStringTest( g.typeExpression(), 'Vector<ArrayList<Integer>>', Schema.GenericTypeExp( target='Vector', args=[ Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ClassOrInterfaceTypeRef( name='Integer' ) ] ) ] ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<Integer[]>', Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ArrayTypeExp( itemTypeExp=Schema.ClassOrInterfaceTypeRef( name='Integer' ) ) ] ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<int[]>', Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ArrayTypeExp( itemTypeExp=Schema.IntTypeRef() ) ] ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<? extends Q>', Schema.GenericTypeExp( target='ArrayList', args=[ Schema.WildCardTypeArgument( extendsOrSuper='extends', typeExp=Schema.ClassOrInterfaceTypeRef( name='Q' ) ) ] ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<? super Q>', Schema.GenericTypeExp( target='ArrayList', args=[ Schema.WildCardTypeArgument( extendsOrSuper='super', typeExp=Schema.ClassOrInterfaceTypeRef( name='Q' ) ) ] ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<?>', Schema.GenericTypeExp( target='ArrayList', args=[ Schema.WildCardTypeArgument( extendsOrSuper='super', typeExp=Schema.ClassOrInterfaceTypeRef( name='Q' ) ) ] ) )
		self._parseStringFailTest( g.typeExpression(), 'ArrayList<int>' )

		
	def test_arrayTypeExp(self):
		g = JavaGrammar()
		self._parseStringTest( g.typeExpression(), 'float []', Schema.ArrayTypeExp( itemTypeExp=Schema.FloatTypeRef() ) )
		self._parseStringTest( g.typeExpression(), 'float [][]', Schema.ArrayTypeExp( itemTypeExp=Schema.ArrayTypeExp( itemTypeExp=Schema.FloatTypeRef() ) ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList []', Schema.ArrayTypeExp( itemTypeExp=Schema.ClassOrInterfaceTypeRef( name='ArrayList' ) ) )
		self._parseStringTest( g.typeExpression(), 'ArrayList<Integer> []', Schema.ArrayTypeExp( itemTypeExp=Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ClassOrInterfaceTypeRef( name='Integer' ) ] ) ) )
		self._parseStringTest( g.typeExpression(), 'java.util.List []', Schema.ArrayTypeExp( itemTypeExp=Schema.MemberTypeExp( target=Schema.MemberTypeExp( target=Schema.ClassOrInterfaceTypeRef( name='java' ), member=Schema.ClassOrInterfaceTypeRef( name='util' ) ), member=Schema.ClassOrInterfaceTypeRef( name='List' ) ) ) )
		
		
		
		
	def test_typeClassExp(self):
		g = JavaGrammar()
		self._parseStringTest( g.primary(), 'float.class', Schema.TypeClassExp( typeExp=Schema.FloatTypeRef() ) )
		self._parseStringTest( g.primary(), 'ArrayList<Integer>.class', Schema.TypeClassExp( typeExp=Schema.GenericTypeExp( target='ArrayList', args=[ Schema.ClassOrInterfaceTypeRef( name='Integer' ) ] ) ) )
		self._parseStringTest( g.primary(), 'void.class', Schema.VoidClassExp() )

		
	def test_thisExp(self):
		g = JavaGrammar()
		self._parseStringTest( g.thisExp(), 'this', Schema.ThisExp() )
		