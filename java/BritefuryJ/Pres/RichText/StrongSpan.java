package BritefuryJ.Pres.RichText;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.util.List;


public class StrongSpan extends RichSpan
{
	public StrongSpan(Object[] contents)
	{
		super( contents );
	}

	public StrongSpan(List<Object> contents)
	{
		super( contents );
	}

	public StrongSpan(String contents)
	{
		super( contents );
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet s = style.get( RichText.strongTextAttrs, StyleSheet.class );

		return super.present( ctx, style.withAttrs( s ) );
	}
}
