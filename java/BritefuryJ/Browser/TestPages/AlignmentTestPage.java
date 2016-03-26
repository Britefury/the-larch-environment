//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class AlignmentTestPage extends TestPage
{
	protected AlignmentTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Alignment test";
	}
	
	protected String getDescription()
	{
		return "Demonstrates horizontal and vertical alignment options.";
	}

	protected Pres createContents()
	{
		StyleSheet styleSheet = StyleSheet.instance;
		StyleSheet textStyleSheet = styleSheet.withValues( Primitive.background.as( new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.7f ), new Color( 1.0f, 1.0f, 0.0f ) ) ) );
		StyleSheet dividerStyleSheet = styleSheet.withValues( Primitive.shapePainter.as( new FillPainter( new Color( 1.0f, 0.5f, 0.0f ) ) ) );
		StyleSheet sectionStyleSheet = styleSheet.withValues( Primitive.columnSpacing.as( 5.0 ) );
		
		Pres halignTitle = new Heading2( "Horizontal alignment" ); 
		Pres halignLeft = new Border( textStyleSheet.applyTo( new Label( "hAlign=LEFT" ).alignHLeft() ) );
		Pres halignCentre = new Border( textStyleSheet.applyTo( new Label( "hAlign=CENTRE" ).alignHCentre() ) );
		Pres halignRight = new Border( textStyleSheet.applyTo( new Label( "hAlign=RIGHT" ).alignHRight() ) );
		Pres halignExpand = new Border( textStyleSheet.applyTo( new Label( "hAlign=EXPAND" ).alignHExpand() ) );
		Pres halignSection = sectionStyleSheet.applyTo( new Column( new Pres[] { halignTitle.alignHPack(), halignLeft, halignCentre, halignRight, halignExpand } ) );
		
		
		Pres halignInRowTitle = new Heading2( "Horizontal alignment in row" );
		Pres halignHBPack = textStyleSheet.applyTo( new Label( "PACK" ).alignHPack() );
		Pres halignHBLeft = textStyleSheet.applyTo( new Label( "LEFT" ).alignHLeft() );
		Pres halignHBCentre = textStyleSheet.applyTo( new Label( "CENTRE" ).alignHCentre() );
		Pres halignHBRight = textStyleSheet.applyTo( new Label( "RIGHT" ).alignHRight() );
		Pres halignHBExpand = textStyleSheet.applyTo( new Label( "EXPAND" ).alignHExpand() );
		Pres hAlignRow = new Border( new Row( new Pres[] {
				halignHBPack, dividerStyleSheet.applyTo( new Box( 1.0, 1.0 ).alignVExpand() ),
				halignHBLeft, dividerStyleSheet.applyTo( new Box( 1.0, 1.0 ).alignVExpand() ),
				halignHBCentre, dividerStyleSheet.applyTo( new Box( 1.0, 1.0 ).alignVExpand() ),
				halignHBRight, dividerStyleSheet.applyTo( new Box( 1.0, 1.0 ).alignVExpand() ),
				halignHBExpand
			} ).alignHExpand() );
		Pres halignRowSection = sectionStyleSheet.applyTo( new Column( new Pres[] { halignInRowTitle.alignHPack(), hAlignRow } ) );

		Pres valignTitle = new Heading2( "Vertical alignment" ); 

		Pres refColumn = new Column( 1, new Pres[] { new Label( "0" ), new Label( "1 (ref-y)" ), new Label( "2" ),
						new Label( "3" ), new Label( "4" ), new Label( "5" ) } );
		Pres refBox = styleSheet.withValues( Primitive.background.as( new FilledOutlinePainter( new Color( 0.8f, 0.85f, 1.0f ), new Color( 0.0f, 0.25f, 1.0f ) ) ) ).applyTo( new Bin( refColumn.pad( 5.0, 5.0 ) ) );
		

		Pres valignBaselines = new Border( textStyleSheet.applyTo( new Label( "vAlign=REFY" ).alignVRefY() ) );
		Pres valignBaselinesExpand = new Border( textStyleSheet.applyTo( new Label( "vAlign=REFY_EXPAND" ).alignVRefYExpand() ) );
		Pres valignTop = new Border( textStyleSheet.applyTo( new Label( "vAlign=TOP" ).alignVTop() ) );
		Pres valignCentre = new Border( textStyleSheet.applyTo( new Label( "vAlign=CENTRE" ).alignVCentre() ) );
		Pres valignBottom = new Border( textStyleSheet.applyTo( new Label( "vAlign=BOTTOM" ).alignVBottom() ) );
		Pres valignExpand = new Border( textStyleSheet.applyTo( new Label( "vAlign=EXPAND" ).alignVExpand() ) );

		Pres valignContentsBox = styleSheet.withValues( Primitive.rowSpacing.as( 10.0 ) ).applyTo( new Row( new Pres[] { valignBaselines.alignVRefYExpand(), valignBaselinesExpand.alignVRefYExpand(),
				valignTop.alignVRefYExpand(), valignCentre.alignVRefYExpand(), valignBottom.alignVRefYExpand(), valignExpand.alignVRefYExpand() } ) );
		
		
		Pres vAlignBox = styleSheet.withValues( Primitive.rowSpacing.as( 50.0 ) ).applyTo( new Row( new Pres[] { refBox.alignVRefYExpand(), valignContentsBox.alignVRefYExpand() } ) );
		
		Pres valignSection = sectionStyleSheet.applyTo( new Column( new Pres[] { valignTitle.alignHPack(), vAlignBox } ) );
		
		Pres mainBox = styleSheet.withValues( Primitive.columnSpacing.as( 15.0 ) ).applyTo( new Column( new Pres[] { halignSection.alignHExpand(), halignRowSection.alignHExpand(), valignSection } ) );
		return new Bin( mainBox );
	}
}
