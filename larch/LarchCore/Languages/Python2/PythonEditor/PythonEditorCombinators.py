##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import re

from java.awt import Color
from java.util.regex import Pattern

from Britefury.Util.Lerp import lerp, lerpColour

from BritefuryJ.AttributeTable import AttributeNamespace, InheritedAttributeNonNull, PyDerivedValueTable
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Graphics import AbstractBorder, SolidBorder, FilledBorder, Painter, FillPainter, FilledOutlinePainter
from BritefuryJ.Live import LiveFunction
from BritefuryJ.Controls import TextArea, DropDownExpander
from BritefuryJ.Pres import ApplyStyleSheetFromAttribute, PyPresCombinatorFn
from BritefuryJ.Pres.Primitive import Primitive, Blank, Label, Text, Whitespace, LineBreak, Box, Border, Segment, Fraction, Script, Span, Row, Column, Paragraph, LineBreakCostSpan, ParagraphIndentMatchSpan
from BritefuryJ.Pres.Sequence import Sequence, SpanSequenceView, TrailingSeparator

from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceBrackets



class PythonEditorStyle (object):
	pythonEditor = AttributeNamespace( 'pythonEditor' )

	_pythonCodeFont = 'Noto Sans; SansSerif'

	#keywordStyle = InheritedAttributeNonNull( pythonEditor, 'keywordStyle', StyleSheet,
							#StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.fontBold( True ),
							#Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ), Primitive.fontSmallCaps( True ) )

	keywordStyle = InheritedAttributeNonNull( pythonEditor, 'keywordStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.fontBold( True ),
	                                          Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ) ) )
	literalFormatStyle = InheritedAttributeNonNull( pythonEditor, 'literalFormatStyle', StyleSheet,
	                                                StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.25, 0.25 ) ) ) )
	quotationStyle = InheritedAttributeNonNull( pythonEditor, 'quotationStyle', StyleSheet,
	                                            StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.0, 0.5 ) ) ) )
	stringLiteralStyle = InheritedAttributeNonNull( pythonEditor, 'stringLiteralStyle', StyleSheet,
	                                                StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ) ) )
	stringLiteralEscapeStyle = InheritedAttributeNonNull( pythonEditor, 'stringLiteralEscapeStyle', StyleSheet,
	                                                StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.25, 0.2, 0.15 ) ),
	                                                                  Primitive.border( SolidBorder( 1.0, 1.0, 4.0, 4.0, Color( 0.75, 0.6, 0.5 ), Color( 1.0, 0.85, 0.75 ) ) ) ) )
	numLiteralStyle = InheritedAttributeNonNull( pythonEditor, 'numLiteralStyle', StyleSheet,
	                                             StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.5, 0.5 ) ) ) )
	punctuationStyle = InheritedAttributeNonNull( pythonEditor, 'punctuationStyle', StyleSheet,
	                                              StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.0, 1.0 ) ) ) )
	delimStyle = InheritedAttributeNonNull( pythonEditor, 'delimStyle', StyleSheet,
	                                        StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.0, 0.0, 1.0 ) ) ) )
	targetStyle = InheritedAttributeNonNull( pythonEditor, 'targetStyle', StyleSheet,
	                                         StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color.black ) ) )
	varStyle = InheritedAttributeNonNull( pythonEditor, 'varStyle', StyleSheet,
	                                      StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color.black ) ) )
	attributeStyle = InheritedAttributeNonNull( pythonEditor, 'attributeStyle', StyleSheet,
	                                            StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color.black ) ) )
	kwNameStyle = InheritedAttributeNonNull( pythonEditor, 'kwNameStyle', StyleSheet,
	                                         StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color.black ) ) )
	operatorStyle = InheritedAttributeNonNull( pythonEditor, 'operatorStyle', StyleSheet,
	                                           StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontBold( True ), Primitive.fontSize( 14 ),
						   Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ) )
	paramStyle = InheritedAttributeNonNull( pythonEditor, 'paramStyle', StyleSheet,
	                                        StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color.black ) ) )
	importStyle = InheritedAttributeNonNull( pythonEditor, 'importStyle', StyleSheet,
	                                         StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color.black ) ) )
	commentStyle = InheritedAttributeNonNull( pythonEditor, 'commentStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ), Primitive.foreground( Color( 0.4, 0.4, 0.4 ) ) ) )
	unparseableStyle = InheritedAttributeNonNull( pythonEditor, 'unparseableStyle', StyleSheet,
	                                              StyleSheet.style( Primitive.fontFace( _pythonCodeFont ), Primitive.fontSize( 14 ),
	                                              Primitive.foreground( Color.black ), Primitive.textSquiggleUnderlinePaint( Color.red ) ) )

	sequenceStyle = InheritedAttributeNonNull( pythonEditor, 'sequenceStyle', StyleSheet,
	                                           StyleSheet.style( Sequence.addLineBreaks( True ), Sequence.matchOuterIndentation( True ), Sequence.addLineBreakCost( True ) ) )

	quoteBorderStyle = InheritedAttributeNonNull( pythonEditor, 'quoteBorderStyle', StyleSheet,
	                                              StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.5, 0.3, 0.7 ), None ) ) ) )
	quoteTitleStyle = InheritedAttributeNonNull( pythonEditor, 'quoteTitleStyle', StyleSheet,
	                                             StyleSheet.style( Primitive.foreground( Color( 0.3, 0.1, 0.5 ) ), Primitive.fontSize( 10 ) ) )

	unquoteBorderStyle = InheritedAttributeNonNull( pythonEditor, 'unquoteBorderStyle', StyleSheet,
	                                                StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 1.0, 0.5, 0.3 ), None ) ) ) )
	unquoteTitleStyle = InheritedAttributeNonNull( pythonEditor, 'unquoteTitleStyle', StyleSheet,
	                                               StyleSheet.style( Primitive.foreground( Color( 0.7, 0.35, 0.0 ) ), Primitive.fontSize( 10 ) ) )

	externalExprBorderStyle = InheritedAttributeNonNull( pythonEditor, 'externalExprBorderStyle', StyleSheet,
	                                                     StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.3, 0.7, 1.0 ), None ) ) ) )
	externalExprTitleStyle = InheritedAttributeNonNull( pythonEditor, 'externalExprTitleStyle', StyleSheet,
	                                                    StyleSheet.style( Primitive.foreground( Color( 0.0, 0.5, 1.0 ) ), Primitive.fontSize( 10 ) ) )

	embeddedObjectBorder = InheritedAttributeNonNull( pythonEditor, 'embeddedObjectBorder', AbstractBorder,
	                                                     SolidBorder( 1.5, 1.5, 4.0, 4.0, Color( 0.6, 0.65, 0.8 ), None ) )
	embeddedObjectLiteralBorder = InheritedAttributeNonNull( pythonEditor, 'embeddedObjectLiteralBorder', AbstractBorder,
							       SolidBorder( 1.5, 1.5, 4.0, 4.0, Color( 0.4, 0.433, 0.533 ), None ) )
	embeddedObjectTagLabelStyle = InheritedAttributeNonNull( pythonEditor, 'embeddedObjectTagLabelStyle', StyleSheet,
								      StyleSheet.style( Primitive.foreground( Color( 0.0, 0.0, 0.0, 0.6 ) ), Primitive.fontSize( 9 ) ) )
	embeddedObjectTagBorder = InheritedAttributeNonNull( pythonEditor, 'embeddedObjectTagBorder', AbstractBorder,
							     SolidBorder( 1.0, 1.0, 3.0, 3.0, Color( 0.45, 0.4, 0.533 ), Color( 0.925, 0.9, 0.95 ) ) )
	embeddedObjectLineStyle = InheritedAttributeNonNull( pythonEditor, 'embeddedObjectLineStyle', StyleSheet,
	                                                   StyleSheet.style( Primitive.shapePainter( FillPainter( Color( 0.1, 0.2, 0.3 ) ) ) ) )
	embeddedObjectExpansionLabelStyle = InheritedAttributeNonNull( pythonEditor, 'embeddedObjectExpansionLabelStyle', StyleSheet,
	                                                     StyleSheet.style( Primitive.fontSize( 10 ) ) )

	paragraphIndentationStyle = InheritedAttributeNonNull( pythonEditor, 'paragraphIndentationStyle', StyleSheet, StyleSheet.style( Primitive.paragraphIndentation( 40.0 ) ) )



	solidHighlightRounding = InheritedAttributeNonNull( pythonEditor, 'solidHighlightRounding', float, 3.0 )
	outlineHighlightThickness = InheritedAttributeNonNull( pythonEditor, 'outlineHighlightThickness', float, 1.5 )
	outlineHighlightInset = InheritedAttributeNonNull( pythonEditor, 'outlineHighlightInset', float, 2.0 )
	outlineHighlightRounding = InheritedAttributeNonNull( pythonEditor, 'outlineHighlightRounding', float, 5.0 )

	defStmtHighlightColour = InheritedAttributeNonNull( pythonEditor, 'defStmtHighlightColour', Color, Color( 0.420, 0.620, 0.522 ) )
	classStmtHighlightColour = InheritedAttributeNonNull( pythonEditor, 'classStmtHighlightColour', Color, Color( 0.522, 0.420, 0.620 ) )
	badIndentationRectanglePainter = InheritedAttributeNonNull( pythonEditor, 'badIndentationRectanglePainter', Painter, FilledOutlinePainter( lerpColour( Color.RED, Color.WHITE, 0.75 ), lerpColour( Color.RED, Color.WHITE, 0.5 ) ) )

	comprehensionSpacing = InheritedAttributeNonNull( pythonEditor, 'comprehensionSpacing', float, 15.0 )
	conditionalSpacing = InheritedAttributeNonNull( pythonEditor, 'conditionalSpacing', float, 15.0 )
	blockIndentation = InheritedAttributeNonNull( pythonEditor, 'blockIndentation', float, 30.0 )



	@PyDerivedValueTable( pythonEditor )
	def _defStmtHeaderHighlightStyle(style):
		border = _solidHighlightBorder( style, style.get( PythonEditorStyle.defStmtHighlightColour ) )
		return style.withValues( Primitive.border( border ) )

	@PyDerivedValueTable( pythonEditor )
	def _defStmtHighlightStyle(style):
		border = _outlineHighlightBorder( style, style.get( PythonEditorStyle.defStmtHighlightColour ) )
		return style.withValues( Primitive.border( border ) )

	@PyDerivedValueTable( pythonEditor )
	def _classStmtHeaderHighlightStyle(style):
		border = _solidHighlightBorder( style, style.get( PythonEditorStyle.classStmtHighlightColour ) )
		return style.withValues( Primitive.border( border ) )

	@PyDerivedValueTable( pythonEditor )
	def _classStmtHighlightStyle(style):
		border = _outlineHighlightBorder( style, style.get( PythonEditorStyle.classStmtHighlightColour ) )
		return style.withValues( Primitive.border( border ) )

	@PyDerivedValueTable( pythonEditor )
	def _badIndentationRectangleStyle(style):
		painter = style.get( PythonEditorStyle.badIndentationRectanglePainter )
		return style.withValues( Primitive.shapePainter( painter ) )





