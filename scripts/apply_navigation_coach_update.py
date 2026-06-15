from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_PATH = ROOT / "app" / "src" / "main" / "java" / "com" / "mominhayat" / "mathsformulamemorizer" / "MainActivity.java"
PRIVACY_PATH = ROOT / "PRIVACY_POLICY_DRAFT.md"
STORE_PATH = ROOT / "STORE_LISTING_DRAFT.md"


def replace_method(source: str, signature: str, new_block: str) -> str:
    start = source.find(signature)
    if start == -1:
        print(f"Method not found: {signature}")
        return source
    brace_start = source.find("{", start)
    if brace_start == -1:
        raise SystemExit(f"Could not find opening brace for {signature}")
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
    return source[:start] + new_block.rstrip() + "\n" + source[end:]


java = JAVA_PATH.read_text(encoding="utf-8").replace("\r\n", "\n")
original_java = java

# This update is designed to run after the product polish update.
required_helpers = ["private LinearLayout cardTint", "private void addProgressBar", "private int currentAccuracy", "private String topicIcon"]
missing_helpers = [h for h in required_helpers if h not in java]
if missing_helpers:
    raise SystemExit("Run scripts/apply_product_polish_update.py first. Missing: " + ", ".join(missing_helpers))

# Imports needed for the student name screen.
if "import android.widget.EditText;" not in java:
    java = java.replace("import android.widget.Button;\n", "import android.widget.Button;\nimport android.widget.EditText;\n")
if "import android.text.InputType;" not in java:
    java = java.replace("import android.os.Bundle;\n", "import android.os.Bundle;\nimport android.text.InputType;\n")

# Constants for firm coaching if product polish has not already added them.
if "private static final int AMBER" not in java:
    java = java.replace(
        "    private static final int RED = Color.rgb(185, 28, 28);\n",
        "    private static final int RED = Color.rgb(185, 28, 28);\n    private static final int AMBER = Color.rgb(245, 158, 11);\n"
    )

# On launch: class first, then student name, then home.
java = replace_method(java, "    protected void onCreate(Bundle savedInstanceState)", '''    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("maths_formula_progress", MODE_PRIVATE);
        selectedClass = prefs.getString("selected_class", null);
        if (selectedClass == null) {
            showClassSelect();
        } else if (isStudentNameMissing()) {
            showNameSelect();
        } else {
            showHome();
        }
    }
''')

# Class selection now moves to name selection if needed.
java = replace_method(java, "    private void showClassSelect()", '''    private void showClassSelect() {
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
            if (isStudentNameMissing()) {
                showNameSelect();
            } else {
                showHome();
            }
        });
        root.addView(continueBtn);
    }

    private void showNameSelect() {
        LinearLayout root = baseScreen("Student name", "The app will use the name only on this phone for friendly coaching.");

        LinearLayout c = cardTint(GREEN_LIGHT);
        c.addView(sectionTitle("What should I call the student?"));
        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setHint("Example: Ali");
        nameInput.setText(isStudentNameMissing() ? "" : getStudentName());
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        nameInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        c.addView(nameInput);
        c.addView(smallText("No login. No internet. Name stays saved only inside this app."));
        root.addView(c);

        Button save = primaryButton("Save and start");
        save.setOnClickListener(v -> {
            String clean = cleanStudentName(nameInput.getText().toString());
            prefs.edit().putString("student_name", clean).apply();
            showHome();
        });
        root.addView(save);

        Button skip = ghostButton("Skip for now");
        skip.setOnClickListener(v -> {
            prefs.edit().putString("student_name", "Student").apply();
            showHome();
        });
        root.addView(skip);
    }
''')

