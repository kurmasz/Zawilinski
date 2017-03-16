#! /usr/bin/ruby 
printing = true
preamble = true
before_target = false
in_target = false

if ARGV.length < 1
  puts "Usage isolatePage.rb title"
  exit(0)
end

target = ARGV[0]

$stdin.each_line do |line|

  if (preamble && line =~ /<page>/)
    preamble = false
    printing = false
    before_target = true

  elsif (before_target && line =~ /<title>#{target}<\/title>/)
     puts "<page>"
    before_target = false
    in_target = true
    printing = true

  elsif (in_target && line =~ /<\/page>/)
    puts line
    puts "</mediawiki>"
    exit 0;
    printing = false
  end

  puts line if (printing)
end
