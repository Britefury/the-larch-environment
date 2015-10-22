##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import math, re

from java.lang import Throwable
from java.util import List
from java.awt import Color
from java.awt.event import KeyEvent

from BritefuryJ.Parser import ParserExpression

from Britefury.Kernel.View.DispatchView import MethodDispatchView
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod

from BritefuryJ.DocModel import DMObject

from BritefuryJ.Graphics import SolidBorder, FilledBorder, FillPainter
from BritefuryJ.LSpace.Interactor import KeyElementInteractor
from BritefuryJ.LSpace.Input import Modifier

from BritefuryJ.Shortcut import Shortcut

from BritefuryJ.Live import LiveValue

from BritefuryJ.AttributeTable import AttributeNamespace, InheritedAttributeNonNull, PyDerivedValueTable

from BritefuryJ.Pres import ApplyPerspective, ApplyStyleSheetFromAttribute, Pres
from BritefuryJ.Pres.Primitive import Primitive, Box, Text, Label, Blank, Spacer, HiddenText, Segment, Script, Span, Row, Column, Paragraph, FlowGrid, Whitespace, Border
from BritefuryJ.Pres.UI import SectionHeading3

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.DefaultPerspective import DefaultPerspective
from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.Projection import Perspective
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.EditFilter import HandleEditResult
from BritefuryJ.Editor.Sequential.Item import StructuralItem, SoftStructuralItem
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment
from BritefuryJ.Editor.SyntaxRecognizing.SyntaxRecognizingController import EditMode

from BritefuryJ.Util.Jython import JythonException

from LarchTools.PythonTools.VisualRegex.View import _repeatBorder, _controlCharStyle

from LarchTools.GrammarEditor import Schema, Precedence, helpers, Properties, Commands, parser_generator
from LarchTools.GrammarEditor.Parser import GrammarEditorGrammar
from LarchTools.GrammarEditor.SRController import GrammarEditorSyntaxRecognizingController



class GrammarEditorStyle (object):
	grammarEditor = AttributeNamespace( 'grammarEditor' )

	_grammarCodeFont = 'Noto Sans; SansSerif'

	keywordStyle = InheritedAttributeNonNull( grammarEditor, 'keywordStyle', StyleSheet,
											  StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.fontBold( True ),
																Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ) ) )

	commentStyle = InheritedAttributeNonNull( grammarEditor, 'commentStyle', StyleSheet,
						  StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ) ) )

	quotationStyle = InheritedAttributeNonNull( grammarEditor, 'quotationStyle', StyleSheet,
												StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) ) )
	stringLiteralStyle = InheritedAttributeNonNull( grammarEditor, 'stringLiteralStyle', StyleSheet,
													StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ) ) )
	stringLiteralEscapeStyle = InheritedAttributeNonNull( grammarEditor, 'stringLiteralEscapeStyle', StyleSheet,
														  StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.25, 0.2, 0.15 ) ),
																			Primitive.border( SolidBorder( 1.0, 1.0, 4.0, 4.0, Color( 0.75, 0.6, 0.5 ), Color( 1.0, 0.85, 0.75 ) ) ) ) )
	ruleNameStyle = InheritedAttributeNonNull( grammarEditor, 'ruleNameStyle', StyleSheet,
												 StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.5, 0.5 ) ) ) )
	macroNameStyle = InheritedAttributeNonNull( grammarEditor, 'macroNameStyle', StyleSheet,
												 StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ) )

	punctuationStyle = InheritedAttributeNonNull( grammarEditor, 'punctuationStyle', StyleSheet,
												  StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.0, 1.0 ) ) ) )
	delimStyle = InheritedAttributeNonNull( grammarEditor, 'delimStyle', StyleSheet,
											StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.0, 1.0 ) ) ) )
	operatorStyle = InheritedAttributeNonNull( grammarEditor, 'operatorStyle', StyleSheet,
											   StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontBold( True ), Primitive.fontSize( 14 ),
																 Primitive.foreground( Color( 0.35, 0.35, 0.9 ) ) ) )

	unparseableStyle = InheritedAttributeNonNull( grammarEditor, 'unparseableStyle', StyleSheet,
												  StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ),
																	Primitive.foreground( Color.black ), Primitive.textSquiggleUnderlinePaint( Color.red ) ) )

	py_tagline_style = InheritedAttributeNonNull( grammarEditor, 'py_tagline_style', StyleSheet,
												  StyleSheet.style(Primitive.fontSize(10), Primitive.foreground(Color(0.0, 0.5, 0.0))))

