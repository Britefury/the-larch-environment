//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.ViewFragmentFunction;

public class GSymPerspective extends GSymAbstractPerspective
{
	private ViewFragmentFunction fragmentViewFn;
	private ClipboardHandler clipboardHandler;
	
	
	public GSymPerspective(ViewFragmentFunction fragmentViewFn, ClipboardHandler clipboardHandler)
	{
		this.fragmentViewFn = fragmentViewFn;
		this.clipboardHandler = clipboardHandler;
	}
	
	public GSymPerspective(ViewFragmentFunction fragmentViewFn)
	{
		this( fragmentViewFn, null );
	}
	
	
	
	@Override
	protected Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return fragmentViewFn.createViewFragment( x, fragment, inheritedState );
	}


	@Override
	public ClipboardHandler getClipboardHandler()
	{
		return clipboardHandler;
	}
}
