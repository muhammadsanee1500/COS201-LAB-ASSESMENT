import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Stores courses and handles UTF-8 tab-separated persistence. */
public final class CourseManager {
    private static final String HEADER = "COURSES_V1";
    private List<Course> courses = new ArrayList<>();

    public void add(Course course) {
        if (find(course.getCode()) != null) {
            throw new IllegalArgumentException("A course with this code already exists.");
        }
        courses.add(course);
    }

    public List<Course> getCourses() { return List.copyOf(courses); }

    public Course find(String code) {
        String normalized = Course.normalizeCode(code);
        for (Course course : courses) {
            if (course.getCode().equals(normalized)) { return course; }
        }
        return null;
    }

    public int totalUnits() { return sumUnits(0, courses.size()); }

    // Divide the range recursively; logarithmic depth avoids a long call stack.
    private int sumUnits(int start, int end) {
        if (start == end) { return 0; }
        if (end - start == 1) { return courses.get(start).getUnits(); }
        int middle = start + (end - start) / 2;
        return sumUnits(start, middle) + sumUnits(middle, end);
    }

    public void save(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Course course : courses) {
            lines.add(course.getCode() + "\t" + course.getTitle() + "\t" + course.getUnits());
        }
        Path target = path.toAbsolutePath();
        Path temporary = Files.createTempFile(target.getParent(), "courses-", ".tmp");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public void load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !HEADER.equals(lines.get(0))) {
            throw new IOException("Invalid course file header.");
        }
        CourseManager loaded = new CourseManager();
        for (int index = 1; index < lines.size(); index++) {
            String[] fields = lines.get(index).split("\t", -1);
            try {
                if (fields.length != 3) {
                    throw new IllegalArgumentException("Expected code, title and units.");
                }
                loaded.add(new Course(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid data on line " + (index + 1) + ": "
                        + exception.getMessage(), exception);
            }
        }
        courses = loaded.courses; // Replace only after the entire file passes validation.
    }
}
