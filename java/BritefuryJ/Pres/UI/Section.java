//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.UI;

import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Section extends CompositePres
{
	private Pres heading, contents;
	
	
	public Section(Object heading, Object contents)
	{
		this.heading = Pres.coerce( heading ).alignVRefY();
		this.contents = Pres.coerce( contents );
	}

	
	@Override
	public Pres pres(PresentationContext ctx, StyleValues style)
	{
		StyleSheet colStyle = style.get( UI.sectionColumnStyle, StyleSheet.class );
		double padding = style.get( UI.sectionPadding, Double.class );
		
		return colStyle.applyTo( new Column( new Object[] { heading, contents } ).pad( padding, padding ) );
	}
}
