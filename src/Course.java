import java.util.Locale;

/** One validated course. */
public final class Course {
    private final String code;
    private final String title;
    private final int units;

    public Course(String code, String title, int units) {
        this.code = normalizeCode(code);
        this.title = title == null ? "" : title.trim();
        if (!this.code.matches("[A-Z][A-Z0-9 -]{1,29}")) {
            throw new IllegalArgumentException("Code must be 2–30 letters, digits, spaces or hyphens and start with a letter.");
        }
        if (this.title.isEmpty() || this.title.length() > 120
                || this.title.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Title must contain 1–120 characters without tabs or line breaks.");
        }
        if (units < 1 || units > 10) {
            throw new IllegalArgumentException("Units must be between 1 and 10.");
        }
        this.units = units;
    }

    public static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replaceAll(" +", " ");
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getUnits() { return units; }

    @Override
    public String toString() {
        return String.format("%-30s | %-50s | %d", code, title, units);
    }
}
