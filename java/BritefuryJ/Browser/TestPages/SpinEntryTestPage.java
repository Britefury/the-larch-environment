//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.IntSpinEntry;
import BritefuryJ.Controls.RealSpinEntry;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.UI.SectionHeading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class SpinEntryTestPage extends TestPage
{
	protected SpinEntryTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Spin entry test";
	}
	
	protected String getDescription()
	{
		return "Spin entry control: edit a numeric value";
	}
	
	
	protected Pres createContents()
	{
		LiveValue realValue = new LiveValue( 0.0 );
		LiveValue intValue = new LiveValue( 0 );
		RealSpinEntry realSpinEntry = new RealSpinEntry( realValue, -100.0, 100.0, 1.0, 10.0 );
		IntSpinEntry intSpinEntry = new IntSpinEntry( intValue, -100, 100, 1, 10 );
		
		Pres realLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real number: " ),
			    new SpaceBin( 100.0, -1.0, realSpinEntry.alignHExpand() ), realValue } ).padX( 5.0 ) );
		Pres intLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer: " ),
			    new SpaceBin( 100.0, -1.0, intSpinEntry.alignHExpand() ), intValue } ).padX( 5.0 ) );
		Pres spinEntrySectionContents = new Column( new Pres[] { realLine, intLine } );
		
		return new Body( new Pres[] { new SectionHeading2( "Spin entries" ), spinEntrySectionContents } );
	}
}
