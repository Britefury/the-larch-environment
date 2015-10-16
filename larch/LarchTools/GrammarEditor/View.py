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

from BritefuryJ.AttributeTable import AttributeNamespace, InheritedAttributeNonNull, PyDerivedValueTable

from BritefuryJ.Pres import ApplyPerspective, ApplyStyleSheetFromAttribute
from BritefuryJ.Pres.Primitive import Primitive, Box, Text, Label, HiddenText, Segment, Script, Span, Row, Column, Paragraph, FlowGrid, Whitespace, Border

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.EditPerspective import EditPerspective

from BritefuryJ.Projection import Perspective
from BritefuryJ.IncrementalView import FragmentView, FragmentData

from BritefuryJ.Editor.Sequential import SequentialEditorPerspective
from BritefuryJ.Editor.Sequential.EditFilter import HandleEditResult
from BritefuryJ.Editor.Sequential.Item import StructuralItem, SoftStructuralItem
from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SREInnerFragment
from BritefuryJ.Editor.SyntaxRecognizing.SyntaxRecognizingController import EditMode

from LarchCore.Languages.Python2.PythonEditor.PythonEditorCombinators import PythonEditorStyle
from LarchTools.PythonTools.VisualRegex.View import _repeatBorder, _controlCharStyle

from LarchTools.GrammarEditor import Schema, Precedence
from LarchTools.GrammarEditor.Parser import GrammarEditorGrammar
from LarchTools.GrammarEditor.SRController import GrammarEditorSyntaxRecognizingController



# class GrammarEditorStyle (object):
# 	grammarEditor = AttributeNamespace( 'grammarEditor' )
#
# 	_grammarCodeFont = 'Noto Sans; SansSerif'
#
# 	#keywordStyle = InheritedAttributeNonNull( pythonEditor, 'keywordStyle', StyleSheet,
# 	#StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.fontBold( True ),
# 	#Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ), Primitive.fontSmallCaps( True ) )
#
# 	keywordStyle = InheritedAttributeNonNull( grammarEditor, 'keywordStyle', StyleSheet,
# 						  StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.fontBold( True ),
# 								    Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ) ) )
# 	commentStyle = InheritedAttributeNonNull( grammarEditor, 'commentStyle', StyleSheet,
# 						  StyleSheet.style( Primitive.fontFace( _grammarCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ) ) )



_non_escaped_string_re = re.compile( r'(\\(?:[abnfrt\\' + '\'\"' + r']|(?:x[0-9a-fA-F]{2})|(?:u[0-9a-fA-F]{4})|(?:U[0-9a-fA-F]{8})))' )

def string_literal(quotation, value, raw):
	boxContents = []

	# Split the value into pieces of escaped and non-escaped content
	if raw:
		valuePres = ApplyStyleSheetFromAttribute( PythonEditorStyle.stringLiteralStyle, Text( value ) )
	else:
		segments = _non_escaped_string_re.split( value )
		if len( segments ) == 1:
			valuePres = ApplyStyleSheetFromAttribute( PythonEditorStyle.stringLiteralStyle, Text( value ) )
		else:
			escape = False
			segsAsPres = []
			for seg in segments:
				if seg is not None  and  len( seg ) > 0:
					if escape:
						segsAsPres.append( ApplyStyleSheetFromAttribute( PythonEditorStyle.stringLiteralEscapeStyle, Border( Text( seg ) ) ) )
					else:
						segsAsPres.append( Text( seg ) )
				escape = not escape
			valuePres = ApplyStyleSheetFromAttribute( PythonEditorStyle.stringLiteralStyle, Span( segsAsPres ) )

	quotationPres = ApplyStyleSheetFromAttribute( PythonEditorStyle.quotationStyle, Text( quotation ) )
	boxContents.extend( [ quotationPres,  valuePres,  quotationPres ] )

	return Row( boxContents )


def literal(string_view):
	return string_view


def keyword(string_view):
	prefix = ApplyStyleSheetFromAttribute( PythonEditorStyle.keywordStyle, Text( 'k' ) )
	return Span([prefix, string_view])


def word(string_view):
	prefix = ApplyStyleSheetFromAttribute( PythonEditorStyle.keywordStyle, Text( 'w' ) )
	return Span([prefix, string_view])


