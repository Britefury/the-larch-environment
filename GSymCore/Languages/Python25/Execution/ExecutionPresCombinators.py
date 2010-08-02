##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Combinators import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.Combinators.Sequence import *

from BritefuryJ.GSym.GenericPerspective.PresCom import GenericPerspectiveInnerFragment
from BritefuryJ.GSym.PresCom import InnerFragment




class ExecutionStyle (object):
	pythonExecution = AttributeNamespace( 'pythonExecution' )
	
	labelStyle = InheritedAttributeNonNull( pythonExecution, 'labelStyle', StyleSheet2,
	                                          StyleSheet2.instance.withAttr( Primitive.fontSize, 10 ).withAttr( Primitive.foreground, Color.BLACK ) )
	
	stdOutStyle = InheritedAttributeNonNull( pythonExecution, 'stdOutStyle', StyleSheet2,
	                                          StyleSheet2.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), Color.WHITE ) ).withAttr( Primitive.foreground, Color( 0.0, 0.5, 0.0 ) ) )
	stdErrStyle = InheritedAttributeNonNull( pythonExecution, 'stdErrStyle', StyleSheet2,
	                                          StyleSheet2.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 1.0, 0.5, 0.0 ), Color.WHITE ) ).withAttr( Primitive.foreground, Color( 0.75, 0.375, 0.0 ) ) )
	exceptionBorderStyle = InheritedAttributeNonNull( pythonExecution, 'exceptionBorderStyle', StyleSheet2,
	                                          StyleSheet2.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.8, 0.0, 0.0 ), Color( 1.0, 0.9, 0.9 ) ) ) )
	resultBorderStyle = InheritedAttributeNonNull( pythonExecution, 'resultBorderStyle', StyleSheet2,
	                                          StyleSheet2.instance.withAttr( Primitive.border, SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.0, 0.8 ), Color.WHITE ) ) )

	resultBoxStyle = InheritedAttributeNonNull( pythonExecution, 'resultSpacing', StyleSheet2, StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 5.0 ) )


	@PyDerivedValueTable( pythonExecution )
	def _resultBoxStyle(style):
		resultSpacing = style.get( ExecutionStyle.resultSpacing )
		return style.withAttr( Primitive.vboxSpacing, resultSpacing )




def _textLines(labelText, text):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( labelText ) )
	lines = ApplyStyleSheetFromAttribute( textStyleAttribute, VBox( [ StaticText( line )   for line in text.split( '\n' ) ] ) )
	return VBox( [ label, lines.padX( 5.0, 0.0 ) ] )


def execStdout(text):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdOutStyle, Border( _textLines( 'STDOUT:', text ).alignHExpand() ).alignHExpand() )

def execStderr(text):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdErrStyle, Border( _textLines( 'STDERR:', text ).alignHExpand() ).alignHExpand() )
	
def execException(exceptionView):
	exceptionBorderStyle = self.exceptionBorderStyle()
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( 'EXCEPTION:' ) )
	return ApplyStyleSheetFromAttribute( ExecutionStyle.exceptionBorderStyle, Border( VBox( [ label, exceptionView.padX( 5.0, 0.0 ).alignHExpand() ] ).alignHExpand() ).alignHExpand() )

def execResult(resultView):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBorderStyle, Border( Paragraph( [ resultView ] ).alignHExpand() ).alignHExpand() )


def executionResult(ctx, style, stdoutText, stderrText, exception, resultInTuple, bUseGenericPerspecitveForException, bUseGenericPerspectiveForResult):
	boxContents = []
	if stderrText is not None:
		boxContents.append( execStderr( stderrText ) )
	if exception is not None:
		exceptionView = GenericPerspectiveInnerFragment( exception )   if bUseGenericPerspecitveForException   else InnerFragment( exception )
		boxContents.append( execException( exceptionView ) )
	if stdoutText is not None:
		boxContents.append( execStdout( stdoutText ) )
	if resultInTuple is not None:
		resultView = GenericPerspectiveInnerFragment( resultInTuple[0] )   if bUseGenericPerspectiveForResult   else InnerFragment( resultInTuple[0] )
		boxContents.append( execResult( resultView ) )
	
	if len( boxContents ) > 0:
		return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, VBox( boxContents ).alignHExpand() )
	else:
		return None


def minimalExecutionResult(ctx, style, stdoutText, stderrText, exception, resultInTuple, bUseGenericPerspecitveForException, bUseGenericPerspectiveForResult):
	if stdoutText is None  and  stderrText is None  and  exception is None:
		if resultInTuple is None:
			return None
		else:
			resultView = GenericPerspectiveInnerFragment( resultInTuple[0] )   if bUseGenericPerspectiveForResult   else InnerFragment( resultInTuple[0] )
			return Paragraph( [ resultView ] ).alignHExpand()
	else:
		boxContents = []
		if stderrText is not None:
			boxContents.append( execStderr( stderrText ) )
		if exception is not None:
			exceptionView = GenericPerspectiveInnerFragment( exception )   if bUseGenericPerspecitveForException   else InnerFragment( exception )
			boxContents.append( execException( exceptionView ) )
		if stdoutText is not None:
			boxContents.append( execStdout( stdoutText ) )
		if resultInTuple is not None:
			resultView = GenericPerspectiveInnerFragment( resultInTuple[0] )   if bUseGenericPerspectiveForResult   else InnerFragment( resultInTuple[0] )
			boxContents.append( execResult( resultView ) )
		
		if len( boxContents ) > 0:
			return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, VBox( boxContents ).alignHExpand() )
		else:
			return None


