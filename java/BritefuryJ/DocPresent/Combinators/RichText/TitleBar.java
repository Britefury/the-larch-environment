//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class TitleBar extends Pres
{
	private String text;
	
	
	public TitleBar(String text)
	{
		this.text = text;
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		Title title = new Title( text );
		Pres titleBackground = new Border( title.alignHCentre() );
		double borderWidth = style.get( RichText.titleBorderWidth, Double.class );
		return RichText.titleStyle.get( style ).applyTo(
				titleBackground.alignHExpand().pad( borderWidth, borderWidth ).alignHExpand() ).present( ctx, style );
	}
}
