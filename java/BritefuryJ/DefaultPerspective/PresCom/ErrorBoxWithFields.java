//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.PresCom;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class ErrorBoxWithFields extends SequentialPres
{
	private String title;
	
	
	public ErrorBoxWithFields(String title, Object fields[])
	{
		super( fields );
		this.title = title;
	}
	
	public ErrorBoxWithFields(String title, List<Object> fields)
	{
		super( fields );
		this.title = title;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( GenericStyle.objectContentPadding, Double.class );
		StyleValues childStyle = GenericStyle.useErrorBorderAttrs( GenericStyle.useErrorBoxAttrs( GenericStyle.useObjectFieldListAttrs( style ) ) );
		
		DPElement childElems[] = mapPresent( ctx, childStyle, children );
		Pres contents = GenericStyle.objectBoxFieldListStyle.get( style ).applyTo( new Column( childElems ).alignHExpand() );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ErrorBorder( new Column( new Pres[] { titlePres, contents.padX( padding ) } ).alignHExpand() ).present( ctx, style );
	}
}
