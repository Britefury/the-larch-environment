package BritefuryJ.Pres.RichText;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.util.Arrays;
import java.util.List;


public class EmphSpan extends RichSpan
{
	public EmphSpan(Object[] contents)
	{
		super( contents );
	}

	public EmphSpan(List<Object> contents)
	{
		super( contents );
	}

	public EmphSpan(String contents)
	{
		super( contents );
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet s = style.get( RichText.emphTextAttrs, StyleSheet.class );

		return super.present( ctx, style.withAttrs( s ) );
	}
}
