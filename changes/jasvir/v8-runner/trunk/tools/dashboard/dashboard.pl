#!/usr/bin/perl
# Main entry point for the code status dashboard.

use strict;
use Cwd qw ( abs_path );
use Date::Format qw ( time2str );
use Encode qw ( decode_utf8 );
use File::Basename qw ( dirname );
use File::Path qw ( rmtree );


sub usage() {
  print "Usage: /usr/bin/perl $0 -o <output_dir> [-b <svn_client_root>]

A scripts for building a dashboard including test, coverage,
profiling stats, and more.

All state is generated by executing Ant targets or derived from files
in SVN or files generated by Ant.

That state is bundled into an index.xml file and includes:
- targets: logs of ant runs
- varz: stats such as time to run, number of failing tests, etc.
- summaries: extracted
- tasks: TODOs in source code
- output trees: URL of directory trees such as ant-docs,
  ant-reports/tests, etc.

tools/dashboard/dashboard.pl generates the dashboard by:
- syncing the SVN client
- running ant
- building index.xml and copying it into the history directory
- applying each of the XSL files under tools/dashboard/reports to
  index.xml to generate an HTML file
- extracting the historical data from the history directory to build
  time-series.xml
- apply varz.xsl to time-series.xml to generate charts
";
  exit;
}


# We need to deal with 2 Subversion clients which may be the same.
# A client is a directory that corresponds to a "trunk" directory in code.google
# in which the "svn" command will work.  They are usually named "google-caja".

# One client is used to store historical statistics and contains templates for
# HTML and supporting files.  This is the "Master Client".
our $MASTER_CLIENT;

# The second client is the "build client" which is the one we're updating,
# building, and extracting stats from.
our $BUILD_CLIENT;


# Directory for output for this particular build.
our $OUTPUT_DIR;
while (@ARGV) {
  my $flag = shift;
  last if '--' eq $flag;
  if ('-o' eq $flag) {
    die "output directory redeclared.  Was $OUTPUT_DIR" if defined($OUTPUT_DIR);
    $OUTPUT_DIR = abs_path(shift);
  } elsif ('-b' eq $flag) {
    die "build client redeclared.  Was $BUILD_CLIENT" if defined($BUILD_CLIENT);
    $BUILD_CLIENT = abs_path(shift);
  } else {
    usage;
  }
}
if (@ARGV) {
  print STDERR "Unused args @ARGV\n";
  usage;
}

die 'Please specify output directory via -o' unless $OUTPUT_DIR;
die "$OUTPUT_DIR not a directory" unless !(-e $OUTPUT_DIR) || -d $OUTPUT_DIR;

sub requireDir($) {
  my $path = shift;
  die "$0 requires a directory at $path" unless -d $path;
}
sub requireExe($) {
  my $path = shift;
  die "$0 requires an executable at $path" unless -x $path;
}


# Directory structure within subversion
our $DASHBOARD_DIR = dirname(abs_path($0));  requireDir $DASHBOARD_DIR;
our $TOOLS_DIR = dirname($DASHBOARD_DIR);    requireDir $TOOLS_DIR;
$MASTER_CLIENT ||= dirname($TOOLS_DIR);      requireDir $MASTER_CLIENT;
$BUILD_CLIENT ||= $MASTER_CLIENT;            requireDir $BUILD_CLIENT;
our $SRC_DIR = "$BUILD_CLIENT/src";          requireDir $SRC_DIR;
our $TESTS_DIR = "$BUILD_CLIENT/tests";      requireDir $TESTS_DIR;

# Caja build output directories
our $REPORTS_DIR = "ant-reports";
our $DOCS_DIR = "ant-docs";
our $DEMOS_DIR = "ant-www";
our $LIB_DIR = "ant-lib";

# History of all builds used to generate time series.
our $HISTORY_DIR = "$MASTER_CLIENT/history"; requireDir $HISTORY_DIR;

# Executables required
our $ANT_HOME = "/usr/local/ant";            requireDir $ANT_HOME;
our $ANT = "$ANT_HOME/bin/ant";              requireExe $ANT;
our $JAVA_HOME = $ENV{JAVA_HOME} or "/usr/lib/jvm/java-6-sun/";
                                             requireDir $JAVA_HOME;
our $JAVA = "$JAVA_HOME/bin/java";           requireExe $JAVA;
our $SVN = "/usr/bin/svn";                   requireExe $SVN;
our $SVNVERSION = "/usr/bin/svnversion";     requireExe $SVNVERSION;
our $XSLTPROC = "/usr/bin/xsltproc";         requireExe $XSLTPROC;


