#!/usr/bin/python

import StringIO
import partition
import re
import state_machine
import sys
import types

# True to generate logging statements and assertions within the output code.
# Assumes the presence of firebug or a similar implementation of console.log.
_DEBUG = False

# A variable that stores the index of the instruction left by gosub'
_RETURN_PC_VAR_NAME = state_machine.Identifier('__return_pc')

class CompilationOptions(object):
  def __init__(self, type_prefix='', complete_output=False):
    self.type_prefix = type_prefix
    self.complete_output = complete_output

def compile(fsm, options, out):
  assert isinstance(fsm, state_machine.StateMachine)
  assert isinstance(options, CompilationOptions)

  expand_namespaces(fsm)

  variable_value_inequalities = {}

  check_variable_usage(fsm, variable_value_inequalities)
  check_state_graph(fsm)
  n_states = assign_state_indices(fsm)
  n_variables = assign_variable_indices(fsm)
  alias_table = _compile_alias_table(fsm)
  compiled_variables = compile_variable_value_tables(
      fsm, variable_value_inequalities)
  state_bitfield_structure = layout_state_bits(compiled_variables, n_states)
  function_list = compile_functions(fsm)
  state_transition_table = compile_transition_table(
      fsm, function_list, alias_table)

  render_js(
      fsm, alias_table, compiled_variables, state_bitfield_structure,
      function_list, state_transition_table, options, out)

def expand_namespaces(fsm):
  """
  For each namespace, apply its templates and overrides to create actual states
  and functions.

  Namespace application proceeds as follows:
  (1) Walk over all states and functions in all instantiated templates and name
      that the set of identifiers_to_prefix.
  (2) For each template, copy each state and function definition replacing any
      occurrence of an identifier in identifiers_to_prefix with the namespace
      prefix + identifier.
  (3) For each state in the namespace, add the overides, replacing any
      existing transitions in the state for that char/alias.
  (4) Take the set of states and functions in the namespace and import it into
      the fsm.
  """

  for namespace in fsm.namespaces.values():
    # (1)
    identifiers_to_prefix = {}
    for template in fsm.templates.values():
      for definition in template.definitions:
        identifiers_to_prefix[definition.name] = state_machine.Identifier(
            '%s%s' % (namespace.prefix, definition.name),
            line_no=definition.name.line_no)

    definitions = list(namespace.members)
    for template in fsm.templates.values():
      for definition in template.definitions:
        # (2)
        definitions.append(definition.clone(
            identifier_remapper=lambda a: identifiers_to_prefix.get(a, a)))

    # (3)
    chars = set()
    alias_names = set()
    has_else = False
    def override_footprint(walkable):
      if isinstance(walkable, state_machine.CharPattern):
        chars.add(walkable.ch)
      if isinstance(walkable, state_machine.CharSetPattern):
        alias_names.add(walkable.alias_name)
      if isinstance(walkable, state_machine.ElseTransition):
        has_else = True
        return False
      return True

    for override in namespace.overrides:
      override.walk(override_footprint)

    for definition in definitions:
      if not isinstance(definition, state_machine.State): continue
      transitions = _mask_transitions(
          definition.transitions, chars=chars, alias_names=alias_names,
          has_else=has_else)
      definition.transitions = tuple(
          list(namespace.overrides) + list(transitions))

    # (4)
    for definition in definitions:
      if isinstance(definition, state_machine.State):
        if definition.name in fsm.states:
          raise Exception('state %s @ line %s has been redefined at %s'
              % (definition.name, fsm.states[definition.name].name.line_no,
                 definition.name.line_no))
        fsm.states[definition.name] = definition
      elif isinstance(definition, state_machine.Function):
        if definition.name in fsm.functions:
          raise Exception('function %s has been multiply defined'
              % definition.name)
        fsm.functions[definition.name] = definition
      else:
        raise Exception('Unrecognized definition in namespace: %s' % definition)

def _mask_transitions(transitions, chars, alias_names, has_else):
  """
  Remove the transitions without mappings for the given characters, alias, or
  an else transition if has_else.
  """
  out = []
  for transition in transitions:
    if isinstance(transition, state_machine.ElseTransition):
      if not has_else:
        out.append(transition)
    else:
      out_patterns = []
      for pattern in transition.patterns:
        if isinstance(pattern, state_machine.CharPattern):
          if pattern.ch in chars:
            out_patterns.append(
                state_machine.CharPattern(pattern.ch, overridden=True))
          else:
            out_patterns.append(pattern)
        else:
          assert isinstance(pattern, state_machine.CharSetPattern)
          if pattern.alias_name in alias_names:
            out_patterns.append(
                state_machine.CharSetPattern(
                    pattern.alias_name, overridden=True))
          else:
            out_patterns.append(pattern)
      out.append(
          state_machine.PatternTransition(out_patterns, transition.result))
  return out  

def check_variable_usage(fsm, variable_value_inequalities):
  variables_declared = set()
  variables_used = set()
  variable_values = {}

  _check_variable_decls(
      fsm=fsm, variables_declared=variables_declared,
      variables_used=variables_used, variable_values=variable_values,
      variable_value_inequalities=variable_value_inequalities)

  _check_variable_usage(
      fsm=fsm, variables_declared=variables_declared,
      variables_used=variables_used, variable_values=variable_values,
      variable_value_inequalities=variable_value_inequalities)

  if _RETURN_PC_VAR_NAME in variables_used:
    if _RETURN_PC_VAR_NAME in variable_values:
      value_set = variable_values[_RETURN_PC_VAR_NAME]
      for value in value_set:
        assert Identifier(value) in fsm.states
    variable_values[_RETURN_PC_VAR_NAME] = set([x.text for x in fsm.states])

  for var_name in variables_declared:
    if var_name not in variable_values:
      variable_values[var_name] = set()

  for var_name, value_set in variable_values.items():
    fsm.variables[var_name] = state_machine.Variable(var_name, value_set)

