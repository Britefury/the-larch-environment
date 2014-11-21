from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import Button, Checkbox, IntSlider, ToggleButton

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer

from LarchCore.ipython.widget import IPythonWidgetView


class ButtonView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_click(control, event):
			self._comm.send({'method': 'custom', 'content': {'event': 'click'}})

		return Button.buttonWithLabel(self.description, _on_click)

