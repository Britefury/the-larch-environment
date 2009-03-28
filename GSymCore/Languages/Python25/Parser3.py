##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string

from BritefuryJ.DocModel import DMObject

from BritefuryJ.Parser import Action, Condition, Forward, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot
from BritefuryJ.Parser.Utils.Tokens import identifier, decimalInteger, hexInteger, integer, singleQuotedString, doubleQuotedString, quotedString, floatingPoint
from BritefuryJ.Parser.Utils.SeparatedList import separatedList, delimitedSeparatedList
from BritefuryJ.Parser.Utils.OperatorParser import Prefix, Suffix, InfixLeft, InfixRight, InfixChain, PrecedenceLevel, OperatorTable

from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase

from Britefury.Util.NodeUtil import makeNullNode

from Britefury.Grammar.Grammar import Grammar, Rule, RuleList

from GSymCore.Languages.Python25.Keywords import *
import GSymCore.Languages.Python25.NodeClasses as Nodes



#
#
#
# !!!!!! NOTES !!!!!!
# Comparison operators are NOT parsed correctly;
# 'a < b < c' is valid in Python, but is not handled here.
# The parser needs to be changed, in addition to changing the python document structure to reflect this.
#
# yieldExpr and yieldAtom are basically the same thing; find a way of unifying this.
#
# Print statements are not handled correctly
#
# Octal integers not handled correctly
#
#
#




