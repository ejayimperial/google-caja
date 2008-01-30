#!/usr/bin/python

import re
import types

class Walkable(object):
  def children(self):
    raise NotImplementedError()

  def walk(self, op):
    op(self)
    for child in self.children():
      child.walk(op)


class Cloneable(object):
  def clone(identifier_remapper):
    raise NotImplementedError()


class Identifier(Walkable, Cloneable):
  def __init__(self, text, line_no=None):
    assert type(text) in types.StringTypes
    assert line_no is None or type(line_no) is types.IntType
    self.text = text
    self.line_no = line_no

  def clone(self, identifier_remapper):
    return Identifier(text=identifier_remapper(self).text, line_no=self.line_no)

  def children(self):
    return ()

  def __cmp__(self, other):
    assert isinstance(other, Identifier)
    return cmp(self.text, other.text)

  def __hash__(self):
    return hash(self.text)

  def __eq__(self, other):
    if not isinstance(other, Identifier):
      raise Exception('Comparing identifier %s to %r:%s'
          % (self, other, type(other)))
    return self.text == other.text

  def __str__(self):
    return self.text

  def __repr__(self):
    return 'Identifier(%r)' % self.text


class State(Walkable, Cloneable):
  def __init__(self, annotations, name):
    assert reduce(
        lambda a, b: type(b) in types.StringTypes and a, annotations, True)
    assert isinstance(name, Identifier)
    self.name = name
    self.annotations = tuple(annotations)
    self.transitions = []
    self.index = None

  def clone(self, identifier_remapper):
    clone = State(self.annotations, self.name.clone(identifier_remapper))
    clone.annotations = self.annotations[:]
    clone.transitions = [x.clone(identifier_remapper) for x in self.transitions]
    return clone

  def children(self):
    return [self.name] + list(self.transitions)

  def __str__(self):
    return '%s%s:%s\n  %s' % (
        len(self.annotations) and '%s ' % ' '.join(self.annotations) or '',
        self.name,
        ((self.index is not None) and (' #%s' % self.index) or ''),
        '\n  '.join([str(transition) for transition in self.transitions]))


class Variable(Walkable):
  def __init__(self, name, values):
    assert isinstance(name, Identifier)
    assert type(values) is set
    assert reduce(lambda a, b: type(b) in types.StringTypes and a, values, True)
    self.name = name
    self.values = values
    self.index = None

  def children(self):
    return (self.name,)

  def __str__(self):
    return '(var %s in %s%s)' % (
        self.name, self.values,
        (self.index is not None and (' #%s' % self.index) or ''))


class Alias(Walkable):
  def __init__(self, name, chars):
    assert isinstance(name, Identifier)
    assert reduce(
       lambda a, b: type(b) in types.StringTypes and len(b) == 1 and a,
       chars, True)
    self.name = name
    chars = list(chars[:])
    chars.sort()
    self.chars = tuple(chars)
    self.representative_char = chars[0]

  def children(self):
    return (self.name,)

  def __str__(self):
    chars = list(self.chars)
    chars.sort()
    return 'alias %s: %s' % (self.name, repr(tuple(chars)))


class VarDecl(Walkable):
  def __init__(self, annotations, name, values):
    assert reduce(
        lambda a, b: type(b) in types.StringTypes and a, annotations, True)
    assert isinstance(name, Identifier)
    assert reduce(lambda a, b: isinstance(b, VarValue) and a, values, True)
    self.annotations = tuple(annotations)
    self.name = name
    self.values = tuple(values)

  def children(self):
    return [self.name] + list(self.values)

  def __str__(self):
    return '%svar %s:\n  %s' % (
        len(self.annotations) and '%s ' % ' '.join(self.annotations) or '',
        self.name, '\n  '.join([str(x) for x in self.values]))


class VarValue(Walkable):
  def children(self):
    return ()

  def add_var_values(self, values):
    raise NotImplementedError()


class SingleVarValue(VarValue):
  def __init__(self, value):
    assert type(value) in types.StringTypes
    self.value = value

  def add_var_values(self, values):
    values.add(self.value)

  def __str__(self):
    return repr(self.value)


class UnionVarValue(VarValue):
  def __init__(self, name, values):
    assert isinstance(name, Identifier)
    assert reduce(
        lambda a, b: (type(b) in types.StringTypes) and a, values, True)
    self.name = name
    self.values = values

  def add_var_values(self, values):
    for value in self.values:
      values.add(value)

  def __str__(self):
    return 'union %s(%s)' % (
        self.name, ', '.join([repr(x) for x in self.values]))


