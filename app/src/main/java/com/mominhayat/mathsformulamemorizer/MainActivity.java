package com.mominhayat.mathsformulamemorizer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(15, 118, 110);
    private static final int GREEN_DARK = Color.rgb(11, 95, 89);
    private static final int BG = Color.rgb(247, 250, 249);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final int MUTED = Color.rgb(107, 114, 128);
    private static final int RED = Color.rgb(185, 28, 28);
    private static final String[] CLASS_OPTIONS = {"Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "General Practice"};

    private SharedPreferences prefs;
    private final List<Formula> formulas = FormulaRepository.all();
    private final Random random = new Random();
    private String selectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("maths_formula_progress", MODE_PRIVATE);
        selectedClass = prefs.getString("selected_class", null);
        if (selectedClass == null) {
            showClassSelect();
        } else {
            showHome();
        }
    }

    @Override
    public void onBackPressed() {
        showHome();
    }

    private void showClassSelect() {
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

    private void showHome() {
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

    private void showTopic(String topic) {
        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        LinearLayout root = baseScreen(topic, "Tap a formula lesson. Each lesson takes about 1 minute.");

        for (Formula f : available) {
            if (!f.topic.equals(topic)) continue;
            LinearLayout item = card();
            String mark = completed.contains(f.id) ? "✓ " : "○ ";
            item.addView(bigText(mark + f.name));
            item.addView(formulaText(f.formula));
            Button open = completed.contains(f.id) ? secondaryButton("Review again") : primaryButton("Start lesson");
            open.setOnClickListener(v -> showLesson(f));
            item.addView(open);
            root.addView(item);
        }

        Button back = ghostButton("Back to home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showLesson(Formula f) {
        LinearLayout root = baseScreen(f.name, f.topic);

        LinearLayout card = card();
        card.addView(sectionTitle("Remember this formula"));
        card.addView(formulaText(f.formula));
        card.addView(bodyText(f.explanation));
        card.addView(sectionTitle("Example"));
        card.addView(bodyText(f.example));
        root.addView(card);

        Button quiz = primaryButton("Start 3-question quiz");
        quiz.setOnClickListener(v -> showQuiz(f));
        root.addView(quiz);

        Button back = ghostButton("Back");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showQuiz(Formula f) {
        List<Question> questions = buildQuiz(f);
        QuizSession session = new QuizSession(f, questions);
        renderQuestion(session);
    }

    private void renderQuestion(QuizSession session) {
        Question q = session.questions.get(session.index);
        LinearLayout root = baseScreen("Question " + (session.index + 1) + " of " + session.questions.size(), session.formula.name);

        LinearLayout card = card();
        card.addView(bigText(q.prompt));
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

    private void showFeedback(QuizSession session, Question q, boolean correct, String chosen) {
        LinearLayout root = baseScreen(correct ? "Correct ✓" : "Not yet", session.formula.name);
        LinearLayout card = card();
        TextView title = bigText(correct ? "Good job!" : "Correct answer: " + q.correctAnswer);
        title.setTextColor(correct ? GREEN_DARK : RED);
        card.addView(title);
        if (!correct) {
            card.addView(smallText("Your answer: " + chosen));
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

    private void finishLesson(QuizSession session) {
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

        LinearLayout root = baseScreen("Lesson complete", f.name);
        LinearLayout card = card();
        card.addView(bigText("Score: " + session.correct + " / " + session.questions.size()));
        if (session.correct == session.questions.size()) {
            card.addView(bodyText("Excellent. This formula will appear less often in weak practice."));
        } else {
            card.addView(bodyText("Good practice. This formula has been added to weak formulas so you can repeat it."));
        }
        root.addView(card);

        Button continueBtn = primaryButton("Continue practice");
        continueBtn.setOnClickListener(v -> showLesson(nextFormula(availableFormulas(), getCompletedSet())));
        root.addView(continueBtn);

        Button home = ghostButton("Home");
        home.setOnClickListener(v -> showHome());
        root.addView(home);
    }

    private void showWeakFormulas() {
        List<Formula> weak = new ArrayList<>();
        for (Formula f : availableFormulas()) {
            if (prefs.getInt("wrong_" + f.id, 0) > 0) weak.add(f);
        }
        Collections.sort(weak, (a, b) -> prefs.getInt("wrong_" + b.id, 0) - prefs.getInt("wrong_" + a.id, 0));

        LinearLayout root = baseScreen("Weak formulas", "Practice formulas you forgot before.");
        if (weak.isEmpty()) {
            LinearLayout c = card();
            c.addView(bigText("No weak formulas yet"));
            c.addView(bodyText("Mistakes will appear here automatically after quizzes."));
            root.addView(c);
        } else {
            for (Formula f : weak) {
                LinearLayout c = card();
                c.addView(bigText(f.name));
                c.addView(formulaText(f.formula));
                c.addView(smallText("Mistakes: " + prefs.getInt("wrong_" + f.id, 0)));
                Button practice = primaryButton("Practice again");
                practice.setOnClickListener(v -> showLesson(f));
                c.addView(practice);
                root.addView(c);
            }
        }
        Button back = ghostButton("Back to home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showProgress() {
        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int totalAnswered = prefs.getInt("total_answered", 0);
        int totalCorrect = prefs.getInt("total_correct", 0);
        int accuracy = totalAnswered == 0 ? 0 : Math.round((100f * totalCorrect) / totalAnswered);

        LinearLayout root = baseScreen("Progress", selectedClass);
        LinearLayout c = card();
        c.addView(bigText("Completed lessons: " + completed.size() + " / " + available.size()));
        c.addView(bigText("Accuracy: " + accuracy + "%"));
        c.addView(bigText("Streak: " + prefs.getInt("streak", 0) + " day(s)"));
        c.addView(smallText("Progress is saved only on this phone. No account and no internet needed."));
        root.addView(c);

        Button reset = ghostButton("Reset progress for this phone");
        reset.setTextColor(RED);
        reset.setOnClickListener(v -> showResetConfirm());
        root.addView(reset);

        Button back = primaryButton("Back to home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showResetConfirm() {
        LinearLayout root = baseScreen("Reset progress?", "This only clears progress on this phone.");
        Button yes = primaryButton("Yes, reset progress");
        yes.setOnClickListener(v -> {
            String keepClass = selectedClass;
            prefs.edit().clear().putString("selected_class", keepClass).apply();
            showHome();
        });
        Button no = ghostButton("Cancel");
        no.setOnClickListener(v -> showProgress());
        root.addView(yes);
        root.addView(no);
    }

    private List<Question> buildQuiz(Formula f) {
        List<Question> list = new ArrayList<>();

        list.add(new Question(
                "Which formula is for " + f.name + "?",
                optionsWithCorrect(f.formula, collectFormulaStrings(f.id)),
                f.formula,
                f.explanation
        ));

        list.add(new Question(
                f.meaningQuestion,
                shuffled(f.meaningOptions),
                f.meaningAnswer,
                "Know what each symbol means, not only the formula."
        ));

        list.add(new Question(
                "What does this formula calculate?\n\n" + f.formula,
                optionsWithCorrect(f.name, collectFormulaNames(f.id)),
                f.name,
                "Formula name: " + f.name
        ));

        return list;
    }

    private List<String> collectFormulaStrings(String excludeId) {
        List<String> values = new ArrayList<>();
        for (Formula f : availableFormulas()) if (!f.id.equals(excludeId)) values.add(f.formula);
        return values;
    }

    private List<String> collectFormulaNames(String excludeId) {
        List<String> values = new ArrayList<>();
        for (Formula f : availableFormulas()) if (!f.id.equals(excludeId)) values.add(f.name);
        return values;
    }

    private List<String> optionsWithCorrect(String correct, List<String> others) {
        List<String> pool = new ArrayList<>(new HashSet<>(others));
        Collections.shuffle(pool, random);
        List<String> options = new ArrayList<>();
        options.add(correct);
        for (String s : pool) {
            if (!s.equals(correct) && options.size() < 4) options.add(s);
        }
        return shuffled(options);
    }

    private List<String> shuffled(String[] arr) {
        return shuffled(Arrays.asList(arr));
    }

    private List<String> shuffled(List<String> arr) {
        List<String> list = new ArrayList<>(arr);
        Collections.shuffle(list, random);
        return list;
    }

    private Formula nextFormula(List<Formula> available, Set<String> completed) {
        for (Formula f : available) if (!completed.contains(f.id)) return f;
        return available.get(random.nextInt(available.size()));
    }

    private LinkedHashMap<String, List<Formula>> groupByTopic(List<Formula> list) {
        LinkedHashMap<String, List<Formula>> map = new LinkedHashMap<>();
        for (Formula f : list) {
            if (!map.containsKey(f.topic)) map.put(f.topic, new ArrayList<>());
            map.get(f.topic).add(f);
        }
        return map;
    }

    private List<Formula> availableFormulas() {
        List<Formula> result = new ArrayList<>();
        int maxClass = selectedClassNumber();
        for (Formula f : formulas) {
            if (maxClass == 0 || f.classNumber <= maxClass) result.add(f);
        }
        Collections.sort(result, Comparator.comparingInt((Formula f) -> f.classNumber).thenComparing(f -> f.topic).thenComparing(f -> f.name));
        return result;
    }

    private Spinner classSpinner(String currentClass) {
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

    private int selectedClassNumber() {
        if (selectedClass == null || selectedClass.equals("General Practice")) return 0;
        try {
            return Integer.parseInt(selectedClass.replace("Class", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private Set<String> getCompletedSet() {
        Set<String> stored = prefs.getStringSet("completed", new HashSet<>());
        return new HashSet<>(stored);
    }

    private void updateStreak(SharedPreferences.Editor editor) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String last = prefs.getString("last_practice_date", "");
        if (today.equals(last)) return;

        int streak = prefs.getInt("streak", 0);
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date lastDate = fmt.parse(last);
            Date todayDate = fmt.parse(today);
            if (lastDate != null && todayDate != null) {
                long diffDays = (todayDate.getTime() - lastDate.getTime()) / (24L * 60L * 60L * 1000L);
                streak = diffDays == 1 ? streak + 1 : 1;
            } else {
                streak = 1;
            }
        } catch (Exception e) {
            streak = 1;
        }
        editor.putString("last_practice_date", today);
        editor.putInt("streak", streak);
    }

    private LinearLayout baseScreen(String title, String subtitle) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(28));
        scroll.addView(root);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(TEXT);
        titleView.setTextSize(27);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setPadding(0, dp(8), 0, dp(4));
        root.addView(titleView);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = smallText(subtitle);
            sub.setPadding(0, 0, 0, dp(12));
            root.addView(sub);
        }
        setContentView(scroll);
        return root;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackgroundColor(CARD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(10));
        c.setLayoutParams(lp);
        return c;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        return r;
    }

    private LinearLayout.LayoutParams rowWeight() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        return lp;
    }

    private TextView bigText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(18);
        t.setTextColor(TEXT);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(4), 0, dp(6));
        return t;
    }

    private TextView bodyText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(16);
        t.setTextColor(TEXT);
        t.setLineSpacing(dp(2), 1.0f);
        t.setPadding(0, dp(4), 0, dp(8));
        return t;
    }

    private TextView smallText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(14);
        t.setTextColor(MUTED);
        t.setPadding(0, dp(3), 0, dp(5));
        return t;
    }

    private TextView sectionTitle(String s) {
        TextView t = bigText(s);
        t.setTextSize(16);
        t.setTextColor(GREEN_DARK);
        return t;
    }

    private TextView formulaText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(28);
        t.setTextColor(GREEN_DARK);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(14), dp(8), dp(14));
        return t;
    }

    private Button primaryButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackgroundColor(GREEN);
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(7), 0, dp(7));
        b.setLayoutParams(lp);
        return b;
    }

    private Button secondaryButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.rgb(236, 253, 245));
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private Button ghostButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(GREEN_DARK);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }

    private static class Formula {
        final String id;
        final int classNumber;
        final String topic;
        final String name;
        final String formula;
        final String explanation;
        final String example;
        final String meaningQuestion;
        final String[] meaningOptions;
        final String meaningAnswer;

        Formula(String id, int classNumber, String topic, String name, String formula, String explanation, String example, String meaningQuestion, String[] meaningOptions, String meaningAnswer) {
            this.id = id;
            this.classNumber = classNumber;
            this.topic = topic;
            this.name = name;
            this.formula = formula;
            this.explanation = explanation;
            this.example = example;
            this.meaningQuestion = meaningQuestion;
            this.meaningOptions = meaningOptions;
            this.meaningAnswer = meaningAnswer;
        }
    }

    private static class Question {
        final String prompt;
        final List<String> options;
        final String correctAnswer;
        final String explanation;

        Question(String prompt, List<String> options, String correctAnswer, String explanation) {
            this.prompt = prompt;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }
    }

    private static class QuizSession {
        final Formula formula;
        final List<Question> questions;
        int index = 0;
        int correct = 0;
        boolean madeMistake = false;

        QuizSession(Formula formula, List<Question> questions) {
            this.formula = formula;
            this.questions = questions;
        }
    }

    private static class FormulaRepository {
        static List<Formula> all() {
            List<Formula> f = new ArrayList<>();

            // Class 6 basics
            f.add(new Formula("c6_rect_area", 6, "Area & Perimeter", "Area of Rectangle", "A = l × w", "Area of a rectangle is length multiplied by width.", "If l = 8 and w = 3, A = 8 × 3 = 24.", "In A = l × w, what does w mean?", new String[]{"Width", "Weight", "Whole number", "Wrong value"}, "Width"));
            f.add(new Formula("c6_square_area", 6, "Area & Perimeter", "Area of Square", "A = s²", "A square has all sides equal, so area is side multiplied by side.", "If s = 5, A = 5² = 25.", "In A = s², what does s mean?", new String[]{"Side", "Sum", "Slope", "Speed"}, "Side"));
            f.add(new Formula("c6_rect_perimeter", 6, "Area & Perimeter", "Perimeter of Rectangle", "P = 2(l + w)", "Perimeter is the distance around the rectangle.", "If l = 7 and w = 2, P = 2(7 + 2) = 18.", "In P = 2(l + w), what does P mean?", new String[]{"Perimeter", "Percentage", "Product", "Point"}, "Perimeter"));
            f.add(new Formula("c6_square_perimeter", 6, "Area & Perimeter", "Perimeter of Square", "P = 4s", "A square has four equal sides.", "If s = 6, P = 4 × 6 = 24.", "In P = 4s, what does 4 represent?", new String[]{"Four sides", "Four angles only", "Four units", "Four answers"}, "Four sides"));
            f.add(new Formula("c6_triangle_area", 6, "Area & Perimeter", "Area of Triangle", "A = 1/2 × b × h", "Area of a triangle is half of base times height.", "If b = 10 and h = 4, A = 1/2 × 10 × 4 = 20.", "In A = 1/2 × b × h, what does h mean?", new String[]{"Height", "Half", "Hypotenuse", "Horizontal"}, "Height"));
            f.add(new Formula("c6_average", 6, "Number Skills", "Average", "Average = Sum / Count", "Average tells the central value of a group of numbers.", "For 3, 5, 7: Average = 15 / 3 = 5.", "In Average = Sum / Count, what does Count mean?", new String[]{"Number of values", "Total answer", "Largest value", "Smallest value"}, "Number of values"));
            f.add(new Formula("c6_percentage", 6, "Number Skills", "Percentage", "Percent = Part/Whole × 100", "Percentage means out of 100.", "If 20 out of 50 are correct, Percent = 20/50 × 100 = 40%.", "What does percent mean?", new String[]{"Out of 100", "Out of 10", "Only a fraction", "A negative number"}, "Out of 100"));
            f.add(new Formula("c6_speed", 6, "Number Skills", "Speed", "Speed = Distance / Time", "Speed tells how fast something moves.", "If distance = 100 km and time = 2 h, speed = 50 km/h.", "In Speed = Distance / Time, what does Time mean?", new String[]{"How long it takes", "How far it goes", "How heavy it is", "How many angles"}, "How long it takes"));
            f.add(new Formula("c6_unit_price", 6, "Number Skills", "Unit Price", "Unit Price = Total Price / Quantity", "Unit price tells the price of one item.", "If 5 pens cost 100, unit price = 100 / 5 = 20.", "What does quantity mean here?", new String[]{"Number of items", "Price of one item", "Total money only", "Discount"}, "Number of items"));

            // Class 7
            f.add(new Formula("c7_circle_circumference", 7, "Circle", "Circumference of Circle", "C = 2πr", "Circumference is the distance around a circle.", "If r = 7, C = 2 × π × 7 = 14π.", "In C = 2πr, what does r mean?", new String[]{"Radius", "Rectangle", "Rate", "Ratio"}, "Radius"));
            f.add(new Formula("c7_circle_area", 7, "Circle", "Area of Circle", "A = πr²", "Area of a circle is pi times radius squared.", "If r = 3, A = π × 3² = 9π.", "In A = πr², what does r mean?", new String[]{"Radius", "Diameter", "Area", "Length"}, "Radius"));
            f.add(new Formula("c7_diameter", 7, "Circle", "Diameter", "d = 2r", "Diameter is twice the radius.", "If r = 4, d = 2 × 4 = 8.", "What is diameter?", new String[]{"Twice the radius", "Half the radius", "Area of circle", "Volume"}, "Twice the radius"));
            f.add(new Formula("c7_profit", 7, "Profit & Loss", "Profit", "Profit = Selling Price - Cost Price", "Profit is earned when selling price is more than cost price.", "If SP = 120 and CP = 100, Profit = 20.", "What does SP mean?", new String[]{"Selling Price", "Simple Product", "Square Perimeter", "Small Price"}, "Selling Price"));
            f.add(new Formula("c7_loss", 7, "Profit & Loss", "Loss", "Loss = Cost Price - Selling Price", "Loss happens when selling price is less than cost price.", "If CP = 100 and SP = 80, Loss = 20.", "What does CP mean?", new String[]{"Cost Price", "Circle Point", "Current Price only", "Common Part"}, "Cost Price"));
            f.add(new Formula("c7_profit_percent", 7, "Profit & Loss", "Profit Percentage", "Profit% = Profit/CP × 100", "Profit percentage compares profit with cost price.", "If profit = 20 and CP = 100, Profit% = 20%.", "In Profit% = Profit/CP × 100, CP means?", new String[]{"Cost Price", "Current Point", "Circle Perimeter", "Class Percentage"}, "Cost Price"));
            f.add(new Formula("c7_discount", 7, "Profit & Loss", "Discount", "Discount = Marked Price - Selling Price", "Discount is the reduction from marked price.", "If MP = 500 and SP = 450, Discount = 50.", "What does MP mean?", new String[]{"Marked Price", "Market Profit", "Main Percentage", "Maths Point"}, "Marked Price"));
            f.add(new Formula("c7_simple_interest", 7, "Finance", "Simple Interest", "SI = P × R × T / 100", "Simple interest depends on principal, rate, and time.", "If P = 1000, R = 10, T = 1, SI = 100.", "In SI = P × R × T / 100, what does P mean?", new String[]{"Principal", "Percentage", "Profit", "Perimeter"}, "Principal"));
            f.add(new Formula("c7_amount", 7, "Finance", "Amount", "Amount = Principal + Interest", "Amount is total money after adding interest.", "If principal = 1000 and interest = 100, amount = 1100.", "What is interest?", new String[]{"Extra money added", "Original money only", "Loss only", "Area"}, "Extra money added"));

            // Class 8
            f.add(new Formula("c8_identity_plus", 8, "Algebra Identities", "Square of Sum", "(a + b)² = a² + 2ab + b²", "This identity expands the square of a sum.", "(x + 3)² = x² + 6x + 9.", "In 2ab, what are a and b?", new String[]{"Two terms being multiplied", "Only angles", "Only constants", "Only fractions"}, "Two terms being multiplied"));
            f.add(new Formula("c8_identity_minus", 8, "Algebra Identities", "Square of Difference", "(a - b)² = a² - 2ab + b²", "This identity expands the square of a difference.", "(x - 3)² = x² - 6x + 9.", "What is the middle term in (a - b)²?", new String[]{"-2ab", "+2ab", "ab²", "a²b"}, "-2ab"));
            f.add(new Formula("c8_difference_squares", 8, "Algebra Identities", "Difference of Squares", "a² - b² = (a - b)(a + b)", "This identity factors a difference of two squares.", "x² - 9 = (x - 3)(x + 3).", "Which expression is a difference of squares?", new String[]{"a² - b²", "a² + b²", "a - b", "2ab"}, "a² - b²"));
            f.add(new Formula("c8_cuboid_volume", 8, "Mensuration", "Volume of Cuboid", "V = l × w × h", "Volume of a cuboid is length times width times height.", "If l=5, w=4, h=3, V=60.", "In V = l × w × h, what does h mean?", new String[]{"Height", "Half", "Hypotenuse", "Horizontal"}, "Height"));
            f.add(new Formula("c8_cube_volume", 8, "Mensuration", "Volume of Cube", "V = s³", "A cube has all sides equal, so volume is side cubed.", "If s = 4, V = 4³ = 64.", "In V = s³, what does s mean?", new String[]{"Side", "Sum", "Slope", "Speed"}, "Side"));
            f.add(new Formula("c8_cuboid_surface", 8, "Mensuration", "Surface Area of Cuboid", "SA = 2(lw + lh + wh)", "Surface area adds the areas of all six faces.", "If l=3, w=2, h=1, SA=2(6+3+2)=22.", "What does SA mean?", new String[]{"Surface Area", "Simple Average", "Square Amount", "Side Angle"}, "Surface Area"));
            f.add(new Formula("c8_cube_surface", 8, "Mensuration", "Surface Area of Cube", "SA = 6s²", "A cube has six equal square faces.", "If s = 3, SA = 6 × 9 = 54.", "Why is there a 6 in SA = 6s²?", new String[]{"Six faces", "Six sides per face", "Six angles only", "Six formulas"}, "Six faces"));
            f.add(new Formula("c8_cylinder_volume", 8, "Mensuration", "Volume of Cylinder", "V = πr²h", "Cylinder volume is circular base area times height.", "If r=2, h=5, V=π×4×5=20π.", "In V = πr²h, what does h mean?", new String[]{"Height", "Half", "Hypotenuse", "Horizontal"}, "Height"));
            f.add(new Formula("c8_cylinder_curved", 8, "Mensuration", "Curved Surface Area of Cylinder", "CSA = 2πrh", "Curved surface area covers only the side of a cylinder.", "If r=3, h=4, CSA=24π.", "What does CSA mean?", new String[]{"Curved Surface Area", "Circle Side Amount", "Cube Surface Area", "Common Square Area"}, "Curved Surface Area"));

            // Class 9
            f.add(new Formula("c9_pythagoras", 9, "Geometry", "Pythagorean Theorem", "a² + b² = c²", "In a right triangle, the square of hypotenuse equals sum of squares of other two sides.", "If a=3 and b=4, c²=25, so c=5.", "In a² + b² = c², what is c?", new String[]{"Hypotenuse", "Area", "Base only", "Angle"}, "Hypotenuse"));
            f.add(new Formula("c9_distance", 9, "Coordinate Geometry", "Distance Formula", "d = √((x₂-x₁)² + (y₂-y₁)²)", "This finds distance between two points.", "For (0,0) and (3,4), d=√(9+16)=5.", "What does d mean?", new String[]{"Distance", "Diameter", "Degree", "Difference only"}, "Distance"));
            f.add(new Formula("c9_midpoint", 9, "Coordinate Geometry", "Midpoint Formula", "M = ((x₁+x₂)/2, (y₁+y₂)/2)", "Midpoint is the middle point between two coordinates.", "For (2,4) and (6,8), M=(4,6).", "What does midpoint mean?", new String[]{"Middle point", "Largest point", "Slope", "Distance"}, "Middle point"));
            f.add(new Formula("c9_slope", 9, "Coordinate Geometry", "Slope", "m = (y₂-y₁)/(x₂-x₁)", "Slope measures steepness of a line.", "For (1,2) and (3,6), m=(6-2)/(3-1)=2.", "What does m usually mean here?", new String[]{"Slope", "Mass", "Midpoint", "Mean"}, "Slope"));
            f.add(new Formula("c9_sin", 9, "Trigonometry", "Sine Ratio", "sin θ = Opposite / Hypotenuse", "Sine compares opposite side with hypotenuse in a right triangle.", "If opposite=3 and hypotenuse=5, sin θ=3/5.", "In sin θ, what is θ?", new String[]{"Angle", "Area", "Arc length", "Answer"}, "Angle"));
            f.add(new Formula("c9_cos", 9, "Trigonometry", "Cosine Ratio", "cos θ = Adjacent / Hypotenuse", "Cosine compares adjacent side with hypotenuse.", "If adjacent=4 and hypotenuse=5, cos θ=4/5.", "Which side is used on top in cos θ?", new String[]{"Adjacent", "Opposite", "Hypotenuse", "Diameter"}, "Adjacent"));
            f.add(new Formula("c9_tan", 9, "Trigonometry", "Tangent Ratio", "tan θ = Opposite / Adjacent", "Tangent compares opposite side with adjacent side.", "If opposite=3 and adjacent=4, tan θ=3/4.", "Which side is denominator in tan θ?", new String[]{"Adjacent", "Opposite", "Hypotenuse", "Radius"}, "Adjacent"));
            f.add(new Formula("c9_ap_nth", 9, "Sequences", "Nth Term of AP", "aₙ = a + (n - 1)d", "This finds the nth term of an arithmetic progression.", "If a=2, d=3, n=5, aₙ=2+4×3=14.", "In aₙ = a + (n - 1)d, what does d mean?", new String[]{"Common difference", "Distance", "Diameter", "Degree"}, "Common difference"));
            f.add(new Formula("c9_linear", 9, "Algebra", "Slope-Intercept Form", "y = mx + c", "This is a common form of a straight-line equation.", "If m=2 and c=1, y=2x+1.", "In y = mx + c, what does c mean?", new String[]{"Y-intercept", "Circle", "Coefficient only", "Cost"}, "Y-intercept"));

            // Class 10
            f.add(new Formula("c10_quadratic_formula", 10, "Quadratic Equations", "Quadratic Formula", "x = (-b ± √(b² - 4ac)) / 2a", "This solves ax² + bx + c = 0.", "For x² - 5x + 6 = 0, solutions are x=2 and x=3.", "What expression is under the square root?", new String[]{"b² - 4ac", "b² + 4ac", "2a", "-b"}, "b² - 4ac"));
            f.add(new Formula("c10_discriminant", 10, "Quadratic Equations", "Discriminant", "D = b² - 4ac", "Discriminant helps identify the type of roots of a quadratic equation.", "If D > 0, there are two real roots.", "In D = b² - 4ac, what does D mean?", new String[]{"Discriminant", "Diameter", "Distance", "Degree"}, "Discriminant"));
            f.add(new Formula("c10_ap_sum", 10, "Sequences", "Sum of AP", "Sₙ = n/2[2a + (n - 1)d]", "This finds the sum of first n terms of an arithmetic progression.", "If a=2, d=3, n=5, Sₙ=5/2[4+12]=40.", "In Sₙ, what does n mean?", new String[]{"Number of terms", "Numerator only", "Negative value", "New term"}, "Number of terms"));
            f.add(new Formula("c10_trig_identity", 10, "Trigonometry", "Pythagorean Trig Identity", "sin²θ + cos²θ = 1", "This identity is true for every angle θ.", "If sin²θ = 0.36, then cos²θ = 0.64.", "What does θ represent?", new String[]{"Angle", "Area", "Volume", "Radius"}, "Angle"));
            f.add(new Formula("c10_tan_identity", 10, "Trigonometry", "Tangent Identity", "tan θ = sin θ / cos θ", "Tangent can be written using sine and cosine.", "If sin θ=3/5 and cos θ=4/5, tan θ=3/4.", "What is in the denominator of tan θ = sin θ / cos θ?", new String[]{"cos θ", "sin θ", "tan θ", "1"}, "cos θ"));
            f.add(new Formula("c10_compound_interest", 10, "Finance", "Compound Amount", "A = P(1 + r/100)ⁿ", "Compound amount grows by a percentage rate every period.", "If P=1000, r=10, n=2, A=1210.", "In A = P(1 + r/100)ⁿ, what does n mean?", new String[]{"Number of periods", "Numerator", "New amount", "Negative rate"}, "Number of periods"));
            f.add(new Formula("c10_sector_area", 10, "Circle", "Area of Sector", "Area = θ/360 × πr²", "Sector area is a fraction of the full circle area.", "For θ=90°, area=90/360×πr²=1/4πr².", "In sector formula, what does θ mean?", new String[]{"Central angle", "Diameter", "Height", "Slope"}, "Central angle"));
            f.add(new Formula("c10_arc_length", 10, "Circle", "Arc Length", "Arc = θ/360 × 2πr", "Arc length is a fraction of the full circumference.", "For θ=180°, arc=1/2×2πr=πr.", "What full-circle formula is used here?", new String[]{"2πr", "πr²", "4s", "l × w"}, "2πr"));
            f.add(new Formula("c10_probability", 10, "Probability", "Probability", "P(E) = Favourable Outcomes / Total Outcomes", "Probability measures chance of an event.", "For rolling a 6 on a die, P(E)=1/6.", "What does P(E) mean?", new String[]{"Probability of event", "Perimeter of event", "Price estimate", "Power equation"}, "Probability of event"));

            return f;
        }
    }
}