_keywordMap = {}



def _initKeywords(keywords):
	for keyword in keywords:
		#keyword = keyword[0].upper() + keyword[1:]
		_keywordMap[keyword] = ApplyStyleSheetFromAttribute( PythonEditorStyle.keywordStyle, Text( keyword, keyword ) )

_initKeywords( [ 'as', 'assert', 'break', 'class', 'continue', 'def', 'del', 'elif', 'else', 'except', 'exec', 'finally', 'for', 'from', 'global', 'lambda', 'if', 'import', 'in', 'pass', 'print', 'raise', 'return', 'try', 'while', 'with', 'yield' ] )









def _solidHighlightBorder(style, colour):
	solidHighlightRounding = style.get( PythonEditorStyle.solidHighlightRounding )
	colour = lerpColour( colour, Color.white, 0.8 )
	return FilledBorder( 0.0, 0.0, 0.0, 0.0, solidHighlightRounding, solidHighlightRounding, colour )

def _outlineHighlightBorder(style, colour):
	thickness = style.get( PythonEditorStyle.outlineHighlightThickness )
	inset = style.get( PythonEditorStyle.outlineHighlightInset )
	rounding = style.get( PythonEditorStyle.outlineHighlightRounding )
	return SolidBorder( thickness, inset, rounding, rounding, colour, None )




