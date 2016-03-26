//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.ObjectPres;

import BritefuryJ.LSpace.LSElement;
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
		this.value = coercePresentingNull(value);
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet fieldStyle = style.get( ObjectPresStyle.objectFieldStyle, StyleSheet.class );
		
		LSElement valueElement = value.present( ctx, ObjectPresStyle.useObjectFieldAttrs( style ) );
		
		return fieldStyle.applyTo( new Paragraph( new Object[] { new Label( title ), new Label( " " ), new LineBreak(), valueElement } ) ).present( ctx, style );
	}
}
