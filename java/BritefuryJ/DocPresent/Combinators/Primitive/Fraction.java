//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.Combinators.PresentationCombinator;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Fraction extends PresentationCombinator
{
	private PresentationCombinator numerator, denominator;
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
		element.setNumeratorChild( numerator.present( ctx.withStyle( fractionNumeratorStyle( style ) ) ) );
		element.setDenominatorChild( denominator.present( ctx.withStyle( fractionDenominatorStyle( style ) ) ) );
		return element;
	}



	private static StyleSheetValues fractionNumeratorStyle(StyleSheetValues style)
	{
		float scale = style.getNonNull( "fontScale", Float.class, 1.0f );
		float fracScale = style.getNonNull( "fractionFontScale", Float.class, StyleSheetValues.defaultFractionFontScale );
		float minFracScale = style.getNonNull( "fractionMinFontScale", Float.class, StyleSheetValues.defaultFractionMinFontScale );
		scale = Math.max( scale * fracScale, minFracScale );
		return style.withAttr( "fontScale", scale );
	}

	private static StyleSheetValues fractionDenominatorStyle(StyleSheetValues style)
	{
		return fractionNumeratorStyle( style );
	}
}
