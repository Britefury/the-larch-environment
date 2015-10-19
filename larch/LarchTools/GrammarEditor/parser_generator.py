##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2015.
##-*************************
import imp

from BritefuryJ.Parser import Production, Literal, RegEx, Word, Keyword, Sequence, Choice

from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchTools.GrammarEditor import Schema



class GrammarParserGeneratorError (Exception):
	pass

class GrammarParserGeneratorUnparsedError (GrammarParserGeneratorError):
	pass

class GrammarParserGeneratorRuleNameError (GrammarParserGeneratorError):
	pass

class GrammarParserGeneratorInvalidNumberError (GrammarParserGeneratorError):
	pass



class _GrammarParserGeneratorFirstPass (object):
	"""
	The first pass:
	- builds the name -> Production table
	- executes all blocks of helper code
	"""
	__dispatch_num_args__ = 0

	def __init__(self, module):
		self.name_to_production = {}
		self.module = module

	# Callable - use document model model method dispatch mechanism
	def __call__(self, x):
		return methodDispatch( self, x )

	# Base class
	@DMObjectNodeDispatchMethod( Schema.Node )
	def Node(self, model):
		return None

	# Define rule - map name to production
	@DMObjectNodeDispatchMethod( Schema.RuleDefinitionStmt )
	def RuleDefinitionStmt(self, model, name, body):
		self.name_to_production[name] = Production(name)

	# Execute helper block
	@DMObjectNodeDispatchMethod( Schema.HelperBlockPy )
	def HelperBlockPy(self, model, py):
		py.executeWithinModule(self.module)

	# Grammar definition
	@DMObjectNodeDispatchMethod( Schema.GrammarDefinition )
	def GrammarDefinition(self, model, rules):
		for stmt in rules:
			self(stmt)



class GrammarParserGenerator (object):
	__dispatch_num_args__ = 0

	def __init__(self, module):
		self.module = module
		self.__first_pass = _GrammarParserGeneratorFirstPass(module)
		self.name_to_rule = self.__first_pass.name_to_production

	# Callable - use document model model method dispatch mechanism
	def __call__(self, x):
		return methodDispatch( self, x )



	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, model):
		raise GrammarParserGeneratorUnparsedError


	# Terminals
	@DMObjectNodeDispatchMethod( Schema.Literal )
	def Literal(self, model, value):
		return Literal(value)

	@DMObjectNodeDispatchMethod( Schema.Keyword )
	def Keyword(self, model, value):
		return Keyword(value)

	@DMObjectNodeDispatchMethod( Schema.Word )
	def Word(self, model, value):
		return Word(value)

	@DMObjectNodeDispatchMethod( Schema.RegEx )
	def RegEx(self, model, regex):
		return RegEx(regex.asString())


	@DMObjectNodeDispatchMethod(Schema.InvokeRule)
	def InvokeRule(self, model, name):
		try:
			return self.__first_pass.name_to_production[name]
		except KeyError:
			raise GrammarParserGeneratorRuleNameError('No rule named \'{0}\''.format(name))


	@DMObjectNodeDispatchMethod( Schema.Optional )
	def Optional(self, model, subexp):
		return self(subexp).optional()

	@DMObjectNodeDispatchMethod( Schema.ZeroOrMore )
	def ZeroOrMore(self, model, subexp):
		return self(subexp).zeroOrMore()

	@DMObjectNodeDispatchMethod( Schema.OneOrMore )
	def OneOrMore(self, model, subexp):
		return self(subexp).oneOrMore()

	@DMObjectNodeDispatchMethod( Schema.Repeat )
	def Repeat(self, model, subexp, n):
		try:
			r = int(n)
		except ValueError:
			raise GrammarParserGeneratorInvalidNumberError('Invalid number of repetitions \'{0}\''.format(n))
		return self(subexp).repeat(r, r)

	@DMObjectNodeDispatchMethod( Schema.RepeatRange )
	def RepeatRange(self, model, subexp, a, b):
		try:
			min_r = int(a)
		except ValueError:
			raise GrammarParserGeneratorInvalidNumberError('Invalid minimum number of repetitions \'{0}\''.format(n))
		try:
			max_r = int(b)
		except ValueError:
			raise GrammarParserGeneratorInvalidNumberError('Invalid maximum number of repetitions \'{0}\''.format(n))
		return self(subexp).repeat(min_r, max_r)


	# Action
	@DMObjectNodeDispatchMethod( Schema.Action )
	def Action(self, model, subexp, action):
		return self(subexp).action(self(action))

	@DMObjectNodeDispatchMethod( Schema.ActionPy )
	def ActionPy(self, model, py):
		return py.evalute(self.__first_pass.module.__dict__, None)


	# Combinators
	@DMObjectNodeDispatchMethod( Schema.Sequence )
	def Sequence(self, model, subexps):
		return Sequence([self(s) for s in subexps])

	@DMObjectNodeDispatchMethod( Schema.Choice )
	def Choice(self, model, subexps):
		return Choice([self(s) for s in subexps])


	# Statements
	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	def BlankLine(self, model):
		return None

	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	def CommentStmt(self, model):
		return None

	@DMObjectNodeDispatchMethod( Schema.UnparsedStmt )
	def UnparsedStmt(self, model):
		raise GrammarParserGeneratorUnparsedError


	# Define rule
	@DMObjectNodeDispatchMethod( Schema.RuleDefinitionStmt )
	def RuleDefinitionStmt(self, model, name, body):
		prod = self.__first_pass.name_to_production[name]
		prod.setExpression(self(body))

	# Ignore helper block
	@DMObjectNodeDispatchMethod( Schema.HelperBlockPy )
	def HelperBlockPy(self, model, py):
		pass

	# Ignore unit tests
	@DMObjectNodeDispatchMethod( Schema.UnitTestTable )
	def UnitTestTable(self, model, test_table):
		pass

	# Grammar definition
	@DMObjectNodeDispatchMethod( Schema.GrammarDefinition )
	def GrammarDefinition(self, model, rules):
		# Perform first-pass
		self.__first_pass(model)
		for stmt in rules:
			self(stmt)



class GrammarTestRunner (object):
	__dispatch_num_args__ = 0

	def __init__(self, module_name='__grammar_tests__'):
		module = imp.new_module(module_name)
		self.parser_gen = GrammarParserGenerator(module)

	# Callable - use document model model method dispatch mechanism
	def __call__(self, x):
		return methodDispatch( self, x )


	# Base class
	@DMObjectNodeDispatchMethod( Schema.Node )
	def Node(self, model):
		pass


	# Ignore unit tests
	@DMObjectNodeDispatchMethod( Schema.UnitTestTable )
	def UnitTestTable(self, model, test_table):
		test_table.run_tests(self.parser_gen.module, self.parser_gen.name_to_rule)

	# Grammar definition
	@DMObjectNodeDispatchMethod( Schema.GrammarDefinition )
	def GrammarDefinition(self, model, rules):
		# Generate parser
		self.parser_gen(model)
		for stmt in rules:
			self(stmt)
