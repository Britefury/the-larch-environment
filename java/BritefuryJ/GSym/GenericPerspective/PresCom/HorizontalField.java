//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.PresCom;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.LineBreak;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class HorizontalField extends Pres
{
	private String title;
	private Pres value;
	
	
	public HorizontalField(String title, Object value)
	{
		this.title = title;
		this.value = coerce( value );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet fieldStyle = style.get( GenericStyle.objectFieldStyle, StyleSheet.class );
		
		DPElement valueElement = value.present( ctx, GenericStyle.useObjectFieldAttrs( style ) );
		
		return fieldStyle.applyTo( new Paragraph( new Object[] { new StaticText( title ), new StaticText( " " ), new LineBreak(), valueElement.alignHExpand() } ) ).present( ctx, style );
	}
}
