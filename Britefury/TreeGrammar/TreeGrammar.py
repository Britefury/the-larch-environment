##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.TreeParser import Production



class _RuleHelper (object):
	def __init__(self, matcherInstance, desc):
		self._grammarInstance = matcherInstance
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
			
			# Place the expression in the forward declaration
			self._desc._setForwardDeclarationExpression( rule, expression )

		return rule



class Rule (object):
	def __init__(self, f):
		self._f = f
		# TODO: The call to str() is to prevent Jython 2.5a3 from complaining about 'intern'ing a unicode, lower down
		self._name = str( f.__name__ )
		self._attrName = intern( '_rule_' + self._name )
		
		
	def _createForwardDeclaration(self):
		return Production( self._name )
	
	def _setForwardDeclarationExpression(self, forward, expression):
		forward.setExpression( expression )
	
	def __get__(self, instance, owner):
		if instance is not None:
			return _RuleHelper( instance, self )
		else:
			return self
	

	

class TreeGrammar (object):
	pass
	