def _check_variable_decls(
    fsm, variables_declared, variables_used, variable_values,
    variable_value_inequalities):

  for decl in fsm.var_decls.values():
    variables_declared.add(decl.name)
    values = variable_values.get(decl.name)
    if decl.name in variable_values:
      raise Exception('Variable %s multiply declared at %s'
          % (decl.name, decl.name.line_no))

    values = variable_values[decl.name] = set()
    inequalities = variable_value_inequalities[decl.name] = set()

    for decl_value in decl.values:
      decl_value.add_var_values(values)

    values_defined = list(decl.values)
    for i in xrange(0, len(values_defined)):
      decl_value1 = values_defined[i]
      strings_1 = set()
      decl_value1.add_var_values(strings_1)

      for decl_value2 in values_defined[i+1:]:
        strings_2 = set()
        decl_value2.add_var_values(strings_2)
        for s1 in strings_1:
          for s2 in strings_2:
            assert s1 != s2
            inequalities.add((s1, s2))

def _check_variable_usage(
    fsm, variables_declared, variables_used, variable_values,
    variable_value_inequalities):
  """
  Examine patterns and side effects to see the possible states that each
  variable can assume.
  """

  subsumes = {}

  def visit(walkable):
    if isinstance(walkable, state_machine.VariablePattern):
      variables_used.add(walkable.var_name)
    elif isinstance(walkable, state_machine.SideEffect):
      var_name = walkable.params[0]
      if walkable.name in ('clear', 'store', 'decode'):
        variables_used.add(var_name)
      elif walkable.name in ('set'):
        variables_declared.add(var_name)
        values = variable_values.get(var_name)
        if values is None: variable_values[var_name] = values = set()
        values.add(walkable.params[1])
      elif walkable.name == 'record':
        variables_declared.add(var_name)
      else:
        raise Exception('Unkown side effect %s' % walkable)
    elif isinstance(walkable, state_machine.ReturnResult):
      variables_used.add(_RETURN_PC_VAR_NAME)
    elif isinstance(walkable, state_machine.GoSubResult):
      variables_declared.add(_RETURN_PC_VAR_NAME)
    elif isinstance(walkable, state_machine.Function):
      var_name = walkable.var_name
      assert var_name is not None
      variables_used.add(var_name)
      values = variable_values.get(var_name)
      if values is None:
        variable_values[var_name] = values = set()
      for transition in walkable.transitions:
        if not isinstance(transition, state_machine.FunctionTransition):
          continue
        for pattern in transition.patterns:
          if isinstance(pattern, state_machine.StringPattern):
            values.add(pattern.value)
          elif isinstance(pattern, state_machine.VariablePattern):
            subsumed = subsumes.get(var_name)
            if subsumed is None: subsumes[var_name] = subsumed = set()
            subsumed.add(pattern.var_name)

  fsm.walk(visit)

  def subsume(var_name):
    if var_name in processed: return
    if var_name in processing:
      raise Exception('Variable %s subsumes itself via cycle containing %s'
          % processing)
    processing.add(var_name)

    for subsumed_var_name in subsumes[var_name]:
      if subsumed_var_name in subsumes:
        subsume(subsumed_var_name)
      if subsumed_var_name not in variable_values:
        raise Exception('variable %s subsumes a variable %s with no values'
            % (var_name, subsumed_var_name))

      variable_values[var_name] = variable_values[var_name].union(
          variable_values[subsumed_var_name])

      # If a variable needs to be compared to another, we can't union values.
      # We could if we established that they unioned the same set, and that
      # each equivalence set mapped to the same canonical value.
      if var_name in variable_value_inequalities:
        del variable_value_inequalities[var_name]
      if subsumed_var_name in variable_value_inequalities:
        del variable_value_inequalities[subsumed_var_name]

    processed.add(var_name)
    processing.remove(var_name)

  processed = set()
  processing = set()
  for var_name in subsumes:
    subsume(var_name)

  for var_name in variables_used.symmetric_difference(variables_declared):
    if var_name not in variables_used:
      raise Exception('variable %s is declared, but not used' % var_name)
    else:
      raise Exception('variable %s is used, but never set' % var_name)

def check_state_graph(fsm):
  states_declared = set()
  states_used = set()

  _check_state_graph(fsm, states_declared, states_used)

  # TODO: determine proper reachability from @start_states to identify
  # disconnected subgraphs
  for state_name in states_used.symmetric_difference(states_declared):
    if state_name not in states_declared:
      raise Exception('state %s used but not declared' % state_name)
    elif state_name not in fsm.states:
      if state_name not in fsm.functions:
        raise Exception('state %s never defined' % state_name)
      else:
        raise Exception(
            ('function %s not used.  '
             'functions cannot be used as start states') % state_name)
    elif '@start_state' not in fsm.states[state_name].annotations:
      raise Exception('state %s not used and not annotated with @start_state'
          % state_name)

def _check_state_graph(fsm, states_declared, states_used):
  def visit(walkable):
    if (isinstance(walkable, state_machine.State)
        or isinstance(walkable, state_machine.Function)):
      if walkable.name in states_declared:
        raise Exception('State %s multiply declared' % walkable.name)
      else:
        states_declared.add(walkable.name)
    elif isinstance(walkable, state_machine.TransitionResult):
      states_used.add(walkable.end_state)
    elif isinstance(walkable, state_machine.ReferenceResult):
      states_used.add(walkable.target_name)
    elif isinstance(walkable, state_machine.GoSubResult):
      states_used.add(walkable.end_state)
  fsm.walk(visit)

def assign_state_indices(fsm):
  counter = 1  # Leave 0 as a null/error state, to agree with return_pc.
  states = fsm.states.values()
  states.sort(lambda a, b: cmp(a.name, b.name))
  for state in states:
    state.index = counter
    counter += 1
  return counter

def assign_variable_indices(fsm):
  variables = list(fsm.variables.values())
  variables.sort(lambda a, b: cmp(a.name, b.name))
  counter = 0
  for variable in variables:
    variable.index = counter
    counter += 1
  return counter

class _CompiledVariableState(object):
  def __init__(self, var, inequalities):
    self.var = var
    self.value_arr = []
    self.value_map = {}

    # Normally, add a value for "other".  This is always bit 0, so that a state
    # variable of 0 indicates all variable state unknown.
    self.value_arr.append(None)
    self.value_map[None] = 0

    values = list(var.values)
    values.sort()

    if inequalities is None:
      for value in values:
        self.value_map[value] = len(self.value_arr)
        self.value_arr.append(value)
    else:
      declared_values = set()
      for a, b in inequalities:
        declared_values.add(a)
        declared_values.add(b)
      if not declared_values.issuperset(var.values):
        raise Exception('declared var %s uses values that were not declared'
            % var.name)
      index = len(self.value_arr)
      for equivalence_set in partition.partition(values, inequalities):
        eset_els = list(equivalence_set)
        eset_els.sort()
        canonical_value = eset_els[0]
        index = len(self.value_arr)
        for value in equivalence_set:
          self.value_map[value] = index
        self.value_arr.append(canonical_value)

    self.bit_width = ceil_log_2_of(len(self.value_arr))

  def __str__(self):
   return 'var %s#%d(%db): arr=%s, map=%s' % (
       self.var.name, self.var.index, self.bit_width, self.value_arr,
       self.value_map)