_space = Text( ' ' )
_lineBreak = LineBreak()


openParen = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( '(' ) )
closeParen = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( ')' ) )

_openParen = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( '(' ) )
_closeParen = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( ')' ) )
_openBracket = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( '[' ) )
_closeBracket = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( ']' ) )
_openBrace = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( '{' ) )
_closeBrace = ApplyStyleSheetFromAttribute( PythonEditorStyle.delimStyle, Text( '}' ) )
_comma = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( ',' ) )
_dot = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '.' ) )
_colon = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( ':' ) )
_ellipsis = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '...' ) )
_equals = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '=' ) )
_asterisk = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '*' ) )
_at = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '@' ) )
_doubleAsterisk = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '**' ) )
_doubleRightChevron = ApplyStyleSheetFromAttribute( PythonEditorStyle.punctuationStyle, Text( '>>' ) )


_exponent = ApplyStyleSheetFromAttribute( PythonEditorStyle.operatorStyle, Text( '**' ) )



def _keyword(keyword):
	return _keywordMap[keyword]


#
#
# Precedence
#
#


def applyPythonParens(expr, precedence, numParens, inheritedState):
	return PrecedenceBrackets.editorPrecedenceBrackets( expr, precedence, numParens, inheritedState, _openParen, _closeParen )



#
#
# Unparsed
#
#

def unparseableText(text):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.unparseableStyle, Text( text ) )

def unparsedElements(components):
	return Span( components )




#
#
# Literals
#
#

_non_escaped_string_re = re.compile( r'(\\(?:[abnfrt\\' + '\'\"' + r']|(?:x[0-9a-fA-F]{2})|(?:u[0-9a-fA-F]{4})|(?:U[0-9a-fA-F]{8})))' )

def stringLiteral(format, quotation, value, isUnicode, raw):
	boxContents = []

	if format is not None  and  format != '':
		boxContents.append( ApplyStyleSheetFromAttribute( PythonEditorStyle.literalFormatStyle, Text( format ) ) )

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


_string_escape_re = Pattern.compile( r'\\(?:[abnfrt\\' + '\'\"' + r']|(?:x[0-9a-fA-F]{2})|(?:u[0-9a-fA-F]{4})|(?:U[0-9a-fA-F]{8}))' )

_stringTextPresFn = TextArea.RegexPresTable()
_stringTextPresFn.addPattern( _string_escape_re, lambda text: ApplyStyleSheetFromAttribute( PythonEditorStyle.stringLiteralEscapeStyle, Border( Text( text ) ) ) )


def multilineStringLiteral(valueLiveFunction, isUnicode, raw, editFn):
	class _Listener (TextArea.TextAreaListener):
		def onTextChanged(self, area):
			editFn( area.getDisplayedText() )


	t = TextArea( valueLiveFunction, _Listener() )
	if not raw:
		t = t.withTextToPresFunction( _stringTextPresFn )

	return t


