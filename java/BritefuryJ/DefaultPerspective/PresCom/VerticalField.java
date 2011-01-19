//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.PresCom;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
		StyleSheet fieldStyle = style.get( GenericStyle.objectFieldStyle, StyleSheet.class );
		double indentation = style.get( GenericStyle.objectFieldIndentation, Double.class );
		
		DPElement valueElement = value.present( ctx, GenericStyle.useObjectFieldAttrs( style ) );
		
		return fieldStyle.applyTo( new Column( new Object[] { new Label( title ), valueElement.alignHExpand().padX( indentation, 0.0 ) } ).alignHExpand() ).present( ctx, style );
	}
}
