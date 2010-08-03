##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from java.awt import Color

from Britefury.Kernel.Lerp import lerp, lerpColour

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Combinators import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.Combinators.Sequence import *

from GSymCore.Languages.Utils.Precedence import applyParens


EDITMODE_DISPLAYCONTENTS = 0
EDITMODE_EDITEXPRESSION = 1
EDITMODE_EDITSTATEMENT = 2




class PythonEditorStyle (object):
	pythonEditor = AttributeNamespace( 'pythonEditor' )
	
	keywordStyle = InheritedAttributeNonNull( pythonEditor, 'keywordStyle', StyleSheet,
	                                          StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontBold, True ).withAttr( Primitive.fontSize, 14 )
	                                          .withAttr( Primitive.foreground, Color( 0.25, 0.0, 0.5 ) ).withAttr( Primitive.textSmallCaps, True ) )
	literalFormatStyle = InheritedAttributeNonNull( pythonEditor, 'literalFormatStyle', StyleSheet,
	                                                StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.0, 0.25, 0.25 ) ) )
	quotationStyle = InheritedAttributeNonNull( pythonEditor, 'quotationStyle', StyleSheet,
	                                            StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) ) )
	stringLiteralStyle = InheritedAttributeNonNull( pythonEditor, 'stringLiteralStyle', StyleSheet,
	                                                StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.25, 0.0, 0.5 ) ) )
	numLiteralStyle = InheritedAttributeNonNull( pythonEditor, 'numLiteralStyle', StyleSheet,
	                                             StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.0, 0.5, 0.5 ) ) )
	punctuationStyle = InheritedAttributeNonNull( pythonEditor, 'punctuationStyle', StyleSheet,
	                                              StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.0, 0.0, 1.0 ) ) )
	delimStyle = InheritedAttributeNonNull( pythonEditor, 'delimStyle', StyleSheet,
	                                        StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.0, 0.0, 1.0 ) ) )
	targetStyle = InheritedAttributeNonNull( pythonEditor, 'targetStyle', StyleSheet,
	                                         StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color.black ) )
	varStyle = InheritedAttributeNonNull( pythonEditor, 'varStyle', StyleSheet,
	                                      StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color.black ) )
	attributeStyle = InheritedAttributeNonNull( pythonEditor, 'attributeStyle', StyleSheet,
	                                            StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color.black ) )
	kwNameStyle = InheritedAttributeNonNull( pythonEditor, 'kwNameStyle', StyleSheet,
	                                         StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color.black ) )
	operatorStyle = InheritedAttributeNonNull( pythonEditor, 'operatorStyle', StyleSheet,
	                                           StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontBold, True )
	                                           .withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.0, 0.5, 0.0 ) ) )
	paramStyle = InheritedAttributeNonNull( pythonEditor, 'paramStyle', StyleSheet,
	                                        StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color.black ) )
	importStyle = InheritedAttributeNonNull( pythonEditor, 'importStyle', StyleSheet,
	                                         StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color.black ) )
	commentStyle = InheritedAttributeNonNull( pythonEditor, 'commentStyle', StyleSheet,
	                                          StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, Color( 0.4, 0.4, 0.4 ) ) )
	unparseableStyle = InheritedAttributeNonNull( pythonEditor, 'unparseableStyle', StyleSheet,
	                                              StyleSheet.instance.withAttr( Primitive.fontFace, 'Sans serif' ).withAttr( Primitive.fontSize, 14 )
	                                              .withAttr( Primitive.foreground, Color.black ).withAttr( Primitive.textSquiggleUnderlinePaint, Color.red ) )
	
	sequenceStyle = InheritedAttributeNonNull( pythonEditor, 'sequenceStyle', StyleSheet,
	                                              StyleSheet.instance.withAttr( Sequence.addLineBreaks, True ).withAttr( Sequence.addParagraphIndentMarkers, True ).withAttr( Sequence.addLineBreakCost, True ) )
	
	solidHighlightRounding = InheritedAttributeNonNull( pythonEditor, 'solidHighlightRounding', float, 3.0 )
	outlineHighlightThickness = InheritedAttributeNonNull( pythonEditor, 'outlineHighlightThickness', float, 2.0 )
	outlineHighlightInset = InheritedAttributeNonNull( pythonEditor, 'outlineHighlightInset', float, 2.0 )
	outlineHighlightRounding = InheritedAttributeNonNull( pythonEditor, 'outlineHighlightRounding', float, 5.0 )
	
	defStmtHighlightColour = InheritedAttributeNonNull( pythonEditor, 'defStmtHighlightColour', Color, Color( 0.420, 0.620, 0.522 ) )
	classStmtHighlightColour = InheritedAttributeNonNull( pythonEditor, 'classStmtHighlightColour', Color, Color( 0.522, 0.420, 0.620 ) )
	badIndentationHighlightColour = InheritedAttributeNonNull( pythonEditor, 'badIndentationHighlightColour', Color, Color.red )
	                                              
	comprehensionSpacing = InheritedAttributeNonNull( pythonEditor, 'comprehensionSpacing', float, 15.0 )
	conditionalSpacing = InheritedAttributeNonNull( pythonEditor, 'conditionalSpacing', float, 15.0 )
	blockIndentation = InheritedAttributeNonNull( pythonEditor, 'blockIndentation', float, 30.0 )
	
	
	@PyDerivedValueTable( pythonEditor )
	def _defStmtHeaderHighlightStyle(style):
		border = _solidHighlightBorder( style, style.get( PythonEditorStyle.defStmtHighlightColour ) )
		return style.withAttr( Primitive.border, border )

	@PyDerivedValueTable( pythonEditor )
	def _defStmtHighlightStyle(style):
		border = _outlineHighlightBorder( style, style.get( PythonEditorStyle.defStmtHighlightColour ) )
		return style.withAttr( Primitive.border, border )

	@PyDerivedValueTable( pythonEditor )
	def _classStmtHeaderHighlightStyle(style):
		border = _solidHighlightBorder( style, style.get( PythonEditorStyle.classStmtHighlightColour ) )
		return style.withAttr( Primitive.border, border )

	@PyDerivedValueTable( pythonEditor )
	def _classStmtHighlightStyle(style):
		border = _outlineHighlightBorder( style, style.get( PythonEditorStyle.classStmtHighlightColour ) )
		return style.withAttr( Primitive.border, border )

	@PyDerivedValueTable( pythonEditor )
	def _badIndentationHighlightStyle(style):
		border = _outlineHighlightBorder( style, style.get( PythonEditorStyle.badIndentationHighlightColour ) )
		return style.withAttr( Primitive.border, border )




	
