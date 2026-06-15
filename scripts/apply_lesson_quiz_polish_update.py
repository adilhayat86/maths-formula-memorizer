from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_PATH = ROOT / "app" / "src" / "main" / "java" / "com" / "mominhayat" / "mathsformulamemorizer" / "MainActivity.java"
ICON_PATH = ROOT / "app" / "src" / "main" / "res" / "drawable" / "ic_launcher.xml"
ROUND_ICON_PATH = ROOT / "app" / "src" / "main" / "res" / "drawable" / "ic_launcher_round.xml"
STORE_PATH = ROOT / "STORE_LISTING_DRAFT.md"


def replace_method(source: str, signature: str, new_block: str) -> str:
    start = source.find(signature)
    if start == -1:
        print(f"Method not found or already changed: {signature}")
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

required = ["private LinearLayout cardTint", "private void addProgressBar", "private String topicIcon"]
missing = [r for r in required if r not in java]
if missing:
    raise SystemExit("Run product polish first. Missing helpers: " + ", ".join(missing))

# Lesson screen: make it feel like a small learning card, not a formula book.
java = replace_method(java, "    private void showLesson(Formula f)", '''    private void showLesson(Formula f) {
        LinearLayout root = baseScreen("Learn formula", selectedClass + " • " + f.topic);

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

        Button back = ghostButton("Back to area");
        back.setOnClickListener(v -> showTopic(f.topic));
        root.addView(back);
    }
''')

# Quiz screen: add progress context and clearer answer interaction.
java = replace_method(java, "    private void renderQuestion(QuizSession session)", '''    private void renderQuestion(QuizSession session) {
        Question q = session.questions.get(session.index);
        LinearLayout root = baseScreen("Quick quiz", session.formula.name);

        LinearLayout progressCard = cardTint(GREEN_LIGHT);
        progressCard.addView(sectionTitle("Question " + (session.index + 1) + " of " + session.questions.size()));
        addProgressBar(progressCard, session.questions.size(), session.index + 1);
        progressCard.addView(smallText("Score so far: " + session.correct + " correct"));
        root.addView(progressCard);

        LinearLayout card = card();
        card.addView(bigText(q.prompt));
        card.addView(smallText("Tap the best answer."));
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
    }
''')

# Feedback screen: stronger success/failure language and cleaner answer display.
java = replace_method(java, "    private void showFeedback(QuizSession session, Question q, boolean correct, String chosen)", '''    private void showFeedback(QuizSession session, Question q, boolean correct, String chosen) {
        LinearLayout root = baseScreen(correct ? "Correct ✓" : "Almost — review it", session.formula.name);
        LinearLayout card = cardTint(correct ? GREEN_LIGHT : Color.rgb(255, 251, 235));
        TextView title = bigText(correct ? "Good job!" : "Correct answer");
        title.setTextColor(correct ? GREEN_DARK : AMBER);
        card.addView(title);
        if (!correct) {
            card.addView(smallText("Your answer: " + chosen));
        }
        if (looksLikeFormula(q.correctAnswer)) {
            card.addView(formulaText(q.correctAnswer));
        } else {
            card.addView(bigText(q.correctAnswer));
        }
        card.addView(bodyText(q.explanation));
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
    }
''')

# Lesson completion: make mastery/review obvious.
java = replace_method(java, "    private void finishLesson(QuizSession session)", '''    private void finishLesson(QuizSession session) {
        Formula f = session.formula;
        Set<String> completed = getCompletedSet();
        completed.add(f.id);

        int totalAnswered = prefs.getInt("total_answered", 0) + session.questions.size();
        int totalCorrect = prefs.getInt("total_correct", 0) + session.correct;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("completed", completed);
        editor.putInt("total_answered", totalAnswered);
        editor.putInt("total_correct", totalCorrect);
        if (session.madeMistake) {
            editor.putInt("wrong_" + f.id, prefs.getInt("wrong_" + f.id, 0) + 1);
        } else {
            editor.putInt("wrong_" + f.id, Math.max(0, prefs.getInt("wrong_" + f.id, 0) - 1));
        }
        updateStreak(editor);
        editor.apply();

        boolean perfect = session.correct == session.questions.size();
        LinearLayout root = baseScreen(perfect ? "Mastered ✓" : "Added to review", f.name);
        LinearLayout card = cardTint(perfect ? GREEN_LIGHT : Color.rgb(255, 251, 235));
        card.addView(bigText(stars(session.correct, session.questions.size()) + "  Score: " + session.correct + " / " + session.questions.size()));
        card.addView(formulaText(f.formula));
        if (perfect) {
            card.addView(bodyText("Excellent. This formula is marked as mastered."));
        } else {
            card.addView(bodyText("Good practice. This formula will appear in mistake review until it becomes easy."));
        }
        card.addView(smallText("One formula at a time builds long-term memory."));
        root.addView(card);

        Button continueBtn = primaryButton("Next formula");
        continueBtn.setOnClickListener(v -> showLesson(nextFormula(availableFormulas(), getCompletedSet())));
        root.addView(continueBtn);

        if (!perfect) {
            Button review = secondaryButton("Review mistakes");
            review.setOnClickListener(v -> showWeakFormulas());
            root.addView(review);
        }

        Button home = ghostButton("Home");
        home.setOnClickListener(v -> showHome());
        root.addView(home);
    }
''')

