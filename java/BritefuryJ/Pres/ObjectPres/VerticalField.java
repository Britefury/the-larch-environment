//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.ObjectPres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class VerticalField extends Pres
{
	private String title;
	private Pres value;
	
	
	public VerticalField(String title, Object value)
	{
		this.title = title;
		this.value = coerceNonNull( value );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet fieldStyle = style.get( ObjectPresStyle.objectFieldStyle, StyleSheet.class );
		double indentation = style.get( ObjectPresStyle.objectFieldIndentation, Double.class );
		
		DPElement valueElement = value.alignHExpand().padX( indentation, 0.0 ).present( ctx, ObjectPresStyle.useObjectFieldAttrs( style ) );
		
		return fieldStyle.applyTo( new Column( new Object[] { new Label( title ), valueElement } ).alignHExpand() ).present( ctx, style );
	}
}
