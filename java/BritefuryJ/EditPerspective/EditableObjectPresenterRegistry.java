//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.EditPerspective;

import BritefuryJ.ObjectPresentation.ObjectPresentationPerspective;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;

public class EditableObjectPresenterRegistry extends ObjectPresenterRegistry
{
	private EditableObjectPresenterRegistry()
	{
	}

	
	public void registerPerspective(ObjectPresentationPerspective perspective)
	{
		super.registerPerspective( perspective );
	}



	public static final EditableObjectPresenterRegistry instance = new EditableObjectPresenterRegistry();
}
