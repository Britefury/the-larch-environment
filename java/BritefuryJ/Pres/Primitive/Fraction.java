//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSFraction;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Fraction extends Pres
{
	public static class FractionBar extends Pres
	{
		private String textRepresentation;
		
		
		public FractionBar(String textRepresentation)
		{
			this.textRepresentation = textRepresentation;
		}
		
		
		@Override
		public LSElement present(PresentationContext ctx, StyleValues style)
		{
			return new LSFraction.DPFractionBar( Primitive.fractionBarParams.get( style ), textRepresentation );
		}
	}
	
	
	
	private Pres numerator, denominator, bar;
	
	public Fraction(Object numerator, Object denominator)
	{
		this( numerator, denominator, "/" );
	}
	
	public Fraction(Object numerator, Object denominator, String barTextRepresentation)
	{
		this.numerator = coerce( numerator );
		this.denominator = coerce( denominator );
		this.bar = new FractionBar( barTextRepresentation );
	}
	
	public Fraction(Object numerator, Object denominator, Object bar)
	{
		this.numerator = coerce( numerator );
		this.denominator = coerce( denominator );
		this.bar = coerce( bar );
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Primitive.useFractionParams( Primitive.useTextParams( style ) );
		LSElement numeratorElem = numerator.present( ctx, fractionNumeratorStyle( usedStyle ) );
		LSElement barElem = bar.present( ctx, style );
		LSElement denominatorElem = denominator.present( ctx, fractionDenominatorStyle( usedStyle ) );
		return new LSFraction( Primitive.fractionParams.get( style ), Primitive.caretSlotParams.get( style ), numeratorElem, barElem, denominatorElem );
	}



	private static StyleValues fractionNumeratorStyle(StyleValues style)
	{
		double scale = style.get( Primitive.fontScale, Double.class );
		double fracScale = style.get( Primitive.fractionFontScale, Double.class );
		double minFracScale = style.get( Primitive.fractionMinFontScale, Double.class );
		scale = Math.max( scale * fracScale, minFracScale );
		return style.withAttr( Primitive.fontScale, scale );
	}

	private static StyleValues fractionDenominatorStyle(StyleValues style)
	{
		return fractionNumeratorStyle( style );
	}
}
