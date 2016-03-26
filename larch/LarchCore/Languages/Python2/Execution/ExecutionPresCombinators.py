##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from BritefuryJ.AttributeTable import AttributeNamespace, InheritedAttributeNonNull, PyDerivedValueTable
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.Pres import ApplyStyleSheetFromAttribute, Pres, ApplyPerspective
from BritefuryJ.Pres.Primitive import Primitive, Label, StaticText, Column, Overlay, Bin, Border, Paragraph




class ExecutionStyle (object):
	pythonExecution = AttributeNamespace( 'pythonExecution' )
	
	labelStyle = InheritedAttributeNonNull( pythonExecution, 'labelStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.fontSize( 10 ) ) )
	
	stdOutStyle = InheritedAttributeNonNull( pythonExecution, 'stdOutStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 7.0, 7.0, Color( 0.5, 1.0, 0.5 ), Color( 0.9, 1.0, 0.9 ) ) ), Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ) )
	stdErrStyle = InheritedAttributeNonNull( pythonExecution, 'stdErrStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 7.0, 7.0, Color( 1.0, 0.75, 0.5 ), Color( 1.0, 0.95, 0.9 ) ) ), Primitive.foreground( Color( 0.75, 0.375, 0.0 ) ) ) )
	exceptionBorderStyle = InheritedAttributeNonNull( pythonExecution, 'exceptionBorderStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 7.0, 7.0, Color( 0.8, 0.0, 0.0 ), Color( 1.0, 0.9, 0.9 ) ) ) ) )
	resultBorderStyle = InheritedAttributeNonNull( pythonExecution, 'resultBorderStyle', StyleSheet,
	                                          StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.0, 0.8 ), Color.WHITE ) ) ) )

	resultBoxStyle = InheritedAttributeNonNull( pythonExecution, 'resultSpacing', StyleSheet, StyleSheet.style( Primitive.columnSpacing( 5.0 ) ) )


	@PyDerivedValueTable( pythonExecution )
	def _resultBoxStyle(style):
		resultSpacing = style.get( ExecutionStyle.resultSpacing )
		return style.withValues( Primitive.columnSpacing( resultSpacing ) )




def _textLines(text, textStyleAttribute):
	if text.endswith('\n'):
		text = text[:-1]
	return ApplyStyleSheetFromAttribute( textStyleAttribute, Column( [ StaticText( line )   for line in text.split( '\n' ) ] ) )

def _richStringItem(item, textStyleAttribute, bUseDefaultPerspectiveForResult):
	if item.isStructural():
		resultView = Pres.coercePresentingNull( item.getValue() )
		if bUseDefaultPerspectiveForResult:
			resultView = ApplyPerspective.defaultPerspective( resultView )
		return resultView
	else:
		return _textLines( item.getValue(), textStyleAttribute )

def _richStringLines(labelText, richString, textStyleAttribute, bUseDefaultPerspectiveForResult):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( labelText ) )
	lines = [ _richStringItem( item, textStyleAttribute, bUseDefaultPerspectiveForResult )   for item in richString.getItems() ]
	return Overlay( [ Column( lines ).alignHExpand(), label.alignHRight().alignVTop() ] )


def execStdout(richString, bUseDefaultPerspectiveForResult):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdOutStyle, Border( _richStringLines( 'STDOUT', richString, ExecutionStyle.stdOutStyle, bUseDefaultPerspectiveForResult ).alignHExpand() ).alignHExpand() )

def execStderr(richString, bUseDefaultPerspectiveForResult):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.stdErrStyle, Border( _richStringLines( 'STDERR', richString, ExecutionStyle.stdErrStyle, bUseDefaultPerspectiveForResult ).alignHExpand() ).alignHExpand() )
	
def execException(exceptionView):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.labelStyle, StaticText( 'EXCEPTION:' ) )
	return ApplyStyleSheetFromAttribute( ExecutionStyle.exceptionBorderStyle, Border( Column( [ label, exceptionView.padX( 5.0, 0.0 ).alignHExpand() ] ).alignHExpand() ).alignHExpand() )

def execResult(resultView):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBorderStyle, Border( Bin( Paragraph( [ resultView ] ) ) ).alignHExpand() )


def executionResultBox(streams, exception, resultInTuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult):
	boxContents = []
	for stream in streams:
		if stream.name == 'out':
			boxContents.append( execStdout( stream.richString, bUseDefaultPerspectiveForResult ) )
		elif stream.name == 'err':
			boxContents.append( execStderr( stream.richString, bUseDefaultPerspectiveForResult ) )
		else:
			raise ValueError, 'Unreckognised stream \'{0}\''.format( stream.name )
	if exception is not None:
		exceptionView = Pres.coerce( exception ).alignHPack()
		if bUseDefaultPerspecitveForException:
			exceptionView = ApplyPerspective.defaultPerspective( exceptionView )
		boxContents.append( execException( exceptionView ) )
	if resultInTuple is not None:
		resultView = Pres.coercePresentingNull( resultInTuple[0] ).alignHPack()
		if bUseDefaultPerspectiveForResult:
			resultView = ApplyPerspective.defaultPerspective( resultView )
		boxContents.append( execResult( resultView ) )
	
	if len( boxContents ) > 0:
		return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, Column( boxContents ).alignHExpand() )
	else:
		return None


def minimalExecutionResultBox(streams, exception, resultInTuple, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult):
	if len( streams ) == 0  and  exception is None:
		if resultInTuple is None:
			return None
		else:
			resultView = Pres.coercePresentingNull( resultInTuple[0] ).alignHPack()
			if bUseDefaultPerspectiveForResult:
				resultView = ApplyPerspective.defaultPerspective( resultView )
			return Paragraph( [ resultView ] ).alignHExpand()
	else:
		boxContents = []
		for stream in streams:
			if stream.name == 'out':
				boxContents.append( execStdout( stream.richString, bUseDefaultPerspectiveForResult ) )
			elif stream.name == 'err':
				boxContents.append( execStderr( stream.richString, bUseDefaultPerspectiveForResult ) )
			else:
				raise ValueError, 'Unreckognised stream \'{0}\''.format( stream.name )
		if exception is not None:
			exceptionView = Pres.coerce( exception )
			if bUseDefaultPerspecitveForException:
				exceptionView = ApplyPerspective.defaultPerspective( exceptionView )
			boxContents.append( execException( exceptionView ) )
		if resultInTuple is not None:
			resultView = Pres.coercePresentingNull( resultInTuple[0] ).alignHPack()
			if bUseDefaultPerspectiveForResult:
				resultView = ApplyPerspective.defaultPerspective( resultView )
			boxContents.append( execResult( resultView ) )
		
		if len( boxContents ) > 0:
			return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, Column( boxContents ).alignHExpand() )
		else:
			return None


