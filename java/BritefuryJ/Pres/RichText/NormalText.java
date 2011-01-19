//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class NormalText extends RichParagraph
{
	public NormalText(String text)
	{
		super( text );
	}

	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return presentParagraph( ctx, style.withAttrs( RichText.normalTextStyle( style ) ) );
	}
	
	
	public static NormalText[] paragraphs(String text)
	{
		String lines[] = text.split( "\n" );
		NormalText paras[] = new NormalText[lines.length];
		
		for (int i = 0; i < lines.length; i++)
		{
			paras[i] = new NormalText( lines[i] );
		}
		
		return paras;
	}
}