# Home screen: greet by name and expose change-name action.
java = replace_method(java, "    private void showHome()", '''    private void showHome() {
        if (selectedClass == null) {
            showClassSelect();
            return;
        }
        if (isStudentNameMissing()) {
            showNameSelect();
            return;
        }

        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;
        int accuracy = currentAccuracy();
        int weakCount = countWeakFormulas(available);
        int percent = percent(completedCount, available.size());
        String name = getStudentName();

        LinearLayout root = baseScreen("Hi, " + name, "Maths Formula Memorizer • Offline • No ads • No login");

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
        classCard.addView(smallText("Student: " + name + " • Progress stays saved on this phone."));
        Button changeName = ghostButton("Change student name");
        changeName.setOnClickListener(v -> showNameSelect());
        classCard.addView(changeName);
        root.addView(classCard);

        LinearLayout hero = cardTint(GREEN_LIGHT);
        hero.addView(sectionTitle("Today’s goal"));
        hero.addView(bigText(name + ", learn 1 formula in 60 seconds"));
        hero.addView(smallText("Short lessons, instant feedback, hints, and mistake review."));
        LinearLayout statsRow = row();
        statsRow.addView(statBox("Done", completedCount + "/" + available.size()), rowWeight());
        statsRow.addView(statBox("Accuracy", accuracy + "%"), rowWeight());
        statsRow.addView(statBox("Weak", String.valueOf(weakCount)), rowWeight());
        hero.addView(statsRow);
        addProgressBar(hero, available.size(), completedCount);
        hero.addView(smallText(percent + "% of " + selectedClass + " formulas completed • Streak: " + prefs.getInt("streak", 0) + " day(s)"));
        root.addView(hero);

        Button cont = primaryButton(completedCount == 0 ? "Start first lesson" : "Continue next formula");
        cont.setOnClickListener(v -> showLesson(nextFormula(available, completed)));
        root.addView(cont);

        LinearLayout actionRow = row();
        Button weak = secondaryButton(weakCount == 0 ? "Mistake review" : "Review " + weakCount + " weak");
        weak.setOnClickListener(v -> showWeakFormulas());
        Button progressBtn = secondaryButton("Progress report");
        progressBtn.setOnClickListener(v -> showProgress());
        actionRow.addView(weak, rowWeight());
        actionRow.addView(progressBtn, rowWeight());
        root.addView(actionRow);

        TextView unitsTitle = sectionTitle("Choose area of study");
        root.addView(unitsTitle);

        LinkedHashMap<String, List<Formula>> byTopic = groupByTopic(available);
        for (String topic : byTopic.keySet()) {
            List<Formula> topicFormulas = byTopic.get(topic);
            int done = topicCompleted(topicFormulas, completed);
            int topicWeak = countWeakFormulas(topicFormulas);

            LinearLayout topicCard = card();
            topicCard.addView(bigText(topicIcon(topic) + " " + topic));
            topicCard.addView(smallText(done + " / " + topicFormulas.size() + " completed" + (topicWeak > 0 ? " • " + topicWeak + " weak" : "")));
            addProgressBar(topicCard, topicFormulas.size(), done);
            Button open = done == topicFormulas.size() ? secondaryButton("Review area") : primaryButton("Start area");
            open.setOnClickListener(v -> showTopic(topic));
            topicCard.addView(open);
            root.addView(topicCard);
        }
    }
''')

# Lesson screen: clearer navigation and name-based coaching.
java = replace_method(java, "    private void showLesson(Formula f)", '''    private void showLesson(Formula f) {
        LinearLayout root = baseScreen("Learn formula", getStudentName() + " • " + selectedClass + " • " + f.topic);

        LinearLayout hero = cardTint(GREEN_LIGHT);
        hero.addView(sectionTitle(topicIcon(f.topic) + " " + f.topic));
        hero.addView(bigText(f.name));
        hero.addView(formulaText(f.formula));
        hero.addView(smallText("Look once, say it in your mind, then test yourself."));
        root.addView(hero);

        LinearLayout meaning = card();
        meaning.addView(sectionTitle("What it means"));
        meaning.addView(bodyText(f.explanation));
        root.addView(meaning);

        LinearLayout example = card();
        example.addView(sectionTitle("Quick example"));
        example.addView(bodyText(f.example));
        root.addView(example);

        Button quiz = primaryButton("I remember it — start quiz");
        quiz.setOnClickListener(v -> showQuiz(f));
        root.addView(quiz);

        LinearLayout nav = row();
        Button area = secondaryButton("Back to area");
        area.setOnClickListener(v -> showTopic(f.topic));
        Button home = secondaryButton("Home");
        home.setOnClickListener(v -> showHome());
        nav.addView(area, rowWeight());
        nav.addView(home, rowWeight());
        root.addView(nav);
    }
''')

