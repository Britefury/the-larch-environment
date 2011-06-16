//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import BritefuryJ.DocPresent.Selection.Selection;

public abstract class DataExporterInterface <SelectionContentsType, SelectionType extends Selection>
{
	abstract protected List<DataFlavor> getTransferDataFlavors(SelectionContentsType selectionContents);
	abstract protected Object getTransferData(SelectionContentsType selectionContents, SelectionType selection, DataFlavor flavor) throws UnsupportedFlavorException, IOException;
}
