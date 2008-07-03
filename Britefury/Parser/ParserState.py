##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import string
from copy import copy



class _MemoEntry (object):
	__slots__ = [ 'answer', 'pos' ]
	
	def __init__(self, answer, pos):
		super( _MemoEntry, self ).__init__()
		self.answer = answer
		self.pos = pos
		
		

	
class _LR (object):
	__slots__ = [ 'seed', 'rule', 'head', 'next' ]
	
	def __init__(self, rule, next):
		super( _LR, self ).__init__()
		self.seed = None
		self.rule = rule
		self.head = None
		self.next = next
		
		
class _Head (object):
	__slots__ = [ 'rule', 'involvedSet', 'evalSet' ]
	
	def __init__(self, rule):
		super( _Head, self ).__init__()
		self.rule = rule
		self.involvedSet = set()
		self.evalSet = set()
		
		
	
	
	
class ParserState (object):
	def __init__(self, ignoreChars=string.whitespace):
		super( ParserState, self ).__init__()
		self.memo = {}
		self.lrStack = None
		self.pos = 0
		self.heads = {}
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
	def memoisedMatch(self, expression, input, start, stop):
		memoEntry = self.__recall( expression, input, start, stop )
		if memoEntry is None:
			# Create a new _LR, and push it onto the rule invocation stack (implemented as a singly-linked-list of _LR objects)
			lr = _LR( expression, self.lrStack )
			self.lrStack = lr
			
			# Memoise @lr, then evaluate @expression
			memoEntry = _MemoEntry( lr, start )
			self.memo[ ( start, expression ) ] = memoEntry
			
			answer, self.pos = expression.evaluate( self, input, start, stop )
			
			# Pop @lr off the rule invocation stack
			self.lrStack = self.lrStack.next
			
			memoEntry.pos = self.pos
			
			#if lr.head is not None  and  isinstance( memoEntry.answer, _LR ):
			if lr.head is not None:
				lr.seed = answer
				return self.__LRAnswer( expression, input, start, stop, memoEntry ),  self.pos
			else:
				memoEntry.answer = answer
				return answer,  self.pos
		else:
			self.pos = memoEntry.pos
			if isinstance( memoEntry.answer, _LR ):
				self.__setupLR( expression, memoEntry.answer )
				return memoEntry.answer.seed,  self.pos
			else:
				return memoEntry.answer,  self.pos

		
	def __recall(self, expression, input, start, stop):
		memoEntry = self.memo.get(  ( start, expression )  )
		h = self.heads.get( start )
		# If not growing a seed-parse, just return what is in the memo-table
		if h is None:
			return memoEntry
		
		# Do not evaluate any rule that is not evolved in this left-recursion
		if memoEntry is None  and  expression is not h.rule  and  expression not in h.involvedSet:
			return _MemoEntry( None, start )
		if expression in h.evalSet:
			h.evalSet.remove( expression )
			answer, self.pos = expression.evaluate( self, input, start, stop )
			memoEntry.answer = answer
			memoEntry.pos = self.pos
		return memoEntry

	
	def __setupLR(self, expression, lr):
		if lr.head is None:
			lr.head = _Head( expression )
		s = self.lrStack
		while s is not None  and  s.head is not lr.head:
			s.head = lr.head
			lr.head.involvedSet.add( s.rule )
			s = s.next


	def __LRAnswer(self, expression, input, start, stop, memoEntry):
		h = memoEntry.answer.head
		if h.rule is not expression:
			return memoEntry.answer.seed
		else:
			memoEntry.answer = memoEntry.answer.seed
			if memoEntry.answer is None:
				return None
			else:
				return self.__growLR( expression, input, start, stop, memoEntry, h )

			
	def __growLR(self, expression, input, start, stop, memoEntry, h):
		self.heads[start] = h
		while True:
			self.pos = start
			h.evalSet = copy( h.involvedSet )
			answer, self.pos = expression.evaluate( self, input, start, stop )
			if answer is None  or  self.pos <= memoEntry.pos:
				break
			memoEntry.answer = answer
			memoEntry.pos = self.pos
		del self.heads[start]
		self.pos = memoEntry.pos
		return memoEntry.answer

	
