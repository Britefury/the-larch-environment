//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double borderWidth = style.get( RichText.titleBorderWidth, Double.class );
		Subtitle subtitle = new Subtitle( subtitleText );
		Pres titleColumn = new Column( new Pres[] { title.pad( borderWidth, borderWidth ).alignHCentre(), subtitle.alignHCentre() } );
		return new Column( new Pres[] { titleColumn, Rule.hrule() } ).alignHExpand().present( ctx, style );
	}
}
