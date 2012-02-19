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
from BritefuryJ.Graphics import *
from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.Sequence import *




class ExecutionStyle (object):
	pythonExecution = AttributeNamespace( 'pythonExecution' )
	
	labelStyle = InheritedAttributeNonNull( pythonExecution, 'labelStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color.BLACK ) ) )
	
	stdOutStyle = InheritedAttributeNonNull( pythonExecution, 'stdOutStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), Color.WHITE ) ), Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ) )
	stdErrStyle = InheritedAttributeNonNull( pythonExecution, 'stdErrStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 1.0, 0.5, 0.0 ), Color.WHITE ) ), Primitive.foreground( Color( 0.75, 0.375, 0.0 ) ) ) )
	exceptionBorderStyle = InheritedAttributeNonNull( pythonExecution, 'exceptionBorderStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.8, 0.0, 0.0 ), Color( 1.0, 0.9, 0.9 ) ) ) ) )
	resultBorderStyle = InheritedAttributeNonNull( pythonExecution, 'resultBorderStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.0, 0.8 ), Color.WHITE ) ) ) )

	resultBoxStyle = InheritedAttributeNonNull( pythonExecution, 'resultSpacing', StyleSheet, StyleSheet.style( Primitive.columnSpacing( 5.0 ) ) )


	@PyDerivedValueTable( pythonExecution )
	def _resultBoxStyle(style):
		resultSpacing = style.get( ExecutionStyle.resultSpacing )
		return style.withValues( Primitive.columnSpacing( resultSpacing ) )




def _textLines(text, textStyleAttribute):
	return ApplyStyleSheetFromAttribute( textStyleAttribute, Column( [ StaticText( line )   for line in text.split( '\n' ) ] ) )

def _streamItem(item, textStyleAttribute, bUseDefaultPerspectiveForResult):
	if item.isStructural():
		resultView = InnerFragment( item.getValue() )
		if bUseDefaultPerspectiveForResult:
			resultView = ApplyPerspective.defaultPerspective( resultView )
		return resultView
	else:
		return _textLines( item.getValue(), textStyleAttribute )

def _streamLines(labelText, stream, textStyleAttribute, bUseDefaultPerspectiveForResult):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( labelText ) )
	lines = [ _streamItem( item, textStyleAttribute, bUseDefaultPerspectiveForResult )   for item in stream.getItems() ]
	return Column( [ label, Column( lines ).padX( 5.0, 0.0 ) ] )


def execStdout(stream, bUseDefaultPerspectiveForResult):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdOutStyle, Border( _streamLines( 'STDOUT:', stream, ExecutionStyle.stdOutStyle, bUseDefaultPerspectiveForResult ).alignHExpand() ).alignHExpand() )

def execStderr(stream, bUseDefaultPerspectiveForResult):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdErrStyle, Border( _streamLines( 'STDERR:', stream, ExecutionStyle.stdErrStyle, bUseDefaultPerspectiveForResult ).alignHExpand() ).alignHExpand() )
	
def execException(exceptionView):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( 'EXCEPTION:' ) )
	return ApplyStyleSheetFromAttribute( ExecutionStyle.exceptionBorderStyle, Border( Column( [ label, exceptionView.padX( 5.0, 0.0 ).alignHExpand() ] ).alignHExpand() ).alignHExpand() )

def execResult(resultView):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBorderStyle, Border( Bin( Paragraph( [ resultView ] ) ) ).alignHExpand() )


def executionResultBox(stdoutStream, stderrStream, exception, resultInTuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult):
	boxContents = []
	if stderrStream is not None:
		boxContents.append( execStderr( stderrStream, bUseDefaultPerspectiveForResult ) )
	if stdoutStream is not None:
		boxContents.append( execStdout( stdoutStream, bUseDefaultPerspectiveForResult ) )
	if exception is not None:
		exceptionView = InnerFragment( exception ).alignHPack()
		if bUseDefaultPerspecitveForException:
			exceptionView = ApplyPerspective.defaultPerspective( exceptionView )
		boxContents.append( execException( exceptionView ) )
	if resultInTuple is not None:
		resultView = InnerFragment( resultInTuple[0] ).alignHPack()
		if bUseDefaultPerspectiveForResult:
			resultView = ApplyPerspective.defaultPerspective( resultView )
		boxContents.append( execResult( resultView ) )
	
	if len( boxContents ) > 0:
		return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, Column( boxContents ).alignHExpand() )
	else:
		return None


def minimalExecutionResultBox(stdoutStream, stderrStream, exception, resultInTuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult):
	if stdoutStream is None  and  stderrStream is None  and  exception is None:
		if resultInTuple is None:
			return None
		else:
			resultView = InnerFragment( resultInTuple[0] ).alignHPack()
			if bUseDefaultPerspectiveForResult:
				resultView = ApplyPerspective.defaultPerspective( resultView )
			return Paragraph( [ resultView ] ).alignHExpand()
	else:
		boxContents = []
		if stderrStream is not None:
			boxContents.append( execStderr( stderrStream, bUseDefaultPerspectiveForResult ) )
		if exception is not None:
			exceptionView = InnerFragment( exception )
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


