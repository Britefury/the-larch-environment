##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from java.awt import Font, Color

from Britefury.Kernel.Lerp import lerp, lerpColour

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.ListView import *
from BritefuryJ.DocPresent.Border import *

from GSymCore.Languages.Utils.PrecedenceStyleSheet import PrecedenceStyleSheetText




class PythonEditorStyleSheet (StyleSheet):
	MODE_DISPLAYCONTENTS = 0
	MODE_EDITEXPRESSION = 1
	MODE_EDITSTATEMENT = 2


	def __init__(self):
		super( PythonEditorStyleSheet, self ).__init__()
		
		self._keywordMap = {}

		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance.withParagraphIndentation( 60.0 ) )
		
		self.initAttr( 'precedenceParens', PrecedenceStyleSheetText.instance )
		self.initAttr( 'parser', None )
		self.initAttr( 'editMode', None )
		
		
		self.initAttr( 'keywordAttrs', AttributeSet( font=Font( 'SansSerif', Font.BOLD, 14 ), foreground=Color( 0.25, 0.0, 0.5 ), textSmallCaps=True ) )
		self.initAttr( 'literalFormatAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color( 0.0, 0.25, 0.25 ) ) )
		self.initAttr( 'quotationAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color( 0.0, 0.0, 0.5 ) ) )
		self.initAttr( 'stringLiteralAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color( 0.25, 0.0, 0.5 ) ) )
		self.initAttr( 'numLiteralAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color( 0.0, 0.5, 0.5 ) ) )
		self.initAttr( 'punctuationAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color( 0.0, 0.0, 1.0 ) ) )
		self.initAttr( 'delimAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color( 0.0, 0.0, 1.0 ) ) )
		self.initAttr( 'targetAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color.black ) )
		self.initAttr( 'varAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color.black ) )
		self.initAttr( 'attributeAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color.black ) )
		self.initAttr( 'kwNameAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color.black ) )
		self.initAttr( 'operatorAttrs', AttributeSet( font=Font( 'SansSerif', Font.BOLD, 14 ), foreground=Color( 0.0, 0.5, 0.0 ) ) )
		self.initAttr( 'paramAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color.black ) )
		self.initAttr( 'importAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color.black ) )
		self.initAttr( 'commentAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color( 0.4, 0.4, 0.4 ) ) )
		self.initAttr( 'unparseableAttrs', AttributeSet( font=Font( 'SansSerif', Font.PLAIN, 14 ), foreground=Color.black, textSquiggleUnderlinePaint=Color.red ) )
		
		self.initAttr( 'solidHighlightRounding', 3.0 )
		self.initAttr( 'outlineHighlightThickness', 2.0 )
		self.initAttr( 'outlineHighlightInset', 2.0 )
		self.initAttr( 'outlineHighlightRounding', 5.0 )
		
		self.initAttr( 'defStmtHighlightColour', Color( 0.420, 0.620, 0.522 ) )
		self.initAttr( 'classStmtHighlightColour', Color( 0.522, 0.420, 0.620 ) )
		self.initAttr( 'badIndentationHighlightColour', Color.red )

		self.initAttr( 'comprehensionSpacing', 15.0 )
		self.initAttr( 'conditionalSpacing', 15.0 )
		self.initAttr( 'blockIndentation', 30.0 )
		
		self._initKeywordAttrs( [ 'as', 'assert', 'break', 'class', 'continue', 'def', 'del', 'elif', 'else', 'except', 'exec', 'finally', 'for', 'from', 'global', 'lambda', 'if', 'import', 'in', 'pass', 'print', 'raise', 'return', 'try', 'while', 'yield' ] )
	
	
	
	def newInstance(self):
		return PythonEditorStyleSheet()
		
		
		
	def _initKeywordAttrs(self, keywords):
		for keyword in keywords:
			self.initAttr( keyword + 'KeywordText', keyword[0].upper() + keyword[1:] )
			self.initAttr( keyword + 'KeywordContent', keyword )
			self._keywordMap[keyword] = ( keyword + 'KeywordText', keyword + 'KeywordContent' )

		


	def withOuterPrecedence(self, outerPrecedence):
		return self.withAttr( 'precedenceParens', self['precedenceParens'].withOuterPrecedence( outerPrecedence ) )
		
	def withParser(self, parser):
		return self.withAttr( 'parser', parser )
		
	def withEditMode(self, editMode):
		return self.withAttr( 'editMode', editMode )
	
	
	def withPythonState(self, outerPrecedence, parser, editMode=MODE_DISPLAYCONTENTS):
		return self.withAttrs( outerPrecedence=outerPrecedence, parser=parser, editMode=editMode )
	
	
	
	def withKeywordAttrs(self, attrs):
		return self.withAttr( 'keywordAttrs', attrs )

	def withLiteralFormatAttrs(self, attrs):
		return self.withAttr( 'literalFormatAttrs', attrs )

	def withQuotationAttrs(self, attrs):
		return self.withAttr( 'quotationAttrs', attrs )

	def withStringLiteralAttrs(self, attrs):
		return self.withAttr( 'stringLiteralAttrs', attrs )

	def withNumLiteralAttrs(self, attrs):
		return self.withAttr( 'numLiteralAttrs', attrs )

	def withPunctuationAttrs(self, attrs):
		return self.withAttr( 'punctuationAttrs', attrs )

	def withDelimAttrs(self, attrs):
		return self.withAttr( 'delimAttrs', attrs )

	def withTargetAttrs(self, attrs):
		return self.withAttr( 'targetAttrs', attrs )

	def withVarAttrs(self, attrs):
		return self.withAttr( 'varAttrs', attrs )
	
	def withAttributeAttrs(self, attrs):
		return self.withAttr( 'attributeAttrs', attrs )
	
	def withKWNameAttrs(self, attrs):
		return self.withAttr( 'kwNameAttrs', attrs )
	
	def withOperatorAttrs(self, attrs):
		return self.withAttr( 'operatorAttrs', attrs )
	
	def withParamAttrs(self, attrs):
		return self.withAttr( 'paramAttrs', attrs )
	
	def withImportAttrs(self, attrs):
		return self.withAttr( 'importAttrs', attrs )
	
	def withCommentAttrs(self, attrs):
		return self.withAttr( 'commentAttrs', attrs )
	
	def withUnparseableAttrs(self, attrs):
		return self.withAttr( 'unparseableAttrs', attrs )
	
	
	def withSolidHighlightRounding(self, rounding):
		return self.withAttr( 'solidHighlightRounding', rounding )
	
	def withOutlineHighlightThickness(self, thickness):
		return self.withAttr( 'outlineHighlightThickness', thickness )
	
	def withOutlineHighlightInset(self, inset):
		return self.withAttr( 'outlineHighlightInset', inset )
	
	def withOutlineHighlightRounding(self, rounding):
		return self.withAttr( 'outlineHighlightRounding', rounding )
	
	
	def withDefStmtHighlightColour(self, colour):
		return self.withAttr( 'defStmtHighlightColour', colour )
	
	def withClassStmtHighlightColour(self, colour):
		return self.withAttr( 'classStmtHighlightColour', colour )
	
	def withBadIndentationHighlightColour(self, colour):
		return self.withAttr( 'badIndentationHighlightColour', colour )
	
	
	def withComprehensionSpacing(self, spacing):
		return self.withAttr( 'comprehensionSpacing', spacing )

	def withConditionalSpacing(self, spacing):
		return self.withAttr( 'conditionalSpacing', spacing )

		
	
	
	@StyleSheetDerivedPyAttrFn
	def _keywordStyle(self):
		return self['primitiveStyle'].withAttrSet( self['keywordAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _literalFormatStyle(self):
		return self['primitiveStyle'].withAttrSet( self['literalFormatAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _quotationStyle(self):
		return self['primitiveStyle'].withAttrSet( self['quotationAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _stringLiteralStyle(self):
		return self['primitiveStyle'].withAttrSet( self['stringLiteralAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _numLiteralStyle(self):
		return self['primitiveStyle'].withAttrSet( self['numLiteralAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _punctuationStyle(self):
		return self['primitiveStyle'].withAttrSet( self['punctuationAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _delimStyle(self):
		return self['primitiveStyle'].withAttrSet( self['delimAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _targetStyle(self):
		return self['primitiveStyle'].withAttrSet( self['targetAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _varStyle(self):
		return self['primitiveStyle'].withAttrSet( self['varAttrs'] )

	@StyleSheetDerivedPyAttrFn
	def _attributeStyle(self):
		return self['primitiveStyle'].withAttrSet( self['attributeAttrs'] )

	@StyleSheetDerivedPyAttrFn
	def _kwNameStyle(self):
		return self['primitiveStyle'].withAttrSet( self['kwNameAttrs'] )

	@StyleSheetDerivedPyAttrFn
	def _operatorStyle(self):
		return self['primitiveStyle'].withAttrSet( self['operatorAttrs'] )

	@StyleSheetDerivedPyAttrFn
	def _paramStyle(self):
		return self['primitiveStyle'].withAttrSet( self['paramAttrs'] )

	@StyleSheetDerivedPyAttrFn
	def _importStyle(self):
		return self['primitiveStyle'].withAttrSet( self['importAttrs'] )

	@StyleSheetDerivedPyAttrFn
	def _commentStyle(self):
		return self['primitiveStyle'].withAttrSet( self['commentAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def _unparseableStyle(self):
		return self['primitiveStyle'].withAttrSet( self['unparseableAttrs'] )

	
	

	def _solidHighlightBorder(self, colour):
		solidHighlightRounding = self['solidHighlightRounding']
		colour = lerpColour( colour, Color.white, 0.8 )
		return EmptyBorder( 0.0, 0.0, 0.0, 0.0, solidHighlightRounding, solidHighlightRounding, colour )
	
	def _outlineHighlightBorder(self, colour):
		thickness = self['outlineHighlightThickness']
		inset = self['outlineHighlightInset']
		rounding = self['outlineHighlightRounding']
		return SolidBorder( thickness, inset, rounding, rounding, colour, None )
	
	
	@StyleSheetDerivedPyAttrFn
	def _defStmtHeaderHighlightStyle(self):
		border = self._solidHighlightBorder( self['defStmtHighlightColour'] )
		return self['primitiveStyle'].withAttrs( border=border )

	@StyleSheetDerivedPyAttrFn
	def _defStmtHighlightStyle(self):
		border = self._outlineHighlightBorder( self['defStmtHighlightColour'] )
		return self['primitiveStyle'].withAttrs( border=border )

	@StyleSheetDerivedPyAttrFn
	def _classStmtHeaderHighlightStyle(self):
		border = self._solidHighlightBorder( self['classStmtHighlightColour'] )
		return self['primitiveStyle'].withAttrs( border=border )

	@StyleSheetDerivedPyAttrFn
	def _classStmtHighlightStyle(self):
		border = self._outlineHighlightBorder( self['classStmtHighlightColour'] )
		return self['primitiveStyle'].withAttrs( border=border )

	@StyleSheetDerivedPyAttrFn
	def _badIndentationHighlightStyle(self):
		border = self._outlineHighlightBorder( self['badIndentationHighlightColour'] )
		return self['primitiveStyle'].withAttrs( border=border )

	
	@StyleSheetDerivedPyAttrFn
	def _tupleListViewLayout(self):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		delimStyle = self._delimStyle()
		
		span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( True ).withAddParagraphIndentMarkers( True )
		
		tupleListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( lambda styleSheet, index, child: punctuationStyle.text( ',' ) ).withSpacingFactory( lambda styleSheet: primitiveStyle.text( ' ' ) )
		tupleListViewStyle = tupleListViewStyle.withBeginDelimFactory( lambda styleSheet: delimStyle.text( '(' ) ).withEndDelimFactory( lambda styleSheet: delimStyle.text( ')' ) )
		tupleListViewStyle = tupleListViewStyle.withListLayout( span_listViewLayout )
		
		return tupleListViewStyle
		
	
	@StyleSheetDerivedPyAttrFn
	def _listListViewLayout(self):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		delimStyle = self._delimStyle()
		
		span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( True ).withAddParagraphIndentMarkers( True )
		
		listListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( lambda styleSheet, index, child: punctuationStyle.text( ',' ) ).withSpacingFactory( lambda styleSheet: primitiveStyle.text( ' ' ) )
		listListViewStyle = listListViewStyle.withBeginDelimFactory( lambda styleSheet: delimStyle.text( '[' ) ).withEndDelimFactory( lambda styleSheet: delimStyle.text( ']' ) )
		listListViewStyle = listListViewStyle.withListLayout( span_listViewLayout )
		
		return listListViewStyle
		
	
	@StyleSheetDerivedPyAttrFn
	def _dictListViewLayout(self):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		delimStyle = self._delimStyle()
		
		span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( True ).withAddParagraphIndentMarkers( True )
		
		dictListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( lambda styleSheet, index, child: punctuationStyle.text( ',' ) ).withSpacingFactory( lambda styleSheet: primitiveStyle.text( ' ' ) )
		dictListViewStyle = dictListViewStyle.withBeginDelimFactory( lambda styleSheet: delimStyle.text( '{' ) ).withEndDelimFactory( lambda styleSheet: delimStyle.text( '}' ) )
		dictListViewStyle = dictListViewStyle.withListLayout( span_listViewLayout )
		
		return dictListViewStyle

	
	@StyleSheetDerivedPyAttrFn
	def _noParenTupleListViewLayout(self):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		delimStyle = self._delimStyle()
		
		span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( True ).withAddParagraphIndentMarkers( True )
		
		tupleListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( lambda styleSheet, index, child: punctuationStyle.text( ',' ) ).withSpacingFactory( lambda styleSheet: primitiveStyle.text( ' ' ) )
		tupleListViewStyle = tupleListViewStyle.withListLayout( span_listViewLayout )
		
		return tupleListViewStyle
		
	
	
	
		
	def applyParens(self, child, precedence, numAdditionalParens):
		primitiveStyleSheet = self['primitiveStyle']
		precedenceParens = self['precedenceParens']
		return precedenceParens.applyParens( primitiveStyleSheet, child, precedence, numAdditionalParens )
	
	
	def getOuterPrecedence(self):
		return self['precedenceParens']['outerPrecedence']
	
	
	def keywordText(self, keywordText, keywordContent):
		return self._keywordStyle().textWithContent( keywordText, keywordContent )
	
	def _keyword(self, keyword):
		textAttr, contentAttr = self._keywordMap[keyword]
		return self.keywordText( self[textAttr], self[contentAttr] )

	
	
	
	def suiteView(self, lineViews):
		primitiveStyleSheet = self['primitiveStyle']
		return primitiveStyleSheet.vbox( lineViews )
	
	
	
	def blankLine(self):
		return self['primitiveStyle'].text( '' )
	
	
	def unparseableText(self, text):
		unparseableStyle = self._unparseableStyle()
		return unparseableStyle.text( text )

	
	def unparsedElements(self, components):
		return self['primitiveStyle'].span( components )
	

	
	
	def stringLiteral(self, format, quotation, value):
		primitiveStyleSheet = self['primitiveStyle']
		boxContents = []
		
		if format is not None:
			boxContents.append( self._literalFormatStyle().text( format ) )
		
		quotationStyle = self._quotationStyle()
		boxContents.extend( [ quotationStyle.text( quotation ),  self._stringLiteralStyle().text( value ),  quotationStyle.text( quotation ) ] )
		
		return primitiveStyleSheet.hbox( boxContents )

	
	def intLiteral(self, format, value):
		primitiveStyleSheet = self['primitiveStyle']
		boxContents = [ self._numLiteralStyle().text( value ) ]
		if format is not None:
			boxContents.append( self._literalFormatStyle().text( format ) )
		
		return primitiveStyleSheet.hbox( boxContents )
		
		
	def floatLiteral(self, value):
		return self._numLiteralStyle().text( value )

	
	def imaginaryLiteral(self, value):
		return self._numLiteralStyle().text( value )
	
	
	def singleTarget(self, name):
		return self._targetStyle().text( name )
	
	
	def tupleTarget(self, items, bTrailingSeparator):
		return self._tupleListViewLayout().createListElement( items, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )

	
	def listTarget(self, items, bTrailingSeparator):
		return self._listListViewLayout().createListElement( items, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	
	
	def load(self, name):
		return self._varStyle().text( name )

	
	def tupleLiteral(self, items, bTrailingSeparator):
		return self._tupleListViewLayout().createListElement( items, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )

	
	def listLiteral(self, items, bTrailingSeparator):
		return self._listListViewLayout().createListElement( items, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	
	
	def comprehensionFor(self, target, source):
		primitiveStyle = self['primitiveStyle']
		return primitiveStyle.span( [ self._keyword( 'for' ),
		                              primitiveStyle.text( ' ' ),
		                              target,
		                              primitiveStyle.text( ' ' ),
		                              self._keyword( 'in' ),
		                              primitiveStyle.text( ' ' ),
		                              source ] )

	
	def comprehensionIf(self, condition):
		primitiveStyle = self['primitiveStyle']
		return primitiveStyle.span( [ self._keyword( 'if' ),
		                              primitiveStyle.text( ' ' ),
		                              condition ] )
	
	
	def listComp(self, resultExpr, comprehensionItems):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		delimStyle = self._delimStyle()
		comprehensionSpacing = self['comprehensionSpacing']
		itemViewsSpaced = []
		if len( comprehensionItems ) > 0:
			for x in comprehensionItems[:-1]:
				itemViewsSpaced.append( x )
				itemViewsSpaced.append( primitiveStyle.lineBreak( primitiveStyle.whitespace( ' ', comprehensionSpacing ) ) )
			itemViewsSpaced.append( comprehensionItems[-1] )
		return primitiveStyle.span( [ delimStyle.text( '[' ),  resultExpr,  primitiveStyle.whitespace( ' ', comprehensionSpacing ) ]  +  itemViewsSpaced  +  [ delimStyle.text( ']' ) ] )

	
	def genExpr(self, resultExpr, comprehensionItems):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		delimStyle = self._delimStyle()
		comprehensionSpacing = self['comprehensionSpacing']
		itemViewsSpaced = []
		if len( comprehensionItems ) > 0:
			for x in comprehensionItems[:-1]:
				itemViewsSpaced.append( x )
				itemViewsSpaced.append( primitiveStyle.lineBreak( primitiveStyle.whitespace( ' ', comprehensionSpacing ) ) )
			itemViewsSpaced.append( comprehensionItems[-1] )
		return primitiveStyle.span( [ delimStyle.text( '(' ),  resultExpr,  primitiveStyle.whitespace( ' ', comprehensionSpacing ) ]  +  itemViewsSpaced  +  [ delimStyle.text( ')' ) ] )
	
	
	def dictKeyValuePair(self, key, value):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ key, punctuationStyle.text( ':' ), value ] )
	
	
	def dictLiteral(self, items, bTrailingSeparator):
		return self._dictListViewLayout().createListElement( items, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	
	
	def yieldExpr(self, value):
		primitiveStyle = self['primitiveStyle']
		return primitiveStyle.span( [ self._keyword( 'yield' ),
		                              primitiveStyle.text( ' ' ),
		                              value ] )
	
	
	def attributeRef(self, target, name):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		attributeStyle = self._attributeStyle()
		return primitiveStyle.span( [ target, punctuationStyle.text( '.' ), attributeStyle.text( name ) ] )
		
		
	def subscriptSlice(self, lower, upper):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		lower = [ lower ]   if lower is not None   else []
		upper = [ upper ]   if upper is not None   else []
		return primitiveStyle.span( lower + [ punctuationStyle.text( ':' ), primitiveStyle.lineBreak() ] + upper )

	
	def subscriptLongSlice(self, lower, upper, stride):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		lower = [ lower ]   if lower is not None   else []
		upper = [ upper ]   if upper is not None   else []
		stride = [ stride ]   if stride is not None   else []
		return primitiveStyle.span( lower + [ punctuationStyle.text( ':' ), primitiveStyle.lineBreak() ] + upper + [ punctuationStyle.text( ':' ), primitiveStyle.lineBreak() ] + stride )
	
	
	def subscriptEllipsis(self):
		punctuationStyle = self._punctuationStyle()
		return punctuationStyle.text( '...' )
	
	
	def subscriptTuple(self, items, trailingSeparator):
		return self._noParenTupleListViewLayout().createListElement( items, TrailingSeparator.ALWAYS   if bTrailingSeparator   else TrailingSeparator.NEVER )
	
	
	def subscript(self, target, index):
		primitiveStyle = self['primitiveStyle']
		delimStyle = self._delimStyle()
		return primitiveStyle.span( [ target, delimStyle.text( '[' ), index, delimStyle.text( ']' ) ] )
		

	
	def callKWArg(self, name, value):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		kwNameStyle = self._kwNameStyle()
		return primitiveStyle.span( [ kwNameStyle.text( name ), punctuationStyle.text( '=' ), value ] )

	
	def callArgList(self, value):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ punctuationStyle.text( '*' ), value ] )

	
	def callKWArgList(self, value):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ punctuationStyle.text( '**' ), value ] )
	
	

	def call(self, target, args, bArgsTrailingSeparator):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		delimStyle = self._delimStyle()
		argElements = []
		if len( args ) > 0:
			argElements.append( primitiveStyle.text( ' ' ) )
			argElements.append( primitiveStyle.paragraphIndentMarker() )
			for a in args[:-1]:
				argElements.append( a )
				argElements.append( punctuationStyle.text( ',' ) )
				argElements.append( primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) )
			argElements.append( args[-1] )
			if bArgsTrailingSeparator:
				argElements.append( punctuationStyle.text( ',' ) )
			argElements.append( primitiveStyle.paragraphDedentMarker() )
			argElements.append( primitiveStyle.text( ' ' ) )
		return primitiveStyle.span( [ target, delimStyle.text( '(' ) ]  +  argElements  +  [ delimStyle.text( ')' ) ] )

	
	
	def pow(self, x, y):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		xElement = primitiveStyle.paragraph( [ x ] )
		yElement = primitiveStyle.paragraph( [ punctuationStyle.text( '**' ), primitiveStyle.text( ' ' ), y ] )
		return primitiveStyle.scriptRSuper( xElement, yElement )

	
	def spanPrefixOp(self, x, op):
		primitiveStyle = self['primitiveStyle']
		opView = self._operatorStyle().text( op )
		return primitiveStyle.span( [ opView, x ] )

	
	def spanBinOp(self, x, y, op):
		primitiveStyle = self['primitiveStyle']
		opView = self._operatorStyle().text( op )
		return primitiveStyle.span( [ x, primitiveStyle.text( ' ' ), opView, primitiveStyle.text( ' ' ), y ] )
	
	
	def spanCmpOp(self, op, y):
		primitiveStyle = self['primitiveStyle']
		opView = self._operatorStyle().text( op )
		return primitiveStyle.span( [ primitiveStyle.text( ' ' ), opView, primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ), y ] )


	def div(self, x, y, fractionBarContent):
		return self._operatorStyle().fraction( x, y, fractionBarContent )
	
	
	def compare(self, x, cmpOps):
		primitiveStyle = self['primitiveStyle']
		return primitiveStyle.span( [ x ]  +  cmpOps )
		
		
	def simpleParam(self, name):
		return self._paramStyle().text( name )
	
	
	def defaultValueParam(self, name, defaultValue):
		primitiveStyle = self['primitiveStyle']
		paramStyle = self._paramStyle()
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ paramStyle.text( name ), punctuationStyle.text( '=' ), defaultValue ] )
	
	
	def paramList(self, name):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		paramStyle = self._paramStyle()
		return primitiveStyle.span( [ punctuationStyle.text( '*' ), paramStyle.text( name ) ] )
	
	
	def kwParamList(self, name):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		paramStyle = self._paramStyle()
		return primitiveStyle.span( [ punctuationStyle.text( '*' ), paramStyle.text( name ) ] )

	
	def lambdaExpr(self, params, bParamsTrailingSeparator, expr):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		
		elements = []
		if len( params ) > 0:
			elements.append( primitiveStyle.paragraphIndentMarker() )
			for p in params[:-1]:
				elements.append( p )
				elements.append( punctuationStyle.text( ',' ) )
				elements.append( primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) )
			elements.append( params[-1] )
			if bParamsTrailingSeparator:
				elements.append( punctuationStyle.text( ',' ) )
				elements.append( primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) )
			elements.append( primitiveStyle.paragraphDedentMarker() )
		
		return primitiveStyle.span( [ self._keyword( 'lambda' ),  primitiveStyle.text( ' ' ) ]  +  elements  +  \
		                            [ punctuationStyle.text( ':' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ), expr ] )


	def conditionalExpr(self, condition, expr, elseExpr):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		conditionalSpacing = self['conditionalSpacing']
		
		return primitiveStyle.span( [ expr,   primitiveStyle.lineBreak( primitiveStyle.whitespace( '  ', conditionalSpacing ) ),
							 self._keyword( 'if' ), primitiveStyle.text( ' ' ), condition,   primitiveStyle.lineBreak( primitiveStyle.whitespace( '  ', conditionalSpacing ) ),
							 self._keyword( 'else' ), primitiveStyle.text( ' ' ), elseExpr ] )


	def exprStmt(self, expr):
		return expr
	
	
	def assertStmt(self, condition, fail):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		elements = [ self._keyword( 'assert' ), primitiveStyle.text( ' ' ), condition ]
		if fail is not None:
			elements.extend( [ punctuationStyle.text( ',' ), primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ), fail ] )
		return primitiveStyle.span( elements )


	def assignStmt(self, targets, value):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		targetElements = []
		for t in targets:
			targetElements.extend( [ t,  primitiveStyle.text( ' ' ),  punctuationStyle.text( '=' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) ] )
		return primitiveStyle.span( targetElements  +  [ value ] )


	def augAssignStmt(self, op, target, value):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		operatorStyle = self._operatorStyle()

		return primitiveStyle.span( [ target, primitiveStyle.text( ' ' ), operatorStyle.text( op ), primitiveStyle.text( ' ' ), value ] )
	
	
	def passStmt(self):
		return self._keyword( 'pass' )
	
	
	def delStmt(self, target):
		primitiveStyle = self['primitiveStyle']

		return primitiveStyle.span( [ self._keyword( 'del' ),  primitiveStyle.text( ' ' ),  target ] )
	
	
	def returnStmt(self, value):
		primitiveStyle = self['primitiveStyle']

		return primitiveStyle.span( [ self._keyword( 'return' ),  primitiveStyle.text( ' ' ),  value ] )
	
	
	def yieldStmt(self, value):
		primitiveStyle = self['primitiveStyle']

		return primitiveStyle.span( [ self._keyword( 'yield' ),  primitiveStyle.text( ' ' ),  value ] )


	def raiseStmt(self, excType, excValue, traceback):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		xs = [ x   for x in [ excType, excValue, traceback ]  if x is not None ]
		xElements = []
		if len( xs ) > 0:
			for x in xs[:-1]:
				xElements.extend( [ x,  punctuationStyle.text( ',' ),  primitiveStyle.text( ' ' ) ] )
			xElements.append( xs[-1] )
		
		return primitiveStyle.span( [ self._keyword( 'raise' ),  primitiveStyle.text( ' ' ) ] + xElements )


	def breakStmt(self):
		return self._keyword( 'break' )


	def continueStmt(self):
		return self._keyword( 'continue' )


	def relativeModule(self, name):
		importStyle = self._importStyle()
		return importStyle.text( name )

	
	def moduleImport(self, name):
		importStyle = self._importStyle()
		return importStyle.text( name )


	def moduleImportAs(self, name, asName):
		primitiveStyle = self['primitiveStyle']
		importStyle = self._importStyle()
		return primitiveStyle.span( [ importStyle.text( name ),  primitiveStyle.text( ' ' ),  self._keyword( 'as' ),  primitiveStyle.text( ' ' ),  importStyle.text( asName ) ] )


	def moduleContentImport(self, name):
		importStyle = self._importStyle()
		return importStyle.text( name )


	def moduleContentImportAs(self, name, asName):
		primitiveStyle = self['primitiveStyle']
		importStyle = self._importStyle()
		return primitiveStyle.span( [ importStyle.text( name ),  primitiveStyle.text( ' ' ),  self._keyword( 'as' ),  primitiveStyle.text( ' ' ),  importStyle.text( asName ) ] )


	def importStmt(self, modules):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		moduleElements = [ primitiveStyle.paragraphIndentMarker() ]
		if len( modules ) > 0:
			for m in modules[:-1]:
				moduleElements.extend( [ m,  punctuationStyle.text( ',' ),  primitiveStyle.lineBreak( punctuationStyle.text( ' ' ) ) ] )
			moduleElements.append( modules[-1] )
		moduleElements.append( primitiveStyle.paragraphDedentMarker() )
		return primitiveStyle.span( [ self._keyword( 'import' ), primitiveStyle.text( ' ' ) ]  +  moduleElements )


	def fromImportStmt(self, module, imports):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		importElements = [ primitiveStyle.paragraphIndentMarker() ]
		if len( imports ) > 0:
			for i in imports[:-1]:
				importElements.extend( [ i,  punctuationStyle.text( ',' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) ] )
			importElements.append( imports[-1] )
		importElements.append( primitiveStyle.paragraphDedentMarker() )
		return primitiveStyle.span( [ self._keyword( 'from' ), primitiveStyle.text( ' ' ), module, primitiveStyle.text( ' ' ),
							self._keyword( 'import' ), primitiveStyle.text( ' ' ) ]  +  importElements )

	
	def fromImportAllStmt(self, module):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		return primitiveStyle.span( [ self._keyword( 'from' ), primitiveStyle.text( ' ' ), module, primitiveStyle.text( ' ' ),
							self._keyword( 'import' ), primitiveStyle.text( ' ' ),  punctuationStyle.text( '*' ) ] )


	def globalVar(self, name):
		varStyle = self._varStyle()
		return varStyle.text( name )


	def globalStmt(self, vars):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		
		varElements = []
		if len( vars ) > 0:
			for v in vars[:-1]:
				varElements.extend( [ v,  punctuationStyle.text( ',' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) ] )
			varElements.append( vars[-1] )
		return primitiveStyle.span( [ self._keyword( 'global' ),  primitiveStyle.text( ' ' ) ]  +  varElements )



	def execStmt(self, source, locals, globals):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		elements = [ self._keyword( 'exec' ),  primitiveStyle.text( ' ' ),  source ]
		if locals is not None:
			elements.extend( [ primitiveStyle.text( ' ' ),  self._keyword( 'in' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ),  locals ] )
		if globals is not None:
			elements.extend( [ punctuationStyle.text( ',' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ),  globals ] )
		return primitiveStyle.span( elements )






	def printStmt(self, destination, values):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()

		elements = [ self._keyword( 'print' ) ]
		if destination is not None  or  len( values ) > 0:
			elements.append( primitiveStyle.text( ' ' ) )
		if destination is not None:
			elements.extend( [ punctuationStyle.text( '>>' ), primitiveStyle.text( ' ' ), destination ] )
			if len( values ) > 0:
				elements.extend( [ punctuationStyle.text( ',' ), primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) ] )
		bFirst = True
		for v in values:
			if not bFirst:
				elements.extend( [ punctuationStyle.text( ',' ), primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ) ] )
			elements.append( v )
			bFirst = False
		return primitiveStyle.span( elements )
	
	
	
	
	#
	#
	# COMPOUND STATEMENT HEADERS
	#
	#

	# If statement
	def ifStmtHeader(self, condition):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ self._keyword( 'if' ),  primitiveStyle.text( ' ' ),  condition,  punctuationStyle.text( ':' ) ] )



	# Elif statement
	def elifStmtHeader(self, condition):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ self._keyword( 'elif' ),  primitiveStyle.text( ' ' ),  condition,  punctuationStyle.text( ':' ) ] )




	# Else statement
	def elseStmtHeader(self):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ self._keyword( 'else' ),  punctuationStyle.text( ':' ) ] )


	# While statement
	def whileStmtHeader(self, condition):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ self._keyword( 'while' ),  primitiveStyle.text( ' ' ),  condition,  punctuationStyle.text( ':' ) ] )


	# For statement
	def forStmtHeader(self, target, source):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ self._keyword( 'for' ),  primitiveStyle.text( ' ' ),  target,  primitiveStyle.text( ' ' ),
							    self._keyword( 'in' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ),
							    source,  punctuationStyle.text( ':' ) ] )


	def tryStmtHeader(self):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ self._keyword( 'try' ),  punctuationStyle.text( ':' ) ] )


	def exceptStmtHeader(self, exception, target):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		elements = [ self._keyword( 'except' ) ]
		if exception is not None:
			elements.extend( [ primitiveStyle.text( ' ' ),  exception ] )
		if target is not None:
			elements.extend( [ punctuationStyle.text( ',' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ),  target ] )
		elements.append( punctuationStyle.text( ':' ) )
		return primitiveStyle.span( elements )


	def finallyStmtHeader(self):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		return primitiveStyle.span( [ self._keyword( 'finally' ),  punctuationStyle.text( ':' ) ] )


	def withStmtHeader(self, expr, target):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		elements = [ self._keyword( 'with' ),  primitiveStyle.text( ' ' ),  exprView ]
		if target is not None:
			elements.extend( [ primitiveStyle.text( ' ' ),  self._keyword( 'as' ),  primitiveStyle.lineBreak( primitiveStyle.text( ' ' ) ),  target ] )
		elements.append( punctuationStyle.text( ':' ) )
		return primitiveStyle.span( elements )


	def decoStmtHeader(self, name, args, bArgsTrailingSeparator):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		varStyle = self._varStyle()
		delimStyle = self._delimStyle()
		
		elements = [ punctuationStyle.text( '@' ),  varStyle.text( name ) ]
		if args is not None:
			elements.append( delimStyle.text( '(' ) )
			if len( args ) > 0:
				for a in args[:-1]:
					elements.extend( [ a, punctuationStyle.text( ',' ), primitiveStyle.text( ' ' ) ] )
				elements.append( args[-1] )
				if bArgsTrailingSeparator:
					elements.extend( [ punctuationStyle.text( ',' ), primitiveStyle.text( ' ' ) ] )
			elements.append( delimStyle.text( ')' ) )
		return primitiveStyle.span( elements )


	def defStmtHeader(self, name, params, bParamsTrailingSeparator):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		varStyle = self._varStyle()
		delimStyle = self._delimStyle()

		elements = [ self._keyword( 'def' ),  primitiveStyle.text( ' ' ),  varStyle.text( name ),  delimStyle.text( '(' ) ]
		if len( params ) > 0:
			elements.append( primitiveStyle.paragraphIndentMarker() )
			for p in params[:-1]:
				elements.extend( [ p,  punctuationStyle.text( ',' ),  primitiveStyle.text( ' ' ) ] )
			elements.append( params[-1] )
			if bParamsTrailingSeparator:
				elements.extend( [ punctuationStyle.text( ',' ),  primitiveStyle.text( ' ' ) ] )
			elements.append( primitiveStyle.paragraphDedentMarker() )

		elements.extend( [ delimStyle.text( ')' ),  punctuationStyle.text( ':' ) ] )
		return primitiveStyle.span( elements )


	def classStmtHeader(self, name, bases, bBasesTrailingSeparator):
		primitiveStyle = self['primitiveStyle']
		punctuationStyle = self._punctuationStyle()
		varStyle = self._varStyle()

		elements = [ self._keyword( 'class' ),  primitiveStyle.text( ' ' ),  varStyle.text( name ) ]
		if bases is not None:
			delimStyle = self._delimStyle()
			basesListLayout = self._noParenTupleListViewLayout()

			trailingSep = TrailingSeparator.ALWAYS   if bBasesTrailingSeparator   else TrailingSeparator.NEVER
			elements.extend( [ delimStyle.text( '(' ),  basesListLayout.createListElement( bases, trailingSep ),  delimStyle.text( ')' ) ] )
		elements.append( punctuationStyle.text( ':' ) )
		return primitiveStyle.span( elements )
	
	
	
	
	def defStmtHeaderHighlight(self, header):
		highlightStyle = self._defStmtHeaderHighlightStyle()
		return highlightStyle.border( header )

	def defStmtHighlight(self, header):
		highlightStyle = self._defStmtHighlightStyle()
		return highlightStyle.border( header )


	def classStmtHeaderHighlight(self, header):
		highlightStyle = self._classStmtHeaderHighlightStyle()
		return highlightStyle.border( header )

	def classStmtHighlight(self, header):
		highlightStyle = self._classStmtHighlightStyle()
		return highlightStyle.border( header )


	

	
	
	#
	#
	# COMMENT STATEMENT
	#
	#
	
	def commentStmt(self, comment):
		commentStyle = self._commentStyle()
		return commentStyle.text( '#' + comment )

	
	
	
	#
	#
	# STRUCTURE STATEMENTS
	#
	#
	
	def indentElement(self):
		return self['primitiveStyle'].empty()
	
	def dedentElement(self):
		return self['primitiveStyle'].empty()
	
	def indentedBlock(self, indentElement, lines, dedentElement):
		primitiveStyleSheet = self['primitiveStyle']
		return primitiveStyleSheet.vbox( [ indentElement ]  +  lines  +  [ dedentElement ] ).padX( self['blockIndentation'], 0.0 )
	
	def compoundStmt(self, components):
		return self['primitiveStyle'].vbox( components )
	
	
	
	
	
	#
	#
	# MISC
	#
	#
	
	def badIndentation(self, child):
		badIndentationStyle = self._badIndentationHighlightStyle()
		return badIndentationStyle.border( child )
	
	def statementLine(self, statement):
		primitiveStyleSheet = self['primitiveStyle']
		segment = primitiveStyleSheet.segment( True, True, statement )
		newLine = primitiveStyleSheet.whitespace( '\n' )
		return primitiveStyleSheet.paragraph( [ segment, newLine ] )





PythonEditorStyleSheet.instance = PythonEditorStyleSheet()

