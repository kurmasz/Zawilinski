
#
# Estimate the number of pages and revisions that contain data for the specified langauge
#

language = "==Polish=="

total_found = 0;

revision_count = 0
current_title = ""
found_in_revision = false
$stdin.each_line do |line|

  revision_count = 0 if line =~ /<page>/
  found_in_revision = false if line =~ /<revision>/
  current_title = $1 if line =~ /<title>(.*)<\/title>/

  if line.include?(language)
    found_in_revision = true 
  end

  revision_count += 1 if line =~ /<\/revision>/ && found_in_revision

  puts "#{revision_count}\t #{current_title}"   if line =~ /<\/page>/ && revision_count > 0

end
