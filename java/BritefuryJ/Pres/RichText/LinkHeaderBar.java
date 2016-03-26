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
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleValues;

public class LinkHeaderBar extends SequentialPres
{
	public LinkHeaderBar(Object children[])
	{
		super( children );
	}
	
	public LinkHeaderBar(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( RichText.linkHeaderPadding, Double.class );
		Pres xs[] = mapCoerce( mapPresent( ctx, RichText.useLinkHeaderAttrs( style ), children ) );
		Pres contents = new Row( xs ).alignHRight().pad( padding, padding );
		Pres rule = Rule.hrule();
		Pres header = new Column( new Pres[] { contents, rule } );
		return RichText.linkHeaderStyle( style ).applyTo( header.alignHExpand() ).present( ctx, style );
	}
}
