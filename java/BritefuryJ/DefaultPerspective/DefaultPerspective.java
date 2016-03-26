//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DefaultPerspective;

import java.lang.reflect.Array;
import java.util.Arrays;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Inspect.InspectorPerspective;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.ObjectPresentation.ObjectPresentationPerspective;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Projection.AbstractPerspective;

public class DefaultPerspective extends ObjectPresentationPerspective
{
	private DefaultPerspective(AbstractPerspective fallbackPerspective)
	{
		super( "__present__", fallbackPerspective );
		DefaultObjectPresenterRegistry.instance.registerPerspective( this );
	}

	

	@Override
	protected Pres presentWithJavaInterface(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( x instanceof Presentable )
		{
			Presentable p = (Presentable)x;
			return p.present( fragment, inheritedState );
		}
		else
		{
			return null;
		}
	}
	
	@Override
	protected Pres presentJavaArray(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		int length = Array.getLength( x );
		Object members[] = new Object[length];
		for (int i = 0; i < length; i++)
		{
			members[i] = new InnerFragment( Array.get( x, i ) );
		}
		return DefaultObjectPresenterRegistry.arrayView( Arrays.asList( members ) );
	}
	

	
	
	@Override
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return null;
	}



	public static final DefaultPerspective instance = new DefaultPerspective( InspectorPerspective.instance );
}
