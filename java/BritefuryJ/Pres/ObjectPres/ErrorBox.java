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
import BritefuryJ.StyleSheet.StyleValues;

public class ErrorBox extends Pres
{
	private String title;
	private Pres contents;
	
	
	public ErrorBox(String title, Object contents)
	{
		this.title = title;
		this.contents = coercePresentingNull(contents);
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( ObjectPresStyle.objectContentPadding, Double.class );
		StyleValues childStyle = ObjectPresStyle.useErrorBorderAttrs( ObjectPresStyle.useErrorBoxAttrs( style ) );
		LSElement contentsElement = contents.alignHExpand().padX( padding ).present( ctx, childStyle.alignHExpand() );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ErrorBorder( new Column( new Object[] { titlePres, contentsElement } ) ).present( ctx, style );
	}
}
