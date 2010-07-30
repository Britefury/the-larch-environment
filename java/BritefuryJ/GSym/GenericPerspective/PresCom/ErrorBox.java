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
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class ErrorBox extends Pres
{
	private String title;
	private Pres contents;
	
	
	public ErrorBox(String title, Object contents)
	{
		this.title = title;
		this.contents = coerce( contents );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( GenericStyle.objectContentPadding, Double.class );
		StyleValues childStyle = GenericStyle.useErrorBorderAttrs( GenericStyle.useErrorBoxAttrs( style ) );
		DPElement contentsElement = contents.present( ctx, childStyle );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ErrorBorder( new VBox( new Object[] { titlePres, contentsElement.padX( padding ) } ) ).present( ctx, style );
	}
}