_keywordMap = {}
	
	
	
def _initKeywords(keywords):
	for keyword in keywords:
		keywordText = keyword[0].upper() + keyword[1:]
		_keywordMap[keyword] = ApplyStyleSheetFromAttribute( PythonEditorStyle.keywordStyle, Text( keywordText, keyword ) )

_initKeywords( [ 'as', 'assert', 'break', 'class', 'continue', 'def', 'del', 'elif', 'else', 'except', 'exec', 'finally', 'for', 'from', 'global', 'lambda', 'if', 'import', 'in', 'pass', 'print', 'raise', 'return', 'try', 'while', 'yield' ] )









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

def applyPythonParens(expr, precedence, numAdditionalParens, inheritedState):
	outerPrecedence = inheritedState['outerPrecedence']
	return applyParens( expr, precedence, outerPrecedence, numAdditionalParens, _openParen, _closeParen )

def getOuterPrecedence(inheritedState):
	return inheritedState['outerPrecedence']
	


#
#
# Suites, blank lines
#
#

def suiteView(statements):
	return VBox( statements )


def blankLine():
	return Text( '' )


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

def stringLiteral(format, quotation, value):
	boxContents = []
	
	if format is not None:
		boxContents.append( ApplyStyleSheetFromAttribute( PythonEditorStyle.literalFormatStyle, Text( format ) ) )
	
	quotationPres = ApplyStyleSheetFromAttribute( PythonEditorStyle.quotationStyle, Text( quotation ) )
	valuePres = ApplyStyleSheetFromAttribute( PythonEditorStyle.stringLiteralStyle, Text( value ) )
	boxContents.extend( [ quotationPres,  valuePres,  quotationPres ] )
	
	return HBox( boxContents )


def intLiteral(format, value):
	valuePres = ApplyStyleSheetFromAttribute( PythonEditorStyle.numLiteralStyle, Text( value ) )
	boxContents = [ valuePres ]
	if format is not None:
		boxContents.append( ApplyStyleSheetFromAttribute( PythonEditorStyle.literalFormatStyle, Text( format ) ) )
	
	return HBox( boxContents )
	
	
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


def yieldExpr(value):
	return Span( [ _keyword( 'yield' ),
                                      _space,
                                      value ] )


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
	return Span( [ target, _lineBreak, _openBracket, index, _closeBracket ] )
	


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
		argElements.append( ParagraphIndentMarker() )
		for a in args[:-1]:
			argElements.append( a )
			argElements.append( _comma )
			argElements.append( _space )
			argElements.append( _lineBreak )
		argElements.append( args[-1] )
		if bArgsTrailingSeparator:
			argElements.append( _comma )
		argElements.append( ParagraphDedentMarker() )
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
	return LineBreakCostSpan( [ x ]  +  cmpOps )
	
	
def simpleParam(name):
	return ApplyStyleSheetFromAttribute( PythonEditorStyle.paramStyle, Text( name ) )


def defaultValueParam(name, defaultValue):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.paramStyle, Text( name ) )
	return Span( [ nameView, _equals, defaultValue ] )


def paramList(name):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.paramStyle, Text( name ) )
	return Span( [ _asterisk, nameView ] )