sub collectCodeStats() {
  my @status_log = ();

  my $build_id = Date::Format::time2str("%Y%m%dT%H%M%S", time, "UTC");
  print STDERR "dashboard $build_id\n";

  push(@status_log, qq'<varz name="time" value="' . (scalar time) . qq'"/>');

  print STDERR "updating\n";
  track(\&updateLocalClient, [], 'update', \@status_log);

  my $rev = svnversion();
  push(@status_log, qq'<varz name="svnversion" value="$rev"/>');

  print STDERR "cleaning\n";
  track(\&build, ['clean'], 'clean', \@status_log);

  print STDERR "extracting tasks\n";
  extractTasks(["$SRC_DIR", "$TESTS_DIR"], $rev, \@status_log);

  print STDERR "building jars\n";
  track(\&build, ['jars'], 'build', \@status_log);

  print STDERR "computing test coverage\n";
  track(\&build, ['emma', 'runtests'], 'coverage', \@status_log);

  print STDERR "copying coverage reports\n";
  extractCoverageSummary("$REPORTS_DIR/coverage/index.html", \@status_log);

  print STDERR "running benchmarks\n";
  track(\&build, ['benchmarks'], 'tests', \@status_log);

  print STDERR "running tests\n";
  track(\&build, ['runtests'], 'tests', \@status_log);
  extractTestSummary("$REPORTS_DIR/tests/TESTS-TestSuites.xml", \@status_log);

  print STDERR "building docs\n";
  track(\&build, ['docs'], 'docs', \@status_log);

  print STDERR "building demos\n";
  track(\&build, ['AllTests', 'demos', 'testbed'], 'demos', \@status_log);

  print STDERR "making output directory\n";
  makeOutputDir();

  print STDERR "copying docs\n";
  outputTree($DOCS_DIR, 'docs', 'java/index.html', \@status_log);
  linkOutput('jsdocs', 'docs/js/index.html', \@status_log);
  linkOutput('ruledocs', 'docs/rules/DefaultCajaRewriter.html', \@status_log);

  print STDERR "copying test reports\n";
  outputTree("$REPORTS_DIR/tests", 'tests', 'index.html', \@status_log);

  outputTree("$REPORTS_DIR/coverage", 'coverage', 'index.html', \@status_log);

  print STDERR "copying demos\n";
  outputTree($DEMOS_DIR, 'demos', '', \@status_log);
  outputTree($LIB_DIR, 'snapshot', '', \@status_log);

  print STDERR "writing reports\n";
  writeReport(\@status_log, $build_id, "$OUTPUT_DIR/index.xml");
  writeReport(\@status_log, $build_id, "$HISTORY_DIR/$build_id.xml");
}

# Execute a function, track the amount of time it takes, and whether it
# succeeds, and capture its stdout and stderr.
sub track($$$$) {
  my ($fn, $args, $name, $status_log_ref) = @_;

  my $t0 = scalar time;
  my ($status, $log) = &$fn(@{$args});
  my $t1 = scalar time;

  my $dt = $t1 - $t0;

  push(@{$status_log_ref},
       qq'<target name="$name">\n'
       . qq'  <log>' . xml($log) . qq'</log>\n'
       . qq'</target>',
       qq'<varz name="target.$name.status" value="$status"/>',
       qq'<varz name="target.$name.time" value="$dt"/>');

  # Extract profiling data.
  my @varz = $log =~ m/\bVarZ:([\w\.\-]+)=(\d+(?:\.\d+)?)\b/g;
  for (my $i = 0; $i <= $#varz; $i += 2) {
    push(@{$status_log_ref}, qq'<varz name="$varz[$i]" value="$varz[$i+1]"/>');
  }
}

# Copy a directory tree to the output directory, e.g. the javadoc tree.
# $src_dir        -- the directory to copy
# $name           -- name of the output directory.
#                    The output will be copied to a directory with this name.
#                    This is also used as the name of the output as it appears
#                    in the dashboard.
# $index          -- path to an $HTML file relative to $OUTPUT_DIR/$name
# $status_log_ref -- ARRAY reference to which outputs are added.
sub outputTree($$$$) {
  my ($src_dir, $name, $index, $status_log_ref) = @_;

  my $out_dir = "$OUTPUT_DIR/$name";
  die "Output directory $out_dir already exists" if -e $out_dir;

  system('cp', '-r', $src_dir, $out_dir);

  linkOutput($name, "$name/$index", $status_log_ref);
}

# Create a link to the given output directory
# $name           -- name of the output as it appears in the dashboard
# $output_branch  -- path relative to the dashboard root/$OUTPUT_DIR
# $status_log_ref -- ARRAY reference to which outputs are added.
sub linkOutput($$$) {
  my ($name, $output_branch, $status_log_ref) = @_;

  my $out_file = "$OUTPUT_DIR/$output_branch";
  die "$out_file does not exist" unless -e "$out_file";

  push(@{$status_log_ref}, qq'<output name="$name" href="$output_branch"/>');
}

