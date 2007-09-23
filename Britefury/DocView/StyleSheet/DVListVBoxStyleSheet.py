##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection

from Britefury.DocView.StyleSheet.DVListBoxStyleSheet import DVListBoxStyleSheet




class DVListVBoxStyleSheet (DVListBoxStyleSheet):
	elementDirection = DTDirection.TOP_TO_BOTTOM
	overallDirection = DTDirection.TOP_TO_BOTTOM
