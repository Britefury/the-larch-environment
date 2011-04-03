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

import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Target.Target;

public abstract class DataImporterInterface <TargetType extends Target>
{
	abstract protected List<DataFlavor> getDataFlavors();
	abstract protected boolean canImport(TargetType target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException;
	abstract protected boolean importData(TargetType target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException;
}