def intLiteral(format, value):
	valuePres = ApplyStyleSheetFromAttribute( PythonEditorStyle.numLiteralStyle, Text( value ) )
	boxContents = [ valuePres ]
	if format is not None:
		boxContents.append( ApplyStyleSheetFromAttribute( PythonEditorStyle.literalFormatStyle, Text( format ) ) )

	return Row( boxContents )


def floatLiteral(value):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.numLiteralStyle, Text( value ) )


def imaginaryLiteral(value):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.numLiteralStyle, Text( value ) )


#
#
# Targets
#
#

def singleTarget( name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.targetStyle, Text( name ) )


def tupleTarget(items, bTrailingSeparator):
	seq = SpanSequenceView( items, None, None, _comma, _space, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, seq )


def listTarget(items, bTrailingSeparator):
	seq = SpanSequenceView( items, _openBracket, _closeBracket, _comma, _space, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, seq )



#
#
# Simple expressions
#
#

def load(name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.varStyle, Text( name ) )


def tupleLiteral(items, bTrailingSeparator):
	seq = SpanSequenceView( items, None, None, _comma, _space, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, seq )


def listLiteral(items, bTrailingSeparator):
	seq = SpanSequenceView( items, _openBracket, _closeBracket, _comma, _space, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, seq )


def comprehensionFor(target, source):
	return Span( [ _keyword( 'for' ),
	               _space,
	               target,
	               _space,
	               _lineBreak,
	               _keyword( 'in' ),
	               _space,
	               source ] )


def comprehensionIf(condition):
	return Span( [ _keyword( 'if' ),
	               _space,
	               condition ] )

@PyPresCombinatorFn
def listComp(ctx, style, resultExpr, comprehensionItems):
	comprehensionSpacing = style.get( PythonEditorStyle.comprehensionSpacing )
	itemViewsSpaced = []
	if len( comprehensionItems ) > 0:
		for x in comprehensionItems[:-1]:
			itemViewsSpaced.append( x )
			itemViewsSpaced.append( Whitespace( ' ', comprehensionSpacing ) )
			itemViewsSpaced.append( _lineBreak )
		itemViewsSpaced.append( comprehensionItems[-1] )
	return LineBreakCostSpan( [ _openBracket,  resultExpr,  Whitespace( ' ', comprehensionSpacing ) ]  +  itemViewsSpaced  +  [ _closeBracket ] ).present( ctx, style )


@PyPresCombinatorFn
def genExpr(ctx, style, resultExpr, comprehensionItems):
	comprehensionSpacing = style.get( PythonEditorStyle.comprehensionSpacing )
	itemViewsSpaced = []
	if len( comprehensionItems ) > 0:
		for x in comprehensionItems[:-1]:
			itemViewsSpaced.append( x )
			itemViewsSpaced.append( Whitespace( ' ', comprehensionSpacing ) )
			itemViewsSpaced.append( _lineBreak )
		itemViewsSpaced.append( comprehensionItems[-1] )
	return LineBreakCostSpan( [ _openParen,  resultExpr,  Whitespace( ' ', comprehensionSpacing ) ]  +  itemViewsSpaced  +  [ _closeParen ] ).present( ctx, style )


def dictKeyValuePair(key, value):
	return Span( [ key, _colon, value ] )


def dictLiteral(items, bTrailingSeparator):
	seq = SpanSequenceView( items, _openBrace, _closeBrace, _comma, _space, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, seq )

@PyPresCombinatorFn
def dictComp(ctx, style, resultExpr, comprehensionItems):
	comprehensionSpacing = style.get( PythonEditorStyle.comprehensionSpacing )
	itemViewsSpaced = []
	if len( comprehensionItems ) > 0:
		for x in comprehensionItems[:-1]:
			itemViewsSpaced.append( x )
			itemViewsSpaced.append( Whitespace( ' ', comprehensionSpacing ) )
			itemViewsSpaced.append( _lineBreak )
		itemViewsSpaced.append( comprehensionItems[-1] )
	return LineBreakCostSpan( [ _openBrace,  resultExpr,  Whitespace( ' ', comprehensionSpacing ) ]  +  itemViewsSpaced  +  [ _closeBrace ] ).present( ctx, style )


def setLiteral(items, bTrailingSeparator):
	seq = SpanSequenceView( items, _openBrace, _closeBrace, _comma, _space, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, seq )

# Just re-use dictComp, since they are the same
setComp = dictComp


def yieldExpr(value):
	if value is not None:
		return Span( [ _keyword( 'yield' ), _space, value ] )
	else:
		return _keyword( 'yield' )


def attributeRef(target, name):
	attribView = ApplyStyleSheetFromAttribute( PythonEditorStyle.attributeStyle, Text( name ) )
	return Span( [ target, _dot, attribView ] )


def subscriptSlice(lower, upper):
	lower = [ lower ]   if lower is not None   else []
	upper = [ upper ]   if upper is not None   else []
	return Span( lower + [ _colon, _lineBreak ] + upper )


def subscriptLongSlice(lower, upper, stride):
	lower = [ lower ]   if lower is not None   else []
	upper = [ upper ]   if upper is not None   else []
	stride = [ stride ]   if stride is not None   else []
	return Span( lower + [ _colon, _lineBreak ] + upper + [ _colon, _lineBreak ] + stride )


def subscriptEllipsis():
	return _ellipsis


