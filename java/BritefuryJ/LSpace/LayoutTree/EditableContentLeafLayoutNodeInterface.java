//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSContentLeafEditable;

public interface EditableContentLeafLayoutNodeInterface extends ContentLeafLayoutNodeInterface
{
	LSContentLeafEditable getLeftEditableContentLeaf();
	LSContentLeafEditable getRightEditableContentLeaf();
}
