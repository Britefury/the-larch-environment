##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string

from BritefuryJ.DocModel import DMObject, DMNode

from BritefuryJ.Parser import Action, Condition, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, SeparatedList, ObjectNode
from BritefuryJ.Parser.Utils.Tokens import identifier, decimalInteger, hexInteger, integer, singleQuotedString, doubleQuotedString, quotedString, floatingPoint
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, UnaryOperator, BinaryOperator, ChainOperator, OperatorTable
from BritefuryJ.Parser.ItemStream import ItemStreamBuilder

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction

from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase

from Britefury.Util.NodeUtil import isStringNode

from Britefury.Grammar.Grammar import Grammar, Rule, RuleList

from GSymCore.Languages.Python25.Keywords import *
import GSymCore.Languages.Python25.NodeClasses as Nodes



#
#
#
# !!!!!! NOTES !!!!!!
# Print statements are not handled correctly
#
# Octal integers not handled correctly
#
# from-import statements are parsed, but information on whether the imports were wrapped in parens, or had a trailing separator, is not obtained
#


_identity = DefaultIdentityTransformationFunction()
def _updatedNodeCopy(node, xform, **fieldValues):
	newNode = _identity( node, xform )
	newNode.update( fieldValues )
	return newNode


def _incrementParens(node):
	def _xform(node, innerNodeXform):
		return node
	p = node['parens']
	numParens = 0
	if p is not None   and   isStringNode( p ):
		p = str( p )
		try:
			numParens = int( p )
		except ValueError:
			pass
	numParens += 1
	return _updatedNodeCopy( node, _xform, parens=str( numParens ) )