def subscriptTuple(items, bTrailingSeparator):
	seq = SpanSequenceView( items, None, None, _comma, _space, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, seq )


def subscript(target, index):
	return Span( [ target, _openBracket, index, _closeBracket ] )



def callKWArg(name, value):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.kwNameStyle, Text( name ) )
	return Span( [ nameView, _equals, value ] )


def callArgList(value):
	return Span( [ _asterisk, value ] )


def callKWArgList(value):
	return Span( [ _doubleAsterisk, value ] )



def call(target, args, bArgsTrailingSeparator):
	argElements = []
	if len( args ) > 0:
		argElements.append( _space )
		argSpanElements = []
		for a in args[:-1]:
			argSpanElements.append( a )
			argSpanElements.append( _comma )
			argSpanElements.append( _space )
			argSpanElements.append( _lineBreak )
		argSpanElements.append( args[-1] )
		if bArgsTrailingSeparator:
			argSpanElements.append( _comma )
		argElements.append( ParagraphIndentMatchSpan( argSpanElements ) )
		argElements.append( _space )
	return LineBreakCostSpan( [ target, _openParen ]  +  argElements  +  [ _closeParen ] )



def exponent(x, y):
	xElement = Paragraph( [ x ] )
	yElement = Paragraph( [ _exponent, _space, y ] )
	return Script.scriptRSuper( xElement, yElement )




def spanPrefixOp(x, op):
	opView = ApplyStyleSheetFromAttribute( PythonEditorStyle.operatorStyle, Text( op ) )
	return LineBreakCostSpan( [ opView, x ] )


def spanBinOp(x, y, op):
	opView = ApplyStyleSheetFromAttribute( PythonEditorStyle.operatorStyle, Text( op ) )
	return LineBreakCostSpan( [ x, Text( ' ' ), opView, Text( ' ' ), _lineBreak, y ] )


def spanCmpOp(op, y):
	opView = ApplyStyleSheetFromAttribute( PythonEditorStyle.operatorStyle, Text( op ) )
	return Span( [ Text( ' ' ), opView, Text( ' ' ), _lineBreak, y ] )



def div(x, y, fractionBarContent):
	bar = ApplyStyleSheetFromAttribute( PythonEditorStyle.operatorStyle, Fraction.FractionBar( '/' ) )
	return Fraction( x, y, bar )


def compare(x, cmpOps):
	return LineBreakCostSpan( [ x ]  +  list(cmpOps) )


def simpleParam(name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.paramStyle, Text( name ) )


def tupleParam(params, bParamsTrailingSeparator):
	elements = []
	if len( params ) > 0:
		for p in params[:-1]:
			elements.append( p )
			elements.append( _comma )
			elements.append( _space )
			elements.append( _lineBreak )
		elements.append( params[-1] )
		if bParamsTrailingSeparator:
			elements.append( _comma )

	return Span( [ _openParen, ParagraphIndentMatchSpan( elements ), _closeParen ] )


def defaultValueParam(param, defaultValue):
	return Span( [ param, _equals, defaultValue ] )


def paramList(name):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.paramStyle, Text( name ) )
	return Span( [ _asterisk, nameView ] )


def kwParamList(name):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.paramStyle, Text( name ) )
	return Span( [ _doubleAsterisk, nameView ] )


def lambdaExpr(params, bParamsTrailingSeparator, expr):
	elements = []
	if len( params ) > 0:
		for p in params[:-1]:
			elements.append( p )
			elements.append( _comma )
			elements.append( _space )
			elements.append( _lineBreak )
		elements.append( params[-1] )
		if bParamsTrailingSeparator:
			elements.append( _comma )
			elements.append( _space )
			elements.append( _lineBreak )

	return LineBreakCostSpan( [ _keyword( 'lambda' ),  _space, ParagraphIndentMatchSpan( elements ), _colon,  _space, _lineBreak, expr ] )


@PyPresCombinatorFn
def conditionalExpr(ctx, style, condition, expr, elseExpr):
	conditionalSpacing = style.get( PythonEditorStyle.conditionalSpacing )

	return LineBreakCostSpan( [ expr,   Whitespace( '  ', conditionalSpacing ),  _lineBreak,
	                            _keyword( 'if' ), _space, condition,   Whitespace( '  ', conditionalSpacing ),  _lineBreak,
	                            _keyword( 'else' ), _space, elseExpr ] ).present( ctx, style )



@PyPresCombinatorFn
def quote(ctx, style, valueView, title, sequentialEditor):
	quoteBorderStyle = style.get( PythonEditorStyle.quoteBorderStyle )
	quoteTitleStyle = style.get( PythonEditorStyle.quoteTitleStyle )

	titleLabel = quoteTitleStyle.applyTo( Label( title ) )

	region = sequentialEditor.region( valueView )

	header = titleLabel.alignHLeft()
	box = quoteBorderStyle.applyTo( Border( Column( [ header.alignHExpand(), region.pad( 3.0, 3.0 ) ] ) ) ).pad( 1.0, 1.0 )

	segment = Segment( box )
	return segment.present( ctx, style )



