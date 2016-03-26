//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.IntNumericLabel;
import BritefuryJ.Controls.RealNumericLabel;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class NumericLabelTestPage extends TestPage
{
	protected NumericLabelTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Numeric label test";
	}
	
	protected String getDescription()
	{
		return "Numeric label control: edit a numeric value";
	}
	
	
	protected Pres createContents()
	{
		LiveValue realValue = new LiveValue( 0.0 );
		LiveValue intValue = new LiveValue( 0 );
		RealNumericLabel realNumLabel = new RealNumericLabel( realValue, -100.0, 100.0 );
		IntNumericLabel intNumLabel = new IntNumericLabel( intValue, -100, 100 );
		
		Pres realLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real number: " ),
			    new SpaceBin( 100.0, -1.0, realNumLabel.alignHExpand() ), realValue } ).padX( 5.0 ) );
		Pres intLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer: " ),
			    new SpaceBin( 100.0, -1.0, intNumLabel.alignHExpand() ), intValue } ).padX( 5.0 ) );
		Pres numericLabelSectionContents = new Column( new Pres[] { realLine, intLine } );
		
		return new Body( new Pres[] { new Heading2( "Numeric labels" ), numericLabelSectionContents } );
	}
}
