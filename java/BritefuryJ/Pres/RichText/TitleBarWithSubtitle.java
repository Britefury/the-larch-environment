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
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleValues;

public class TitleBarWithSubtitle extends Pres
{
	private Title title;
	private String subtitleText;
	
	
	public TitleBarWithSubtitle(Object contents[], String subtitleText)
	{
		title = new Title( contents );
		this.subtitleText = subtitleText;
	}

	public TitleBarWithSubtitle(List<Object> contents, String subtitleText)
	{
		title = new Title( contents );
		this.subtitleText = subtitleText;
	}

	public TitleBarWithSubtitle(String text, String subtitleText)
	{
		title = new Title( text );
		this.subtitleText = subtitleText;
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		Subtitle subtitle = new Subtitle( subtitleText );
		Pres titleColumn = new Column( new Pres[] { title.alignHCentre(), subtitle.alignHCentre() } );
		Pres titleBackground = new Border( titleColumn.alignHCentre() );
		double borderWidth = style.get( RichText.titleBorderWidth, Double.class );
		return RichText.titleStyle.get( style ).applyTo(
				titleBackground.alignHExpand().pad( borderWidth, borderWidth ).alignHExpand() ).present( ctx, style );
	}
}
