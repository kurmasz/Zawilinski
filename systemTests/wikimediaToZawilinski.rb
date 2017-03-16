#
# The JAXB writer produces XML formatted slightly differently than the wikimedia dumps.
# This script converts Wikimedia dumps into the format produced by Zawilinksi.  After
# this transformation we can compare Zawilinksi output to expected results.
#

first = true
$stdin.each_line do |line|

  # add the <?xml header if it doesn't exist.  
  if first && line !~ /^<\?xml/ 
    puts '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    first = false
  end  

  if (line =~ /mediawiki/)
    # remove attributes that the WiktionaryWriter doesn't print
    line.gsub!(/xmlns:xsi="[^"]*"/, "")
    line.gsub!(/xsi:schemaLocation="[^"]*"/, "")
  end

  line.gsub!("&quot;", '"')

  # add xmlns: to minor
  if line =~ /<minor\s+\/>/
    puts '<minor xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>'

  # Change <foo bar/> tags to <foo bar></foo>
  elsif line =~ /<(\S+)(.*)\/>/
    puts  "<#{$1} #{$2}></#{$1}>"

  else
    puts line
  end
end

