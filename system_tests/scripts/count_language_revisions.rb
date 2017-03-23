#
# Examine the raw XML from a MediaWiki dump and list the
# number of revisions for each page containing the target langauge.
#

language = ARGV[0];

title = nil
revision_count = 0
language_count = 0

in_page = false

foundOnce = false

page_count = 0;
while line = STDIN.gets
  if (line =~ /<page>/)
    in_page = true;
    title = "<no title yet>"
  elsif (line =~ /<\/page>/)
    in_page = false
    puts "%10d %4d %4d %s" % [page_count, language_count, revision_count, title]
    page_count += 1
    revision_count = 0
    language_count = 0
  elsif (line =~ /<title>(.+)<\/title>/)
    title = $1
    title.gsub!('&quot;', '"')
    title.gsub!('&amp;', '&')
    if (!in_page)
      puts("ERROR! Found title outside page. #{title}")
    end
    revision_count = 0
  elsif (line =~ /<revision>/)
    in_revision = true
    foundOnce = false
    if (!in_page)
      puts("ERROR! Found revision outside page. #{title}")
    end

    revision_count += 1

    # The full regular expression for a language header is expensive (time-wise)
    # so, we first do a quick check for the bare language before
    # doing the more expensive check for the entire, correctly formatted header
  elsif (!foundOnce && line =~ /#{language}/ && line =~ /(^|[^=])==\s*(\[\[#{language}\]\]|#{language})\s*==([^=]|$)/)
    # Some revisions have ==Polish== more than once.  Two leading
    # causes (1) an extra ==Polish== section.  Such extra sections
    # are usually condensed into one within a few revisions.
    # (2) the <comment> section occasionally contains ==Polish==
    if (!foundOnce)
      language_count += 1
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

