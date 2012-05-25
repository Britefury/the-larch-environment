##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Inspect import JavaInspectorPerspective as _JavaInspectorPerspective
from BritefuryJ.Inspect import InspectorPerspective as _InspectorPerspective
from BritefuryJ.DefaultPerspective import DefaultPerspective as _DefaultPerspective
from BritefuryJ.EditPerspective import EditPerspective as _EditPerspective



javaInspect = _JavaInspectorPerspective.instance
inspect = _InspectorPerspective.instance
defaultPerspective = _DefaultPerspective.instance
edit = _EditPerspective.instance
