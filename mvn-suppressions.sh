# JFX 17: The parent POM contains duplicate profile IDs
function suppressDuplicateProfileIdsInJfxParentPom() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] Failed to build parent project for org\\.openjfx:javafx-(base|controls|fxml|graphics):jar:17\\.0\\.0\\.1$"
}

# PMD: The Maven Plugin tries to perform Java type resolution on POM typed projects.
function suppressPmdWarnings() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] Auxclasspath entry .*[/\\\\]election-results[/\\\\]target[/\\\\]classes doesn't exist, ignoring it$"
}

cat < /dev/stdin \
| suppressDuplicateProfileIdsInJfxParentPom \
| suppressPmdWarnings
