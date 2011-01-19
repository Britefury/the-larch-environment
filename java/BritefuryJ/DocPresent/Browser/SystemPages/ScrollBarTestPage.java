//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.Controls.HScrollBar;
import BritefuryJ.Controls.ScrollBar;
import BritefuryJ.Controls.VScrollBar;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class ScrollBarTestPage extends SystemPage
{
	protected ScrollBarTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Scroll bar test";
	}
	
	protected String getDescription()
	{
		return "Scroll bar control: used to take the browser to a different location, or perform an action";
	}
	

	protected Pres createContents()
	{
		Range range = new Range( 0.0, 100.0, 0.0, 10.0, 1.0 );
		ScrollBar horizontal = new HScrollBar( range );
		ScrollBar vertical = new VScrollBar( range );
		
		return new Body( new Pres[] { new Heading2( "Horizontal scroll bar" ), new SpaceBin( horizontal.alignHExpand(), 300.0, -1.0 ),
				new Heading2( "Vertical scroll bar" ), new SpaceBin( vertical.alignVExpand(), -1.0, 300.0 ).padX( 20.0 ) } );
	}
}
