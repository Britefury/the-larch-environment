##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color

from BritefuryJ.AttributeTable import *
from BritefuryJ.StyleSheet import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.Sequence import *




class ExecutionStyle (object):
	pythonExecution = AttributeNamespace( 'pythonExecution' )
	
	labelStyle = InheritedAttributeNonNull( pythonExecution, 'labelStyle', StyleSheet,
	                                          StyleSheet.instance.withAttr( Primitive.fontSize, 10 ).withAttr( Primitive.foreground, Color.BLACK ) )
	
	stdOutStyle = InheritedAttributeNonNull( pythonExecution, 'stdOutStyle', StyleSheet,
	                                          StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), Color.WHITE ) ).withAttr( Primitive.foreground, Color( 0.0, 0.5, 0.0 ) ) )
	stdErrStyle = InheritedAttributeNonNull( pythonExecution, 'stdErrStyle', StyleSheet,
	                                          StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 1.0, 0.5, 0.0 ), Color.WHITE ) ).withAttr( Primitive.foreground, Color( 0.75, 0.375, 0.0 ) ) )
	exceptionBorderStyle = InheritedAttributeNonNull( pythonExecution, 'exceptionBorderStyle', StyleSheet,
	                                          StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.8, 0.0, 0.0 ), Color( 1.0, 0.9, 0.9 ) ) ) )
	resultBorderStyle = InheritedAttributeNonNull( pythonExecution, 'resultBorderStyle', StyleSheet,
	                                          StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.0, 0.8 ), Color.WHITE ) ) )

	resultBoxStyle = InheritedAttributeNonNull( pythonExecution, 'resultSpacing', StyleSheet, StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ) )


	@PyDerivedValueTable( pythonExecution )
	def _resultBoxStyle(style):
		resultSpacing = style.get( ExecutionStyle.resultSpacing )
		return style.withAttr( Primitive.columnSpacing, resultSpacing )




def _textLines(text, textStyleAttribute):
	return ApplyStyleSheetFromAttribute( textStyleAttribute, Column( [ StaticText( line )   for line in text.split( '\n' ) ] ) )

def _streamItem(item, textStyleAttribute, bUseDefaultPerspectiveForResult):
	if item.isStructural():
		resultView = InnerFragment( item.getStructuralValue() )
		if bUseDefaultPerspectiveForResult:
			resultView = ApplyPerspective.defaultPerspective( resultView )
		return resultView
	else:
		return _textLines( item.getTextValue(), textStyleAttribute )

def _streamLines(labelText, stream, textStyleAttribute, bUseDefaultPerspectiveForResult):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( labelText ) )
	lines = [ _streamItem( item, textStyleAttribute, bUseDefaultPerspectiveForResult )   for item in stream.getItems() ]
	return Column( [ label, Column( lines ).padX( 5.0, 0.0 ) ] )


def execStdout(text, bUseDefaultPerspectiveForResult):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdOutStyle, Border( _streamLines( 'STDOUT:', text, ExecutionStyle.stdOutStyle, bUseDefaultPerspectiveForResult ).alignHExpand() ).alignHExpand() )

def execStderr(text, bUseDefaultPerspectiveForResult):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdErrStyle, Border( _streamLines( 'STDERR:', text, ExecutionStyle.stdErrStyle, bUseDefaultPerspectiveForResult ).alignHExpand() ).alignHExpand() )
	
def execException(exceptionView):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( 'EXCEPTION:' ) )
	return ApplyStyleSheetFromAttribute( ExecutionStyle.exceptionBorderStyle, Border( Column( [ label, exceptionView.padX( 5.0, 0.0 ).alignHExpand() ] ).alignHExpand() ).alignHExpand() )

def execResult(resultView):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBorderStyle, Border( Bin( Paragraph( [ resultView ] ) ) ).alignHExpand() )


def executionResultBox(stdoutStream, stderrStream, exception, resultInTuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult):
	boxContents = []
	if stderrStream is not None:
		boxContents.append( execStderr( stderrStream, bUseDefaultPerspectiveForResult ) )
	if exception is not None:
		exceptionView = InnerFragment( exception ).alignHPack()
		if bUseDefaultPerspecitveForException:
			exceptionView = ApplyPerspective.defaultPerspective( exceptionView )
		boxContents.append( execException( exceptionView ) )
	if stdoutStream is not None:
		boxContents.append( execStdout( stdoutStream, bUseDefaultPerspectiveForResult ) )
	if resultInTuple is not None:
		resultView = InnerFragment( resultInTuple[0] ).alignHPack()
		if bUseDefaultPerspectiveForResult:
			resultView = ApplyPerspective.defaultPerspective( resultView )
		boxContents.append( execResult( resultView ) )
	
	if len( boxContents ) > 0:
		return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, Column( boxContents ).alignHExpand() )
	else:
		return None


def minimalExecutionResultBox(stdoutText, stderrText, exception, resultInTuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult):
	if stdoutText is None  and  stderrText is None  and  exception is None:
		if resultInTuple is None:
			return None
		else:
			resultView = InnerFragment( resultInTuple[0] ).alignHPack()
			if bUseDefaultPerspectiveForResult:
				resultView = ApplyPerspective.defaultPerspective( resultView )
			return Paragraph( [ resultView ] ).alignHExpand()
	else:
		boxContents = []
		if stderrText is not None:
			boxContents.append( execStderr( stderrText ) )
		if exception is not None:
			exceptionView = InnerFragment( exception )
			if bUseDefaultPerspecitveForException:
				exceptionView = ApplyPerspective.defaultPerspective( exceptionView )
			boxContents.append( execException( exceptionView ) )
		if stdoutText is not None:
			boxContents.append( execStdout( stdoutText ) )
		if resultInTuple is not None:
			resultView = InnerFragment( resultInTuple[0] ).alignHPack()
			if bUseDefaultPerspectiveForResult:
				resultView = ApplyPerspective.defaultPerspective( resultView )
			boxContents.append( execResult( resultView ) )
		
		if len( boxContents ) > 0:
			return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, Column( boxContents ).alignHExpand() )
		else:
			return None

