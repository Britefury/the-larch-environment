##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from GSymCore.Languages.Python25 import ExternalExpression
from GSymCore.Languages.Python25 import Prelude

from GSymCore.PresTemplate import PresTemplate
from GSymCore.PresTemplate import Schema



def initPlugin(plugin, world):
	world.registerSchema( Schema.schema )
	ExternalExpression.registerExternalExpressionFactory( 'Presenation template', PresTemplate.newTemplate )
	ExternalExpression.registerExternalExpressionPresenterAndTitle( Schema.schema, PresTemplate.pythonExternalExpressionPresenter, 'Pres template' )
	ExternalExpression.registerExternalExpressionCodeGeneratorFactory( Schema.schema, PresTemplate.pythonExternalExpressionCodeGeneratorFactory )
	Prelude.registerPrelude( PresTemplate.presTemplatePrelude )
	
