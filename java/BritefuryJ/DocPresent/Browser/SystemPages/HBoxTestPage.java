//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class HBoxTestPage extends SystemPage
{
	protected HBoxTestPage()
	{
		register( "tests.hbox" );
	}
	
	
	public String getTitle()
	{
		return "H-Box test";
	}
	
	protected String getDescription()
	{
		return "The HBox element arranges its child elements in a horizontal box."; 
	}

	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet outlineStyleSheet =styleSheet.withBorder( new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null ) );

	
	protected DPWidget makeText(String text, int size)
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance.withFont( new Font( "Sans serif", Font.BOLD, size ) );
		return outlineStyleSheet.border( styleSheet.text( text ) );
	}
	
	
	protected DPWidget createContents()
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		children.add( makeText( "a", 24 ).alignVBaselines() );
		children.add( makeText( "g", 24 ).alignVBaselines() );
		children.add( makeText( "v_ref_y", 18 ).alignVBaselines() );
		children.add( makeText( "v_ref_y48", 48 ).alignVBaselines() );
		children.add( makeText( "v_ref_y-expand", 18 ).alignVBaselinesExpand() );
		children.add( makeText( "v_top", 18 ).alignVTop() );
		children.add( makeText( "v_centre", 18 ).alignVCentre() );
		children.add( makeText( "v_bottom", 18 ).alignVBottom() );
		children.add( makeText( "v_expand", 18 ).alignVExpand() );
		
		return outlineStyleSheet.border( styleSheet.hbox( children ) ).pad( 10.0, 20.0 );
	}
}