_pyActionBorder = SolidBorder( 1.5, 4.0, 10.0, 10.0, Color( 0.2, 0.75, 0.0 ), None )



_non_escaped_string_re = re.compile( r'(\\(?:[abnfrt\\' + '\'\"' + r']|(?:x[0-9a-fA-F]{2})|(?:u[0-9a-fA-F]{4})|(?:U[0-9a-fA-F]{8})))' )

def string_literal(quotation, value, raw):
	boxContents = []

	# Split the value into pieces of escaped and non-escaped content
	if raw:
		valuePres = ApplyStyleSheetFromAttribute( GrammarEditorStyle.stringLiteralStyle, Text( value ) )
	else:
		segments = _non_escaped_string_re.split( value )
		if len( segments ) == 1:
			valuePres = ApplyStyleSheetFromAttribute( GrammarEditorStyle.stringLiteralStyle, Text( value ) )
		else:
			escape = False
			segsAsPres = []
			for seg in segments:
				if seg is not None  and  len( seg ) > 0:
					if escape:
						segsAsPres.append( ApplyStyleSheetFromAttribute( GrammarEditorStyle.stringLiteralEscapeStyle, Border( Text( seg ) ) ) )
					else:
						segsAsPres.append( Text( seg ) )
				escape = not escape
			valuePres = ApplyStyleSheetFromAttribute( GrammarEditorStyle.stringLiteralStyle, Span( segsAsPres ) )

	quotationPres = ApplyStyleSheetFromAttribute( GrammarEditorStyle.quotationStyle, Text( quotation ) )
	boxContents.extend( [ quotationPres,  valuePres,  quotationPres ] )

	return Row( boxContents )


def literal(string_view):
	return string_view


def keyword(string_view):
	prefix = ApplyStyleSheetFromAttribute( GrammarEditorStyle.keywordStyle, Text( 'k' ) )
	return Span([prefix, string_view])


def word(string_view):
	prefix = ApplyStyleSheetFromAttribute( GrammarEditorStyle.keywordStyle, Text( 'w' ) )
	return Span([prefix, string_view])


def invoke_rule(name):
	left = ApplyStyleSheetFromAttribute( GrammarEditorStyle.delimStyle, Text( '<' ) )
	right = ApplyStyleSheetFromAttribute( GrammarEditorStyle.delimStyle, Text( '>' ) )
	n = ApplyStyleSheetFromAttribute( GrammarEditorStyle.ruleNameStyle, Text( name ) )
	return Span([left, n, right])

def invoke_macro(macro_name, param_views):
	open = ApplyStyleSheetFromAttribute( GrammarEditorStyle.delimStyle, Text( '(' ) )
	close = ApplyStyleSheetFromAttribute( GrammarEditorStyle.delimStyle, Text( ')' ) )
	comma = ApplyStyleSheetFromAttribute( GrammarEditorStyle.punctuationStyle, Text( ', ' ) )
	name = ApplyStyleSheetFromAttribute( GrammarEditorStyle.macroNameStyle, Text( macro_name ) )
	params = []
	if len(param_views) > 0:
		params.append(param_views[0])
		for p in param_views[1:]:
			params.append(comma)
			params.append(p)
	return Span([name, open] + params + [close])


def _repetition(subexp, repetitions):
	return Script.scriptRSuper( Row( [ subexp ] ), _repeatBorder.surround( repetitions ) )

def repeat(subexp, repetitions):
	return _repetition( subexp, Row( [ _controlCharStyle( Text( '{' ) ), Text( repetitions ), _controlCharStyle( Text( '}' ) ) ] ) )

def zero_or_more(subexp):
	return _repetition( subexp, Text( '*' ) )

