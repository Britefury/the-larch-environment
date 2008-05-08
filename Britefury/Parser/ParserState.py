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
	def __init__(self, answer, pos):
		super( _MemoEntry, self ).__init__()
		self.answer = answer
		self.pos = pos
		
		

	
class _LR (object):
	def __init__(self, seed, rule, head, next):
		super( _LR, self ).__init__()
		self.seed = seed
		self.rule = rule
		self.head = head
		self.next = next
		
		
class _Head (object):
	def __init__(self, rule, involvedSet, evalSet):
		super( _Head, self ).__init__()
		self.rule = rule
		self.involvedSet = involvedSet
		self.evalSet = evalSet
		
		
	def __eq__(self, x):
		return self.rule  is  x.rule   and  self.involvedSet  ==  x.involvedSet   and   self.evalSet  ==  x.evalSet
		
		
	
	
	
class ParserState (object):
	def __init__(self, ignoreChars=string.whitespace):
		super( ParserState, self ).__init__()
		self.memo = {}
		self.lrStack = []
		self.pos = 0
		self.heads = {}
		self.ignoreChars = ignoreChars
		
	def lrStackTop(self):
		if len( self.lrStack ) == 0:
			return None
		else:
			return self.lrStack[-1]
	

	def chomp(self, input, start, stop):
		for i in xrange( start, stop ):
			if input[i] not in self.ignoreChars:
				return i
		return stop
					
				
	# The next five methods; _f_memoisedMatch(), _p_setup_lr(), _p_lr_answer(), _p_grow_lr(), _p_recall() are taken from the paper
	# "Packrat Parsers Can Support Left Recursion" by Allesandro Warth, James R. Douglass, Todd Millstein; Viewpoints Research Institute.
	# To be honest, I don't really understand how they work. I transcribed their pseudo-code into Python, and it just worked.
	# Test grammars in the unit tests seem to work okay, so its fine by me! :D
	def _f_memoisedMatch(self, expression, input, start, stop):
		memoEntry = self._p_recall( expression, input, start, stop )
		if memoEntry is None:
			lr = _LR( None, expression, None, self.lrStackTop() )
			self.lrStack.append( lr )
			
			memoEntry = _MemoEntry( lr, start )
			key = start, expression
			self.memo[key] = memoEntry
			
			answer, self.pos = expression._o_evaluate( self, input, start, stop )
			
			self.lrStack.pop()
			
			memoEntry.pos = self.pos
			
			if lr.head is not None:
				lr.seed = answer
				return self._p_lr_answer( expression, input, start, stop, memoEntry ),  self.pos
			else:
				memoEntry.answer = answer
				return answer,  self.pos
		else:
			self.pos = memoEntry.pos
			if isinstance( memoEntry.answer, _LR ):
				self._p_setup_lr( expression, memoEntry.answer )
				return memoEntry.answer.seed,  self.pos
			else:
				return memoEntry.answer,  self.pos

		
	def _p_setup_lr(self, expression, lr):
		if lr.head is None:
			lr.head = _Head( expression, set(), set() )
		s = self.lrStackTop()
		while s.head != lr.head:
			s.head = lr.head
			lr.head.involvedSet.add( s.rule )
			s = s.next
				
	
	def _p_lr_answer(self, expression, input, start, stop, memoEntry):
		h = memoEntry.answer.head
		if h.rule is not expression:
			return memoEntry.answer.seed
		else:
			memoEntry.answer = memoEntry.answer.seed
			if memoEntry.answer is None:
				return None
			else:
				return self._p_grow_lr( expression, input, start, stop, memoEntry, h )

			
	def _p_grow_lr(self, expression, input, start, stop, memoEntry, h):
		self.heads[start] = h
		while True:
			self.pos = start
			h.evalSet = copy( h.involvedSet )
			answer, self.pos = expression._o_evaluate( self, input, start, stop )
			if answer is None  or  self.pos <= memoEntry.pos:
				break
			memoEntry.answer = answer
			memoEntry.pos = self.pos
		del self.heads[start]
		self.pos = memoEntry.pos
		return memoEntry.answer

	
	def _p_recall(self, expression, input, start, stop):
		key = start, expression
		memoEntry = self.memo.get( key )
		h = self.heads.get( start )
		if h is None:
			return memoEntry
		if memoEntry is None  and  expression not in h.head  and  expression not in h.involvedSet:
			return _MemoEntry( None, start )
		if expression in h.evalSet:
			h.evalSet.remove( expression )
			answer, self.pos = expression._o_evaluate( self, input, start, stop )
			memoEntry.answer = answer
			memoEntry.pos = self.pos
		return memoEntry
