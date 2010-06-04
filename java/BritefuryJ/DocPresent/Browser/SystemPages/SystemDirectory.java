//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.ArrayList;
import java.util.List;

public class SystemDirectory
{
	private static ArrayList<SystemPage> primitiveTestPages = new ArrayList<SystemPage>();
	private static ArrayList<SystemPage> controlTestPages = new ArrayList<SystemPage>();
	
	
	
	protected static void initialise()
	{
		primitiveTestPages.add( new AlignmentTestPage() );
		primitiveTestPages.add( new BorderTestPage() );
		primitiveTestPages.add( new CanvasTestPage() );
		primitiveTestPages.add( new DndTestPage() );
		primitiveTestPages.add( new FractionTestPage() );
		primitiveTestPages.add( new GridTestPage() );
		primitiveTestPages.add( new HBoxTestPage() );
		primitiveTestPages.add( new ImageTestPage() );
		primitiveTestPages.add( new ListViewTestPage() );
		primitiveTestPages.add( new MathRootTestPage() );
		primitiveTestPages.add( new NonLocalDndTestPage() );
		primitiveTestPages.add( new ParagraphTestPage() );
		primitiveTestPages.add( new ParagraphWithSpanTestPage() );
		primitiveTestPages.add( new ProxyAndSpanTestPage() );
		primitiveTestPages.add( new ShapeTestPage() );
		primitiveTestPages.add( new ScriptTestPage() );
		primitiveTestPages.add( new SegmentTestPage() );
		primitiveTestPages.add( new TableTestPage() );
		primitiveTestPages.add( new TextTestPage() );
		primitiveTestPages.add( new VBoxTestPage() );
		primitiveTestPages.add( new ViewportTestPage() );

		controlTestPages.add( new ButtonTestPage() );
		controlTestPages.add( new CheckboxTestPage() );
		controlTestPages.add( new HyperlinkTestPage() );
		controlTestPages.add( new ScrollBarTestPage() );
		controlTestPages.add( new ScrolledViewportTestPage() );
		controlTestPages.add( new TextEntryTestPage() );
	}
	
	
	protected static List<SystemPage> getPrimitiveTestPages()
	{
		return primitiveTestPages;
	}

	protected static List<SystemPage> getControlTestPages()
	{
		return controlTestPages;
	}
}
