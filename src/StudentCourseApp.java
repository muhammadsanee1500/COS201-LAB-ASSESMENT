import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Scanner;

/** Organized console menu for the Student Course Management mini-system. */
public final class StudentCourseApp {
    private final Scanner input;
    private final CourseManager manager = new CourseManager();
    private boolean changed;

    public StudentCourseApp(Scanner input) { this.input = input; }

    public static void main(String[] args) {
        new StudentCourseApp(new Scanner(System.in)).run();
    }

    private String read(String prompt) {
        System.out.print(prompt);
        return input.hasNextLine() ? input.nextLine().trim() : null;
    }

    public void run() {
        System.out.println("STUDENT COURSE MANAGEMENT SYSTEM");
        while (true) {
            System.out.println("\n1. Add Course\n2. View All Courses\n3. Search Course by Code"
                    + "\n4. Compute Total Units\n5. Save to File\n6. Load from File\n7. Exit Program");
            String choice = read("Select an option: ");
            if (choice == null) {
                System.out.println("\nInput closed." + (changed ? " Unsaved changes were not saved." : ""));
                return;
            }
            try {
                switch (choice) {
                    case "1" -> addCourse();
                    case "2" -> viewCourses();
                    case "3" -> searchCourse();
                    case "4" -> System.out.println("Total units: " + manager.totalUnits());
                    case "5" -> saveCourses();
                    case "6" -> loadCourses();
                    case "7" -> {
                        if (!changed || "y".equalsIgnoreCase(read("Exit without saving changes? (y/N): "))) {
                            System.out.println("Goodbye.");
                            return;
                        }
                    }
                    default -> System.out.println("Invalid selection. Enter a number from 1 to 7.");
                }
            } catch (NumberFormatException exception) {
                System.out.println("Invalid units. Enter a whole number from 1 to 10.");
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid input: " + exception.getMessage());
            } catch (IOException exception) {
                System.out.println("File operation failed: " + exception.getMessage());
            }
        }
    }

    private void addCourse() {
        String code = read("Course code: ");
        if (code == null) { return; }
        String title = read("Course title: ");
        if (title == null) { return; }
        String units = read("Units (1–10): ");
        if (units == null) { return; }
        manager.add(new Course(code, title, Integer.parseInt(units)));
        changed = true;
        System.out.println("Course added.");
    }

    private void viewCourses() {
        if (manager.getCourses().isEmpty()) {
            System.out.println("No courses recorded.");
            return;
        }
        System.out.printf("%-30s | %-50s | Units%n", "CODE", "TITLE");
        for (Course course : manager.getCourses()) { System.out.println(course); }
    }

    private void searchCourse() {
        String code = read("Course code to search: ");
        if (code == null) { return; }
        Course course = manager.find(code);
        System.out.println(course == null ? "Course not found." : course);
    }

    private Path filePath() throws InvalidPathException {
        String value = read("File path (Enter for courses.tsv): ");
        return value == null ? null : Path.of(value.isEmpty() ? "courses.tsv" : value);
    }

    private void saveCourses() throws IOException {
        Path path = filePath();
        if (path == null) { return; }
        if (java.nio.file.Files.exists(path)
                && !"y".equalsIgnoreCase(read("Overwrite existing file? (y/N): "))) {
            System.out.println("Save cancelled.");
            return;
        }
        manager.save(path);
        changed = false;
        System.out.println("Saved " + manager.getCourses().size() + " course(s) to " + path);
    }

    private void loadCourses() throws IOException {
        if (changed && !"y".equalsIgnoreCase(read("Replace unsaved courses? (y/N): "))) {
            System.out.println("Load cancelled.");
            return;
        }
        Path path = filePath();
        if (path == null) { return; }
        manager.load(path);
        changed = false;
        System.out.println("Loaded " + manager.getCourses().size() + " course(s).");
    }
}