@PyPresCombinatorFn
def unquote(ctx, style, valueView, title, sequentialEditor):
	unquoteBorderStyle = style.get( PythonEditorStyle.unquoteBorderStyle )
	unquoteTitleStyle = style.get( PythonEditorStyle.unquoteTitleStyle )

	titleLabel = unquoteTitleStyle.applyTo( Label( title ) )

	region = sequentialEditor.region( valueView )

	header = titleLabel.alignHLeft()
	box = unquoteBorderStyle.applyTo( Border( Column( [ header.alignHExpand(), region.pad( 3.0, 3.0 ) ] ) ) ).pad( 1.0, 1.0 )

	segment = Segment( box )
	return segment.present( ctx, style )



@PyPresCombinatorFn
def externalExpr(ctx, style, exprView, title, deleteButton):
	externalExprBorderStyle = style.get( PythonEditorStyle.externalExprBorderStyle )
	externalExprTitleStyle = style.get( PythonEditorStyle.externalExprTitleStyle )

	titleLabel = externalExprTitleStyle.applyTo( Label( title ) )

	header = Row( [ titleLabel.alignHLeft(), deleteButton.alignHRight().alignVCentre() ] )
	box = externalExprBorderStyle.applyTo( Border( Column( [ header.alignHExpand(), exprView.pad( 3.0, 3.0 ) ] ) ) ).pad( 1.0, 1.0 )

	segment = Segment( box )
	return segment.present( ctx, style )



@PyPresCombinatorFn
def embeddedObjectLiteral(ctx, style, valueView, hideFrame):
	embeddedObjectLiteralBorder = style.get( PythonEditorStyle.embeddedObjectLiteralBorder )
	embeddedObjectTagLabelStyle = style.get( PythonEditorStyle.embeddedObjectTagLabelStyle )
	embeddedObjectTagBorder = style.get( PythonEditorStyle.embeddedObjectTagBorder )

	tagLabel = embeddedObjectTagBorder.surround( embeddedObjectTagLabelStyle( Label( 'l' ) ) )
	contents = Row( [ tagLabel.alignVCentre(), valueView.pad( 2.0, 2.0 ) ] )
	box = embeddedObjectLiteralBorder.surround( contents ).padX( 1.0, 1.0 )

	segment = Segment( box )
	return segment.present( ctx, style )



@PyPresCombinatorFn
def embeddedObjectExpr(ctx, style, valueView, hideFrame):
	if hideFrame:
		box = valueView
	else:
		embeddedObjectBorder = style.get( PythonEditorStyle.embeddedObjectBorder )
		embeddedObjectTagLabelStyle = style.get( PythonEditorStyle.embeddedObjectTagLabelStyle )
		embeddedObjectTagBorder = style.get( PythonEditorStyle.embeddedObjectTagBorder )

		tagLabel = embeddedObjectTagBorder.surround( embeddedObjectTagLabelStyle( Label( 'e' ) ) )
		contents = Row( [ tagLabel.alignVCentre(), valueView.pad( 2.0, 2.0 ) ] )
		box = embeddedObjectBorder.surround( contents ).padX( 1.0, 1.0 )

	segment = Segment( box )
	return segment.present( ctx, style )



@PyPresCombinatorFn
def embeddedObjectStmt(ctx, style, valueView, hideFrame):
	if hideFrame:
		box = valueView
	else:
		embeddedObjectBorder = style.get( PythonEditorStyle.embeddedObjectBorder )
		embeddedObjectTagLabelStyle = style.get( PythonEditorStyle.embeddedObjectTagLabelStyle )
		embeddedObjectTagBorder = style.get( PythonEditorStyle.embeddedObjectTagBorder )

		tagLabel = embeddedObjectTagBorder.surround( embeddedObjectTagLabelStyle( Label( 'Stmt' ) ) )
		contents = Column( [ tagLabel, valueView.pad( 2.0, 2.0 ) ] )
		box = embeddedObjectBorder.surround( contents ).padX( 1.0, 1.0 )

	segment = Segment( box )
	return segment.present( ctx, style )



@PyPresCombinatorFn
def embeddedObjectMacro(ctx, style, valueView, modelView):
	modelView = StyleSheet.style( Primitive.editable( False ) ).applyTo( modelView )

	embeddedObjectBorderStyle = style.get( PythonEditorStyle.embeddedObjectBorderStyle )
	embeddedObjectLineStyle = style.get( PythonEditorStyle.embeddedObjectLineStyle )
	embeddedObjectExpansionLabelStyle = style.get( PythonEditorStyle.embeddedObjectExpansionLabelStyle )

	hLine = embeddedObjectLineStyle.applyTo( Box( 1, 1 ).alignHExpand() ).pad( 8.0, 2.0 ).alignHExpand()
	
	expansionLabel = embeddedObjectExpansionLabelStyle.applyTo( Label( 'Expansion' ) )
	
	expander = DropDownExpander( expansionLabel, modelView )
	
	view = embeddedObjectBorderStyle.applyTo( Border( Column( [ valueView, expander ] ) ) )

	segment = Segment( view )
	return segment.present( ctx, style )



def unparsedStmt(value):
	return value


def exprStmt(expr):
	return expr


def assertStmt(condition, fail):
	elements = [ _keyword( 'assert' ), _space, condition ]
	if fail is not None:
		elements.extend( [ _comma, _space, _lineBreak, fail ] )
	return Span( elements )


