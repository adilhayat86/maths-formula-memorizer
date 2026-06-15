from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_PATH = ROOT / "app" / "src" / "main" / "java" / "com" / "mominhayat" / "mathsformulamemorizer" / "MainActivity.java"
README_PATH = ROOT / "README.md"

java = JAVA_PATH.read_text(encoding="utf-8").replace("\r\n", "\n")

old_show_class = '''    private void showClassSelect() {
        LinearLayout root = baseScreen("Choose your class", "The app will show formulas suitable for this level.");
        String[] classes = {"Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "General Practice"};
        for (String c : classes) {
            Button b = primaryButton(c);
            b.setOnClickListener(v -> {
                selectedClass = c;
                prefs.edit().putString("selected_class", c).apply();
                showHome();
            });
            root.addView(b);
        }
        TextView note = smallText("Tip: You can change class later from the home screen.");
        note.setPadding(dp(8), dp(16), dp(8), dp(4));
        root.addView(note);
    }
'''

new_show_class = '''    private void showClassSelect() {
        LinearLayout root = baseScreen("Choose your class", "Pick your class once. You can change it later from the top of the home screen.");

        LinearLayout c = card();
        c.addView(sectionTitle("Class"));
        Spinner classSpinner = classSpinner(selectedClass == null ? "Class 6" : selectedClass);
        c.addView(classSpinner);
        c.addView(smallText("This filters formulas and areas of study for your level."));
        root.addView(c);

        Button continueBtn = primaryButton("Continue");
        continueBtn.setOnClickListener(v -> {
            selectedClass = classSpinner.getSelectedItem().toString();
            prefs.edit().putString("selected_class", selectedClass).apply();
            showHome();
        });
        root.addView(continueBtn);
    }
'''

old_show_home = '''    private void showHome() {
        if (selectedClass == null) {
            showClassSelect();
            return;
        }

        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;

        LinearLayout root = baseScreen("Maths Formula Memorizer", selectedClass + " • Offline • No ads • No login");

        LinearLayout statCard = card();
        TextView progress = bigText("Progress: " + completedCount + " / " + available.size() + " lessons");
        statCard.addView(progress);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(Math.max(1, available.size()));
        bar.setProgress(completedCount);
        statCard.addView(bar);
        statCard.addView(smallText("Streak: " + prefs.getInt("streak", 0) + " day(s)"));
        root.addView(statCard);

        Button cont = primaryButton("Start quick practice");
        cont.setOnClickListener(v -> showLesson(nextFormula(available, completed)));
        root.addView(cont);

        LinearLayout actionRow = row();
        Button weak = secondaryButton("Weak formulas");
        weak.setOnClickListener(v -> showWeakFormulas());
        Button progressBtn = secondaryButton("Progress");
        progressBtn.setOnClickListener(v -> showProgress());
        actionRow.addView(weak, rowWeight());
        actionRow.addView(progressBtn, rowWeight());
        root.addView(actionRow);

        TextView unitsTitle = sectionTitle("Learning path");
        root.addView(unitsTitle);

        LinkedHashMap<String, List<Formula>> byTopic = groupByTopic(available);
        for (String topic : byTopic.keySet()) {
            List<Formula> topicFormulas = byTopic.get(topic);
            int done = 0;
            for (Formula f : topicFormulas) if (completed.contains(f.id)) done++;

            LinearLayout topicCard = card();
            topicCard.addView(bigText(topic));
            topicCard.addView(smallText(done + " / " + topicFormulas.size() + " completed"));
            Button open = secondaryButton("Open unit");
            open.setOnClickListener(v -> showTopic(topic));
            topicCard.addView(open);
            root.addView(topicCard);
        }

        Button change = ghostButton("Change class");
        change.setOnClickListener(v -> showClassSelect());
        root.addView(change);
    }
'''

