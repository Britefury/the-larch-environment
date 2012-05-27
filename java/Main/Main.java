//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package Main;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;

public class Main
{
	public static void main(String[] args) throws PyException
	{
		PythonInterpreter interp = new PythonInterpreter();
//		interp.exec( "import sys" );
//		interp.exec( "sys.path.append( 'larch' )" );
		interp.exec( "from Britefury.app_larch import start_larch" );
		interp.exec( "start_larch()" );
	}
}
