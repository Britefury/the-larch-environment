##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import string
from copy import copy


"""
This module provides a left-recursive packrat parser state.

The algorithm used here is based on the algorithm that is presented in the paper
	"Packrat Parsers Can Support Left Recursion"
		by
		Allesandro Warth, James R. Douglass, and Todd Millstein
		
While their algorithm handles most indirect left recursion cases, it does not handle
overlapping left recursion, in which left-recursion must be handled for several
rules, at the same position in the stream.

The 'heads' table described in their paper is a mapping from position to 'Head' structure.
This means that in the case where a rule A is found to be left-recursive, should a rule B
also be discovered to be left-recursive, which is invoked by A, then their algorithm will
not behave as expected.

The algorithm here is a reformulated version of the one presented by Warth et al.
"""




class _MemoEntry (object):
	__slots__ = [ 'rule', 'answer', 'pos', 'bEvaluating', 'bLeftRecursionDetected', 'lrApplications', 'growingLRParseCount' ]
	
	def __init__(self, rule, answer, pos):
		super( _MemoEntry, self ).__init__()
		self.rule = rule
		self.answer = answer
		self.pos = pos
		self.bEvaluating = False
		self.bLeftRecursionDetected = False
		self.lrApplications = {}
		self.growingLRParseCount = 0
		
		
class _RuleInvocation (object):
	__slots__ = [ 'rule', 'memoEntry', 'outerInvocation' ]
	
	def __init__(self, rule, memoEntry, outerInvocation):
		self.rule = rule
		self.memoEntry = memoEntry
		self.outerInvocation = outerInvocation
		
		
class _LeftRecursiveApplication (object):
	__slots__ = [ 'memoEntry', 'rule', 'involvedSet', 'evalSet' ]
	
	def __init__(self, memoEntry, rule):
		self.memoEntry = memoEntry
		self.rule = rule
		self.involvedSet = set()
		self.evalSet = set()
		
	
	
	
		
		
		
		
		
	
	
