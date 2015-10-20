from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import TrackedLiveValue, LiveFunction

from BritefuryJ.Controls import OptionMenu

from BritefuryJ.Pres.Primitive import Label

from BritefuryJ.Editor.Table.ObjectList import AttributeColumn, ObjectListTableEditor

from Britefury.Util.LiveList import LiveList

from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr


class EnumOptionMenu (object):
    def __init__(self, values, visuals, value_live):
        assert len(values) == len(visuals)
        self.__values = values
        self.__visuals = visuals
        self.__value_live = value_live

        @LiveFunction
        def index():
            return self.__values.index(self.__value_live.getValue())
        self.__index_live = index

        def listener(control, prev_choice, choice):
            self.__value_live.setLiteralValue(self.__values[choice])

        self.__listener = listener


    @property
    def static_value(self):
        return self.__value_live.getStaticValue()


    def __present__(self, fragmnet, inh):
        return OptionMenu(self.__visuals, self.__index_live, self.__listener).alignVRefY()


class OpLevelRow (object):
    def __init__(self, action=None):
        if action is None:
            action = EmbeddedPython2Expr()
        self.__action = TrackedLiveValue(action)
        self.__change_history__ = None


    def __getstate__(self):
        state = {}
        state['action'] = self.__action.getStaticValue()
        return state

    def __setstate__(self, state):
        self.__action = TrackedLiveValue(state['action'])
        self.__change_history__ = None


    def __get_trackable_contents__(self):
        return [ self.__action ]


    def __clipboard_copy__(self, memo):
        action = memo.copy(self.__action.getStaticValue())
        return OpLevelRow(action)



    @property
    def action(self):
        return self.__action.getValue()

    @action.setter
    def action(self, value):
        self.__action.setLiteralValue(value)




class OpLevelTable (LiveList):
    # _operator_expr_column = AttributeColumn('Op. expr', 'op_expr')
    _operator_action_column = AttributeColumn('Op. action', 'action', EmbeddedPython2Expr)

    # _table_editor = ObjectListTableEditor([_operator_expr_column, _operator_action_column],
    #                                       OpLevelRow, True, True, True, True)
    _table_editor = ObjectListTableEditor([_operator_action_column],
                                          OpLevelRow, True, True, True, True)


    def __present__(self, fragment, inh):
        return self._table_editor.editTable(self)




class OperatorTableRow (object):
    LEVEL_TYPE_PREFIX = 'prefix'
    LEVEL_TYPE_SUFFIX = 'suffix'
    LEVEL_TYPE_INFIX_LEFT = 'infix-left'
    LEVEL_TYPE_INFIX_RIGHT = 'infix-right'
    LEVEL_TYPE_INFIX_CHAIN = 'infix-chain'
    # LEVEL_TYPE_INFIX_UNIFORM_CHAIN = 'infix-uniform-chain'

    _LEVEL_TYPE_VALUES = [
        LEVEL_TYPE_PREFIX,
        LEVEL_TYPE_SUFFIX,
        LEVEL_TYPE_INFIX_LEFT,
        LEVEL_TYPE_INFIX_RIGHT,
        LEVEL_TYPE_INFIX_CHAIN,
        # LEVEL_TYPE_INFIX_UNIFORM_CHAIN
    ]

    _LEVEL_TYPE_LABELS = [Label(x) for x in [
                        'Prefix',
                        'Suffix',
                        'Infix left assoc.',
                        'Infix right assoc.',
                        'Infix chain',
                        # 'Infix uniform chain',
    ]]

    def __init__(self):
        self.__rule_name = TrackedLiveValue('')
        self.__level_type = TrackedLiveValue(self.LEVEL_TYPE_PREFIX)
        self.__operators = TrackedLiveValue(OpLevelTable())
        self.__change_history__ = None

        self.__level_type_view = EnumOptionMenu(self._LEVEL_TYPE_VALUES, self._LEVEL_TYPE_LABELS, self.__level_type)



    def __getstate__(self):
        state = {}
        state['rule_name'] = self.__rule_name.getStaticValue()
        state['level_type'] = self.__level_type.getStaticValue()
        state['operators'] = self.__operators.getStaticValue()
        return state

    def __setstate__(self, state):
        self.__rule_name = TrackedLiveValue(state['rule_name'])
        self.__level_type = TrackedLiveValue(state['level_type'])
        self.__operators = TrackedLiveValue(state['operators'])
        self.__change_history__ = None
        self.__level_type_view = EnumOptionMenu(self._LEVEL_TYPE_VALUES, self._LEVEL_TYPE_LABELS, self.__level_type)


    def __get_trackable_contents__(self):
        return [ self.__rule_name, self.__level_type, self.__operators ]


    @property
    def rule_name(self):
        return self.__rule_name.getValue()

    @rule_name.setter
    def rule_name(self, value):
        self.__rule_name.setLiteralValue(value)


    @property
    def level_type(self):
        return self.__level_type.getValue()

    @level_type.setter
    def level_type(self, value):
        self.__level_type.setLiteralValue(value)


    @property
    def level_type_view(self):
        return self.__level_type_view

    @level_type_view.setter
    def level_type_view(self, view):
        self.__level_type.setLiteralValue(view.static_value)


    @property
    def operators(self):
        return self.__operators.getValue()

    @operators.setter
    def operators(self, value):
        if value is None:
            value = OpLevelTable()
        self.__operators.setLiteralValue(value)






class OperatorTable (LiveList):
    _rule_name_column = AttributeColumn('Grammar rule', 'rule_name', str)
    _level_type_column = AttributeColumn('Precedence level', 'level_type_view')
    _operators_column = AttributeColumn('Operators', 'operators')

    _table_editor = ObjectListTableEditor([_rule_name_column, _level_type_column, _operators_column],
                                          OperatorTableRow, True, True, True, True)

    def __present__(self, fragment, inh):
        return self._table_editor.editTable(self)


