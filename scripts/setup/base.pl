#!/usr/bin/env perl
use v5.10;
use strict;
use warnings;

###########################################################
# Basic script for installing the base of our dependencies
##########################################################

#install cpanm
`curl -L https://cpanmin.us | perl - --sudo App::cpanminus`;

my @modules = qw(
    Perlmazing
    Getopt::Long
    Template::Tiny
    JSON
    FindBin
);

foreach my $modules (@modules) {
    `cpanm $module`;
}