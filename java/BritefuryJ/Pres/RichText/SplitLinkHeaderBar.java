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
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class SplitLinkHeaderBar extends Pres
{
	protected Pres leftChildren[], rightChildren[];
	
	
	public SplitLinkHeaderBar(Object leftChildren[], Object rightChildren[])
	{
		this.leftChildren = mapCoerce( leftChildren );
		this.rightChildren = mapCoerce( rightChildren );
	}
	
	public SplitLinkHeaderBar(List<Object> leftChildren, List<Object> rightChildren)
	{
		this.leftChildren = mapCoerce( leftChildren );
		this.rightChildren = mapCoerce( rightChildren );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( RichText.linkHeaderPadding, Double.class );
		Pres left[] = mapCoerce( mapPresent( ctx, RichText.useLinkHeaderAttrs( style ), leftChildren ) );
		Pres right[] = mapCoerce( mapPresent( ctx, RichText.useLinkHeaderAttrs( style ), rightChildren ) );
		StyleSheet linkHeaderStyle = RichText.linkHeaderStyle( style );
		
		Pres contents = new Row( new Pres[] { linkHeaderStyle.applyTo( new Row( left ).alignHLeft() ),
				linkHeaderStyle.applyTo( new Row( right ).alignHRight() ) } ).pad( padding, padding );
		Pres rule = Rule.hrule();
		Pres header = new Column( new Pres[] { contents, rule } );
		return RichText.linkHeaderStyle( style ).applyTo( header.alignHExpand() ).present( ctx, style );
	}
}