def one_or_more(subexp):
	return _repetition( subexp, Text( '+' ) )

def optional(subexp):
	return _repetition( subexp, Text( '?' ) )

def repeat_range(subexp, min, max):
	return _repetition( subexp, Row( [ _controlCharStyle( Text( '{' ) ), Text( min ),  _controlCharStyle( Text( ':' ) ), Text( max ), _controlCharStyle( Text( '}' ) ) ] ) )

def peek(subexp):
	return Span([ApplyStyleSheetFromAttribute( GrammarEditorStyle.punctuationStyle, Text('/')), subexp])

def peek_not(subexp):
	return Span([ApplyStyleSheetFromAttribute( GrammarEditorStyle.punctuationStyle, Text('/!')), subexp])

def suppress(subexp):
	return Span([subexp, ApplyStyleSheetFromAttribute( GrammarEditorStyle.punctuationStyle, Text('~'))])

def action_pres(subexp, action):
	arrow =ApplyStyleSheetFromAttribute( GrammarEditorStyle.operatorStyle, Text(' -> '))
	return Row([subexp, arrow, action])

def condition_pres(subexp, action):
	arrow =ApplyStyleSheetFromAttribute( GrammarEditorStyle.operatorStyle, Text(' & '))
	return Row([subexp, arrow, action])

def sequence(subexps):
	items = []
	if len(subexps) > 0:
		items.append(subexps[0])
		for x in subexps[1:]:
			items.append(Text(' '))
			items.append(x)
	return Span(items)

def combine(subexps):
	items = []
	if len(subexps) > 0:
		items.append(subexps[0])
		for x in subexps[1:]:
			items.append(ApplyStyleSheetFromAttribute( GrammarEditorStyle.operatorStyle, Text(' - ')))
			items.append(x)
	return Span(items)

def choice(subexps):
	items = []
	if len(subexps) > 0:
		items.append(Paragraph([subexps[0]]))
		for x in subexps[1:]:
			p = [ApplyStyleSheetFromAttribute( GrammarEditorStyle.operatorStyle, Text('| ')), x]
			items.append(Paragraph(p))
	return Column(0, items)



def blank_line():
	return Text( '' )

def comment_stmt(comment):
	return ApplyStyleSheetFromAttribute( GrammarEditorStyle.commentStyle, Text( '#' + comment ) )

def statement_line(rule, model):
	rule = Segment( rule )
	newLine = Whitespace( '\n' )
	p = Paragraph( [ rule, newLine ] ).alignHPack()
	if model is not None:
		p = p.withProperty( Properties.StatementProperty.instance, model )
	return p

def special_form_statement_line(v, model):
	v = StructuralItem( _controller, model, v )
	v = Segment( v )
	v = Paragraph( [ v ] ).alignHPack()
	v = v.withProperty( Properties.StatementProperty.instance, model )
	return v


def rule_def(name, body_view):
	n = ApplyStyleSheetFromAttribute( GrammarEditorStyle.ruleNameStyle, Text( name ) )
	assign = ApplyStyleSheetFromAttribute( GrammarEditorStyle.operatorStyle, Text( ' := ' ) )
	return Row([n, assign, Paragraph([body_view])])

def macro_def(name, args, body_view):
	def_keyword = ApplyStyleSheetFromAttribute(GrammarEditorStyle.keywordStyle, Text('def '))
	macro_name = ApplyStyleSheetFromAttribute( GrammarEditorStyle.macroNameStyle, Text( name ) )
	open = ApplyStyleSheetFromAttribute( GrammarEditorStyle.delimStyle, Text( '(' ) )
	close = ApplyStyleSheetFromAttribute( GrammarEditorStyle.delimStyle, Text( ')' ) )
	comma = ApplyStyleSheetFromAttribute( GrammarEditorStyle.punctuationStyle, Text( ', ' ) )
	colon = ApplyStyleSheetFromAttribute( GrammarEditorStyle.punctuationStyle, Text( ':' ) )
	args_view = []
	if len(args) > 0:
		args_view.append(ApplyStyleSheetFromAttribute( GrammarEditorStyle.ruleNameStyle, Text(args[0]) ))
		for a in args[1:]:
			args_view.append(comma)
			args_view.append(ApplyStyleSheetFromAttribute( GrammarEditorStyle.ruleNameStyle, Text(a) ))
	header = Paragraph([def_keyword, macro_name, open] + args_view + [close, colon])
	body = Paragraph([Spacer(30.0, 0.0), body_view])
	return Column(0, [header, body])