class Python25Grammar (Grammar):
	# Python identifier
	@Rule
	def pythonIdentifier(self):
		return identifier  &  ( lambda input, pos, result: result not in keywordsSet )


	@Rule
	def dottedPythonIdentifer(self):
		return ( separatedList( self.pythonIdentifier(), '.', True, False, False ) ).action( lambda input, pos, xs: '.'.join( xs ) )





	# String literal
	@Rule
	def asciiStringSLiteral(self):
		return singleQuotedString.action( lambda input, pos, xs: Nodes.StringLiteral( format='ascii', quotation='single', value=xs[1:-1] ) )

	@Rule
	def asciiStringDLiteral(self):
		return doubleQuotedString.action( lambda input, pos, xs: Nodes.StringLiteral( format='ascii', quotation='double', value=xs[1:-1] ) )

	@Rule
	def unicodeStringSLiteral(self):
		return ( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + singleQuotedString ).action( lambda input, pos, xs: Nodes.StringLiteral( format='unicode', quotation='single', value=xs[0][1:-1] ) )

	@Rule
	def unicodeStringDLiteral(self):
		return ( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + doubleQuotedString ).action( lambda input, pos, xs: Nodes.StringLiteral( format='unicode', quotation='double', value=xs[0][1:-1] ) )

	@Rule
	def regexAsciiStringSLiteral(self):
		return ( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + singleQuotedString ).action( lambda input, pos, xs: Nodes.StringLiteral( format='ascii-regex', quotation='single', value=xs[0][1:-1] ) )

	@Rule
	def regexAsciiStringDLiteral(self):
		return ( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + doubleQuotedString ).action( lambda input, pos, xs: Nodes.StringLiteral( format='ascii-regex', quotation='double', value=xs[0][1:-1] ) )

	@Rule
	def regexUnicodeStringSLiteral(sefl):
		return ( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + singleQuotedString ).action( lambda input, pos, xs: Nodes.StringLiteral( format='unicode-regex', quotation='single', value=xs[0][1:-1] ) )

	@Rule
	def regexUnicodeStringDLiteral(self):
		return ( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + doubleQuotedString ).action( lambda input, pos, xs: Nodes.StringLiteral( format='unicode-regex', quotation='double', value=xs[0][1:-1] ) )

	@Rule
	def shortStringLiteral(self):
		return self.asciiStringSLiteral() | self.asciiStringDLiteral() | self.unicodeStringSLiteral() | self.unicodeStringDLiteral() | self.regexAsciiStringSLiteral() | self.regexAsciiStringDLiteral() | \
		       self.regexUnicodeStringSLiteral() | self.regexUnicodeStringDLiteral()






	# Integer literal
	@Rule
	def decimalIntLiteral(self):
		return decimalInteger.action( lambda input, pos, xs: Nodes.IntLiteral( format='decimal', numType='int', value=xs ) )

	@Rule
	def decimalLongLiteral(self):
		return ( decimalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: Nodes.IntLiteral( format='decimal', numType='long', value=xs[0] ) )

	@Rule
	def hexIntLiteral(self):
		return hexInteger.action( lambda input, pos, xs: Nodes.IntLiteral( format='hex', numType='int', value=xs ) )

	@Rule
	def hexLongLiteral(self):
		return ( hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: Nodes.IntLiteral( format='hex', numType='long', value=xs[0] ) )

	@Rule
	def integerLiteral(self):
		return self.hexLongLiteral() | self.hexIntLiteral() | self.decimalLongLiteral() | self.decimalIntLiteral()





	# Float literal
	@Rule
	def floatLiteral(self):
		return floatingPoint.action( lambda input, pos, xs: Nodes.FloatLiteral( value=xs ) )




	# Imaginary literal
	@Rule
	def imaginaryLiteral(self):
		return Combine( [ ( floatingPoint | decimalInteger ), Literal( 'j' ) ] ).action( lambda input, pos, xs: Nodes.ImaginaryLiteral( value=xs ) )



	# Literal
	@Rule
	def literal(self):
		return self.shortStringLiteral() | self.imaginaryLiteral() | self.floatLiteral() | self.integerLiteral()



	# Attribute name
	@Rule
	def attrName(self):
		return self.pythonIdentifier()



	# Target (assignment, for-loop, ...)
	@Rule
	def singleTarget(self):
		return self.pythonIdentifier().action( lambda input, pos, xs: Nodes.SingleTarget( name=xs ) )

	@Rule
	def tupleTarget(self):
		return separatedList( self.targetItem(), True, True, True ).action( lambda input, pos, xs: Nodes.TupleTarget( targets=xs ) )

	@Rule
	def targetList(self):
		return self.tupleTarget()  |  self.targetItem()

	@Rule
	def parenTarget(self):
		return ( Literal( '(' )  +  self.targetList()  +  Literal( ')' ) ).action( lambda input, pos, xs: xs[1] )

	@Rule
	def listTarget(self):
		return delimitedSeparatedList( self.targetItem(), '[', ']', False, True, False ).action( lambda input, pos, xs: Nodes.ListTarget( targets=xs ) )

	@Rule
	def targetItem(self):
		return ( ( self.attributeRef()  ^  self.subscript() )  |  self.parenTarget()  |  self.listTarget()  |  self.singleTarget() )
		#return self.parenTarget()  |  self.listTarget()  |  self.singleTarget()





	# Load local variable
	@Rule
	def loadLocal(self):
		return self.pythonIdentifier().action( lambda input, pos, xs: Nodes.Load( name=xs ) )



	# Tuples
	@Rule
	def tupleLiteral(self):
		return separatedList( self.expression(), True, True, True ).action( lambda input, pos, xs: Nodes.TupleLiteral( values=xs ) )

	@Rule
	def oldTupleLiteral(self):
		return separatedList( self.expression(), True, True, True ).action( lambda input, pos, xs: Nodes.TupleLiteral( values=xs ) )



	# Expression list
	@Rule
	def expressionList(self):
		return separatedList( self.expression(), True, True, False ) 



	# Parentheses
	@Rule
	def parenForm(self):
		return( Literal( '(' ) + self.tupleOrExpression() + ')' ).action( lambda input, pos, xs: xs[1] )



	# List literal
	@Rule
	def listLiteral(self):
		return delimitedSeparatedList( self.expression(), '[', ']', False, True, False ).action( lambda input, pos, xs: Nodes.ListLiteral( values=xs ) )



	# List comprehension and generator expression
	@Rule
	def comprehensionFor(self):
		return ( Keyword( forKeyword )  +  self.targetList()  +  Keyword( inKeyword )  +  self.oldTupleOrExpression() ).action( lambda input, pos, xs: Nodes.ComprehensionFor( target=xs[1], source=xs[3] ) )

	@Rule
	def comprehensionIf(self):
		return ( Keyword( ifKeyword )  +  self.oldExpression() ).action( lambda input, pos, xs: Nodes.ComprehensionIf( condition=xs[1] ) )

	@Rule
	def comprehensionItem(self):
		return self.comprehensionFor() | self.comprehensionIf()

	@Rule
	def listComprehension(self):
		return ( Literal( '[' )  +  self.expression()  +  self.comprehensionFor()  +  ZeroOrMore( self.comprehensionItem() )  +  Literal( ']' ) ).action( lambda input, pos, xs: Nodes.ListComp( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )

	@Rule
	def generatorExpression(self):
		return ( Literal( '(' )  +  self.expression()  +  self.comprehensionFor()  +  ZeroOrMore( self.comprehensionItem() )  +  Literal( ')' ) ).action( lambda input, pos, xs: Nodes.GeneratorExpr( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )




	# Dictionary literal
	@Rule
	def keyValuePair(self):
		return ( self.expression()  +  Literal( ':' )  +  self.expression() ).action( lambda input, pos, xs: Nodes.DictKeyValuePair( key=xs[0], value=xs[2] ) )

	@Rule
	def dictLiteral(self):
		return delimitedSeparatedList( self.keyValuePair(), '{', '}', False, True, False ).action( lambda input, pos, xs: Nodes.DictLiteral( values=xs ) )




	# Yield expression
	@Rule
	def yieldExpression(self):
		return ( Keyword( yieldKeyword )  +  self.expression() ).action( lambda input, pos, xs: Nodes.YieldExpr( value=xs[1] ) )

	@Rule
	def yieldAtom(self):
		return ( Literal( '(' )  +  Keyword( yieldKeyword )  +  self.expression()  +  Literal( ')' ) ).action( lambda input, pos, xs: Nodes.YieldAtom( value=xs[2] ) )



	# Enclosure
	@Rule
	def enclosure(self):
		return self.parenForm() | self.listLiteral() | self.listComprehension() | self.generatorExpression() | self.dictLiteral() | self.yieldExpression()




	# Atom
	@Rule
	def atom(self):
		return self.enclosure() | self.literal() | self.loadLocal()




	# Attribute ref
	@Rule
	def attributeRef(self):
		return ( self.primary() + '.' + self.attrName() ).action( lambda input, pos, xs: Nodes.AttributeRef( target=xs[0], name=xs[2] ) )




	# Subscript and slice
	def _sliceItem(self, x):
		return x   if x is not None   else   makeNullNode()

	@Rule
	def subscriptSlice(self):
		return ( ( Optional( self.expression() ) + ':' + Optional( self.expression() )  ).action( lambda input, pos, xs: Nodes.SubscriptSlice( lower=self._sliceItem( xs[0] ), upper=self._sliceItem( xs[2] ) ) ) )

	@Rule
	def subscriptLongSlice(self):
		return ( ( Optional( self.expression() )  + ':' + Optional( self.expression() )  + ':' + Optional( self.expression() )  ).action( \
			lambda input, pos, xs: Nodes.SubscriptLongSlice( lower=self._sliceItem( xs[0] ), upper=self._sliceItem( xs[2] ), stride=self._sliceItem( xs[4] ) ) ) )

	@Rule
	def subscriptEllipsis(self):
		return Literal( '...' ).action( lambda input, pos, xs: Nodes.SubscriptEllipsis() )

	@Rule
	def subscriptItem(self):
		return self.subscriptLongSlice() | self.subscriptSlice() | self.subscriptEllipsis() | self.expression()

	@Rule
	def subscriptTuple(self):
		return separatedList( self.subscriptItem(), True, True, True ).action( lambda input, pos, xs: Nodes.SubscriptTuple( values=xs ) )

	@Rule
	def subscriptIndex(self):
		return self.subscriptTuple()  |  self.subscriptItem()

	@Rule
	def subscript(self):
		return ( self.primary() + '[' + self.subscriptIndex() + ']' ).action( lambda input, pos, xs: Nodes.Subscript( target=xs[0], index=xs[2] ) )




	# Call
	def _checkCallArgs(self, input, pos, xs):
		bKW = False
		bArgList = False
		bKWArgList = False
		for x in xs:
			if isinstance( x, DMObject ):
				if x.isInstanceOf( Nodes.CallKWArgList ):
					if bKWArgList:
						# Not after KW arg list (only 1 allowed)
						return False
					bKWArgList = True
					continue
				if x.isInstanceOf( Nodes.CallArgList ):
					if bKWArgList | bArgList:
						# Not after KW arg list
						# Not after arg list (only 1 allowed)
						return False
					bArgList = True
					continue
				if x.isInstanceOf( Nodes.CallKWArg ):
					if bKWArgList | bArgList:
						# Not after arg list or KW arg list
						return False
					bKW = True
					continue
			if bKWArgList | bArgList | bKW:
				# Not after KW arg list, or arg list, or KW arg
				return False
		return True

	@Rule
	def argName(self):
		return self.pythonIdentifier()

	@Rule
	def kwArg(self):
		return ( self.argName() + '=' + self.expression() ).action( lambda input, pos, xs: Nodes.CallKWArg( name=xs[0], value=xs[2] ) )

	@Rule
	def argList(self):
		return ( Literal( '*' )  +  self.expression() ).action( lambda input, pos, xs: Nodes.CallArgList( value=xs[1] ) )

	@Rule
	def kwArgList(self):
		return ( Literal( '**' )  +  self.expression() ).action( lambda input, pos, xs: Nodes.CallKWArgList( value=xs[1] ) )

	@Rule
	def callArg(self):
		return self.kwArgList() | self.argList() | self.kwArg() | self.expression()

	@Rule
	def callArgs(self):
		return separatedList( self.callArg(), False, True, False ).condition( self._checkCallArgs )

	@Rule
	def call(self):
		return ( self.primary() + Literal( '(' ) + self.callArgs() + Literal( ')' ) ).action( lambda input, pos, xs: Nodes.Call( target=xs[0], args=xs[2] ) )



	# Primary
	@Rule
	def primary(self):
		return self.call() | self.attributeRef() | self.subscript() | self.atom()



	# Python operators
	@RuleList( [ 'powOp', 'invNegPosOp', 'mulDivModOp', 'addSubOp', 'lrShiftOp', 'andOP', 'xorOp', 'orOp', 'cmpOp', 'isOp', 'inOp', 'notTestOp', 'andTestOp', 'orTestOp' ] )
	def _operators(self):
		opTable = OperatorTable( 
			[
				PrecedenceLevel( [ InfixRight( Literal( '**' ),  Nodes.Pow, 'x', 'y' ) ] ),
				PrecedenceLevel( [ Prefix( Literal( '~' ),  Nodes.Invert, 'x' ),   Prefix( Literal( '-' ),  Nodes.Negate, 'x' ), Prefix( Literal( '+' ),  Nodes.Pos, 'x' ) ] ),
				PrecedenceLevel( [ InfixLeft( Literal( '*' ),  Nodes.Mul, 'x', 'y' ),   InfixLeft( Literal( '/' ),  Nodes.Div, 'x', 'y' ),   InfixLeft( Literal( '%' ),  Nodes.Mod, 'x', 'y' ) ] ),
				PrecedenceLevel( [ InfixLeft( Literal( '+' ),  Nodes.Add, 'x', 'y' ),   InfixLeft( Literal( '-' ),  Nodes.Sub, 'x', 'y' ) ] ),
				PrecedenceLevel( [ InfixLeft( Literal( '<<' ),  Nodes.LShift, 'x', 'y' ),   InfixLeft( Literal( '>>' ),  Nodes.RShift, 'x', 'y') ] ),
				PrecedenceLevel( [ InfixLeft( Literal( '&' ),  Nodes.BitAnd, 'x', 'y' ) ] ),
				PrecedenceLevel( [ InfixLeft( Literal( '^' ),  Nodes.BitXor, 'x', 'y' ) ] ),
				PrecedenceLevel( [ InfixLeft( Literal( '|' ),  Nodes.BitOr, 'x', 'y' ) ] ),
				PrecedenceLevel( [ InfixChain( [
						InfixChain.ChainOperator( Literal( '<=' ),  Nodes.CmpOpLte, 'y' ),
						InfixChain.ChainOperator( Literal( '<' ),  Nodes.CmpOpLt, 'y' ),
						InfixChain.ChainOperator( Literal( '>=' ),  Nodes.CmpOpGte, 'y' ),
						InfixChain.ChainOperator( Literal( '>' ),  Nodes.CmpOpGt, 'y' ),
						InfixChain.ChainOperator( Literal( '==' ),  Nodes.CmpOpEq, 'y' ),
						InfixChain.ChainOperator( Literal( '!=' ),  Nodes.CmpOpNeq, 'y' ),
						],  Nodes.Cmp, 'x', 'ops' ),
					] ),
				PrecedenceLevel( [ InfixLeft( Keyword( isKeyword ) + Keyword( notKeyword ),  Nodes.IsNotTest, 'x', 'y' ),   InfixLeft( Keyword( isKeyword ),  Nodes.IsTest, 'x', 'y' ) ] ),
				PrecedenceLevel( [ InfixLeft( Keyword( notKeyword ) + Keyword( inKeyword ),  Nodes.NotInTest, 'x', 'y' ),   InfixLeft( Keyword( inKeyword ),  Nodes.InTest, 'x', 'y' ) ] ),
				PrecedenceLevel( [ Prefix( Keyword( notKeyword ),  Nodes.NotTest, 'x' ) ] ),
				PrecedenceLevel( [ InfixLeft( Keyword( andKeyword ),  Nodes.AndTest, 'x', 'y' ) ] ),
				PrecedenceLevel( [ InfixLeft( Keyword( orKeyword ),  Nodes.OrTest, 'x', 'y' ) ] ),
				],  self.primary() )

		return opTable.buildParsers()



	@Rule
	def orOp(self):
		return self._operators()[7]


	@Rule
	def orTest(self):
		return self._operators()[-1]




	# Parameters (lambda, def statement, etc)
	def _checkParams(self, input, pos, xs):
		bDefaultValParam = False
		bParamList = False
		bKWParamList = False
		for x in xs:
			if isinstance( x, DMObject ):
				if x.isInstanceOf( Nodes.KWParamList ):
					if bKWParamList:
						# Not after KW param list (only 1 allowed)
						return False
					bKWParamList = True
					continue
				elif x.isInstanceOf( Nodes.ParamList ):
					if bKWParamList | bParamList:
						# Not after KW param list
						# Not after param list (only 1 allowed)
						return False
					bParamList = True
					continue
				elif x.isInstanceOf( Nodes.DefaultValueParam ):
					if bKWParamList | bParamList:
						# Not after param list or KW param list
						return False
					bDefaultValParam = True
					continue
			if bKWParamList | bParamList | bDefaultValParam:
				# Not after KW param list, or param list, or default value param
				return False
		return True

	@Rule
	def paramName(self):
		return self.pythonIdentifier()

	@Rule
	def simpleParam(self):
		return self.pythonIdentifier().action( lambda input, pos, xs: Nodes.SimpleParam( name=xs ) )

	@Rule
	def defaultValueParam(self):
		return ( self.paramName() + '=' + self.expression() ).action( lambda input, pos, xs: Nodes.DefaultValueParam( name=xs[0], defaultValue=xs[2] ) )

	@Rule
	def paramList(self):
		return ( Literal( '*' )  +  self.paramName() ).action( lambda input, pos, xs: Nodes.ParamList( name=xs[1] ) )

	@Rule
	def kwParamList(self):
		return ( Literal( '**' )  +  self.paramName() ).action( lambda input, pos, xs: Nodes.KWParamList( name=xs[1] ) )

	@Rule
	def param(self):
		return self.kwParamList() | self.paramList() | self.defaultValueParam() | self.simpleParam()

	@Rule
	def params(self):
		return separatedList( self.param(), False, True, False ).condition( self._checkParams )




	# Lambda expression_checkParams
	@Rule
	def lambdaExpr(self):
		return ( Keyword( lambdaKeyword )  +  self.params()  +  Literal( ':' )  +  self.expression() ).action( lambda input, pos, xs: Nodes.LambdaExpr( params=xs[1], expr=xs[3] ) )




	# Conditional expression
	@Rule
	def conditionalExpression(self):
		return ( self.orTest()  +  Keyword( ifKeyword )  +  self.orTest()  +  Keyword( elseKeyword )  +  self.expression() ).action( lambda input, pos, xs: Nodes.ConditionalExpr( condition=xs[2], expr=xs[0], elseExpr=xs[4] ) )



	# Expression and old expression (old expression is expression without conditional expression)
	@Rule
	def oldExpression(self):
		return self.lambdaExpr()  |  self.orTest()

	@Rule
	def expression(self):
		return self.lambdaExpr()  |  self.conditionalExpression()  |  self.orTest()



	# Tuple or (old) expression
	@Rule
	def tupleOrExpression(self):
		return self.tupleLiteral() | self.expression()

	@Rule
	def oldTupleOrExpression(self):
		return self.oldTupleLiteral() | self.oldExpression()




	# Tuple or expression or yield expression
	@Rule
	def tupleOrExpressionOrYieldExpression(self):
		return self.tupleOrExpression() | self.yieldExpression()




	# Assert statement
	@Rule
	def assertStmt(self):
		return ( Keyword( assertKeyword ) + self.expression()  +  Optional( Literal( ',' ) + self.expression() ) ).action( lambda input, pos, xs: Nodes.AssertStmt( condition=xs[1], fail=xs[2][1]   if xs[2] is not None  else  makeNullNode() ) )




	# Assignment statement
	@Rule
	def assignmentStmt(self):
		return ( OneOrMore( ( self.targetList()  +  '=' ).action( lambda input, pos, xs: xs[0] ) )  +  self.tupleOrExpressionOrYieldExpression() ).action( lambda input, pos, xs: Nodes.AssignStmt( targets=xs[0], value=xs[1] ) )




	# Augmented assignment statement
	@Rule
	def augOp(self):
		return Choice( [ Literal( op )   for op in augAssignOps ] )

	@Rule
	def augAssignStmt(self):
		return ( self.targetItem()  +  self.augOp()  +  self.tupleOrExpressionOrYieldExpression() ).action( lambda input, pos, xs: Nodes.AugAssignStmt( op=xs[1], target=xs[0], value=xs[2] ) )




	# Pass statement
	@Rule
	def passStmt(self):
		return Keyword( passKeyword ).action( lambda input, pos, xs: Nodes.PassStmt() )



	# Del statement
	@Rule
	def delStmt(self):
		return ( Keyword( delKeyword )  +  self.targetList() ).action( lambda input, pos, xs: Nodes.DelStmt( target=xs[1] ) )



	# Return statement
	@Rule
	def returnStmt(self):
		return ( Keyword( returnKeyword )  +  self.tupleOrExpression() ).action( lambda input, pos, xs: Nodes.ReturnStmt( value=xs[1] ) )



	# Yield statement
	@Rule
	def yieldStmt(self):
		return ( Keyword( yieldKeyword )  +  self.expression() ).action( lambda input, pos, xs: Nodes.YieldStmt( value=xs[1] ) )




	# Raise statement
	def _raiseFlatten(self, xs, level):
		if xs is None:
			return [ makeNullNode() ] * level
		else:
			if xs[0] == ',':
				xs = xs[1:]
			if len( xs ) == 2:
				return [ xs[0] ]  +  self._raiseFlatten( xs[1], level - 1 )
			else:
				return [ xs[0] ]

	@Rule
	def raiseStmt(self):
		return ( Keyword( raiseKeyword ) + Optional( self.expression() + Optional( Literal( ',' ) + self.expression() + Optional( Literal( ',' ) + self.expression() ) ) ) ).action( lambda input, pos, xs: [ 'raiseStmt', ]  +  self._raiseFlatten( xs[1], 3 ) )




	# Break statement
	@Rule
	def breakStmt(self):
		return Keyword( breakKeyword ).action( lambda input, pos, xs: [ 'breakStmt' ] )




	# Continue statement
	@Rule
	def continueStmt(self):
		return Keyword( continueKeyword ).action( lambda input, pos, xs: [ 'continueStmt' ] )




	# Import statement
	@Rule
	def _moduleIdentifier(self):
		return self.pythonIdentifier()

	# dotted name
	@Rule
	def moduleName(self):
		return separatedList( self._moduleIdentifier(), '.', True, False, False ).action( lambda input, pos, xs: '.'.join( xs ) )


	# relative module name
	@Rule
	def _relModDotsModule(self):
		return ( ZeroOrMore( '.' ) + self.moduleName() ).action( lambda input, pos, xs: ''.join( xs[0] )  +  xs[1] )

	@Rule
	def _relModDots(self):
		return OneOrMore( '.' ).action( lambda input, pos, xs: ''.join( xs ) )

	@Rule
	def relativeModule(self):
		return ( self._relModDotsModule() | self._relModDots() ).action( lambda input, pos, xs: [ 'relativeModule', xs ] )


	# ( <moduleName> 'as' <pythonIdentifier> )  |  <moduleName>
	@Rule
	def moduleImport(self):
		return ( self.moduleName() + Keyword( asKeyword ) + self.pythonIdentifier() ).action( lambda input, pos, xs: [ 'moduleImportAs', xs[0], xs[2] ] )   |   self.moduleName().action( lambda input, pos, xs: [ 'moduleImport', xs ] )


	# 'import' <separatedList( moduleImport )>
	@Rule
	def simpleImport(self):
		return ( Keyword( importKeyword )  +  separatedList( self.moduleImport(), True, False, False ) ).action( lambda input, pos, xs: [ 'importStmt' ] + xs[1] )


	# ( <pythonIdentifier> 'as' <pythonIdentifier> )  |  <pythonIdentifier>
	@Rule
	def moduleContentImport(self):
		return ( self.pythonIdentifier() + Keyword( asKeyword ) + self.pythonIdentifier() ).action( lambda input, pos, xs: [ 'moduleContentImportAs', xs[0], xs[2] ] )   |   \
		       self.pythonIdentifier().action( lambda input, pos, xs: [ 'moduleContentImport', xs ] )


	# 'from' <relativeModule> 'import' ( <separatedList( moduleContentImport )>  |  ( '(' <separatedList( moduleContentImport )> ',' ')' )
	@Rule
	def fromImport(self):
		return ( Keyword( fromKeyword ) + self.relativeModule() + Keyword( importKeyword ) + \
			 (  \
				 separatedList( self.moduleContentImport(), True, False, False )  |  \
				 ( Literal( '(' )  +  separatedList( self.moduleContentImport(), True, True, False )  +  Literal( ')' ) ).action( lambda input, pos, xs: xs[1] )  \
				 )  \
			 ).action( lambda input, pos, xs: [ 'fromImportStmt', xs[1] ] + xs[3] )


	# 'from' <relativeModule> 'import' '*'
	@Rule
	def fromImportAll(self):
		return ( Keyword( fromKeyword ) + self.relativeModule() + Keyword( importKeyword ) + '*' ).action( lambda input, pos, xs: [ 'fromImportAllStmt', xs[1] ] )


	# Final :::
	@Rule
	def importStmt(self):
		return self.simpleImport() | self.fromImport() | self.fromImportAll()




	# Global statement
	@Rule
	def globalVar(self):
		return self.pythonIdentifier().action( lambda input, pos, xs: [ 'globalVar', xs ] )

	@Rule
	def globalStmt(self):
		return ( Keyword( globalKeyword )  +  separatedList( self.globalVar(), True, False, False ) ).action( lambda input, pos, xs: [ 'globalStmt' ]  +  xs[1] )





	# Exec statement
	@Rule
	def execCodeStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp() ).action( lambda input, pos, xs: [ 'execStmt', xs[1], makeNullNode(), makeNullNode() ] )

	@Rule
	def execCodeInLocalsStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp()  +  Keyword( inKeyword )  +  self.expression() ).action( lambda input, pos, xs: [ 'execStmt', xs[1], xs[3], makeNullNode() ] )

	@Rule
	def execCodeInLocalsAndGlobalsStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp()  +  Keyword( inKeyword )  +  self.expression()  +  ','  +  self.expression() ).action( lambda input, pos, xs: [ 'execStmt', xs[1], xs[3], xs[5] ] )

	@Rule
	def execStmt(self):
		return self.execCodeInLocalsAndGlobalsStmt() | self.execCodeInLocalsStmt() | self.execCodeStmt()




	# If statement
	@Rule
	def ifStmt(self):
		return ( Keyword( ifKeyword )  +  self.expression()  +  ':' ).action( lambda input, pos, xs: [ 'ifStmt', xs[1], [] ] )



	# Elif statement
	@Rule
	def elifStmt(self):
		return ( Keyword( elifKeyword )  +  self.expression()  +  ':' ).action( lambda input, pos, xs: [ 'elifStmt', xs[1], [] ] )



	# Else statement
	@Rule
	def elseStmt(self):
		return( Keyword( elseKeyword )  +  ':' ).action( lambda input, pos, xs: [ 'elseStmt', [] ] )



	# While statement
	@Rule
	def whileStmt(self):
		return ( Keyword( whileKeyword )  +  self.expression()  +  ':' ).action( lambda input, pos, xs: [ 'whileStmt', xs[1], [] ] )



	# For statement
	@Rule
	def forStmt(self):
		return ( Keyword( forKeyword )  +  self.targetList()  +  Keyword( inKeyword )  +  self.tupleOrExpression()  +  ':' ).action( lambda input, pos, xs: [ 'forStmt', xs[1], xs[3], [] ] )



	# Try statement
	@Rule
	def tryStmt(self):
		return ( Keyword( tryKeyword )  +  ':' ).action( lambda input, pos, xs: [ 'tryStmt', [] ] )




	# Except statement
	@Rule
	def exceptAllStmt(self):
		return ( Keyword( exceptKeyword ) + ':' ).action( lambda input, pos, xs: [ 'exceptStmt', makeNullNode(), makeNullNode(), [] ] )

	@Rule
	def exceptExcStmt(self):
		return ( Keyword( exceptKeyword )  +  self.expression() + ':' ).action( lambda input, pos, xs: [ 'exceptStmt', xs[1], makeNullNode(), [] ] )

	@Rule
	def exceptExcIntoTargetStmt(self):
		return ( Keyword( exceptKeyword )  +  self.expression()  +  ','  +  self.targetItem() + ':' ).action( lambda input, pos, xs: [ 'exceptStmt', xs[1], xs[3], [] ] )

	@Rule
	def exceptStmt(self):
		return self.exceptExcIntoTargetStmt() | self.exceptExcStmt() | self.exceptAllStmt()




	# Finally statement
	@Rule
	def finallyStmt(self):
		return ( Keyword( finallyKeyword )  +  ':' ).action( lambda input, pos, xs: [ 'finallyStmt', [] ] )



	# With statement
	@Rule
	def withStmt(self):
		return ( Keyword( withKeyword )  +  self.expression()  +  Optional( Keyword( asKeyword )  +  self.targetItem() )  +  ':' ).action( lambda input, pos, xs: [ 'withStmt', xs[1], xs[2][1]   if xs[2] is not None   else   makeNullNode(), [] ] )



	# Def statement
	@Rule
	def defStmt(self):
		return ( Keyword( defKeyword )  +  self.pythonIdentifier()  +  '('  +  self.params()  +  ')'  +  ':' ).action( lambda input, pos, xs: [ 'defStmt', xs[1], xs[3], [] ] )



	# Decorator statement
	@Rule
	def decoStmt(self):
		return ( Literal( '@' )  +  self.dottedPythonIdentifer()  +  Optional( Literal( '(' )  +  self.callArgs()  +  ')' ) ).action( lambda input, pos, xs: [ 'decoStmt', xs[1], xs[2][1]   if xs[2] is not None   else   makeNullNode() ] )



	# Class statement
	@Rule
	def classStmt(self):
		return ( Keyword( classKeyword )  +  self.pythonIdentifier()  +  Optional( Literal( '(' )  +  self.expressionList()  +  ')' )  +  ':' ).action( lambda input, pos, xs: [ 'classStmt', xs[1], xs[2][1]   if xs[2] is not None   else   makeNullNode(), [] ] )



	# Comment statement
	@Rule
	def commentStmt(self):
		return ( Literal( '#' )  +  Word( string.printable ) ).action( lambda input, pos, xs: [ 'commentStmt', xs[1] ] )





	# Statements
	@Rule
	def simpleStmt(self):
		return self.assertStmt() | self.assignmentStmt() | self.augAssignStmt() | self.passStmt() | self.delStmt() | self.returnStmt() | self.yieldStmt() | self.raiseStmt() | self.breakStmt() | \
		       self.continueStmt() | self.importStmt() | self.globalStmt() | self.execStmt()

	@Rule
	def compoundStmtHeader(self):
		return self.ifStmt() | self.elifStmt() | self.elseStmt() | self.whileStmt() | self.forStmt() | self.tryStmt() | self.exceptStmt() | self.finallyStmt() | self.withStmt() | self.defStmt() | self.decoStmt() | self.classStmt()

	@Rule
	def statement(self):
		return self.simpleStmt() | self.compoundStmtHeader() | self.commentStmt() | self.expression()