# Quiz screen: hint and quit options.
java = replace_method(java, "    private void renderQuestion(QuizSession session)", '''    private void renderQuestion(QuizSession session) {
        Question q = session.questions.get(session.index);
        LinearLayout root = baseScreen("Quick quiz", getStudentName() + " • " + session.formula.name);

        LinearLayout progressCard = cardTint(GREEN_LIGHT);
        progressCard.addView(sectionTitle("Question " + (session.index + 1) + " of " + session.questions.size()));
        addProgressBar(progressCard, session.questions.size(), session.index + 1);
        progressCard.addView(smallText("Score so far: " + session.correct + " correct"));
        root.addView(progressCard);

        LinearLayout card = card();
        card.addView(bigText(q.prompt));
        card.addView(smallText("Tap the best answer. You can use a hint or quit without saving progress."));
        Button hint = secondaryButton("Show hint");
        hint.setOnClickListener(v -> showHint(session, q));
        card.addView(hint);
        for (String option : q.options) {
            Button b = secondaryButton(option);
            b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            b.setOnClickListener(v -> {
                boolean correct = option.equals(q.correctAnswer);
                if (correct) {
                    session.correct++;
                } else {
                    session.madeMistake = true;
                }
                showFeedback(session, q, correct, option);
            });
            card.addView(b);
        }
        root.addView(card);

        Button quit = ghostButton("Quit quiz");
        quit.setTextColor(RED);
        quit.setOnClickListener(v -> showQuitQuizConfirm(session));
        root.addView(quit);
    }
''')

# Feedback screen: comments on correct answers and firm coaching on wrong answers.
java = replace_method(java, "    private void showFeedback(QuizSession session, Question q, boolean correct, String chosen)", '''    private void showFeedback(QuizSession session, Question q, boolean correct, String chosen) {
        LinearLayout root = baseScreen(correct ? "Correct ✓" : "Focus — not correct", session.formula.name);
        LinearLayout card = cardTint(correct ? GREEN_LIGHT : Color.rgb(255, 251, 235));
        TextView title = bigText(correct ? praiseMessage() : firmCoachMessage());
        title.setTextColor(correct ? GREEN_DARK : AMBER);
        card.addView(title);
        if (!correct) {
            card.addView(smallText("Your answer: " + chosen));
        }
        card.addView(sectionTitle(correct ? "Why it is right" : "Correct answer"));
        if (looksLikeFormula(q.correctAnswer)) {
            card.addView(formulaText(q.correctAnswer));
        } else {
            card.addView(bigText(q.correctAnswer));
        }
        card.addView(bodyText(q.explanation));
        if (!correct) {
            card.addView(smallText("Read slowly, " + getStudentName() + ". You are not allowed to guess carelessly."));
        }
        root.addView(card);

        Button next = primaryButton(session.index == session.questions.size() - 1 ? "Finish lesson" : "Next question");
        next.setOnClickListener(v -> {
            session.index++;
            if (session.index >= session.questions.size()) {
                finishLesson(session);
            } else {
                renderQuestion(session);
            }
        });
        root.addView(next);

        Button quit = ghostButton("Quit quiz");
        quit.setTextColor(RED);
        quit.setOnClickListener(v -> showQuitQuizConfirm(session));
        root.addView(quit);
    }
''')

