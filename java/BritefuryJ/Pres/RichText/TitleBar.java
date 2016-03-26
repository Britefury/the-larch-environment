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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double borderWidth = style.get( RichText.titleBorderWidth, Double.class );
		Pres titlePres = title.alignHCentre().pad( borderWidth, borderWidth );
		return new Column( new Pres[] { titlePres, Rule.hrule() } ).alignHExpand().present( ctx, style );
	}
}
