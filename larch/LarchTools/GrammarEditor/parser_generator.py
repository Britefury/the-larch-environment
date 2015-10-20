##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2015.
##-*************************
import imp

from BritefuryJ.Parser import Production, Literal, RegEx, Word, Keyword, Sequence, Combine, Choice

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

class GrammarParserGeneratorWongNumberOfMacroArgumentsError (GrammarParserGeneratorError):
	pass



class _GrammarGeneratorContext (object):
	def __init__(self, module, name_to_rule, name_to_macro):
		self.module = module
		self.name_to_rule = name_to_rule
		self.name_to_macro = name_to_macro


class _GrammarParserExpressionGenerator (object):
	__dispatch_num_args__ = 0

	# Callable - use document model model method dispatch mechanism
	def __call__(self, x):
		return methodDispatch( self, x )



	def _get_rule_by_name(self, name):
		raise NotImplementedError, 'abstract for {0}'.format(type(self))

	def _get_macro_by_name(self, name):
		raise NotImplementedError, 'abstract for {0}'.format(type(self))

	def _get_globals(self):
		raise NotImplementedError, 'abstract for {0}'.format(type(self))



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
			return self._get_rule_by_name(name)
		except KeyError:
			raise GrammarParserGeneratorRuleNameError('No rule named \'{0}\''.format(name))


	@DMObjectNodeDispatchMethod(Schema.InvokeMacro)
	def InvokeMacro(self, model, macro_name, param_exprs):
		try:
			macro = self._get_macro_by_name(macro_name)
		except KeyError:
			raise GrammarParserGeneratorRuleNameError('No macro named \'{0}\''.format(macro_name))
		params = [self(p) for p in param_exprs]
		return macro(*params)


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
			raise GrammarParserGeneratorInvalidNumberError('Invalid minimum number of repetitions \'{0}\''.format(a))
		try:
			max_r = int(b)
		except ValueError:
			raise GrammarParserGeneratorInvalidNumberError('Invalid maximum number of repetitions \'{0}\''.format(b))
		return self(subexp).repeat(min_r, max_r)


	# Look ahead
	@DMObjectNodeDispatchMethod( Schema.Peek )
	def Peek(self, model, subexp):
		return self(subexp).peek()

	@DMObjectNodeDispatchMethod( Schema.PeekNot )
	def PeekNot(self, model, subexp):
		return self(subexp).peekNot()


	# Control
	@DMObjectNodeDispatchMethod( Schema.Suppress )
	def Suppress(self, model, subexp):
		return self(subexp).suppress()


	# Action
	@DMObjectNodeDispatchMethod( Schema.Action )
	def Action(self, model, subexp, action):
		return self(subexp).action(self(action))

	@DMObjectNodeDispatchMethod( Schema.ActionPy )
	def ActionPy(self, model, py):
		return py.evaluate(self._get_globals(), None)


	# Combinators
	@DMObjectNodeDispatchMethod( Schema.Sequence )
	def Sequence(self, model, subexps):
		return Sequence([self(s) for s in subexps])

	@DMObjectNodeDispatchMethod( Schema.Combine )
	def Combine(self, model, subexps):
		return Combine([self(s) for s in subexps])

	@DMObjectNodeDispatchMethod( Schema.Choice )
	def Choice(self, model, subexps):
		return Choice([self(s) for s in subexps])



class _GrammarParserGeneratorFirstPass (object):
	"""
	The first pass:
	- builds the name -> Production table
	- executes all blocks of helper code
	"""
	__dispatch_num_args__ = 0

	def __init__(self, context):
		self.context = context

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
		self.context.name_to_rule[name] = Production(name)

	@DMObjectNodeDispatchMethod(Schema.MacroDefinitionStmt)
	def MacroDefinitionStmt(self, model, name, args, body):
		macro = _Macro(self.context, name, args, body)
		self.context.name_to_macro[name] = macro

	# Execute helper block
	@DMObjectNodeDispatchMethod( Schema.HelperBlockPy )
	def HelperBlockPy(self, model, py):
		py.executeWithinModule(self.context.module)

	# Grammar definition
	@DMObjectNodeDispatchMethod( Schema.GrammarDefinition )
	def GrammarDefinition(self, model, rules):
		for stmt in rules:
			self(stmt)


class GrammarMacroInvocation (_GrammarParserExpressionGenerator):
	def __init__(self, macro, name_to_param):
		self.macro = macro
		self.name_to_param = name_to_param

	def _get_rule_by_name(self, name):
		try:
			return self.name_to_param[name]
		except KeyError:
			return self.macro.context.name_to_rule[name]

	def _get_macro_by_name(self, name):
		return self.macro.context.name_to_macro[name]

	def _get_globals(self):
		return self.macro.context.module.__dict__



class _Macro (object):
	def __init__(self, context, name, args, body):
		self.context = context
		self.name = name
		self.args = args
		self.body = body

	def __call__(self, *params):
		if len(params) != len(self.args):
			raise GrammarParserGeneratorWongNumberOfMacroArgumentsError(
				'Macro {0} accepts {1} arguments, given {2}'.format(self.name, len(self.args), len(params)))
		name_to_param = {arg: param for arg, param in zip(self.args, params)}
		# print 'Macro invocation: {0}({1})'.format(self.macro.name, self.name_to_param)
		return GrammarMacroInvocation(self, name_to_param)(self.body)



class GrammarParserGenerator (_GrammarParserExpressionGenerator):
	def __init__(self, context):
		self.context = context
		self.__first_pass = _GrammarParserGeneratorFirstPass(context)


	def _get_rule_by_name(self, name):
		return self.context.name_to_rule[name]

	def _get_macro_by_name(self, name):
		return self.context.name_to_macro[name]

	def _get_globals(self):
		return self.context.module.__dict__


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
		prod = self.__first_pass.context.name_to_rule[name]
		prod.setExpression(self(body))

	@DMObjectNodeDispatchMethod(Schema.MacroDefinitionStmt)
	def MacroDefinitionStmt(self, model, name, args, body):
		pass

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
		context = _GrammarGeneratorContext(module, {}, {})
		self.context = context
		self.parser_gen = GrammarParserGenerator(context)

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
		test_table.run_tests(self.context.module, self.context.name_to_rule)

	# Grammar definition
	@DMObjectNodeDispatchMethod( Schema.GrammarDefinition )
	def GrammarDefinition(self, model, rules):
		# Generate parser
		self.parser_gen(model)
		for stmt in rules:
			self(stmt)