def _compile_alias_table(fsm):
  alias_table = {}
  for alias in fsm.aliases.values():
    repr_char = alias.representative_char
    for char in alias.chars:
      if char == repr_char: continue
      if char in alias_table:
        raise Exception('Character %r in alias %s already aliased to %r'
            % (char, alias.name, alias_table[char]))
      alias_table[char] = repr_char
  return alias_table

def compile_variable_value_tables(fsm, variable_value_inequalities):
  variables = fsm.variables.values()
  variables.sort(lambda a, b: cmp(a.index, b.index))

  compiled_vars = [
      _CompiledVariableState(var, variable_value_inequalities.get(var.name))
      for var in variables]

  for compiled_var in compiled_vars:
    if compiled_var.var.name == _RETURN_PC_VAR_NAME:
      for state in fsm.states.values():
        assert state.index == compiled_var.value_map[state.name.text], (
            'state %s #%s in position %s in variable' % (
            state.name, state.index, compiled_var.value_map[state.name.text]))

  return compiled_vars

def result_to_effect(result, fsm):
  if isinstance(result, state_machine.TransitionResult):
    if result.end_state in fsm.functions:
      return ('invoke', (fsm.functions[result.end_state].index,))
    else:
      return ('goto', (fsm.states[result.end_state].index,))
  elif isinstance(result, state_machine.ReferenceResult):
    if result.target_name in fsm.functions:
      return ('invoke', (fsm.functions[result.target_name].index,))
    else:
      return ('run_state', (fsm.states[result.target_name].index,))
  elif isinstance(result, state_machine.ErrorResult):
    return ('error', (result.message or '',))
  elif isinstance(result, state_machine.GoSubResult):
    target_name = result.end_state
    if target_name in fsm.states:
      index = fsm.states[target_name].index
    else:
      raise Exception('unknown end state %s' % target_name)
    return ('gosub', (index,))
  elif isinstance(result, state_machine.ReturnResult):
    return ('return', ())
  else:
    raise AssertionError(str(result))

def compile_transition_table(fsm, function_list, alias_table):
  # maps (char, start_state) to end_state.  None for char indicates else
  # if end state is negative, it is an index into the side-effect array
  transition_table = {}
  # an array of functions that take the current character and state and return
  # an end state
  side_effect_cache = {}

  def define_handler(result):
    # generate the list of results so we can reuse equivalent functions
    operations = [(eff.name, tuple(eff.params)) for eff in result.side_effects]
    result_effect = result_to_effect(result, fsm)
    operations.append(result_effect)
    operations = tuple(operations)
    k = side_effect_cache.get(operations)
    if k is None:
      k = len(function_list)
      handler_name = state_machine.Identifier('anon$%d__%s'
          % (k, re.sub(r'\W+', '_', str(result_effect))))
      anonymous_function = state_machine.Function((), handler_name, None)
      anonymous_function.transitions.append(
          state_machine.ElseTransition(result))
      anonymous_function.index = k
      function_list.append(anonymous_function)
      side_effect_cache[operations] = k
    return ~k

  def add_mapping(chars, state, result, transitions_handled):
    """
    Add transitions for the given char (or all unhandled chars if ch is None)
    and the given state.

    ch: the character to add transitions for, or None for 'else' transitions
    state: the current state_machine.State
    result: the result of the transition
    transitions_handled: characters (or representative characters for aliases)
        already handled, that needn't be handled by reference states.
    """
    if len(chars) == 0: return
    if isinstance(result, state_machine.TransitionResult):
      if result.end_state in fsm.states and len(result.side_effects) == 0:
        end_state_index = fsm.states[result.end_state].index

        transition_result = end_state_index
        for ch in chars:
          transition_table[(state.index, ch)] = transition_result
          transitions_handled.add(ch)
      else:
        handler = define_handler(result)
        for ch in chars:
          transition_table[(state.index, ch)] = handler
          transitions_handled.add(ch)
    elif isinstance(result, state_machine.ReferenceResult):
      dest_name = result.target_name
      dest_state = fsm.states.get(dest_name)
      if dest_state is not None:
        for dest_transition in dest_state.transitions:
          dest_chars = set()
          dest_transition.add_input_chars(fsm, dest_chars)
          dest_chars.intersection_update(chars)
          dest_chars.difference_update(transitions_handled)
          if len(dest_chars):
            add_mapping(dest_chars, state, dest_transition.result,
                        transitions_handled)
      else:
        handler = define_handler(result)
        for ch in chars:
          transition_table[(state.index, ch)] = handler
          transitions_handled.add(ch)
    else:
      assert (isinstance(result, state_machine.ErrorResult)
              or isinstance(result, state_machine.GoSubResult)
              or isinstance(result, state_machine.ReturnResult))
      handler = define_handler(result)
      for ch in chars:
        transition_table[(state.index, ch)] = handler

  for state in fsm.states.values():
    # Set of characters handled by this state.  Used to only pull in the needed
    # transitions when evaluating "as <state>" transitions.
    transitions_handled = set()
    for transition in state.transitions:
      result = transition.result
      if isinstance(result, state_machine.ReferenceResult): continue
      chars = set()
      transition.add_input_chars(fsm, chars)
      for ch in chars:
        if ch in alias_table:
          raise Exception('char %r cannot be handled separately from its alias'
              % ch)
        if ch in transitions_handled:
          raise Exception('char %r multiply handled in state %s'
              % (ch, state.name))
        add_mapping((ch,), state, result, transitions_handled)
    
    for transition in state.transitions:
      result = transition.result
      if not isinstance(result, state_machine.ReferenceResult): continue
      chars = set()
      transition.add_input_chars(fsm, chars)
      for ch in chars:
        add_mapping((ch,), state, result, transitions_handled)

  return transition_table

