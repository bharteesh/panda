#!/opt/local/bin/perl

use strict;
use Text::WagnerFischer qw(distance);
use YAML;
use YAML::Dumper;

my $dumper = YAML::Dumper->new;
$dumper->indent_width(4);

my(%p);

open(INF, "$ARGV[0]");
my(%t) = map { chomp; prep($_), 1; } <INF>;
close(INF);

my($t);
foreach $t ( sort keys (%t) ) {
    my($x);
    my(@r);
    foreach $x ( sort keys ( %t ) ) {
	if ( $t eq $x ) { next; }
	if ( $t =~ m/para-/ && $x !~ m/para-/ ) { next; }
	if ( $t !~ m/para-/ && $x =~ m/para-/ ) { next; }
	if ( similarFirstWords($x, $t) ) {
	    my($distance, $dissimilar) = wdistance($t, $x);
	    if ( ! $dissimilar ) {
		push(@r, sprintf( "%d\t%s\n", $distance, $x ) );
	    }
	}
    }
    if ( scalar @r ) {
	printf "START: %s\n", $t;
	print @r;
	printf "\n";
    }
}

sub wdistance {
   my($t1, $t2) = @_;
   my(@w1) = grep(m/\S+/, split(m/\s+/, $t1));
   my(@w2) = grep(m/\S+/, split(m/\s+/, $t2));

   my(%f);
   my($g);
   foreach $g ( @w1 ) {
       $f{$g}++;
   }
   foreach $g ( @w2 ) {
       $f{$g}++;
   }
   my($d);
   foreach $g ( keys %f ) {
       if ( $f{$g} == 1 ) {
	   $d++;
       }
   }

   

   return $d, ($d > ((scalar keys %f)/2.0) ? 1 : 0);
}

#print $dumper->dump(\%t); 

sub similarFirstWords {
    my($t1, $t2) = @_;

    if ( (length($t1) > length($t2) * 2) || (length($t1) * 2 < length($t2)) ) {
	return 0;
    }

    my(@w1) = grep(m/\S+/, split(m/\s+/, $t1));
    my(@w2) = grep(m/\S+/, split(m/\s+/, $t2));

    if ( ($#w1 > $#w2+3) || ($#w1+3 < $#w2) ) {
	return 0;
    }

    my(%f);
    my($g);
    foreach $g ( @w1[0..3] ) {
	$f{$g}++;
    }
    foreach $g ( @w2[0..3] ) {
	$f{$g}++;
    }

    if ( (scalar keys %f) > 5 ) {
	return 0;
    }

    return 1;
}

sub prep {

    my($t) = shift;
    if ( $p{$t} ) { return $p{$t}; }

    my($x) = lc($t);
    $x =~ s,\s, ,gsm;
    $x =~ s,  +, ,gsm;
    $x =~ s,^ +,,;
    $x =~ s, +$,,;
    $x =~ s,^the ,,;
    $x =~ s,^\(the\) ,,;

    $p{$t} = $x;
    return $x;
}
