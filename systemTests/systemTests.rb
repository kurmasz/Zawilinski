#! /usr/bin/ruby


def check_file(file) 
  if (!File.exist?(file)) 
    puts "File #{file} doesn't exist."
    exit 0;
  end

  if (!File.readable?(file))
    puts "File #{file} is not readable."
    exit 0;
  end
end


def run_command(command) 
  puts "Running #{command}"
  `#{command}`
end


###############################################################################
#
# Main
#
###############################################################################

test_num = 1
faiures = false

jarFile = '../dist/zawilinski-1.0.3.jar'
fullDump = '/research/kurmasz/enwiktionary-20170201-pages-meta-history.xml'
fullDump = ARGV[0] if ARGV.length > 0

execute_long_runs = false

check_file(jarFile)
check_file(fullDump)

if (fullDump.end_with?('bz2'))
  readDump = "bunzip2 -c #{fullDump}"
else
  readDump = "cat #{fullDump}"
end


puts "Using jar #{jarFile}"
puts "Using dump #{fullDump}"

Dir.mkdir 'output' unless File.exist?('output')

#################################################################
#
# (1) Filter by Title can 
#     (a) Handle a full Wiktionary dump
#     (b) Finds all titles matching the listed regexps
#     (c) Finds only titles matching the listed regexp
#     (d) Interprets parameters as regular expressions
#
#     (e) Retains all the data for selected entries
#
#################################################################

# These patterns were chosen to (1) produce a relatively small output, and (2) use a variety of regexp features.
patterns = ['xyz', 'a.x', 'zo.+u.*u']
z_patterns = patterns.map {|p| "'#{p}'"}.join(' ')
g_patterns = patterns.join("|")

filter_dump_by_title = "output/filterDumpByTitle.xml"
grep_dump_by_title = "output/grepDumpByTitle.txt"

run_command("java -jar #{jarFile} Title #{fullDump} #{z_patterns} > #{filter_dump_by_title}") if execute_long_runs
run_command("#{readDump} | grep '<title>' | egrep '#{g_patterns}' > #{grep_dump_by_title}") if execute_long_runs

# (a) (b) (c) (d)
# The Wikitionary dump tends to put <title> tags on their own line.
# If we grep the "filter by title" output for <title> tags, we should get the same output generated
# by the grep above.  (The -w flag ignores whitespace)

diff_results = `grep '<title>' #{filter_dump_by_title} | diff -w - #{grep_dump_by_title}`

# If the two lists match, then precisely the desired titles were found.  This also means that the tool could 
# handle the complete wiktionary dump, and interpreted all the regular expressions given.
if (diff_results != "") 
  puts "Filter By Title failure.  Different titles filtered:"
  puts diff_results
  failures = true
else
  puts "*** Test #{test_num} passed ***"
end 
test_num += 1

# (e) When a matching title is found, all the data for that page should be preserved (unless some data is dumped
#     because the page has been vandalized)  head_of_dump contains a few pages at the beginning of the dump.
#     expected_output is the expected result of filtering by title.  We generated this file by hand by 
#     copying the input, opening the copy in a text editor and deleting the unwanted pages.  
#     Because the JAXB writer doesn't produce XML in precisely the same way as the raw wiktionary dumps, 
#     we must transform the expected_output into transformed_expected_output

head_of_dump = "data/enwiktionary-200170201-beginningOnly.xml"
expected_output = "data/enwiktionary-200170201-UserAndFreeAtBeginning.xml"
transformed_expected_output = "output/tmp_UserAndFreeAtBeginning.xml"

run_command("ruby wikimediaToZawilinski.rb < #{expected_output} > #{transformed_expected_output}")

diff_results = run_command("java -jar #{jarFile} Title #{head_of_dump} User free | diff -w - #{transformed_expected_output}")
if (diff_results != "") 
    puts "Filter by Title failure.  Contents of chosen pages differs:"
    puts diff_results
    failures = true
else
  puts "*** Test #{test_num} passed ***"
end
test_num += 1







puts "\n\n"
puts failures ? "There were failures" : "All tests pass"

