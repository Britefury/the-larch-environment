//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.Controls.Expander;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading3;
import BritefuryJ.Pres.UI.SectionHeading2;

public class ExpanderTestPage extends TestPage
{
	protected ExpanderTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Expander test";
	}
	
	protected String getDescription()
	{
		return "Expander control: show or hide content";
	}
	
	
	protected Pres createContents()
	{
		final LiveValue state = new LiveValue( false );
		
		Pres heading = new Label( "Click to expand" );
		Pres contents = new Border( new Heading3( "The contents of the expander control" ) );
		
		LiveFunction.Function statusLiveFn = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				boolean x = (Boolean)state.getValue();
				return x  ?  new Label( "Expanded" )  :  new Label( "Collapsed" );
			}
		};
		LiveFunction statusLive = new LiveFunction( statusLiveFn );
		
		Expander expander = new DropDownExpander( heading, contents, state );
		Pres optionMenuSectionContents = new Column( new Pres[] { statusLive, expander } );
		
		return new Body( new Pres[] { new SectionHeading2( "Expander" ), optionMenuSectionContents } );
	}
}
