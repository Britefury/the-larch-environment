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
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;

public class ObjectBox extends Pres
{
	private String title;
	private Pres contents;
	
	
	public ObjectBox(String title, Object contents)
	{
		this.title = title;
		this.contents = coerce( contents );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		double padding = ctx.getStyle().get( GenericStyle.objectContentPadding, Double.class );
		PresentationContext childCtx = GenericStyle.useObjectBorderAttrs( GenericStyle.useObjectBoxAttrs( ctx ) );
		DPElement contentsElement = contents.present( childCtx );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ObjectBorder( new VBox( new Object[] { titlePres, contentsElement.padX( padding ) } ) ).present( ctx );
	}
}
