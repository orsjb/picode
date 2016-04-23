#!/usr/bin/env perl
use v5.10;
use strict;
use warnings;

###########################################################
# Basic script for installing the base of our dependencies
##########################################################

#install cpanm
die "Unable to access cpanm setup script: 'install-cpanm.sh'" unless stat "install-cpanm.sh";
unless ( system("./install-cpanm.sh") == 0 ) {
	die "Installation of cpanm failed, aborting setup";
}


my @modules = qw(
    Perlmazing
    Getopt::Long
    Template::Tiny
    JSON
    FindBin
);

foreach my $module (@modules) {
	say "Installing $module...";
    	say `cpanm --sudo $module`;
}
