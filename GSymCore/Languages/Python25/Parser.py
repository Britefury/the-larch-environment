##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string

from BritefuryJ.DocModel import DMObject, DMNode

from BritefuryJ.Parser import Action, Condition, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, SeparatedList
from BritefuryJ.Parser.Utils.Tokens import identifier, decimalInteger, hexInteger, integer, singleQuotedString, doubleQuotedString, quotedString, floatingPoint
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, UnaryOperator, BinaryOperator, ChainOperator, OperatorTable

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
	# Python identifier
	@Rule
	def pythonIdentifier(self):
		return identifier  &  ( lambda input, begin, end, x, bindings: x not in keywordsSet )


	@Rule
	def dottedPythonIdentifer(self):
		return SeparatedList( self.pythonIdentifier(), '.', 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ).action( lambda input, begin, end, xs, bindings: '.'.join( xs ) )





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



	# Attribute name
	@Rule
	def attrName(self):
		return self.pythonIdentifier()



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
	def yieldAtom(self):
		return ( Literal( '(' )  +  Keyword( yieldKeyword )  +  self.tupleOrExpression()  +  Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: Nodes.YieldAtom( value=xs[2] ) )



	# Enclosure
	@Rule
	def enclosure(self):
		return self.parenForm() | self.tupleLiteral() | self.listLiteral() | self.listComprehension() | self.generatorExpression() | self.dictLiteral() | self.yieldAtom()




	# Atom
	@Rule
	def atom(self):
		return self.enclosure() | self.literal() | self.loadLocal()




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
		return self.lambdaExpr()  |  self.conditionalExpression()  |  self.orTest()



	# Tuple or (old) expression
	@Rule
	def tupleOrExpression(self):
		return self.tupleAsExpressionList() | self.expression()

	@Rule
	def oldTupleOrExpression(self):
		return self.oldTupleAsExpressionList() | self.oldExpression()




	# Tuple or expression or yield expression
	@Rule
	def tupleOrExpressionOrYieldExpression(self):
		return self.tupleOrExpression() | self.yieldAtom()





	# Expression statement
	@Rule
	def exprStmt(self):
		return self.expression().action( lambda input, begin, end, xs, bindings: Nodes.ExprStmt( expr=xs ) )




	# Assert statement
	@Rule
	def assertStmt(self):
		return ( Keyword( assertKeyword ) + self.expression()  +  Optional( Literal( ',' ) + self.expression() ) ).action(
			lambda input, begin, end, xs, bindings: Nodes.AssertStmt( condition=xs[1], fail=xs[2][1]   if xs[2] is not None  else  None ) )




	# Assignment statement
	@Rule
	def assignmentStmt(self):
		return ( OneOrMore( ( self.targetListOrTargetItem()  +  '=' ).action( lambda input, begin, end, xs, bindings: xs[0] ) )  +  self.tupleOrExpressionOrYieldExpression() ).action(
			lambda input, begin, end, xs, bindings: Nodes.AssignStmt( targets=xs[0], value=xs[1] ) )




	# Augmented assignment statement
	@Rule
	def augOp(self):
		return Choice( [ Literal( op )   for op in augAssignOps ] )

	@Rule
	def augAssignStmt(self):
		return ( self.targetItem()  +  self.augOp()  +  self.tupleOrExpressionOrYieldExpression() ).action(
			lambda input, begin, end, xs, bindings: Nodes.AugAssignStmt( op=xs[1], target=xs[0], value=xs[2] ) )




	# Pass statement
	@Rule
	def passStmt(self):
		return Keyword( passKeyword ).action( lambda input, begin, end, xs, bindings: Nodes.PassStmt() )



	# Del statement
	@Rule
	def delStmt(self):
		return ( Keyword( delKeyword )  +  self.targetListOrTargetItem() ).action( lambda input, begin, end, xs, bindings: Nodes.DelStmt( target=xs[1] ) )



	# Return statement
	@Rule
	def returnStmt(self):
		return ( Keyword( returnKeyword )  +  self.tupleOrExpression() ).action( lambda input, begin, end, xs, bindings: Nodes.ReturnStmt( value=xs[1] ) )



	# Yield statement
	@Rule
	def yieldStmt(self):
		return ( Keyword( yieldKeyword )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Nodes.YieldStmt( value=xs[1] ) )




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
		return ( Keyword( raiseKeyword ) + Optional( self.expression() + Optional( Literal( ',' ) + self.expression() + Optional( Literal( ',' ) + self.expression() ) ) ) ).action(
			lambda input, begin, end, xs, bindings: self._buildRaise( xs ) )




	# Break statement
	@Rule
	def breakStmt(self):
		return Keyword( breakKeyword ).action( lambda input, begin, end, xs, bindings: Nodes.BreakStmt() )




	# Continue statement
	@Rule
	def continueStmt(self):
		return Keyword( continueKeyword ).action( lambda input, begin, end, xs, bindings: Nodes.ContinueStmt() )




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
		return ( Keyword( importKeyword )  +  SeparatedList( self.moduleImport(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) ).action( lambda input, begin, end, xs, bindings: Nodes.ImportStmt( modules=xs[1] ) )


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
				 )  \
			 ).action( lambda input, begin, end, xs, bindings: Nodes.FromImportStmt( module=xs[1], imports=xs[3] ) )


	# 'from' <relativeModule> 'import' '*'
	@Rule
	def fromImportAll(self):
		return ( Keyword( fromKeyword ) + self.relativeModule() + Keyword( importKeyword ) + '*' ).action( lambda input, begin, end, xs, bindings: Nodes.FromImportAllStmt( module=xs[1] ) )


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
		return ( Keyword( globalKeyword )  +  SeparatedList( self.globalVar(), 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) ).action( lambda input, begin, end, xs, bindings: Nodes.GlobalStmt( vars=xs[1] ) )





	# Exec statement
	@Rule
	def execCodeStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp() ).action( lambda input, begin, end, xs, bindings: Nodes.ExecStmt( source=xs[1], locals=None, globals=None ) )

	@Rule
	def execCodeInLocalsStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp()  +  Keyword( inKeyword )  +  self.expression() ).action( lambda input, begin, end, xs, bindings: Nodes.ExecStmt( source=xs[1], locals=xs[3], globals=None ) )

	@Rule
	def execCodeInLocalsAndGlobalsStmt(self):
		return ( Keyword( execKeyword )  +  self.orOp()  +  Keyword( inKeyword )  +  self.expression()  +  ','  +  self.expression() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ExecStmt( source=xs[1], locals=xs[3], globals=xs[5] ) )

	@Rule
	def execStmt(self):
		return self.execCodeInLocalsAndGlobalsStmt() | self.execCodeInLocalsStmt() | self.execCodeStmt()




	# If statement
	@Rule
	def ifStmt(self):
		return ( Keyword( ifKeyword )  +  self.expression()  +  ':' ).action( lambda input, begin, end, xs, bindings: Nodes.IfStmt( condition=xs[1], suite=[] ) )



	# Elif statement
	@Rule
	def elifStmt(self):
		return ( Keyword( elifKeyword )  +  self.expression()  +  ':' ).action( lambda input, begin, end, xs, bindings: Nodes.ElifStmt( condition=xs[1], suite=[] ) )



	# Else statement
	@Rule
	def elseStmt(self):
		return( Keyword( elseKeyword )  +  ':' ).action( lambda input, begin, end, xs, bindings: Nodes.ElseStmt( suite=[] ) )



	# While statement
	@Rule
	def whileStmt(self):
		return ( Keyword( whileKeyword )  +  self.expression()  +  ':' ).action( lambda input, begin, end, xs, bindings: Nodes.WhileStmt( condition=xs[1], suite=[] ) )



	# For statement
	@Rule
	def forStmt(self):
		return ( Keyword( forKeyword )  +  self.targetListOrTargetItem()  +  Keyword( inKeyword )  +  self.tupleOrExpression()  +  ':' ).action(
			lambda input, begin, end, xs, bindings: Nodes.ForStmt( target=xs[1], source=xs[3], suite=[] ) )



	# Try statement
	@Rule
	def tryStmt(self):
		return ( Keyword( tryKeyword )  +  ':' ).action( lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=[] ) )




	# Except statement
	@Rule
	def exceptAllStmt(self):
		return ( Keyword( exceptKeyword ) + ':' ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptStmt( exception=None, target=None, suite=[] ) )

	@Rule
	def exceptExcStmt(self):
		return ( Keyword( exceptKeyword )  +  self.expression() + ':' ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptStmt( exception=xs[1], target=None, suite=[] ) )

	@Rule
	def exceptExcIntoTargetStmt(self):
		return ( Keyword( exceptKeyword )  +  self.expression()  +  ','  +  self.targetItem() + ':' ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptStmt( exception=xs[1], target=xs[3], suite=[] ) )

	@Rule
	def exceptStmt(self):
		return self.exceptExcIntoTargetStmt() | self.exceptExcStmt() | self.exceptAllStmt()




	# Finally statement
	@Rule
	def finallyStmt(self):
		return ( Keyword( finallyKeyword )  +  ':' ).action( lambda input, begin, end, xs, bindings: Nodes.FinallyStmt( suite=[] ) )



	# With statement
	@Rule
	def withStmt(self):
		return ( Keyword( withKeyword )  +  self.expression()  +  Optional( Keyword( asKeyword )  +  self.targetItem() )  +  ':' ).action(
			lambda input, begin, end, xs, bindings: Nodes.WithStmt( expr=xs[1], target=xs[2][1]   if xs[2] is not None   else   None, suite=[] ) )



	# Def statement
	@Rule
	def defStmt(self):
		return ( Keyword( defKeyword )  +  self.pythonIdentifier()  +  '('  +  self.params()  +  ')'  +  ':' ).action(
			lambda input, begin, end, xs, bindings: Nodes.DefStmt( name=xs[1], params=xs[3][0], paramsTrailingSeparator=xs[3][1], suite=[] ) )



	# Decorator statement
	@Rule
	def decoStmt(self):
		def _action(input, begin, end, xs, bindings):
			if xs[2] is not None:
				args = xs[2][1][0]
				trailingSeparator = xs[2][1][1]
			else:
				args = None
				trailingSeparator = None
			return Nodes.DecoStmt( name=xs[1], args=args, argsTrailingSeparator=trailingSeparator )

		return ( Literal( '@' )  +  self.dottedPythonIdentifer()  +  Optional( Literal( '(' )  +  self.callArgs()  +  ')' ) ).action( _action )



	# Class statement
	@Rule
	def classStmt(self):
		bases = SeparatedList( self.expression(), '(', ')', 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listAction( lambda input, begin, end, xs, bindings, bTrailingSep: [ xs, '1'   if bTrailingSep   else None ] )
		def _action(input, begin, end, xs, bindings):
			if xs[2] is not None:
				bases = xs[2][0]
				trailingSep = xs[2][1]
			else:
				bases = None
				trailingSep = None
			return Nodes.ClassStmt( name=xs[1], bases=bases, basesTrailingSeparator=trailingSep, suite=[] )
		return ( Keyword( classKeyword )  +  self.pythonIdentifier()  +  Optional( bases )  +  ':' ).action( _action )



	# Comment statement
	@Rule
	def commentStmt(self):
		return ( Literal( '#' )  +  Word( string.printable ).optional() ).action( lambda input, begin, end, xs, bindings: Nodes.CommentStmt( comment=xs[1]   if xs[1] is not None   else  '' ) )





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
		return self.simpleStmt() | self.compoundStmtHeader() | self.commentStmt() | self.exprStmt()






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
		self._matchTest( g.targetListOrTargetItem(), 'a', Nodes.SingleTarget( name='a' ) )
		self._matchTest( g.targetListOrTargetItem(), '(a)', Nodes.SingleTarget( name='a', parens='1' ) )

		self._matchTest( g.targetListOrTargetItem(), '(a,)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ) ], trailingSeparator='1', parens='1' ) )
		self._matchTest( g.targetListOrTargetItem(), 'a,b', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._matchTest( g.targetListOrTargetItem(), '(a,b)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ], parens='1' ) )
		self._matchTest( g.targetListOrTargetItem(), '(a,b,)', Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ], trailingSeparator='1', parens='1' ) )
		self._matchTest( g.targetListOrTargetItem(), '(a,b),(c,d)', Nodes.TupleTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ], parens='1' ),
													 Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='c' ), Nodes.SingleTarget( name='d' ) ], parens='1' ) ] ) )

		self._matchFailTest( g.targetListOrTargetItem(), '(a,) (b,)' )

		self._matchTest( g.targetListOrTargetItem(), '[a]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ) )
		self._matchTest( g.targetListOrTargetItem(), '[a,]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ], trailingSeparator='1' ) )
		self._matchTest( g.targetListOrTargetItem(), '[a,b]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ) )
		self._matchTest( g.targetListOrTargetItem(), '[a,b,]', Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ], trailingSeparator='1' ) )
		self._matchTest( g.targetListOrTargetItem(), '[a],[b,]', Nodes.TupleTarget( targets=[ Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ) ] ),
												      Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='b' ) ], trailingSeparator='1' ) ] ) )
		self._matchTest( g.targetListOrTargetItem(), '[(a,)],[(b,)]', Nodes.TupleTarget( targets=[ Nodes.ListTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ) ], trailingSeparator='1', parens='1' ) ] ),
													   Nodes.ListTarget( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='b' ) ], trailingSeparator='1', parens='1' ) ] ) ] ) )

		self._matchTest( g.subscript(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.attributeRefOrSubscript(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetItem(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetListOrTargetItem(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetListOrTargetItem(), 'a[x][y]', Nodes.Subscript( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ), index=Nodes.Load( name='y' ) ) )
		self._matchTest( g.targetListOrTargetItem(), 'a.b', Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ) )
		self._matchTest( g.targetListOrTargetItem(), 'a.b.c', Nodes.AttributeRef( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), name='c' ) )

		self._matchTest( g.targetListOrTargetItem(), 'a.b[x]', Nodes.Subscript( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.targetListOrTargetItem(), 'a[x].b', Nodes.AttributeRef( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ), name='b' ) )


	def testTupleLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '()', Nodes.TupleLiteral( values=[] ) )
		self._matchTest( g.expression(), '(())', Nodes.TupleLiteral( values=[], parens='1' ) )
		self._matchTest( g.expression(), '(a)', Nodes.Load( name='a', parens='1' ) )
		self._matchTest( g.expression(), '(a,)', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ) ], trailingSeparator='1' ) )
		self._matchTest( g.expression(), '((a,))', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ) ], trailingSeparator='1', parens='1' ) )
		self._matchTest( g.expression(), '(a,b)', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		self._matchTest( g.expression(), '(a,b,)', Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ], trailingSeparator='1' ) )


	def testListLiteral(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '[]', Nodes.ListLiteral( values=[] ) )
		self._matchTest( g.expression(), '[a,b]', Nodes.ListLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		self._matchTest( g.expression(), '[a,b,]', Nodes.ListLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ], trailingSeparator='1' ) )


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
											   Nodes.DictKeyValuePair( key=Nodes.Load( name='b' ), value=Nodes.Load( name='y' ) ) ], trailingSeparator='1' ) )


	def testYieldAtom(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '(yield 2+3)', Nodes.YieldAtom( value=Nodes.Add( x=Nodes.IntLiteral( format='decimal', numType='int', value='2' ), y=Nodes.IntLiteral( format='decimal', numType='int', value='3' ) ) ) )



	def testAttributeRef(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'a.b', Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ) )


	def testSubscript(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'a[x]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ) )
		self._matchTest( g.expression(), 'a[x:p]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ) ) ) )
		self._matchTest( g.expression(), 'a[x:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='x' ), upper=None ) ) )
		self._matchTest( g.expression(), 'a[:p]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=None, upper=Nodes.Load( name='p' ) ) ) )
		self._matchTest( g.expression(), 'a[:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=None, upper=None ) ) )
		self._matchTest( g.expression(), 'a[x:p:f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[x:p:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=Nodes.Load( name='p' ), stride=None ) ) )
		self._matchTest( g.expression(), 'a[x::f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=None, stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[:p:f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=Nodes.Load( name='p' ), stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[::]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=None, stride=None ) ) )
		self._matchTest( g.expression(), 'a[::f]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=None, stride=Nodes.Load( name='f' ) ) ) )
		self._matchTest( g.expression(), 'a[x::]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='x' ), upper=None, stride=None ) ) )
		self._matchTest( g.expression(), 'a[:p:]', Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=None, upper=Nodes.Load( name='p' ), stride=None ) ) )
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
		self._matchTest( g.expression(), 'a(f,)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ) ], argsTrailingSeparator='1' ) )
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
		self._matchTest( g.expression(), 'a(**w+x)', Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.CallKWArgList( value=Nodes.Add( x=Nodes.Load( name='w' ), y=Nodes.Load( name='x' ) ) ) ] ) )
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
		self._matchTest( g.expression(), 'a is not b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIsNot( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a is b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIs( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a not in b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpNotIn( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'a in b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIn( y=Nodes.Load( name='b' ) ) ] ) )
		self._matchTest( g.expression(), 'not a', Nodes.NotTest( x=Nodes.Load( name='a' ) ) )
		self._matchTest( g.expression(), 'a and b', Nodes.AndTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), 'a or b', Nodes.OrTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )


	def testOperatorPrecedence(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'a + b < c', Nodes.Cmp( x=Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), ops=[ Nodes.CmpOpLt( y=Nodes.Load( name='c' ) ) ] ) )


	def testParens(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), '(a)', Nodes.Load( name='a', parens='1' ) )
		self._matchTest( g.expression(), '(((a)))', Nodes.Load( name='a', parens='3' ) )
		self._matchTest( g.expression(), '(a+b)', Nodes.Add( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._matchTest( g.expression(), '(a+b)*c', Nodes.Mul( x=Nodes.Add( parens='1', x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Load( name='c' ) ) )


	def testParams(self):
		g = Python25Grammar()
		self._matchTest( g.params(), '', [ [], None ] )
		self._matchTest( g.params(), 'f', [ [ Nodes.SimpleParam( name='f' ) ], None ] )
		self._matchTest( g.params(), 'f,', [ [ Nodes.SimpleParam( name='f' ) ], '1' ] )
		self._matchTest( g.params(), 'f,g', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ) ], None ] )
		self._matchTest( g.params(), 'f,g,m=a', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ) ], None ] )
		self._matchTest( g.params(), 'f,g,m=a,n=b', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ) ], None ] )
		self._matchTest( g.params(), 'f,g,m=a,n=b,*p', [ [ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ), Nodes.ParamList( name='p' ) ], None ] )
		self._matchTest( g.params(), 'f,m=a,*p,**w', [ [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._matchTest( g.params(), 'f,m=a,*p', [ [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ) ], None ] )
		self._matchTest( g.params(), 'f,m=a,**w', [ [ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.KWParamList( name='w' ) ], None ] )
		self._matchTest( g.params(), 'f,*p,**w', [ [ Nodes.SimpleParam( name='f' ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._matchTest( g.params(), 'm=a,*p,**w', [ [ Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._matchTest( g.params(), '*p,**w', [ [ Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], None ] )
		self._matchTest( g.params(), '**w', [ [ Nodes.KWParamList( name='w' ) ], None ] )
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
		self._matchTest( g.expression(), '(x   if y else   z)   if w else   q', Nodes.ConditionalExpr( condition=Nodes.Load( name='w' ), expr=Nodes.ConditionalExpr( parens='1', condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ), elseExpr=Nodes.Load( name='q' ) ) )
		self._matchTest( g.expression(), 'w   if (x   if y else   z) else   q', Nodes.ConditionalExpr( condition=Nodes.ConditionalExpr( parens='1', condition=Nodes.Load( name='y' ), expr=Nodes.Load( name='x' ), elseExpr=Nodes.Load( name='z' ) ), expr=Nodes.Load( name='w' ), elseExpr=Nodes.Load( name='q' ) ) )
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
		self._matchTest( g.statement(), 'assert x', Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=None ) )
		self._matchTest( g.statement(), 'assert x,y', Nodes.AssertStmt( condition=Nodes.Load( name='x' ), fail=Nodes.Load( name='y' ) ) )


	def testAssignmentStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'a=x', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.Load( name='x' ) ) )
		self._matchTest( g.statement(), 'a=b=x', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ], value=Nodes.Load( name='x' ) ) )
		self._matchTest( g.statement(), 'a,b=c,d=x', Nodes.AssignStmt( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ),  Nodes.SingleTarget( name='b' ) ] ),
											 Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='c' ),  Nodes.SingleTarget( name='d' ) ] ) ], value=Nodes.Load( name='x' ) ) )
		self._matchTest( g.statement(), 'a=(yield x)', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.YieldAtom( value=Nodes.Load( name='x' ) ) ) )
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
		self._matchTest( g.statement(), 'raise', Nodes.RaiseStmt( excType=None, excValue=None, traceback=None ) )
		self._matchTest( g.statement(), 'raise x', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=None, traceback=None ) )
		self._matchTest( g.statement(), 'raise x,y', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=Nodes.Load( name='y' ), traceback=None ) )
		self._matchTest( g.statement(), 'raise x,y,z', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=Nodes.Load( name='y' ), traceback=Nodes.Load( name='z' ) ) )


	def testBreakStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'break', Nodes.BreakStmt() )


	def testContinueStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'continue', Nodes.ContinueStmt() )


	def testImportStmt(self):
		g = Python25Grammar()
		self._matchTest( g._moduleIdentifier(), 'abc', 'abc' )
		self._matchTest( g.moduleName(), 'abc', 'abc' )
		self._matchTest( g.moduleName(), 'abc.xyz', 'abc.xyz' )
		self._matchTest( g._relModDotsModule(), 'abc.xyz', 'abc.xyz' )
		self._matchTest( g._relModDotsModule(), '...abc.xyz', '...abc.xyz' )
		self._matchTest( g._relModDots(), '...', '...' )
		self._matchTest( g.relativeModule(), 'abc.xyz', Nodes.RelativeModule( name='abc.xyz' ) )
		self._matchTest( g.relativeModule(), '...abc.xyz', Nodes.RelativeModule( name='...abc.xyz' ) )
		self._matchTest( g.relativeModule(), '...', Nodes.RelativeModule( name='...' ) )
		self._matchTest( g.moduleImport(), 'abc.xyz', Nodes.ModuleImport( name='abc.xyz' ) )
		self._matchTest( g.moduleImport(), 'abc.xyz as q', Nodes.ModuleImportAs( name='abc.xyz', asName='q' ) )
		self._matchTest( g.simpleImport(), 'import a', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a' ) ] ) )
		self._matchTest( g.simpleImport(), 'import a.b', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a.b' ) ] ) )
		self._matchTest( g.simpleImport(), 'import a.b as x', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._matchTest( g.simpleImport(), 'import a.b as x, c.d as y', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ), Nodes.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._matchTest( g.moduleContentImport(), 'xyz', Nodes.ModuleContentImport( name='xyz' ) )
		self._matchTest( g.moduleContentImport(), 'xyz as q', Nodes.ModuleContentImportAs( name='xyz', asName='q' ) )
		self._matchTest( g.fromImport(), 'from x import a', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import a as p', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import a as p, b as q', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import (a)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import (a,)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import (a as p)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import (a as p,)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import ( a as p, b as q )', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._matchTest( g.fromImport(), 'from x import ( a as p, b as q, )', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._matchTest( g.fromImportAll(), 'from x import *', Nodes.FromImportAllStmt( module=Nodes.RelativeModule( name='x' ) ) )
		self._matchTest( g.importStmt(), 'import a', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a' ) ] ) )
		self._matchTest( g.importStmt(), 'import a.b', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a.b' ) ] ) )
		self._matchTest( g.importStmt(), 'import a.b as x', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._matchTest( g.importStmt(), 'import a.b as x, c.d as y', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ), Nodes.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import a', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import a as p', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import a as p, b as q', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import (a)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import (a,)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import (a as p)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import (a as p,)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import ( a as p, b as q )', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import ( a as p, b as q, )', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._matchTest( g.importStmt(), 'from x import *', Nodes.FromImportAllStmt( module=Nodes.RelativeModule( name='x' ) ) )


	def testGlobalStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'global x', Nodes.GlobalStmt( vars=[ Nodes.GlobalVar( name='x' ) ] ) )
		self._matchTest( g.statement(), 'global x, y', Nodes.GlobalStmt( vars=[ Nodes.GlobalVar( name='x' ), Nodes.GlobalVar( name='y' ) ] ) )


	def testExecStmt(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'exec a', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=None, globals=None ) )
		self._matchTest( g.statement(), 'exec a in b', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=Nodes.Load( name='b' ), globals=None ) )
		self._matchTest( g.statement(), 'exec a in b,c', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=Nodes.Load( name='b' ), globals=Nodes.Load( name='c' ) ) )


	def testIfStmt(self):
		g = Python25Grammar()
		self._matchTest( g.ifStmt(), 'if a:', Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[] ) )


	def testElIfStmt(self):
		g = Python25Grammar()
		self._matchTest( g.elifStmt(), 'elif a:', Nodes.ElifStmt( condition=Nodes.Load( name='a' ), suite=[] ) )


	def testElseStmt(self):
		g = Python25Grammar()
		self._matchTest( g.elseStmt(), 'else:', Nodes.ElseStmt( suite=[] ) )


	def testWhileStmt(self):
		g = Python25Grammar()
		self._matchTest( g.whileStmt(), 'while a:', Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[] ) )


	def testForStmt(self):
		g = Python25Grammar()
		self._matchTest( g.forStmt(), 'for x in y:', Nodes.ForStmt( target=Nodes.SingleTarget( name='x' ), source=Nodes.Load( name='y' ), suite=[] ) )


	def testTryStmt(self):
		g = Python25Grammar()
		self._matchTest( g.tryStmt(), 'try:', Nodes.TryStmt( suite=[] ) )


	def testExceptStmt(self):
		g = Python25Grammar()
		self._matchTest( g.exceptStmt(), 'except:', Nodes.ExceptStmt( exception=None, target=None, suite=[] ) )
		self._matchTest( g.exceptStmt(), 'except x:', Nodes.ExceptStmt( exception=Nodes.Load( name='x' ), target=None, suite=[] ) )
		self._matchTest( g.exceptStmt(), 'except x, y:', Nodes.ExceptStmt( exception=Nodes.Load( name='x' ), target=Nodes.SingleTarget( name='y' ), suite=[] ) )


	def testFinallyStmt(self):
		g = Python25Grammar()
		self._matchTest( g.finallyStmt(), 'finally:', Nodes.FinallyStmt( suite=[] ) )


	def testWithStmt(self):
		g = Python25Grammar()
		self._matchTest( g.withStmt(), 'with a:', Nodes.WithStmt( expr=Nodes.Load( name='a' ), target=None, suite=[] ) )
		self._matchTest( g.withStmt(), 'with a as b:', Nodes.WithStmt( expr=Nodes.Load( name='a' ), target=Nodes.SingleTarget( name='b' ), suite=[] ) )


	def testDefStmt(self):
		g = Python25Grammar()
		self._matchTest( g.defStmt(), 'def f():', Nodes.DefStmt( name='f', params=[], suite=[] ) )
		self._matchTest( g.defStmt(), 'def f(x):', Nodes.DefStmt( name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[] ) )


	def testDecoStmt(self):
		g = Python25Grammar()
		self._matchTest( g.decoStmt(), '@f', Nodes.DecoStmt( name='f', args=None ) )
		self._matchTest( g.decoStmt(), '@f(x)', Nodes.DecoStmt( name='f', args=[ Nodes.Load( name='x' ) ] ) )


	def testClassStmt(self):
		g = Python25Grammar()
		self._matchTest( g.classStmt(), 'class Q:', Nodes.ClassStmt( name='Q', bases=None, suite=[] ) )
		self._matchTest( g.classStmt(), 'class Q (x):', Nodes.ClassStmt( name='Q', bases=[ Nodes.Load( name='x' ) ], suite=[] ) )
		self._matchTest( g.classStmt(), 'class Q (x,):', Nodes.ClassStmt( name='Q', bases=[ Nodes.Load( name='x' ) ], basesTrailingSeparator='1', suite=[] ) )
		self._matchTest( g.classStmt(), 'class Q (x,y):', Nodes.ClassStmt( name='Q', bases=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ], suite=[] ) )


	def testCommentStmt(self):
		g = Python25Grammar()
		self._matchTest( g.commentStmt(), '#x', Nodes.CommentStmt( comment='x' ) )
		self._matchTest( g.commentStmt(), '#' + string.printable, Nodes.CommentStmt( comment=string.printable ) )





	def testFnCallStStmt(self):
		g = Python25Grammar()
		self._matchTest( g.expression(), 'x.y()', Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) )
		self._matchTest( g.statement(), 'x.y()', Nodes.ExprStmt( expr=Nodes.Call( target=Nodes.AttributeRef( target=Nodes.Load( name='x' ), name='y' ), args=[] ) ) )




	def testDictInList(self):
		g = Python25Grammar()
		self._matchTest( g.statement(), 'y = [ x, { a : b } ]', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='y' ) ], value=Nodes.ListLiteral( values=[ Nodes.Load( name='x' ), Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=Nodes.Load( name='b' ) ) ] ) ] ) ) )




def parserViewTest():
	#result, pos, dot = targetListOrTargetItem.debugParseString( 'a.b' )
	#result, pos, dot = subscript.debugParseString( 'a.b' )
	#print dot

	#g = Python25Grammar()
	#g.statement().parseStringChars( 'raise' )

	from BritefuryJ.ParserDebugViewer import ParseViewFrame

	g = Python25Grammar()
	result = g.expression().debugParseStringChars( '[i for i in a]' )
	ParseViewFrame( result )
