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
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Fraction extends Pres
{
	private Pres numerator, denominator;
	private String barContent;
	
	public Fraction(Object numerator, Object denominator, String barContent)
	{
		this.numerator = coerce( numerator );
		this.denominator = coerce( denominator );
		this.barContent = barContent;
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		DPFraction element = new DPFraction( style.getFractionParams(), style.getTextParams(), barContent );
		StyleSheetValues usedStyle = style.useFractionParams().useTextParams();
		element.setNumeratorChild( numerator.present( ctx.withStyle( fractionNumeratorStyle( usedStyle ) ) ) );
		element.setDenominatorChild( denominator.present( ctx.withStyle( fractionDenominatorStyle( usedStyle ) ) ) );
		return element;
	}



	private static StyleSheetValues fractionNumeratorStyle(StyleSheetValues style)
	{
		double scale = style.get( StyleSheet2.fontScale, Double.class );
		double fracScale = style.get( StyleSheet2.fractionFontScale, Double.class );
		double minFracScale = style.get( StyleSheet2.fractionMinFontScale, Double.class );
		scale = Math.max( scale * fracScale, minFracScale );
		return style.withAttr( StyleSheet2.fontScale, scale );
	}

	private static StyleSheetValues fractionDenominatorStyle(StyleSheetValues style)
	{
		return fractionNumeratorStyle( style );
	}
}