def grammar_expr_view(expr_view):
	return Paragraph( [expr_view] )

def grammar_def_view(rules):
	return Column( rules )





#
#
# Unparsed
#
#

def unparseable_text(text):
	return ApplyStyleSheetFromAttribute( GrammarEditorStyle.unparseableStyle, Text( text ) )

def unparsed_elements(components):
	return Span( components )

def unparsed_statement(value):
	return value



_controller = GrammarEditorSyntaxRecognizingController.instance


class GrammarEditorView (MethodDispatchView):
	def __init__(self, parser):
		super( GrammarEditorView, self ).__init__()


	# TERMINALS
	@DMObjectNodeDispatchMethod( Schema.Literal )
	@_controller.expressionEditRule
	def Literal(self, fragment, inheritedState, model, value, quotation):
		quote = "'"   if quotation == 'single'   else   '"'
		return literal(string_literal( quote, value, False ))


	@DMObjectNodeDispatchMethod( Schema.Keyword )
	@_controller.expressionEditRule
	def Keyword(self, fragment, inheritedState, model, value, quotation):
		quote = "'"   if quotation == 'single'   else   '"'
		return keyword(string_literal( quote, value, False ))


	@DMObjectNodeDispatchMethod( Schema.Word )
	@_controller.expressionEditRule
	def Word(self, fragment, inheritedState, model, value, quotation):
		quote = "'"   if quotation == 'single'   else   '"'
		return word(string_literal( quote, value, False ))


	@DMObjectNodeDispatchMethod( Schema.RegEx )
	@_controller.expressionEditRule
	def RegEx(self, fragment, inheritedState, model, regex):
		p = EditPerspective.instance.applyTo(regex)
		p = Segment(StructuralItem( _controller, model, p ))
		return p



	# INVOKE RULE

	@DMObjectNodeDispatchMethod( Schema.InvokeRule )
	@_controller.expressionEditRule
	def InvokeRule(self, fragment, inheritedState, model, name):
		return invoke_rule(name)


	# INVOKE MACRO

	@DMObjectNodeDispatchMethod( Schema.InvokeMacro )
	@_controller.expressionEditRule
	def InvokeMacro(self, fragment, inheritedState, model, macro_name, param_exprs):
		param_views = SREInnerFragment.map( param_exprs, Precedence.PRECEDENCE_SEQUENCE )
		return invoke_macro(macro_name, param_views)


	# REPETITION

	@DMObjectNodeDispatchMethod( Schema.Optional )
	@_controller.expressionEditRule
	def Optional(self, fragment, inheritedState, model, subexp):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return optional(subexp_view)


	@DMObjectNodeDispatchMethod( Schema.ZeroOrMore )
	@_controller.expressionEditRule
	def ZeroOrMore(self, fragment, inheritedState, model, subexp):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return zero_or_more(subexp_view)


	@DMObjectNodeDispatchMethod( Schema.OneOrMore )
	@_controller.expressionEditRule
	def OneOrMore(self, fragment, inheritedState, model, subexp):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return one_or_more(subexp_view)


	@DMObjectNodeDispatchMethod( Schema.Repeat )
	@_controller.expressionEditRule
	def Repeat(self, fragment, inheritedState, model, subexp, n):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return repeat(subexp_view, n)


	@DMObjectNodeDispatchMethod( Schema.RepeatRange )
	@_controller.expressionEditRule
	def RepeatRange(self, fragment, inheritedState, model, subexp, a, b):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return repeat_range(subexp_view, a, b)


	# LOOK AHEAD

	@DMObjectNodeDispatchMethod( Schema.Peek )
	@_controller.expressionEditRule
	def Peek(self, fragment, inheritedState, model, subexp):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return peek(subexp_view)


	@DMObjectNodeDispatchMethod( Schema.PeekNot )
	@_controller.expressionEditRule
	def PeekNot(self, fragment, inheritedState, model, subexp):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return peek_not(subexp_view)


	@DMObjectNodeDispatchMethod( Schema.Suppress )
	@_controller.expressionEditRule
	def Suppress(self, fragment, inheritedState, model, subexp):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_REPEAT )
		return suppress(subexp_view)



	# ACTION, CONDITION

	@DMObjectNodeDispatchMethod( Schema.ActionPy )
	@_controller.expressionEditRule
	def ActionPy(self, fragment, inheritedState, model, py):
		p = EditPerspective.instance.applyTo(py)
		p = Paragraph( [ HiddenText( u'\ue000' ), p, HiddenText( u'\ue000' ) ] )
		tagline = ApplyStyleSheetFromAttribute( GrammarEditorStyle.py_tagline_style, Label('py') )
		p = Column(1, [tagline, p])
		p = _pyActionBorder.surround(p)
		p = Segment(StructuralItem( _controller, model, p ))
		return p


	@DMObjectNodeDispatchMethod( Schema.Action )
	@_controller.expressionEditRule
	def Action(self, fragment, inheritedState, model, subexp, action):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_ACTION )
		action_view = SREInnerFragment( action, Precedence.PRECEDENCE_ACTION )
		return action_pres(subexp_view, action_view)


	@DMObjectNodeDispatchMethod( Schema.Condition )
	@_controller.expressionEditRule
	def Condition(self, fragment, inheritedState, model, subexp, condition):
		subexp_view = SREInnerFragment( subexp, Precedence.PRECEDENCE_ACTION )
		condition_view = SREInnerFragment( condition, Precedence.PRECEDENCE_ACTION )
		return condition_pres(subexp_view, condition_view)


	# SEQUENCE, CHOICE

	@DMObjectNodeDispatchMethod( Schema.Sequence )
	@_controller.expressionEditRule
	def Sequence(self, fragment, inheritedState, model, subexps):
		subexp_views = SREInnerFragment.map( subexps, Precedence.PRECEDENCE_SEQUENCE )
		return sequence(subexp_views)


	@DMObjectNodeDispatchMethod( Schema.Combine )
	@_controller.expressionEditRule
	def Combine(self, fragment, inheritedState, model, subexps):
		subexp_views = SREInnerFragment.map( subexps, Precedence.PRECEDENCE_CHOICE )
		return combine(subexp_views)


	@DMObjectNodeDispatchMethod( Schema.Choice )
	@_controller.expressionEditRule
	def Choice(self, fragment, inheritedState, model, subexps):
		subexp_views = SREInnerFragment.map( subexps, Precedence.PRECEDENCE_CHOICE )
		return choice(subexp_views)


	# Unparsed
	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	@_controller.unparsedEditRule
	def UNPARSED(self, fragment, inheritedState, model, value):
		def _viewItem(x):
			if x is model:
				raise ValueError, 'Python2View.UNPARSED: self-referential unparsed node'
			if isinstance( x, str )  or  isinstance( x, unicode ):
				view = unparseable_text( x )
				return view
			elif isinstance( x, DMObject ):
				view = SREInnerFragment( x, Precedence.PRECEDENCE_CONTAINER_UNPARSED, EditMode.DISPLAY )
				#<NO_TREE_EVENT_LISTENER>
				view = StructuralItem( _controller, x, view )
				return view
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsed_elements( views )


	# Statements
	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	@_controller.statementEditRule
	def BlankLine(self, fragment, inheritedState, model):
		return statement_line(blank_line(), model)


	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	@_controller.statementEditRule
	def CommentStmt(self, fragment, inheritedState, model, comment):
		return statement_line(comment_stmt(comment), model)

	@DMObjectNodeDispatchMethod(Schema.RuleDefinitionStmt)
	@_controller.statementEditRule
	def RuleDefinitionStmt(self, fragment, inh, model, name, body):
		body_view = SREInnerFragment( body, Precedence.PRECEDENCE_STMT )
		return statement_line(rule_def(name, body_view), model)

	@DMObjectNodeDispatchMethod(Schema.MacroDefinitionStmt)
	@_controller.statementEditRule
	def MacroDefinitionStmt(self, fragment, inh, model, name, args, body):
		body_view = SREInnerFragment( body, Precedence.PRECEDENCE_STMT )
		return statement_line(macro_def(name, args, body_view), model)


	@DMObjectNodeDispatchMethod(Schema.HelperBlockPy)
	@_controller.specialFormStatementEditRule
	def HelperBlockPy(self, fragment, inh, model, py):
		p = EditPerspective.instance.applyTo(py)
		tagline = ApplyStyleSheetFromAttribute( GrammarEditorStyle.py_tagline_style, Label('py') )
		p = Column(1, [tagline, p])
		p = _pyActionBorder.surround(p)
		return special_form_statement_line(p, model)


	@DMObjectNodeDispatchMethod(Schema.OperatorTable)
	@_controller.specialFormStatementEditRule
	def OperatorTable(self, fragment, inh, model, op_table):
		p = EditPerspective.instance.applyTo(op_table)
		return special_form_statement_line(p, model)


	@DMObjectNodeDispatchMethod(Schema.UnitTestTable)
	@_controller.specialFormStatementEditRule
	def UnitTestTable(self, fragment, inh, model, test_table):
		p = EditPerspective.instance.applyTo(test_table)
		return special_form_statement_line(p, model)


	@DMObjectNodeDispatchMethod( Schema.UnparsedStmt )
	@_controller.specialFormStatementEditRule
	def UnparsedStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, Precedence.PRECEDENCE_STMT )
		return statement_line(unparsed_statement( valueView ), model)


	# OUTER NODES
	@DMObjectNodeDispatchMethod( Schema.GrammarExpression )
	@_controller.expressionTopLevelEditRule
	def GrammarExpression(self, fragment, inheritedState, model, expr):
		if expr is not None:
			expr_view = SREInnerFragment(expr, Precedence.PRECEDENCE_NONE, EditMode.EDIT)
		else:
			expr_view = Text('')

		g = grammar_expr_view( expr_view ).alignHPack().alignVRefY()
		return g


	@DMObjectNodeDispatchMethod( Schema.GrammarDefinition )
	def GrammarDefinition(self, fragment, inheritedState, model, rules):
		exc_view = LiveValue(Blank())

		def _run_unit_tests(element):
			try:
				test_runner = parser_generator.GrammarTestRunner()
				test_runner(model)
			except:
				exc = JythonException.getCurrentException()
				exc_pres = DefaultPerspective.instance.applyTo(exc)
				exc_view.setLiteralValue(Column([SectionHeading3('Caught exception:'), Pres.coerce(exc_pres).padX(10.0, 0.0)]))
			else:
				exc_view.setLiteralValue(Blank())

		if len( rules ) == 0:
			# Empty module - create a single blank line so that there is something to edit
			lineViews = [ statement_line( blank_line(), None ) ]
		else:
			lineViews = SREInnerFragment.map( rules, Precedence.PRECEDENCE_NONE, EditMode.EDIT )
		g = grammar_def_view( lineViews ).alignHPack().alignVRefY().withProperty(Properties.GrammarDefProperty.instance, model).withCommands(Commands.grammarCommands)
		g = g.withShortcut( Shortcut( KeyEvent.VK_U, Modifier.ALT), _run_unit_tests )
		g = g.withShortcut( Shortcut( KeyEvent.VK_U, Modifier.META), _run_unit_tests )
		g = Column([g, Spacer(0.0, 5.0), exc_view.withFixedValue('')])
		g = SoftStructuralItem( GrammarEditorSyntaxRecognizingController.instance, [ GrammarEditorSyntaxRecognizingController.instance._makeGrammarEditFilter( rules ),
																					 GrammarEditorSyntaxRecognizingController.instance._topLevel ], rules, g )
		return g


_parser = GrammarEditorGrammar()
_view = GrammarEditorView( _parser )
perspective = SequentialEditorPerspective( _view.fragmentViewFunction, _controller )





