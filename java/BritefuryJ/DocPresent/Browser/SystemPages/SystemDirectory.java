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
	private static ArrayList<SystemPage> testPages = new ArrayList<SystemPage>();
	
	
	
	protected static void initialise()
	{
		testPages.add( new BorderTestPage() );
		testPages.add( new FractionTestPage() );
		testPages.add( new HBoxTestPage() );
		testPages.add( new HBoxTypesetTestPage() );
		testPages.add( new MathRootTestPage() );
		testPages.add( new ParagraphCollationTestPage() );
		testPages.add( new ParagraphTestPage() );
		testPages.add( new ScriptTestPage() );
		testPages.add( new SegmentTestPage() );
		testPages.add( new TableTestPage() );
		testPages.add( new TextTestPage() );
		testPages.add( new VBoxTestPage() );
	}
	
	
	protected static List<SystemPage> getTestPages()
	{
		return testPages;
	}
}
