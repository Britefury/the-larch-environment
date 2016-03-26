//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.ObjectPres;

import BritefuryJ.LSpace.LSElement;
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
		this.value = coercePresentingNull(value);
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet fieldStyle = style.get( ObjectPresStyle.objectFieldStyle, StyleSheet.class );
		double indentation = style.get( ObjectPresStyle.objectFieldIndentation, Double.class );
		
		LSElement valueElement = value.alignHExpand().padX( indentation, 0.0 ).present( ctx, ObjectPresStyle.useObjectFieldAttrs( style ) );
		
		return fieldStyle.applyTo( new Column( new Object[] { new Label( title ), valueElement } ).alignHExpand() ).present( ctx, style );
	}
}
