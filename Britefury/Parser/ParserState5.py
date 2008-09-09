##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import string
from copy import copy


def ansstr(x):
	if x is None:
		return '<FAIL>'
	else:
		return x.result
	
	
bDebugStmts = False
bDot = True


class Logger (object):
	def __init__(self):
		self.level = 0
		
		
	def _ind(self):
		return '\t' * self.level
		
	
		
	def __rshift__(self, message):
		if bDebugStmts:
			print self._ind() + '>' + message
		self.level += 1

	def __lshift__(self, message):
		self.level -= 1
		if bDebugStmts:
			print self._ind() + '<' + message
		
	def __le__(self, message):
		if bDebugStmts:
			print self._ind() + '' + message
		
log = Logger()


		
		
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
	def __init__(self, rule, memoEntry, outerInvocation):
		self.rule = rule
		self.memoEntry = memoEntry
		self.outerInvocation = outerInvocation
		
		
class _LeftRecursiveApplication (object):
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
					
				
	# The next five methods; memoisedMatch(), __recall(), __setupLR(), __LRAnswer(), __growLR() are taken from the paper
	# "Packrat Parsers Can Support Left Recursion" by Allesandro Warth, James R. Douglass, Todd Millstein; Viewpoints Research Institute.
	# To be honest, I don't really understand how they work. I transcribed their pseudo-code into Python, and it just worked.
	# Test grammars in the unit tests seem to work okay, so its fine by me! :D
	def memoisedMatch(self, rule, input, start, stop):
		log >>  'match %s @ %d'  %  ( rule.debugName, start )
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
			
			log  <<   'match %s @ %d  ->  %s : %d'  %  ( rule.debugName, start, ansstr( answer ), pos )
			return answer, pos
		else:
			if memoEntry.bEvaluating:
				# Somewhere up in the call stack is the outer application of @rule; this application
				# recursive; specifically left-recursive since we are at the same position in the stream
				if memoEntry.growingLRParseCount > 0:
					self.__onLeftRecursionInnerReapplication( rule, input, start, stop, memoEntry )
				else:
					self.__onLeftRecursionDetected( rule, input, start, stop, memoEntry )
			log  <<  'match %s @ %d  (memo)  ->  %s : %d'  %  ( rule.debugName, start, ansstr( memoEntry.answer ), memoEntry.pos )
			return memoEntry.answer, memoEntry.pos

		
	def __recall(self, rule, input, start, stop):
		posMemo = self.memo.get( start )
		if posMemo is not None:
			memoEntry = posMemo.get( rule )
		else:
			memoEntry = None
		log >>  '__recall %s @ %d'  %  ( rule.debugName, start )
		
#		if memoEntry is not None  and  memoEntry.lrApplication is not None  and  memoEntry is not memoEntry.lrApplication.memoEntry:
		if memoEntry is not None  and  len( memoEntry.lrApplications ) > 0:
			log <=  '__recall %s @ %d; innerLR, lr-application for %s'  %  ( rule.debugName, start, [ app.rule.debugName   for app in memoEntry.lrApplications.values() ] )
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
			else:
				log <=  ( '__recall %s @ %d; rule; not in eval set'  %  ( rule.debugName, start ) )
		else:
			if memoEntry is None:
				log <=  ( '__recall %s @ %d; rule; no entry'  %  ( rule.debugName, start ) )
			elif len( memoEntry.lrApplications ) == 0:
				log <=  ( '__recall %s @ %d; rule; no lr-application'  %  ( rule.debugName, start ) )
			

		log <<  '__recall %s @ %d'  %  ( rule.debugName, start )
		return memoEntry
	
	
	def __onLeftRecursionDetected(self, rule, input, start, stop, memoEntry):
		log >>  '__onLeftRecursionDetected %s @ %d'  %  ( rule.debugName, start )
		# Left recursion has been detected
		memoEntry.bLeftRecursionDetected = True
		
		# Create a left-recursive application record
		try:
			lrApplication = memoEntry.lrApplications[rule]
		except KeyError:
			lrApplication = _LeftRecursiveApplication( memoEntry, rule )
			memoEntry.lrApplications[rule] = lrApplication

		# No rule invocation record has been created for this invocation yet, so the current top of
		# the stack points to the outer invocation
		invocation = self.ruleInvocationStack

		# Walk up the stack until @memoEntry is encountered again, adding each entry to the involvedSet set
		while invocation is not None   and   invocation.memoEntry is not memoEntry:
			lrApplication.involvedSet.add( invocation.memoEntry )
			invocation.memoEntry.lrApplications[rule] = lrApplication
			invocation = invocation.outerInvocation
			
		involved = [ m.rule.debugName   for m in lrApplication.involvedSet ]
		log <<  '__onLeftRecursionDetected %s @ %d  ::  involved=%s'  %  ( rule.debugName, start, involved )
			
		
	
	def __onLeftRecursionInnerReapplication(self, rule, input, start, stop, memoEntry):
		log >>  '__onLeftRecursionInnerReapplication %s @ %d'  %  ( rule.debugName, start )
		lrApplication = memoEntry.lrApplications[rule]
		#lrApplication.involvedSet = set()

		# No rule invocation record has been created for this invocation yet, so the current top of
		# the stack points to the outer invocation
		invocation = self.ruleInvocationStack

		# Walk up the stack until @memoEntry is encountered again, adding each entry to the involvedSet set
		while invocation is not None   and   invocation.memoEntry is not memoEntry:
			lrApplication.involvedSet.add( invocation.memoEntry )
			invocation = invocation.outerInvocation
			
		involved = [ m.rule.debugName   for m in lrApplication.involvedSet ]
		log <<  '__onLeftRecursionInnerReapplication %s @ %d  ::  involved=%s'  %  ( rule.debugName, start, involved )
			
		
	
		
	def __growLeftRecursiveParse(self, rule, input, start, stop, memoEntry, answer, pos):
		log <= '__growLeftRecursiveParse begin %s @ %d' % ( rule.debugName, start )
		memoEntry.growingLRParseCount += 1
		lrApplication = memoEntry.lrApplications[rule]
		
		while True:
			involved = [ m.rule.debugName   for m in lrApplication.involvedSet ]
			log <= '__growLeftRecursiveParse %s @ %d iter: involved= %s'  %  ( rule.debugName, start, involved )
			# Put the answer and position into the memo entry for the next attempt
			memoEntry.answer, memoEntry.pos = answer, pos
			# Prepare the evaluation set
			lrApplication.evalSet = copy( lrApplication.involvedSet )
			# Try re-evaluation
			newAnswer, newPos = rule.evaluate( self, input, start, stop )
			# Fail or no additional characters consumed?
			if newAnswer is None   or   newPos <= pos:
				if newAnswer is None:
					log <= '__growLeftRecursiveParse terminating due to fail %s @ %d' % ( rule.debugName, start )
				if newPos <= pos:
					log <= '__growLeftRecursiveParse terminating due to no additional consumption %s @ %d' % ( rule.debugName, start )
				# Further applications will not improve matters
				break
			# This application of @rule improved matters; take the answer and position to use for the next iteration
			answer, pos = newAnswer, newPos
			
			
		# Clear any fields associated with LR
		memoEntry.growingLRParseCount -= 1
		memoEntry.bLeftRecursionDetected = False
		#for m in lrApplication.involvedSet:
		#	del m.lrApplications[rule]
		
		log <= '__growLeftRecursiveParse end %s @ %d' % ( rule.debugName, start )

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
		
				
		
		def testLeftRecursionJavaPrimary(self):
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

			
	