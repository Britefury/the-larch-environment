//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class AlignmentTestPage extends SystemPage
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
		StyleSheet textStyleSheet = styleSheet.withAttr( Primitive.background, new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.7f ), new Color( 1.0f, 1.0f, 0.0f ) ) );
		StyleSheet dividerStyleSheet = styleSheet.withAttr( Primitive.shapePainter, new FillPainter( new Color( 1.0f, 0.5f, 0.0f ) ) );
		StyleSheet sectionStyleSheet = styleSheet.withAttr( Primitive.columnSpacing, 5.0 );
		
		Pres halignTitle = new Heading2( "Horizontal alignment" ); 
		Pres halignLeft = new Border( textStyleSheet.applyTo( new Label( "hAlign=LEFT" ).alignHLeft() ) );
		Pres halignCentre = new Border( textStyleSheet.applyTo( new Label( "hAlign=CENTRE" ).alignHCentre() ) );
		Pres halignRight = new Border( textStyleSheet.applyTo( new Label( "hAlign=RIGHT" ).alignHRight() ) );
		Pres halignExpand = new Border( textStyleSheet.applyTo( new Label( "hAlign=EXPAND" ).alignHExpand() ) );
		Pres halignSection = sectionStyleSheet.applyTo( new Column( new Pres[] { halignTitle, halignLeft.alignHExpand(), halignCentre.alignHExpand(), halignRight.alignHExpand(), halignExpand.alignHExpand() } ) );
		
		
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
		Pres halignRowSection = sectionStyleSheet.applyTo( new Column( new Pres[] { halignInRowTitle, hAlignRow.alignHExpand() } ) );

		Pres valignTitle = new Heading2( "Vertical alignment" ); 

		Pres refColumn = new Column( new Pres[] { new Label( "0" ), new Label( "1 (ref-y)" ), new Label( "2" ),
				new Label( "3" ), new Label( "4" ), new Label( "5" ) }, 1 );
		Pres refBox = styleSheet.withAttr( Primitive.background, new FilledOutlinePainter( new Color( 0.8f, 0.85f, 1.0f ), new Color( 0.0f, 0.25f, 1.0f ) ) ).applyTo( new Bin( refColumn.pad( 5.0, 5.0 ) ) );
		

		Pres valignBaselines = new Border( textStyleSheet.applyTo( new Label( "vAlign=REFY" ).alignVRefY() ) );
		Pres valignBaselinesExpand = new Border( textStyleSheet.applyTo( new Label( "vAlign=REFY_EXPAND" ).alignVRefYExpand() ) );
		Pres valignTop = new Border( textStyleSheet.applyTo( new Label( "vAlign=TOP" ).alignVTop() ) );
		Pres valignCentre = new Border( textStyleSheet.applyTo( new Label( "vAlign=CENTRE" ).alignVCentre() ) );
		Pres valignBottom = new Border( textStyleSheet.applyTo( new Label( "vAlign=BOTTOM" ).alignVBottom() ) );
		Pres valignExpand = new Border( textStyleSheet.applyTo( new Label( "vAlign=EXPAND" ).alignVExpand() ) );

		Pres valignContentsBox = styleSheet.withAttr( Primitive.rowSpacing, 10.0 ).applyTo( new Row( new Pres[] { valignBaselines.alignVRefYExpand(), valignBaselinesExpand.alignVRefYExpand(),
				valignTop.alignVRefYExpand(), valignCentre.alignVRefYExpand(), valignBottom.alignVRefYExpand(), valignExpand.alignVRefYExpand() } ) );
		
		
		Pres vAlignBox = styleSheet.withAttr( Primitive.rowSpacing, 50.0 ).applyTo( new Row( new Pres[] { refBox.alignVRefYExpand(), valignContentsBox.alignVRefYExpand() } ) );
		
		Pres valignSection = sectionStyleSheet.applyTo( new Column( new Pres[] { valignTitle, vAlignBox.alignHExpand() } ) );
		
		Pres mainBox = styleSheet.withAttr( Primitive.columnSpacing, 15.0 ).applyTo( new Column( new Pres[] { halignSection.alignHExpand(), halignRowSection.alignHExpand(), valignSection } ) );
		return new Bin( mainBox );
	}
}
