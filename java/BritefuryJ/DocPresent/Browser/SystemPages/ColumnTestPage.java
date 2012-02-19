//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

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

	
	private StyleSheet styleSheet = StyleSheet.instance;
	private AbstractBorder outlineBorder = new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null );
	private StyleSheet textOnGreyStyle = styleSheet.withValues( Primitive.background.as( new FillPainter( new Color( 0.8f, 0.8f, 0.8f ) ) ) );
	private StyleSheet t12Style = styleSheet.withValues( Primitive.fontSize.as( 12 ) );
	private StyleSheet t18Style = styleSheet.withValues( Primitive.fontSize.as( 18 ), Primitive.foreground.as( new Color( 0.0f, 0.3f, 0.6f ) ) );
	private StyleSheet t24Style = styleSheet.withValues( Primitive.fontSize.as( 24 ) );

	
	
	private Pres makeRefAlignedRow(int refPointIndex, String header)
	{
		Pres v = new Column( new Pres[] { styleSheet.applyTo( new Label( "First item" ) ), styleSheet.applyTo( new Label( "Second item" ) ),
				styleSheet.applyTo( new Label( "Third item" ) ), styleSheet.applyTo( new Label( "Fourth item item" ) ) }, refPointIndex );
		v = styleSheet.withValues( Primitive.columnSpacing.as( 0.0 ) ).applyTo( v );
		return new Row( new Pres[] { t18Style.applyTo( new Label( header ) ), v, t18Style.applyTo( new Label( "After" ) ) } );
	}
	
	

	protected Pres createContents()
	{
		Pres columnTest = new Column( new Pres[] { t24Style.applyTo( new Label( "Column" ) ),
				t12Style.applyTo( new Label( "First item" ) ),
				t12Style.applyTo( new Label( "Second item" ) ),
				t12Style.applyTo( new Label( "Third item" ) ) } );
		
		Pres hAlignTest = styleSheet.withValues( Primitive.columnSpacing.as( 10.0 ) ).applyTo( new Column( new Pres[] {
				t24Style.applyTo( new Label( "Horizontal alignment" ) ),
				textOnGreyStyle.applyTo( new Label( "Left" ) ).alignHLeft(),
				textOnGreyStyle.applyTo( new Label( "Centre" ) ).alignHCentre(),
				textOnGreyStyle.applyTo( new Label( "Right" ) ).alignHRight(),
				textOnGreyStyle.applyTo( new Label( "Expand" ) ).alignHExpand() } ) );
		
		
		Pres refPointAlignTest = styleSheet.withValues( Primitive.columnSpacing.as( 20.0 ) ).applyTo( new Column( new Pres[] {
				t24Style.applyTo( new Label( "Column reference point alignment" ) ),
				makeRefAlignedRow( 0, "ALIGN_WITH_0" ),
				makeRefAlignedRow( 1, "ALIGN_WITH_1" ),
				makeRefAlignedRow( 2, "ALIGN_WITH_2" ),
				makeRefAlignedRow( 3, "ALIGN_WITH_3" ) } ) );
		
		
		return new Body( new Pres[] {
				outlineBorder.surround( columnTest ),
				outlineBorder.surround( hAlignTest ),
				outlineBorder.surround( refPointAlignTest ) } );
	}
}
