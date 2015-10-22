from java.awt import Color

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import TrackedLiveValue, LiveFunction

from BritefuryJ.Parser import Production
from BritefuryJ.Parser.Utils.OperatorParser import UnaryOperator, BinaryOperator, ChainOperator, \
    InfixLeftLevel, InfixRightLevel, InfixChainLevel, PrefixLevel, SuffixLevel, OperatorTable

from BritefuryJ.Controls import OptionMenu

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Label, Spacer, Row, Column
from BritefuryJ.Pres.UI import SectionHeading2, SectionHeading3

from BritefuryJ.Editor.Table.ObjectList import AttributeColumn, ObjectListTableEditor

from Britefury.Util.LiveList import LiveList

from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr
from LarchTools.GrammarEditor.GrammarEditor import GrammarExpressionEditor


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
    def __init__(self, op_expr=None, action=None):
        if op_expr is None:
            op_expr = GrammarExpressionEditor()
        if action is None:
            action = EmbeddedPython2Expr()
        self.__op_expr = TrackedLiveValue(op_expr)
        self.__action = TrackedLiveValue(action)
        self.__change_history__ = None


    def __getstate__(self):
        state = {}
        state['op_expr'] = self.__op_expr.getStaticValue()
        state['action'] = self.__action.getStaticValue()
        return state

    def __setstate__(self, state):
        self.__op_expr = TrackedLiveValue(state['op_expr'])
        self.__action = TrackedLiveValue(state['action'])
        self.__change_history__ = None


    def __get_trackable_contents__(self):
        return [ self.__op_expr, self.__action ]


    def __clipboard_copy__(self, memo):
        op_expr = memo.copy(self.__op_expr.getStaticValue())
        action = memo.copy(self.__action.getStaticValue())
        return OpLevelRow(op_expr, action)


    def op_parser_expression(self, context):
        return self.__op_expr.getStaticValue().parser_expression(context)

    def parse_action(self, context):
        return self.__action.getStaticValue().evaluate(context.module.__dict__, None)

    def unary_operator(self, context):
        f = self.parse_action(context)
        return UnaryOperator(self.op_parser_expression(context), lambda input, begin, end, subexp, op: f(subexp, op))

    def binary_operator(self, context):
        f = self.parse_action(context)
        return BinaryOperator(self.op_parser_expression(context), lambda input, begin, end, left, op, right: f(left, op, right))

    def chain_operator(self, context):
        f = self.parse_action(context)
        return ChainOperator(self.op_parser_expression(context), lambda input, begin, end, subexp, op: f(subexp, op))


    @property
    def op_expr(self):
        return self.__op_expr.getValue()

    @op_expr.setter
    def op_expr(self, value):
        self.__op_expr.setLiteralValue(value)


    @property
    def action(self):
        return self.__action.getValue()

    @action.setter
    def action(self, value):
        self.__action.setLiteralValue(value)




class OpLevelTable (LiveList):
    _operator_expr_column = AttributeColumn('Op. expr', 'op_expr', GrammarExpressionEditor)
    _operator_action_column = AttributeColumn('Op. action', 'action', EmbeddedPython2Expr)

    _table_editor = ObjectListTableEditor([_operator_expr_column, _operator_action_column],
                                          OpLevelRow, True, True, True, True)


    def __present__(self, fragment, inh):
        return self._table_editor.editTable(self)


_pyActionBorder = SolidBorder( 1.5, 4.0, 10.0, 10.0, Color( 0.2, 0.75, 0.0 ), None )

