//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Inspect;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Projection.AbstractPerspective;

public class JavaInspectorPerspective extends AbstractPerspective
{
	private JavaInspectorPerspective()
	{
	}

	
	@Override
	protected Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres result = new ObjectBox( x.getClass().getName(), PrimitivePresenter.presentJavaObjectInspector( x, fragment, inheritedState ) );
		
		result.setDebugName( x.getClass().getName() );
		return result;
	}


	@Override
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return null;
	}


	public static final JavaInspectorPerspective instance = new JavaInspectorPerspective();
}
