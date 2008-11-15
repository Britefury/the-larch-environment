##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Parser import Forward, Production



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
			if hasattr( self._grammarInstance, self._desc._bEvaluatingAttrName ):
				# Recursive rule detected; create a @Forward parser expression
				rule = Forward()
				setattr( self._grammarInstance, self._desc._attrName, rule )
			else:
				# Set the attribute in @self._desc._bEvaluatingAttrName so that recursive rule creation can be detected
				setattr( self._grammarInstance, self._desc._bEvaluatingAttrName, True )
				
				# Create the parser expression
				parser = self._desc._f( self._grammarInstance, *args, **kwargs )
				
				# Create a production and set the debug name
				rule = Production( parser ).debug( self._desc._name )
				
				if hasattr( self._grammarInstance, self._desc._attrName ):
					# The attribute @self._desc._attrName has been set, then this rule is recursive, so a @Forward will already have been created
					forward = getattr( self._grammarInstance, self._desc._attrName )
					forward.setExpression( rule )
					rule = forward
				else:
					# Set the rule
					setattr( self._grammarInstance, self._desc._attrName, rule )
				# No longer evaluating
				delattr( self._grammarInstance, self._desc._bEvaluatingAttrName )
		return rule
	

class Rule (object):
	def __init__(self, f):
		self._f = f
		# TODO: The call to str() is to prevent Jython 2.5a3 from complaining about 'intern'ing a unicode, lower down
		self._name = str( f.__name__ )
		self._attrName = intern( '_rule_' + self._name )
		self._bEvaluatingAttrName = intern( '_forward_' + self._name )
		
		
	def __get__(self, instance, owner):
		if instance is not None:
			return _RuleHelper( instance, self )
		else:
			return self
	

	

class Grammar (object):
	pass
	