class _OpTableRowOperatorsView (object):
    def __init__(self, level_type, operators, level_action):
        self.level_type = level_type
        self.operators = operators
        self.level_action = level_action

    def __present__(self, fragment, inh):
        if self.level_type == OperatorTableRow.LEVEL_TYPE_INFIX_CHAIN:
            return Column([self.operators, Spacer(0.0, 5.0), Row([SectionHeading3('Level action: ').alignHPack().alignVRefY(),
                                                                  _pyActionBorder.surround(self.level_action).alignHExpand().alignVRefY()])])
        else:
            return Pres.coerce(self.operators)


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
        self.__level_action = TrackedLiveValue(EmbeddedPython2Expr())
        self.__change_history__ = None

        self.__level_type_view = EnumOptionMenu(self._LEVEL_TYPE_VALUES, self._LEVEL_TYPE_LABELS, self.__level_type)



    def __getstate__(self):
        state = {}
        state['rule_name'] = self.__rule_name.getStaticValue()
        state['level_type'] = self.__level_type.getStaticValue()
        state['operators'] = self.__operators.getStaticValue()
        state['level_action'] = self.__level_action.getStaticValue()
        return state

    def __setstate__(self, state):
        self.__rule_name = TrackedLiveValue(state['rule_name'])
        self.__level_type = TrackedLiveValue(state['level_type'])
        self.__operators = TrackedLiveValue(state['operators'])
        level_action = state.get('level_action')
        if level_action is None:
            level_action = EmbeddedPython2Expr()
        self.__level_action = TrackedLiveValue(level_action)
        self.__change_history__ = None
        self.__level_type_view = EnumOptionMenu(self._LEVEL_TYPE_VALUES, self._LEVEL_TYPE_LABELS, self.__level_type)


    def __get_trackable_contents__(self):
        return [ self.__rule_name, self.__level_type, self.__operators ]


    def forward_declaration(self):
        return Production(self.__rule_name.getValue())


    def operator_level(self, context):
        level_type = self.__level_type.getStaticValue()
        if level_type == self.LEVEL_TYPE_PREFIX:
            return PrefixLevel([op.unary_operator(context) for op in self.__operators.getStaticValue()])
        elif level_type == self.LEVEL_TYPE_SUFFIX:
            return SuffixLevel([op.unary_operator(context) for op in self.__operators.getStaticValue()])
        elif level_type == self.LEVEL_TYPE_INFIX_LEFT:
            return InfixLeftLevel([op.binary_operator(context) for op in self.__operators.getStaticValue()])
        elif level_type == self.LEVEL_TYPE_INFIX_RIGHT:
            return InfixRightLevel([op.binary_operator(context) for op in self.__operators.getStaticValue()])
        elif level_type == self.LEVEL_TYPE_INFIX_CHAIN:
            f = self.__level_action.getStaticValue().evaluate(context.module.__dict__, None)
            level_action = lambda input, begin, end, x, ys: f(x, ys)
            return InfixChainLevel([op.chain_operator(context) for op in self.__operators.getStaticValue()], level_action)
        else:
            raise ValueError('Unknown level type \'{0}\''.format(level_type))



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


    @property
    def operators_view(self):
        return _OpTableRowOperatorsView(self.__level_type.getValue(), self.__operators.getValue(), self.__level_action.getValue())

    @operators_view.setter
    def operators_view(self, value):
        if value is None:
            self.__operators.setLiteralValue(OpLevelTable())
            self.__level_action.setLiteralValue(EmbeddedPython2Expr())
        else:
            self.__operators.setLiteralValue(value.operators)
            self.__level_action.setLiteralValue(value.level_action)






class GrammarOperatorTable (object):
    def __init__(self, incoming_expr=None, levels=None):
        super( GrammarOperatorTable, self ).__init__()
        if incoming_expr is None:
            incoming_expr = GrammarExpressionEditor()
        levels = LiveList(levels)
        self.__incoming_expr = TrackedLiveValue(incoming_expr)
        self.__levels = levels
        self.__change_history__ = None



    def __getstate__(self):
        state = {}
        state['incoming_expr'] = self.__incoming_expr.getStaticValue()
        state['levels'] = self.__levels
        return state

    def __setstate__(self, state):
        self.__incoming_expr = TrackedLiveValue( state['incoming_expr'] )
        self.__levels = state['levels']
        self.__change_history__ = None


    def __get_trackable_contents__(self):
        return [ self.__incoming_expr, self.__levels ]


    def forward_declarations(self):
        return [lvl.forward_declaration() for lvl in self.__levels]

    def build_parsers(self, forward_declarations, context):
        root_parser = self.__incoming_expr.getStaticValue().parser_expression(context)
        ops = OperatorTable([lvl.operator_level(context) for lvl in self.__levels])
        ops.buildParsers(forward_declarations, root_parser)


    @property
    def levels(self):
        return self.__levels

    @property
    def incoming_expr(self):
        return self.__incoming_expr.getValue()


    __embed_hide_frame__ = True

    def __present__(self, fragment, inheritedState):
        title = SectionHeading2( 'Operators' )
        incoming = Row([SectionHeading3('Incoming expr: '), self.__incoming_expr])

        header = Column( [ title, incoming ] )

        table = self._table_editor.editTable( self.__levels )

        return self._operator_table_border.surround( Column( [ header, Spacer( 0.0, 5.0 ), table ] ) )


    _rule_name_column = AttributeColumn('Grammar rule', 'rule_name', str)
    _level_type_column = AttributeColumn('Precedence level', 'level_type_view')
    _operators_column = AttributeColumn('Operators', 'operators_view')

    _table_editor = ObjectListTableEditor([_rule_name_column, _level_type_column, _operators_column],
                                          OperatorTableRow, True, True, True, True)

    _operator_table_border = SolidBorder( 1.5, 3.0, 5.0, 5.0, Color( 0.4, 0.4, 0.5 ), None )