# Progress screen: include name and change-name action.
java = replace_method(java, "    private void showProgress()", '''    private void showProgress() {
        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;
        int accuracy = currentAccuracy();
        int weakCount = countWeakFormulas(available);

        LinearLayout root = baseScreen("Progress report", getStudentName() + " • " + selectedClass);
        LinearLayout c = cardTint(GREEN_LIGHT);
        c.addView(bigText("Overall progress: " + percent(completedCount, available.size()) + "%"));
        addProgressBar(c, available.size(), completedCount);
        LinearLayout stats = row();
        stats.addView(statBox("Lessons", completedCount + "/" + available.size()), rowWeight());
        stats.addView(statBox("Accuracy", accuracy + "%"), rowWeight());
        stats.addView(statBox("Streak", prefs.getInt("streak", 0) + "d"), rowWeight());
        c.addView(stats);
        c.addView(smallText("Student name and progress are saved only on this phone. No account and no internet needed."));
        root.addView(c);

        root.addView(sectionTitle("Area progress"));
        LinkedHashMap<String, List<Formula>> byTopic = groupByTopic(available);
        for (String topic : byTopic.keySet()) {
            List<Formula> topicFormulas = byTopic.get(topic);
            int done = topicCompleted(topicFormulas, completed);
            LinearLayout area = card();
            area.addView(bigText(topicIcon(topic) + " " + topic));
            area.addView(smallText(done + " / " + topicFormulas.size() + " completed"));
            addProgressBar(area, topicFormulas.size(), done);
            root.addView(area);
        }

        if (weakCount > 0) {
            Button weak = primaryButton("Review " + weakCount + " weak formula(s)");
            weak.setOnClickListener(v -> showWeakFormulas());
            root.addView(weak);
        }

        Button changeName = secondaryButton("Change student name");
        changeName.setOnClickListener(v -> showNameSelect());
        root.addView(changeName);

        Button reset = ghostButton("Reset progress for this phone");
        reset.setTextColor(RED);
        reset.setOnClickListener(v -> showResetConfirm());
        root.addView(reset);

        Button back = primaryButton("Back to home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }
''')

