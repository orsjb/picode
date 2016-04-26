#! /usr/bin/env perl
use Perlmazing;
use Getopt::Long qw( :config auto_help );
use Template::Tiny;
use JSON;
use FindBin qw($Bin);

#defaults:
my $interfaces_template = "$Bin/templates/interfaces-template.txt";
my $interfaces_file     = "/etc/network/interfaces";
my $pi_config_json      = "$Bin/../config/pi-config.json";

GetOptions (
    "interfaces-template=s"     => \$interfaces_template,
    "interfaces-file=s"         => \$interfaces_file,
    "pi-config=s"               => \$pi_config_json,
 ) or croak("Error in command line arguments\n");

#get local pi config
say "Reading in piConfig: '$pi_config_json'";
my $piconfig = decode_json(slurp($pi_config_json));

#check data
croak "Invalid json file :(" unless defined $piconfig;
croak "Missing wifi configuration key :(" unless exists $piconfig->{'wifi'} and is_hash $piconfig->{'wifi'};
#check on our required keys:
map {
    croak "Missing wifi->$_ configuration key" unless exists $piconfig->{'wifi'}{$_} and not_empty $piconfig->{'wifi'}{$_}
} qw(ssid psk);


#get our interfaces template
my $template = Template::Tiny->new( TRIM => 1 );
say "Reading in template: '$interfaces_template'";
my $template_text = slurp($interfaces_template);

#apply values to template
say "New interfaces file:";
my $new_config;
$template->process(
    \$template_text,
    $piconfig->{'wifi'},
    \$new_config,
);

#Apply our new interfaces file :)
say "Writing out interfaces: '$interfaces_file'";
open(my $interfaces_fh, ">", $interfaces_file) or croak "Unable to open '$interfaces_file' for write: $!";
print $interfaces_fh $new_config;
close($interfaces_fh);

say "Updated interfaces successfully.";
