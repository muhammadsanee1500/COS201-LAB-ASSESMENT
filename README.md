# Student Course Management System

Java console project for the COS 201 lab assessment. All seven required menu operations are implemented, with course validation, recursive total units and UTF-8 file persistence.

## Build and run

Requires Java 21. With a JDK installed:

```sh
bash scripts/build.sh
java -cp build StudentCourseApp
```

In the prepared cloud environment the build script falls back to the verified Eclipse Java compiler at `/workspace/.tools/ecj-3.42.0.jar` when `javac` is absent. There are no application libraries or background services.

## Tests

```sh
bash scripts/test.sh
```

Runs 33 core checks and 30 console checks. Python 3 is needed for the console tests. Tests use temporary files and leave the user's saved courses alone.

## Menu

1. Add Course
2. View All Courses
3. Search Course by Code
4. Compute Total Units
5. Save to File
6. Load from File
7. Exit Program

Codes are normalized to uppercase, with repeated spaces collapsed. Duplicate codes are rejected. Units must be whole numbers from 1 to 10. Titles cannot contain tabs or line breaks. Saved files use a `COURSES_V1` header followed by tab-separated code, title and units. Loading replaces the list after complete validation; it does not merge lists. Confirmations protect existing files and unsaved edits. Data remains in memory until saved.

## Submission files

- `submission/Report.pdf`: two-page report with student details, design, error handling, recursion and test results.
- `submission/Source-Code.pdf`: readable application source appendix.
- `submission/Complete-Submission.pdf`: combined report and source appendix for PDF-only submission.
- `submission/program-running.png`: browser screenshot of actual captured Java console output, rather than a screenshot of a live terminal.
- `submission/program-transcript.txt`: full functional-run stdout.
- `submission/example-courses.tsv`: the eight courses from the supplied image (18 total units). Load this file through menu option 6 to try them.
- `src/`: the three application source files; `tests/`: regression checks.

Review the report and personal details before submission. Confirm that a screenshot of captured console output meets the instructor's screenshot requirement; otherwise take a screenshot of the application running in your own terminal. Follow the institution's rules on permitted assistance and attribution.

## Design

`Course` validates one record. `CourseManager` owns the list and persistence. `StudentCourseApp` controls the console menu. The recursive `sumUnits` splits the list range into halves, returns zero for an empty range and a course's units for a single-record range. Its recursion depth is logarithmic. File saving uses a temporary file and atomic replacement where supported; on filesystems without atomic moves it uses normal replacement.

The report documents tested behavior, not a claim that every possible platform or input has been tested. No GUI, database, authentication or automatic saving is required by this brief.
