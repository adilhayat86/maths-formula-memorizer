from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_PATH = ROOT / "app" / "src" / "main" / "java" / "com" / "mominhayat" / "mathsformulamemorizer" / "MainActivity.java"


def find_method_ranges(source: str, signature: str):
    ranges = []
    search_from = 0
    while True:
        start = source.find(signature, search_from)
        if start == -1:
            break
        brace_start = source.find("{", start)
        if brace_start == -1:
            break
        depth = 0
        end = None
        for i in range(brace_start, len(source)):
            ch = source[i]
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    end = i + 1
                    break
        if end is None:
            raise SystemExit(f"Could not find closing brace for {signature}")
        ranges.append((start, end))
        search_from = end
    return ranges


def remove_duplicate_methods(source: str, signature: str) -> str:
    ranges = find_method_ranges(source, signature)
    if len(ranges) <= 1:
        print(f"OK: {signature.strip()} appears {len(ranges)} time(s)")
        return source

    print(f"Fixing: {signature.strip()} appears {len(ranges)} times; keeping first")
    # Remove later copies from end to start so indices stay valid.
    for start, end in reversed(ranges[1:]):
        # Also remove extra blank lines around duplicate method.
        while start > 0 and source[start - 1] == "\n":
            start -= 1
            if start > 0 and source[start - 1] == "\n":
                break
        source = source[:start] + "\n" + source[end:]
    return source


java = JAVA_PATH.read_text(encoding="utf-8").replace("\r\n", "\n")
original = java

# The update scripts may have inserted duplicate helper methods if run more than once or in a different order.
for sig in [
    "    private void showNameSelect()",
    "    private boolean looksLikeFormula(String s)",
    "    private String stars(int correct, int total)",
]:
    java = remove_duplicate_methods(java, sig)

# Fix regex string so Java receives the regex \s+ instead of relying on Java's \s escape behavior.
java = java.replace(r'replaceAll("\s+", " ")', r'replaceAll("\\s+", " ")')
java = java.replace(r'replaceAll("\s+", " ")', r'replaceAll("\\s+", " ")')
java = java.replace('replaceAll("\\s+", " ")', 'replaceAll("\\\\s+", " ")') if 'replaceAll("\\s+", " ")' in java and 'replaceAll("\\\\s+", " ")' not in java else java

# The above handles escaping differences conservatively. Ensure exactly one intended Java source pattern.
java = java.replace('replaceAll("\\\\\\s+", " ")', 'replaceAll("\\\\s+", " ")')

if java != original:
    JAVA_PATH.write_text(java, encoding="utf-8", newline="\n")
    print("Updated MainActivity.java")
else:
    print("No source duplicate fixes were needed")

print("Next: run gradlew.bat assembleDebug or press Run in Android Studio.")