class Function(Walkable, Cloneable):
  def __init__(self, annotations, name, var_name):
    assert reduce(
        lambda a, b: type(b) in types.StringTypes and a, annotations, True)
    assert isinstance(name, Identifier)
    assert var_name is None or isinstance(var_name, Identifier)
    self.annotations = tuple(annotations)
    self.name = name
    self.var_name = var_name
    self.transitions = []
    self.index = None

  def clone(self, identifier_remapper):
    clone = Function(
        annotations=self.annotations,
        name=self.name.clone(identifier_remapper),
        var_name=self.var_name)  # variable names not rewritten
    clone.transitions = [x.clone(identifier_remapper) for x in self.transitions]
    return clone

  def children(self):
    return [self.name, self.var_name] + list(self.transitions)

  def __str__(self):
    return '%sdef %s(%s):\n  %s' % (
        len(self.annotations) and '%s ' % ' '.join(self.annotations) or '',
        self.name, self.var_name and self.var_name or '',
        '\n  '.join([str(transition) for transition in self.transitions]))


class StateMachine(Walkable):
  def __init__(self):
    self.aliases = {}  # maps alias names to aliases
    self.var_decls = {}  # maps var names to variable declarations
    self.states = {}  # maps state names to states
    self.functions = {}  # maps function names to functions
    self.templates = {}  # maps template names to templates
    self.namespaces = {}  # maps namespace prefixes to namespaces
    self.variables = {}  # maps var_names to variables -- computed post parse

  def children(self):
    children = []
    children.extend(self.aliases.values())
    children.extend(self.functions.values())
    children.extend(self.states.values())
    children.extend(self.variables.values())
    return children

  def __str__(self):
    return ('StateMachine(\n  aliases=\n    %s,\n  states=\n    %s,'
            '\n  functions=\n    %s,\n  variables=\n    %s)') % (
        _indent('\n'.join([str(x) for x in self.aliases.values()]), 4),
        _indent('\n'.join([str(x) for x in self.states.values()]), 4),
        _indent('\n'.join([str(x) for x in self.functions.values()]), 4),
        _indent('\n'.join([str(x) for x in self.variables.values()]), 4))


class Pattern(Walkable, Cloneable):
  def add_input_chars(self, fsm, out_set):
    raise NotImplementedError()


class CharPattern(Pattern):
  def __init__(self, ch, overridden=False):
    assert type(ch) in types.StringTypes
    assert len(ch) == 1
    self.ch = ch
    self.overridden = overridden

  def clone(self, identifier_remapper):
    return CharPattern(self.ch, self.overridden)

  def children(self):
    return ()

  def add_input_chars(self, fsm, out_set):
    ch = self.ch
    if self.overridden: ch = '\\%s' % ch
    if ch in out_set:
      raise Exception('char %r multiply handled' % ch)
    out_set.add(ch)

  def __str__(self):
    if self.overridden:
      return '@overridden %r' % self.ch
    return repr(self.ch)

  def __repr__(self):
    return 'CharPattern(ch=%r)' % self.ch


class CharSetPattern(Pattern):
  def __init__(self, alias_name, overridden=False):
    assert isinstance(alias_name, Identifier)
    self.alias_name = alias_name
    self.overridden = overridden

  def clone(self, identifier_remapper):
    # aliases are not renamed
    return CharSetPattern(self.alias_name, self.overridden)

  def children(self):
    return (self.alias_name,)

  def add_input_chars(self, fsm, out_set):
    ch = fsm.aliases[self.alias_name].representative_char
    if self.overridden: ch = '\\%s' % ch
    if ch in out_set:
      raise Exception('alias %s multiply handled' % self.alias_name)
    out_set.add(ch)

  def __str__(self):
    if self.overridden: return '@overridden %s' % self.alias_name
    return self.alias_name.text

  def __repr__(self):
    return 'CharSetPattern(alias_name=%r)' % self.alias_name


class Transition(Walkable, Cloneable):
  def __init__(self, result):
    assert isinstance(result, Result)
    self.result = result


class ElseTransition(Transition):
  def __init__(self, result):
    Transition.__init__(self, result)

  def add_input_chars(self, fsm, out_set):
    out_set.add(None)

  def clone(self, identifier_remapper):
    return ElseTransition(self.result.clone(identifier_remapper))

  def children(self):
    return (self.result,)

  def __str__(self):
    return 'else %s' % self.result


class PatternTransition(Transition):
  def __init__(self, patterns, result):
    Transition.__init__(self, result)
    self.patterns = tuple(patterns)
    assert reduce(lambda a, b: isinstance(b, Pattern) and a, patterns, True)

  def add_input_chars(self, fsm, out_set):
    for pattern in self.patterns:
      pattern.add_input_chars(fsm, out_set)

  def clone(self, identifier_remapper):
    return PatternTransition(
        [x.clone(identifier_remapper) for x in self.patterns],
        self.result.clone(identifier_remapper))

  def children(self):
    return list(self.patterns) + [self.result]

  def __str__(self):
    return '%s %s' % (', '.join([str(x) for x in self.patterns]), self.result)


class FunctionTransition(Transition):
  def __init__(self, patterns, result):
    Transition.__init__(self, result)
    self.patterns = tuple(patterns)
    assert reduce(lambda a, b: isinstance(b, Pattern) and a, patterns, True)

  def children(self):
    return list(self.patterns) + [self.result]

  def __str__(self):
    return '%s %s' % (', '.join([str(x) for x in self.patterns]), self.result)