def compile_functions(fsm):
  functions = fsm.functions.values()
  functions.sort(lambda a, b: cmp(a.name, b.name))
  index = 0
  for function in functions:
    function.index = index
    index += 1
  return functions

def ceil_log_2_of(n):
  i = 0
  while True:
   if (1L << i) >= n:
     return i
   i += 1

class _StateBitfieldLayout(object):
  def __init__(self, n_bits_for_state, variable_bits):
    self.state_bits = (0, n_bits_for_state)
    self.variable_bits = []
    n = n_bits_for_state
    for k in variable_bits:
      self.variable_bits.append((n, n + k))
      n += k
    self.variable_bits = tuple(self.variable_bits)
    self.n_state_bits = n

  def __str__(self):
    return ('_StateBitfieldLayout(%s)'
        % ', '.join(
            ['%s:(%s,%s)' % (part, start, end)
             for part, (start, end) in 
             ([('state', self.state_bits)]
              + [('var %s' % k, self.variable_bits[k])
                 for k in xrange(0, len(self.variable_bits))])]))

def layout_state_bits(compiled_variables, n_states):
  return _StateBitfieldLayout(
      ceil_log_2_of(n_states), [v.bit_width for v in compiled_variables])

def render_js(fsm, alias_table, compiled_variables, state_bitfield_structure,
              functions_by_index, state_transition_table, options, out):
  var_index_map = dict([(v.var.name, v.var.index) for v in compiled_variables])
  max_state_index = reduce(lambda a, b: max(a, b),
                           [s.index for s in fsm.states.values()], 0)
  states_by_index = [None for _ in xrange(0, max_state_index + 1)]
  for state in fsm.states.values():
    states_by_index[state.index] = state

  if options.type_prefix:
    # For type_prefix = foo.bar.
    # output
    #   var foo = foo || {};
    #   foo.bar = foo.bar || {};
    #   foo.bar.variable = foo.bar.variable || {};
    dot = 0
    package_path = '%svariable.' % options.type_prefix
    while True:
      dot = package_path.find('.', dot + 1)
      if dot < 0: break
      package_name = package_path[:dot]
      out.write('%s%s = %s || {};\n' % (
          ('.' not in package_name and 'var ' or ''),
          package_name, package_name))

  def state_definitions():
    for state in states_by_index:
      if state is None: continue
      if not (options.complete_output
              or '@public' in state.annotations
              or '@start_state' in state.annotations
              or '@valid_end' in state.annotations):
        continue
      yield (state.name.text.upper(), str(state.index))
  out.write(
      '/**\n'
      ' * State values usable with {@link #is_state()}.\n'
      ' * Only public states and start and end states appear in this list.\n'
      ' * @enum{number}\n'
      ' */\n')
  _write_js_object('%sState' % options.type_prefix, state_definitions(), out)

  for _, var_decl in _sorted_items(fsm.var_decls):
    if '@public' not in var_decl.annotations: continue
    var_index = fsm.variables[var_decl.name].index
    def variable_value_pairs():
      value_array = compiled_variables[var_index].value_arr
      for i in xrange(0, len(value_array)):
        if value_array[i] is None: continue
        yield (_js(value_array[i]), str(i))

    # Output 2 sets of definitions for public variables.  An enum of values,
    # and a function that extracts the value from the state.
    out.write(
        ('/**\n'
         ' * Value indices for the variable "%s".\n'
         ' * @enum{number}\n'
         ' */\n'
         ) % var_decl.name)
    _write_js_object(
        '%svariable.%s' % (options.type_prefix, _ucamel(var_decl.name.text)),
        variable_value_pairs(), out)
    bounds = state_bitfield_structure.variable_bits[var_index]
    out.write(
        ('function variable%(ucamel_name)sValue(currentState) {\n'
         '  if ("number" !== typeof currentState) {\n'
         '    currentState = currentState[currentState.length - 1];\n'
         '  }\n'
         '  return (currentState & 0x%(mask)x) >> %(bit0)d;\n'
         '}\n')
        % {
           'ucamel_name': _ucamel(var_decl.name.text),
           'mask': _mask(bounds),
           'bit0': bounds[0]
          })

  def compile_side_effect(command, params):
    buf = StringIO.StringIO()
    if command == 'store':
      idx = var_index_map[params[0]]
      _comment('Store value of %s\n' % compiled_variables[idx].var.name, buf)
      buf.write('var value = fetchTail(t, %d);\n' % idx)
      if 'i' in params[1]:
        buf.write('value = value && value.toLowerCase();\n')
      if _DEBUG:
        buf.write('console.log("canon value=`%s`", value);\n')
      buf.write('t.recordings[%d] = undefined;\n' % idx)  # Clear var start
      buf.write('var value_index = var_value_to_index[%s][value] || 0;\n' % idx)
      if _DEBUG:
        buf.write(
            ('console.log("value_index = %%s, remapped=%%s",'
             ' value_index, var_values[%d][value_index]);\n') % idx)
      bounds = state_bitfield_structure.variable_bits[idx]
      buf.write(
          't.currentState = (t.currentState & 0x%x) | (value_index << %d);\n'
          % ((_mask(bounds) ^ 0xffffffffL), bounds[0]))
    elif command == 'record':
      idx = var_index_map[params[0]]
      _comment(
          'Start recording value of %s\n' % compiled_variables[idx].var.name,
          buf)
      if _DEBUG:
        buf.write('console.assert(t.buf.length);\n')
      buf.write('var buftail = t.buf.length - 1;\n')
      buf.write('t.recordings[%d] = (buftail << 16) | t.i;\n' % idx)
      if _DEBUG:
        buf.write(
            ('console.log("recording started at 0x%%s",'
             ' t.recordings[%d].toString(16));\n')
            % idx)
    elif command == 'error':
      _comment('Reporting error %s' % _js(params[0]), buf)
      buf.write('throw new Error(%s);\n' % (
          len(params) and params[0] and _js(params[0]) or ''))
    elif command == 'goto':
      state_index = params[0]
      _comment('Goto state %s' % states_by_index[state_index].name, buf)
      bounds = state_bitfield_structure.state_bits
      buf.write('t.currentState = ((t.currentState & 0x%x) | 0x%x);\n'
          % ((_mask(bounds) ^ 0xffffffffL), state_index))
    elif command == 'invoke':
      _comment('Invoking function %s' % functions_by_index[params[0]].name, buf)
      buf.write('functions[%s](t);\n' % params[0])
    elif command == 'run_state':
      _comment('Running as state %s' % states_by_index[params[0]].name, buf)
      buf.write('var stateTable = transition_table[%d];\n' % params[0])
      buf.write('var dest = stateTable[t.ch];\n')
      buf.write('if (dest === undefined) { dest = stateTable[""]; }\n')
      buf.write('if (dest >= 0) {\n')
      if _DEBUG:
        buf.write('console.log("dest is state %s", state_name[dest]);\n')
      buf.write('  t.currentState = (t.currentState & 0x%x) | dest;\n'
          % (_mask(state_bitfield_structure.state_bits) ^ 0xffffffffL))
      buf.write('} else {\n')
      if _DEBUG:
        buf.write(
            'console.log("dest is function %s", function_name[~dest]);\n')
      buf.write('  functions[~dest](t);\n')
      buf.write('}\n')
    elif command == 'set':
      _comment('Setting value of variable %s to %r' % params, buf)
      var_index = var_index_map.get(params[0])
      assert var_index is not None
      bounds = state_bitfield_structure.variable_bits[var_index]
      value_index = compiled_variables[var_index].value_map.get(params[1])
      if value_index is None:
        raise Exception('Cannot set variable.  Variable %s has no value %r'
            % (compiled_variables[var_index].var.name, params[1]))
      buf.write('t.currentState = (t.currentState & 0x%x) | 0x%x;\n'
          % ((_mask(bounds) ^ 0xffffffffL), value_index << bounds[0]))
    elif command == 'clear':
      _comment('Clearing value of variable %s' % params[0], buf)
      var_index = var_index_map.get(params[0])
      assert var_index is not None
      bounds = state_bitfield_structure.variable_bits[var_index]
      buf.write('t.currentState &= 0x%x;\n' % (_mask(bounds) ^ 0xffffffffL))
    elif command == 'return':
      _comment('Returning from subroutine', buf)
      return_var_index = fsm.variables[_RETURN_PC_VAR_NAME].index
      return_bounds = state_bitfield_structure.variable_bits[return_var_index]
      buf.write('var returnIndex = (t.currentState & 0x%x) >> %d;\n'
          % (_mask(return_bounds), return_bounds[0]))
      if _DEBUG:
        buf.write('console.assert(returnIndex != 0, "Return w/out gosub");\n')
      buf.write('var stateMap = transition_table[returnIndex];\n')
      buf.write(
          'var transition = (t.decoded && stateMap["\\\\" + t.ch])'
          ' || stateMap[t.ch];\n')
      buf.write('if (transition === undefined) {\n')
      buf.write('  transition = stateMap[""];\n')
      buf.write('}\n')
      buf.write('if (transition >= 0) {\n')
      buf.write('  t.currentState = transition | (t.currentState & 0x%x);\n'
          % (_mask(state_bitfield_structure.state_bits) ^ 0xffffffffL))
      buf.write('} else {\n')
      buf.write('  functions[~transition](t);\n')
      buf.write('}\n')
      buf.write('delete t.decoded;\n')
    elif command == 'gosub':
      _comment('Storing the current state in a variable', buf)
      return_var_index = fsm.variables[_RETURN_PC_VAR_NAME].index
      return_bounds = state_bitfield_structure.variable_bits[return_var_index]
      buf.write(
          ('t.currentState = (t.currentState & 0x%x)'
           ' | ((t.currentState & 0x%x) << %d);\n')
          % (_mask(return_bounds) ^ 0xffffffffL,
             _mask(state_bitfield_structure.state_bits),
             return_bounds[0]))
      target_index = params[0]
      _comment('Jumping to a subroutine', buf)
      buf.write(
          't.currentState = (t.currentState & 0x%x) | %d;\n'
          % (_mask(state_bitfield_structure.state_bits) ^ 0xffffffffL,
             target_index))
    elif command == 'decode':
      var_name, extern_fn = params
      _comment('decoding the content of variable %s' % var_name, buf)
      var_index = fsm.variables[var_name].index
      buf.write('t.ch = %s(fetchTail(t, %d));\n' % (extern_fn, var_index));
      buf.write('t.ch = alias_table[t.ch] || t.ch;\n')
      buf.write('t.decoded = true;\n')
      buf.write('t.recordings[%d] = undefined;\n' % var_index)
    else:
      raise Exception('unknown command %s' % command)
    return buf.getvalue()

  print >>out, 'var alias_table = { %s };' % (
      ', '.join(['%s: %s' % (_js(k), _js(v))
                 for k, v in _sorted_items(alias_table)]))

  start_state_to_char_to_end_state = {}
  for ((start_state, char), end_state) in state_transition_table.items():
    assert type(start_state) == types.IntType
    assert char is None or type(char) in types.StringTypes, '%r' % char
    assert type(end_state) == types.IntType
    char_to_end_state = start_state_to_char_to_end_state.get(start_state)
    if char_to_end_state is None:
      char_to_end_state = start_state_to_char_to_end_state[start_state] = {}
    char_to_end_state[char] = end_state

  if _DEBUG:
    def var_name():
      array_index = 0
      for compiled_variable in compiled_variables:
        assert compiled_variable.var.index == array_index
        yield _js(compiled_variable.var.name.text)
        array_index += 1
    _write_js_array('var_name', var_name(), out)

  def var_values_to_index():
    array_index = 0
    for compiled_variable in compiled_variables:
      assert compiled_variable.var.index == array_index
      yield '/* %s #%s: */ { %s }' % (
          compiled_variable.var.name,
          compiled_variable.var.index,
          ', '.join(['%s: %s' % (_js(k), v)
                     for k, v in _sorted_items(
                         compiled_variable.value_map,
                         lambda a, b: cmp(a[1], b[1]))
                     if k is not None]))
      array_index += 1
  _write_js_array('var_value_to_index', var_values_to_index(), out)

  def var_values():
    array_index = 0  # The array constructor element to write.
    for compiled_variable in compiled_variables:
      assert compiled_variable.var.index == array_index
      yield '/* %s #%s: */ [ %s ]' % (
          compiled_variable.var.name,
          compiled_variable.var.index,
          ', '.join(['%s' % _js(v) for v in compiled_variable.value_arr]))
      array_index += 1
  _write_js_array('var_values', var_values(), out)

  if _DEBUG:
    def function_name():
      array_index = 0
      for function in functions_by_index:
        assert function.index == array_index
        yield _js(function.name.text)
        array_index += 1
    _write_js_array('function_name', function_name(), out)

  def functions():
    for fn in functions_by_index:
      stmts = []
      else_transitions = []
      var_transitions = []  # pairs of VariablePatterns and Results
      str_transitions = []  # pairs of StringPatterns and Results
      for transition in fn.transitions:
        if isinstance(transition, state_machine.ElseTransition):
          else_transitions.append(transition)
        else:
          assert isinstance(transition, state_machine.FunctionTransition)
          for pattern in transition.patterns:
            if isinstance(pattern, state_machine.VariablePattern):
              var_transitions.append((pattern, transition.result))
            else:
              assert isinstance(pattern, state_machine.StringPattern)
              str_transitions.append((pattern, transition.result))

      var_index = var_index_map.get(fn.var_name)

      value_index_map = (var_index is not None
                         and compiled_variables[var_index].value_map
                         or None)

      if var_index is not None:
        # This sorting will lead to the best collapsing of the switch statement
        # cases.
        str_transitions.sort(
            lambda a, b: (cmp(a[1], b[1])
                          or cmp(value_index_map[a[0].value],
                                 value_index_map[b[0].value])))
      var_transitions.sort(lambda a, b: cmp(a[0].var_name, b[0].var_name))
      if len(else_transitions) > 1:
        raise Exception('Function %s has more than one "else" transition'
            % fn.name)

      # Functions extracted from side effects need no variables.
      needs_var = len(str_transitions) != 0 or len(var_transitions) != 0

      if needs_var and var_index is None:
        raise Exception('function %s depends on undefined var %s'
            % (fn.name, fn.var_name))

      buf = StringIO.StringIO()
      buf.write('function %s(t) {\n' % fn.name)
      if _DEBUG:
        buf.write('console.log("invoking %s with state %%o", t);\n' % fn.name)

      need_switch_block = len(str_transitions) != 0

      if needs_var:
        var_bounds = state_bitfield_structure.variable_bits[var_index]
        buf.write('  var valueIndex = (t.currentState & 0x%x) >> %d;\n'
            % (_mask(var_bounds), var_bounds[0]))
      else:
        var_bounds = None

      if need_switch_block:
        buf.write('  switch (valueIndex) {\n')

      for i in xrange(0, len(str_transitions)):
        pattern, result = str_transitions[i]

        value_index = value_index_map[pattern.value]
        assert value_index is not None

        if i + 1 < len(str_transitions):
          # drop through to the next case if it does the same thing
          next_pattern, next_result = str_transitions[i + 1]
          next_value_index = value_index_map[next_pattern.value]
        else:
          next_pattern = None
          next_result = None
          next_eff_name = None
          next_eff_params = None
          next_value_index = None

        if next_value_index == value_index:
          continue

        buf.write('    case %s: /* %r */\n' % (value_index, pattern.value))
        eff_name, eff_params = result_to_effect(result, fsm)

        if next_result is not None:
          next_eff_name, next_eff_params = result_to_effect(next_result, fsm)
          if (next_eff_name == eff_name and next_eff_params == eff_params
              and (next_result.side_effects == result.side_effects)):
            # drop through to the next case if it does the same thing
            continue

        for eff in result.side_effects:
          buf.write(_indent(compile_side_effect(eff.name, eff.params), 6))
        buf.write(_indent(compile_side_effect(eff_name, eff_params), 6))
        buf.write('      break;\n')
      if need_switch_block:
        buf.write('    default:\n')

      need_if_block = len(var_transitions) != 0
      indent = (need_switch_block and 6 or 2) + (need_if_block and 2 or 0)
      if len(var_transitions) != 0:
        clause = '%sif' % (' ' * (indent - 2))
        for pattern, result in var_transitions:
          other_var_index = fsm.variables[pattern.var_name].index
          other_var_bounds = (
              state_bitfield_structure.variable_bits[other_var_index])
          lhs = 'var_values[%d][valueIndex]' % var_index
          rhs = 'var_values[%d][(t.currentState & 0x%x) >> %d]' % (
              other_var_index, _mask(other_var_bounds), other_var_bounds[0])
          condition = '%s === %s' % (lhs, rhs)

          log_comparison = ''
          if _DEBUG:
            log_comparison = ('console.log("%%s === %%s -> %%s",'
                              ' (%s), (%s), (%s)),') % (lhs, rhs, condition)

          buf.write('%s (%s%s) {\n' % (clause, log_comparison, condition))
          clause = ' else if'

          for eff in result.side_effects:
            buf.write(_indent(compile_side_effect(eff.name, eff.params),indent))
          eff_name, eff_params = result_to_effect(result, fsm)
          buf.write(_indent(compile_side_effect(eff_name, eff_params), indent))
          buf.write('%s}' % (' ' * (indent - 2)))

      if len(else_transitions):
        if need_if_block:
          buf.write(' else {\n')

        transition = else_transitions[0]
        for eff in transition.result.side_effects:
          buf.write(_indent(compile_side_effect(eff.name, eff.params), indent))
        eff_name, eff_params = result_to_effect(transition.result, fsm)
        buf.write(_indent(compile_side_effect(eff_name, eff_params), indent))

        if need_if_block:
          buf.write('%s}\n' % (' ' * (indent - 2)))
      elif need_if_block:
        buf.write('\n')

      if need_switch_block:
        buf.write('      break;\n')
        buf.write('  }\n')
      buf.write('}')

      yield _indent(buf.getvalue(), 4).lstrip()
  _write_js_array('functions', functions(), out)

  def transition_table():
    yield 'null'
    array_index = 1  # The array constructor element to write.
    for start_state, char_to_end_state in (
        _sorted_items(start_state_to_char_to_end_state)):

      if start_state != array_index:
        # The below assertion is true because we sort states by index above.
        # If it is not true, we can compose the array by assigning each element
        # separately, as in transition_table[state] = ...
        raise AssertionError('%s != %s.  Expected state %s' % (
            start_state, array_index, states_by_index[array_index]))

      state_pairs = _sorted_items(char_to_end_state)
      if None not in char_to_end_state:
        state_pairs.append((None, start_state))

      def transition_comment(index):
        if index >= 0:
          return states_by_index[index].name
        else:
          return functions_by_index[~index].name

      yield '/* %s #%d: */ { %s }' % (
          states_by_index[start_state].name,
          array_index,
          ', '.join(['%s: %s /* %s */'
                     % (_js(k or ''), v, transition_comment(v))
                     for k, v in state_pairs]))
      array_index += 1
  _write_js_array('transition_table', transition_table(), out)

  if _DEBUG:
    def state_name():
      state_index = 0
      while state_index < len(states_by_index):
        state = states_by_index[state_index]
        if state is None:
          yield 'null'
        else:
          assert state.index == state_index
          yield _js(state.name.text)
        state_index += 1
    _write_js_array('state_name', state_name(), out)

  print >>out, """
function process(s, currentState, buf) {
  var traversal;
  var recordings = [];
  currentState = unpackRecordings(currentState, recordings);
  %(log_unpack)s
  for (var i = 0, n = s.length; i < n; ++i) {
    var ch = s.charAt(i);
    %(log_fetch)s
    ch = alias_table[ch] || ch;
    %(log_alias)s
    var stateMap = transition_table[currentState & 0x%(current_state_mask)x];
    var transition = stateMap[ch];
    if (transition === undefined) { transition = stateMap[""]; }
    %(log_transition)s
    if (transition >= 0) {
      currentState = transition | (currentState & 0x%(inv_current_state_mask)x);
      %(log_goto)s
    } else {
      if (!traversal) {
        traversal = { buf: buf, s: s, recordings: recordings };
      }
      traversal.currentState = currentState;
      traversal.ch = ch;
      traversal.i = i;
      %(log_invoke)s
      functions[~transition](traversal);
      currentState = traversal.currentState;
      %(log_post_invoke)s
    }
  }

  currentState = packRecordings(currentState, recordings);
  %(log_pack)s
  return currentState;
}
""" % { 'current_state_mask': _mask(state_bitfield_structure.state_bits),
        'inv_current_state_mask':
            _mask(state_bitfield_structure.state_bits) ^ 0xffffffffL,
        'log_unpack': (_DEBUG
            and ('console.log("UNPACKED unpacked to %s#%d, [%s]",'
                 ' state_name[currentState], currentState, recordings);\n')
            or ''),
        'log_alias': (_DEBUG and 'console.log("aliased to %s", ch);\n' or ''),
        'log_transition': (_DEBUG
            and 'console.log("transition=%s", transition);\n'
            or ''),
        'log_fetch': (_DEBUG
            and ('console.log("i=%%s, ch=%%s, currentState=%%s : %%s",'
                 ' i, uneval(ch), state_name[currentState & %s],'
                 ' decodeVars(currentState));\n'
                 % _mask(state_bitfield_structure.state_bits))
            or ''),
        'log_goto': (_DEBUG
            and 'console.log("goto state %s", state_name[transition]);\n'
            or ''),
        'log_invoke': (_DEBUG
            and ('console.log("invoking %s with %o",'
                 ' function_name[~transition], traversal);\n')
            or ''),
        'log_pack': (_DEBUG
            and ('console.log("packed to %s: %s", currentState,'
                 ' decodeState(currentState));\n')
            or ''),
        'log_post_invoke': (_DEBUG
            and 'console.log("invoked.  state=" + decodeState(currentState));\n'
            or ''),
      }

  print >>out, """/** True iff the given state is a valid end state. */
function isValidEndState(currentState) {
  if ((typeof currentState) === 'object') {
    currentState = currentState[currentState.length - 1];
  }
  switch (currentState & 0x%(current_state_mask)x) {
%(cases)s
      return true;
    default:
      return false
  }
}
/**
 * Returns the state id for the given state.
 * @param {number|Array} currentState the opaque state value.
 * @return {number} a value from the %(type_prefix)sState enum.
 */
function getStateId(currentState) {
  if ((typeof currentState) === 'object') {
    currentState = currentState[currentState.length - 1];
  }
  return currentState & 0x%(current_state_mask)x;
}

/**
 * Returns the state replaced with the given state id.
 * @param {number|Array} currentState the opaque state value.
 * @param {number} stateId from the %(type_prefix)sState enum.
 * @return {number|Array} an opaque state value.
 */
function withStateId(currentState, stateId) {
  if ((typeof currentState) === 'object') {
    currentState = currentState[currentState.length - 1];
  }
  return (currentState & 0x%(inv_current_state_mask)x) | stateId;
}
""" % {
        'current_state_mask': _mask(state_bitfield_structure.state_bits),
        'inv_current_state_mask': (
            _mask(state_bitfield_structure.state_bits) ^ 0xffffffffL),
        'cases': '\n'.join(['    case %d:' % x.index
                            for x in fsm.states.values()
                            if '@valid_end' in x.annotations]),
        'type_prefix': options.type_prefix
      }

  n_state_bits = state_bitfield_structure.n_state_bits
  print >>out, """
/** dual of record operation */
function fetchTail(t, var_index) {
  var recording = t.recordings[var_index];
  if (recording == null) { return null; }  // Match null or undefined
  var chunk = (recording >> 16) & 0xffff;
  var posInChunk = recording & 0xffff;
%(log_recording_decomposed)s
  var buf = t.buf;
  var tail;
  var last = buf.length - 1;
%(log_current_pos)s
  if (chunk === last) {
    tail = buf[chunk].substring(posInChunk, t.i);
  } else {
    var sb = [buf[chunk].substring(posInChunk)];
    for (var i = chunk + 1; i < last; ++i) {
      sb.push(buf[i]);
    }
    sb.push(buf[last].substring(0, t.i));
    tail = sb.join('');
  }
%(log_tail)s
  return tail;
}
""" % {
       'log_recording_decomposed': (_DEBUG
           and "  console.log('chunk=%s, posInChunk=%s', chunk, posInChunk);\n"
           or ''),
       'log_current_pos': (_DEBUG
           and "  console.log('last=%s, i=%s, t=%o', last, t.i, t);\n"
           or ''),
       'log_tail': (_DEBUG
           and "  console.log('tail=%s', tail);\n"
           or ''),
       }

  if _DEBUG:
    def decode_vars():
      for var in compiled_variables:
        bounds = state_bitfield_structure.variable_bits[var.var.index]
        if bounds[1] == bounds[0]: continue
        yield ('%s + uneval(var_values[%d][((state & 0x%x) >> %d)])'
               % (_js('%s=' % var.var.name.text),
                  var.var.index,
                  _mask(bounds),
                  bounds[0]))

    print >>out, "function decodeVars(state) {"
    _write_js_array('sb', decode_vars(), out);
    print >>out, "  return sb.join();"
    print >>out, "}"

    print >>out, "function decodeState(state) {"
    print >>out, "  state = quickUnpackRecordings(state);"
    print >>out, (("  return state_name[state & 0x%x] + '#' + state + ':['"
                   " + decodeVars(state) + ']';")
                  % _mask(state_bitfield_structure.state_bits))
    print >>out, "}"

  if _DEBUG:
    print >>out, """
function quickUnpackRecordings(currentState) {
  return (typeof currentState) === 'number'
      ? currentState
      : currentState[currentState.length - 1];
}"""

  print >>out, """
function unpackRecordings(currentState, recordings) {
  %(log_unpack_type)s
  if ((typeof currentState) !== 'number') {
    var last = currentState.length - 1;
    %(log_unpack_from_array)s
    for (var i = last; --i >= 0;) {
      recordings[i] = currentState[i];
    }
    return currentState[last];
  }
  var compressed = (currentState / 0x%(two_to_n_state_bits)x) | 0;
  %(log_unpack_compressed)s
  if (compressed) {
    var varIndex = compressed %% %(n_vars)d;
    recordings[varIndex] = ((compressed / %(n_vars)d) | 0) - 1;
    %(log_unpack_decoded)s
  }
  return currentState & 0x%(state_and_vars_mask)x;
}

function isRecording(currentState) {
  return ((currentState & 0x%(inv_state_and_vars_mask)x) | 1) !== 1;
}

function packRecordings(currentState, recordings) {
  %(log_pack)s
  var index;
  for (var i = recordings.length; --i >= 0;) {
    if (recordings[i] !== undefined) {
      if (index !== undefined) {
        index = -1;
        break;
      } else {
        index = i;
      }
    }
  }
  if (index === undefined) {
    %(log_no_packing)s
    return currentState;
  }
  if (index >= 0) {
    var compressed = index + %(n_vars)d * (recordings[index] + 1);
    if (compressed < 0x%(max_compressed_value)x) {
      // TODO(mikesamuel): if there's an easy way to interleave bits when
      // computing members of the recordings array, it would make better use
      // of the leftover bits.
      // By interleave bits, I mean combine two bitstrings A0A1... and B0B1...
      // to produce A0B0A1B1... though interleaving by nibble or byte would
      // still give an advantage.
      %(log_packing)s;
      return currentState + compressed * 0x%(two_to_n_state_bits)x;
    }
  }
  recordings.push(currentState);
  %(log_packing_to_array)s
  return recordings;
}
""" % {
       'two_to_n_state_bits': 1 << n_state_bits,
       'state_and_vars_mask': _mask((0, n_state_bits)),
       'inv_state_and_vars_mask': (_mask((0, n_state_bits)) ^ 0xffffffffL),
       'n_vars': len(compiled_variables),
       'max_compressed_value': (1 << (52 - n_state_bits)) - 1,
       'log_no_packing': (_DEBUG
           and ('console.log("PACK not needed.  state=0x%s",'
                ' currentState.toString(16));\n')
           or ''),
       'log_pack': (_DEBUG
           and ('console.log("PACK state=%d, recordings=[%s]",'
                ' currentState, recordings);\n')
           or ''),
       'log_packing': (_DEBUG
           and ('console.log("PACK idx=%s, compressed=0x%s",'
                ' index, compressed.toString(16));\n')
           or ''),
       'log_packing_to_array': (_DEBUG
           and ('console.log("PACK idx=%s, recordings=%s",'
                ' index, recordings);\n')
           or ''),
       'log_unpack_type': (_DEBUG
           and ('console.log("UNPACK: state=%s, type=%s",'
                ' currentState, typeof currentState);\n')
           or ''),
       'log_unpack_from_array': (_DEBUG
           and 'console.log("UNPACK: from array");\n'
           or ''),
       'log_unpack_compressed': (_DEBUG
           and ('console.log("UNPACK: compressed=0x%s",'
                ' compressed.toString(16));\n')
           or ''),
       'log_unpack_decoded': (_DEBUG
           and ('console.log("UNPACK: idx=%s, value=0x%s, recordings=[%s]",'
                ' varIndex, recordings[varIndex].toString(16), recordings);\n')
           or ''),
      }


