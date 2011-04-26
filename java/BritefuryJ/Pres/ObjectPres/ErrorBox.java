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
import BritefuryJ.StyleSheet.StyleValues;

public class ErrorBox extends Pres
{
	private String title;
	private Pres contents;
	
	
	public ErrorBox(String title, Object contents)
	{
		this.title = title;
		this.contents = coerceNonNull( contents );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( ObjectPresStyle.objectContentPadding, Double.class );
		StyleValues childStyle = ObjectPresStyle.useErrorBorderAttrs( ObjectPresStyle.useErrorBoxAttrs( style ) );
		DPElement contentsElement = contents.alignHExpand().padX( padding ).present( ctx, childStyle.alignHExpand() );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ErrorBorder( new Column( new Object[] { titlePres, contentsElement } ) ).present( ctx, style );
	}
}
