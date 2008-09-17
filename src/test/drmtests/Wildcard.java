class Wildcard{
  int foo(List<?> list) { return list.length; }

  int bar(List<? extends CharSequence> list) { return list.length; }

}
