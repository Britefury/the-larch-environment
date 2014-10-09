##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color

from BritefuryJ.AttributeTable import AttributeNamespace, InheritedAttributeNonNull, PyDerivedValueTable
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.Pres import ApplyStyleSheetFromAttribute, Pres, ApplyPerspective
from BritefuryJ.Pres.Primitive import Primitive, Label, StaticText, Column, Overlay, Bin, Border, Paragraph



class ExecutionStyle (object):
	execution = AttributeNamespace( 'CodeExecution' )

	label_style = InheritedAttributeNonNull( execution, 'label_style', StyleSheet,
						StyleSheet.style( Primitive.fontSize( 10 ) ) )

	stdout_style = InheritedAttributeNonNull( execution, 'stdout_style', StyleSheet,
						 StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 7.0, 7.0, Color( 0.5, 1.0, 0.5 ), Color( 0.9, 1.0, 0.9 ) ) ), Primitive.foreground( Color( 0.0, 0.5, 0.0 ) ) ) )
	stderr_style = InheritedAttributeNonNull( execution, 'stderr_style', StyleSheet,
						 StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 7.0, 7.0, Color( 1.0, 0.75, 0.5 ), Color( 1.0, 0.95, 0.9 ) ) ), Primitive.foreground( Color( 0.75, 0.375, 0.0 ) ) ) )
	stream_style = InheritedAttributeNonNull( execution, 'stream_style', StyleSheet,
						 StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 7.0, 7.0, Color( 0.75, 0.75, 0.75 ), Color( 0.95, 0.95, 0.95 ) ) ), Primitive.foreground( Color( 0.75, 0.375, 0.0 ) ) ) )

	exceptionBorderStyle = InheritedAttributeNonNull( execution, 'exceptionBorderStyle', StyleSheet,
							  StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 7.0, 7.0, Color( 0.8, 0.0, 0.0 ), Color( 1.0, 0.9, 0.9 ) ) ) ) )
	resultBorderStyle = InheritedAttributeNonNull( execution, 'resultBorderStyle', StyleSheet,
						       StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.0, 0.8 ), Color.WHITE ) ) ) )

	resultBoxStyle = InheritedAttributeNonNull( execution, 'resultSpacing', StyleSheet, StyleSheet.style( Primitive.columnSpacing( 5.0 ) ) )


	@PyDerivedValueTable( execution )
	def _resultBoxStyle(style):
		resultSpacing = style.get( ExecutionStyle.resultSpacing )
		return style.withValues( Primitive.columnSpacing( resultSpacing ) )




def _text_lines(text, text_style_attribute):
	if text.endswith('\n'):
		text = text[:-1]
	return ApplyStyleSheetFromAttribute(text_style_attribute, Column([StaticText(line)   for line in text.split('\n')]))

def _rich_string_item(item, text_style_attribute):
	if item.isStructural():
		resultView = Pres.coercePresentingNull( item.getValue() )
		return resultView
	else:
		return _text_lines( item.getValue(), text_style_attribute )

def _rich_string_lines(label_text, rich_string, text_style_attribute):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.label_style, StaticText( label_text ) )
	lines = [ _rich_string_item( item, text_style_attribute )   for item in rich_string.getItems() ]
	return Overlay( [ Column( lines ).alignHExpand(), label.alignHRight().alignVTop() ] )

_stream_styles = {
	'stdout': ExecutionStyle.stdout_style,
	'stderr': ExecutionStyle.stderr_style,
}

def stream_pres(rich_string, stream_name):
	stream_style = _stream_styles.get(stream_name, ExecutionStyle.stream_style)
	return ApplyStyleSheetFromAttribute(stream_style, Border( _rich_string_lines( stream_name.upper(), rich_string, stream_style ).alignHExpand() ).alignHExpand() )

def exec_exception(exception_view):
	label = ApplyStyleSheetFromAttribute( ExecutionStyle.label_style, StaticText( 'EXCEPTION:' ) )
	return ApplyStyleSheetFromAttribute( ExecutionStyle.exceptionBorderStyle, Border( Column( [ label, exception_view.padX( 5.0, 0.0 ).alignHExpand() ] ).alignHExpand() ).alignHExpand() )

def exec_result(result_view):
	return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBorderStyle, Border( Bin( Paragraph( [ result_view ] ) ) ).alignHExpand() )



def execution_result_box(streams, exception, result_in_tuple, use_default_perspective_for_exception, use_default_perspective_for_result):
	box_contents = []
	if use_default_perspective_for_result:
		box_contents.append(ApplyPerspective.defaultPerspective(streams))
	else:
		box_contents.append(streams)
	if exception is not None:
		exception_view = Pres.coerce( exception ).alignHPack()
		if use_default_perspective_for_exception:
			exception_view = ApplyPerspective.defaultPerspective( exception_view )
		box_contents.append( exec_exception( exception_view ) )
	if result_in_tuple is not None:
		result_view = Pres.coercePresentingNull( result_in_tuple[0] ).alignHPack()
		if use_default_perspective_for_result:
			result_view = ApplyPerspective.defaultPerspective( result_view )
		box_contents.append( exec_result( result_view ) )

	if len( box_contents ) > 0:
		return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, Column( box_contents ).alignHExpand() )
	else:
		return None


def minimal_execution_result_box(streams, exception, result_in_tuple, use_default_perspective_for_exception, use_default_perspective_for_result):
	if len( streams ) == 0  and  exception is None:
		if result_in_tuple is None:
			return None
		else:
			result_view = Pres.coercePresentingNull( result_in_tuple[0] ).alignHPack()
			if use_default_perspective_for_result:
				result_view = ApplyPerspective.defaultPerspective( result_view )
			return Paragraph( [ result_view ] ).alignHExpand()
	else:
		box_contents = []
		for stream in streams:
			box_contents.append(stream_pres(stream.rich_string, stream.name))
		if exception is not None:
			exception_view = Pres.coerce( exception )
			if use_default_perspective_for_exception:
				exception_view = ApplyPerspective.defaultPerspective( exception_view )
			box_contents.append( exec_exception( exception_view ) )
		if result_in_tuple is not None:
			result_view = Pres.coercePresentingNull( result_in_tuple[0] ).alignHPack()
			if use_default_perspective_for_result:
				result_view = ApplyPerspective.defaultPerspective( result_view )
			box_contents.append( exec_result( result_view ) )

		if len( box_contents ) > 0:
			return ApplyStyleSheetFromAttribute( ExecutionStyle.resultBoxStyle, Column( box_contents ).alignHExpand() )
		else:
			return None