class StringPattern(Pattern):
  def __init__(self, value):
    Pattern.__init__(self)
    assert type(value) in types.StringTypes
    self.value = value

  def children(self):
    return ()

  def __str__(self):
    return repr(self.value)


class VariablePattern(Pattern):
  def __init__(self, var_name):
    Pattern.__init__(self)
    assert isinstance(var_name, Identifier)
    self.var_name = var_name

  def children(self):
    return (self.var_name,)

  def __str__(self):
    return str(self.var_name)


class Result(Walkable, Cloneable):
  def __init__(self, side_effects):
    assert reduce(
        lambda a, b: isinstance(b, SideEffect) and a, side_effects, True)
    self.side_effects = tuple(side_effects)


class TransitionResult(Result):
  def __init__(self, end_state, side_effects):
    Result.__init__(self, side_effects)
    assert isinstance(end_state, Identifier)
    self.end_state = end_state

  def clone(self, identifier_remapper):
    return TransitionResult(
        self.end_state.clone(identifier_remapper),
        [x.clone(identifier_remapper) for x in self.side_effects])

  def children(self):
    return [self.end_state] + list(self.side_effects)

  def __str__(self):
    return ': %s%s' % (
        self.end_state, ''.join([', %s' % x for x in self.side_effects]))


class ReferenceResult(Result):
  def __init__(self, target_name, side_effects):
    Result.__init__(self, side_effects)
    assert isinstance(target_name, Identifier)
    self.target_name = target_name

  def clone(self, identifier_remapper):
    return ReferenceResult(
        self.target_name.clone(identifier_remapper),
        [x.clone(identifier_remapper) for x in self.side_effects])

  def children(self):
    return [self.target_name] + list(self.side_effects)

  def __str__(self):
    return 'as %s%s' % (
        self.target_name, ''.join([', %s' % x for x in self.side_effects]))


class ErrorResult(Result):
  def __init__(self, message):
    Result.__init__(self, ())
    assert message is None or type(message) in types.StringTypes
    self.message = message

  def clone(self, identifier_remapper):
    return ErrorResult(self.message)

  def children(self):
    return ()

  def __str__(self):
    if self.message is not None:
      return ': error(%r)' % self.message
    else:
      return ': error'


class GoSubResult(Result):
  def __init__(self, end_state, side_effects):
    Result.__init__(self, side_effects)
    assert isinstance(end_state, Identifier)
    self.end_state = end_state

  def clone(self, identifier_remapper):
    return GoSubResult(
        self.end_state.clone(identifier_remapper),
        [x.clone(identifier_remapper) for x in self.side_effects])

  def children(self):
    return [self.end_state] + list(self.side_effects)

  def __str__(self):
    return ': gosub %s%s' % (
        self.end_state, ', '.join([str(x) for x in self.side_effects]))


class ReturnResult(Result):
  def __init__(self, side_effects):
    Result.__init__(self, side_effects)

  def clone(self, identifier_remapper):
    return ReturnResult(
        self.end_state.clone(identifier_remapper),
        [x.clone(identifier_remapper) for x in self.side_effects])

  def children(self):
    return self.side_effects

  def __str__(self):
    return ': return%s' % (', '.join([str(x) for x in self.side_effects]))


class SideEffect(Walkable):
  def __init__(self, name, params):
    assert type(name) in types.StringTypes
    self.name = name
    self.params = tuple(params)

  def children(self):
    return ()

  def __str__(self):
    return 'SideEffect(name=%r, params=%s)' % (self.name, self.params)

  def __repr__(self):
    return str(self)


class Template(Walkable):
  def __init__(self, name, definitions):
    assert isinstance(name, Identifier)
    assert reduce(
        lambda a, b: (isinstance(b, State) or isinstance(b, Function)) and a,
        definitions, True)
    self.name = name
    self.definitions = definitions

  def children(self):
    return [self.name] + list(self.definitions)

  def __str__(self):
    return 'template %s:\n  %s' % (
        '\n  '.join([str(d) for d in self.definitions]))


class Namespace(Walkable):
  def __init__(self, prefix, overrides, instantiations, members):
    assert type(prefix) in types.StringTypes
    assert reduce(lambda a, b: isinstance(b, Transition) and a, overrides, True)
    assert reduce(
        lambda a, b: isinstance(b, Identifier) and a, instantiations, True)
    assert reduce(
        lambda a, b: (isinstance(b, State) or isinstance(b, Function)) and a,
        members, True)

    self.prefix = prefix  # TODO: should this be an identifier?
    self.overrides = overrides
    self.instantiations = instantiations
    self.members = members

  def children(self):
    return list(self.overrides) + list(self.instantiations) + list(self.members)

  def __str__(self):
    return 'namespace %s:\n  *:%s%s%s' % (
        self.prefix,
        ''.join(['\n    %s' % x for x in self.overrides]),
        ''.join(['\n  %s' % x for x in self.instantiations]),
        ''.join(['\n  %s' % x for x in self.members]))


_NEWLINE = re.compile('^(?=.)', re.MULTILINE)
def _indent(s, n_spaces):
  return _NEWLINE.sub(' ' * n_spaces, s)
