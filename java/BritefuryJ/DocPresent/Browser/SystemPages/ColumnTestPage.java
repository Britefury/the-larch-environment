//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ColumnTestPage extends SystemPage
{
	protected ColumnTestPage()
	{
	}
	
	public String getTitle()
	{
		return "Column test";
	}
	
	protected String getDescription()
	{
		return "The column element arranges its child elements in a vertical box."; 
	}

	
	StyleSheet styleSheet = StyleSheet.instance;
	StyleSheet outlineStyleSheet = styleSheet.withAttr( Primitive.border, new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null ) );
	StyleSheet textOnGreyStyle = styleSheet.withAttr( Primitive.background, new FillPainter( new Color( 0.8f, 0.8f, 0.8f ) ) );
	StyleSheet t12Style = styleSheet.withAttr( Primitive.fontSize, 12 );
	StyleSheet t18Style = styleSheet.withAttr( Primitive.fontSize, 18 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.3f, 0.6f ) );
	StyleSheet t24Style = styleSheet.withAttr( Primitive.fontSize, 24 );

	
	
	private Pres makeRefAlignedRow(int refPointIndex, String header)
	{
		Pres v = new Column( new Pres[] { styleSheet.applyTo( new StaticText( "First item" ) ), styleSheet.applyTo( new StaticText( "Second item" ) ),
				styleSheet.applyTo( new StaticText( "Third item" ) ), styleSheet.applyTo( new StaticText( "Fourth item item" ) ) }, refPointIndex );
		v = styleSheet.withAttr( Primitive.columnSpacing, 0.0 ).applyTo( v );
		return new Row( new Pres[] { t18Style.applyTo( new StaticText( header ) ), v, t18Style.applyTo( new StaticText( "After" ) ) } );
	}
	
	

	protected Pres createContents()
	{
		Pres columnTest = new Column( new Pres[] { t24Style.applyTo( new StaticText( "Column" ) ),
				t12Style.applyTo( new StaticText( "First item" ) ),
				t12Style.applyTo( new StaticText( "Second item" ) ),
				t12Style.applyTo( new StaticText( "Third item" ) ) } );
		
		Pres hAlignTest = styleSheet.withAttr( Primitive.columnSpacing, 10.0 ).applyTo( new Column( new Pres[] {
				t24Style.applyTo( new StaticText( "Horizontal alignment" ) ),
				textOnGreyStyle.applyTo( new StaticText( "Left" ) ).alignHLeft(),
				textOnGreyStyle.applyTo( new StaticText( "Centre" ) ).alignHCentre(),
				textOnGreyStyle.applyTo( new StaticText( "Right" ) ).alignHRight(),
				textOnGreyStyle.applyTo( new StaticText( "Expand" ) ).alignHExpand() } ) );
		
		
		Pres refPointAlignTest = styleSheet.withAttr( Primitive.columnSpacing, 20.0 ).applyTo( new Column( new Pres[] {
				t24Style.applyTo( new StaticText( "Column reference point alignment" ) ),
				makeRefAlignedRow( 0, "ALIGN_WITH_0" ),
				makeRefAlignedRow( 1, "ALIGN_WITH_1" ),
				makeRefAlignedRow( 2, "ALIGN_WITH_2" ),
				makeRefAlignedRow( 3, "ALIGN_WITH_3" ) } ) );
		
		
		return new Body( new Pres[] {
				outlineStyleSheet.applyTo( new Border( columnTest ) ),
				outlineStyleSheet.applyTo( new Border( hAlignTest ) ),
				outlineStyleSheet.applyTo( new Border( refPointAlignTest ) ) } );
	}
}
