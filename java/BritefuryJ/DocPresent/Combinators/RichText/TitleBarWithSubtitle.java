//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;

public class TitleBarWithSubtitle extends Pres
{
	private String text, subtitleText;
	
	
	public TitleBarWithSubtitle(String text, String subtitleText)
	{
		this.text = text;
		this.subtitleText = subtitleText;
	}


	@Override
	public DPElement present(PresentationContext ctx)
	{
		Title title = new Title( text );
		Subtitle subtitle = new Subtitle( subtitleText );
		Pres titleVBox = new VBox( new Pres[] { title.alignHCentre(), subtitle.alignHCentre() } );
		Pres titleBackground = new Border( titleVBox.alignHCentre() );
		double borderWidth = ctx.getStyle().get( RichText.titleBorderWidth, Double.class );
		return higherOrderPresent( ctx, RichText.titleStyle.get( ctx.getStyle() ),
				titleBackground.alignHExpand().pad( borderWidth, borderWidth ).alignHExpand() );
	}
}