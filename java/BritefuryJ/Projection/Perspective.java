//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Projection;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.ViewFragmentFunction;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.Pres.Pres;

public class Perspective extends AbstractPerspective
{
	private ViewFragmentFunction fragmentViewFn;
	private ClipboardHandlerInterface clipboardHandler;
	
	
	public Perspective(ViewFragmentFunction fragmentViewFn, ClipboardHandlerInterface clipboardHandler)
	{
		this.fragmentViewFn = fragmentViewFn;
		this.clipboardHandler = clipboardHandler;
	}
	
	public Perspective(ViewFragmentFunction fragmentViewFn)
	{
		this( fragmentViewFn, null );
	}
	
	
	
	@Override
	protected Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return fragmentViewFn.createViewFragment( x, fragment, inheritedState );
	}


	@Override
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return clipboardHandler;
	}
}