# Run svn to pull down the latest version.
sub updateLocalClient() {
  return exec_log($BUILD_CLIENT, $SVN, 'update');
}

# Get the current version number
sub svnversion() {
  my $version = `"$SVNVERSION"`;
  die "Bad svnversion: $version @ $ENV{PWD}" unless $version =~ m/^(\d{4,})/;
  return $1;
}

# Run ant.
sub build(@) {
  return exec_log($BUILD_CLIENT, $ANT, @_);
}

# Extract TODOs from code.
sub extractTasks($$$) {
  my ($dir_ref, $rev, $status_log_ref) = @_;
  push(@{$status_log_ref}, qq'<tasks rev="$rev">');
  foreach my $dir (@{$dir_ref}) {
    my $find = cmd('find', $dir, '-type', 'f',
                   '!', '-name', '*~',
                   '!', '-path', '*/.svn/*');
    my $grep = cmd('xargs', 'egrep', '-Hn', '\bTODO\b');
    open(IN, "$find | $grep|") or die "Failed to grep for TODOs: $!";
    while (<IN>) {
      chomp;
      my ($file, $line, $task) = m/([^:]*):(\d+):.*?(TODO.*)/;
      $file =~ s|.*/google-caja/||;
      die "Bad grep output: $_" unless defined($file);

      my $file_xml = xml($file);

      # Typical format is
      # TODO(<owner-email-or-username>): <details>
      my ($owners, $detail) = ($task =~ /TODO(?:\s*\(\s*([\w\.@\s,]+)\))?(.*)/);
      die "Malformed task: $task" unless defined($detail);
      $owners =~ s/^\s+|\s+$//g;
      $detail =~ s/^:?\s*//;

      my @owners = split /\s+/, $owners;
      push(@owners, 'unknown') unless @owners;

      foreach my $owner (@owners) {
        my $ownername = $owner;
        $ownername =~ s/@.*//;
        $ownername =~ s/\W+//g;
        push (@{$status_log_ref},
              qq'<task file="$file_xml" line="$line" owner="\L$owner\E">'
              . xml($detail) . '</task>');
      }
    }
    close(IN);
  }
  push (@{$status_log_ref}, '</tasks>');
}

sub extractTestSummary($$) {
  my ($xml_file, $status_log_ref) = @_;

  my ($tests, $errors, $failures) = (0, 0, 0);
  open(IN, "<$xml_file") or die "$xml_file: $!";
  while (<IN>) {
    chomp;
    next unless m/<testsuite\b(.*)/;
    my $testsummary = $1;
    die "Malformed $xml_file: $_" unless $testsummary =~ s/>.*//;
    die "Malformed $xml_file: $_" unless $testsummary =~ m/\btests="(\d+)"/;
    $tests += $1;
    die "Malformed $xml_file: $_" unless $testsummary =~ m/\berrors="(\d+)"/;
    $errors += $1;
    die "Malformed $xml_file: $_" unless $testsummary =~ m/\bfailures="(\d+)"/;
    $failures += $1;
  }
  close(IN);

  push(@{$status_log_ref}, qq'<varz name="junit.total" value="$tests"/>');
  push(@{$status_log_ref}, qq'<varz name="junit.errors" value="$errors"/>');
  push(@{$status_log_ref}, qq'<varz name="junit.failures" value="$failures"/>');
  push(@{$status_log_ref},
       qq'<varz name="junit.pct" value="'
       . sprintf("%3.1f", 100 * ($failures + $errors) / $tests)
       . qq'"/>');
}

sub extractCoverageSummary($$) {
  my ($html_file, $status_log_ref) = @_;

  my $html = "";
  open(IN, "<$html_file") or die "$html_file: $!";
  while (<IN>) { $html .= $_; }
  close(IN);
  $html = decode_utf8($html);

  die "Malformed $html_file: $html"
      unless $html =~ /<h2[^>]*>OVERALL\s+COVERAGE\s+SUMMARY.*?<\/table>/is;
  my $summaryTable = $&;

  my ($pct, $covered, $total) = ($summaryTable
      =~ m|<td>([\d\.]+)%\s*\(([\d\.]+)/([\d\.]+)\)</TD></TR>|i);
  die "Malformed $html_file: $summaryTable" unless defined($pct);

  push(@{$status_log_ref}, qq'<varz name="emma.pct" value="$pct"/>');
  push(@{$status_log_ref}, qq'<varz name="emma.covered" value="$covered"/>');
  push(@{$status_log_ref}, qq'<varz name="emma.total" value="$total"/>');
}

