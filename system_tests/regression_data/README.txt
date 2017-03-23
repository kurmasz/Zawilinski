Directory Contents:

enwiktionary-200170201-beginningOnly.xml
  A few pages copied from the beginning of the full dump.  Used
  to verify that Zawilinski keeps the entire article when filtering
  by title.

enwiktionary-200170201-selectedPages.xml
  Four pages copied from the full dump.  Used to verify that (1)
  Zawilinski correctly retains only the ==Polish== section of articles
  and (2) articles without any ==Polish== sections are removed.

enwiktionary-200170201-UserAndFreeAtBeginning.xml
  Selected pages copied from the original full dump.  These are all the 
  pages in "beginningOnly" that match the regexp "User|Free".  We use
  this as the expected output for applying this filter to the
  "beginningOnly" file.

enwiktionary-200170201-selectedPages-PolishOnly.xml
  The expected result of running Zawilinski's language filter on 
  the "selectedPages" input.  (We created this file by hand.)