new_show_home = '''    private void showHome() {
        if (selectedClass == null) {
            showClassSelect();
            return;
        }

        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;

        LinearLayout root = baseScreen("Maths Formula Memorizer", "Offline • No ads • No login");

        LinearLayout classCard = card();
        classCard.addView(sectionTitle("Class"));
        Spinner classSpinner = classSpinner(selectedClass);
        final boolean[] skipFirstSelection = {true};
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (skipFirstSelection[0]) {
                    skipFirstSelection[0] = false;
                    return;
                }
                String newClass = CLASS_OPTIONS[position];
                if (!newClass.equals(selectedClass)) {
                    selectedClass = newClass;
                    prefs.edit().putString("selected_class", selectedClass).apply();
                    showHome();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do.
            }
        });
        classCard.addView(classSpinner);
        classCard.addView(smallText("Change class anytime. Progress stays saved on this phone."));
        root.addView(classCard);

        LinearLayout statCard = card();
        TextView progress = bigText("Progress: " + completedCount + " / " + available.size() + " lessons");
        statCard.addView(progress);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(Math.max(1, available.size()));
        bar.setProgress(completedCount);
        statCard.addView(bar);
        statCard.addView(smallText("Streak: " + prefs.getInt("streak", 0) + " day(s)"));
        root.addView(statCard);

        Button cont = primaryButton("Start quick practice");
        cont.setOnClickListener(v -> showLesson(nextFormula(available, completed)));
        root.addView(cont);

        LinearLayout actionRow = row();
        Button weak = secondaryButton("Weak formulas");
        weak.setOnClickListener(v -> showWeakFormulas());
        Button progressBtn = secondaryButton("Progress");
        progressBtn.setOnClickListener(v -> showProgress());
        actionRow.addView(weak, rowWeight());
        actionRow.addView(progressBtn, rowWeight());
        root.addView(actionRow);

        TextView unitsTitle = sectionTitle("Choose area of study");
        root.addView(unitsTitle);

        LinkedHashMap<String, List<Formula>> byTopic = groupByTopic(available);
        for (String topic : byTopic.keySet()) {
            List<Formula> topicFormulas = byTopic.get(topic);
            int done = 0;
            for (Formula f : topicFormulas) if (completed.contains(f.id)) done++;

            LinearLayout topicCard = card();
            topicCard.addView(bigText(topic));
            topicCard.addView(smallText(done + " / " + topicFormulas.size() + " completed"));
            Button open = secondaryButton("Start area");
            open.setOnClickListener(v -> showTopic(topic));
            topicCard.addView(open);
            root.addView(topicCard);
        }
    }
'''

helper_methods = '''    private Spinner classSpinner(String currentClass) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CLASS_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(classIndex(currentClass));
        spinner.setPadding(dp(8), dp(8), dp(8), dp(8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(10));
        spinner.setLayoutParams(lp);
        return spinner;
    }

    private int classIndex(String className) {
        if (className == null) return 0;
        for (int i = 0; i < CLASS_OPTIONS.length; i++) {
            if (CLASS_OPTIONS[i].equals(className)) return i;
        }
        return 0;
    }

'''

changed = False

if "android.widget.Spinner" not in java:
    java = java.replace(
        "import android.view.View;\nimport android.widget.Button;",
        "import android.view.View;\nimport android.widget.AdapterView;\nimport android.widget.ArrayAdapter;\nimport android.widget.Button;"
    )
    java = java.replace(
        "import android.widget.ScrollView;\nimport android.widget.TextView;",
        "import android.widget.ScrollView;\nimport android.widget.Spinner;\nimport android.widget.TextView;"
    )
    changed = True

if "CLASS_OPTIONS" not in java:
    java = java.replace(
        "    private static final int RED = Color.rgb(185, 28, 28);\n",
        "    private static final int RED = Color.rgb(185, 28, 28);\n    private static final String[] CLASS_OPTIONS = {\"Class 6\", \"Class 7\", \"Class 8\", \"Class 9\", \"Class 10\", \"General Practice\"};\n"
    )
    changed = True

if old_show_class in java:
    java = java.replace(old_show_class, new_show_class)
    changed = True
else:
    print("showClassSelect was not replaced; it may already be updated.")

if old_show_home in java:
    java = java.replace(old_show_home, new_show_home)
    changed = True
else:
    print("showHome was not replaced; it may already be updated.")

if "private Spinner classSpinner" not in java:
    marker = "    private int selectedClassNumber() {\n"
    if marker not in java:
        raise SystemExit("Could not find selectedClassNumber marker. Please ask ChatGPT to inspect MainActivity.java.")
    java = java.replace(marker, helper_methods + marker)
    changed = True

if changed:
    JAVA_PATH.write_text(java, encoding="utf-8", newline="\n")
    print("Updated MainActivity.java")
else:
    print("MainActivity.java already looked updated")

if README_PATH.exists():
    readme = README_PATH.read_text(encoding="utf-8").replace("\r\n", "\n")
    original = readme
    readme = readme.replace("1. Choose class\n2. Choose unit", "1. Choose class from a dropdown\n2. Choose area of study")
    readme = readme.replace("- Class selection: Class 6, 7, 8, 9, 10, General Practice", "- Class selection using a drop-down menu: Class 6, 7, 8, 9, 10, General Practice")
    readme = readme.replace("- Learning path grouped by topic", "- Choose area of study screen grouped by topic")
    if readme != original:
        README_PATH.write_text(readme, encoding="utf-8", newline="\n")
        print("Updated README.md")

print("Done. Now run the app in Android Studio, then commit and push with GitHub Desktop.")
