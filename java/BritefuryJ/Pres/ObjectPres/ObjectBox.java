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

public class ObjectBox extends Pres
{
	private String title;
	private Pres contents;
	
	
	public ObjectBox(String title, Object contents)
	{
		this.title = title;
		this.contents = coercePresentingNull(contents);
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( ObjectPresStyle.objectContentPadding, Double.class );
		StyleValues childStyle = ObjectPresStyle.useObjectBorderAttrs( ObjectPresStyle.useObjectBoxAttrs( style ) );
		LSElement contentsElement = contents.padX( padding ).present( ctx, childStyle );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ObjectBorder( new Column( new Object[] { titlePres, contentsElement } ).alignHExpand() ).present( ctx, style );
	}
}