def assignStmt(targets, value):
	if len( targets ) == 1:
		return Span( [ targets[0], _space, _equals, _space, value ] )
	else:
		targetElements = []
		for t in targets:
			targetElements.extend( [ t,  _space,  _equals,  _space,  _lineBreak ] )
		return Span( targetElements  +  [ value ] )


def augAssignStmt(op, target, value):
	opView = ApplyStyleSheetFromAttribute( PythonEditorStyle.operatorStyle, Text( op ) )

	return Span( [ target, _space, opView, _space, value ] )


def passStmt():
	return _keyword( 'pass' )


def delStmt(target):
	return Span( [ _keyword( 'del' ),  _space,  target ] )


def returnStmt(value):
	if value is not None:
		return Span( [ _keyword( 'return' ),  _space,  value ] )
	else:
		return _keyword( 'return' )


def yieldStmt(value):
	if value is not None:
		return Span( [ _keyword( 'yield' ),  _space,  value ] )
	else:
		return _keyword( 'yield' )


def raiseStmt(excType, excValue, traceback):
	xs = [ x   for x in [ excType, excValue, traceback ]  if x is not None ]
	xElements = []
	if len( xs ) > 0:
		for x in xs[:-1]:
			xElements.extend( [ x,  _comma,  _space ] )
		xElements.append( xs[-1] )

	return Span( [ _keyword( 'raise' ),  _space ] + xElements )


def breakStmt():
	return _keyword( 'break' )


def continueStmt():
	return _keyword( 'continue' )


def relativeModule(name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.importStyle, Text( name ) )


def moduleImport(name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.importStyle, Text( name ) )


def moduleImportAs(name, asName):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.importStyle, Text( name ) )
	asNameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.importStyle, Text( asName ) )
	return Span( [ nameView,  _space,  _keyword( 'as' ),  _space,  asNameView ] )


def moduleContentImport(name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.importStyle, Text( name ) )


def moduleContentImportAs(name, asName):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.importStyle, Text( name ) )
	asNameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.importStyle, Text( asName ) )
	return Span( [ nameView,  _space,  _keyword( 'as' ),  _space,  asNameView ] )


def importStmt(modules):
	moduleElements = []
	if len( modules ) > 0:
		for m in modules[:-1]:
			moduleElements.extend( [ m,  _comma,  _space,  _lineBreak ] )
		moduleElements.append( modules[-1] )
	return Span( [ _keyword( 'import' ), _space, ParagraphIndentMatchSpan( moduleElements ) ] )


def fromImportStmt(module, imports):
	importElements = []
	if len( imports ) > 0:
		for i in imports[:-1]:
			importElements.extend( [ i,  _comma,  _space,  _lineBreak ] )
		importElements.append( imports[-1] )
	return Span( [ _keyword( 'from' ), _space, module, _space,
	               _keyword( 'import' ), _space, ParagraphIndentMatchSpan( importElements ) ] )


def fromImportAllStmt(module):
	return Span( [ _keyword( 'from' ), _space, module, _space,
	               _keyword( 'import' ), _space,  _asterisk ] )


def globalVar(name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.varStyle, Text( name ) )


def globalStmt(vars):
	varElements = []
	if len( vars ) > 0:
		for v in vars[:-1]:
			varElements.extend( [ v,  _comma,  _space,  _lineBreak ] )
		varElements.append( vars[-1] )
	return Span( [ _keyword( 'global' ),  _space ]  +  varElements )



def execStmt(source, globals, locals):
	elements = [ _keyword( 'exec' ),  _space,  source ]
	if globals is not None:
		elements.extend( [ _space,  _keyword( 'in' ),  _space,  _lineBreak,  globals ] )
	if locals is not None:
		elements.extend( [ _comma,  _space,  _lineBreak,  locals ] )
	return Span( elements )






def printStmt(destination, values):
	elements = [ _keyword( 'print' ) ]
	if destination is not None  or  len( values ) > 0:
		elements.append( _space )
	if destination is not None:
		elements.extend( [ _doubleRightChevron, _space, destination ] )
		if len( values ) > 0:
			elements.extend( [ _comma, _space, _lineBreak ] )
	bFirst = True
	for v in values:
		if not bFirst:
			elements.extend( [ _comma, _space, _lineBreak ] )
		elements.append( v )
		bFirst = False
	return Span( elements )




#
#
# COMPOUND STATEMENT HEADERS
#
#

# If statement
def ifStmtHeader(condition):
	return Span( [ _keyword( 'if' ),  _space,  condition,  _colon ] )



# Elif statement
def elifStmtHeader(condition):
	return Span( [ _keyword( 'elif' ),  _space,  condition,  _colon ] )




# Else statement
def elseStmtHeader():
	return Span( [ _keyword( 'else' ),  _colon ] )


# While statement
def whileStmtHeader(condition):
	return Span( [ _keyword( 'while' ),  _space,  condition,  _colon ] )


# For statement
def forStmtHeader(target, source):
	return Span( [ _keyword( 'for' ),  _space,  target,  _space,
	               _keyword( 'in' ),  _space,  _lineBreak,
	               source,  _colon ] )


def tryStmtHeader():
	return Span( [ _keyword( 'try' ),  _colon ] )


