//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Logging;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.ObjectView.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

public class LogEntry implements Presentable
{
	private static final List<String> emptyTags = new ArrayList<String>();
	
	
	private List<String> tags;
	
	
	
	public LogEntry()
	{
		tags = emptyTags;
	}
	
	public LogEntry(List<String> tags)
	{
		this.tags = tags;
	}
	
	
	
	public List<String> getTags()
	{
		return tags;
	}



	public String getLogEntryTitle()
	{
		return "Log entry";
	}
	
	public DPElement createLogEntryPresentationContent(GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
	{
		return PrimitiveStyleSheet.instance.staticText( "<empty>" );
	}


	public DPElement present(GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
	{
		DPElement label = labelStyle.staticText( getLogEntryTitle() );
		
		DPElement entryContent = createLogEntryPresentationContent( ctx, styleSheet, state );
		DPElement content = PrimitiveStyleSheet.instance.vbox( Arrays.asList( new DPElement[] { entryContent }  ) );
		
		return borderStyle.border( PrimitiveStyleSheet.instance.vbox( Arrays.asList( new DPElement[] { label, content.padX( 5.0, 0.0 ) } ) ) );
	}


	private static PrimitiveStyleSheet labelStyle = PrimitiveStyleSheet.instance.withFont( new Font( "Sans serif", Font.PLAIN, 10 ) ).withForeground( new Color( 0.45f, 0.65f, 0.0f ) ); 
	private static PrimitiveStyleSheet borderStyle = PrimitiveStyleSheet.instance.withBorder( new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.45f, 0.65f, 0.0f ), null ) ); 
}
