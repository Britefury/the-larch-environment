##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import java.util.List

from BritefuryJ.Parser import Production



class RuleListExpressionNotList (Exception):
	pass

class RuleListIncorrectSize (Exception):
	pass


class _RuleHelper (object):
	def __init__(self, grammarInstance, desc):
		self._grammarInstance = grammarInstance
		self._desc = desc
		
		
	def __call__(self, *args, **kwargs):
		try:
			# If the rule has already been produced, then just return it
			rule = getattr( self._grammarInstance, self._desc._attrName )
		except AttributeError:
			# We need to build the parser expression
			rule = self._desc._createForwardDeclaration()
			setattr( self._grammarInstance, self._desc._attrName, rule )

			# Create the parser expression
			expression = self._desc._f( self._grammarInstance, *args, **kwargs )
			self._desc._verifyExpression( expression )
				
			# Place the expression in the forward declaration
			self._desc._setForwardDeclarationExpression( rule, expression )
				
		return rule



class Rule (object):
	def __init__(self, f):
		self._f = f
		# TODO: The call to str() is to prevent Jython 2.5a3 from complaining about 'intern'ing a unicode, lower down
		self._name = str( f.__name__ )
		self._attrName = intern( '_rule_' + self._name )
		
		
	def __get__(self, instance, owner):
		if instance is not None:
			return _RuleHelper( instance, self )
		else:
			return self
		
		
	def _createForwardDeclaration(self):
		return Production( self._name )
	
	def _verifyExpression(self, expression):
		pass
	
	def _setForwardDeclarationExpression(self, forward, expression):
		forward.setExpression( expression )
	

	

class _RuleList (object):
	def __init__(self, f, names):
		self._f = f
		self._names = names
		# TODO: The call to str() is to prevent Jython 2.5a3 from complaining about 'intern'ing a unicode, lower down
		self._name = str( f.__name__ )
		self._attrName = intern( '_rulelist_' + self._name )
		self._bEvaluatingAttrName = intern( '_forward_' + self._name )
		
		
	def __get__(self, instance, owner):
		if instance is not None:
			return _RuleHelper( instance, self )
		else:
			return self
		
		
	
	def _createForwardDeclaration(self):
		return [ Production( name )   for name in self._names ]
	
	def _verifyExpression(self, expression):
		if not ( isinstance( expression, java.util.List )  or  isinstance( expression, list )  or  isinstance( expression, tuple ) ) :
			raise RuleListExpressionNotList
		if len( expression ) != len( self._names ):
			raise RuleListIncorrectSize
			
	def _setForwardDeclarationExpression(self, forward, expression):
		for f, x in zip( forward, expression ):
			f.setExpression( x )

	

def RuleList(names):
	def _RuleListBuilder(f):
		return _RuleList( f, names )
	return _RuleListBuilder
	
	

class Grammar (object):
	pass
	