class Python25Grammar (Grammar):
	#
	#
	# BASICS
	#
	#
	
	
	# Python identifier
	@Rule
	def pythonIdentifier(self):
		return identifier  &  ( lambda input, begin, end, x, bindings: x not in keywordsSet )


	@Rule
	def dottedPythonIdentifer(self):
		return SeparatedList( self.pythonIdentifier(), '.', 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ).action( lambda input, begin, end, xs, bindings: '.'.join( xs ) )



	# Attribute name
	@Rule
	def attrName(self):
		return self.pythonIdentifier()





	#
	#
	# LITERALS
	#
	#
	
	
	
	
	# String literal
	@Rule
	def asciiStringSLiteral(self):
		return singleQuotedString.action( lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='ascii', quotation='single', value=xs[1:-1] ) )

	@Rule
	def asciiStringDLiteral(self):
		return doubleQuotedString.action( lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='ascii', quotation='double', value=xs[1:-1] ) )

	@Rule
	def unicodeStringSLiteral(self):
		return ( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + singleQuotedString ).action( lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='unicode', quotation='single', value=xs[0][1:-1] ) )

	@Rule
	def unicodeStringDLiteral(self):
		return ( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + doubleQuotedString ).action( lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='unicode', quotation='double', value=xs[0][1:-1] ) )

	@Rule
	def regexAsciiStringSLiteral(self):
		return ( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + singleQuotedString ).action( lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='ascii-regex', quotation='single', value=xs[0][1:-1] ) )

	@Rule
	def regexAsciiStringDLiteral(self):
		return ( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + doubleQuotedString ).action( lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='ascii-regex', quotation='double', value=xs[0][1:-1] ) )

	@Rule
	def regexUnicodeStringSLiteral(sefl):
		return ( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + singleQuotedString ).action(
			lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='unicode-regex', quotation='single', value=xs[0][1:-1] ) )

	@Rule
	def regexUnicodeStringDLiteral(self):
		return ( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + doubleQuotedString ).action(
			lambda input, begin, end, xs, bindings: Nodes.StringLiteral( format='unicode-regex', quotation='double', value=xs[0][1:-1] ) )

	@Rule
	def shortStringLiteral(self):
		return self.asciiStringSLiteral() | self.asciiStringDLiteral() | self.unicodeStringSLiteral() | self.unicodeStringDLiteral() | self.regexAsciiStringSLiteral() | self.regexAsciiStringDLiteral() | \
		       self.regexUnicodeStringSLiteral() | self.regexUnicodeStringDLiteral()






	# Integer literal
	@Rule
	def decimalIntLiteral(self):
		return decimalInteger.action( lambda input, begin, end, xs, bindings: Nodes.IntLiteral( format='decimal', numType='int', value=xs ) )

	@Rule
	def decimalLongLiteral(self):
		return ( decimalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, xs, bindings: Nodes.IntLiteral( format='decimal', numType='long', value=xs[0] ) )

	@Rule
	def hexIntLiteral(self):
		return hexInteger.action( lambda input, begin, end, xs, bindings: Nodes.IntLiteral( format='hex', numType='int', value=xs ) )

	@Rule
	def hexLongLiteral(self):
		return ( hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, xs, bindings: Nodes.IntLiteral( format='hex', numType='long', value=xs[0] ) )

	@Rule
	def integerLiteral(self):
		return self.hexLongLiteral() | self.hexIntLiteral() | self.decimalLongLiteral() | self.decimalIntLiteral()





	# Float literal
	@Rule
	def floatLiteral(self):
		return floatingPoint.action( lambda input, begin, end, xs, bindings: Nodes.FloatLiteral( value=xs ) )




	# Imaginary literal
	@Rule
	def imaginaryLiteral(self):
		return Combine( [ ( floatingPoint | decimalInteger ), Literal( 'j' ) ] ).action( lambda input, begin, end, xs, bindings: Nodes.ImaginaryLiteral( value=xs ) )



	# Literal
	@Rule
	def literal(self):
		return self.shortStringLiteral() | self.imaginaryLiteral() | self.floatLiteral() | self.integerLiteral()


	
	
	
	#
	#
	# TARGETS (assignment, for-loop, etc)
	#
	#
	

	# Target (assignment, for-loop, ...)
	@Rule
	def singleTarget(self):
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Nodes.SingleTarget( name=xs ) )

	@Rule
	def tupleTarget(self):
		return SeparatedList( self.targetItem(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.TupleTarget( targets=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def targetListOrTargetItem(self):
		return self.tupleTarget()  |  self.targetItem()

	@Rule
	def parenTarget(self):
		return ( Literal( '(' )  +  self.targetListOrTargetItem()  +  Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: _incrementParens( xs[1] ) )

	@Rule
	def listTarget(self):
		return SeparatedList( self.targetItem(), '[', ']', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.ListTarget( targets=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def targetItem(self):
		return self.attributeRefOrSubscript()  |  self.parenTarget()  |  self.listTarget()  |  self.singleTarget()
		#return ( ( self.attributeRef()  ^  self.subscript() )  |  self.parenTarget()  |  self.listTarget()  |  self.singleTarget() )
		#return self.parenTarget()  |  self.listTarget()  |  self.singleTarget()




		
		
	#
	#
	# EXPRESSIONS
	#
	#
	

	# Load local variable
	@Rule
	def loadLocal(self):
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Nodes.Load( name=xs ) )



	# Tuples
	@Rule
	def tupleAsExpressionList(self):
		return SeparatedList( self.expression(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.TupleLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def tupleLiteral(self):
		return SeparatedList( self.expression(), '(', ')', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.TupleLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def oldTupleAsExpressionList(self):
		return SeparatedList( self.oldExpression(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.TupleLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )



	# Parentheses
	@Rule
	def parenForm(self):
		return ( Literal( '(' ) + self.expression() + ')' ).action( lambda input, begin, end, xs, bindings: _incrementParens( xs[1] ) )



	# List literal
	@Rule
	def listLiteral(self):
		return SeparatedList( self.expression(), '[', ']', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.ListLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )



	# List comprehension
	@Rule
	def listCompFor(self):
		return ( Keyword( forKeyword )  +  self.targetListOrTargetItem()  +  Keyword( inKeyword )  +  self.oldTupleOrExpression() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ComprehensionFor( target=xs[1], source=xs[3] ) )

	@Rule
	def listCompIf(self):
		return ( Keyword( ifKeyword )  +  self.oldExpression() ).action( lambda input, begin, end, xs, bindings: Nodes.ComprehensionIf( condition=xs[1] ) )

	@Rule
	def listCompItem(self):
		return self.listCompFor() | self.listCompIf()

	@Rule
	def listComprehension(self):
		return ( Literal( '[' )  +  self.expression()  +  self.listCompFor()  +  ZeroOrMore( self.listCompItem() )  +  Literal( ']' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.ListComp( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )




	# Generator expression
	@Rule
	def genExpFor(self):
		return ( Keyword( forKeyword )  +  self.targetListOrTargetItem()  +  Keyword( inKeyword )  +  self.orTest() ).action( lambda input, begin, end, xs, bindings: Nodes.ComprehensionFor( target=xs[1], source=xs[3] ) )

	@Rule
	def genExpIf(self):
		return ( Keyword( ifKeyword )  +  self.oldExpression() ).action( lambda input, begin, end, xs, bindings: Nodes.ComprehensionIf( condition=xs[1] ) )

	@Rule
	def genExpItem(self):
		return self.genExpFor() | self.genExpIf()

	@Rule
	def generatorExpression(self):
		return ( Literal( '(' )  +  self.expression()  +  self.genExpFor()  +  ZeroOrMore( self.genExpItem() )  +  Literal( ')' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.GeneratorExpr( resultExpr=xs[1], comprehensionItems=[ xs[2] ] + xs[3] ) )




	# Dictionary literal
	@Rule
	def keyValuePair(self):
		return ( self.expression()  +  Literal( ':' )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Nodes.DictKeyValuePair( key=xs[0], value=xs[2] ) )

	@Rule
	def dictLiteral(self):
		return SeparatedList( self.keyValuePair(), '{', '}', 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listAction(
			lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.DictLiteral( values=xs, trailingSeparator='1' if bTrailingSep else None ) )




	# Yield expression
	@Rule
	def yieldExpression(self):
		return ( Keyword( yieldKeyword )  +  self.tupleOrExpression() ).action( lambda input, begin, end, xs, bindings: Nodes.YieldExpr( value=xs[1] ) )


	@Rule
	def yieldAtom(self):
		return ( Literal( '(' )  +  self.yieldExpression()  +  Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: xs[1] )



	# Enclosure
	@Rule
	def enclosure(self):
		return self.parenForm() | self.tupleLiteral() | self.listLiteral() | self.listComprehension() | self.generatorExpression() | self.dictLiteral() | self.yieldAtom()




	# Atom
	@Rule
	def atom(self):
		return ObjectNode( Nodes.Expr )  |  self.enclosure() | self.literal() | self.loadLocal()




	# Attribute ref
	@Rule
	def attributeRef(self):
		return ( self.primary() + '.' + self.attrName() ).action( lambda input, begin, end, xs, bindings: Nodes.AttributeRef( target=xs[0], name=xs[2] ) )




	# Subscript and slice
	def _sliceItem(self, x):
		return x   if x is not None   else   None

	@Rule
	def subscriptSlice(self):
		return ( ( Optional( self.expression() ) + ':' + Optional( self.expression() )  ).action( lambda input, begin, end, xs, bindings: Nodes.SubscriptSlice( lower=self._sliceItem( xs[0] ), upper=self._sliceItem( xs[2] ) ) ) )

	@Rule
	def subscriptLongSlice(self):
		return ( ( Optional( self.expression() )  + ':' + Optional( self.expression() )  + ':' + Optional( self.expression() )  ).action( \
			lambda input, begin, end, xs, bindings: Nodes.SubscriptLongSlice( lower=self._sliceItem( xs[0] ), upper=self._sliceItem( xs[2] ), stride=self._sliceItem( xs[4] ) ) ) )

	@Rule
	def subscriptEllipsis(self):
		return Literal( '...' ).action( lambda input, begin, end, xs, bindings: Nodes.SubscriptEllipsis() )

	@Rule
	def subscriptItem(self):
		return self.subscriptLongSlice() | self.subscriptSlice() | self.subscriptEllipsis() | self.expression()

	@Rule
	def subscriptTuple(self):
		return SeparatedList( self.subscriptItem(), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( lambda input, begin, end, xs, bindings, bTrailingSep: len( xs ) != 1  or  bTrailingSep ). \
		       listAction( lambda input, begin, end, xs, bindings, bTrailingSep: Nodes.SubscriptTuple( values=xs, trailingSeparator='1' if bTrailingSep else None ) )

	@Rule
	def subscriptIndex(self):
		return self.subscriptTuple()  |  self.subscriptItem()

	@Rule
	def subscript(self):
		return ( self.primary() + '[' + self.subscriptIndex() + ']' ).action( lambda input, begin, end, xs, bindings: Nodes.Subscript( target=xs[0], index=xs[2] ) )




	# Call
	def _checkCallArgs(self, input, begin, end, xs, bindings, bTrailingSep):
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
		return ( self.argName() + '=' + self.expression() ).action( lambda input, begin, end, xs, bindings: Nodes.CallKWArg( name=xs[0], value=xs[2] ) )

	@Rule
	def argList(self):
		return ( Literal( '*' )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Nodes.CallArgList( value=xs[1] ) )

	@Rule
	def kwArgList(self):
		return ( Literal( '**' )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Nodes.CallKWArgList( value=xs[1] ) )

	@Rule
	def callArg(self):
		return self.kwArgList() | self.argList() | self.kwArg() | self.expression()

	@Rule
	def callArgs(self):
		# Result is ( list_of_args, bTrailingSep )
		return SeparatedList( self.callArg(), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( self._checkCallArgs ).listAction( lambda input, begin, end, xs, bindings, bTrailingSep: [ xs, '1'   if bTrailingSep   else None ] )

	@Rule
	def call(self):
		return ( self.primary() + Literal( '(' ) + self.callArgs() + Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: Nodes.Call( target=xs[0], args=xs[2][0], argsTrailingSeparator=xs[2][1] ) )



	# Primary
	@Rule
	def attributeRefOrSubscript(self):
		return self.attributeRef() | self.subscript()

	@Rule
	def primary(self):
		return self.call() | self.attributeRefOrSubscript() | self.atom()



	# Python operators
	@RuleList( [ 'powOp', 'invNegPosOp', 'mulDivModOp', 'addSubOp', 'lrShiftOp', 'andOP', 'xorOp', 'orOp', 'cmpOp', 'notTestOp', 'andTestOp', 'orTestOp' ] )
	def _operators(self):
		opTable = OperatorTable( 
			[
				InfixRightLevel( [ BinaryOperator( Literal( '**' ),  Nodes.Pow, 'x', 'y' ) ] ),
				PrefixLevel( [ UnaryOperator( Literal( '~' ),  Nodes.Invert, 'x' ),   UnaryOperator( Literal( '-' ),  Nodes.Negate, 'x' ),   UnaryOperator( Literal( '+' ),  Nodes.Pos, 'x' ) ], True ),
				InfixLeftLevel( [ BinaryOperator( Literal( '*' ),  Nodes.Mul, 'x', 'y' ),   BinaryOperator( Literal( '/' ),  Nodes.Div, 'x', 'y' ),   BinaryOperator( Literal( '%' ),  Nodes.Mod, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '+' ),  Nodes.Add, 'x', 'y' ),   BinaryOperator( Literal( '-' ),  Nodes.Sub, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '<<' ),  Nodes.LShift, 'x', 'y' ),   BinaryOperator( Literal( '>>' ),  Nodes.RShift, 'x', 'y') ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '&' ),  Nodes.BitAnd, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '^' ),  Nodes.BitXor, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Literal( '|' ),  Nodes.BitOr, 'x', 'y' ) ] ),
				InfixChainLevel( [
					ChainOperator( Literal( '<=' ),  Nodes.CmpOpLte, 'y' ),
					ChainOperator( Literal( '<' ),  Nodes.CmpOpLt, 'y' ),
					ChainOperator( Literal( '>=' ),  Nodes.CmpOpGte, 'y' ),
					ChainOperator( Literal( '>' ),  Nodes.CmpOpGt, 'y' ),
					ChainOperator( Literal( '==' ),  Nodes.CmpOpEq, 'y' ),
					ChainOperator( Literal( '!=' ),  Nodes.CmpOpNeq, 'y' ),
					ChainOperator( Keyword( isKeyword ) + Keyword( notKeyword ),  Nodes.CmpOpIsNot, 'y' ),
					ChainOperator( Keyword( isKeyword ),  Nodes.CmpOpIs, 'y' ),
					ChainOperator( Keyword( notKeyword ) + Keyword( inKeyword ),  Nodes.CmpOpNotIn, 'y' ),
					ChainOperator( Keyword( inKeyword ),  Nodes.CmpOpIn, 'y' ),
					],  Nodes.Cmp, 'x', 'ops' ),
				PrefixLevel( [ UnaryOperator( Keyword( notKeyword ),  Nodes.NotTest, 'x' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Keyword( andKeyword ),  Nodes.AndTest, 'x', 'y' ) ] ),
				InfixLeftLevel( [ BinaryOperator( Keyword( orKeyword ),  Nodes.OrTest, 'x', 'y' ) ] ),
				],  self.primary() )

		return opTable.buildParsers()



	@Rule
	def orOp(self):
		return self._operators()[7]


	@Rule
	def orTest(self):
		return self._operators()[-1]




	# Parameters (lambda, def statement, etc)
	def _checkParams(self, input, begin, end, xs, bindings, bTrailingSep):
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
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Nodes.SimpleParam( name=xs ) )

	@Rule
	def defaultValueParam(self):
		return ( self.paramName() + '=' + self.expression() ).action( lambda input, begin, end, xs, bindings: Nodes.DefaultValueParam( name=xs[0], defaultValue=xs[2] ) )

	@Rule
	def paramList(self):
		return ( Literal( '*' )  +  self.paramName() ).action( lambda input, begin, end, xs, bindings: Nodes.ParamList( name=xs[1] ) )

	@Rule
	def kwParamList(self):
		return ( Literal( '**' )  +  self.paramName() ).action( lambda input, begin, end, xs, bindings: Nodes.KWParamList( name=xs[1] ) )

	@Rule
	def param(self):
		return self.kwParamList() | self.paramList() | self.defaultValueParam() | self.simpleParam()

	@Rule
	def params(self):
		return SeparatedList( self.param(), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ). \
		       listCondition( self._checkParams ).listAction( lambda input, begin, end, xs, bindings, bTrailingSep: [ xs, '1'   if bTrailingSep   else None ] )




	# Lambda expression_checkParams
	@Rule
	def oldLambdaExpr(self):
		return ( Keyword( lambdaKeyword )  +  self.params()  +  Literal( ':' )  +  self.oldExpression() ).action(
			lambda input, begin, end, xs, bindings: Nodes.LambdaExpr( params=xs[1][0], expr=xs[3], paramsTrailingSeparator=xs[1][1] ) )

	@Rule
	def lambdaExpr(self):
		return ( Keyword( lambdaKeyword )  +  self.params()  +  Literal( ':' )  +  self.expression() ).action(
			lambda input, begin, end, xs, bindings: Nodes.LambdaExpr( params=xs[1][0], expr=xs[3], paramsTrailingSeparator=xs[1][1] ) )




	# Conditional expression
	@Rule
	def conditionalExpression(self):
		return ( self.orTest()  +  Keyword( ifKeyword )  +  self.orTest()  +  Keyword( elseKeyword )  +  self.expression() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ConditionalExpr( condition=xs[2], expr=xs[0], elseExpr=xs[4] ) )



	# Expression and old expression (old expression is expression without conditional expression)
	@Rule
	def oldExpression(self):
		return self.oldLambdaExpr()  |  self.orTest()

	@Rule
	def expression(self):
		return ObjectNode( Nodes.Expr )  |  self.lambdaExpr()  |  self.conditionalExpression()  |  self.orTest()



	# Tuple or (old) expression
	@Rule
	def tupleOrExpression(self):
		return self.tupleAsExpressionList()  |  self.expression()

	@Rule
	def oldTupleOrExpression(self):
		return self.oldTupleAsExpressionList()  |  self.oldExpression()
	
	
	@Rule
	def tupleOrExpressionOrYieldExpression(self):
		return self.tupleOrExpression()  |  self.yieldExpression()




	
	
	
	#
	#
	# UNPARSED
	#
	#
	
	@Rule
	def unparsed(self):
		return ObjectNode( Nodes.UNPARSED )  |  ( ( RegEx( '[^\n]*' ) | ObjectNode( Nodes.Node ) ).oneOrMore()  +  Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.UNPARSED( value=xs[0] ) )
	
	
	
	

	#
	#
	# SIMPLE STATEMENTS
	#
	#
	
	# Expression statement
	@Rule
	def exprStmt(self):
		return ( self.tupleOrExpression() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ExprStmt( expr=xs[0] ) )




	# Assert statement
	@Rule
	def assertStmt(self):
		return ( Keyword( assertKeyword ) + self.expression()  +  Optional( Literal( ',' ) + self.expression() ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.AssertStmt( condition=xs[1], fail=xs[2][1]   if xs[2] is not None  else  None ) )




	# Assignment statement
	@Rule
	def assignmentStmt(self):
		return ( OneOrMore( ( self.targetListOrTargetItem()  +  '=' ).action( lambda input, begin, end, xs, bindings: xs[0] ) )  +  self.tupleOrExpressionOrYieldExpression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.AssignStmt( targets=xs[0], value=xs[1] ) )




	# Augmented assignment statement
	@Rule
	def augOp(self):
		return Choice( [ Literal( op )   for op in augAssignOps ] )

	@Rule
	def augAssignStmt(self):
		return ( self.targetItem()  +  self.augOp()  +  self.tupleOrExpressionOrYieldExpression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.AugAssignStmt( op=xs[1], target=xs[0], value=xs[2] ) )




	# Pass statement
	@Rule
	def passStmt(self):
		return ( Keyword( passKeyword ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.PassStmt() )



	# Del statement
	@Rule
	def delStmt(self):
		return ( Keyword( delKeyword )  +  self.targetListOrTargetItem() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.DelStmt( target=xs[1] ) )



	# Return statement
	@Rule
	def returnStmt(self):
		return ( Keyword( returnKeyword )  +  self.tupleOrExpression() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ReturnStmt( value=xs[1] ) )



	# Yield statement
	@Rule
	def yieldStmt(self):
		return ( Keyword( yieldKeyword )  +  self.expression() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.YieldStmt( value=xs[1] ) )




	# Raise statement
	def _buildRaise(self, xs):
		excType = None
		excValue = None
		traceback = None
		if xs[1] is not None:
			excType = xs[1][0]
			if xs[1][1] is not None:
				excValue = xs[1][1][1]
				if xs[1][1][2] is not None:
					traceback = xs[1][1][2][1]
		return Nodes.RaiseStmt( excType=excType, excValue=excValue, traceback=traceback )

	@Rule
	def raiseStmt(self):
		return ( Keyword( raiseKeyword ) + Optional( self.expression() + Optional( Literal( ',' ) + self.expression() + Optional( Literal( ',' ) + self.expression() ) ) ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: self._buildRaise( xs ) )




	# Break statement
	@Rule
	def breakStmt(self):
		return ( Keyword( breakKeyword ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.BreakStmt() )




	# Continue statement
	@Rule
	def continueStmt(self):
		return ( Keyword( continueKeyword ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ContinueStmt() )




	# Import statement
	@Rule
	def _moduleIdentifier(self):
		return self.pythonIdentifier()

	# dotted name
	@Rule
	def moduleName(self):
		return SeparatedList( self._moduleIdentifier(), '.', 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ).action( lambda input, begin, end, xs, bindings: '.'.join( xs ) )


	# relative module name
	@Rule
	def _relModDotsModule(self):
		return ( ZeroOrMore( '.' ) + self.moduleName() ).action( lambda input, begin, end, xs, bindings: ''.join( xs[0] )  +  xs[1] )

	@Rule
	def _relModDots(self):
		return OneOrMore( '.' ).action( lambda input, begin, end, xs, bindings: ''.join( xs ) )

	@Rule
	def relativeModule(self):
		return ( self._relModDotsModule() | self._relModDots() ).action( lambda input, begin, end, xs, bindings: Nodes.RelativeModule( name=xs ) )


	# ( <moduleName> 'as' <pythonIdentifier> )  |  <moduleName>
	@Rule
	def moduleImport(self):
		return ( self.moduleName() + Keyword( asKeyword ) + self.pythonIdentifier() ).action( lambda input, begin, end, xs, bindings: Nodes.ModuleImportAs( name=xs[0], asName=xs[2] ) )   |	\
		       self.moduleName().action( lambda input, begin, end, xs, bindings: Nodes.ModuleImport( name=xs ) )


	# 'import' <separatedList( moduleImport )>
	@Rule
	def simpleImport(self):
		return ( Keyword( importKeyword )  +  SeparatedList( self.moduleImport(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.ImportStmt( modules=xs[1] ) )


	# ( <pythonIdentifier> 'as' <pythonIdentifier> )  |  <pythonIdentifier>
	@Rule
	def moduleContentImport(self):
		return ( self.pythonIdentifier() + Keyword( asKeyword ) + self.pythonIdentifier() ).action( lambda input, begin, end, xs, bindings: Nodes.ModuleContentImportAs( name=xs[0], asName=xs[2] ) )   |   \
		       self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Nodes.ModuleContentImport( name=xs ) )


	# 'from' <relativeModule> 'import' ( <separatedList( moduleContentImport )>  |  ( '(' <separatedList( moduleContentImport )> ',' ')' )
	@Rule
	def fromImport(self):
		return ( Keyword( fromKeyword ) + self.relativeModule() + Keyword( importKeyword ) + \
			 (  \
				 SeparatedList( self.moduleContentImport(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER )  |  
				 SeparatedList( self.moduleContentImport(), '(', ')', 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL )\
				 ) + Literal( '\n' )  \
			 ).action( lambda input, begin, end, xs, bindings: Nodes.FromImportStmt( module=xs[1], imports=xs[3] ) )


	# 'from' <relativeModule> 'import' '*'
	@Rule
	def fromImportAll(self):
		return ( Keyword( fromKeyword ) + self.relativeModule() + Keyword( importKeyword ) + '*' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.FromImportAllStmt( module=xs[1] ) )


	# Final :::
	@Rule
	def importStmt(self):
		return self.simpleImport() | self.fromImport() | self.fromImportAll()




	# Global statement
	@Rule
	def globalVar(self):
		return self.pythonIdentifier().action( lambda input, begin, end, xs, bindings: Nodes.GlobalVar( name=xs ) )

	@Rule
	def globalStmt(self):
		return ( Keyword( globalKeyword )  +  SeparatedList( self.globalVar(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.GlobalStmt( vars=xs[1] ) )





	# Exec statement
	@Rule
	def execCodeStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ExecStmt( source=xs[1], locals=None, globals=None ) )

	@Rule
	def execCodeInLocalsStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp()  +  Keyword( inKeyword )  +  self.expression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.ExecStmt( source=xs[1], locals=xs[3], globals=None ) )

	@Rule
	def execCodeInLocalsAndGlobalsStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp()  +  Keyword( inKeyword )  +  self.expression()  +  ','  +  self.expression() + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.ExecStmt( source=xs[1], locals=xs[3], globals=xs[5] ) )

	@Rule
	def execStmt(self):
		return self.execCodeInLocalsAndGlobalsStmt() | self.execCodeInLocalsStmt() | self.execCodeStmt()




	
	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#
		
	# If statement
	@Rule
	def ifStmtHeader(self):
		return ObjectNode( Nodes.IfStmtHeader )  |  \
		       ( Keyword( ifKeyword )  +  self.expression()  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.IfStmtHeader( condition=xs[1] ) )



	# Elif statement
	@Rule
	def elifStmtHeader(self):
		return ObjectNode( Nodes.ElifStmtHeader )  |  \
		       ( Keyword( elifKeyword )  +  self.expression()  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ElifStmtHeader( condition=xs[1] ) )



	# Else statement
	@Rule
	def elseStmtHeader(self):
		return ObjectNode( Nodes.ElseStmtHeader )  |  \
		       ( Keyword( elseKeyword )  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ElseStmtHeader() )



	# While statement
	@Rule
	def whileStmtHeader(self):
		return ObjectNode( Nodes.WhileStmtHeader )  |  \
		       ( Keyword( whileKeyword )  +  self.expression()  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.WhileStmtHeader( condition=xs[1] ) )



	# For statement
	@Rule
	def forStmtHeader(self):
		return ObjectNode( Nodes.ForStmtHeader )  |  \
		       ( Keyword( forKeyword )  +  self.targetListOrTargetItem()  +  Keyword( inKeyword )  +  self.tupleOrExpression()  +  ':' + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.ForStmtHeader( target=xs[1], source=xs[3] ) )



	# Try statement
	@Rule
	def tryStmtHeader(self):
		return ObjectNode( Nodes.TryStmtHeader )  |  \
		       ( Keyword( tryKeyword )  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.TryStmtHeader() )




	# Except statement
	@Rule
	def exceptAllStmtHeader(self):
		return ( Keyword( exceptKeyword ) + ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptStmtHeader( exception=None, target=None ) )

	@Rule
	def exceptExcStmtHeader(self):
		return ( Keyword( exceptKeyword )  +  self.expression() + ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptStmtHeader( exception=xs[1], target=None ) )

	@Rule
	def exceptExcIntoTargetStmtHeader(self):
		return ( Keyword( exceptKeyword )  +  self.expression()  +  ','  +  self.targetItem() + ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptStmtHeader( exception=xs[1], target=xs[3] ) )

	@Rule
	def exceptStmtHeader(self):
		return ObjectNode( Nodes.ExceptStmtHeader )  |  \
		       self.exceptExcIntoTargetStmtHeader() | self.exceptExcStmtHeader() | self.exceptAllStmtHeader()




	# Finally statement
	@Rule
	def finallyStmtHeader(self):
		return ObjectNode( Nodes.FinallyStmtHeader )  |  \
		       ( Keyword( finallyKeyword )  +  ':' + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.FinallyStmtHeader() )



	# With statement
	@Rule
	def withStmtHeader(self):
		return ObjectNode( Nodes.WithStmtHeader )  |  \
		       ( Keyword( withKeyword )  +  self.expression()  +  Optional( Keyword( asKeyword )  +  self.targetItem() )  +  ':' + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.WithStmtHeader( expr=xs[1], target=xs[2][1]   if xs[2] is not None   else   None ) )



	# Def statement
	@Rule
	def defStmtHeader(self):
		return ObjectNode( Nodes.DefStmtHeader )  |  \
		       ( Keyword( defKeyword )  +  self.pythonIdentifier()  +  '('  +  self.params()  +  ')'  +  ':' + Literal( '\n' ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.DefStmtHeader( name=xs[1], params=xs[3][0], paramsTrailingSeparator=xs[3][1] ) )



	# Decorator statement
	@Rule
	def decoStmtHeader(self):
		def _action(input, begin, end, xs, bindings):
			if xs[2] is not None:
				args = xs[2][1][0]
				trailingSeparator = xs[2][1][1]
			else:
				args = None
				trailingSeparator = None
			return Nodes.DecoStmtHeader( name=xs[1], args=args, argsTrailingSeparator=trailingSeparator )

		return ObjectNode( Nodes.DecoStmtHeader )  |  \
		       ( Literal( '@' )  +  self.dottedPythonIdentifer()  +  Optional( Literal( '(' )  +  self.callArgs()  +  ')' ) + Literal( '\n' ) ).action( _action )



	# Class statement
	@Rule
	def classStmtHeader(self):
		bases = SeparatedList( self.expression(), '(', ')', 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listAction( lambda input, begin, end, xs, bindings, bTrailingSep: [ xs, '1'   if bTrailingSep   else None ] )
		def _action(input, begin, end, xs, bindings):
			if xs[2] is not None:
				bases = xs[2][0]
				trailingSep = xs[2][1]
			else:
				bases = None
				trailingSep = None
			return Nodes.ClassStmtHeader( name=xs[1], bases=bases, basesTrailingSeparator=trailingSep )
		return ObjectNode( Nodes.ClassStmtHeader )  |  \
		       ( Keyword( classKeyword )  +  self.pythonIdentifier()  +  Optional( bases )  +  ':' + Literal( '\n' ) ).action( _action )



	
	#
	#
	# COMMENT STATEMENT
	#
	#
	
	# Comment statement
	@Rule
	def commentStmt(self):
		return ObjectNode( Nodes.CommentStmt )  |  \
		       ( Literal( '#' )  +  Word( string.printable.replace( '\n', '' ) ).optional() + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Nodes.CommentStmt( comment=xs[1]   if xs[1] is not None   else  '' ) )
	
	@Rule
	def blankLine(self):
		return ObjectNode( Nodes.BlankLine )  |  \
		       Literal( '\n' ).action( lambda input, begin, end, xs, bindings: Nodes.BlankLine() )
	
	
	
	
	#
	#
	# COMPOUND STATEMENTS
	#
	#
	
	@Rule
	def ifStmt(self):
		byLine = ( self.ifStmtHeader()  +  self.compoundSuite()  +  self.elifBlock().zeroOrMore()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.IfStmt( condition=xs[0]['condition'], suite=xs[1], elifBlocks=xs[2], elseSuite=xs[3] ) )
		join = ( ObjectNode( Nodes.IfStmt, elseSuite=None )  +  self.elifBlock().zeroOrMore()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.IfStmt( condition=xs[0]['condition'], suite=xs[0]['suite'], elifBlocks=list(xs[0]['elifBlocks']) + list(xs[1]), elseSuite=xs[2] ) )
		return byLine  |  join
		
	@Rule
	def elifBlock(self):
		return ( self.elifStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: Nodes.ElifBlock( condition=xs[0]['condition'], suite=xs[1] ) )
	
	@Rule
	def elseBlock(self):
		return ( self.elseStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def whileStmt(self):
		byLine = ( self.whileStmtHeader()  +  self.compoundSuite()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.WhileStmt( condition=xs[0]['condition'], suite=xs[1], elseSuite=xs[2] ) )
		join = ( ObjectNode( Nodes.WhileStmt, elseSuite=None )  +  self.elseBlock() ).action(
			lambda input, begin, end, xs, bindings: Nodes.WhileStmt( condition=xs[0]['condition'], suite=xs[0]['suite'], elseSuite=xs[1] ) )
		return byLine  |  join
		

	@Rule
	def forStmt(self):
		byLine = ( self.forStmtHeader()  +  self.compoundSuite()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ForStmt( target=xs[0]['target'], source=xs[0]['source'], suite=xs[1], elseSuite=xs[2] ) )
		join = ( ObjectNode( Nodes.ForStmt, elseSuite=None )  +  self.elseBlock() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ForStmt( target=xs[0]['target'], source=xs[0]['source'], suite=xs[0]['suite'], elseSuite=xs[1] ) )
		return byLine  |  join
		
		
	@Rule
	def tryStmt(self):
		tryStmt1ByLine = ( self.tryStmtHeader()  +  self.compoundSuite()  +  self.exceptBlock().oneOrMore()  +  self.elseBlock().optional()  +  self.finallyBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=xs[1], exceptBlocks=xs[2], elseSuite=xs[3], finallySuite=xs[4] ) )
		# No else or finally clause; add 1+ except blocks, and optionally, else and finally clauses
		tryStmt1JoinA = ( ObjectNode( Nodes.TryStmt, elseSuite=None, finallySuite=None )  +  self.exceptBlock().oneOrMore()  +  self.elseBlock().optional()  +  self.finallyBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=xs[0]['suite'], exceptBlocks=list(xs[0]['exceptBlocks']) + list(xs[1]), elseSuite=xs[2], finallySuite=xs[3] ) )
		# 1 or more except blocks, no else or finally clause; add an else clause, and optionally, a finally clause
		tryStmt1JoinB = ( ObjectNode( Nodes.TryStmt, elseSuite=None, finallySuite=None ).condition( lambda input, begin, end, xs, bindings: len( xs['exceptBlocks'] ) > 0 )  +  \
				  self.elseBlock()  +  self.finallyBlock().optional() ).action(
					  lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=xs[0]['suite'], exceptBlocks=xs[0]['exceptBlocks'], elseSuite=xs[1], finallySuite=xs[2] ) )
		# 1 or more except blocks, no finally clause; add a finally clause
		tryStmt1JoinC = ( ObjectNode( Nodes.TryStmt, finallySuite=None ).condition( lambda input, begin, end, xs, bindings: len( xs['exceptBlocks'] ) > 0 )  +  self.finallyBlock() ).action(
					  lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=xs[0]['suite'], exceptBlocks=xs[0]['exceptBlocks'], finallySuite=xs[1] ) )
		tryStmt2ByLine = ( self.tryStmtHeader()  +  self.compoundSuite()  +  self.finallyBlock() ).action(
			lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=xs[1], exceptBlocks=[], elseSuite=None, finallySuite=xs[2] ) )
		return tryStmt1ByLine | tryStmt1JoinA  |  tryStmt1JoinB  |  tryStmt1JoinC  |  tryStmt2ByLine
	
	@Rule
	def exceptBlock(self):
		return ( self.exceptStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptBlock( exception=xs[0]['exception'], target=xs[0]['target'], suite=xs[1] ) )
	
	@Rule
	def finallyBlock(self):
		return ( self.finallyStmtHeader()  +  self.compoundSuite() ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def withStmt(self):
		return ( self.withStmtHeader()  +  self.compoundSuite() ).action(
			lambda input, begin, end, xs, bindings: Nodes.WithStmt( expr=xs[0]['expr'], target=xs[0]['target'], suite=xs[1] ) )
		

	@Rule
	def decorator(self):
		return self.decoStmtHeader().action( lambda input, begin, end, xs, bindings: Nodes.Decorator( name=xs['name'], args=xs['args'], argsTrailingSeparator=xs['argsTrailingSeparator'] ) )
	
	@Rule
	def defStmt(self):
		byLine = ( self.decorator().zeroOrMore()  +  self.defStmtHeader()  +  self.compoundSuite() ).action(
			lambda input, begin, end, xs, bindings: Nodes.DefStmt( decorators=xs[0], name=xs[1]['name'], params=xs[1]['params'], paramsTrailingSeparator=xs[1]['paramsTrailingSeparator'] , suite=xs[2] ) )
		join = ( self.decorator().oneOrMore()  +  ObjectNode( Nodes.DefStmt ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.DefStmt( decorators=list(xs[0]) + list(xs[1]['decorators']), name=xs[1]['name'], params=xs[1]['params'], paramsTrailingSeparator=xs[1]['paramsTrailingSeparator'],
									       suite=xs[1]['suite'] ) )
		return byLine  |  join
	
	
	@Rule
	def classStmt(self):
		return ( self.classStmtHeader()  +  self.compoundSuite() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ClassStmt( name=xs[0]['name'], bases=xs[0]['bases'], basesTrailingSeparator=xs[0]['basesTrailingSeparator'] , suite=xs[1] ) )

	
	
	
	
	#
	#
	# STATEMENTS
	#
	#
	
	@Rule
	def simpleStmt(self):
		return ObjectNode( Nodes.SimpleStmt )  |  \
		       self.assertStmt() | self.assignmentStmt() | self.augAssignStmt() | self.passStmt() | self.delStmt() | self.returnStmt() | self.yieldStmt() | self.raiseStmt() | self.breakStmt() | \
		       self.continueStmt() | self.importStmt() | self.globalStmt() | self.execStmt() | self.exprStmt()

	@Rule
	def compoundStmtHeader(self):
		return ObjectNode( Nodes.CompountStmtHeader )  |  \
		       self.ifStmtHeader() | self.elifStmtHeader() | self.elseStmtHeader() | self.whileStmtHeader() | self.forStmtHeader() | self.tryStmtHeader() | self.exceptStmtHeader() | self.finallyStmtHeader() | \
		       self.withStmtHeader() | self.defStmtHeader() | self.decoStmtHeader() | self.classStmtHeader()

	@Rule
	def compoundStmt(self):
		return self.ifStmt()  |  self.whileStmt()  |  self.forStmt()  |  self.tryStmt()  |  self.withStmt()  |  self.defStmt()  |  self.classStmt()  |  ObjectNode( Nodes.CompoundStmt )
		       
	
	@Rule
	def singleLineStatementValid(self):
		return self.simpleStmt() | self.compoundStmtHeader() | self.commentStmt() | self.blankLine()

	@Rule
	def singleLineStatement(self):
		return self.singleLineStatementValid() | self.unparsed()


	
	
	#
	#
	# BLOCKS / STRUCTURE
	#
	#
	
	@Rule
	def indentedBlock(self):
		return self.compoundSuite().action( lambda input, begin, end, xs, bindings: Nodes.IndentedBlock( suite=xs ) )
	
	
	@Rule
	def suiteItem(self):
		return self.compoundStmt()  |  self.indentedBlock()  |  self.simpleStmt()  |  self.compoundStmtHeader()  |  self.emptyIndentedSuite()  |  self.commentStmt()  |  self.blankLine()  |  self.unparsed()
	
	
	@Rule
	def singleIndentedSuite(self):
		return ( ObjectNode( Nodes.Indent )  +  self.suiteItem().oneOrMore()  +  ObjectNode( Nodes.Dedent ) ).action( lambda input, begin, end, xs, bindings: xs[1] )  |  \
		       ObjectNode( Nodes.IndentedBlock ).action( lambda input, begin, end, xs, bindings: xs['suite'] )
	
	@Rule
	def compoundSuite(self):
		return self.singleIndentedSuite().oneOrMore().action( lambda input, begin, end, xs, bindings: reduce( lambda a, b: list(a)+list(b), xs ) )

	
	@Rule
	def emptyIndentedSuite(self):
		return ( ObjectNode( Nodes.Indent )  +  ObjectNode( Nodes.Dedent ) ).suppress()
	
	@Rule
	def suite(self):
		return self.suiteItem().zeroOrMore()









import unittest


class TestCase_Python25Parser (ParserTestCase):
	def _pythonStream(self, *args):
		b = ItemStreamBuilder()
		for x in args:
			if isinstance( x, str ):
				b.appendTextValue( x )
			else:
				b.appendStructuralValue( x )
		return b.stream()
	
	
	def test_shortStringLiteral(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '\'abc\'', Nodes.StringLiteral( format='ascii', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), '\"abc\"', Nodes.StringLiteral( format='ascii', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'u\'abc\'', Nodes.StringLiteral( format='unicode', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'u\"abc\"', Nodes.StringLiteral( format='unicode', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'r\'abc\'', Nodes.StringLiteral( format='ascii-regex', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'r\"abc\"', Nodes.StringLiteral( format='ascii-regex', quotation='double', value='abc' ) )
		self._parseStringTest( g.expression(), 'ur\'abc\'', Nodes.StringLiteral( format='unicode-regex', quotation='single', value='abc' ) )
		self._parseStringTest( g.expression(), 'ur\"abc\"', Nodes.StringLiteral( format='unicode-regex', quotation='double', value='abc' ) )


	def test_integerLiteral(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '123', Nodes.IntLiteral( format='decimal', numType='int', value='123' ) )
		self._parseStringTest( g.expression(), '123L', Nodes.IntLiteral( format='decimal', numType='long', value='123' ) )
		self._parseStringTest( g.expression(), '0x123', Nodes.IntLiteral( format='hex', numType='int', value='0x123' ) )
		self._parseStringTest( g.expression(), '0x123L', Nodes.IntLiteral( format='hex', numType='long', value='0x123' ) )


	def test_floatLiteral(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '123.0', Nodes.FloatLiteral( value='123.0' ) )


	def test_imaginaryLiteral(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '123.0j', Nodes.ImaginaryLiteral( value='123.0j' ) )


	def testTargets(self):
		g = Python25Grammar()
		self._parseStringTest( g.targetListOrTargetItem(), 'a', Nodes.SingleTarget( name='a' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a)', Nodes.SingleTarget( name='a', parens='1' ) )

		self._parseStringTest( g.targetListOrTargetItem(), '(a,)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ) ], trailingSeparator='1', parens='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a,b', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a,b)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ], parens='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a,b,)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ], trailingSeparator='1', parens='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '(a,b),(c,d)', Nodes.TupleTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ], parens='1' ),
													 Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='c' ), Nodes.SingleTarget( name='d' ) ], parens='1' ) ] ) )

		self._parseStringFailTest( g.targetListOrTargetItem(), '(a,) (b,)' )

		self._parseStringTest( g.targetListOrTargetItem(), '[a]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a,]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ], trailingSeparator='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a,b]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a,b,]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ], trailingSeparator='1' ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[a],[b,]', Nodes.TupleTarget( targets=[ Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ),
												      Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='b' ) ], trailingSeparator='1' ) ] ) )
		self._parseStringTest( g.targetListOrTargetItem(), '[(a,)],[(b,)]', Nodes.TupleTarget( targets=[ Nodes.ListTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ) ], trailingSeparator='1', parens='1' ) ] ),
													   Nodes.ListTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='b' ) ], trailingSeparator='1', parens='1' ) ] ) ] ) )

		self._parseStringTest( g.subscript(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.attributeRefOrSubscript(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.targetItem(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a[x][y]', Nodes.Subscript( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ), index=Nodes.Load( name='y' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a.b', Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a.b.c', Nodes.AttributeRef( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), name='c' ) )

		self._parseStringTest( g.targetListOrTargetItem(), 'a.b[x]', Nodes.Subscript( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), index=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.targetListOrTargetItem(), 'a[x].b', Nodes.AttributeRef( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ), name='b' ) )


	def testTupleLiteral(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '()', Nodes.TupleLiteral( values=[] ) )
		self._parseStringTest( g.expression(), '(())', Nodes.TupleLiteral( values=[], parens='1' ) )
		self._parseStringTest( g.expression(), '(a)', Nodes.Load( name='a', parens='1' ) )
		self._parseStringTest( g.expression(), '(a,)', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ) ], trailingSeparator='1' ) )
		self._parseStringTest( g.expression(), '((a,))', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ) ], trailingSeparator='1', parens='1' ) )
		self._parseStringTest( g.expression(), '(a,b)', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		self._parseStringTest( g.expression(), '(a,b,)', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ], trailingSeparator='1' ) )


	def testListLiteral(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '[]', Nodes.ListLiteral( values=[] ) )
		self._parseStringTest( g.expression(), '[a,b]', Nodes.ListLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		self._parseStringTest( g.expression(), '[a,b,]', Nodes.ListLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ], trailingSeparator='1' ) )


	def testListComprehension(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '[i  for i in a]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
										    comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ) ]
										    ) )
		self._parseStringFailTest( g.expression(), '[i  if x]', )
		self._parseStringTest( g.expression(), '[i  for i in a  if x]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
											  comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
													       Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ) ]
											  ) )
		self._parseStringTest( g.expression(), '[i  for i in a  for j in b]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
												comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
														     Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
												) )
		self._parseStringTest( g.expression(), '[i  for i in a  if x  for j in b]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
												      comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
															   Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
															   Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
												      ) )
		self._parseStringTest( g.expression(), '[i  for i in a  if x  for j in b  if y]', Nodes.ListComp( resultExpr=Nodes.Load( name='i' ),
													    comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
																 Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
																 Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ),
																 Nodes.ComprehensionIf( condition=Nodes.Load( name='y' ) ) ]
													    ) )



	def testGeneratorExpression(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '(i  for i in a)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
											 comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ) ]
											 ) )
		self._parseStringFailTest( g.expression(), '(i  if x)', )
		self._parseStringTest( g.expression(), '(i  for i in a  if x)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
											       comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
														    Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ) ]
											       ) )
		self._parseStringTest( g.expression(), '(i  for i in a  for j in b)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
												     comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
															  Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
												     ) )
		self._parseStringTest( g.expression(), '(i  for i in a  if x  for j in b)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
													   comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
																Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
																Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ) ]
													   ) )
		self._parseStringTest( g.expression(), '(i  for i in a  if x  for j in b  if y)', Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='i' ),
														 comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='i' ), source=Nodes.Load( name='a' ) ),
																      Nodes.ComprehensionIf( condition=Nodes.Load( name='x' ) ),
																      Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='j' ), source=Nodes.Load( name='b' ) ),
																      Nodes.ComprehensionIf( condition=Nodes.Load( name='y' ) ) ]
														 ) )


	def testDictLiteral(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '{a:x,b:y}', Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=Nodes.Load( name='x' ) ),
											  Nodes.DictKeyValuePair( key=Nodes.Load( name='b' ), value=Nodes.Load( name='y' ) ) ] ) )
		self._parseStringTest( g.expression(), '{a:x,b:y,}', Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=Nodes.Load( name='x' ) ),
											   Nodes.DictKeyValuePair( key=Nodes.Load( name='b' ), value=Nodes.Load( name='y' ) ) ], trailingSeparator='1' ) )


	def testYieldExpr(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '(yield 2+3)', Nodes.YieldExpr( value=Nodes.Add( x=Nodes.IntLiteral( format='decimal', numType='int', value='2' ), y=Nodes.IntLiteral( format='decimal', numType='int', value='3' ) ) ) )



	def testAttributeRef(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'a.b', Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ) )


	def testSubscript(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.expression(), 'a[x:p]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ) ) ) )
		self._parseStringTest( g.expression(), 'a[x:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=None ) ) )
		self._parseStringTest( g.expression(), 'a[:p]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=None, upper=Nodes.Load( name='p' ) ) ) )
		self._parseStringTest( g.expression(), 'a[:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=None, upper=None ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[x::f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=None, stride=Nodes.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[:p:f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[::]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=None, stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[::f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=None, stride=Nodes.Load( name='f' ) ) ) )
		self._parseStringTest( g.expression(), 'a[x::]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=None, stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[:p:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=Nodes.Load( name='p' ), stride=None ) ) )
		self._parseStringTest( g.expression(), 'a[x,y]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptTuple( values=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ) ) )
		self._parseStringTest( g.expression(), 'a[x:p,y:q]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptTuple( values=[ Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ) ), Nodes.SubscriptSlice( lower=Nodes.Load( name='y' ), upper=Nodes.Load( name='q' ) ) ] ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:f,y:q:g]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptTuple( values=[ Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ), Nodes.SubscriptLongSlice( lower=Nodes.Load( name='y' ), upper=Nodes.Load( name='q' ), stride=Nodes.Load( name='g' ) ) ] ) ) )
		self._parseStringTest( g.expression(), 'a[x:p:f,y:q:g,...]', Nodes.Subscript( target=Nodes.Load( name='a' ),
											index=Nodes.SubscriptTuple( values=[ Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ),
															     Nodes.SubscriptLongSlice( lower=Nodes.Load( name='y' ), upper=Nodes.Load( name='q' ), stride=Nodes.Load( name='g' ) ),
															     Nodes.SubscriptEllipsis() ] ) ) )



	def testCall(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'a()', Nodes.Call( target=Nodes.Load( name='a' ), args=[] ) )
		self._parseStringTest( g.expression(), 'a(f)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ) ], argsTrailingSeparator='1' ) )
		self._parseStringTest( g.expression(), 'a(f,g)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,g,m=a)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,g,m=a,n=b)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallKWArg( name='n', value=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,g,m=a,n=b,*p)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.Load( name='g' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallKWArg( name='n', value=Nodes.Load( name='b' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,m=a,*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,m=a,*p)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,m=a,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(f,*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(m=a,*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallKWArg( name='m', value=Nodes.Load( name='a' ) ), Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(*p,**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallArgList( value=Nodes.Load( name='p' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(**w)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallKWArgList( value=Nodes.Load( name='w' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a(**w+x)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallKWArgList( value=Nodes.Add( x=Nodes.Load( name='w' ), y=Nodes.Load( name='x' ) ) ) ] ) )
		self._parseStringFailTest( g.expression(), 'a(m=a,f)' )
		self._parseStringFailTest( g.expression(), 'a(*p,f)' )
		self._parseStringFailTest( g.expression(), 'a(**w,f)' )
		self._parseStringFailTest( g.expression(), 'a(*p,m=a)' )
		self._parseStringFailTest( g.expression(), 'a(**w,m=a)' )
		self._parseStringFailTest( g.expression(), 'a(**w,*p)' )



	def testOperators(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'a**b', Nodes.Pow( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), '~a', Nodes.Invert( x=Nodes.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), '-a', Nodes.Negate( x=Nodes.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), '+a', Nodes.Pos( x=Nodes.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), 'a*b', Nodes.Mul( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a/b', Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a%b', Nodes.Mod( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a+b', Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a-b', Nodes.Sub( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a<<b', Nodes.LShift( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a>>b', Nodes.RShift( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a&b', Nodes.BitAnd( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a^b', Nodes.BitXor( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a|b', Nodes.BitOr( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a<=b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpLte( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a<b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpLt( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a>=b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpGte( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a>b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpGt( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a==b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpEq( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a!=b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpNeq( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a is not b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIsNot( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a is b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIs( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a not in b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpNotIn( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'a in b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIn( y=Nodes.Load( name='b' ) ) ] ) )
		self._parseStringTest( g.expression(), 'not a', Nodes.NotTest( x=Nodes.Load( name='a' ) ) )
		self._parseStringTest( g.expression(), 'a and b', Nodes.AndTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), 'a or b', Nodes.OrTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )


	def testOperatorPrecedence(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'a + b < c', Nodes.Cmp( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), ops=[ Nodes.CmpOpLt( y=Nodes.Load( name='c' ) ) ] ) )


	def testParens(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), '(a)', Nodes.Load( name='a', parens='1' ) )
		self._parseStringTest( g.expression(), '(((a)))', Nodes.Load( name='a', parens='3' ) )
		self._parseStringTest( g.expression(), '(a+b)', Nodes.Add( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.expression(), '(a+b)*c', Nodes.Mul( x=Nodes.Add( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Load( name='c' ) ) )


	def testParams(self):
		g = Python25Grammar()
		self._parseStringTest( g.params(), '', [ [], None ] )
		self._parseStringTest( g.params(), 'f', [ [ Nodes.SimpleParam( name='f' ) ], None ] )
		self._parseStringTest( g.params(), 'f,', [ [ Nodes.SimpleParam( name='f' ) ], '1' ] )
		self._parseStringTest( g.params(), 'f,g', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ) ], None ] )
		self._parseStringTest( g.params(), 'f,g,m=a', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ) ], None ] )
		self._parseStringTest( g.params(), 'f,g,m=a,n=b', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ) ], None ] )
		self._parseStringTest( g.params(), 'f,g,m=a,n=b,*p', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ), Nodes.ParamList( name='p' ) ], None ] )
		self._parseStringTest( g.params(), 'f,m=a,*p,**w', [ [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), 'f,m=a,*p', [ [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ) ], None ] )
		self._parseStringTest( g.params(), 'f,m=a,**w', [ [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), 'f,*p,**w', [ [ Nodes.SimpleParam( name='f' ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), 'm=a,*p,**w', [ [ Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), '*p,**w', [ [ Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._parseStringTest( g.params(), '**w', [ [ Nodes.KWParamList( name='w' ) ], None ] )
		self._parseStringFailTest( g.params(), 'm=a,f' )
		self._parseStringFailTest( g.params(), '*p,f' )
		self._parseStringFailTest( g.params(), '**w,f' )
		self._parseStringFailTest( g.params(), '*p,m=a' )
		self._parseStringFailTest( g.params(), '**w,m=a' )
		self._parseStringFailTest( g.params(), '**w,*p' )



	def testLambda(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'lambda f,m=a,*p,**w: f+m+p+w', Nodes.LambdaExpr( 
			params=[ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ],
			expr=Nodes.Add( x=Nodes.Add( x=Nodes.Add( x=Nodes.Load( name='f' ), y=Nodes.Load( name='m' ) ), y=Nodes.Load( name='p' ) ), y=Nodes.Load( name='w' ) ) ) )



	def testConditionalExpr(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'x   if y else   z', Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ) )
		self._parseStringTest( g.expression(), '(x   if y else   z)   if w else   q', Nodes.ConditionalExpr( condition=Nodes.Load( name='w' ), expr=Nodes.ConditionalExpr( parens='1', condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ), elseExpr=Nodes.Load( name='q' ) ) )
		self._parseStringTest( g.expression(), 'w   if (x   if y else   z) else   q', Nodes.ConditionalExpr( condition=Nodes.ConditionalExpr( parens='1', condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ), expr=Nodes.Load( name='w' ), elseExpr=Nodes.Load( name='q' ) ) )
		self._parseStringTest( g.expression(), 'w   if q else   x   if y else   z', Nodes.ConditionalExpr( condition=Nodes.Load( name='q' ), expr=Nodes.Load( name='w' ), elseExpr=Nodes.ConditionalExpr( condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ) ) )
		self._parseStringFailTest( g.expression(), 'w   if x   if y else   z else   q' )



	def testTupleOrExpression(self):
		g = Python25Grammar()
		self._parseStringTest( g.tupleOrExpression(), 'a', Nodes.Load( name='a' ) )
		self._parseStringTest( g.tupleOrExpression(), 'a,b', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		self._parseStringTest( g.tupleOrExpression(), 'a,2', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.IntLiteral( format='decimal', numType='int', value='2' ) ] ) )
		self._parseStringTest( g.tupleOrExpression(), 'lambda x, y: x+y,2', Nodes.TupleLiteral(
			values=[ Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='x' ), Nodes.SimpleParam( name='y' ) ],
						   expr=Nodes.Add( x=Nodes.Load( name='x' ), y=Nodes.Load( name='y' ) ) ),
				 Nodes.IntLiteral( format='decimal', numType='int', value='2' ) ] ) )



	def test_structuralAtom(self):
		g = Python25Grammar()
		s = self._pythonStream( Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStreamTest( g.atom(), s, Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )

		
		
	def test_embeddedStructuralExpression(self):
		g = Python25Grammar()
		s = self._pythonStream( Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStreamTest( g.tupleOrExpression(), s, Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		s = self._pythonStream( 'return ', Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), '\n' )
		self._parseStreamTest( g.singleLineStatement(), s, Nodes.ReturnStmt( value=Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ) )
		s = self._pythonStream( 'x + ', Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._parseStreamTest( g.tupleOrExpression(), s, Nodes.Add( x=Nodes.Load( name='x' ), y=Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ) )

		
		
	def testAssertStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'assert x\n', Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'assert x,y\n', Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=Nodes.Load( name='y' ) ) )
		self._parseNodeTest( g.singleLineStatementValid(), Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=None ), Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=None ) )


	def testAssignmentStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'a=x\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a=b=x\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ], value=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a,b=c,d=x\n', Nodes.AssignStmt( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ),
											 Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='c' ),  Nodes.SingleTarget( name='d' ) ] ) ], value=Nodes.Load( name='x' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a=(yield x)\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.YieldExpr( value=Nodes.Load( name='x' ) ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a = yield x\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.YieldExpr( value=Nodes.Load( name='x' ) ) ) )
		self._parseStringFailTest( g.singleLineStatementValid(), '=x' )


	def testAugAssignStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'a += b\n', Nodes.AugAssignStmt( op='+=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a -= b\n', Nodes.AugAssignStmt( op='-=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a *= b\n', Nodes.AugAssignStmt( op='*=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a /= b\n', Nodes.AugAssignStmt( op='/=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a %= b\n', Nodes.AugAssignStmt( op='%=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a **= b\n', Nodes.AugAssignStmt( op='**=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a >>= b\n', Nodes.AugAssignStmt( op='>>=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a <<= b\n', Nodes.AugAssignStmt( op='<<=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a &= b\n', Nodes.AugAssignStmt( op='&=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a ^= b\n', Nodes.AugAssignStmt( op='^=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a |= b\n', Nodes.AugAssignStmt( op='|=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._parseStringTest( g.singleLineStatementValid(), 'a += yield b\n', Nodes.AugAssignStmt( op='+=', target=Nodes.SingleTarget( name='a' ), value=Nodes.YieldExpr( value=Nodes.Load( name='b' ) ) ) )


	def testPassStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'pass\n', Nodes.PassStmt() )


	def testDelStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'del x\n', Nodes.DelStmt( target=Nodes.SingleTarget( name='x' ) ) )


	def testReturnStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'return x\n', Nodes.ReturnStmt( value=Nodes.Load( name='x' ) ) )


	def testYieldStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'yield x\n', Nodes.YieldStmt( value=Nodes.Load( name='x' ) ) )


	def testRaiseStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'raise\n', Nodes.RaiseStmt( excType=None, excValue=None, traceback=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'raise x\n', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=None, traceback=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'raise x,y\n', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=Nodes.Load( name='y' ), traceback=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'raise x,y,z\n', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=Nodes.Load( name='y' ), traceback=Nodes.Load( name='z' ) ) )


	def testBreakStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'break\n', Nodes.BreakStmt() )


	def testContinueStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'continue\n', Nodes.ContinueStmt() )


	def testImportStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g._moduleIdentifier(), 'abc', 'abc' )
		self._parseStringTest( g.moduleName(), 'abc', 'abc' )
		self._parseStringTest( g.moduleName(), 'abc.xyz', 'abc.xyz' )
		self._parseStringTest( g._relModDotsModule(), 'abc.xyz', 'abc.xyz' )
		self._parseStringTest( g._relModDotsModule(), '...abc.xyz', '...abc.xyz' )
		self._parseStringTest( g._relModDots(), '...', '...' )
		self._parseStringTest( g.relativeModule(), 'abc.xyz', Nodes.RelativeModule( name='abc.xyz' ) )
		self._parseStringTest( g.relativeModule(), '...abc.xyz', Nodes.RelativeModule( name='...abc.xyz' ) )
		self._parseStringTest( g.relativeModule(), '...', Nodes.RelativeModule( name='...' ) )
		self._parseStringTest( g.moduleImport(), 'abc.xyz', Nodes.ModuleImport( name='abc.xyz' ) )
		self._parseStringTest( g.moduleImport(), 'abc.xyz as q', Nodes.ModuleImportAs( name='abc.xyz', asName='q' ) )
		self._parseStringTest( g.simpleImport(), 'import a\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a' ) ] ) )
		self._parseStringTest( g.simpleImport(), 'import a.b\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a.b' ) ] ) )
		self._parseStringTest( g.simpleImport(), 'import a.b as x\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._parseStringTest( g.simpleImport(), 'import a.b as x, c.d as y\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ), Nodes.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._parseStringTest( g.moduleContentImport(), 'xyz', Nodes.ModuleContentImport( name='xyz' ) )
		self._parseStringTest( g.moduleContentImport(), 'xyz as q', Nodes.ModuleContentImportAs( name='xyz', asName='q' ) )
		self._parseStringTest( g.fromImport(), 'from x import a\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import a as p\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import a as p, b as q\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a,)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a as p)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import (a as p,)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import ( a as p, b as q )\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.fromImport(), 'from x import ( a as p, b as q, )\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.fromImportAll(), 'from x import *\n', Nodes.FromImportAllStmt( module=Nodes.RelativeModule( name='x' ) ) )
		self._parseStringTest( g.importStmt(), 'import a\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'import a.b\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a.b' ) ] ) )
		self._parseStringTest( g.importStmt(), 'import a.b as x\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._parseStringTest( g.importStmt(), 'import a.b as x, c.d as y\n', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ), Nodes.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import a\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import a as p\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import a as p, b as q\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a,)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a as p)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import (a as p,)\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import ( a as p, b as q )\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import ( a as p, b as q, )\n', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._parseStringTest( g.importStmt(), 'from x import *\n', Nodes.FromImportAllStmt( module=Nodes.RelativeModule( name='x' ) ) )


	def testGlobalStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'global x\n', Nodes.GlobalStmt( vars=[ Nodes.GlobalVar( name='x' ) ] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'global x, y\n', Nodes.GlobalStmt( vars=[ Nodes.GlobalVar( name='x' ), Nodes.GlobalVar( name='y' ) ] ) )


	def testExecStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'exec a\n', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=None, globals=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'exec a in b\n', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=Nodes.Load( name='b' ), globals=None ) )
		self._parseStringTest( g.singleLineStatementValid(), 'exec a in b,c\n', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=Nodes.Load( name='b' ), globals=Nodes.Load( name='c' ) ) )


		
		
	#
	# Compound statement headers
	#
		
	def testIfStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.ifStmtHeader(), 'if a:\n', Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ) )
		self._parseNodeTest( g.ifStmtHeader(), Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ), Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ), Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ) )


	def testElIfStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.elifStmtHeader(), 'elif a:\n', Nodes.ElifStmtHeader( condition=Nodes.Load( name='a' ) ) )
		self._parseNodeTest( g.elifStmtHeader(), Nodes.ElifStmtHeader( condition=Nodes.Load( name='a' ) ), Nodes.ElifStmtHeader( condition=Nodes.Load( name='a' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.ElifStmtHeader( condition=Nodes.Load( name='a' ) ), Nodes.ElifStmtHeader( condition=Nodes.Load( name='a' ) ) )


	def testElseStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.elseStmtHeader(), 'else:\n', Nodes.ElseStmtHeader() )
		self._parseNodeTest( g.elseStmtHeader(), Nodes.ElseStmtHeader(), Nodes.ElseStmtHeader() )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.ElseStmtHeader(), Nodes.ElseStmtHeader() )


	def testWhileStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.whileStmtHeader(), 'while a:\n', Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ) )
		self._parseNodeTest( g.whileStmtHeader(), Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ), Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ), Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ) )


	def testForStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.forStmtHeader(), 'for x in y:\n', Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='x' ), source=Nodes.Load( name='y' ) ) )
		self._parseNodeTest( g.forStmtHeader(), Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='x' ), source=Nodes.Load( name='y' ) ),
				     Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='x' ), source=Nodes.Load( name='y' ) ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='x' ), source=Nodes.Load( name='y' ) ),
				     Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='x' ), source=Nodes.Load( name='y' ) ) )


	def testTryStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.tryStmtHeader(), 'try:\n', Nodes.TryStmtHeader() )
		self._parseNodeTest( g.tryStmtHeader(), Nodes.TryStmtHeader(), Nodes.TryStmtHeader() )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.TryStmtHeader(), Nodes.TryStmtHeader() )


	def testExceptStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.exceptStmtHeader(), 'except:\n', Nodes.ExceptStmtHeader( exception=None, target=None ) )
		self._parseStringTest( g.exceptStmtHeader(), 'except x:\n', Nodes.ExceptStmtHeader( exception=Nodes.Load( name='x' ), target=None ) )
		self._parseStringTest( g.exceptStmtHeader(), 'except x, y:\n', Nodes.ExceptStmtHeader( exception=Nodes.Load( name='x' ), target=Nodes.SingleTarget( name='y' ) ) )
		self._parseNodeTest( g.exceptStmtHeader(), Nodes.ExceptStmtHeader( exception=None, target=None ), Nodes.ExceptStmtHeader( exception=None, target=None ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.ExceptStmtHeader( exception=None, target=None ), Nodes.ExceptStmtHeader( exception=None, target=None ) )


	def testFinallyStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.finallyStmtHeader(), 'finally:\n', Nodes.FinallyStmtHeader() )
		self._parseNodeTest( g.finallyStmtHeader(), Nodes.FinallyStmtHeader(), Nodes.FinallyStmtHeader() )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.FinallyStmtHeader(), Nodes.FinallyStmtHeader() )


	def testWithStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.withStmtHeader(), 'with a:\n', Nodes.WithStmtHeader( expr=Nodes.Load( name='a' ), target=None ) )
		self._parseStringTest( g.withStmtHeader(), 'with a as b:\n', Nodes.WithStmtHeader( expr=Nodes.Load( name='a' ), target=Nodes.SingleTarget( name='b' ) ) )
		self._parseNodeTest( g.withStmtHeader(), Nodes.WithStmtHeader( expr=Nodes.Load( name='a' ), target=None ), Nodes.WithStmtHeader( expr=Nodes.Load( name='a' ), target=None ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.WithStmtHeader( expr=Nodes.Load( name='a' ), target=None ), Nodes.WithStmtHeader( expr=Nodes.Load( name='a' ), target=None ) )


	def testDecoStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.decoStmtHeader(), '@f\n', Nodes.DecoStmtHeader( name='f', args=None ) )
		self._parseStringTest( g.decoStmtHeader(), '@f(x)\n', Nodes.DecoStmtHeader( name='f', args=[ Nodes.Load( name='x' ) ] ) )
		self._parseNodeTest( g.decoStmtHeader(), Nodes.DecoStmtHeader( name='f', args=None ), Nodes.DecoStmtHeader( name='f', args=None ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.DecoStmtHeader( name='f', args=None ), Nodes.DecoStmtHeader( name='f', args=None ) )


	def testDefStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.defStmtHeader(), 'def f():\n', Nodes.DefStmtHeader( name='f', params=[] ) )
		self._parseStringTest( g.defStmtHeader(), 'def f(x):\n', Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ) )
		self._parseNodeTest( g.defStmtHeader(), Nodes.DefStmtHeader( name='f', params=[] ), Nodes.DefStmtHeader( name='f', params=[] ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.DefStmtHeader( name='f', params=[] ), Nodes.DefStmtHeader( name='f', params=[] ) )


	def testClassStmtHeader(self):
		g = Python25Grammar()
		self._parseStringTest( g.classStmtHeader(), 'class Q:\n', Nodes.ClassStmtHeader( name='Q', bases=None ) )
		self._parseStringTest( g.classStmtHeader(), 'class Q (x):\n', Nodes.ClassStmtHeader( name='Q', bases=[ Nodes.Load( name='x' ) ] ) )
		self._parseStringTest( g.classStmtHeader(), 'class Q (x,):\n', Nodes.ClassStmtHeader( name='Q', bases=[ Nodes.Load( name='x' ) ], basesTrailingSeparator='1' ) )
		self._parseStringTest( g.classStmtHeader(), 'class Q (x,y):\n', Nodes.ClassStmtHeader( name='Q', bases=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ) )
		self._parseNodeTest( g.classStmtHeader(), Nodes.ClassStmtHeader( name='Q', bases=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ),
				     Nodes.ClassStmtHeader( name='Q', bases=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ) )
		self._parseNodeTest( g.compoundStmtHeader(), Nodes.ClassStmtHeader( name='Q', bases=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ),
				     Nodes.ClassStmtHeader( name='Q', bases=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ) )


	def testCommentStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.commentStmt(), '#x\n', Nodes.CommentStmt( comment='x' ) )
		self._parseStringTest( g.commentStmt(), '#' + string.printable.replace( '\n', '' ) + '\n', Nodes.CommentStmt( comment=string.printable.replace( '\n', '' ) ) )
		self._parseNodeTest( g.commentStmt(), Nodes.CommentStmt( comment=string.printable.replace( '\n', '' ) ), Nodes.CommentStmt( comment=string.printable.replace( '\n', '' ) ) )



	def testBlankLine(self):
		g = Python25Grammar()
		self._parseStringTest( g.blankLine(), '\n', Nodes.BlankLine() )





	def testFnCallStStmt(self):
		g = Python25Grammar()
		self._parseStringTest( g.expression(), 'x.y()', Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) )
		self._parseStringTest( g.singleLineStatementValid(), 'x.y()\n', Nodes.ExprStmt( expr=Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) ) )
		self._parseNodeTest( g.singleLineStatementValid(), Nodes.ExprStmt( expr=Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) ),
				     Nodes.ExprStmt( expr=Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) ) )




	def testDictInList(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatementValid(), 'y = [ x, { a : b } ]\n', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='y' ) ], value=Nodes.ListLiteral( values=[ Nodes.Load( name='x' ), Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=Nodes.Load( name='b' ) ) ] ) ] ) ) )
		
		
		
		
	def test_unparsed(self):
		g = Python25Grammar()
		self._parseStringTest( g.singleLineStatement(), 'foo bar xyz\n', Nodes.UNPARSED( value=[ 'foo bar xyz' ] ) )
		self._parseStringTest( g.singleLineStatement(), 'as\n', Nodes.UNPARSED( value=[ 'as' ] ) )
		self._parseStringTest( g.suite(), 'as\n', [ Nodes.UNPARSED( value=[ 'as' ] ) ] )
		


		
		
		
	def test_emptyIndentedSuite(self):
		g = Python25Grammar()
		self._parseListTest( g.emptyIndentedSuite(),
				     [
					     Nodes.Indent(),
					     Nodes.Dedent(), ],
				     [
					     Nodes.Indent(),
					     Nodes.Dedent(), ] )

		
	def test_singleIndentedSuite(self):
		g = Python25Grammar()
		self._parseListTest( g.singleIndentedSuite(),
				     [
					     Nodes.Indent(),
					     Nodes.ContinueStmt(),
					     Nodes.Dedent(), ],
				     [ Nodes.ContinueStmt() ] )
		self._parseListTest( g.singleIndentedSuite(),
				     [
					     Nodes.IndentedBlock( suite=[ Nodes.ContinueStmt() ] ), ],
				     [ Nodes.ContinueStmt() ] )

		
	def test_compoundSuite(self):
		g = Python25Grammar()
		self._parseListTest( g.compoundSuite(),
				     [
					     Nodes.Indent(),
					     Nodes.ContinueStmt(),
					     Nodes.Dedent(), ],
				     [ Nodes.ContinueStmt() ] )
		self._parseListTest( g.compoundSuite(),
				     [
					     Nodes.Indent(),
					     Nodes.ContinueStmt(),
					     Nodes.Dedent(),
					     Nodes.Indent(),
					     Nodes.BreakStmt(),
					     Nodes.Dedent(), ],
				     [ Nodes.ContinueStmt(), Nodes.BreakStmt() ] )
		self._parseListTest( g.compoundSuite(),
				     [
					     Nodes.Indent(),
					     Nodes.ContinueStmt(),
					     Nodes.Dedent(),
					     Nodes.IndentedBlock( suite=[ Nodes.BreakStmt() ] ), ],
				     [ Nodes.ContinueStmt(), Nodes.BreakStmt() ] )

		
		
	def test_indentedBlock(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.IndentedBlock( suite=[ Nodes.BlankLine() ] ) )


		
	def test_ifStmt(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElifStmtHeader( condition=Nodes.Load( name='b' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[ Nodes.ElifBlock( condition=Nodes.Load( name='b' ), suite=[ Nodes.CommentStmt( comment='y' ) ] ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BreakStmt() ], elifBlocks=[] ),
					     Nodes.ElifStmtHeader( condition=Nodes.Load( name='b' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BreakStmt() ], elifBlocks=[ Nodes.ElifBlock( condition=Nodes.Load( name='b' ), suite=[ Nodes.CommentStmt( comment='y' ) ] ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[ Nodes.ElifBlock( condition=Nodes.Load( name='b' ), suite=[ Nodes.CommentStmt( comment='y' ) ] ) ] ),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='z' ),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[ Nodes.ElifBlock( condition=Nodes.Load( name='b' ), suite=[ Nodes.CommentStmt( comment='y' ) ] ) ],
						   elseSuite=[ Nodes.CommentStmt( comment='z' ) ]) )

		
	def test_whileStmt(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
	
		
	def test_forStmt(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ] ),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )

		
	def test_tryStmt(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[], finallySuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ),
												Nodes.ExceptBlock( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ), suite=[ Nodes.CommentStmt( comment='y' ) ] )] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Nodes.CommentStmt( comment='y' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    finallySuite=[ Nodes.CommentStmt( comment='y' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent(),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='z' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Nodes.CommentStmt( comment='y' ) ], finallySuite=[ Nodes.CommentStmt( comment='z' ) ] ) )

		self._parseListFailTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ] )
		self._parseListFailTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ] )

		# Try with 1 except block, add another
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
							    exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ),
												Nodes.ExceptBlock( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ) )
		# Try with 1 except block, add an else
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
							    exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Nodes.CommentStmt( comment='y' ) ] ) )
		# Try with 1 except block, add a finally
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
							    exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    finallySuite=[ Nodes.CommentStmt( comment='y' ) ] ) )
		# Try with 1 except block, add an else and a finally
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
							    exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent(),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='z' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Nodes.CommentStmt( comment='y' ) ],  finallySuite=[ Nodes.CommentStmt( comment='z' ) ] ) )
		
		
	def test_withStmt(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WithStmtHeader( expr=Nodes.SingleTarget( name='a' ), target=Nodes.Load( name='x' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.WithStmt( expr=Nodes.SingleTarget( name='a' ), target=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ] ) )
	
		
	def test_defStmt(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.DefStmt( decorators=[], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.DecoStmtHeader( name='a', args=[ Nodes.Load( name='x' ) ] ),
					     Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.DefStmt( decorators=[ Nodes.Decorator( name='a', args=[ Nodes.Load( name='x' ) ] ) ], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.DecoStmtHeader( name='a', args=[ Nodes.Load( name='x' ) ] ),
					     Nodes.DecoStmtHeader( name='b', args=[ Nodes.Load( name='y' ) ] ),
					     Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.DefStmt( decorators=[ Nodes.Decorator( name='a', args=[ Nodes.Load( name='x' ) ] ), Nodes.Decorator( name='b', args=[ Nodes.Load( name='y' ) ] ) ],
						    name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.DecoStmtHeader( name='a', args=[ Nodes.Load( name='x' ) ] ),
					     Nodes.DefStmt( decorators=[ Nodes.Decorator( name='b', args=[ Nodes.Load( name='y' ) ] ) ], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) ],
				     Nodes.DefStmt( decorators=[ Nodes.Decorator( name='a', args=[ Nodes.Load( name='x' ) ] ), Nodes.Decorator( name='b', args=[ Nodes.Load( name='y' ) ] ) ],
						    name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
	
		
	def test_classStmt(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ClassStmtHeader( name='A', bases=[ Nodes.Load( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.ClassStmt( name='A', bases=[ Nodes.Load( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
		
		
	def test_nestedStructure(self):
		g = Python25Grammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ClassStmtHeader( name='A', bases=[ Nodes.Load( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
						Nodes.Indent(),
					     		Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     		Nodes.Indent(),
								Nodes.BlankLine(),
					     		Nodes.Dedent(),
						Nodes.Dedent(),
					     Nodes.Dedent() ],
				     Nodes.ClassStmt( name='A', bases=[ Nodes.Load( name='x' ) ], suite=[
					     Nodes.DefStmt( decorators=[], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ) ] ) ] ) )
	
		
	def test_suite(self):
		g = Python25Grammar()
		self._parseListTest( g.suite(),
				     [
					     Nodes.CommentStmt( comment='x' ),
					     Nodes.BlankLine() ],
				      [
					     Nodes.CommentStmt( comment='x' ),
					     Nodes.BlankLine() ] )
		

	
		
	def test_header_indentedBlock(self):
		g = Python25Grammar()
		self._parseListTest( g.suite(),
				     [
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='x' ) ),
					     Nodes.IndentedBlock( suite=[ Nodes.CommentStmt( comment='a' ) ] ) ],
				      [
					     Nodes.WhileStmt( condition=Nodes.Load( name='x' ), suite=[ Nodes.CommentStmt( comment='a' ) ] ) ] )
		

	
		
	def test_headers(self):
		g = Python25Grammar()
		self._parseListTest( g.suite(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='x' ) ),
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				      [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='x' ) ),
					     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ) ] )

		
	def test_streamSuite(self):	
		g = Python25Grammar()
		s = self._pythonStream( 'while a:\n', Nodes.Indent(), 'continue\n', Nodes.Dedent() )
		self._parseStreamTest( g.suite(), s, [ Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.ContinueStmt() ] ) ] )
		
		
	def test_embeddedStructural(self):
		g = Python25Grammar()
		#s = self._pythonStream( 'x = ', Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), '\n' )
		s = self._pythonStream( 'x = ', Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), '\n' )
		self._parseStreamTest( g.suite(), s, [ Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='x' ) ], value=Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ) ] )
		
		


def parserViewTest():
	#result, pos, dot = targetListOrTargetItem.debugParseString( 'a.b' )
	#result, pos, dot = subscript.debugParseString( 'a.b' )
	#print dot

	#g = Python25Grammar()
	#g.singleLineStatementValid().parseStringChars( 'raise' )

	from BritefuryJ.ParserDebugViewer import ParseViewFrame

	g = Python25Grammar()
	result = g.expression().debugParseStringChars( '[i for i in a]' )
	ParseViewFrame( result )