class ParserState2 (object):
	def __init__(self, ignoreChars=string.whitespace):
		super( ParserState2, self ).__init__()
		self.memo = {}
		self.ruleInvocationStack = None
		self.ignoreChars = ignoreChars
		self.debugStack = []
		self.nodes = []
		self.edges = []
		self.callEdges = set()
		
	def chomp(self, input, start, stop):
		for i in xrange( start, stop ):
			if input[i] not in self.ignoreChars:
				return i
		return stop
					
				
	def memoisedMatch(self, rule, input, start, stop):
		memoEntry = self.__recall( rule, input, start, stop )
		
		if memoEntry is None:
			# Create the memo-entry, and memoise
			memoEntry = _MemoEntry( rule, None, start )
			posMemo = self.memo.setdefault( start, {} )
			posMemo[rule] = memoEntry
			
			
			posMemoCopy = posMemo.copy()
			
			# Create a rule invocation record, and push onto the rule invocation stack
			self.ruleInvocationStack = _RuleInvocation( rule, memoEntry, self.ruleInvocationStack )
			memoEntry.bEvaluating = True
			answer, pos = rule.evaluate( self, input, start, stop )
			
			if memoEntry.bLeftRecursionDetected:
				answer, pos = self.__growLeftRecursiveParse( rule, input, start, stop, memoEntry, answer, pos )
				posMemo.clear()
				posMemo.update( posMemoCopy )

			# Pop the rule invocation off the rule invocation stack
			self.ruleInvocationStack = self.ruleInvocationStack.outerInvocation
			memoEntry.bEvaluating = False
			memoEntry.answer, memoEntry.pos = answer, pos
			
			return answer, pos
		else:
			if memoEntry.bEvaluating:
				# Somewhere up in the call stack is the outer application of @rule; this application
				# recursive; specifically left-recursive since we are at the same position in the stream
				if memoEntry.growingLRParseCount > 0:
					# The left-recursive parse is being grown right now
					self.__onLeftRecursionInnerReapplication( rule, input, start, stop, memoEntry )
				else:
					# Left recursion has been detected
					self.__onLeftRecursionDetected( rule, input, start, stop, memoEntry )
			return memoEntry.answer, memoEntry.pos

		
	def __recall(self, rule, input, start, stop):
		posMemo = self.memo.get( start )
		if posMemo is not None:
			memoEntry = posMemo.get( rule )
		else:
			memoEntry = None
		
		if memoEntry is not None  and  len( memoEntry.lrApplications ) > 0:
			# This rule application is involved in a left-recursive application of a rule
			bInEvalSet = False
			for lrApplication in memoEntry.lrApplications.values():
				# Remove from the evaluation set
				if memoEntry in lrApplication.evalSet:
					lrApplication.evalSet.remove( memoEntry )
					bInEvalSet = True

			if bInEvalSet:
				# Create a rule invocation record, and push onto the rule invocation stack
				self.ruleInvocationStack = _RuleInvocation( rule, memoEntry, self.ruleInvocationStack )
				memoEntry.bEvaluating = True
				# Just evaluate it, and fill in the memo entry with the new values
				memoEntry.answer, memoEntry.pos = rule.evaluate( self, input, start, stop )
				# Pop the rule invocation off the rule invocation stack
				self.ruleInvocationStack = self.ruleInvocationStack.outerInvocation
				memoEntry.bEvaluating = False
			

		return memoEntry
	
	
	def __onLeftRecursionDetected(self, rule, input, start, stop, memoEntry):
		# Left recursion has been detected
		memoEntry.bLeftRecursionDetected = True
		
		# Create a left-recursive application record, if one does not already exist
		try:
			lrApplication = memoEntry.lrApplications[rule]
		except KeyError:
			lrApplication = _LeftRecursiveApplication( memoEntry, rule )
			memoEntry.lrApplications[rule] = lrApplication

		# No rule invocation record has been created for this invocation yet, so the current top of
		# the stack points to the outer invocation
		invocation = self.ruleInvocationStack

		# Walk up the stack until @memoEntry is encountered again, adding each entry to the involvedSet set
		# Simply walking until we reach the bottom of the stack is not sufficient; in the case where the outer
		# invocation of @rule is invoked inside another left-recursive rule, B, we will reach the outer invocation of B,
		# not the outer invocation of @rule. Stopping when we reach the same memo-entry will ensure that we
		# stop at the outer invocation of @rule.
		while invocation is not None   and   invocation.memoEntry is not memoEntry:
			lrApplication.involvedSet.add( invocation.memoEntry )
			invocation.memoEntry.lrApplications[rule] = lrApplication
			invocation = invocation.outerInvocation
			
		
	
	def __onLeftRecursionInnerReapplication(self, rule, input, start, stop, memoEntry):
		lrApplication = memoEntry.lrApplications[rule]

		# No rule invocation record has been created for this invocation yet, so the current top of
		# the stack points to the outer invocation
		invocation = self.ruleInvocationStack

		# Walk up the stack until @memoEntry is encountered again, adding each entry to the involvedSet set
		while invocation is not None   and   invocation.memoEntry is not memoEntry:
			lrApplication.involvedSet.add( invocation.memoEntry )
			invocation = invocation.outerInvocation
			
		
	
		
	def __growLeftRecursiveParse(self, rule, input, start, stop, memoEntry, answer, pos):
		memoEntry.growingLRParseCount += 1
		lrApplication = memoEntry.lrApplications[rule]
		
		while True:
			# Put the answer and position into the memo entry for the next attempt
			memoEntry.answer, memoEntry.pos = answer, pos
			# Prepare the evaluation set
			lrApplication.evalSet = copy( lrApplication.involvedSet )
			# Try re-evaluation
			newAnswer, newPos = rule.evaluate( self, input, start, stop )
			# Fail or no additional characters consumed?
			if newAnswer is None   or   newPos <= pos:
				# Further applications will not improve matters
				break
			# This application of @rule improved matters; take the answer and position to use for the next iteration
			answer, pos = newAnswer, newPos
			
			
		# Left recursive application is finished
		memoEntry.growingLRParseCount -= 1
		memoEntry.bLeftRecursionDetected = False
		
		return answer, pos
		
		
