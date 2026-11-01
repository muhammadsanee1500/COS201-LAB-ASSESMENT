import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dependency-free regression checks; throws on any failed assertion. */
public final class CourseManagerTest {
    private static int checks;

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
        checks++;
    }

    private static void rejects(Runnable operation) {
        try { operation.run(); }
        catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("Expected invalid input to be rejected");
    }

    public static void main(String[] args) throws Exception {
        CourseManager manager = new CourseManager();
        check(manager.totalUnits() == 0, "Empty total");
        check(manager.find("COS 201") == null, "Empty search");
        Course course = new Course(" cos   201 ", " Computer Programming I ", 3);
        check(course.getCode().equals("COS 201"), "Code normalization");
        check(course.getTitle().equals("Computer Programming I"), "Title trimming");
        manager.add(course);
        manager.add(new Course("CYB 203", "Cybercrime Law and Countermeasures", 2));
        check(manager.find(" cos 201 ") == course, "Case-insensitive search");
        check(manager.find("MTH 205") == null, "Missing search");
        check(manager.totalUnits() == 5, "Recursive total");
        rejects(() -> manager.add(new Course("cos 201", "Duplicate", 2)));
        rejects(() -> new Course("", "Title", 2));
        rejects(() -> new Course("123", "Title", 2));
        rejects(() -> new Course("COS\t201", "Title", 2));
        rejects(() -> new Course("COS 202", "  ", 2));
        rejects(() -> new Course("COS 202", "Bad\tTitle", 2));
        rejects(() -> new Course("COS 202", "Title", 0));
        rejects(() -> new Course("COS 202", "Title", 11));
        rejects(() -> new Course("COS 202", "x".repeat(121), 2));
        check(manager.getCourses().size() == 2, "Rejected add preserves courses");
        try { manager.getCourses().clear(); throw new AssertionError("Mutable view"); }
        catch (UnsupportedOperationException expected) { checks++; }

        Path directory = Files.createTempDirectory("course-manager-test-");
        try {
            Path file = directory.resolve("courses.tsv");
            manager.save(file);
            CourseManager restored = new CourseManager();
            restored.load(file);
            check(restored.getCourses().size() == 2, "Round-trip size");
            check(restored.find("COS 201").getTitle().equals(course.getTitle()), "Round-trip title");
            check(restored.totalUnits() == 5, "Round-trip units");
            restored.add(new Course("ENT 211", "Entrepreneurship – Innovation", 2));
            restored.save(file);
            manager.load(file);
            check(manager.totalUnits() == 7, "Overwrite and UTF-8 round trip");
            check(manager.find("ENT 211").getTitle().contains("–"), "UTF-8 preserved");
            String[] invalidFiles = {"", "BAD_HEADER\n", "COURSES_V1\nBAD\n",
                "COURSES_V1\nCOS 201\tTitle\tNaN\n",
                "COURSES_V1\nCOS 201\tTitle\t0\n",
                "COURSES_V1\nCOS 201\tTitle\t3\ncos 201\tDuplicate\t2\n"};
            for (String invalid : invalidFiles) {
                Files.writeString(file, invalid);
                try { manager.load(file); throw new AssertionError("Invalid file accepted"); }
                catch (IOException expected) {
                    check(manager.totalUnits() == 7, "Failed load preserves current courses");
                }
            }
            try { manager.load(directory.resolve("missing.tsv")); throw new AssertionError("Missing file"); }
            catch (IOException expected) { checks++; }
            try { manager.save(directory.resolve("missing/courses.tsv")); throw new AssertionError("Missing parent"); }
            catch (IOException expected) { checks++; }
            new CourseManager().save(file);
            manager.load(file);
            check(manager.getCourses().isEmpty() && manager.totalUnits() == 0, "Empty file round trip");
            StringBuilder large = new StringBuilder("COURSES_V1\n");
            for (int i = 0; i < 10000; i++) { large.append("C").append(i).append("\tCourse\t1\n"); }
            Files.writeString(file, large);
            manager.load(file);
            check(manager.totalUnits() == 10000, "Recursive total on 10,000 courses");
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
        System.out.println("PASS: " + checks + " core checks.");
    }
}
