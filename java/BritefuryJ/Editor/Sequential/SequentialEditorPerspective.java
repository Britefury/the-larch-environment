//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.IncrementalView.ViewFragmentFunction;
import BritefuryJ.Projection.Perspective;

public class SequentialEditorPerspective extends Perspective
{
	public SequentialEditorPerspective(ViewFragmentFunction fragmentViewFn, SequentialController sequentialController)
	{
		super( fragmentViewFn, sequentialController.getClipboardHandler() );
	}
}