# Helpers for personalization, hint, and quit confirmation.
helper_block = '''
    private boolean isStudentNameMissing() {
        String stored = prefs.getString("student_name", "");
        return stored == null || stored.trim().isEmpty();
    }

    private String getStudentName() {
        String stored = prefs.getString("student_name", "Student");
        return cleanStudentName(stored);
    }

    private String cleanStudentName(String raw) {
        if (raw == null) return "Student";
        String clean = raw.trim().replaceAll("\\s+", " ");
        if (clean.isEmpty()) return "Student";
        if (clean.length() > 18) clean = clean.substring(0, 18).trim();
        return clean;
    }

    private void showHint(QuizSession session, Question q) {
        LinearLayout root = baseScreen("Hint", getStudentName() + " • " + session.formula.name);
        LinearLayout c = cardTint(Color.rgb(239, 246, 255));
        c.addView(sectionTitle("Use this clue"));
        c.addView(bodyText(hintText(session.formula, q)));
        c.addView(smallText("Hints help memory. Try answering without looking again."));
        root.addView(c);

        Button back = primaryButton("Back to question");
        back.setOnClickListener(v -> renderQuestion(session));
        root.addView(back);

        Button quit = ghostButton("Quit quiz");
        quit.setTextColor(RED);
        quit.setOnClickListener(v -> showQuitQuizConfirm(session));
        root.addView(quit);
    }

    private String hintText(Formula f, Question q) {
        if (q.correctAnswer.equals(f.formula)) {
            return "Think of the formula for " + f.name + ". It belongs to " + f.topic + ". Formula shape: " + softFormulaHint(f.formula);
        }
        if (q.correctAnswer.equals(f.name)) {
            return "Look at the formula symbols and ask: what quantity does this calculate? Topic: " + f.topic + ".";
        }
        return "Remember the meaning of the symbols in " + f.formula + ". The lesson example can guide you.";
    }

    private String softFormulaHint(String formula) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < formula.length(); i++) {
            char ch = formula.charAt(i);
            if (ch == '=' || ch == '+' || ch == '-' || ch == '×' || ch == '/' || ch == 'π' || ch == '²' || ch == '³' || ch == '√' || ch == '(' || ch == ')') {
                sb.append(ch);
            } else if (Character.isWhitespace(ch)) {
                sb.append(' ');
            } else {
                sb.append('•');
            }
        }
        return sb.toString();
    }

    private void showQuitQuizConfirm(QuizSession session) {
        LinearLayout root = baseScreen("Quit quiz?", "Your current quiz answers will not be saved.");
        LinearLayout c = cardTint(Color.rgb(255, 251, 235));
        c.addView(bigText("Leave this quiz, " + getStudentName() + "?"));
        c.addView(bodyText("The lesson will not be completed. You can restart it anytime from the same area."));
        root.addView(c);

        Button continueQuiz = primaryButton("Continue quiz");
        continueQuiz.setOnClickListener(v -> renderQuestion(session));
        root.addView(continueQuiz);

        Button leave = ghostButton("Leave quiz and go home");
        leave.setTextColor(RED);
        leave.setOnClickListener(v -> showHome());
        root.addView(leave);
    }

    private String praiseMessage() {
        String name = getStudentName();
        String[] messages = {
                "Excellent, " + name + "!",
                "Correct — sharp memory, " + name + "!",
                "Good work, " + name + ". Keep going.",
                "Nice! That formula is sticking.",
                "Right answer. Fast and focused."
        };
        return messages[random.nextInt(messages.length)];
    }

    private String firmCoachMessage() {
        String name = getStudentName();
        String[] messages = {
                "Not good enough yet, " + name + " — focus.",
                "Careful, " + name + ". Do not guess.",
                "Slow down, " + name + ". Read the formula again.",
                "Wrong answer, but fix it now.",
                "Focus. You can do better than guessing."
        };
        return messages[random.nextInt(messages.length)];
    }

    private boolean looksLikeFormula(String s) {
        return s.contains("=") || s.contains("×") || s.contains("/") || s.contains("π") || s.contains("²") || s.contains("√");
    }

    private String stars(int correct, int total) {
        if (correct == total) return "★★★";
        if (correct >= Math.max(1, total - 1)) return "★★☆";
        return "★☆☆";
    }

'''

if "private void showQuitQuizConfirm" not in java:
    marker = "    private LinearLayout cardTint(int color) {\n"
    if marker not in java:
        raise SystemExit("Could not find helper insertion point. Run product polish first.")
    java = java.replace(marker, helper_block + marker)

JAVA_PATH.write_text(java, encoding="utf-8", newline="\n")

# Update privacy draft because the app now asks for a local-only student name.
if PRIVACY_PATH.exists():
    privacy = PRIVACY_PATH.read_text(encoding="utf-8").replace("\r\n", "\n")
    if "student name" not in privacy.lower():
        privacy += """

## Student name

The app may ask for a student name so it can show friendly coaching messages inside the app. The name is saved only on the user's device. It is not sent to the developer, not uploaded to a server, and not shared with anyone.
"""
        PRIVACY_PATH.write_text(privacy, encoding="utf-8", newline="\n")

# Update store draft to mention hints and local name gently.
if STORE_PATH.exists():
    store = STORE_PATH.read_text(encoding="utf-8").replace("\r\n", "\n")
    if "Hints during quizzes" not in store:
        store = store.replace("- 3-question quizzes\n", "- 3-question quizzes\n- Hints during quizzes\n- Friendly coaching using the student name saved on device\n")
        STORE_PATH.write_text(store, encoding="utf-8", newline="\n")

print("Updated navigation, student name, hints, quiz abandon option, and coaching feedback.")
print("No screenshots were created.")
