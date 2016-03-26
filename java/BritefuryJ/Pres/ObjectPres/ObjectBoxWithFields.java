//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.ObjectPres;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleValues;

public class ObjectBoxWithFields extends SequentialPres
{
	private String title;
	
	
	public ObjectBoxWithFields(String title, Object fields[])
	{
		super( fields );
		this.title = title;
	}
	
	public ObjectBoxWithFields(String title, List<Object> fields)
	{
		super( fields );
		this.title = title;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( ObjectPresStyle.objectContentPadding, Double.class );
		StyleValues childStyle = ObjectPresStyle.useObjectBorderAttrs( ObjectPresStyle.useObjectBoxAttrs( ObjectPresStyle.useObjectFieldListAttrs( style ) ) );
		
		LSElement childElems[] = mapPresent( ctx, childStyle, children );
		Pres contents = ObjectPresStyle.objectBoxFieldListStyle.get( style ).applyTo( new Column( childElems ).alignHExpand() );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ObjectBorder( new Column( new Pres[] { titlePres, contents.padX( padding ) } ).alignHExpand() ).present( ctx, style );
	}
}
