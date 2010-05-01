//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Logging;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
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
	
	public DPElement createLogEntryPresentationContent(GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		return PrimitiveStyleSheet.instance.staticText( "<empty>" );
	}


	public DPElement present(GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement entryContent = createLogEntryPresentationContent( ctx, styleSheet, inheritedState );
		DPElement content = PrimitiveStyleSheet.instance.layoutWrap( entryContent );
		
		return logEntryStyle.objectBox( getLogEntryTitle(), content );
	}


	private static GenericPerspectiveStyleSheet logEntryStyle = GenericPerspectiveStyleSheet.instance.withObjectBorderPaint( new Color( 0.45f, 0.65f, 0.0f ) ).withObjectTitlePaint(
			new Color( 0.45f, 0.65f, 0.0f ) );
}
