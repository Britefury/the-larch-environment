//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Pres.InnerFragment;

public class SREInnerFragment extends InnerFragment
{
	public SREInnerFragment(Object model, int containerPrecedence, SyntaxRecognizingController.EditMode editMode)
	{
		super( model, SimpleAttributeTable.instance.withAttr( "outerPrecedence", containerPrecedence ).withAttr( "__SREditor_edit", editMode ) );
	}

	public SREInnerFragment(Object model, int containerPrecedence)
	{
		this( model, containerPrecedence, SyntaxRecognizingController.EditMode.DISPLAY );
	}

	
	public static SREInnerFragment[] map(Object models[], int containerPrecedence, SyntaxRecognizingController.EditMode editMode)
	{
		SREInnerFragment fragments[] = new SREInnerFragment[models.length];
		for (int i = 0; i < models.length; i++)
		{
			fragments[i] = new SREInnerFragment( models[i], containerPrecedence, editMode );
		}
		return fragments;
	}

	public static List<SREInnerFragment> map(List<Object> models, int containerPrecedence, SyntaxRecognizingController.EditMode editMode)
	{
		ArrayList<SREInnerFragment> fragments = new ArrayList<SREInnerFragment>();
		fragments.ensureCapacity( models.size() );
		for (Object model: models)
		{
			fragments.add( new SREInnerFragment( model, containerPrecedence, editMode ) );
		}
		return fragments;
	}


	public static SREInnerFragment[] map(Object models[], int containerPrecedence)
	{
		return map( models, containerPrecedence, SyntaxRecognizingController.EditMode.DISPLAY );
	}

	public static List<SREInnerFragment> map(List<Object> models, int containerPrecedence)
	{
		return map( models, containerPrecedence, SyntaxRecognizingController.EditMode.DISPLAY );
	}
}