# Weak formulas: quick first review and clearer empty state.
java = replace_method(java, "    private void showWeakFormulas()", '''    private void showWeakFormulas() {
        List<Formula> weak = new ArrayList<>();
        for (Formula f : availableFormulas()) {
            if (prefs.getInt("wrong_" + f.id, 0) > 0) weak.add(f);
        }
        Collections.sort(weak, (a, b) -> prefs.getInt("wrong_" + b.id, 0) - prefs.getInt("wrong_" + a.id, 0));

        LinearLayout root = baseScreen("Mistake review", "Practice formulas you forgot before.");
        if (weak.isEmpty()) {
            LinearLayout c = cardTint(GREEN_LIGHT);
            c.addView(bigText("No weak formulas yet ✓"));
            c.addView(bodyText("Mistakes will appear here automatically after quizzes."));
            c.addView(smallText("Keep practicing one formula per day."));
            root.addView(c);
        } else {
            LinearLayout top = cardTint(Color.rgb(255, 251, 235));
            top.addView(bigText("Review the hardest one first"));
            top.addView(smallText("Weak formulas are sorted by number of mistakes."));
            Button start = primaryButton("Start mistake review");
            start.setOnClickListener(v -> showLesson(weak.get(0)));
            top.addView(start);
            root.addView(top);

            for (Formula f : weak) {
                LinearLayout c = card();
                c.addView(bigText("⚠ " + f.name));
                c.addView(smallText(f.topic + " • Mistakes: " + prefs.getInt("wrong_" + f.id, 0)));
                c.addView(formulaText(f.formula));
                Button practice = secondaryButton("Practice again");
                practice.setOnClickListener(v -> showLesson(f));
                c.addView(practice);
                root.addView(c);
            }
        }
        Button back = ghostButton("Back to home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }
''')

# Add helper methods used by the improved screens.
helper_methods = '''
    private boolean looksLikeFormula(String s) {
        return s.contains("=") || s.contains("×") || s.contains("/") || s.contains("π") || s.contains("²") || s.contains("√");
    }

    private String stars(int correct, int total) {
        if (correct == total) return "★★★";
        if (correct >= Math.max(1, total - 1)) return "★★☆";
        return "★☆☆";
    }

'''

if "private boolean looksLikeFormula" not in java:
    marker = "    private LinearLayout cardTint(int color) {\n"
    if marker not in java:
        raise SystemExit("Could not find helper insertion point. Run product polish first.")
    java = java.replace(marker, helper_methods + marker)

if java != original_java:
    JAVA_PATH.write_text(java, encoding="utf-8", newline="\n")
    print("Updated MainActivity.java with lesson and quiz polish.")
else:
    print("MainActivity.java already looked updated.")

# Better placeholder launcher icons. These are still simple vectors; replace with a professional icon before final launch if desired.
ICON_PATH.write_text('''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#0F766E" android:pathData="M0,0h108v108h-108z"/>
    <path android:fillColor="#D1FAE5" android:pathData="M14,18h80c4,0 7,3 7,7v58c0,4 -3,7 -7,7H14c-4,0 -7,-3 -7,-7V25c0,-4 3,-7 7,-7z"/>
    <path android:fillColor="#FFFFFF" android:pathData="M23,27h62c4,0 7,3 7,7v44c0,4 -3,7 -7,7H23c-4,0 -7,-3 -7,-7V34c0,-4 3,-7 7,-7z"/>
    <path android:fillColor="#0F766E" android:pathData="M30,42h18v6H30zM56,42h22v6H56zM30,58h48v6H30zM30,73h29v6H30z"/>
    <path android:fillColor="#F59E0B" android:pathData="M74,69l4,6l8,-12l5,4l-12,18l-10,-12z"/>
    <path android:fillColor="#0F766E" android:pathData="M32,31l5,8h-4l-1,-2l-1,2h-4l5,-8zM52,31h4v10h-4zM59,31h4v10h-4z"/>
</vector>
''', encoding="utf-8", newline="\n")

ROUND_ICON_PATH.write_text('''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#0F766E" android:pathData="M54,54m-54,0a54,54 0,1 1,108 0a54,54 0,1 1,-108 0"/>
    <path android:fillColor="#D1FAE5" android:pathData="M19,23h70c4,0 7,3 7,7v50c0,4 -3,7 -7,7H19c-4,0 -7,-3 -7,-7V30c0,-4 3,-7 7,-7z"/>
    <path android:fillColor="#FFFFFF" android:pathData="M27,31h54c4,0 7,3 7,7v36c0,4 -3,7 -7,7H27c-4,0 -7,-3 -7,-7V38c0,-4 3,-7 7,-7z"/>
    <path android:fillColor="#0F766E" android:pathData="M34,45h14v6H34zM56,45h20v6H56zM34,59h42v6H34zM34,72h24v6H34z"/>
    <path android:fillColor="#F59E0B" android:pathData="M72,65l4,6l8,-12l5,4l-12,18l-10,-12z"/>
</vector>
''', encoding="utf-8", newline="\n")
print("Updated launcher icon vectors.")

if STORE_PATH.exists():
    store = STORE_PATH.read_text(encoding="utf-8")
    if "Duolingo" not in store and "mistake review" in store:
        print("Store listing already updated enough; no screenshot work done.")

print("Done. No screenshots were created.")