if False:
	from Britefury.Parser.Parser import *
	from Britefury.Parser import Parser
	Parser.ParserState = ParserState2
	
	
	import unittest
	
	class TestCase_ParserState4 (ParserTestCase):
		def testLiteral(self):
			self._matchTest( Literal( 'abc' ), 'abc', 'abc' )
			
		def testRightRecursion(self):
			x = Production( 'x' )
			y = Forward()
			y << Production( ( x + y )  |  'y' )
			
			self._matchTest( y, 'xxxy', [ 'x', [ 'x', [ 'x', 'y' ] ] ] )
		
		def testDirectLeftRecursion(self):
			x = Production( 'x' ).debug( 'x' )
			y = Forward()
			y << Production( ( y + x )  |  'y' ).debug( 'y' )
			
			self._matchTest( y, 'yxxx', [[[ 'y', 'x' ], 'x' ], 'x' ] )
		
		
		def testIndirectLeftRecursion(self):
			x = Production( 'x' ).debug( 'x' )
			z = Forward()
			y = Production( ( z + x )  |  'z' ).debug( 'y' )
			z << Production( y  |  'y' ).debug( 'z' )
			
			self._matchTest( z, 'zxxx', [[[ 'z', 'x' ], 'x' ], 'x' ] )
		
				
		
		def testLeftRecursionSimplifiedJavaPrimary(self):
			primary = Forward()
			
			
			expression = Production( Literal( 'i' )  |  Literal( 'j' ) ).debug( 'expression' )
			methodName = Production( Literal( 'm' )  |  Literal( 'n' ) ).debug( 'methodName' )
			interfaceTypeName = Production( Literal( 'I' )  |  Literal( 'J' ) ).debug( 'interfaceTypeName' )
			className = Production( Literal( 'C' )  |  Literal( 'D' ) ).debug( 'className' )
	
			classOrInterfaceType = Production( className | interfaceTypeName ).debug( 'classOrInterfaceType' )
			
			identifier = Production( Literal( 'x' )  |  Literal( 'y' )  |  classOrInterfaceType ).debug( 'identifier' )
			expressionName = Production( identifier ).debug( 'expressionName' )
			
			arrayAccess = Production( ( primary + '[' + expression + ']' )   |   ( expressionName + '[' + expression + ']' ) ).debug( 'arrayAccess' )
			fieldAccess = Production( ( primary + '.' + identifier )   |   ( Literal( 'super' ) + '.' + identifier ) ).debug( 'fieldAccess' )
			methodInvocation = Production( ( primary + '.' + methodName + '()' )   |   ( methodName + '()' ) ).debug( 'methodInvocation' )
			
			classInstanceCreationExpression = Production( ( Literal( 'new' )  +  classOrInterfaceType  +  '()' )  |  ( primary + '.' + 'new' + identifier + '()' ) ).debug( 'classInstanceCreationExpression' )
			
			primaryNoNewArray = Production( classInstanceCreationExpression | methodInvocation | fieldAccess | arrayAccess | 'this' ).debug( 'primaryNoNewArray' )
			
			primary  <<  Production( primaryNoNewArray ).debug( 'primary' )
			
			fieldAccessOrArrayAccess = Production( fieldAccess ^ arrayAccess )
			
					
			self._matchTest( primary, 'this', 'this' )
			self._matchTest( primary, 'this.x', [ 'this', '.', 'x' ] )
			self._matchTest( primary, 'this.x[i]', [ [ 'this', '.', 'x' ], '[', 'i', ']' ] )
			self._matchTest( primary, 'this.x.y', [ [ 'this', '.', 'x' ], '.', 'y' ] )
			self._matchTest( primary, 'this.x.m()', [ [ 'this', '.', 'x' ], '.', 'm', '()' ] )
			self._matchTest( primary, 'this.x.m().n()', [ [ [ 'this', '.', 'x' ], '.', 'm', '()' ], '.', 'n', '()' ] )
			self._matchTest( primary, 'x[i][j].y', [ [ [ 'x', '[', 'i', ']' ], '[', 'j', ']' ], '.', 'y' ] )
			
			
			self._matchTest( primary, 'this', 'this' )
			self._matchTest( methodInvocation, 'this.m()', [ 'this', '.', 'm', '()' ] )
			self._matchTest( methodInvocation, 'this.m().n()', [ [ 'this', '.', 'm', '()' ], '.', 'n', '()' ] )
			self._matchTest( methodInvocation, 'this.x.m()', [ [ 'this', '.', 'x' ], '.', 'm', '()' ] )
			self._matchTest( methodInvocation, 'this.x.y.m()', [ [ [ 'this', '.', 'x' ], '.', 'y' ], '.', 'm', '()' ] )
			self._matchTest( methodInvocation, 'this[i].m()', [ [ 'this', '[', 'i', ']' ], '.', 'm', '()' ] )
			self._matchTest( methodInvocation, 'this[i][j].m()', [ [ [ 'this', '[', 'i', ']' ], '[', 'j', ']' ], '.', 'm', '()' ] )
			self._matchTest( arrayAccess, 'this[i]', [ 'this', '[', 'i', ']' ] )
			self._matchTest( arrayAccess, 'this[i][j]', [ [ 'this', '[', 'i', ']' ], '[', 'j', ']' ] )
			self._matchTest( arrayAccess, 'this.x[i]', [ [ 'this', '.', 'x' ], '[', 'i', ']' ] )
			self._matchTest( arrayAccess, 'this.x.y[i]', [ [ [ 'this', '.', 'x' ], '.', 'y' ], '[', 'i', ']' ] )
			self._matchTest( arrayAccess, 'this.m()[i]', [ [ 'this', '.', 'm', '()' ], '[', 'i', ']' ] )
			self._matchTest( arrayAccess, 'this.m().n()[i]', [ [ [ 'this', '.', 'm', '()' ], '.', 'n', '()' ], '[', 'i', ']' ] )
			self._matchTest( fieldAccess, 'this.x', [ 'this', '.', 'x' ] )
			self._matchTest( fieldAccess, 'this.x.y', [ [ 'this', '.', 'x' ], '.', 'y' ] )
			self._matchTest( fieldAccess, 'this[i].y', [ [ 'this', '[', 'i', ']' ], '.', 'y' ] )
			self._matchTest( fieldAccess, 'this[i][j].y', [ [ [ 'this', '[', 'i', ']' ], '[', 'j', ']' ], '.', 'y' ] )
			self._matchTest( fieldAccessOrArrayAccess, 'this[i]', [ 'this', '[', 'i', ']' ] )
			self._matchTest( fieldAccessOrArrayAccess, 'this[i].x', [ [ 'this', '[', 'i', ']' ], '.', 'x' ] )
			self._matchTest( fieldAccessOrArrayAccess, 'this.x[i]', [ [ 'this', '.', 'x' ], '[', 'i', ']' ] )

		
	if __name__ == '__main__':
		if False:
			x = Production( 'x' ).debug( 'x' )
			z = Forward()
			y = Production( ( z + x )  |  'z' ).debug( 'y' )
			z << Production( y  |  'y' ).debug( 'z' )
			
			ans, pos = z.parseString( 'yxxx' )
			
			print ans.result, pos
	

		if False:
			integer = Word( string.digits ).debug( 'integer' )
			plus = Literal( '+' )
			minus = Literal( '-' )
			star = Literal( '*' )
			slash = Literal( '/' )
			
			addop = plus | minus
			mulop = star | slash
			
			mul = Forward()
			mul  <<  Production( ( mul + mulop + integer )  |  integer ).debug( 'mul' )
			add = Forward()
			add  <<  Production( ( add + addop + mul )  |  mul ).debug( 'add' )
			
			expr = add
			
			parser = expr
			
			ans, pos = parser.parseString( '123' )
			
			
		if True:
		
			primary = Forward()
			
			
			expression = Production( Literal( 'i' )  |  Literal( 'j' ) ).debug( 'expression' )
			methodName = Production( Literal( 'm' )  |  Literal( 'n' ) ).debug( 'methodName' )
			interfaceTypeName = Production( Literal( 'I' )  |  Literal( 'J' ) ).debug( 'interfaceTypeName' )
			className = Production( Literal( 'C' )  |  Literal( 'D' ) ).debug( 'className' )
	
			classOrInterfaceType = Production( className | interfaceTypeName ).debug( 'classOrInterfaceType' )
			
			identifier = Production( Literal( 'x' )  |  Literal( 'y' )  |  classOrInterfaceType ).debug( 'identifier' )
			expressionName = Production( identifier ).debug( 'expressionName' )
			
			arrayAccess = Production( ( primary + '[' + expression + ']' )   |   ( expressionName + '[' + expression + ']' ) ).debug( 'arrayAccess' )
			fieldAccess = Production( ( primary + '.' + identifier )   |   ( Literal( 'super' ) + '.' + identifier ) ).debug( 'fieldAccess' )
			methodInvocation = Production( ( primary + '.' + methodName + '()' )   |   ( methodName + '()' ) ).debug( 'methodInvocation' )
			
			classInstanceCreationExpression = Production( ( Literal( 'new' )  +  classOrInterfaceType  +  '()' )  |  ( primary + '.' + 'new' + identifier + '()' ) ).debug( 'classInstanceCreationExpression' )
			
			primaryNoNewArray = Production( classInstanceCreationExpression | methodInvocation | fieldAccess | arrayAccess | 'this' ).debug( 'primaryNoNewArray' )
			
			primary  <<  Production( primaryNoNewArray ).debug( 'primary' )
			
			fieldAccessOrArrayAccess = Production( fieldAccess | arrayAccess )
			
			parser = fieldAccessOrArrayAccess
			testString = 'this.x[i]'
			
		if bDot:
			ans, pos, dot =  parser.debugParseString( testString )
			print dot
		else:
			ans, pos =  parser.parseString( testString )
			print ansstr( ans ), pos

			
	