def kwParamList(name):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.paramStyle, Text( name ) )
	return Span( [ _doubleAsterisk, nameView ] )


def lambdaExpr(params, bParamsTrailingSeparator, expr):
	elements = []
	if len( params ) > 0:
		elements.append( ParagraphIndentMarker() )
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
		elements.append( ParagraphDedentMarker() )
	
	return LineBreakCostSpan( [ _keyword( 'lambda' ),  _space ]  +  elements  +  \
                                    [ _colon,  _space, _lineBreak, expr ] )


@PyPresCombinatorFn
def conditionalExpr(ctx, style, condition, expr, elseExpr):
	conditionalSpacing = style.get( PythonEditorStyle.conditionalSpacing )
	
	return LineBreakCostSpan( [ expr,   Whitespace( '  ', conditionalSpacing ),  _lineBreak,
                                                 _keyword( 'if' ), _space, condition,   Whitespace( '  ', conditionalSpacing ),  _lineBreak,
                                                 _keyword( 'else' ), _space, elseExpr ] ).present( ctx, style )


def exprStmt(expr):
	return expr


def assertStmt(condition, fail):
	elements = [ _keyword( 'assert' ), _space, condition ]
	if fail is not None:
		elements.extend( [ _comma, _space, _lineBreak, fail ] )
	return Span( elements )


def assignStmt(targets, value):
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
	return Span( [ _keyword( 'return' ),  _space,  value ] )


def yieldStmt(value):
	return Span( [ _keyword( 'yield' ),  _space,  value ] )


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
	moduleElements = [ ParagraphIndentMarker() ]
	if len( modules ) > 0:
		for m in modules[:-1]:
			moduleElements.extend( [ m,  _comma,  _space,  _lineBreak ] )
		moduleElements.append( modules[-1] )
	moduleElements.append( ParagraphDedentMarker() )
	return Span( [ _keyword( 'import' ), _space ]  +  moduleElements )


def fromImportStmt(module, imports):
	importElements = [ ParagraphIndentMarker() ]
	if len( imports ) > 0:
		for i in imports[:-1]:
			importElements.extend( [ i,  _comma,  _space,  _lineBreak ] )
		importElements.append( imports[-1] )
	importElements.append( ParagraphDedentMarker() )
	return Span( [ _keyword( 'from' ), _space, module, _space,
                                                _keyword( 'import' ), _space ]  +  importElements )


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
		elements.extend( [ _comma,  _space,  _lineBreak,  target ] )
	elements.append( _colon )
	return Span( elements )


def finallyStmtHeader():
	return Span( [ _keyword( 'finally' ),  _colon ] )


def withStmtHeader(expr, target):
	elements = [ _keyword( 'with' ),  _space,  exprView ]
	if target is not None:
		elements.extend( [ _space,  _keyword( 'as' ),  _space,  _lineBreak,  target ] )
	elements.append( _colon )
	return Span( elements )


def decoStmtHeader(name, args, bArgsTrailingSeparator):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.varStyle, Text( name ) )
	
	elements = [ _at, nameView ]
	if args is not None:
		elements.append( _openParen )
		if len( args ) > 0:
			for a in args[:-1]:
				elements.extend( [ a, _comma, _space ] )
			elements.append( args[-1] )
			if bArgsTrailingSeparator:
				elements.extend( [ _comma, _space ] )
		elements.append( _closeParen )
	return Span( elements )


def defStmtHeader(name, params, bParamsTrailingSeparator):
	nameView = ApplyStyleSheetFromAttribute( PythonEditorStyle.varStyle, Text( name ) )

	elements = [ _keyword( 'def' ),  _space,  nameView,  _openParen ]
	if len( params ) > 0:
		elements.append( ParagraphIndentMarker() )
		for p in params[:-1]:
			elements.extend( [ p,  _comma,  _space ] )
		elements.append( params[-1] )
		if bParamsTrailingSeparator:
			elements.extend( [ _comma,  _space ] )
		elements.append( ParagraphDedentMarker() )

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
# STRUCTURE STATEMENTS
#
#

def indentElement():
	return HiddenContent( '' )

def dedentElement():
	return HiddenContent( '' )

@PyPresCombinatorFn
def indentedBlock(ctx, style, indentElement, lines, dedentElement):
	blockIndentation = style.get( PythonEditorStyle.blockIndentation )
	return VBox( [ indentElement ]  +  lines  +  [ dedentElement ] ).padX( blockIndentation, 0.0 ).present( ctx, style )

def compoundStmt(components):
	return VBox( components )



#
#
# MISC
#
#
	
@PyPresCombinatorFn
def badIndentation(ctx, style, child):
	badIndentationStyle = PythonEditorStyle._badIndentationHighlightStyle.get( style )
	return badIndentationStyle.applyTo( Border( child ) ).present( ctx, style )

def statementLine(statement):
	segment = Segment( True, True, statement )
	newLine = Whitespace( '\n' )
	return Paragraph( [ segment, newLine ] )


	
	
	
	
