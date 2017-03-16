#! /usr/bin/ruby


#
# Estimate the number of pages and revisions that contain data for the 
# specified langauge
#

# When "--all" present, produce a line of output for every page 
# Otherwise, only produce a line of output for pages with "==Polish==" content
show_all = ARGV.any? {|item| item == "--all"}

progress = ARGV.any? {|item| item == "--progress"}

$stderr.puts "Full dump has 3.7 billion lines" if progress

language = "Polish"

total_found = 0;

revision_count = 0
current_title = ""
found_in_revision = false
line_count = 0;
$stdin.each_line do |line|

  line_count += 1
  if (line_count % 10000000 == 0) 
      $stderr.puts "#{line_count} #{`date`}"
  end

  revision_count = 0 if line =~ /<page>/
  found_in_revision = false if line =~ /<revision>/
  current_title = $1 if line =~ /<title>(.*)<\/title>/

  # The post filter expects the language header to be on a line by itself.
  # Header is usually ==Polish==, but could also be
  # == Polish ==
  # ==[[Polish]]==
  # == [[Polish]] ==
  if line.include?(language) && line =~ /==( )*(\[\[)?#{language}(\]\])?( )*==$/
    found_in_revision = true 
  end

  revision_count += 1 if line =~ /<\/revision>/ && found_in_revision

  puts "#{revision_count}\t #{current_title}"   if line =~ /<\/page>/ && (revision_count > 0 || show_all)
end
