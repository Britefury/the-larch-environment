//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
		public DPElement present(PresentationContext ctx, StyleValues style)
		{
			return new DPFraction.DPFractionBar( Primitive.fractionBarParams.get( style ), textRepresentation );
		}
	}
	
	
	
	private Pres numerator, denominator, bar;
	
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement barElement = bar.present( ctx, style );
		DPFraction element = new DPFraction( Primitive.fractionParams.get( style ), Primitive.textParams.get( style ), "/" );
		StyleValues usedStyle = Primitive.useFractionParams( Primitive.useTextParams( style ) );
		element.setNumeratorChild( numerator.present( ctx, fractionNumeratorStyle( usedStyle ) ) );
		element.setDenominatorChild( denominator.present( ctx, fractionDenominatorStyle( usedStyle ) ) );
		element.setBarChild( barElement );
		return element;
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
