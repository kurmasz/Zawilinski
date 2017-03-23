#
# Examine the raw XML from a MediaWiki dump and list the
# number of revisions for each page.
#

title = nil
revisionCount = -1
pageCount = 0;
while line = STDIN.gets
  if (line =~ /<title>(.+)<\/title>/)
    if (!title.nil?)
      puts "%10d %4d %s" % [pageCount, revisionCount, title]
      pageCount += 1
    end
    title = $1
    title.gsub!('&quot;', '"')
    title.gsub!('&amp;', '&')
    revisionCount = 0
  end
  
  if (line =~ /<revision>/)
    revisionCount += 1
  end
end
puts "%10d %4d %s" % [pageCount, revisionCount, title]
