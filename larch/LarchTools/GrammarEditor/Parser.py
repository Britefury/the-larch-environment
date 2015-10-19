##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from copy import deepcopy
import re, string

from BritefuryJ.DocModel import DMNode

from BritefuryJ.Parser import Literal, Keyword, RegEx, Word, SeparatedList, ObjectNode
from BritefuryJ.Parser.Utils import Tokens
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, UnaryOperator, BinaryOperator, ChainOperator, OperatorTable

from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase

from Britefury.Grammar.Grammar import Grammar, Rule, RuleList

from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr
from LarchTools.PythonTools.VisualRegex.VisualRegex import VisualPythonRegex

from LarchTools.GrammarEditor import Schema, helpers



def _incrementParens(node):
	p = node['parens']
	numParens = 0
	if p is not None   and   ( isinstance( p, str )  or  isinstance( p, unicode ) ):
		p = str( p )
		try:
			numParens = int( p )
		except ValueError:
			pass
	numParens += 1
	newNode = deepcopy( node )
	newNode['parens'] = str( numParens )
	return  newNode



def _string_literal_to_quotation(x):
	x = x.strip()
	if x[0] == "'":
		return 'single'
	elif x[0] == '"':
		return 'double'
	else:
		raise ValueError('Unknown quotation mark {0}'.format(x[0]))