_JS_ESCS = { '\n': '\\n', '\r': '\\r', '\'': '\\\'', '\\': '\\\\', '\t': '\\t' }

def _js(s):
  if s is None: return 'null'
  return "'%s'" % (
      re.sub(ur'[^\u0020-\u0026\u0028-\u005b\u005f-\u007e]',
             lambda m: _JS_ESCS.get(m.group(0), '\\u%004x' % ord(m.group(0))),
             s)
      .encode('UTF-8'))

def _sorted_items(map, comparator=lambda a, b: cmp(a, b)):
  items = map.items()
  items.sort(comparator)
  return items

def _mask((start, end)):
  return ((1 << (end - start)) - 1) << start

def _write_js_array(name, items, out):
  out.write('%s%s = [' % ('.' not in name and 'var ' or '', name))
  first = True
  for item in items:
    if first:
      first = False
    else:
      out.write(',')
    out.write('\n    %s' % item)
  out.write('\n    ];\n')

def _write_js_object(name, items, out):
  out.write('%s%s = {' % ('.' not in name and 'var ' or '', name))
  first = True
  for key, value in items:
    if first:
      first = False
    else:
      out.write(',')
    out.write('\n    %s: %s' % (key, value))
  out.write('\n    };\n')

def _comment(s, out):
  out.write('// %s\n' % re.sub(r'\r\n\u2028\u2029', '~/~', s))
  if _DEBUG:
    out.write('console.log(%s);\n' % _js(re.sub('%', '%%', s)))

_NEWLINE = re.compile('^(?=.)', re.MULTILINE)
def _indent(s, n_spaces):
  return _NEWLINE.sub(' ' * n_spaces, s)

def _ucamel(s):
  return '%s%s' % (
      s[0:1].upper(), re.sub(r'_([a-z])', lambda m: m.group(1).upper(), s[1:]))
