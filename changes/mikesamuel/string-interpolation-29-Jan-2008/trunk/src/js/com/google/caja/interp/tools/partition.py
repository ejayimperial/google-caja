#!/usr/bin/python2.4

def partition(items, inequalities):
  """
  Given a set of items and a set of pairs of the form (item1, item2) that
  asserts that item1 != item2, partition items into equivalence classes.

  items: array<T>
  inequalities: set<tuple<T,T>>

  returns: array<array<T>>
  """
  inequalities_sorted = set()
  for a, b in inequalities:
    if a < b:
      inequalities_sorted.add((a, b))
    else:
      inequalities_sorted.add((b, a))
  def unequal(a, b):
    if a < b:
      return (a, b) in inequalities_sorted
    else:
      return (b, a) in inequalities_sorted
  equivalence_sets = []
  for item1 in items:
    equivalence_set = None
    for eset in equivalence_sets:
      constrained = False
      for item2 in eset:
        if unequal(item1, item2):
          constrained = True
          break
      if not constrained:
        equivalence_set = eset
        break
    if equivalence_set is None:
      equivalence_set = set()
      equivalence_sets.append(equivalence_set)
    equivalence_set.add(item1)
  return equivalence_sets
