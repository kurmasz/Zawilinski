#
# Examine the raw XML from a MediaWiki dump and list the
# number of revisions for each page containing the target langauge.
#


# Count and display info for only those pages containing 
# data for the target language
language_only = ARGV.any? {|item| item == "--languageOnly"}

# Show the total number of revisions --- including those that do not 
# include dtata for the target langauge
show_total_revisions = ARGV.any? {|item| item == "--showTotalRevs"}

# Remove any options from the command-line parameters.
# NOTE:  This only works if all possible parameters take 
# no options!
non_options = ARGV.reject {|item| item.start_with?("--")}

language = non_options[0]

title = nil
total_revisions = 0           # Total number of revisions for this page
revisions_with_language = 0   # Number of revisions containing the target language

in_page = false

foundOnce = false

page_count = 0;
while line = STDIN.gets
  if (line =~ /<page>/)
    in_page = true;
    title = "<no title yet>"
  elsif (line =~ /<\/page>/)
    in_page = false
    if (!language_only || revisions_with_language != 0) 
      total_revisions_format = show_total_revisions ? "%4d " : ""  # only show total revisions when requested.
      puts "%10d %4d #{total_revisions_format}#{title}" % [page_count, revisions_with_language, total_revisions]
      page_count += 1
    end
    total_revisions = 0
    revisions_with_language = 0
  elsif (line =~ /<title>(.+)<\/title>/)
    title = $1
    title.gsub!('&quot;', '"')
    title.gsub!('&amp;', '&')
    if (!in_page)
      puts("ERROR! Found title outside page. #{title}")
    end
    total_revisions = 0
  elsif (line =~ /<revision>/)
    in_revision = true
    foundOnce = false
    if (!in_page)
      puts("ERROR! Found revision outside page. #{title}")
    end

    total_revisions += 1

    # The full regular expression for a language header is expensive (time-wise)
    # so, we first do a quick check for the bare language before
    # doing the more expensive check for the entire, correctly formatted header
  elsif (!foundOnce && line =~ /#{language}/ && line =~ /(^|[^=])==\s*(\[\[#{language}\]\]|#{language})\s*==([^=]|$)/)
    # Some revisions have ==Polish== more than once.  Two leading
    # causes (1) an extra ==Polish== section.  Such extra sections
    # are usually condensed into one within a few revisions.
    # (2) the <comment> section occasionally contains ==Polish==
    if (!foundOnce)
      revisions_with_language += 1
    end
    foundOnce = true
    if (!in_revision)
      puts("ERROR! Found langauge header outside revision. #{title}")
    end
  elsif (line =~ /<\/revision>/)
    if (!in_revision)
      puts("ERROR! Found end_revision header" +
               "outside revision. #{title}")
    end
    in_revision = false
  end
end

