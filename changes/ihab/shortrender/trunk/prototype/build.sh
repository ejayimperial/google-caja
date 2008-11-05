rake clean_tmp
rake caja:cajole_gadgets CAJA_SRC_PATH=..
open -a /Applications/Webkit.app test/unit/tmp/truth_test.html
