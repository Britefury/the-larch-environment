//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.util.ArrayList;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;


public class TimedTip extends TimedPopup
{
	public TimedTip(String text, double timeout)
	{
		super( createChild( text ), timeout, false );
	}
	
	
	private static Pres createChild(String text)
	{
		String lineTexts[] = text.split( "\\r?\\n" );
		ArrayList<Object> lines = new ArrayList<Object>();
		for (String line: lineTexts)
		{
			lines.add( new Label( line ) );
		}
		return StyleSheet.instance.remapAttr( Primitive.border, Controls.tooltipBorder ).applyTo( new Border( new Column( lines ) ) );
	}
}
