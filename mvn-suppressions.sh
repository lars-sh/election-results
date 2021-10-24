# JFX 11 and later: The parent POM contains duplicate profile IDs
function suppressDuplicateProfileIdsInJfxParentPom() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] Failed to build parent project for org\\.openjfx:javafx-(base|controls|fxml|graphics):jar:17\\.0\\.1$"
}

# JaCoCo Maven Plugin: The Maven Plugin is referencing a moved Maven artifact.
function suppressJaCoCoMavenPlugin() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] The artifact xml-apis:xml-apis:jar:2\\.0\\.2 has been relocated to xml-apis:xml-apis:jar:1\\.0\\.b2$"
}

# PMD: The Maven Plugin tries to perform Java type resolution on POM typed projects.
function suppressPmdWarnings() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] Auxclasspath entry .*[/\\\\]election-results[/\\\\]target[/\\\\]classes doesn't exist, ignoring it$"
}

cat < /dev/stdin \
| suppressDuplicateProfileIdsInJfxParentPom \
| suppressJaCoCoMavenPlugin \
| suppressPmdWarnings
