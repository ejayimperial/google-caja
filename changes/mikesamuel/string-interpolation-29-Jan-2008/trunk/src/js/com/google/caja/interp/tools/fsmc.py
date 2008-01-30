#!/usr/bin/python

r"""Usage: %0 < state_transitions > context_scanner.js

Takes a file describing state transitions and generates javascript that
implements a context scanner that implements that SM.

The FSM is executed in an environment that includes an input character, a set
of variable values, and an output buffer.  The output buffer is read-only and
is examined by the store and record operators.

Execution of the FSM proceeds for each character:
  (1) Alias the input character using the <alias> productions.
      input_char = alias_char_map[input_char] || input_char
  (2) Look up the state transitions for the current (input_char, state) pair
  (3) If none found, use the transition for the special 'else' char
  (4) Apply any side effects
  (5) If the result of the transition is a function, bind the current value of
      its input variable and apply it to get the end_state
  (6) Set the current state to the end state.

After finishing processing all input,
  (1) If the current state does not have the @valid_end annotation, transition
      to the error state.
  (2) If the state is the special error state, raise an exception.
"""

import compile
import parse
import StringIO
import sys

if '__main__' == __name__:
  complete_output = False
  type_prefix=''

  args = sys.argv[1:]
  while len(args):
    arg = args[0]
    if not arg.startswith('--'):
      break

    del args[0:1]

    if arg == '--':
      break
    elif arg == '--debug':
      compile._DEBUG = True
    elif arg == '--full':
      complete_output = True
    elif arg == '--type_prefix':
      type_prefix = args[0]
      del args[0:1]
    elif arg.startswith('--type_prefix='):
      type_prefix = arg[arg.find('=') + 1:]
    else:
      raise Exception('Unknown flag %s' % arg)

  if len(args) != 2:
    print >>sys.stderr, (
        "Usage: %s [--debug] [--full] <infile.fsm> <outfile.js>"
        % sys.argv[0])
    sys.exit(-1)

  in_file, out_file = args

  if in_file == '-':
    in_stream = sys.stdin
  else:
    in_stream = open(in_file, 'r')
  tq = parse.TokenQueue(
      [token for token in parse.lex(in_stream.read())])
  in_stream.close()

  fsm = parse.parse(tq)

  options = compile.CompilationOptions(
     type_prefix=type_prefix,
     complete_output=complete_output)

  buf = StringIO.StringIO()
  compile.compile(fsm=fsm, options=options, out=buf)

  if out_file == '-':
    out_stream = sys.stdout
  else:
    out_stream = open(out_file, 'w')
  out_stream.write(buf.getvalue())
  out_stream.close()

  sys.exit(0)
