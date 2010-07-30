//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.PresCom;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;

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
	public DPElement present(PresentationContext ctx)
	{
		double padding = ctx.getStyle().get( GenericStyle.objectContentPadding, Double.class );
		PresentationContext childCtx = GenericStyle.useObjectBorderAttrs( GenericStyle.useObjectBoxAttrs( GenericStyle.useObjectFieldListAttrs( ctx ) ) );
		
		DPElement childElems[] = mapPresent( childCtx, children );
		Pres contents = GenericStyle.objectBoxFieldListStyle.get( ctx.getStyle() ).applyTo( new VBox( childElems ) );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ObjectBorder( new VBox( new Pres[] { titlePres, contents.padX( padding ) } ) ).present( ctx );
	}
}
