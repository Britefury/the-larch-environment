//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class HorizontalField extends Pres
{
	private String title;
	private Pres value;
	
	
	public HorizontalField(String title, Object value)
	{
		this.title = title;
		this.value = coerceNonNull( value );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet fieldStyle = style.get( GenericStyle.objectFieldStyle, StyleSheet.class );
		
		DPElement valueElement = value.present( ctx, GenericStyle.useObjectFieldAttrs( style ) );
		
		return fieldStyle.applyTo( new Paragraph( new Object[] { new Label( title ), new Label( " " ), new LineBreak(), valueElement.alignHExpand() } ) ).present( ctx, style );
	}
}
