# -*- coding: utf-8 -*-
#
# Examine the raw XML from a MediaWiki dump and list the
# number of revisions for each page containing the target langauge.
#
# Notable "interesting" cases:
#
# Some words have ==Polish and Russian== as a language.  Both this 
# script and Zawilinski correclty ignore it.
#
# Some words have whitespace between ==Polish== and the newline.
# Both this script and Zawilinski correctly handle these cases 
# by including the revision.
#
# Some words have ==Polish==&lt; other strange stuff
# Both this script and Zawilinski correctly handle these cases
# by including the revision.


# Count and display info for only those pages containing 
# data for the target language
language_only = ARGV.any? {|item| item == "--languageOnly"}

# Show the total number of revisions --- including those that do not 
# include dtata for the target langaugex1
show_total_revisions = ARGV.any? {|item| item == "--showTotalRevs"}

# display debug info
debug  = ARGV.any? {|item| item == "--debug"}

# Remove any options from the command-line parameters.
# NOTE:  This only works if all possible parameters take 
# no options!
non_options = ARGV.reject {|item| item.start_with?("--")}



if non_options.length < 1
  $stderr.puts "Usage:  count_langauge_revisions.rb language"
  exit 1
end

language = non_options[0]

title = nil
total_revisions = 0           # Total number of revisions for this page
revisions_with_language = 0   # Number of revisions containing target language

# The target language has been found at least once in this revision.
found_once = false            

# The current line is within a <page></page> pair
in_page = false               

line_num = 0
page_count = 0
while line = STDIN.gets
  line.strip!
  if (line.include?('<page>'))
    in_page = true;
    title = "<no title yet>"
  elsif (line.include?('</page>'))
    in_page = false
    if ((!language_only || revisions_with_language != 0))
      # only show total revisions when requested.
      total_revisions_format = show_total_revisions ? "%4d " : ""  
      puts "%10d %4d #{total_revisions_format}#{title}" % 
        [page_count, revisions_with_language, total_revisions]
      page_count += 1
    end
    total_revisions = 0
    revisions_with_language = 0
  elsif (line.include?('<title>') && line =~ /<title>(.+)<\/title>/)
    title = $1
    title.gsub!('&quot;', '"')
    title.gsub!('&amp;', '&')
    if (!in_page)
      puts("ERROR! Found title outside page. #{title}")
    end
    total_revisions = 0
  elsif (line.include?('<revision>'))
    in_revision = true
    found_once = false
    if (!in_page)
      puts("ERROR! Found revision outside page. #{title}")
    end

    total_revisions += 1

    # The full regular expression for a language header is expensive (time-wise)
    # so, we first do a quick check for the bare language before
    # doing the more expensive check for the entire, correctly formatted header
    #
    # REMEMBER!  The line was stripped of leading and trailing whitespace!!!
  elsif (!found_once && line.include?(language) && 
         line =~ /(^|[^=])==\s*(\[\[#{language}\]\]|#{language})\s*==([^=]|$)/)
    # Some revisions have ==Polish== more than once.  Two leading
    # causes (1) an extra ==Polish== section.  Such extra sections
    # are usually condensed into one within a few revisions.
    # (2) the <comment> section occasionally contains ==Polish==

    if (!found_once)
      revisions_with_language += 1
    end
    found_once = true
    if (!in_revision)
      puts("ERROR! Found language header outside revision. #{title}")
    end
  elsif (debug && !found_once && line.include?(language) && line.include?("==")) 
    puts "#{line_num} Almost:  =>#{line}<="
  elsif (line.include?('</revision>'))
    if (!in_revision)
      puts("ERROR! Found end_revision header" +
               "outside revision. #{title}")
    end
    in_revision = false
  end
  line_num += 1
end

