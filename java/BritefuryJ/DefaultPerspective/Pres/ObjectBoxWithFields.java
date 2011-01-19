//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Pres;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( GenericStyle.objectContentPadding, Double.class );
		StyleValues childStyle = GenericStyle.useObjectBorderAttrs( GenericStyle.useObjectBoxAttrs( GenericStyle.useObjectFieldListAttrs( style ) ) );
		
		DPElement childElems[] = mapPresent( ctx, childStyle, children );
		Pres contents = GenericStyle.objectBoxFieldListStyle.get( style ).applyTo( new Column( childElems ).alignHExpand() );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ObjectBorder( new Column( new Pres[] { titlePres, contents.padX( padding ) } ).alignHExpand() ).present( ctx, style );
	}
}