def exceptStmtHeader(exception, target):
	elements = [ _keyword( 'except' ) ]
	if exception is not None:
		elements.extend( [ _space,  exception ] )
	if target is not None:
		elements.extend( [ _space, _keyword( 'as' ),  _space,  _lineBreak,  target ] )
	elements.append( _colon )
	return Span( elements )


def finallyStmtHeader():
	return Span( [ _keyword( 'finally' ),  _colon ] )


def withContext(expr, target):
	elements = [ expr ]
	if target is not None:
		elements.extend( [ _space,  _keyword( 'as' ),  _space, target ] )
	return Span( elements )

def withStmtHeader(contexts):
	elements = [ _keyword( 'with' ),  _space ]
	first = True
	for ctx in contexts:
		if not first:
			elements.append( _comma )
			elements.append( _space )
		elements.append( ctx )
		first = False
	elements.append( _colon )
	return Span( elements )


def decoStmtHeader(name, args, bArgsTrailingSeparator):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.varStyle, Text( name ) )

	elements = [ _at, nameView ]
	if args is not None:
		argsSeq = SpanSequenceView( args, _openParen, _closeParen, _comma, _space, TrailingSeparator.ALWAYS   if bArgsTrailingSeparator   else TrailingSeparator.NEVER )
		argsView = ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, argsSeq )
		elements.append( argsView )
	return Span( elements )


def defStmtHeader(name, params, bParamsTrailingSeparator):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.varStyle, Text( name ) )

	elements = [ _keyword( 'def' ),  _space,  nameView,  _openParen ]
	if len( params ) > 0:
		paramsSeq = SpanSequenceView( params, None, None, _comma, _space, TrailingSeparator.ALWAYS   if bParamsTrailingSeparator   else TrailingSeparator.NEVER )
		paramsView = ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, paramsSeq )
		elements.append( paramsView )

	elements.extend( [ _closeParen,  _colon ] )
	return Span( elements )


def classStmtHeader(name, bases, bBasesTrailingSeparator):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.varStyle, Text( name ) )

	elements = [ _keyword( 'class' ),  _space,  nameView ]
	if bases is not None:
		basesSeq = SpanSequenceView( bases, None, None, _comma, _space, TrailingSeparator.ALWAYS   if bBasesTrailingSeparator   else TrailingSeparator.NEVER )
		basesView = ApplyStyleSheetFromAttribute( PythonEditorStyle.sequenceStyle, basesSeq )
		elements.extend( [ _space,  _openParen,  basesView,  _closeParen ] )
	elements.append( _colon )
	return Span( elements )




@PyPresCombinatorFn
def defStmtHeaderHighlight(ctx, style, header):
	highlightStyle = PythonEditorStyle._defStmtHeaderHighlightStyle.get( style )
	return highlightStyle.applyTo( Border( header ) ).present( ctx, style )

@PyPresCombinatorFn
def defStmtHighlight(ctx, style, header):
	highlightStyle = PythonEditorStyle._defStmtHighlightStyle.get( style )
	return highlightStyle.applyTo( Border( header ) ).present( ctx, style )


@PyPresCombinatorFn
def classStmtHeaderHighlight(ctx, style, header):
	highlightStyle = PythonEditorStyle._classStmtHeaderHighlightStyle.get( style )
	return highlightStyle.applyTo( Border( header ) ).present( ctx, style )

@PyPresCombinatorFn
def classStmtHighlight(ctx, style, header):
	highlightStyle = PythonEditorStyle._classStmtHighlightStyle.get( style )
	return highlightStyle.applyTo( Border( header ) ).present( ctx, style )






#
#
# COMMENT STATEMENT
#
#

def commentStmt(comment):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.commentStyle, Text( '#' + comment ) )




#
#
# Suites, blank lines
#
#

def suiteView(statements):
	return Column( statements )


def blankLine():
	return Text( '' )


#
#
# STRUCTURE STATEMENTS
#
#

def indentElement():
	return Blank()

def dedentElement():
	return Blank()

@PyPresCombinatorFn
def indentedBlock(ctx, style, indentElement, lines, dedentElement):
	blockIndentation = style.get( PythonEditorStyle.blockIndentation )
	return Column( [ indentElement ]  +  list(lines)  +  [ dedentElement ] ).padX( blockIndentation, 0.0 ).present( ctx, style )

def compoundStmt(components):
	return Column( components )



#
#
# MISC
#
#

@PyPresCombinatorFn
def badIndentedBlock(ctx, style, indentElement, lines, dedentElement):
	rectStyle = PythonEditorStyle._badIndentationRectangleStyle.get( style )
	blockIndentation = style.get( PythonEditorStyle.blockIndentation )
	
	block = Column( [ indentElement ]  +  list(lines)  +  [ dedentElement ] )

	return Row( [ rectStyle.applyTo( Box( blockIndentation, 0.0 ).alignVExpand() ), block ] ).alignHPack().present( ctx, style )

def statementLine(statement):
	segment = Segment( statement )
	newLine = Whitespace( '\n' )
	return Paragraph( [ segment, newLine ] ).alignHPack()

def specialFormStatementLine(statement):
	segment = Segment( statement )
	return Paragraph( [ segment ] ).alignHPack()






