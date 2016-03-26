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
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ObjectTitle extends Pres
{
	private String title;
	
	
	public ObjectTitle(String title)
	{
		this.title = title;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet titleStyle = ObjectPresStyle.objectTitleStyle.get( style );
		return titleStyle.applyTo( new Label( title ) ).present( ctx, style );
	}
}
