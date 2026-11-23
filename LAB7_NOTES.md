# Lab 7 notes (Ikenna Anyanwu, ianyanwu)

Only files under src/test/java were added or changed. App code is untouched.

Run the graded suite:   mvn verify -Dui.excludedGroups=examples,defect
Run the defect tests:   mvn verify -Dui.excludedGroups=examples
Run one mutant:         mvn verify -Dui.excludedGroups=examples,defect -Dlab.mutant=P1

Page Objects: ui/pages (BasePage, StudentListPage, CourseListPage)
Support:      ui/support (ApiSetup creates test data through the REST API; TestData)
Tests:        AntiPatternFixedIT, StudentCreateIT, StudentEditDeleteIT, CourseCreateIT,
              CourseEditDeleteIT, EnrollmentIT, NavigationIT
Test case ids (S-xx, C-xx, E-xx, N-xx) are in each @DisplayName and in the report.
