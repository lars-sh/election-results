# PMD: The Maven Plugin tries to perform Java type resolution on POM typed projects.
function suppressPmdWarnings() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] Auxclasspath entry .*[/\\\\]election-results[/\\\\]target[/\\\\]classes doesn't exist, ignoring it$"
}

cat < /dev/stdin \
| suppressPmdWarnings
