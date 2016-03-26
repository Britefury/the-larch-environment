//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Inspect;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Projection.AbstractPerspective;

public class JavaInspectorPerspective extends AbstractPerspective
{
	private JavaInspectorPerspective()
	{
	}

	
	@Override
	protected Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres result;
		if ( PrimitivePresenter.isPrimitive( x ) )
		{
			result = PrimitivePresenter.presentPrimitive( x );
		}
		else
		{
			Class<?> cls = x.getClass();
			if ( cls.isArray() )
			{
				Class<?> comp = cls.getComponentType();
				Object arr[] = (Object[])x;
				result = new ObjectBox( comp.getName() + "[]", new Column( Pres.mapCoercePresentingNull(arr) ) );
			}
			else
			{
				result = new ObjectBox( cls.getName(), Inspector.presentJavaObjectInspector( x, fragment, inheritedState ) );
			}
		}
		
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