# Execute a command and return whether it succeeded and the stderr&stdout.
sub exec_log($$@) {
  die "exec_log called in wrong context" unless wantarray;

  my $dir = shift;
  my $cmd = cmd(@_);

  chdir($dir);
  my $log = `$cmd 2>&1`;
  my $result = $?;

  return ($result >> 8 ? 0 : 1, "$log");
}

# Create the output directory wiping out any that existed prior.
sub makeOutputDir() {
  if (-d $OUTPUT_DIR) {
    rmtree $OUTPUT_DIR or die "clean $OUTPUT_DIR: $!";
  }
  mkdir($OUTPUT_DIR, 0700);
}

# Generate the report XML.
sub writeReport($$$) {
  my ($status_log_ref, $build_id, $out_file) = @_;
  open(OUT, ">$out_file") or die "$out_file: $!";
  print OUT
      qq'<report id="$build_id">\n'
      . (join "\n", @{$status_log_ref})
      . qq'\n</report>\n';
  close(OUT);
}

# A string that will be interpreted as an XML text node equivalent to the input.
sub xml($) {
  my $s = shift;
  $s =~ s/&/&amp;/g;
  $s =~ s/</&lt;/g;
  $s =~ s/>/&gt;/g;
  $s =~ s/\"/&quot;/g;
  return $s;
}

# A string that can be executed in bash to produce the same result as
# system with the same parameters.
sub cmd(@) {
  my @shell = ();
  foreach my $arg (@_) {
    my $arg_esc;
    if ($arg =~ /\'/) {
      $arg_esc = $arg;
      $arg_esc =~ s/[\"\$\@\\]/\\$&/g;
      $arg_esc = qq'"$arg_esc"';
    } else {
      $arg_esc = "'$arg'";
    }
    push(@shell, $arg_esc);
  }
  return (join ' ', @shell);
}


# Extract all varz into one xml file from the historical logs.
sub extractTimeSeries() {
  print STDERR "extracting time series\n";
  open(OUT, ">$OUTPUT_DIR/time-series.xml") or die "timeseries.xml: $!";
  print OUT "<time-series>\n";
  foreach my $historical_report (sort(glob("$HISTORY_DIR/*"))) {
    my $xml = "";
    open(IN, "<$historical_report") or die "$historical_report: $!";
    while (<IN>) { $xml .= $_; }
    close(IN);

    $xml =~ s/<[!CDATA[.*?]]>|<!--.*?-->//g;
    die "Malformed historical report $historical_report"
        unless $xml =~ m/<report\b[^>]*\bid="(\w+)"/;
    my $report_id = $1;
    print OUT qq'<instant id="$report_id">\n';
    while ($xml =~ s/<varz[^>]*>//) {
      print OUT "$&\n";
    }
    print OUT qq'</instant>\n';
  }
  print OUT "</time-series>\n";
  close(OUT);
}


# Process the report XML and derivative XML files to produce HTML/XHTML.
sub makeDashboard() {
  my $dashboard_root = "$BUILD_CLIENT/tools/dashboard";

  foreach my $xsl_file (glob("$dashboard_root/reports/*.xsl")) {
    my $html_output = "$OUTPUT_DIR/"
        . substr($xsl_file, length("$dashboard_root/reports/"));
    die "Bad stylesheet filename: $xsl_file"
        unless $html_output =~ s/\.xsl$/\.html/;

    print STDERR "$xsl_file -> $html_output\n";
    system($XSLTPROC, '-o', $html_output, '--novalid', '--nonet',
           $xsl_file, "$OUTPUT_DIR/index.xml");
    die "xsltproc failed on $xsl_file" if $?;
  }

  foreach my $xsl_file (glob("$dashboard_root/time-series/*.xsl")) {
    my $xhtml_output = "$OUTPUT_DIR/"
        . substr($xsl_file, length("$dashboard_root/time-series/"));
    die "Bad stylesheet filename: $xsl_file"
        unless $xhtml_output =~ s/\.xsl$/\.xhtml/ ;

    print STDERR "$xsl_file -> $xhtml_output\n";
    system($XSLTPROC, '-o', $xhtml_output, '--novalid', '--nonet',
           $xsl_file, "$OUTPUT_DIR/time-series.xml");
    die "xsltproc failed on $xsl_file" if $?;
  }

  foreach my $supporting_file (glob("$dashboard_root/files/*")) {
    system('cp', $supporting_file, $OUTPUT_DIR);
  }
}


collectCodeStats();

extractTimeSeries();

makeDashboard();
