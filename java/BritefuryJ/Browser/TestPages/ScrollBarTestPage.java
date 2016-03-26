//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.HScrollBar;
import BritefuryJ.Controls.ScrollBar;
import BritefuryJ.Controls.VScrollBar;
import BritefuryJ.LSpace.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class ScrollBarTestPage extends TestPage
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
		
		return new Body( new Pres[] { new Heading2( "Horizontal scroll bar" ), new SpaceBin( 300.0, -1.0, horizontal.alignHExpand() ),
				new Heading2( "Vertical scroll bar" ), new SpaceBin( -1.0, 300.0, vertical.alignVExpand() ).padX( 20.0 ) } );
	}
}