class GrammarEditorGrammar (Grammar):
	__junk_regex__ = '[ ]*'


	@Rule
	def rule_name(self):
		return RegEx('[A-Za-z_][A-Za-z_0-9]*')


	# Terminals

	@Rule
	def literal(self):
		return Tokens.quotedString.action(lambda input, begin, end, xs, bindings: Schema.Literal(value=xs[1:-1], quotation=_string_literal_to_quotation(xs)))

	@Rule
	def keyword(self):
		return (Literal('k') + Tokens.quotedString).action(lambda input, begin, end, xs, bindings: Schema.Keyword(value=xs[1][1:-1], quotation=_string_literal_to_quotation(xs[1])))

	@Rule
	def word(self):
		return (Literal('w') + Tokens.quotedString).action(lambda input, begin, end, xs, bindings: Schema.Word(value=xs[1][1:-1], quotation=_string_literal_to_quotation(xs[1])))

	@Rule
	def regex(self):
		return ObjectNode( Schema.RegEx ) | Literal('re').action(lambda input, begin, end, xs, bindings: Schema.RegEx(regex=DMNode.embed(helpers.new_empty_regex())))


	@Rule
	def invoke_rule(self):
		return (Literal('<') + self.rule_name() + Literal('>')).action(lambda input, begin, end, xs, bindings: Schema.InvokeRule(name=xs[1]))


	@Rule
	def atom(self):
		return self.literal() | self.keyword() | self.word() | self.regex() | self.invoke_rule() | self.enclosure()


	@Rule
	def _repeats(self):
		return Literal('*').action(lambda input, begin, end, xs, bindings: ('*',)) | \
			Literal('+').action(lambda input, begin, end, xs, bindings: ('+',)) | \
			Literal('?').action(lambda input, begin, end, xs, bindings: ('?',)) | \
		       (Literal('{') + Tokens.decimalInteger + Literal(':') + Tokens.decimalInteger + Literal('}')).action(
			       lambda input, begin, end, xs, bindings: ('a:b', xs[1], xs[3])) | \
		       (Literal('{') + Literal(':') + Tokens.decimalInteger + Literal('}')).action(
			       lambda input, begin, end, xs, bindings: ('a:b', None, xs[2])) | \
		       (Literal('{') + Tokens.decimalInteger + Literal(':') + Literal('}')).action(
			       lambda input, begin, end, xs, bindings: ('a:b', xs[1], None)) | \
		       (Literal('{') + Tokens.decimalInteger + Literal('}')).action(
			       lambda input, begin, end, xs, bindings: ('n', xs[1]))

	@Rule
	def repetition(self):
		def _apply_rep(subexp, rep):
			x = rep[0]
			if x == '*':
				return Schema.ZeroOrMore(subexp=subexp)
			elif x == '+':
				return Schema.OneOrMore(subexp=subexp)
			elif x == '?':
				return Schema.Optional(subexp=subexp)
			elif x == 'n':
				return Schema.Repeat(subexp=subexp, n=rep[1])
			elif x == 'a:b':
				return Schema.RepeatRange(subexp=subexp, a=rep[1], b=rep[2])
			else:
				raise ValueError('Invalid repeat code')
		return (self.atom() + self._repeats()).action(lambda input, begin, end, xs, bindings: _apply_rep(xs[0], xs[1])) | self.atom()

	@Rule
	def peekOrControl(self):
		return (Literal('/!') + self.repetition()).action(lambda input, begin, end, xs, bindings: Schema.PeekNot(subexp=xs[1])) | \
			(Literal('/') + self.repetition()).action(lambda input, begin, end, xs, bindings: Schema.Peek(subexp=xs[1])) | \
			(self.repetition() + Literal('~')).action(lambda input, begin, end, xs, bindings: Schema.Suppress(subexp=xs[0])) | \
			self.repetition()

	@Rule
	def action(self):
		def _action_py(xs):
			a = xs[2]
			if a is None:
				a = Schema.ActionPy(py=DMNode.embed(helpers.new_python_action()))
			return a
		return ObjectNode( Schema.Action ) | \
		       (self.peekOrControl() + Literal('->') + ObjectNode(Schema.ActionPy).optional()).action(
			       lambda input, begin, end, xs, bindings: Schema.Action(subexp=xs[0], action=_action_py(xs))) | self.peekOrControl()

	@Rule
	def sequence(self):
		def _node(xs):
			if len(xs) == 1:
				return xs[0]
			else:
				return Schema.Sequence(subexps=xs)
		return self.action().oneOrMore().action(lambda input, begin, end, xs, bindings: _node(xs))

	@Rule
	def combine(self):
		def _unwind(xs):
			head, tail = xs
			if len(tail) == 0:
				return head
			else:
				se = [head] + [pair[1] for pair in tail]
				return Schema.Combine(subexps=se)
		return (self.sequence() + (Literal('-') + self.sequence()).zeroOrMore()).action(
			lambda input, begin, end, xs, bindings: _unwind(xs))

	@Rule
	def choice(self):
		def _unwind(xs):
			head, tail = xs
			if len(tail) == 0:
				return head
			else:
				se = [head] + [pair[1] for pair in tail]
				return Schema.Choice(subexps=se)
		return (self.combine() + (Literal('|') + self.combine()).zeroOrMore()).action(
				lambda input, begin, end, xs, bindings: _unwind(xs))


	# Parentheses
	@Rule
	def paren_form(self):
		return ( Literal( '(' ) + self.choice() + ')' ).action( lambda input, begin, end, xs, bindings: _incrementParens( xs[1] ) )


	@Rule
	def enclosure(self):
		return self.paren_form()


	@Rule
	def expression(self):
		return self.choice()


	@Rule
	def unparsed(self):
		return ObjectNode( Schema.UNPARSED )  |  ( ( ( RegEx( '[^\n]*' ) | ObjectNode( Schema.Expression ) ).oneOrMore()  +  Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.UnparsedStmt( value=Schema.UNPARSED( value=xs[0] ) ) ) )



	#
	#
	# COMMENT STATEMENT
	#
	#

	# Comment statement
	@Rule
	def comment_stmt(self):
		return ObjectNode( Schema.CommentStmt )  | \
		       ( RegEx( re.escape( '#' ) + '[' + re.escape( string.printable.replace( '\n', '' ) ) + ']*' ) + Literal( '\n' ) ).action( lambda input, begin, end, xs, bindings: Schema.CommentStmt( comment=xs[0][1:] ) )


	@Rule
	def blank_line(self):
		return ObjectNode( Schema.BlankLine )  | \
		       Literal( '\n' ).action( lambda input, begin, end, xs, bindings: Schema.BlankLine() )



	#
	# Statements
	#


	def rule_definition(self):
		return (self.rule_name() + Literal(':=') + self.expression() + Literal('\n')).action(
				lambda input, begin, end, xs, bindings: Schema.RuleDefinitionStmt(name=xs[0], body=xs[2]))


	@Rule
	def single_line_statement_valid(self):
		return ObjectNode( Schema.Statement ) | self.rule_definition() | self.comment_stmt() | self.blank_line()


	@Rule
	def single_line_statement(self):
		return self.single_line_statement_valid() | self.unparsed()


	@Rule
	def suite(self):
		return self.single_line_statement().oneOrMore()