def invoke_rule(name):
	left = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '<' ) )
	right = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '>' ) )
	n = ApplyStyleSheetFromAttribute( PythonEditorStyle.numLiteralStyle, Text( name ) )
	return Span([left, n, right])

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



def sequence(subexps):
	items = []
	if len(subexps) > 0:
		items.append(subexps[0])
		for x in subexps[1:]:
			items.append(Text(' '))
			items.append(x)
	return Span(items)

def choice(subexps):
	items = []
	if len(subexps) > 0:
		items.append(Paragraph([subexps[0]]))
		for x in subexps[1:]:
			p = [ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text('| ')), x]
			items.append(Paragraph(p))
	return Column(items)



def blank_line():
	return Text( '' )

def comment_stmt(comment):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.commentStyle, Text( '#' + comment ) )

def statement_line(rule):
	rule = Segment( rule )
	newLine = Whitespace( '\n' )
	return Paragraph( [ rule, newLine ] ).alignHPack()

def rule_def(name, body_view):
	n = ApplyStyleSheetFromAttribute( PythonEditorStyle.numLiteralStyle, Text( name ) )
	assign = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( ' := ' ) )
	return Row([n, assign, Paragraph([body_view])])

def grammar_view(rules):
	return Column( rules )





#
#
# Unparsed
#
#

def unparseable_text(text):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.unparseableStyle, Text( text ) )

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



	# INVOKE RULE

	@DMObjectNodeDispatchMethod( Schema.InvokeRule )
	@_controller.expressionEditRule
	def InvokeRule(self, fragment, inheritedState, model, name):
		return invoke_rule(name)


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


	# SEQUENCE, CHOICE

	@DMObjectNodeDispatchMethod( Schema.Sequence )
	@_controller.expressionEditRule
	def Sequence(self, fragment, inheritedState, model, subexps):
		subexp_views = SREInnerFragment.map( subexps, Precedence.PRECEDENCE_SEQUENCE )
		return sequence(subexp_views)


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
			else:
				raise TypeError, 'UNPARSED should contain a list of only strings or nodes, not a %s'  %  ( type( x ), )
		views = [ _viewItem( x )   for x in value ]
		return unparsed_elements( views )


	# Statements
	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	@_controller.statementEditRule
	def BlankLine(self, fragment, inheritedState, model):
		return statement_line(blank_line())


	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	@_controller.statementEditRule
	def CommentStmt(self, fragment, inheritedState, model, comment):
		return statement_line(comment_stmt(comment))

	@DMObjectNodeDispatchMethod(Schema.RuleDefinitionStmt)
	@_controller.statementEditRule
	def RuleDefinitionStmt(self, fragment, inh, model, name, body):
		body_view = SREInnerFragment( body, Precedence.PRECEDENCE_STMT )
		return statement_line(rule_def(name, body_view))


	@DMObjectNodeDispatchMethod( Schema.UnparsedStmt )
	@_controller.statementEditRule
	def UnparsedStmt(self, fragment, inheritedState, model, value):
		valueView = SREInnerFragment( value, Precedence.PRECEDENCE_STMT )
		return statement_line(unparsed_statement( valueView ))


	# OUTER NODES
	@DMObjectNodeDispatchMethod( Schema.GrammarDefinition )
	def GrammarDefinition(self, fragment, inheritedState, model, rules):
		if len( rules ) == 0:
			# Empty module - create a single blank line so that there is something to edit
			lineViews = [ statement_line( blank_line() ) ]
		else:
			lineViews = SREInnerFragment.map( rules, Precedence.PRECEDENCE_NONE, EditMode.EDIT )
		g = grammar_view( lineViews ).alignHPack().alignVRefY()
		g = SoftStructuralItem( GrammarEditorSyntaxRecognizingController.instance, [ GrammarEditorSyntaxRecognizingController.instance._makeGrammarEditFilter( rules ),
											     GrammarEditorSyntaxRecognizingController.instance._topLevel ], rules, g )
		return g



_parser = GrammarEditorGrammar()
_view = GrammarEditorView( _parser )
perspective = SequentialEditorPerspective( _view.fragmentViewFunction, _controller )
