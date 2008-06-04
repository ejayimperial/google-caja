#!/usr/bin/perl

use warnings;
use strict;

my($outputdir) = "examples";

sub extract {
  my($type, $suffix) = @_;
  while (/\\begin{$type}{(.*?)}(.*?)\\end{$type}/msg) {
    my($jsfile) = $1;
    my($js) = $2;
    open OUTPUT, ">$outputdir/$jsfile.$suffix" or die "Could not open $jsfile: $!";
    print OUTPUT $js;
    close OUTPUT;
  }
}

sub usage {
  return "Usage: extract-examples.pl file.tex\n"
}

print usage and exit unless $#ARGV == 0;

# Slurp entire file
undef $/; 
$_=<>; 

# Extract the examples in file.tex into individual files
# in the output directory
mkdir $outputdir;
extract("caja", "js");
extract("cajita", "js");
