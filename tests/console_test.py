"""End-to-end tests run the real console application in isolated directories."""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
CLASSPATH = str(ROOT / 'build')
checks = 0

def run(text, directory):
    result = subprocess.run(['java', '-cp', CLASSPATH, 'StudentCourseApp'],
                            input=text, text=True, capture_output=True,
                            cwd=directory, timeout=15)
    assert result.returncode == 0, result.stderr
    assert not result.stderr, result.stderr
    return result.stdout

def expect(output, *texts):
    global checks
    for text in texts:
        assert text in output, (text, output)
        checks += 1

with tempfile.TemporaryDirectory() as directory:
    out = run('2\n4\n3\nCOS 201\nabc\n0\n8\n7\n', directory)
    expect(out, 'No courses recorded.', 'Total units: 0', 'Course not found.',
           'Invalid selection.', 'Goodbye.')
    out = run('1\n cos 201 \n Computer Programming I \n3\n'
              '1\nCOS 201\nDuplicate\n2\n'
              '1\nCYB 203\nTitle\nabc\n'
              '1\nCYB 203\nTitle\n0\n'
              '3\ncos 201\n4\n5\n\n7\n', directory)
    expect(out, 'Course added.', 'already exists', 'Invalid units.',
           'Units must be between', 'Computer Programming I', 'Total units: 3',
           'Saved 1 course(s)', 'Goodbye.')
    out = run('6\n\n2\n4\n5\n\nn\n7\n', directory)
    expect(out, 'Loaded 1 course(s).', 'COS 201', 'Total units: 3', 'Save cancelled.')
    out = run('6\nmissing.tsv\n5\nmissing/out.tsv\n7\n', directory)
    expect(out, 'File operation failed:', 'Goodbye.')
    out = run('1\nCOS 201\nProgramming\n3\n6\nn\n7\nn\n4\n7\ny\n', directory)
    expect(out, 'Load cancelled.', 'Total units: 3', 'Goodbye.')
    out = run('1\nCOS 201\nProgramming\n3\n', directory)
    expect(out, 'Input closed. Unsaved changes were not saved.')
    for partial in ['1\n', '1\nCOS 201\n', '3\n', '5\n', '6\n']:
        expect(run(partial, directory), 'Input closed.')
    Path(directory, 'bad.tsv').write_text('bad file\n')
    out = run('1\nCOS 201\nProgramming\n3\n6\ny\nbad.tsv\n4\n7\ny\n', directory)
    expect(out, 'Invalid course file header.', 'Total units: 3')

print(f'PASS: {checks} console checks.')
