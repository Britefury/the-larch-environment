//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.StyleSheet.StyleValues;

public class TitleBar extends Pres
{
	private Title title;
	
	
	public TitleBar(Object contents[])
	{
		title = new Title( contents );
	}

	public TitleBar(List<Object> contents)
	{
		title = new Title( contents );
	}

	public TitleBar(String text)
	{
		title = new Title( text );
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		Pres titleBackground = new Border( title.alignHCentre() );
		double borderWidth = style.get( RichText.titleBorderWidth, Double.class );
		return RichText.titleStyle.get( style ).applyTo(
				titleBackground.pad( borderWidth, borderWidth ).alignHExpand() ).present( ctx, style );
	}
}
