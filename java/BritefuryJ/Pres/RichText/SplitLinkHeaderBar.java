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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( RichText.linkHeaderPadding, Double.class );
		Pres left[] = mapCoerce( mapPresent( ctx, RichText.useLinkHeaderAttrs( style ), leftChildren ) );
		Pres right[] = mapCoerce( mapPresent( ctx, RichText.useLinkHeaderAttrs( style ), rightChildren ) );
		StyleSheet linkHeaderStyle = RichText.linkHeaderStyle( style );
		return linkHeaderStyle.applyTo( 
				new Border( new Row( new Pres[] { linkHeaderStyle.applyTo( new Row( left ).alignHLeft() ),
						linkHeaderStyle.applyTo( new Row( right ).alignHRight() ) } ) ).pad( padding, padding ).alignHExpand() ).present( ctx, style );
	}
}