class TestCase_GEParser (ParserTestCase):
	__junk_regex__ = ''

	def test_rule_name(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.rule_name(), 'a', 'a' )


	def test_literal(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.literal(), '"a"', Schema.Literal(value='a', quotation='double') )
		self._parseStringTest( g.literal(), "'a'", Schema.Literal(value='a', quotation='single') )

	def test_keyword(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.keyword(), 'k"a"', Schema.Keyword(value='a', quotation='double') )
		self._parseStringTest( g.keyword(), 'k"a_1"', Schema.Keyword(value='a_1', quotation='double') )
		self._parseStringFailTest(g.keyword(), ':0')

	def test_word(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.word(), 'w"abcdef"', Schema.Word(value='abcdef', quotation='double') )

	def test_regex(self):
		g = GrammarEditorGrammar()
		# self._parseStringTest( g.regex(), 're', Schema.RegEx(regex=DMNode.embed(VisualPythonRegex())) )

	def test_invoke_rule(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.invoke_rule(), '<a>', Schema.InvokeRule(name='a') )

	def test_atom(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.atom(), '"a"', Schema.Literal(value='a', quotation='double'))
		self._parseStringTest( g.atom(), 'k"a_1"', Schema.Keyword(value='a_1', quotation='double') )
		self._parseStringTest( g.atom(), 'w"abcdef"', Schema.Word(value='abcdef', quotation='double') )
		self._parseStringTest( g.atom(), '<a>', Schema.InvokeRule(name='a') )

	def test_repetition(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.repetition(), '<a>+', Schema.OneOrMore(subexp=Schema.InvokeRule(name='a')))
		self._parseStringTest(g.repetition(), '<a>*', Schema.ZeroOrMore(subexp=Schema.InvokeRule(name='a')))
		self._parseStringTest(g.repetition(), '<a>?', Schema.Optional(subexp=Schema.InvokeRule(name='a')))
		self._parseStringTest(g.repetition(), '<a>{2}', Schema.Repeat(subexp=Schema.InvokeRule(name='a'), n='2'))
		self._parseStringTest(g.repetition(), '<a>{2:}', Schema.RepeatRange(subexp=Schema.InvokeRule(name='a'), a='2', b=None))
		self._parseStringTest(g.repetition(), '<a>{:2}', Schema.RepeatRange(subexp=Schema.InvokeRule(name='a'), a=None, b='2'))
		self._parseStringTest(g.repetition(), '<a>{1:2}', Schema.RepeatRange(subexp=Schema.InvokeRule(name='a'), a='1', b='2'))

	def test_action(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.action(), '<a> ->', Schema.Action(subexp=Schema.InvokeRule(name='a'),
									  action=Schema.ActionPy(py=EmbeddedPython2Expr())))


	def test_sequence(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.sequence(), '<a>', Schema.InvokeRule(name='a'))
		self._parseStringTest(g.sequence(), '<a><b>', Schema.Sequence(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.InvokeRule(name='b')]))
		self._parseStringTest(g.sequence(), '<a><b><c>', Schema.Sequence(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.InvokeRule(name='b'),
			Schema.InvokeRule(name='c')]))

	def test_choice(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.choice(), '<a>', Schema.InvokeRule(name='a'))
		self._parseStringTest(g.choice(), '<a>|<b>', Schema.Choice(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.InvokeRule(name='b')]))
		self._parseStringTest(g.choice(), '<a>|<b>|<c>', Schema.Choice(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.InvokeRule(name='b'),
			Schema.InvokeRule(name='c')]))

	def test_paren_form(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.paren_form(), '(<a>)', Schema.InvokeRule(name='a', parens='1'))

	def test_expression(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.expression(), '(<a>)', Schema.InvokeRule(name='a', parens='1'))
		self._parseStringTest(g.expression(), '((<a>))', Schema.InvokeRule(name='a', parens='2'))
		self._parseStringTest(g.expression(), '(<a><b>)', Schema.Sequence(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.InvokeRule(name='b')], parens='1'))
		self._parseStringTest(g.expression(), '((<a><b>))', Schema.Sequence(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.InvokeRule(name='b')], parens='2'))
		self._parseStringTest(g.expression(), '(<a>|<b>+)', Schema.Choice(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.OneOrMore(subexp=Schema.InvokeRule(name='b'))], parens='1'))
		self._parseStringTest(g.expression(), '((<a>|<b>+))', Schema.Choice(subexps=[
			Schema.InvokeRule(name='a'),
			Schema.OneOrMore(subexp=Schema.InvokeRule(name='b'))], parens='2'))



	def test_rule_definition(self):
		g = GrammarEditorGrammar()
		self._parseStringTest(g.single_line_statement(), 'a := <a>\n',
				      Schema.RuleDefinitionStmt(name='a', body=Schema.InvokeRule(name='a')))


	def test_blank_line(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.blank_line(), '\n', Schema.BlankLine() )


	def test_comment(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.comment_stmt(), '#x\n', Schema.CommentStmt( comment='x' ) )
		self._parseStringTest( g.comment_stmt(), '#' + string.printable.replace( '\n', '' ) + '\n', Schema.CommentStmt( comment=string.printable.replace( '\n', '' ) ) )
		self._parseNodeTest( g.comment_stmt(), Schema.CommentStmt( comment=string.printable.replace( '\n', '' ) ), Schema.CommentStmt( comment=string.printable.replace( '\n', '' ) ) )


	def test_unparsed(self):
		g = GrammarEditorGrammar()
		self._parseStringTest( g.single_line_statement(), 'foo bar xyz\n', Schema.UnparsedStmt( value=Schema.UNPARSED( value=[ 'foo bar xyz' ] ) ) )
		self._parseStringTest( g.single_line_statement(), 'as\n', Schema.UnparsedStmt( value=Schema.UNPARSED( value=[ 'as' ] ) ) )
		self._parseStringTest( g.suite(), 'as\n', [ Schema.UnparsedStmt( value=Schema.UNPARSED( value=[ 'as' ] ) ) ] )