import unittest


class TestCase_Python25Parser (ParserTestCase):
	def test_shortStringLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '\'abc\'', Nodes.StringLiteral( format='ascii', quotation='single', value='abc' ) )
		self._matchTest( g.expression(), '\"abc\"', Nodes.StringLiteral( format='ascii', quotation='double', value='abc' ) )
		self._matchTest( g.expression(), 'u\'abc\'', Nodes.StringLiteral( format='unicode', quotation='single', value='abc' ) )
		self._matchTest( g.expression(), 'u\"abc\"', Nodes.StringLiteral( format='unicode', quotation='double', value='abc' ) )
		self._matchTest( g.expression(), 'r\'abc\'', Nodes.StringLiteral( format='ascii-regex', quotation='single', value='abc' ) )
		self._matchTest( g.expression(), 'r\"abc\"', Nodes.StringLiteral( format='ascii-regex', quotation='double', value='abc' ) )
		self._matchTest( g.expression(), 'ur\'abc\'', Nodes.StringLiteral( format='unicode-regex', quotation='single', value='abc' ) )
		self._matchTest( g.expression(), 'ur\"abc\"', Nodes.StringLiteral( format='unicode-regex', quotation='double', value='abc' ) )


	def test_integerLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '123', Nodes.IntLiteral( format='decimal', numType='int', value='123' ) )
		self._matchTest( g.expression(), '123L', Nodes.IntLiteral( format='decimal', numType='long', value='123' ) )
		self._matchTest( g.expression(), '0x123', Nodes.IntLiteral( format='hex', numType='int', value='0x123' ) )
		self._matchTest( g.expression(), '0x123L', Nodes.IntLiteral( format='hex', numType='long', value='0x123' ) )


	def test_floatLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '123.0', Nodes.FloatLiteral( value='123.0' ) )


	def test_imaginaryLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '123.0j', Nodes.ImaginaryLiteral( value='123.0j' ) )


	def testTargets(self):
		g = Python25Grammar()
		self._matchTest( g.targetList(), 'a', Nodes.SingleTarget( name='a' ) )
		self._matchTest( g.targetList(), '(a)', Nodes.SingleTarget( name='a' ) )

		self._matchTest( g.targetList(), '(a,)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ) )
		self._matchTest( g.targetList(), 'a,b', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._matchTest( g.targetList(), '(a,b)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._matchTest( g.targetList(), '(a,b,)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._matchTest( g.targetList(), '(a,b),(c,d)', Nodes.TupleTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ] ),
											     Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='c' ), Nodes.SingleTarget( name='d' ) ] ) ] ) )

		self._matchFailTest( g.targetList(), '(a,) (b,)' )

		self._matchTest( g.targetList(), '[a]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ) )
		self._matchTest( g.targetList(), '[a,]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ) )
		self._matchTest( g.targetList(), '[a,b]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._matchTest( g.targetList(), '[a,b,]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._matchTest( g.targetList(), '[a],[b,]', Nodes.TupleTarget( targets=[ Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ), Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='b' ) ] ) ] ) )
		self._matchTest( g.targetList(), '[(a,)],[(b,)]', Nodes.TupleTarget( targets=[ Nodes.ListTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ) ] ),
											       Nodes.ListTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='b' ) ] ) ] ) ] ) )

		self._matchTest( g.subscript(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.attributeRef() | g.subscript(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetItem(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetList(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetList(), 'a[x][y]', Nodes.Subscript( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ), index=Nodes.Load( name='y' ) ) )
		self._matchTest( g.targetList(), 'a.b', Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ) )
		self._matchTest( g.targetList(), 'a.b.c', Nodes.AttributeRef( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), name='c' ) )

		self._matchTest( g.targetList(), 'a.b[x]', Nodes.Subscript( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetList(), 'a[x].b', Nodes.AttributeRef( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ), name='b' ) )


	def testListLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '[a,b]', Nodes.ListLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		self._matchTest( g.expression(), '[a,b,]', Nodes.ListLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )


	def testListComprehension(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '[i  for i in a]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
										    comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ) ]
										    ) )
		self._matchFailTest( g.expression(), '[i  if x]', )
		self._matchTest( g.expression(), '[i  for i in a  if x]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
											  comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
													       Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ) ]
											  ) )
		self._matchTest( g.expression(), '[i  for i in a  for j in b]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
												comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
														     Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
												) )
		self._matchTest( g.expression(), '[i  for i in a  if x  for j in b]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
												      comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
															   Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
															   Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
												      ) )
		self._matchTest( g.expression(), '[i  for i in a  if x  for j in b  if y]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
													    comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
																 Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
																 Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ),
																 Nodes.ComprehensionIf( condition=Nodes.Load( name='y' ) ) ]
													    ) )



	def testGeneratorExpression(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '(i  for i in a)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
											 comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ) ]
											 ) )
		self._matchFailTest( g.expression(), '(i  if x)', )
		self._matchTest( g.expression(), '(i  for i in a  if x)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
											       comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
														    Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ) ]
											       ) )
		self._matchTest( g.expression(), '(i  for i in a  for j in b)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
												     comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
															  Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
												     ) )
		self._matchTest( g.expression(), '(i  for i in a  if x  for j in b)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
													   comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
																Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
																Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
													   ) )
		self._matchTest( g.expression(), '(i  for i in a  if x  for j in b  if y)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
														 comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
																      Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
																      Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ),
																      Nodes.ComprehensionIf( condition=Nodes.Load( name='y' ) ) ]
														 ) )


	def testDictLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '{a:x,b:y}', Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=Nodes.Load( name='x' ) ),
											  Nodes.DictKeyValuePair( key=Nodes.Load( name='b' ), value=Nodes.Load( name='y' ) ) ] ) )
		self._matchTest( g.expression(), '{a:x,b:y,}', Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=Nodes.Load( name='x' ) ),
											   Nodes.DictKeyValuePair( key=Nodes.Load( name='b' ), value=Nodes.Load( name='y' ) ) ] ) )


	def testYieldExpression(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '(yield 2+3)', Nodes.YieldExpr( value=Nodes.Add( x=Nodes.IntLiteral( format='decimal', numType='int', value='2' ), y=Nodes.IntLiteral( format='decimal', numType='int', value='3' ) ) ) )



	def testAttributeRef(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'a.b', Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ) )


	def testSubscript(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.expression(), 'a[x:p]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ) ) ) )
		self._matchTest( g.expression(), 'a[x:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=makeNullNode() ) ) )
		self._matchTest( g.expression(), 'a[:p]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=makeNullNode(), upper=Nodes.Load( name='p' ) ) ) )
		self._matchTest( g.expression(), 'a[:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=makeNullNode(), upper=makeNullNode() ) ) )
		self._matchTest( g.expression(), 'a[x:p:f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[x:p:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=makeNullNode() ) ) )
		self._matchTest( g.expression(), 'a[x::f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=makeNullNode(), stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[:p:f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=makeNullNode(), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[::]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=makeNullNode(), upper=makeNullNode(), stride=makeNullNode() ) ) )
		self._matchTest( g.expression(), 'a[::f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=makeNullNode(), upper=makeNullNode(), stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[x::]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=makeNullNode(), stride=makeNullNode() ) ) )
		self._matchTest( g.expression(), 'a[:p:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=makeNullNode(), upper=Nodes.Load( name='p' ), stride=makeNullNode() ) ) )
		self._matchTest( g.expression(), 'a[x,y]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptTuple( values=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ) ) )
		self._matchTest( g.expression(), 'a[x:p,y:q]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptTuple( values=[ Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ) ), Nodes.SubscriptSlice( lower=Nodes.Load( name='y' ), upper=Nodes.Load( name='q' ) ) ] ) ) )
		self._matchTest( g.expression(), 'a[x:p:f,y:q:g]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptTuple( values=[ Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ), Nodes.SubscriptLongSlice( lower=Nodes.Load( name='y' ), upper=Nodes.Load( name='q' ), stride=Nodes.Load( name='g' ) ) ] ) ) )
		self._matchTest( g.expression(), 'a[x:p:f,y:q:g,...]', Nodes.Subscript( target=Nodes.Load( name='a' ),
									index=Nodes.SubscriptTuple( values=[ Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ),
										Nodes.SubscriptLongSlice( lower=Nodes.Load( name='y' ), upper=Nodes.Load( name='q' ), stride=Nodes.Load( name='g' ) ),
										Nodes.SubscriptEllipsis() ] ) ) )



	def testCall(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'a()', Nodes.Call( target=Nodes.Load( name='a' ), args=[] ) )
		self._matchTest( g.expression(), 'a(f)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ) ] ) )
		self._matchTest( g.expression(), 'a(f,)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ) ] ) )
		self._matchTest( g.expression(), 'a(f,g)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ) ] ) )
		self._matchTest( g.expression(), 'a(f,g,m=a)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ) ] ) )
		self._matchTest( g.expression(), 'a(f,g,m=a,n=b)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallKWArg( name='n', value=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a(f,g,m=a,n=b,*p)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallKWArg( name='n', value=Nodes.Load( name='b' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ) ] ) )
		self._matchTest( g.expression(), 'a(f,m=a,*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._matchTest( g.expression(), 'a(f,m=a,*p)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ) ] ) )
		self._matchTest( g.expression(), 'a(f,m=a,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._matchTest( g.expression(), 'a(f,*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._matchTest( g.expression(), 'a(m=a,*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._matchTest( g.expression(), 'a(*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._matchTest( g.expression(), 'a(**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._matchFailTest( g.expression(), 'a(m=a,f)' )
		self._matchFailTest( g.expression(), 'a(*p,f)' )
		self._matchFailTest( g.expression(), 'a(**w,f)' )
		self._matchFailTest( g.expression(), 'a(*p,m=a)' )
		self._matchFailTest( g.expression(), 'a(**w,m=a)' )
		self._matchFailTest( g.expression(), 'a(**w,*p)' )
		
		
		
	def testOperators(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'a**b', Nodes.Pow( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), '~a', Nodes.Invert( x=Nodes.Load( name='a' ) ) )
		self._matchTest( g.expression(), '-a', Nodes.Negate( x=Nodes.Load( name='a' ) ) )
		self._matchTest( g.expression(), '+a', Nodes.Pos( x=Nodes.Load( name='a' ) ) )
		self._matchTest( g.expression(), 'a*b', Nodes.Mul( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a/b', Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a%b', Nodes.Mod( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a+b', Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a-b', Nodes.Sub( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a<<b', Nodes.LShift( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a>>b', Nodes.RShift( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a&b', Nodes.BitAnd( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a^b', Nodes.BitXor( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a|b', Nodes.BitOr( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a<=b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpLte( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a<b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpLt( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a>=b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpGte( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a>b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpGt( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a==b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpEq( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a!=b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpNeq( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a is not b', Nodes.IsNotTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a is b', Nodes.IsTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a not in b', Nodes.NotInTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a in b', Nodes.InTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'not a', Nodes.NotTest( x=Nodes.Load( name='a' ) ) )
		self._matchTest( g.expression(), 'a and b', Nodes.AndTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a or b', Nodes.OrTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )

		
		
	def testParams(self):
		g = Python25Grammar()
		self._matchTest( g.params(), '', [] )
		self._matchTest( g.params(), 'f', [ Nodes.SimpleParam( name='f' ) ] )
		self._matchTest( g.params(), 'f,', [ Nodes.SimpleParam( name='f' ) ] )
		self._matchTest( g.params(), 'f,g', [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ) ] )
		self._matchTest( g.params(), 'f,g,m=a', [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ) ] )
		self._matchTest( g.params(), 'f,g,m=a,n=b', [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ) ] )
		self._matchTest( g.params(), 'f,g,m=a,n=b,*p', [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ), Nodes.ParamList( name='p' ) ] )
		self._matchTest( g.params(), 'f,m=a,*p,**w', [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ] )
		self._matchTest( g.params(), 'f,m=a,*p', [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ) ] )
		self._matchTest( g.params(), 'f,m=a,**w', [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.KWParamList( name='w' ) ] )
		self._matchTest( g.params(), 'f,*p,**w', [ Nodes.SimpleParam( name='f' ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ] )
		self._matchTest( g.params(), 'm=a,*p,**w', [ Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ] )
		self._matchTest( g.params(), '*p,**w', [ Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ] )
		self._matchTest( g.params(), '**w', [ Nodes.KWParamList( name='w' ) ] )
		self._matchFailTest( g.params(), 'm=a,f' )
		self._matchFailTest( g.params(), '*p,f' )
		self._matchFailTest( g.params(), '**w,f' )
		self._matchFailTest( g.params(), '*p,m=a' )
		self._matchFailTest( g.params(), '**w,m=a' )
		self._matchFailTest( g.params(), '**w,*p' )



	def testLambda(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'lambda f,m=a,*p,**w: f+m+p+w', Nodes.LambdaExpr( 
			params=[ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ],
			expr=Nodes.Add( x=Nodes.Add( x=Nodes.Add( x=Nodes.Load( name='f' ), y=Nodes.Load( name='m' ) ), y=Nodes.Load( name='p' ) ), y=Nodes.Load( name='w' ) ) ) )



	def testConditionalExpr(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'x   if y else   z', Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ) )
		self._matchTest( g.expression(), '(x   if y else   z)   if w else   q', Nodes.ConditionalExpr( condition=Nodes.Load( name='w' ), expr=Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ), elseExpr=Nodes.Load( name='q' ) ) )
		self._matchTest( g.expression(), 'w   if (x   if y else   z) else   q', Nodes.ConditionalExpr( condition=Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ), expr=Nodes.Load( name='w' ), elseExpr=Nodes.Load( name='q' ) ) )
		self._matchTest( g.expression(), 'w   if q else   x   if y else   z', Nodes.ConditionalExpr( condition=Nodes.Load( name='q' ), expr=Nodes.Load( name='w' ), elseExpr=Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ) ) )
		self._matchFailTest( g.expression(), 'w   if x   if y else   z else   q' )



	def testTupleOrExpression(self):
		g = Python25Grammar()
		self._matchTest( g.tupleOrExpression(), 'a', Nodes.Load( name='a' ) )
		self._matchTest( g.tupleOrExpression(), 'a,b', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		self._matchTest( g.tupleOrExpression(), 'a,2', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.IntLiteral( format='decimal', numType='int', value='2' ) ] ) )
		self._matchTest( g.tupleOrExpression(), 'lambda x, y: x+y,2', Nodes.TupleLiteral(
			values=[ Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='x' ), Nodes.SimpleParam( name='y' ) ],
						   expr=Nodes.Add( x=Nodes.Load( name='x' ), y=Nodes.Load( name='y' ) ) ),
				Nodes.IntLiteral( format='decimal', numType='int', value='2' ) ] ) )



	def testAssertStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'assert x', Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=makeNullNode() ) )
		self._matchTest( g.statement(), 'assert x,y', Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=Nodes.Load( name='y' ) ) )


	def testAssignmentStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'a=x', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.Load( name='x' ) ) )
		self._matchTest( g.statement(), 'a=b=x', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ], value=Nodes.Load( name='x' ) ) )
		self._matchTest( g.statement(), 'a,b=c,d=x', Nodes.AssignStmt( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ),
										   Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='c' ),  Nodes.SingleTarget( name='d' ) ] ) ], value=Nodes.Load( name='x' ) ) )
		self._matchTest( g.statement(), 'a=yield x', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.YieldExpr( value=Nodes.Load( name='x' ) ) ) )
		self._matchFailTest( g.statement(), '=x' )


	def testAugAssignStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'a += b', Nodes.AugAssignStmt( op='+=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a -= b', Nodes.AugAssignStmt( op='-=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a *= b', Nodes.AugAssignStmt( op='*=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a /= b', Nodes.AugAssignStmt( op='/=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a %= b', Nodes.AugAssignStmt( op='%=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a **= b', Nodes.AugAssignStmt( op='**=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a >>= b', Nodes.AugAssignStmt( op='>>=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a <<= b', Nodes.AugAssignStmt( op='<<=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a &= b', Nodes.AugAssignStmt( op='&=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a ^= b', Nodes.AugAssignStmt( op='^=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._matchTest( g.statement(), 'a |= b', Nodes.AugAssignStmt( op='|=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )


	def testPassStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'pass', Nodes.PassStmt() )


	def testDelStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'del x', Nodes.DelStmt( target=Nodes.SingleTarget( name='x' ) ) )


	def testReturnStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'return x', Nodes.ReturnStmt( value=Nodes.Load( name='x' ) ) )


	def testYieldStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'yield x', Nodes.YieldStmt( value=Nodes.Load( name='x' ) ) )


	def testRaiseStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'raise', [ 'raiseStmt', makeNullNode(), makeNullNode(), makeNullNode() ] )
		self._matchTest( g.statement(), 'raise x', [ 'raiseStmt', Nodes.Load( name='x' ), makeNullNode(), makeNullNode() ] )
		self._matchTest( g.statement(), 'raise x,y', [ 'raiseStmt', Nodes.Load( name='x' ), Nodes.Load( name='y' ), makeNullNode() ] )
		self._matchTest( g.statement(), 'raise x,y,z', [ 'raiseStmt', Nodes.Load( name='x' ), Nodes.Load( name='y' ), Nodes.Load( name='z' ) ] )


	def testBreakStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'break', [ 'breakStmt' ] )


	def testContinueStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'continue', [ 'continueStmt' ] )


	def testImportStmt(self):
		g = Python25Grammar()
		self._matchTest( g._moduleIdentifier(), 'abc', 'abc' )
		self._matchTest( g.moduleName(), 'abc', 'abc' )
		self._matchTest( g.moduleName(), 'abc.xyz', 'abc.xyz' )
		self._matchTest( g._relModDotsModule(), 'abc.xyz', 'abc.xyz' )
		self._matchTest( g._relModDotsModule(), '...abc.xyz', '...abc.xyz' )
		self._matchTest( g._relModDots(), '...', '...' )
		self._matchTest( g.relativeModule(), 'abc.xyz', [ 'relativeModule', 'abc.xyz' ] )
		self._matchTest( g.relativeModule(), '...abc.xyz', [ 'relativeModule', '...abc.xyz' ] )
		self._matchTest( g.relativeModule(), '...', [ 'relativeModule', '...' ] )
		self._matchTest( g.moduleImport(), 'abc.xyz', [ 'moduleImport', 'abc.xyz' ] )
		self._matchTest( g.moduleImport(), 'abc.xyz as q', [ 'moduleImportAs', 'abc.xyz', 'q' ] )
		self._matchTest( g.simpleImport(), 'import a', [ 'importStmt', [ 'moduleImport', 'a' ] ] )
		self._matchTest( g.simpleImport(), 'import a.b', [ 'importStmt', [ 'moduleImport', 'a.b' ] ] )
		self._matchTest( g.simpleImport(), 'import a.b as x', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ] ] )
		self._matchTest( g.simpleImport(), 'import a.b as x, c.d as y', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ], [ 'moduleImportAs', 'c.d', 'y' ] ] )
		self._matchTest( g.moduleContentImport(), 'xyz', [ 'moduleContentImport', 'xyz' ] )
		self._matchTest( g.moduleContentImport(), 'xyz as q', [ 'moduleContentImportAs', 'xyz', 'q' ] )
		self._matchTest( g.fromImport(), 'from x import a', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( g.fromImport(), 'from x import a as p', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( g.fromImport(), 'from x import a as p, b as q', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( g.fromImport(), 'from x import (a)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( g.fromImport(), 'from x import (a,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( g.fromImport(), 'from x import (a as p)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( g.fromImport(), 'from x import (a as p,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( g.fromImport(), 'from x import ( a as p, b as q )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( g.fromImport(), 'from x import ( a as p, b as q, )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( g.fromImportAll(), 'from x import *', [ 'fromImportAllStmt', [ 'relativeModule', 'x' ] ] )
		self._matchTest( g.importStmt(), 'import a', [ 'importStmt', [ 'moduleImport', 'a' ] ] )
		self._matchTest( g.importStmt(), 'import a.b', [ 'importStmt', [ 'moduleImport', 'a.b' ] ] )
		self._matchTest( g.importStmt(), 'import a.b as x', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ] ] )
		self._matchTest( g.importStmt(), 'import a.b as x, c.d as y', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ], [ 'moduleImportAs', 'c.d', 'y' ] ] )
		self._matchTest( g.importStmt(), 'from x import a', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( g.importStmt(), 'from x import a as p', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( g.importStmt(), 'from x import a as p, b as q', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( g.importStmt(), 'from x import (a)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( g.importStmt(), 'from x import (a,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( g.importStmt(), 'from x import (a as p)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( g.importStmt(), 'from x import (a as p,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( g.importStmt(), 'from x import ( a as p, b as q )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( g.importStmt(), 'from x import ( a as p, b as q, )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( g.importStmt(), 'from x import *', [ 'fromImportAllStmt', [ 'relativeModule', 'x' ] ] )


	def testGlobalStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'global x', [ 'globalStmt', [ 'globalVar', 'x' ] ] )
		self._matchTest( g.statement(), 'global x, y', [ 'globalStmt', [ 'globalVar', 'x' ], [ 'globalVar', 'y' ] ] )


	def testExecStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'exec a', [ 'execStmt', Nodes.Load( name='a' ), makeNullNode(), makeNullNode() ] )
		self._matchTest( g.statement(), 'exec a in b', [ 'execStmt', Nodes.Load( name='a' ), Nodes.Load( name='b' ), makeNullNode() ] )
		self._matchTest( g.statement(), 'exec a in b,c', [ 'execStmt', Nodes.Load( name='a' ), Nodes.Load( name='b' ), Nodes.Load( name='c' ) ] )


	def testIfStmt(self):
		g = Python25Grammar()
		self._matchTest( g.ifStmt(), 'if a:', [ 'ifStmt', Nodes.Load( name='a' ), [] ] )


	def testElIfStmt(self):
		g = Python25Grammar()
		self._matchTest( g.elifStmt(), 'elif a:', [ 'elifStmt', Nodes.Load( name='a' ), [] ] )


	def testElseStmt(self):
		g = Python25Grammar()
		self._matchTest( g.elseStmt(), 'else:', [ 'elseStmt', [] ] )


	def testWhileStmt(self):
		g = Python25Grammar()
		self._matchTest( g.whileStmt(), 'while a:', [ 'whileStmt', Nodes.Load( name='a' ), [] ] )


	def testForStmt(self):
		g = Python25Grammar()
		self._matchTest( g.forStmt(), 'for x in y:', [ 'forStmt', Nodes.SingleTarget( name='x' ), Nodes.Load( name='y' ), [] ] )


	def testTryStmt(self):
		g = Python25Grammar()
		self._matchTest( g.tryStmt(), 'try:', [ 'tryStmt', [] ] )


	def testExceptStmt(self):
		g = Python25Grammar()
		self._matchTest( g.exceptStmt(), 'except:', [ 'exceptStmt', makeNullNode(), makeNullNode(), [] ] )
		self._matchTest( g.exceptStmt(), 'except x:', [ 'exceptStmt', Nodes.Load( name='x' ), makeNullNode(), [] ] )
		self._matchTest( g.exceptStmt(), 'except x, y:', [ 'exceptStmt', Nodes.Load( name='x' ), Nodes.SingleTarget( name='y' ), [] ] )


	def testFinallyStmt(self):
		g = Python25Grammar()
		self._matchTest( g.finallyStmt(), 'finally:', [ 'finallyStmt', [] ] )


	def testWithStmt(self):
		g = Python25Grammar()
		self._matchTest( g.withStmt(), 'with a:', [ 'withStmt', Nodes.Load( name='a' ), makeNullNode(), [] ] )
		self._matchTest( g.withStmt(), 'with a as b:', [ 'withStmt', Nodes.Load( name='a' ), Nodes.SingleTarget( name='b' ), [] ] )


	def testDefStmt(self):
		g = Python25Grammar()
		self._matchTest( g.defStmt(), 'def f():', [ 'defStmt', 'f', [], [] ] )
		self._matchTest( g.defStmt(), 'def f(x):', [ 'defStmt', 'f', [ Nodes.SimpleParam( name='x' ) ], [] ] )


	def testDecoStmt(self):
		g = Python25Grammar()
		self._matchTest( g.decoStmt(), '@f', [ 'decoStmt', 'f', makeNullNode() ] )
		self._matchTest( g.decoStmt(), '@f(x)', [ 'decoStmt', 'f', [ Nodes.Load( name='x' ) ] ] )


	def testClassStmt(self):
		g = Python25Grammar()
		self._matchTest( g.classStmt(), 'class Q:', [ 'classStmt', 'Q', makeNullNode(), [] ] )
		self._matchTest( g.classStmt(), 'class Q (x):', [ 'classStmt', 'Q', [ Nodes.Load( name='x' ) ], [] ] )
		self._matchTest( g.classStmt(), 'class Q (x,y):', [ 'classStmt', 'Q', [ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ], [] ] )


	def testCommentStmt(self):
		g = Python25Grammar()
		self._matchTest( g.commentStmt(), '#x', [ 'commentStmt', 'x' ] )
		self._matchTest( g.commentStmt(), '#' + string.printable, [ 'commentStmt', string.printable ] )





	def testFnCallStStmt(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'x.y()', Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) )
		self._matchTest( g.statement(), 'x.y()', Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) )




	def testDictInList(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'y = [ x, { a : b } ]', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='y' ) ], value=Nodes.ListLiteral( values=[ Nodes.Load( name='x' ), Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=Nodes.Load( name='b' ) ) ] ) ] ) ) )




if __name__ == '__main__':
	#result, pos, dot = targetList.debugParseString( 'a.b' )
	result, pos, dot = subscript.debugParseString( 'a.b' )
	print dot
