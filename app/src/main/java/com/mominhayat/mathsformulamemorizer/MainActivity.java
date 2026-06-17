package com.mominhayat.mathsformulamemorizer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private static final int GREEN_LIGHT = Color.rgb(220, 252, 231);
    private static final int BORDER = Color.rgb(209, 250, 229);
    private static final int AMBER = Color.rgb(245, 158, 11);
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final String[] VISIBLE_CLASSES = {"Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
    private static final int SCREEN_CLASS_SELECT = 1;
    private static final int SCREEN_NAME_SELECT = 2;
    private static final int SCREEN_HOME = 3;
    private static final int SCREEN_TOPIC = 4;
    private static final int SCREEN_LESSON = 5;
    private static final int SCREEN_QUIZ = 6;
    private static final int SCREEN_QUIZ_CONFIRM = 7;
    private static final int SCREEN_LESSON_RESULT = 8;
    private static final int SCREEN_WEAK = 9;
    private static final int SCREEN_PROGRESS = 10;
    private static final int SCREEN_RESET_ALL_CONFIRM = 11;
    private static final int SCREEN_RESET_SCOPE_CONFIRM = 12;
    private static final int SCREEN_ABOUT = 13;
    private static final int SCREEN_LANGUAGE = 14;
    private static final String STATE_SCREEN = "screen";
    private static final String STATE_TOPIC = "topic";
    private static final String STATE_FORMULA_ID = "formula_id";
    private static final String PREF_LANGUAGE = "app_language";
    private static final String LANG_EN = "en";
    private static final String LANG_ES = "es";
    private static final String LANG_HI = "hi";
    private static final String LANG_UR = "ur";
    private static final String LANG_AR = "ar";
    private static final String LANG_FR = "fr";
    private static final String[] LANGUAGE_CODES = {LANG_EN, LANG_ES, LANG_HI, LANG_UR, LANG_AR, LANG_FR};

    private SharedPreferences prefs;
    private final List<Formula> formulas = FormulaRepository.all();
    private final Random random = new Random();
    private String selectedClass;
    private String appLanguage = LANG_EN;
    private int currentScreen = SCREEN_HOME;
    private String currentTopic;
    private Formula currentFormula;
    private QuizSession currentQuizSession;
    private Runnable quizContinueAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("maths_formula_progress", MODE_PRIVATE);
        selectedClass = prefs.getString("selected_class", null);
        appLanguage = prefs.getString(PREF_LANGUAGE, LANG_EN);
        if (savedInstanceState != null && restoreSavedScreen(savedInstanceState)) {
            return;
        }
        if (selectedClass == null) {
            showClassSelect();
        } else if (isStudentNameMissing()) {
            showNameSelect();
        } else {
            showHome();
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SCREEN, currentScreen);
        outState.putString(STATE_TOPIC, currentTopic);
        if (currentFormula != null) {
            outState.putString(STATE_FORMULA_ID, currentFormula.id);
        }
    }

    private boolean restoreSavedScreen(Bundle state) {
        int savedScreen = state.getInt(STATE_SCREEN, SCREEN_HOME);
        String savedTopic = state.getString(STATE_TOPIC);
        Formula savedFormula = findFormulaById(state.getString(STATE_FORMULA_ID));

        if (selectedClass == null) {
            if (savedScreen == SCREEN_LANGUAGE) {
                showLanguageSelect();
            } else {
                showClassSelect();
            }
            return true;
        }
        if (savedScreen == SCREEN_CLASS_SELECT) {
            showClassSelect();
            return true;
        }
        if (savedScreen == SCREEN_NAME_SELECT || isStudentNameMissing()) {
            showNameSelect();
            return true;
        }
        if (savedScreen == SCREEN_TOPIC && savedTopic != null) {
            showTopic(savedTopic);
            return true;
        }
        if ((savedScreen == SCREEN_LESSON
                || savedScreen == SCREEN_QUIZ
                || savedScreen == SCREEN_QUIZ_CONFIRM
                || savedScreen == SCREEN_LESSON_RESULT) && savedFormula != null) {
            showLesson(savedFormula);
            return true;
        }
        if (savedScreen == SCREEN_WEAK) {
            showWeakFormulas();
            return true;
        }
        if (savedScreen == SCREEN_PROGRESS || savedScreen == SCREEN_RESET_ALL_CONFIRM) {
            showProgress();
            return true;
        }
        if (savedScreen == SCREEN_ABOUT) {
            showAboutPrivacy();
            return true;
        }
        if (savedScreen == SCREEN_LANGUAGE) {
            showLanguageSelect();
            return true;
        }
        showHome();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentScreen == SCREEN_HOME) {
            finish();
            return;
        }
        if (currentScreen == SCREEN_CLASS_SELECT) {
            if (selectedClass != null && !isStudentNameMissing()) {
                showHome();
            } else {
                finish();
            }
            return;
        }
        if (currentScreen == SCREEN_NAME_SELECT) {
            if (isStudentNameMissing()) {
                showClassSelect();
            } else {
                showHome();
            }
            return;
        }
        if (currentScreen == SCREEN_TOPIC || currentScreen == SCREEN_WEAK || currentScreen == SCREEN_RESET_SCOPE_CONFIRM) {
            showHome();
            return;
        }
        if (currentScreen == SCREEN_LESSON || currentScreen == SCREEN_LESSON_RESULT) {
            if (currentTopic != null && !currentTopic.isEmpty()) {
                showTopic(currentTopic);
            } else {
                showHome();
            }
            return;
        }
        if (currentScreen == SCREEN_QUIZ) {
            if (currentQuizSession != null) {
                Runnable continueAction = quizContinueAction;
                if (continueAction == null) {
                    QuizSession session = currentQuizSession;
                    continueAction = () -> renderQuestion(session);
                }
                showQuitQuizConfirm(currentQuizSession, continueAction);
            } else {
                showHome();
            }
            return;
        }
        if (currentScreen == SCREEN_QUIZ_CONFIRM) {
            if (quizContinueAction != null) {
                quizContinueAction.run();
            } else if (currentQuizSession != null) {
                renderQuestion(currentQuizSession);
            } else {
                showHome();
            }
            return;
        }
        if (currentScreen == SCREEN_PROGRESS || currentScreen == SCREEN_RESET_ALL_CONFIRM || currentScreen == SCREEN_ABOUT || currentScreen == SCREEN_LANGUAGE) {
            showHome();
            return;
        }
        showHome();
    }

    private void showClassSelect() {
        currentScreen = SCREEN_CLASS_SELECT;
        currentQuizSession = null;
        quizContinueAction = null;
        LinearLayout root = baseScreen(tr("Choose class"), tr("Tap a class to continue."));

        Button language = compactSecondaryButton(tr("Language") + ": " + languageName(appLanguage));
        language.setOnClickListener(v -> showLanguageSelect());
        root.addView(language);

        LinearLayout hero = cardTint(GREEN_LIGHT);
        hero.addView(sectionTitle(tr("Maths Formula Memorizer")));
        hero.addView(bigText(tr("Pick your school level")));
        hero.addView(smallText(tr("Each class includes revision from earlier levels and a small next-level challenge.")));
        root.addView(hero);

        for (String className : VISIBLE_CLASSES) {
            Button classButton = classChoiceButton(className);
            classButton.setOnClickListener(v -> chooseClass(className));
            root.addView(classButton);
        }

        root.addView(smallText(tr("You can edit this later from the home screen.")));
    }

    private void chooseClass(String className) {
        selectedClass = className;
        prefs.edit().putString("selected_class", selectedClass).apply();
        if (isStudentNameMissing()) {
            showNameSelect();
        } else {
            showHome();
        }
    }

    private void showNameSelect() {
        currentScreen = SCREEN_NAME_SELECT;
        currentQuizSession = null;
        quizContinueAction = null;
        LinearLayout root = baseScreen(tr("What should I call you?"), tr("Saved only on this phone. You can skip and start right away."));

        Button language = compactSecondaryButton(tr("Language") + ": " + languageName(appLanguage));
        language.setOnClickListener(v -> showLanguageSelect());
        root.addView(language);

        LinearLayout c = cardTint(GREEN_LIGHT);
        c.addView(sectionTitle(tr("Add a friendly name")));
        c.addView(smallText(tr("This helps the app make coaching messages feel personal.")));
        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setHint(tr("Example: Ali"));
        nameInput.setContentDescription(tr("Student name"));
        nameInput.setText(isStudentNameMissing() ? "" : getStudentName());
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        nameInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        applyDirection(nameInput);
        c.addView(nameInput);
        c.addView(smallText(tr("Saved only on this phone. No login. No internet.")));
        root.addView(c);

        Button save = primaryButton(tr("Save and start learning"));
        save.setOnClickListener(v -> {
            String clean = cleanStudentName(nameInput.getText().toString());
            prefs.edit().putString("student_name", clean).apply();
            showHome();
        });
        root.addView(save);

        Button skip = ghostButton(tr("Skip for now"));
        skip.setOnClickListener(v -> {
            prefs.edit().putString("student_name", "Student").apply();
            showHome();
        });
        root.addView(skip);
    }

    private void showLanguageSelect() {
        currentScreen = SCREEN_LANGUAGE;
        currentQuizSession = null;
        quizContinueAction = null;
        LinearLayout root = baseScreen(tr("Choose language"), tr("This changes app buttons and headings only. Maths content stays in English for accuracy."));

        LinearLayout c = cardTint(GREEN_LIGHT);
        c.addView(sectionTitle(tr("App language")));
        c.addView(smallText(tr("You can change this anytime from Home.")));
        root.addView(c);

        for (String code : LANGUAGE_CODES) {
            boolean selected = code.equals(appLanguage);
            Button choice = selected ? primaryButton(languageName(code) + "  " + tr("Selected")) : secondaryButton(languageName(code));
            choice.setOnClickListener(v -> {
                appLanguage = code;
                prefs.edit().putString(PREF_LANGUAGE, appLanguage).apply();
                if (selectedClass == null) {
                    showClassSelect();
                } else if (isStudentNameMissing()) {
                    showNameSelect();
                } else {
                    showHome();
                }
            });
            root.addView(choice);
        }

        Button back = ghostButton(selectedClass == null ? tr("Back to class") : tr("Back to home"));
        back.setOnClickListener(v -> {
            if (selectedClass == null) {
                showClassSelect();
            } else if (isStudentNameMissing()) {
                showNameSelect();
            } else {
                showHome();
            }
        });
        root.addView(back);
    }

    private void showHome() {
        if (selectedClass == null) {
            showClassSelect();
            return;
        }
        if (isStudentNameMissing()) {
            showNameSelect();
            return;
        }
        if (selectedClass.equals("General Practice")) {
            selectedClass = "Class 7";
            prefs.edit().putString("selected_class", selectedClass).apply();
        }
        currentScreen = SCREEN_HOME;
        currentTopic = null;
        currentFormula = null;
        currentQuizSession = null;
        quizContinueAction = null;

        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;
        int mistakeCount = countMistakeMarks(available);
        String name = getStudentName();

        LinearLayout root = blankScreen();

        TextView brand = metaText(tr("Maths Formula Memorizer"));
        brand.setGravity(isRtlLanguage() ? Gravity.LEFT : Gravity.RIGHT);
        root.addView(brand);

        LinearLayout greetingRow = row();
        greetingRow.setGravity(Gravity.CENTER_VERTICAL);
        greetingRow.setMinimumHeight(dp(58));
        TextView greeting = screenTitle(trf("Hi, %s", name));
        greetingRow.addView(greeting, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        Button editName = tinyActionButton(tr("Edit"));
        editName.setContentDescription(tr("Edit student name"));
        editName.setOnClickListener(v -> showNameSelect());
        greetingRow.addView(editName);
        root.addView(greetingRow);

        LinearLayout classRow = row();
        classRow.setGravity(Gravity.CENTER_VERTICAL);
        classRow.setMinimumHeight(dp(48));
        TextView classText = classTitle(displayClass(selectedClass));
        classRow.addView(classText, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        Button editClass = tinyActionButton(tr("Edit"));
        editClass.setContentDescription(tr("Change class"));
        editClass.setOnClickListener(v -> showClassSelect());
        classRow.addView(editClass);
        classRow.setPadding(0, 0, 0, dp(8));
        root.addView(classRow);

        Button language = compactSecondaryButton(tr("Language") + ": " + languageName(appLanguage));
        language.setOnClickListener(v -> showLanguageSelect());
        root.addView(language);

        LinearLayout hero = cardTint(Color.rgb(232, 252, 244));
        hero.addView(sectionTitle(trf("%s cumulative progress", displayClass(selectedClass))));
        hero.addView(bigText(cumulativeProgressTitle(completedCount, available.size())));
        hero.addView(smallText(tr("Revision, new formulas, and one-step challenges move together here.")));
        hero.addView(glowProgressBar(available.size(), completedCount));
        hero.addView(levelRail(tr("Start"), tr("Practice"), tr("Confident"), tr("Mastered")));
        Button resetClass = smallResetButton(trf("Reset %s progress", displayClass(selectedClass)));
        resetClass.setOnClickListener(v -> showResetScopeConfirm(
                trf("Reset %s progress?", displayClass(selectedClass)),
                trf("This clears completed lessons and mistake marks for formulas visible in %s.", displayClass(selectedClass)),
                available,
                true
        ));
        hero.addView(resetClass);
        root.addView(hero);

        Button weak = secondaryButton(trf("Mistake review (%d)", mistakeCount));
        weak.setOnClickListener(v -> showWeakFormulas());
        root.addView(weak);

        Button about = ghostButton(tr("Privacy & app info"));
        about.setOnClickListener(v -> showAboutPrivacy());
        root.addView(about);

        TextView unitsTitle = sectionTitle(tr("Choose a topic"));
        root.addView(unitsTitle);

        LinkedHashMap<String, List<Formula>> byTopic = groupByTopic(available);
        for (String topic : byTopic.keySet()) {
            List<Formula> topicFormulas = byTopic.get(topic);
            int done = topicCompleted(topicFormulas, completed);
            int topicWeak = countWeakFormulas(topicFormulas);

            LinearLayout topicCard = card();
            topicCard.addView(glowProgressBar(topicFormulas.size(), done));
            topicCard.addView(levelRail(tr("Begin"), tr("Learn"), tr("Strong"), tr("Ready")));
            Button resetArea = smallResetButton(tr("Reset topic"));
            resetArea.setContentDescription(trf("Reset progress for %s", topic));
            resetArea.setOnClickListener(v -> showResetScopeConfirm(
                    trf("Reset %s?", topic),
                    tr("This clears completed lessons and mistake marks for this topic only."),
                    topicFormulas,
                    false
            ));
            topicCard.addView(resetArea);
            topicCard.addView(sectionTitle(topicIcon(topic) + " " + topic));
            topicCard.addView(smallText(areaProgressMessage(topicFormulas, done, topicWeak)));
            Button open = done == topicFormulas.size() ? secondaryButton(tr("Review topic")) : compactPrimaryButton(tr("Open topic"));
            open.setContentDescription(done == topicFormulas.size() ? trf("Review %s", topic) : trf("Open %s", topic));
            open.setOnClickListener(v -> showTopic(topic));
            topicCard.addView(open);
            root.addView(topicCard);
        }
    }



    private void showTopic(String topic) {
        currentScreen = SCREEN_TOPIC;
        currentTopic = topic;
        currentFormula = null;
        currentQuizSession = null;
        quizContinueAction = null;
        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        List<Formula> topicFormulas = new ArrayList<>();
        for (Formula f : available) if (f.topic.equals(topic)) topicFormulas.add(f);
        int done = topicCompleted(topicFormulas, completed);
        int weak = countWeakFormulas(topicFormulas);

        LinearLayout root = baseScreen(topicIcon(topic) + " " + topic, trf("%s / 1-minute formula lessons", displayClass(selectedClass)));

        LinearLayout summary = cardTint(GREEN_LIGHT);
        summary.addView(bigText(trf("%d / %d formulas completed", done, topicFormulas.size())));
        addProgressBar(summary, topicFormulas.size(), done);
        summary.addView(smallText(weak == 0 ? tr("No weak formulas in this topic yet.") : trf("%d formula(s) need review in this topic.", weak)));
        root.addView(summary);

        if (isGeneralPractice()) {
            for (Formula f : topicFormulas) {
                addFormulaItem(root, f, completed);
            }
        } else {
            addFormulaSection(root, tr("New for you"), formulasWithLevel(topicFormulas, "New for you"), completed);
            addFormulaSection(root, tr("Revision"), formulasWithLevel(topicFormulas, "Revision"), completed);
            addFormulaSection(root, tr("Challenge"), formulasWithLevel(topicFormulas, "Challenge"), completed);
        }

        Button back = ghostButton(tr("Back to home"));
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showLesson(Formula f) {
        currentScreen = SCREEN_LESSON;
        currentTopic = f.topic;
        currentFormula = f;
        currentQuizSession = null;
        quizContinueAction = null;
        LinearLayout root = blankScreen();
        String levelLabel = quizLevelLabel(f);

        TextView context = metaText(getStudentName() + " / " + displayClass(selectedClass) + " / " + f.topic);
        context.setText(getStudentName() + " / " + displayClass(selectedClass) + " / " + f.topic);

        LinearLayout nav = row();
        Button area = topNavButton(tr("Back to topic"));
        area.setOnClickListener(v -> showTopic(f.topic));
        Button home = topNavButton(tr("Home page"));
        home.setOnClickListener(v -> showHome());
        nav.addView(area, rowWeight());
        nav.addView(home, rowWeight());
        root.addView(nav);

        context.setGravity(Gravity.CENTER);
        root.addView(context);

        LinearLayout hero = cardTint(Color.rgb(236, 253, 245));
        hero.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout metaRow = row();
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView topicLabel = lessonMetaTitle(topicIcon(f.topic) + " " + f.topic);
        metaRow.addView(topicLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        if (!levelLabel.isEmpty()) {
            metaRow.addView(lessonChip(levelLabel));
        }
        hero.addView(metaRow);

        hero.addView(lessonTitle(f.name));
        hero.addView(compactFormulaBox(f.formula));

        TextView visual = compactLessonText(tr("Visual memory"));
        visual.setTextColor(GREEN_DARK);
        visual.setTypeface(Typeface.DEFAULT_BOLD);
        visual.setPadding(0, dp(4), 0, dp(2));
        hero.addView(visual);

        FormulaDiagramView diagram = new FormulaDiagramView(f);
        LinearLayout.LayoutParams diagramLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(210));
        diagramLp.setMargins(0, dp(2), 0, dp(6));
        diagram.setLayoutParams(diagramLp);
        hero.addView(diagram);
        hero.addView(lessonInfoBlock(tr("Meaning"), f.explanation));
        hero.addView(lessonInfoBlock(tr("Example"), f.example));
        root.addView(hero);

        Button quiz = compactPrimaryButton(tr("Start quiz"));
        quiz.setText(tr("Start quiz"));
        quiz.setOnClickListener(v -> showQuiz(f));
        root.addView(quiz);
    }




    private void showQuiz(Formula f) {
        List<Question> questions = buildQuiz(f);
        QuizSession session = new QuizSession(f, questions);
        renderQuestion(session);
    }

    private void renderQuestion(QuizSession session) {
        currentScreen = SCREEN_QUIZ;
        currentQuizSession = session;
        currentFormula = session.formula;
        currentTopic = session.formula.topic;
        quizContinueAction = () -> renderQuestion(session);
        Question q = session.questions.get(session.index);
        String quizLabel = quizLevelLabel(session.formula);
        LinearLayout root = baseScreen(quizLabel.isEmpty() ? tr("Quick quiz") : quizLabel, getStudentName() + " / " + session.formula.name);

        LinearLayout progressCard = cardTint(GREEN_LIGHT);
        progressCard.addView(sectionTitle(trf("Question %d of %d", session.index + 1, session.questions.size())));
        addProgressBar(progressCard, session.questions.size(), session.index + 1);
        progressCard.addView(smallText(trf("Score so far: %d correct", session.correct)));
        root.addView(progressCard);

        LinearLayout card = card();
        card.addView(bigText(q.prompt));
        card.addView(smallText(tr("Tap the best answer. You can use a hint or quit without saving progress.")));
        Button hint = secondaryButton(tr("Show hint"));
        hint.setOnClickListener(v -> showHint(session, q));
        card.addView(hint);
        for (String option : q.options) {
            Button b = secondaryButton(option);
            b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            b.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            b.setTextDirection(View.TEXT_DIRECTION_LTR);
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

        Button quit = ghostButton(tr("Quit quiz"));
        quit.setTextColor(RED);
        quit.setOnClickListener(v -> showQuitQuizConfirm(session, () -> renderQuestion(session)));
        root.addView(quit);
    }




    private void showFeedback(QuizSession session, Question q, boolean correct, String chosen) {
        currentScreen = SCREEN_QUIZ;
        currentQuizSession = session;
        currentFormula = session.formula;
        currentTopic = session.formula.topic;
        quizContinueAction = () -> showFeedback(session, q, correct, chosen);
        LinearLayout root = baseScreen(correct ? tr("Correct") : tr("Focus - not correct"), session.formula.name);
        LinearLayout card = cardTint(correct ? GREEN_LIGHT : Color.rgb(255, 251, 235));
        TextView title = bigText(correct ? praiseMessage() : firmCoachMessage());
        title.setTextColor(correct ? GREEN_DARK : AMBER);
        card.addView(title);
        if (!correct) {
            card.addView(smallText(trf("Your answer: %s", chosen)));
        }
        card.addView(sectionTitle(correct ? tr("Why it is right") : tr("Correct answer")));
        if (looksLikeFormula(q.correctAnswer)) {
            card.addView(formulaText(q.correctAnswer));
        } else {
            card.addView(bigText(q.correctAnswer));
        }
        card.addView(bodyText(q.explanation));
        if (!correct) {
            card.addView(smallText(trf("Read slowly, %s. You are not allowed to guess carelessly.", getStudentName())));
        }
        root.addView(card);

        Button next = primaryButton(session.index == session.questions.size() - 1 ? tr("Finish lesson") : tr("Next question"));
        next.setOnClickListener(v -> {
            session.index++;
            if (session.index >= session.questions.size()) {
                finishLesson(session);
            } else {
                renderQuestion(session);
            }
        });
        root.addView(next);

        Button quit = ghostButton(tr("Quit quiz"));
        quit.setTextColor(RED);
        quit.setOnClickListener(v -> showQuitQuizConfirm(session, () -> showFeedback(session, q, correct, chosen)));
        root.addView(quit);
    }




    private void finishLesson(QuizSession session) {
        Formula f = session.formula;
        currentScreen = SCREEN_LESSON_RESULT;
        currentTopic = f.topic;
        currentFormula = f;
        currentQuizSession = null;
        quizContinueAction = null;
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
        LinearLayout root = baseScreen(perfect ? tr("Mastered") : tr("Added to review"), f.name);
        LinearLayout card = cardTint(perfect ? GREEN_LIGHT : Color.rgb(255, 251, 235));
        card.addView(bigText(stars(session.correct, session.questions.size()) + "  " + trf("Score: %d / %d", session.correct, session.questions.size())));
        card.addView(formulaText(f.formula));
        if (perfect) {
            card.addView(bodyText(tr("Excellent. This formula is marked as mastered.")));
        } else {
            card.addView(bodyText(tr("Good practice. This formula will appear in mistake review until it becomes easy.")));
        }
        card.addView(smallText(tr("One formula at a time builds long-term memory.")));
        root.addView(card);

        Button continueBtn = primaryButton(tr("Next formula"));
        continueBtn.setOnClickListener(v -> showLesson(nextFormula(availableFormulas(), getCompletedSet())));
        root.addView(continueBtn);

        if (!perfect) {
            Button review = secondaryButton(tr("Review mistakes"));
            review.setOnClickListener(v -> showWeakFormulas());
            root.addView(review);
        }

        Button home = ghostButton(tr("Home"));
        home.setOnClickListener(v -> showHome());
        root.addView(home);
    }


    private void showWeakFormulas() {
        currentScreen = SCREEN_WEAK;
        currentTopic = null;
        currentFormula = null;
        currentQuizSession = null;
        quizContinueAction = null;
        List<Formula> weak = new ArrayList<>();
        for (Formula f : availableFormulas()) {
            if (prefs.getInt("wrong_" + f.id, 0) > 0) weak.add(f);
        }
        Collections.sort(weak, (a, b) -> prefs.getInt("wrong_" + b.id, 0) - prefs.getInt("wrong_" + a.id, 0));

        LinearLayout root = baseScreen(tr("Mistake review"), tr("Practice formulas you forgot before."));
        if (weak.isEmpty()) {
            LinearLayout c = cardTint(GREEN_LIGHT);
            c.addView(bigText(tr("No weak formulas yet")));
            c.addView(bodyText(tr("Mistakes will appear here automatically after quizzes.")));
            c.addView(smallText(tr("Keep practicing one formula per day.")));
            root.addView(c);
        } else {
            LinearLayout top = cardTint(Color.rgb(255, 251, 235));
            top.addView(bigText(tr("Review the hardest one first")));
            top.addView(smallText(tr("Weak formulas are sorted by number of mistakes.")));
            Button start = primaryButton(tr("Start mistake review"));
            start.setOnClickListener(v -> showLesson(weak.get(0)));
            top.addView(start);
            root.addView(top);

            for (Formula f : weak) {
                LinearLayout c = card();
                c.addView(bigText("⚠ " + f.name));
                c.addView(smallText(f.topic + " / " + trf("Mistakes: %d", prefs.getInt("wrong_" + f.id, 0))));
                c.addView(formulaText(f.formula));
                Button practice = secondaryButton(tr("Practice again"));
                practice.setOnClickListener(v -> showLesson(f));
                c.addView(practice);
                root.addView(c);
            }
        }
        Button back = ghostButton(tr("Back to home"));
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }


    private void showProgress() {
        currentScreen = SCREEN_PROGRESS;
        currentTopic = null;
        currentFormula = null;
        currentQuizSession = null;
        quizContinueAction = null;
        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;
        int accuracy = currentAccuracy();
        int weakCount = countWeakFormulas(available);

        LinearLayout root = baseScreen(tr("Progress report"), getStudentName() + " / " + displayClass(selectedClass));
        LinearLayout c = cardTint(GREEN_LIGHT);
        c.addView(bigText(trf("Overall progress: %d%%", percent(completedCount, available.size()))));
        addProgressBar(c, available.size(), completedCount);
        LinearLayout stats = row();
        stats.addView(statBox(tr("Lessons"), completedCount + "/" + available.size()), rowWeight());
        stats.addView(statBox(tr("Accuracy"), accuracy + "%"), rowWeight());
        stats.addView(statBox(tr("Streak"), prefs.getInt("streak", 0) + "d"), rowWeight());
        c.addView(stats);
        c.addView(smallText(tr("Student name and progress are saved only on this phone. No account and no internet needed.")));
        root.addView(c);

        root.addView(sectionTitle(tr("Topic progress")));
        LinkedHashMap<String, List<Formula>> byTopic = groupByTopic(available);
        for (String topic : byTopic.keySet()) {
            List<Formula> topicFormulas = byTopic.get(topic);
            int done = topicCompleted(topicFormulas, completed);
            LinearLayout area = card();
            area.addView(bigText(topicIcon(topic) + " " + topic));
            area.addView(smallText(trf("%d / %d completed", done, topicFormulas.size())));
            addProgressBar(area, topicFormulas.size(), done);
            root.addView(area);
        }

        if (weakCount > 0) {
            Button weak = primaryButton(trf("Review %d weak formula(s)", weakCount));
            weak.setOnClickListener(v -> showWeakFormulas());
            root.addView(weak);
        }

        Button changeName = secondaryButton(tr("Change student name"));
        changeName.setOnClickListener(v -> showNameSelect());
        root.addView(changeName);

        Button reset = ghostButton(tr("Reset progress for this phone"));
        reset.setTextColor(RED);
        reset.setOnClickListener(v -> showResetConfirm());
        root.addView(reset);

        Button back = primaryButton(tr("Back to home"));
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showAboutPrivacy() {
        currentScreen = SCREEN_ABOUT;
        currentTopic = null;
        currentFormula = null;
        currentQuizSession = null;
        quizContinueAction = null;

        LinearLayout root = baseScreen(tr("Privacy & app info"), tr("Clear facts for students and parents."));

        LinearLayout privacy = cardTint(GREEN_LIGHT);
        privacy.addView(sectionTitle(tr("Offline by design")));
        privacy.addView(bodyText(tr("Maths Formula Memorizer works without internet. It has no ads, no login, no Firebase, no analytics, and no server.")));
        privacy.addView(smallText(tr("The app does not send student name, class, progress, or mistakes anywhere.")));
        root.addView(privacy);

        LinearLayout storage = card();
        storage.addView(sectionTitle(tr("Saved only on this phone")));
        storage.addView(bodyText(tr("Student name, selected class, completed formulas, quiz accuracy, streak, and mistake review are saved inside this app on this device.")));
        storage.addView(smallText(tr("Use reset buttons to clear progress from this phone.")));
        root.addView(storage);

        LinearLayout learning = card();
        learning.addView(sectionTitle(tr("What the app does")));
        learning.addView(bodyText(tr("Students choose a class, learn formulas with visual memory diagrams, answer short quizzes, and review weak formulas until they improve.")));
        learning.addView(smallText(tr("Class progress includes revision from earlier classes and one-class-ahead challenge formulas.")));
        root.addView(learning);

        LinearLayout contact = card();
        contact.addView(sectionTitle(tr("Contact")));
        contact.addView(bodyText(tr("For privacy questions, contact: adilhayat@yahoo.com")));
        root.addView(contact);

        Button back = primaryButton(tr("Back to home"));
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }



    private void showResetConfirm() {
        currentScreen = SCREEN_RESET_ALL_CONFIRM;
        currentQuizSession = null;
        quizContinueAction = null;
        LinearLayout root = baseScreen(tr("Reset progress?"), tr("This only clears progress on this phone."));
        Button yes = primaryButton(tr("Yes, reset progress"));
        yes.setOnClickListener(v -> {
            String keepClass = selectedClass;
            String keepName = getStudentName();
            String keepLanguage = appLanguage;
            prefs.edit()
                    .clear()
                    .putString("selected_class", keepClass)
                    .putString("student_name", keepName)
                    .putString(PREF_LANGUAGE, keepLanguage)
                    .apply();
            showHome();
        });
        Button no = ghostButton(tr("Cancel"));
        no.setOnClickListener(v -> showProgress());
        root.addView(yes);
        root.addView(no);
    }

    private void showResetScopeConfirm(String title, String message, List<Formula> formulasToReset, boolean resetTotals) {
        currentScreen = SCREEN_RESET_SCOPE_CONFIRM;
        currentQuizSession = null;
        quizContinueAction = null;
        LinearLayout root = baseScreen(title, tr("This only changes progress saved on this phone."));
        LinearLayout c = cardTint(Color.rgb(255, 251, 235));
        c.addView(sectionTitle(tr("Reset progress")));
        c.addView(bodyText(message));
        c.addView(smallText(tr("Student name and selected class will stay the same.")));
        root.addView(c);

        Button yes = primaryButton(tr("Reset"));
        yes.setTextColor(Color.WHITE);
        yes.setBackground(rounded(RED, 12));
        yes.setOnClickListener(v -> {
            resetFormulaProgress(formulasToReset, resetTotals);
            showHome();
        });
        root.addView(yes);

        Button no = ghostButton(tr("Cancel"));
        no.setOnClickListener(v -> showHome());
        root.addView(no);
    }

    private void resetFormulaProgress(List<Formula> formulasToReset, boolean resetTotals) {
        Set<String> completed = getCompletedSet();
        SharedPreferences.Editor editor = prefs.edit();
        for (Formula f : formulasToReset) {
            completed.remove(f.id);
            editor.remove("wrong_" + f.id);
        }
        editor.putStringSet("completed", completed);
        if (resetTotals) {
            editor.putInt("total_answered", 0);
            editor.putInt("total_correct", 0);
            editor.putInt("streak", 0);
            editor.remove("last_practice_date");
        }
        editor.apply();
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

    private Formula findFormulaById(String id) {
        if (id == null) return null;
        for (Formula f : formulas) {
            if (f.id.equals(id)) return f;
        }
        return null;
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

    private void addFormulaSection(LinearLayout root, String title, List<Formula> list, Set<String> completed) {
        if (list.isEmpty()) return;
        root.addView(sectionTitle(title));
        for (Formula f : list) {
            addFormulaItem(root, f, completed);
        }
    }

    private void addFormulaItem(LinearLayout root, Formula f, Set<String> completed) {
        LinearLayout item = card();
        item.addView(bigText(formulaStatus(f, completed) + " " + f.name));
        item.addView(smallText(formulaMeta(f)));
        item.addView(formulaText(f.formula));
        item.addView(smallText(f.explanation));
        Button open = completed.contains(f.id) ? secondaryButton(tr("Review again")) : primaryButton(tr("Start lesson"));
        open.setOnClickListener(v -> showLesson(f));
        item.addView(open);
        root.addView(item);
    }

    private String formulaMeta(Formula f) {
        String meta = trf("Introduced Class %d", f.introducedClass) + " / " + f.topic;
        String level = displayFormulaLevel(f);
        return level.isEmpty() ? meta : level + " / " + meta;
    }

    private List<Formula> formulasWithLevel(List<Formula> list, String level) {
        List<Formula> result = new ArrayList<>();
        for (Formula f : list) {
            if (formulaLevel(f).equals(level)) result.add(f);
        }
        return result;
    }

    private String cumulativeProgressTitle(int done, int total) {
        int p = percent(done, total);
        if (p == 0) return tr("Fresh start");
        if (p < 35) return tr("Warm-up level");
        if (p < 70) return tr("Steady practice");
        if (p < 100) return tr("Almost mastered");
        return tr("Mastered");
    }

    private String areaProgressMessage(List<Formula> topicFormulas, int done, int weakCount) {
        if (weakCount > 0) return tr("Mistake review is waiting in this topic.");
        int p = percent(done, topicFormulas.size());
        if (p == 0) return tr("Fresh topic. Open it when you are ready.");
        if (p < 35) return tr("Warm-up level. Keep building memory.");
        if (p < 70) return tr("Practice level. The pattern is starting to stick.");
        if (p < 100) return tr("Confident level. A little review will finish it.");
        return tr("Mastered level. Review anytime.");
    }

    private String visualCaption(Formula f) {
        String topic = f.topic.toLowerCase(Locale.US);
        String name = f.name.toLowerCase(Locale.US);
        if (topic.contains("circle")) return "Circle formulas become easier when radius, diameter, and arc are visible.";
        if (topic.contains("mensuration")) return "Match each symbol to a real edge, face, or height.";
        if (topic.contains("coordinate") || name.contains("slope") || name.contains("linear") || name.contains("quadratic")) return "The graph shows how the formula connects points and lines.";
        if (topic.contains("algebra")) return "Colored blocks make expansion and factorization easier to remember.";
        if (topic.contains("profit") || topic.contains("finance")) return "Money bars show what is original, added, reduced, or compared.";
        if (topic.contains("number")) return "Blocks and dots turn the formula into a quick mental picture.";
        if (topic.contains("probability")) return "The outcome grid shows favourable choices against all choices.";
        if (topic.contains("sequence")) return "Equal jumps on the number line show the pattern.";
        return "Use the picture to connect the formula with a shape, comparison, or pattern.";
    }

    private String diagramDescription(Formula f) {
        String topic = f.topic.toLowerCase(Locale.US);
        String name = f.name.toLowerCase(Locale.US);
        if (topic.contains("circle") || name.contains("diameter") || name.contains("arc") || name.contains("sector")) {
            return "Visual memory diagram for " + f.name + ". It shows circle parts such as radius, diameter, arc, sector, or circumference.";
        }
        if (topic.contains("mensuration")) {
            return "Visual memory diagram for " + f.name + ". It shows a three dimensional shape with labelled radius, height, edge, or face.";
        }
        if (topic.contains("coordinate") || name.contains("slope") || name.contains("midpoint") || name.contains("distance") || name.contains("linear") || name.contains("quadratic")) {
            return "Visual memory diagram for " + f.name + ". It shows a coordinate grid with labelled points, line, or curve.";
        }
        if (topic.contains("algebra")) {
            return "Visual memory diagram for " + f.name + ". It uses colored algebra blocks to show the pattern.";
        }
        if (topic.contains("profit") || topic.contains("finance")) {
            return "Visual memory diagram for " + f.name + ". It uses money bars to compare values.";
        }
        if (topic.contains("number")) {
            return "Visual memory diagram for " + f.name + ". It uses blocks, dots, or bars to show the number relationship.";
        }
        if (topic.contains("probability")) {
            return "Visual memory diagram for " + f.name + ". It shows favourable outcomes compared with total outcomes.";
        }
        if (topic.contains("sequence")) {
            return "Visual memory diagram for " + f.name + ". It shows equal jumps on a number line.";
        }
        return "Visual memory diagram for " + f.name + ". It connects the formula to a shape, comparison, or pattern.";
    }

    private String formulaLevel(Formula f) {
        int selected = selectedClassNumber();
        if (selected == 0) return "";
        if (f.introducedClass < selected) return "Revision";
        if (f.introducedClass == selected) return "New for you";
        if (f.introducedClass == selected + 1) return "Challenge";
        return "";
    }

    private String displayFormulaLevel(Formula f) {
        String level = formulaLevel(f);
        return level.isEmpty() ? "" : tr(level);
    }

    private String quizLevelLabel(Formula f) {
        String level = formulaLevel(f);
        if (level.equals("Revision")) return tr("Quick revision");
        if (level.equals("New for you")) return tr("New for this level");
        if (level.equals("Challenge")) return tr("Challenge formula");
        return "";
    }

    private boolean isGeneralPractice() {
        return selectedClass == null || selectedClass.equals("General Practice");
    }

    private List<Formula> availableFormulas() {
        List<Formula> result = new ArrayList<>();
        int maxClass = selectedClassNumber();
        for (Formula f : formulas) {
            if (maxClass == 0 || f.introducedClass <= maxClass + 1) result.add(f);
        }
        Collections.sort(result, Comparator.comparingInt((Formula f) -> f.introducedClass).thenComparing(f -> f.topic).thenComparing(f -> f.name));
        return result;
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
        applyDirection(scroll);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(28));
        applyDirection(root);
        scroll.addView(root);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(TEXT);
        titleView.setTextSize(27);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setPadding(0, dp(8), 0, dp(4));
        applyDirection(titleView);
        root.addView(titleView);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = smallText(subtitle);
            sub.setPadding(0, 0, 0, dp(12));
            root.addView(sub);
        }
        setContentView(scroll);
        return root;
    }

    private LinearLayout blankScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        applyDirection(scroll);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(28));
        applyDirection(root);
        scroll.addView(root);
        setContentView(scroll);
        return root;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackground(roundedStroke(CARD, Color.rgb(229, 231, 235), 14));
        applyDirection(c);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(10));
        c.setLayoutParams(lp);
        return c;
    }

    private boolean looksLikeFormula(String s) {
        return s.contains("=") || s.contains("×") || s.contains("/") || s.contains("π") || s.contains("²") || s.contains("√");
    }

    private String stars(int correct, int total) {
        if (correct == total) return "★★★";
        if (correct >= Math.max(1, total - 1)) return "★★☆";
        return "★☆☆";
    }


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

    private String tr(String text) {
        if (text == null) return "";
        String translated = null;
        if (LANG_ES.equals(appLanguage)) translated = trEs(text);
        else if (LANG_HI.equals(appLanguage)) translated = trHi(text);
        else if (LANG_UR.equals(appLanguage)) translated = trUr(text);
        else if (LANG_AR.equals(appLanguage)) translated = trAr(text);
        else if (LANG_FR.equals(appLanguage)) translated = trFr(text);
        return translated == null ? text : translated;
    }

    private String trf(String text, Object... args) {
        try {
            return String.format(localeForLanguage(), tr(text), args);
        } catch (Exception e) {
            return String.format(Locale.US, text, args);
        }
    }

    private Locale localeForLanguage() {
        if (LANG_ES.equals(appLanguage)) return new Locale("es");
        if (LANG_HI.equals(appLanguage)) return new Locale("hi");
        if (LANG_UR.equals(appLanguage)) return new Locale("ur");
        if (LANG_AR.equals(appLanguage)) return new Locale("ar");
        if (LANG_FR.equals(appLanguage)) return Locale.FRENCH;
        return Locale.US;
    }

    private String languageName(String code) {
        if (LANG_ES.equals(code)) return "Español";
        if (LANG_HI.equals(code)) return "हिन्दी";
        if (LANG_UR.equals(code)) return "اردو";
        if (LANG_AR.equals(code)) return "العربية";
        if (LANG_FR.equals(code)) return "Français";
        return "English";
    }

    private String displayClass(String className) {
        if (className == null || className.equals("General Practice")) return tr("General Practice");
        String number = className.replace("Class", "").trim();
        if (number.isEmpty()) return className;
        return trf("Class %s", number);
    }

    private boolean isRtlLanguage() {
        return LANG_UR.equals(appLanguage) || LANG_AR.equals(appLanguage);
    }

    private void applyDirection(View view) {
        int layoutDirection = isRtlLanguage() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
        int textDirection = isRtlLanguage() ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR;
        view.setLayoutDirection(layoutDirection);
        view.setTextDirection(textDirection);
    }

    private String trEs(String s) {
        switch (s) {
            case "Maths Formula Memorizer": return "Memorizador de fórmulas matemáticas";
            case "Choose class": return "Elige la clase";
            case "Tap a class to continue.": return "Toca una clase para continuar.";
            case "Pick your school level": return "Elige tu nivel escolar";
            case "Each class includes revision from earlier levels and a small next-level challenge.": return "Cada clase incluye repaso de niveles anteriores y un pequeño reto del siguiente nivel.";
            case "You can edit this later from the home screen.": return "Puedes cambiarlo después desde la pantalla principal.";
            case "What should I call you?": return "¿Cómo debo llamarte?";
            case "Saved only on this phone. You can skip and start right away.": return "Guardado solo en este teléfono. Puedes omitirlo y empezar.";
            case "Add a friendly name": return "Añade un nombre";
            case "This helps the app make coaching messages feel personal.": return "Esto ayuda a que los mensajes sean más personales.";
            case "Example: Ali": return "Ejemplo: Ali";
            case "Student name": return "Nombre del estudiante";
            case "Saved only on this phone. No login. No internet.": return "Guardado solo en este teléfono. Sin inicio de sesión. Sin internet.";
            case "Save and start learning": return "Guardar y empezar";
            case "Skip for now": return "Omitir por ahora";
            case "Choose language": return "Elige idioma";
            case "This changes app buttons and headings only. Maths content stays in English for accuracy.": return "Esto cambia solo botones y títulos. El contenido matemático queda en inglés por precisión.";
            case "App language": return "Idioma de la app";
            case "You can change this anytime from Home.": return "Puedes cambiarlo cuando quieras desde Inicio.";
            case "Selected": return "Seleccionado";
            case "Back to class": return "Volver a clase";
            case "Back to home": return "Volver al inicio";
            case "Language": return "Idioma";
            case "Hi, %s": return "Hola, %s";
            case "Edit": return "Editar";
            case "Edit student name": return "Editar nombre del estudiante";
            case "Change class": return "Cambiar clase";
            case "Class %s": return "Clase %s";
            case "%s cumulative progress": return "Progreso acumulado de %s";
            case "Revision, new formulas, and one-step challenges move together here.": return "El repaso, las fórmulas nuevas y los retos avanzan juntos aquí.";
            case "Start": return "Inicio";
            case "Practice": return "Práctica";
            case "Confident": return "Seguro";
            case "Mastered": return "Dominado";
            case "Reset %s progress": return "Reiniciar progreso de %s";
            case "Reset %s progress?": return "¿Reiniciar progreso de %s?";
            case "This clears completed lessons and mistake marks for formulas visible in %s.": return "Esto borra lecciones completadas y errores de las fórmulas visibles en %s.";
            case "Mistake review (%d)": return "Repaso de errores (%d)";
            case "Privacy & app info": return "Privacidad e información";
            case "Choose a topic": return "Elige un tema";
            case "Begin": return "Comenzar";
            case "Learn": return "Aprender";
            case "Strong": return "Fuerte";
            case "Ready": return "Listo";
            case "Reset topic": return "Reiniciar tema";
            case "Reset progress for %s": return "Reiniciar progreso de %s";
            case "Reset %s?": return "¿Reiniciar %s?";
            case "This clears completed lessons and mistake marks for this topic only.": return "Esto borra lecciones completadas y errores solo de este tema.";
            case "Review topic": return "Repasar tema";
            case "Open topic": return "Abrir tema";
            case "Review %s": return "Repasar %s";
            case "Open %s": return "Abrir %s";
            case "%s / 1-minute formula lessons": return "%s / lecciones de fórmula de 1 minuto";
            case "%d / %d formulas completed": return "%d / %d fórmulas completadas";
            case "No weak formulas in this topic yet.": return "Aún no hay fórmulas débiles en este tema.";
            case "%d formula(s) need review in this topic.": return "%d fórmula(s) necesitan repaso en este tema.";
            case "New for you": return "Nuevo para ti";
            case "Revision": return "Repaso";
            case "Challenge": return "Reto";
            case "Back to topic": return "Volver al tema";
            case "Home page": return "Inicio";
            case "Visual memory": return "Memoria visual";
            case "Meaning": return "Significado";
            case "Example": return "Ejemplo";
            case "Start quiz": return "Empezar quiz";
            case "Quick quiz": return "Quiz rápido";
            case "Question %d of %d": return "Pregunta %d de %d";
            case "Score so far: %d correct": return "Puntuación: %d correctas";
            case "Tap the best answer. You can use a hint or quit without saving progress.": return "Toca la mejor respuesta. Puedes usar pista o salir sin guardar.";
            case "Show hint": return "Mostrar pista";
            case "Quit quiz": return "Salir del quiz";
            case "Correct": return "Correcto";
            case "Focus - not correct": return "Concéntrate - no es correcto";
            case "Your answer: %s": return "Tu respuesta: %s";
            case "Why it is right": return "Por qué es correcto";
            case "Correct answer": return "Respuesta correcta";
            case "Read slowly, %s. You are not allowed to guess carelessly.": return "Lee despacio, %s. No adivines sin cuidado.";
            case "Finish lesson": return "Terminar lección";
            case "Next question": return "Siguiente pregunta";
            case "Added to review": return "Añadido al repaso";
            case "Score: %d / %d": return "Puntuación: %d / %d";
            case "Excellent. This formula is marked as mastered.": return "Excelente. Esta fórmula quedó dominada.";
            case "Good practice. This formula will appear in mistake review until it becomes easy.": return "Buen trabajo. Esta fórmula aparecerá en repaso de errores hasta que sea fácil.";
            case "One formula at a time builds long-term memory.": return "Una fórmula a la vez crea memoria duradera.";
            case "Next formula": return "Siguiente fórmula";
            case "Review mistakes": return "Repasar errores";
            case "Home": return "Inicio";
            case "Mistake review": return "Repaso de errores";
            case "Practice formulas you forgot before.": return "Practica fórmulas que olvidaste.";
            case "No weak formulas yet": return "Aún no hay fórmulas débiles";
            case "Mistakes will appear here automatically after quizzes.": return "Los errores aparecerán aquí automáticamente después de los quizzes.";
            case "Keep practicing one formula per day.": return "Sigue practicando una fórmula al día.";
            case "Review the hardest one first": return "Repasa primero la más difícil";
            case "Weak formulas are sorted by number of mistakes.": return "Las fórmulas débiles se ordenan por número de errores.";
            case "Start mistake review": return "Empezar repaso de errores";
            case "Mistakes: %d": return "Errores: %d";
            case "Practice again": return "Practicar otra vez";
            case "Progress report": return "Informe de progreso";
            case "Overall progress: %d%%": return "Progreso total: %d%%";
            case "Lessons": return "Lecciones";
            case "Accuracy": return "Precisión";
            case "Streak": return "Racha";
            case "Student name and progress are saved only on this phone. No account and no internet needed.": return "El nombre y progreso se guardan solo en este teléfono. Sin cuenta ni internet.";
            case "Topic progress": return "Progreso por tema";
            case "%d / %d completed": return "%d / %d completadas";
            case "Review %d weak formula(s)": return "Repasar %d fórmula(s) débiles";
            case "Change student name": return "Cambiar nombre";
            case "Reset progress for this phone": return "Reiniciar progreso de este teléfono";
            case "Clear facts for students and parents.": return "Información clara para estudiantes y padres.";
            case "Offline by design": return "Diseñada sin conexión";
            case "Maths Formula Memorizer works without internet. It has no ads, no login, no Firebase, no analytics, and no server.": return "Maths Formula Memorizer funciona sin internet. No tiene anuncios, inicio de sesión, Firebase, analíticas ni servidor.";
            case "The app does not send student name, class, progress, or mistakes anywhere.": return "La app no envía nombre, clase, progreso ni errores a ningún lugar.";
            case "Saved only on this phone": return "Guardado solo en este teléfono";
            case "Student name, selected class, completed formulas, quiz accuracy, streak, and mistake review are saved inside this app on this device.": return "Nombre, clase, fórmulas completadas, precisión, racha y errores se guardan en esta app en este dispositivo.";
            case "Use reset buttons to clear progress from this phone.": return "Usa los botones de reinicio para borrar el progreso.";
            case "What the app does": return "Qué hace la app";
            case "Students choose a class, learn formulas with visual memory diagrams, answer short quizzes, and review weak formulas until they improve.": return "Los estudiantes eligen una clase, aprenden con diagramas, responden quizzes cortos y repasan fórmulas débiles.";
            case "Class progress includes revision from earlier classes and one-class-ahead challenge formulas.": return "El progreso incluye repaso de clases anteriores y retos de una clase superior.";
            case "Contact": return "Contacto";
            case "For privacy questions, contact: adilhayat@yahoo.com": return "Para preguntas de privacidad, contacta: adilhayat@yahoo.com";
            case "Reset progress?": return "¿Reiniciar progreso?";
            case "This only clears progress on this phone.": return "Esto solo borra el progreso de este teléfono.";
            case "Yes, reset progress": return "Sí, reiniciar progreso";
            case "Cancel": return "Cancelar";
            case "This only changes progress saved on this phone.": return "Esto solo cambia el progreso guardado en este teléfono.";
            case "Reset progress": return "Reiniciar progreso";
            case "Student name and selected class will stay the same.": return "El nombre y la clase seleccionada no cambiarán.";
            case "Reset": return "Reiniciar";
            case "Hint": return "Pista";
            case "Use this clue": return "Usa esta pista";
            case "Hints help memory. Try answering without looking again.": return "Las pistas ayudan a recordar. Intenta responder sin mirar otra vez.";
            case "Back to question": return "Volver a la pregunta";
            case "Quit quiz?": return "¿Salir del quiz?";
            case "Your current quiz answers will not be saved.": return "Tus respuestas actuales no se guardarán.";
            case "Leave this quiz, %s?": return "¿Salir de este quiz, %s?";
            case "The lesson will not be completed. You can restart it anytime from the same topic.": return "La lección no se completará. Puedes reiniciarla desde el mismo tema.";
            case "Continue quiz": return "Continuar quiz";
            case "Leave quiz and go back to topic": return "Salir y volver al tema";
            case "Quick revision": return "Repaso rápido";
            case "New for this level": return "Nuevo para este nivel";
            case "Challenge formula": return "Fórmula de reto";
            case "Fresh start": return "Comienzo nuevo";
            case "Warm-up level": return "Nivel de calentamiento";
            case "Steady practice": return "Práctica constante";
            case "Almost mastered": return "Casi dominado";
            case "Mistake review is waiting in this topic.": return "Hay repaso de errores en este tema.";
            case "Fresh topic. Open it when you are ready.": return "Tema nuevo. Ábrelo cuando estés listo.";
            case "Warm-up level. Keep building memory.": return "Nivel de calentamiento. Sigue construyendo memoria.";
            case "Practice level. The pattern is starting to stick.": return "Nivel de práctica. El patrón empieza a fijarse.";
            case "Confident level. A little review will finish it.": return "Nivel seguro. Un poco de repaso lo terminará.";
            case "Mastered level. Review anytime.": return "Nivel dominado. Repasa cuando quieras.";
            case "Introduced Class %d": return "Introducida en clase %d";
            case "General Practice": return "Práctica general";
            case "Start lesson": return "Empezar lección";
            case "Review again": return "Repasar otra vez";
            case "Excellent, %s!": return "¡Excelente, %s!";
            case "Correct - sharp memory, %s!": return "¡Correcto, buena memoria, %s!";
            case "Good work, %s. Keep going.": return "Buen trabajo, %s. Sigue así.";
            case "Nice! That formula is sticking.": return "¡Bien! Esa fórmula se está quedando.";
            case "Right answer. Fast and focused.": return "Respuesta correcta. Rápido y concentrado.";
            case "Not good enough yet, %s - focus.": return "Aún no es suficiente, %s. Concéntrate.";
            case "Careful, %s. Do not guess.": return "Cuidado, %s. No adivines.";
            case "Slow down, %s. Read the formula again.": return "Más despacio, %s. Lee la fórmula otra vez.";
            case "Wrong answer, but fix it now.": return "Respuesta incorrecta, pero corrígela ahora.";
            case "Focus. You can do better than guessing.": return "Concéntrate. Puedes hacerlo mejor que adivinar.";
            default: return null;
        }
    }

    private String trHi(String s) {
        switch (s) {
            case "Maths Formula Memorizer": return "गणित सूत्र मेमोराइज़र";
            case "Choose class": return "कक्षा चुनें";
            case "Tap a class to continue.": return "आगे बढ़ने के लिए कक्षा चुनें।";
            case "Pick your school level": return "अपना स्कूल स्तर चुनें";
            case "Each class includes revision from earlier levels and a small next-level challenge.": return "हर कक्षा में पुराने स्तर का रिवीजन और अगले स्तर की छोटी चुनौती है।";
            case "You can edit this later from the home screen.": return "आप इसे बाद में होम स्क्रीन से बदल सकते हैं।";
            case "What should I call you?": return "मैं आपको क्या बुलाऊँ?";
            case "Saved only on this phone. You can skip and start right away.": return "सिर्फ इस फोन पर सेव होगा। आप छोड़कर तुरंत शुरू कर सकते हैं।";
            case "Add a friendly name": return "अपना नाम लिखें";
            case "This helps the app make coaching messages feel personal.": return "इससे कोचिंग संदेश निजी लगते हैं।";
            case "Example: Ali": return "उदाहरण: Ali";
            case "Student name": return "विद्यार्थी का नाम";
            case "Saved only on this phone. No login. No internet.": return "सिर्फ इस फोन पर सेव। लॉगिन नहीं। इंटरनेट नहीं।";
            case "Save and start learning": return "सेव करें और सीखना शुरू करें";
            case "Skip for now": return "अभी छोड़ें";
            case "Choose language": return "भाषा चुनें";
            case "This changes app buttons and headings only. Maths content stays in English for accuracy.": return "यह केवल ऐप के बटन और शीर्षक बदलता है। सही गणित के लिए सामग्री अंग्रेज़ी में रहेगी।";
            case "App language": return "ऐप भाषा";
            case "You can change this anytime from Home.": return "आप इसे होम से कभी भी बदल सकते हैं।";
            case "Selected": return "चयनित";
            case "Back to class": return "कक्षा पर वापस";
            case "Back to home": return "होम पर वापस";
            case "Language": return "भाषा";
            case "Hi, %s": return "नमस्ते, %s";
            case "Edit": return "बदलें";
            case "Edit student name": return "विद्यार्थी का नाम बदलें";
            case "Change class": return "कक्षा बदलें";
            case "Class %s": return "कक्षा %s";
            case "%s cumulative progress": return "%s कुल प्रगति";
            case "Revision, new formulas, and one-step challenges move together here.": return "रिवीजन, नए सूत्र और छोटी चुनौतियाँ यहाँ साथ चलती हैं।";
            case "Start": return "शुरू";
            case "Practice": return "अभ्यास";
            case "Confident": return "आत्मविश्वास";
            case "Mastered": return "महारत";
            case "Reset %s progress": return "%s प्रगति रीसेट करें";
            case "Reset %s progress?": return "%s की प्रगति रीसेट करें?";
            case "This clears completed lessons and mistake marks for formulas visible in %s.": return "यह %s में दिखने वाले सूत्रों की पूरी lessons और mistakes हटाएगा।";
            case "Mistake review (%d)": return "गलती रिव्यू (%d)";
            case "Privacy & app info": return "प्राइवेसी और ऐप जानकारी";
            case "Choose a topic": return "विषय चुनें";
            case "Begin": return "शुरुआत";
            case "Learn": return "सीखें";
            case "Strong": return "मजबूत";
            case "Ready": return "तैयार";
            case "Reset topic": return "विषय रीसेट";
            case "Reset progress for %s": return "%s की प्रगति रीसेट करें";
            case "Reset %s?": return "%s रीसेट करें?";
            case "This clears completed lessons and mistake marks for this topic only.": return "यह केवल इस विषय की पूरी lessons और mistakes हटाएगा।";
            case "Review topic": return "विषय दोहराएँ";
            case "Open topic": return "विषय खोलें";
            case "Review %s": return "%s दोहराएँ";
            case "Open %s": return "%s खोलें";
            case "%s / 1-minute formula lessons": return "%s / 1 मिनट के सूत्र पाठ";
            case "%d / %d formulas completed": return "%d / %d सूत्र पूरे";
            case "No weak formulas in this topic yet.": return "इस विषय में अभी कमजोर सूत्र नहीं हैं।";
            case "%d formula(s) need review in this topic.": return "इस विषय में %d सूत्रों को रिव्यू चाहिए।";
            case "New for you": return "आपके लिए नया";
            case "Revision": return "रिवीजन";
            case "Challenge": return "चुनौती";
            case "Back to topic": return "विषय पर वापस";
            case "Home page": return "होम";
            case "Visual memory": return "दृश्य याददाश्त";
            case "Meaning": return "अर्थ";
            case "Example": return "उदाहरण";
            case "Start quiz": return "क्विज शुरू करें";
            case "Quick quiz": return "त्वरित क्विज";
            case "Question %d of %d": return "प्रश्न %d / %d";
            case "Score so far: %d correct": return "अब तक स्कोर: %d सही";
            case "Tap the best answer. You can use a hint or quit without saving progress.": return "सबसे अच्छा उत्तर चुनें। आप संकेत ले सकते हैं या बिना सेव किए छोड़ सकते हैं।";
            case "Show hint": return "संकेत दिखाएँ";
            case "Quit quiz": return "क्विज छोड़ें";
            case "Correct": return "सही";
            case "Focus - not correct": return "ध्यान दें - सही नहीं";
            case "Your answer: %s": return "आपका उत्तर: %s";
            case "Why it is right": return "यह सही क्यों है";
            case "Correct answer": return "सही उत्तर";
            case "Read slowly, %s. You are not allowed to guess carelessly.": return "धीरे पढ़ें, %s। बिना सोचे अनुमान न लगाएँ।";
            case "Finish lesson": return "पाठ समाप्त करें";
            case "Next question": return "अगला प्रश्न";
            case "Added to review": return "रिव्यू में जोड़ा गया";
            case "Score: %d / %d": return "स्कोर: %d / %d";
            case "Excellent. This formula is marked as mastered.": return "बहुत अच्छा। यह सूत्र mastered मार्क हो गया।";
            case "Good practice. This formula will appear in mistake review until it becomes easy.": return "अच्छा अभ्यास। यह सूत्र आसान होने तक गलती रिव्यू में आएगा।";
            case "One formula at a time builds long-term memory.": return "एक समय में एक सूत्र लंबे समय की याद बनाता है।";
            case "Next formula": return "अगला सूत्र";
            case "Review mistakes": return "गलतियाँ दोहराएँ";
            case "Home": return "होम";
            case "Mistake review": return "गलती रिव्यू";
            case "Practice formulas you forgot before.": return "वे सूत्र अभ्यास करें जो पहले भूल गए थे।";
            case "No weak formulas yet": return "अभी कोई कमजोर सूत्र नहीं";
            case "Mistakes will appear here automatically after quizzes.": return "क्विज के बाद गलतियाँ यहाँ अपने आप आएँगी।";
            case "Keep practicing one formula per day.": return "हर दिन एक सूत्र अभ्यास करें।";
            case "Review the hardest one first": return "सबसे कठिन को पहले दोहराएँ";
            case "Weak formulas are sorted by number of mistakes.": return "कमजोर सूत्र गलतियों की संख्या से क्रम में हैं।";
            case "Start mistake review": return "गलती रिव्यू शुरू करें";
            case "Mistakes: %d": return "गलतियाँ: %d";
            case "Practice again": return "फिर अभ्यास करें";
            case "Progress report": return "प्रगति रिपोर्ट";
            case "Overall progress: %d%%": return "कुल प्रगति: %d%%";
            case "Lessons": return "पाठ";
            case "Accuracy": return "सटीकता";
            case "Streak": return "स्ट्रीक";
            case "Student name and progress are saved only on this phone. No account and no internet needed.": return "नाम और प्रगति सिर्फ इस फोन पर सेव हैं। अकाउंट और इंटरनेट की जरूरत नहीं।";
            case "Topic progress": return "विषय प्रगति";
            case "%d / %d completed": return "%d / %d पूरे";
            case "Review %d weak formula(s)": return "%d कमजोर सूत्र दोहराएँ";
            case "Change student name": return "विद्यार्थी का नाम बदलें";
            case "Reset progress for this phone": return "इस फोन की प्रगति रीसेट करें";
            case "Clear facts for students and parents.": return "विद्यार्थियों और माता-पिता के लिए साफ जानकारी।";
            case "Offline by design": return "ऑफलाइन बनाया गया";
            case "Maths Formula Memorizer works without internet. It has no ads, no login, no Firebase, no analytics, and no server.": return "Maths Formula Memorizer बिना इंटरनेट चलता है। इसमें विज्ञापन, लॉगिन, Firebase, analytics या server नहीं है।";
            case "The app does not send student name, class, progress, or mistakes anywhere.": return "ऐप नाम, कक्षा, प्रगति या गलतियाँ कहीं नहीं भेजता।";
            case "Saved only on this phone": return "सिर्फ इस फोन पर सेव";
            case "Student name, selected class, completed formulas, quiz accuracy, streak, and mistake review are saved inside this app on this device.": return "नाम, कक्षा, पूरे सूत्र, accuracy, streak और mistake review इसी डिवाइस की ऐप में सेव हैं।";
            case "Use reset buttons to clear progress from this phone.": return "प्रगति साफ करने के लिए reset बटन इस्तेमाल करें।";
            case "What the app does": return "ऐप क्या करती है";
            case "Students choose a class, learn formulas with visual memory diagrams, answer short quizzes, and review weak formulas until they improve.": return "विद्यार्थी कक्षा चुनते हैं, diagrams से सूत्र सीखते हैं, छोटे quizzes करते हैं और कमजोर सूत्र दोहराते हैं।";
            case "Class progress includes revision from earlier classes and one-class-ahead challenge formulas.": return "कक्षा प्रगति में पिछली कक्षाओं का रिवीजन और एक कक्षा आगे की challenges शामिल हैं।";
            case "Contact": return "संपर्क";
            case "For privacy questions, contact: adilhayat@yahoo.com": return "प्राइवेसी सवालों के लिए संपर्क करें: adilhayat@yahoo.com";
            case "Reset progress?": return "प्रगति रीसेट करें?";
            case "This only clears progress on this phone.": return "यह सिर्फ इस फोन की प्रगति हटाता है।";
            case "Yes, reset progress": return "हाँ, प्रगति रीसेट करें";
            case "Cancel": return "रद्द करें";
            case "This only changes progress saved on this phone.": return "यह सिर्फ इस फोन पर सेव प्रगति बदलता है।";
            case "Reset progress": return "प्रगति रीसेट";
            case "Student name and selected class will stay the same.": return "नाम और चुनी हुई कक्षा वही रहेगी।";
            case "Reset": return "रीसेट";
            case "Hint": return "संकेत";
            case "Use this clue": return "यह संकेत इस्तेमाल करें";
            case "Hints help memory. Try answering without looking again.": return "संकेत याद में मदद करते हैं। फिर बिना देखे उत्तर दें।";
            case "Back to question": return "प्रश्न पर वापस";
            case "Quit quiz?": return "क्विज छोड़ें?";
            case "Your current quiz answers will not be saved.": return "आपके वर्तमान उत्तर सेव नहीं होंगे।";
            case "Leave this quiz, %s?": return "%s, यह क्विज छोड़ें?";
            case "The lesson will not be completed. You can restart it anytime from the same topic.": return "पाठ पूरा नहीं होगा। आप इसे उसी विषय से फिर शुरू कर सकते हैं।";
            case "Continue quiz": return "क्विज जारी रखें";
            case "Leave quiz and go back to topic": return "क्विज छोड़ें और विषय पर जाएँ";
            case "Quick revision": return "त्वरित रिवीजन";
            case "New for this level": return "इस स्तर के लिए नया";
            case "Challenge formula": return "चुनौती सूत्र";
            case "Fresh start": return "नई शुरुआत";
            case "Warm-up level": return "वार्म-अप स्तर";
            case "Steady practice": return "नियमित अभ्यास";
            case "Almost mastered": return "लगभग महारत";
            case "Mistake review is waiting in this topic.": return "इस विषय में गलती रिव्यू बाकी है।";
            case "Fresh topic. Open it when you are ready.": return "नया विषय। तैयार हों तो खोलें।";
            case "Warm-up level. Keep building memory.": return "वार्म-अप स्तर। याददाश्त बनाते रहें।";
            case "Practice level. The pattern is starting to stick.": return "अभ्यास स्तर। पैटर्न याद होने लगा है।";
            case "Confident level. A little review will finish it.": return "आत्मविश्वास स्तर। थोड़ा रिव्यू पूरा कर देगा।";
            case "Mastered level. Review anytime.": return "महारत स्तर। कभी भी दोहराएँ।";
            case "Introduced Class %d": return "कक्षा %d में शुरू";
            case "General Practice": return "सामान्य अभ्यास";
            case "Start lesson": return "पाठ शुरू करें";
            case "Review again": return "फिर दोहराएँ";
            default: return null;
        }
    }

    private String trUr(String s) {
        switch (s) {
            case "Maths Formula Memorizer": return "ریاضی فارمولا میمورائزر";
            case "Choose class": return "کلاس منتخب کریں";
            case "Tap a class to continue.": return "جاری رکھنے کے لیے کلاس پر ٹیپ کریں۔";
            case "Pick your school level": return "اپنا اسکول لیول منتخب کریں";
            case "Each class includes revision from earlier levels and a small next-level challenge.": return "ہر کلاس میں پچھلے لیولز کی دہرائی اور اگلے لیول کا چھوٹا چیلنج شامل ہے۔";
            case "You can edit this later from the home screen.": return "آپ اسے بعد میں ہوم اسکرین سے بدل سکتے ہیں۔";
            case "What should I call you?": return "میں آپ کو کیا کہوں؟";
            case "Saved only on this phone. You can skip and start right away.": return "صرف اس فون پر محفوظ ہوگا۔ آپ چھوڑ کر فوراً شروع کر سکتے ہیں۔";
            case "Add a friendly name": return "اپنا نام لکھیں";
            case "This helps the app make coaching messages feel personal.": return "اس سے کوچنگ پیغامات زیادہ ذاتی لگتے ہیں۔";
            case "Example: Ali": return "مثال: Ali";
            case "Student name": return "طالب علم کا نام";
            case "Saved only on this phone. No login. No internet.": return "صرف اس فون پر محفوظ۔ لاگ اِن نہیں۔ انٹرنیٹ نہیں۔";
            case "Save and start learning": return "محفوظ کریں اور سیکھنا شروع کریں";
            case "Skip for now": return "ابھی چھوڑ دیں";
            case "Choose language": return "زبان منتخب کریں";
            case "This changes app buttons and headings only. Maths content stays in English for accuracy.": return "یہ صرف بٹن اور سرخیاں بدلتا ہے۔ درستگی کے لیے ریاضی کا مواد انگریزی میں رہے گا۔";
            case "App language": return "ایپ کی زبان";
            case "You can change this anytime from Home.": return "آپ اسے ہوم سے کبھی بھی بدل سکتے ہیں۔";
            case "Selected": return "منتخب";
            case "Back to class": return "کلاس پر واپس";
            case "Back to home": return "ہوم پر واپس";
            case "Language": return "زبان";
            case "Hi, %s": return "سلام، %s";
            case "Edit": return "تبدیل";
            case "Edit student name": return "طالب علم کا نام تبدیل کریں";
            case "Change class": return "کلاس تبدیل کریں";
            case "Class %s": return "کلاس %s";
            case "%s cumulative progress": return "%s مجموعی پیش رفت";
            case "Revision, new formulas, and one-step challenges move together here.": return "دہرائی، نئے فارمولے اور چھوٹے چیلنج یہاں ساتھ چلتے ہیں۔";
            case "Start": return "شروع";
            case "Practice": return "مشق";
            case "Confident": return "اعتماد";
            case "Mastered": return "ماہر";
            case "Reset %s progress": return "%s کی پیش رفت ری سیٹ";
            case "Reset %s progress?": return "%s کی پیش رفت ری سیٹ کریں؟";
            case "This clears completed lessons and mistake marks for formulas visible in %s.": return "یہ %s میں نظر آنے والے فارمولوں کی مکمل lessons اور غلطیاں صاف کرے گا۔";
            case "Mistake review (%d)": return "غلطی ریویو (%d)";
            case "Privacy & app info": return "پرائیویسی اور ایپ معلومات";
            case "Choose a topic": return "موضوع منتخب کریں";
            case "Begin": return "آغاز";
            case "Learn": return "سیکھیں";
            case "Strong": return "مضبوط";
            case "Ready": return "تیار";
            case "Reset topic": return "موضوع ری سیٹ";
            case "Reset progress for %s": return "%s کی پیش رفت ری سیٹ کریں";
            case "Reset %s?": return "%s ری سیٹ کریں؟";
            case "This clears completed lessons and mistake marks for this topic only.": return "یہ صرف اس موضوع کی مکمل lessons اور غلطیاں صاف کرے گا۔";
            case "Review topic": return "موضوع دہرائیں";
            case "Open topic": return "موضوع کھولیں";
            case "Review %s": return "%s دہرائیں";
            case "Open %s": return "%s کھولیں";
            case "%s / 1-minute formula lessons": return "%s / ایک منٹ کے فارمولا اسباق";
            case "%d / %d formulas completed": return "%d / %d فارمولے مکمل";
            case "No weak formulas in this topic yet.": return "اس موضوع میں ابھی کمزور فارمولے نہیں ہیں۔";
            case "%d formula(s) need review in this topic.": return "اس موضوع میں %d فارمولوں کو ریویو چاہیے۔";
            case "New for you": return "آپ کے لیے نیا";
            case "Revision": return "دہرائی";
            case "Challenge": return "چیلنج";
            case "Back to topic": return "موضوع پر واپس";
            case "Home page": return "ہوم";
            case "Visual memory": return "بصری یادداشت";
            case "Meaning": return "مطلب";
            case "Example": return "مثال";
            case "Start quiz": return "کوئز شروع کریں";
            case "Quick quiz": return "فوری کوئز";
            case "Question %d of %d": return "سوال %d از %d";
            case "Score so far: %d correct": return "اب تک اسکور: %d درست";
            case "Tap the best answer. You can use a hint or quit without saving progress.": return "بہترین جواب منتخب کریں۔ آپ اشارہ لے سکتے ہیں یا محفوظ کیے بغیر نکل سکتے ہیں۔";
            case "Show hint": return "اشارہ دکھائیں";
            case "Quit quiz": return "کوئز چھوڑیں";
            case "Correct": return "درست";
            case "Focus - not correct": return "توجہ دیں - درست نہیں";
            case "Your answer: %s": return "آپ کا جواب: %s";
            case "Why it is right": return "یہ درست کیوں ہے";
            case "Correct answer": return "درست جواب";
            case "Read slowly, %s. You are not allowed to guess carelessly.": return "آہستہ پڑھیں، %s۔ بے دھیانی سے اندازہ نہ لگائیں۔";
            case "Finish lesson": return "سبق ختم کریں";
            case "Next question": return "اگلا سوال";
            case "Added to review": return "ریویو میں شامل";
            case "Score: %d / %d": return "اسکور: %d / %d";
            case "Excellent. This formula is marked as mastered.": return "بہت خوب۔ یہ فارمولا mastered نشان زد ہو گیا۔";
            case "Good practice. This formula will appear in mistake review until it becomes easy.": return "اچھی مشق۔ یہ فارمولا آسان ہونے تک غلطی ریویو میں آئے گا۔";
            case "One formula at a time builds long-term memory.": return "ایک وقت میں ایک فارمولا لمبی یادداشت بناتا ہے۔";
            case "Next formula": return "اگلا فارمولا";
            case "Review mistakes": return "غلطیاں دہرائیں";
            case "Home": return "ہوم";
            case "Mistake review": return "غلطی ریویو";
            case "Practice formulas you forgot before.": return "وہ فارمولے مشق کریں جو پہلے بھول گئے تھے۔";
            case "No weak formulas yet": return "ابھی کمزور فارمولے نہیں";
            case "Mistakes will appear here automatically after quizzes.": return "کوئز کے بعد غلطیاں یہاں خود آ جائیں گی۔";
            case "Keep practicing one formula per day.": return "ہر دن ایک فارمولا مشق کریں۔";
            case "Review the hardest one first": return "سب سے مشکل پہلے دہرائیں";
            case "Weak formulas are sorted by number of mistakes.": return "کمزور فارمولے غلطیوں کی تعداد سے ترتیب دیے گئے ہیں۔";
            case "Start mistake review": return "غلطی ریویو شروع کریں";
            case "Mistakes: %d": return "غلطیاں: %d";
            case "Practice again": return "دوبارہ مشق";
            case "Progress report": return "پیش رفت رپورٹ";
            case "Overall progress: %d%%": return "کل پیش رفت: %d%%";
            case "Lessons": return "اسباق";
            case "Accuracy": return "درستگی";
            case "Streak": return "سلسلہ";
            case "Student name and progress are saved only on this phone. No account and no internet needed.": return "نام اور پیش رفت صرف اس فون پر محفوظ ہیں۔ اکاؤنٹ اور انٹرنیٹ کی ضرورت نہیں۔";
            case "Topic progress": return "موضوع کی پیش رفت";
            case "%d / %d completed": return "%d / %d مکمل";
            case "Review %d weak formula(s)": return "%d کمزور فارمولے دہرائیں";
            case "Change student name": return "طالب علم کا نام تبدیل کریں";
            case "Reset progress for this phone": return "اس فون کی پیش رفت ری سیٹ کریں";
            case "Clear facts for students and parents.": return "طلبہ اور والدین کے لیے صاف معلومات۔";
            case "Offline by design": return "آف لائن ڈیزائن";
            case "Maths Formula Memorizer works without internet. It has no ads, no login, no Firebase, no analytics, and no server.": return "Maths Formula Memorizer انٹرنیٹ کے بغیر چلتا ہے۔ اس میں ads، login، Firebase، analytics یا server نہیں۔";
            case "The app does not send student name, class, progress, or mistakes anywhere.": return "ایپ نام، کلاس، پیش رفت یا غلطیاں کہیں نہیں بھیجتی۔";
            case "Saved only on this phone": return "صرف اس فون پر محفوظ";
            case "Student name, selected class, completed formulas, quiz accuracy, streak, and mistake review are saved inside this app on this device.": return "نام، منتخب کلاس، مکمل فارمولے، quiz accuracy، streak اور mistake review اسی ڈیوائس میں محفوظ ہیں۔";
            case "Use reset buttons to clear progress from this phone.": return "پیش رفت صاف کرنے کے لیے reset بٹن استعمال کریں۔";
            case "What the app does": return "ایپ کیا کرتی ہے";
            case "Students choose a class, learn formulas with visual memory diagrams, answer short quizzes, and review weak formulas until they improve.": return "طلبہ کلاس منتخب کرتے ہیں، diagrams کے ساتھ فارمولے سیکھتے ہیں، مختصر quizzes دیتے ہیں اور کمزور فارمولے دہراتے ہیں۔";
            case "Class progress includes revision from earlier classes and one-class-ahead challenge formulas.": return "کلاس پیش رفت میں پچھلی کلاسوں کی دہرائی اور ایک کلاس آگے کے چیلنج فارمولے شامل ہیں۔";
            case "Contact": return "رابطہ";
            case "For privacy questions, contact: adilhayat@yahoo.com": return "پرائیویسی سوالات کے لیے رابطہ: adilhayat@yahoo.com";
            case "Reset progress?": return "پیش رفت ری سیٹ کریں؟";
            case "This only clears progress on this phone.": return "یہ صرف اس فون کی پیش رفت صاف کرتا ہے۔";
            case "Yes, reset progress": return "ہاں، پیش رفت ری سیٹ کریں";
            case "Cancel": return "منسوخ";
            case "This only changes progress saved on this phone.": return "یہ صرف اس فون پر محفوظ پیش رفت بدلتا ہے۔";
            case "Reset progress": return "پیش رفت ری سیٹ";
            case "Student name and selected class will stay the same.": return "نام اور منتخب کلاس وہی رہیں گے۔";
            case "Reset": return "ری سیٹ";
            case "Hint": return "اشارہ";
            case "Use this clue": return "یہ اشارہ استعمال کریں";
            case "Hints help memory. Try answering without looking again.": return "اشارے یادداشت میں مدد کرتے ہیں۔ دوبارہ دیکھے بغیر جواب دیں۔";
            case "Back to question": return "سوال پر واپس";
            case "Quit quiz?": return "کوئز چھوڑیں؟";
            case "Your current quiz answers will not be saved.": return "آپ کے موجودہ جوابات محفوظ نہیں ہوں گے۔";
            case "Leave this quiz, %s?": return "%s، یہ کوئز چھوڑیں؟";
            case "The lesson will not be completed. You can restart it anytime from the same topic.": return "سبق مکمل نہیں ہوگا۔ آپ اسے اسی موضوع سے دوبارہ شروع کر سکتے ہیں۔";
            case "Continue quiz": return "کوئز جاری رکھیں";
            case "Leave quiz and go back to topic": return "کوئز چھوڑ کر موضوع پر واپس جائیں";
            case "Quick revision": return "فوری دہرائی";
            case "New for this level": return "اس لیول کے لیے نیا";
            case "Challenge formula": return "چیلنج فارمولا";
            case "Fresh start": return "نئی شروعات";
            case "Warm-up level": return "وارم اَپ لیول";
            case "Steady practice": return "مسلسل مشق";
            case "Almost mastered": return "تقریباً ماہر";
            case "Mistake review is waiting in this topic.": return "اس موضوع میں غلطی ریویو موجود ہے۔";
            case "Fresh topic. Open it when you are ready.": return "نیا موضوع۔ تیار ہوں تو کھولیں۔";
            case "Warm-up level. Keep building memory.": return "وارم اَپ لیول۔ یادداشت بناتے رہیں۔";
            case "Practice level. The pattern is starting to stick.": return "مشق لیول۔ pattern یاد ہونے لگا ہے۔";
            case "Confident level. A little review will finish it.": return "اعتماد لیول۔ تھوڑی دہرائی اسے مکمل کر دے گی۔";
            case "Mastered level. Review anytime.": return "ماہر لیول۔ کبھی بھی دہرائیں۔";
            case "Introduced Class %d": return "کلاس %d میں شروع";
            case "General Practice": return "عام مشق";
            case "Start lesson": return "سبق شروع کریں";
            case "Review again": return "دوبارہ دہرائیں";
            default: return null;
        }
    }


    private String trAr(String s) {
        switch (s) {
            case "Maths Formula Memorizer": return "حافظ صيغ الرياضيات";
            case "Choose class": return "اختر الصف";
            case "Tap a class to continue.": return "اضغط على الصف للمتابعة.";
            case "Pick your school level": return "اختر مستواك الدراسي";
            case "Each class includes revision from earlier levels and a small next-level challenge.": return "كل صف يتضمن مراجعة للمستويات السابقة وتحديا صغيرا للمستوى التالي.";
            case "You can edit this later from the home screen.": return "يمكنك تعديل ذلك لاحقا من الشاشة الرئيسية.";
            case "What should I call you?": return "بماذا أناديك؟";
            case "Saved only on this phone. You can skip and start right away.": return "يحفظ فقط على هذا الهاتف. يمكنك التخطي والبدء الآن.";
            case "Add a friendly name": return "أضف اسما ودودا";
            case "This helps the app make coaching messages feel personal.": return "يساعد هذا في جعل رسائل التدريب شخصية أكثر.";
            case "Example: Ali": return "مثال: Ali";
            case "Student name": return "اسم الطالب";
            case "Saved only on this phone. No login. No internet.": return "محفوظ فقط على هذا الهاتف. لا تسجيل دخول. لا إنترنت.";
            case "Save and start learning": return "احفظ وابدأ التعلم";
            case "Skip for now": return "تخطي الآن";
            case "Choose language": return "اختر اللغة";
            case "This changes app buttons and headings only. Maths content stays in English for accuracy.": return "هذا يغير أزرار التطبيق والعناوين فقط. يبقى محتوى الرياضيات بالإنجليزية للدقة.";
            case "App language": return "لغة التطبيق";
            case "You can change this anytime from Home.": return "يمكنك تغيير ذلك في أي وقت من الرئيسية.";
            case "Selected": return "محدد";
            case "Back to class": return "العودة إلى الصف";
            case "Back to home": return "العودة للرئيسية";
            case "Language": return "اللغة";
            case "Hi, %s": return "مرحبا، %s";
            case "Edit": return "تعديل";
            case "Edit student name": return "تعديل اسم الطالب";
            case "Change class": return "تغيير الصف";
            case "Class %s": return "الصف %s";
            case "%s cumulative progress": return "التقدم التراكمي: %s";
            case "Revision, new formulas, and one-step challenges move together here.": return "المراجعة والصيغ الجديدة والتحديات القصيرة تتحرك معا هنا.";
            case "Start": return "البداية";
            case "Practice": return "تدريب";
            case "Confident": return "واثق";
            case "Mastered": return "متقن";
            case "Reset %s progress": return "إعادة ضبط تقدم %s";
            case "Reset %s progress?": return "إعادة ضبط تقدم %s؟";
            case "This clears completed lessons and mistake marks for formulas visible in %s.": return "سيحذف هذا الدروس المكتملة وعلامات الأخطاء للصيغ الظاهرة في %s.";
            case "Mistake review (%d)": return "مراجعة الأخطاء (%d)";
            case "Privacy & app info": return "الخصوصية ومعلومات التطبيق";
            case "Choose a topic": return "اختر موضوعا";
            case "Begin": return "ابدأ";
            case "Learn": return "تعلم";
            case "Strong": return "قوي";
            case "Ready": return "جاهز";
            case "Reset topic": return "إعادة ضبط الموضوع";
            case "Reset progress for %s": return "إعادة ضبط تقدم %s";
            case "Reset %s?": return "إعادة ضبط %s؟";
            case "This clears completed lessons and mistake marks for this topic only.": return "يحذف هذا الدروس المكتملة والأخطاء لهذا الموضوع فقط.";
            case "Review topic": return "مراجعة الموضوع";
            case "Open topic": return "فتح الموضوع";
            case "Review %s": return "مراجعة %s";
            case "Open %s": return "فتح %s";
            case "%s / 1-minute formula lessons": return "%s / دروس صيغ لمدة دقيقة";
            case "%d / %d formulas completed": return "اكتمل %d / %d من الصيغ";
            case "No weak formulas in this topic yet.": return "لا توجد صيغ ضعيفة في هذا الموضوع بعد.";
            case "%d formula(s) need review in this topic.": return "%d صيغة تحتاج إلى مراجعة في هذا الموضوع.";
            case "New for you": return "جديد لك";
            case "Revision": return "مراجعة";
            case "Challenge": return "تحد";
            case "Back to topic": return "العودة للموضوع";
            case "Home page": return "الرئيسية";
            case "Visual memory": return "ذاكرة بصرية";
            case "Meaning": return "المعنى";
            case "Example": return "مثال";
            case "Start quiz": return "ابدأ الاختبار";
            case "Quick quiz": return "اختبار سريع";
            case "Question %d of %d": return "السؤال %d من %d";
            case "Score so far: %d correct": return "النتيجة حتى الآن: %d صحيح";
            case "Tap the best answer. You can use a hint or quit without saving progress.": return "اضغط أفضل إجابة. يمكنك استخدام تلميح أو الخروج دون حفظ التقدم.";
            case "Show hint": return "إظهار تلميح";
            case "Quit quiz": return "إنهاء الاختبار";
            case "Correct": return "صحيح";
            case "Focus - not correct": return "ركز - غير صحيح";
            case "Your answer: %s": return "إجابتك: %s";
            case "Why it is right": return "لماذا هذا صحيح";
            case "Correct answer": return "الإجابة الصحيحة";
            case "Read slowly, %s. You are not allowed to guess carelessly.": return "اقرأ ببطء يا %s. لا تخمن بلا تركيز.";
            case "Finish lesson": return "إنهاء الدرس";
            case "Next question": return "السؤال التالي";
            case "Added to review": return "أضيف إلى المراجعة";
            case "Score: %d / %d": return "النتيجة: %d / %d";
            case "Excellent. This formula is marked as mastered.": return "ممتاز. تم وضع هذه الصيغة كمتقنة.";
            case "Good practice. This formula will appear in mistake review until it becomes easy.": return "تدريب جيد. ستظهر هذه الصيغة في مراجعة الأخطاء حتى تصبح سهلة.";
            case "One formula at a time builds long-term memory.": return "صيغة واحدة في كل مرة تبني ذاكرة طويلة.";
            case "Next formula": return "الصيغة التالية";
            case "Review mistakes": return "مراجعة الأخطاء";
            case "Home": return "الرئيسية";
            case "Mistake review": return "مراجعة الأخطاء";
            case "Practice formulas you forgot before.": return "تدرب على الصيغ التي نسيتها من قبل.";
            case "No weak formulas yet": return "لا توجد صيغ ضعيفة بعد";
            case "Mistakes will appear here automatically after quizzes.": return "ستظهر الأخطاء هنا تلقائيا بعد الاختبارات.";
            case "Keep practicing one formula per day.": return "استمر في تدريب صيغة واحدة يوميا.";
            case "Review the hardest one first": return "راجع الأصعب أولا";
            case "Weak formulas are sorted by number of mistakes.": return "ترتب الصيغ الضعيفة حسب عدد الأخطاء.";
            case "Start mistake review": return "بدء مراجعة الأخطاء";
            case "Mistakes: %d": return "الأخطاء: %d";
            case "Practice again": return "تدرب مرة أخرى";
            case "Progress report": return "تقرير التقدم";
            case "Overall progress: %d%%": return "التقدم الكلي: %d%%";
            case "Lessons": return "الدروس";
            case "Accuracy": return "الدقة";
            case "Streak": return "السلسلة";
            case "Student name and progress are saved only on this phone. No account and no internet needed.": return "اسم الطالب والتقدم محفوظان فقط على هذا الهاتف. لا حساب ولا إنترنت.";
            case "Topic progress": return "تقدم الموضوع";
            case "%d / %d completed": return "اكتمل %d / %d";
            case "Review %d weak formula(s)": return "مراجعة %d صيغة ضعيفة";
            case "Change student name": return "تغيير اسم الطالب";
            case "Reset progress for this phone": return "إعادة ضبط تقدم هذا الهاتف";
            case "Clear facts for students and parents.": return "حقائق واضحة للطلاب والآباء.";
            case "Offline by design": return "مصمم للعمل دون إنترنت";
            case "Maths Formula Memorizer works without internet. It has no ads, no login, no Firebase, no analytics, and no server.": return "يعمل Maths Formula Memorizer دون إنترنت. لا يحتوي على إعلانات أو تسجيل دخول أو Firebase أو تحليلات أو خادم.";
            case "The app does not send student name, class, progress, or mistakes anywhere.": return "لا يرسل التطبيق اسم الطالب أو الصف أو التقدم أو الأخطاء إلى أي مكان.";
            case "Saved only on this phone": return "محفوظ فقط على هذا الهاتف";
            case "Student name, selected class, completed formulas, quiz accuracy, streak, and mistake review are saved inside this app on this device.": return "اسم الطالب والصف والصيغ المكتملة ودقة الاختبار والسلسلة ومراجعة الأخطاء تحفظ داخل التطبيق على هذا الجهاز.";
            case "Use reset buttons to clear progress from this phone.": return "استخدم أزرار الإعادة لمسح التقدم من هذا الهاتف.";
            case "What the app does": return "ماذا يفعل التطبيق";
            case "Students choose a class, learn formulas with visual memory diagrams, answer short quizzes, and review weak formulas until they improve.": return "يختار الطلاب الصف، ويتعلمون الصيغ برسومات بصرية، ويجيبون على اختبارات قصيرة، ويراجعون الصيغ الضعيفة.";
            case "Class progress includes revision from earlier classes and one-class-ahead challenge formulas.": return "يتضمن تقدم الصف مراجعة للصفوف السابقة وتحديات من صف واحد أعلى.";
            case "Contact": return "التواصل";
            case "For privacy questions, contact: adilhayat@yahoo.com": return "لأسئلة الخصوصية تواصل عبر: adilhayat@yahoo.com";
            case "Reset progress?": return "إعادة ضبط التقدم؟";
            case "This only clears progress on this phone.": return "هذا يمسح التقدم على هذا الهاتف فقط.";
            case "Yes, reset progress": return "نعم، أعد ضبط التقدم";
            case "Cancel": return "إلغاء";
            case "This only changes progress saved on this phone.": return "هذا يغير فقط التقدم المحفوظ على هذا الهاتف.";
            case "Reset progress": return "إعادة ضبط التقدم";
            case "Student name and selected class will stay the same.": return "سيبقى اسم الطالب والصف كما هما.";
            case "Reset": return "إعادة ضبط";
            case "Hint": return "تلميح";
            case "Use this clue": return "استخدم هذا التلميح";
            case "Hints help memory. Try answering without looking again.": return "التلميحات تساعد الذاكرة. حاول الإجابة دون النظر مرة أخرى.";
            case "Back to question": return "العودة للسؤال";
            case "Quit quiz?": return "إنهاء الاختبار؟";
            case "Your current quiz answers will not be saved.": return "لن تحفظ إجابات الاختبار الحالية.";
            case "Leave this quiz, %s?": return "هل تريد مغادرة الاختبار يا %s؟";
            case "The lesson will not be completed. You can restart it anytime from the same topic.": return "لن يكتمل الدرس. يمكنك بدءه مرة أخرى من نفس الموضوع.";
            case "Continue quiz": return "متابعة الاختبار";
            case "Leave quiz and go back to topic": return "مغادرة الاختبار والعودة للموضوع";
            case "Quick revision": return "مراجعة سريعة";
            case "New for this level": return "جديد لهذا المستوى";
            case "Challenge formula": return "صيغة تحد";
            case "Fresh start": return "بداية جديدة";
            case "Warm-up level": return "مستوى الإحماء";
            case "Steady practice": return "تدريب ثابت";
            case "Almost mastered": return "قريب من الإتقان";
            case "Mistake review is waiting in this topic.": return "مراجعة الأخطاء تنتظر في هذا الموضوع.";
            case "Fresh topic. Open it when you are ready.": return "موضوع جديد. افتحه عندما تكون جاهزا.";
            case "Warm-up level. Keep building memory.": return "مستوى الإحماء. واصل بناء الذاكرة.";
            case "Practice level. The pattern is starting to stick.": return "مستوى التدريب. بدأ النمط يثبت.";
            case "Confident level. A little review will finish it.": return "مستوى الثقة. مراجعة قصيرة تنهيه.";
            case "Mastered level. Review anytime.": return "مستوى متقن. راجع في أي وقت.";
            case "Introduced Class %d": return "تم تقديمه في الصف %d";
            case "General Practice": return "تدريب عام";
            case "Start lesson": return "ابدأ الدرس";
            case "Review again": return "راجع مرة أخرى";
            default: return null;
        }
    }

    private String trFr(String s) {
        switch (s) {
            case "Maths Formula Memorizer": return "Mémo des formules de maths";
            case "Choose class": return "Choisis la classe";
            case "Tap a class to continue.": return "Touche une classe pour continuer.";
            case "Pick your school level": return "Choisis ton niveau scolaire";
            case "Each class includes revision from earlier levels and a small next-level challenge.": return "Chaque classe inclut une révision des niveaux précédents et un petit défi du niveau suivant.";
            case "You can edit this later from the home screen.": return "Tu peux modifier cela plus tard depuis l'accueil.";
            case "What should I call you?": return "Comment dois-je t'appeler ?";
            case "Saved only on this phone. You can skip and start right away.": return "Enregistré seulement sur ce téléphone. Tu peux passer et commencer.";
            case "Add a friendly name": return "Ajoute un prénom";
            case "This helps the app make coaching messages feel personal.": return "Cela rend les messages d'encouragement plus personnels.";
            case "Example: Ali": return "Exemple : Ali";
            case "Student name": return "Nom de l'élève";
            case "Saved only on this phone. No login. No internet.": return "Enregistré seulement sur ce téléphone. Pas de connexion. Pas d'internet.";
            case "Save and start learning": return "Enregistrer et commencer";
            case "Skip for now": return "Passer pour l'instant";
            case "Choose language": return "Choisir la langue";
            case "This changes app buttons and headings only. Maths content stays in English for accuracy.": return "Cela change seulement les boutons et titres. Le contenu de maths reste en anglais pour la précision.";
            case "App language": return "Langue de l'app";
            case "You can change this anytime from Home.": return "Tu peux changer cela à tout moment depuis l'accueil.";
            case "Selected": return "Sélectionné";
            case "Back to class": return "Retour à la classe";
            case "Back to home": return "Retour à l'accueil";
            case "Language": return "Langue";
            case "Hi, %s": return "Salut, %s";
            case "Edit": return "Modifier";
            case "Edit student name": return "Modifier le nom de l'élève";
            case "Change class": return "Changer la classe";
            case "Class %s": return "Classe %s";
            case "%s cumulative progress": return "Progression cumulative de %s";
            case "Revision, new formulas, and one-step challenges move together here.": return "Révisions, nouvelles formules et petits défis avancent ensemble ici.";
            case "Start": return "Début";
            case "Practice": return "Pratique";
            case "Confident": return "Confiant";
            case "Mastered": return "Maîtrisé";
            case "Reset %s progress": return "Réinitialiser la progression de %s";
            case "Reset %s progress?": return "Réinitialiser la progression de %s ?";
            case "This clears completed lessons and mistake marks for formulas visible in %s.": return "Cela efface les leçons terminées et les erreurs des formules visibles en %s.";
            case "Mistake review (%d)": return "Révision des erreurs (%d)";
            case "Privacy & app info": return "Confidentialité et infos";
            case "Choose a topic": return "Choisis un sujet";
            case "Begin": return "Début";
            case "Learn": return "Apprendre";
            case "Strong": return "Solide";
            case "Ready": return "Prêt";
            case "Reset topic": return "Réinitialiser le sujet";
            case "Reset progress for %s": return "Réinitialiser la progression de %s";
            case "Reset %s?": return "Réinitialiser %s ?";
            case "This clears completed lessons and mistake marks for this topic only.": return "Cela efface les leçons terminées et les erreurs de ce sujet seulement.";
            case "Review topic": return "Réviser le sujet";
            case "Open topic": return "Ouvrir le sujet";
            case "Review %s": return "Réviser %s";
            case "Open %s": return "Ouvrir %s";
            case "%s / 1-minute formula lessons": return "%s / leçons de formules d'une minute";
            case "%d / %d formulas completed": return "%d / %d formules terminées";
            case "No weak formulas in this topic yet.": return "Aucune formule faible dans ce sujet pour l'instant.";
            case "%d formula(s) need review in this topic.": return "%d formule(s) à revoir dans ce sujet.";
            case "New for you": return "Nouveau pour toi";
            case "Revision": return "Révision";
            case "Challenge": return "Défi";
            case "Back to topic": return "Retour au sujet";
            case "Home page": return "Accueil";
            case "Visual memory": return "Mémoire visuelle";
            case "Meaning": return "Sens";
            case "Example": return "Exemple";
            case "Start quiz": return "Commencer le quiz";
            case "Quick quiz": return "Quiz rapide";
            case "Question %d of %d": return "Question %d sur %d";
            case "Score so far: %d correct": return "Score actuel : %d correctes";
            case "Tap the best answer. You can use a hint or quit without saving progress.": return "Touche la meilleure réponse. Tu peux utiliser un indice ou quitter sans enregistrer.";
            case "Show hint": return "Afficher l'indice";
            case "Quit quiz": return "Quitter le quiz";
            case "Correct": return "Correct";
            case "Focus - not correct": return "Concentre-toi - incorrect";
            case "Your answer: %s": return "Ta réponse : %s";
            case "Why it is right": return "Pourquoi c'est correct";
            case "Correct answer": return "Bonne réponse";
            case "Read slowly, %s. You are not allowed to guess carelessly.": return "Lis lentement, %s. Ne devine pas au hasard.";
            case "Finish lesson": return "Terminer la leçon";
            case "Next question": return "Question suivante";
            case "Added to review": return "Ajouté à la révision";
            case "Score: %d / %d": return "Score : %d / %d";
            case "Excellent. This formula is marked as mastered.": return "Excellent. Cette formule est marquée comme maîtrisée.";
            case "Good practice. This formula will appear in mistake review until it becomes easy.": return "Bonne pratique. Cette formule restera dans la révision des erreurs jusqu'à devenir facile.";
            case "One formula at a time builds long-term memory.": return "Une formule à la fois construit la mémoire durable.";
            case "Next formula": return "Formule suivante";
            case "Review mistakes": return "Réviser les erreurs";
            case "Home": return "Accueil";
            case "Mistake review": return "Révision des erreurs";
            case "Practice formulas you forgot before.": return "Pratique les formules que tu as oubliées.";
            case "No weak formulas yet": return "Aucune formule faible pour l'instant";
            case "Mistakes will appear here automatically after quizzes.": return "Les erreurs apparaîtront ici automatiquement après les quiz.";
            case "Keep practicing one formula per day.": return "Continue avec une formule par jour.";
            case "Review the hardest one first": return "Révise d'abord la plus difficile";
            case "Weak formulas are sorted by number of mistakes.": return "Les formules faibles sont triées par nombre d'erreurs.";
            case "Start mistake review": return "Commencer la révision des erreurs";
            case "Mistakes: %d": return "Erreurs : %d";
            case "Practice again": return "Repratiquer";
            case "Progress report": return "Rapport de progression";
            case "Overall progress: %d%%": return "Progression totale : %d%%";
            case "Lessons": return "Leçons";
            case "Accuracy": return "Précision";
            case "Streak": return "Série";
            case "Student name and progress are saved only on this phone. No account and no internet needed.": return "Le nom et la progression sont enregistrés seulement sur ce téléphone. Pas de compte ni d'internet.";
            case "Topic progress": return "Progression par sujet";
            case "%d / %d completed": return "%d / %d terminées";
            case "Review %d weak formula(s)": return "Réviser %d formule(s) faibles";
            case "Change student name": return "Changer le nom de l'élève";
            case "Reset progress for this phone": return "Réinitialiser la progression de ce téléphone";
            case "Clear facts for students and parents.": return "Informations claires pour élèves et parents.";
            case "Offline by design": return "Conçu hors ligne";
            case "Maths Formula Memorizer works without internet. It has no ads, no login, no Firebase, no analytics, and no server.": return "Maths Formula Memorizer fonctionne sans internet. Pas de pubs, pas de connexion, pas de Firebase, pas d'analytics et pas de serveur.";
            case "The app does not send student name, class, progress, or mistakes anywhere.": return "L'app n'envoie nulle part le nom, la classe, la progression ou les erreurs.";
            case "Saved only on this phone": return "Enregistré seulement sur ce téléphone";
            case "Student name, selected class, completed formulas, quiz accuracy, streak, and mistake review are saved inside this app on this device.": return "Le nom, la classe, les formules terminées, la précision, la série et les erreurs sont enregistrés dans cette app sur cet appareil.";
            case "Use reset buttons to clear progress from this phone.": return "Utilise les boutons de réinitialisation pour effacer la progression.";
            case "What the app does": return "Ce que fait l'app";
            case "Students choose a class, learn formulas with visual memory diagrams, answer short quizzes, and review weak formulas until they improve.": return "Les élèves choisissent une classe, apprennent avec des diagrammes, répondent à de courts quiz et révisent les formules faibles.";
            case "Class progress includes revision from earlier classes and one-class-ahead challenge formulas.": return "La progression inclut les révisions des classes précédentes et des défis d'une classe au-dessus.";
            case "Contact": return "Contact";
            case "For privacy questions, contact: adilhayat@yahoo.com": return "Pour les questions de confidentialité : adilhayat@yahoo.com";
            case "Reset progress?": return "Réinitialiser la progression ?";
            case "This only clears progress on this phone.": return "Cela efface seulement la progression sur ce téléphone.";
            case "Yes, reset progress": return "Oui, réinitialiser";
            case "Cancel": return "Annuler";
            case "This only changes progress saved on this phone.": return "Cela change seulement la progression enregistrée sur ce téléphone.";
            case "Reset progress": return "Réinitialiser la progression";
            case "Student name and selected class will stay the same.": return "Le nom et la classe sélectionnée resteront identiques.";
            case "Reset": return "Réinitialiser";
            case "Hint": return "Indice";
            case "Use this clue": return "Utilise cet indice";
            case "Hints help memory. Try answering without looking again.": return "Les indices aident la mémoire. Essaie de répondre sans regarder.";
            case "Back to question": return "Retour à la question";
            case "Quit quiz?": return "Quitter le quiz ?";
            case "Your current quiz answers will not be saved.": return "Tes réponses actuelles ne seront pas enregistrées.";
            case "Leave this quiz, %s?": return "Quitter ce quiz, %s ?";
            case "The lesson will not be completed. You can restart it anytime from the same topic.": return "La leçon ne sera pas terminée. Tu peux la relancer depuis le même sujet.";
            case "Continue quiz": return "Continuer le quiz";
            case "Leave quiz and go back to topic": return "Quitter et revenir au sujet";
            case "Quick revision": return "Révision rapide";
            case "New for this level": return "Nouveau pour ce niveau";
            case "Challenge formula": return "Formule défi";
            case "Fresh start": return "Nouveau départ";
            case "Warm-up level": return "Niveau échauffement";
            case "Steady practice": return "Pratique régulière";
            case "Almost mastered": return "Presque maîtrisé";
            case "Mistake review is waiting in this topic.": return "Une révision des erreurs t'attend dans ce sujet.";
            case "Fresh topic. Open it when you are ready.": return "Nouveau sujet. Ouvre-le quand tu es prêt.";
            case "Warm-up level. Keep building memory.": return "Niveau échauffement. Continue à construire la mémoire.";
            case "Practice level. The pattern is starting to stick.": return "Niveau pratique. Le modèle commence à rester.";
            case "Confident level. A little review will finish it.": return "Niveau confiant. Une petite révision finira le sujet.";
            case "Mastered level. Review anytime.": return "Niveau maîtrisé. Révise quand tu veux.";
            case "Introduced Class %d": return "Introduit en classe %d";
            case "General Practice": return "Pratique générale";
            case "Start lesson": return "Commencer la leçon";
            case "Review again": return "Réviser encore";
            default: return null;
        }
    }

    private void showHint(QuizSession session, Question q) {
        currentScreen = SCREEN_QUIZ;
        currentQuizSession = session;
        currentFormula = session.formula;
        currentTopic = session.formula.topic;
        quizContinueAction = () -> showHint(session, q);
        LinearLayout root = baseScreen(tr("Hint"), getStudentName() + " / " + session.formula.name);
        LinearLayout c = cardTint(Color.rgb(239, 246, 255));
        c.addView(sectionTitle(tr("Use this clue")));
        c.addView(bodyText(hintText(session.formula, q)));
        c.addView(smallText(tr("Hints help memory. Try answering without looking again.")));
        root.addView(c);

        Button back = primaryButton(tr("Back to question"));
        back.setOnClickListener(v -> renderQuestion(session));
        root.addView(back);

        Button quit = ghostButton(tr("Quit quiz"));
        quit.setTextColor(RED);
        quit.setOnClickListener(v -> showQuitQuizConfirm(session, () -> showHint(session, q)));
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
        showQuitQuizConfirm(session, () -> renderQuestion(session));
    }

    private void showQuitQuizConfirm(QuizSession session, Runnable continueAction) {
        currentScreen = SCREEN_QUIZ_CONFIRM;
        currentQuizSession = session;
        currentFormula = session.formula;
        currentTopic = session.formula.topic;
        quizContinueAction = continueAction == null ? () -> renderQuestion(session) : continueAction;
        LinearLayout root = baseScreen(tr("Quit quiz?"), tr("Your current quiz answers will not be saved."));
        LinearLayout c = cardTint(Color.rgb(255, 251, 235));
        c.addView(bigText(trf("Leave this quiz, %s?", getStudentName())));
        c.addView(bodyText(tr("The lesson will not be completed. You can restart it anytime from the same topic.")));
        root.addView(c);

        Button continueQuiz = primaryButton(tr("Continue quiz"));
        continueQuiz.setOnClickListener(v -> quizContinueAction.run());
        root.addView(continueQuiz);

        Button leave = ghostButton(tr("Leave quiz and go back to topic"));
        leave.setTextColor(RED);
        leave.setOnClickListener(v -> showTopic(session.formula.topic));
        root.addView(leave);
    }

    private String praiseMessage() {
        String name = getStudentName();
        String[] messages = {
                trf("Excellent, %s!", name),
                trf("Correct - sharp memory, %s!", name),
                trf("Good work, %s. Keep going.", name),
                tr("Nice! That formula is sticking."),
                tr("Right answer. Fast and focused.")
        };
        return messages[random.nextInt(messages.length)];
    }

    private String firmCoachMessage() {
        String name = getStudentName();
        String[] messages = {
                trf("Not good enough yet, %s - focus.", name),
                trf("Careful, %s. Do not guess.", name),
                trf("Slow down, %s. Read the formula again.", name),
                tr("Wrong answer, but fix it now."),
                tr("Focus. You can do better than guessing.")
        };
        return messages[random.nextInt(messages.length)];
    }

    private LinearLayout cardTint(int color) {
        LinearLayout c = card();
        c.setBackground(roundedStroke(color, BORDER, 14));
        return c;
    }

    private LinearLayout statBox(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(8), dp(8), dp(8));
        box.setBackground(rounded(Color.WHITE, 12));
        TextView valueView = bigText(value);
        valueView.setGravity(Gravity.CENTER);
        TextView labelView = smallText(label);
        labelView.setGravity(Gravity.CENTER);
        box.addView(valueView);
        box.addView(labelView);
        return box;
    }

    private View glowProgressBar(int max, int progress) {
        GlowProgressBar bar = new GlowProgressBar(max, progress);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38));
        lp.setMargins(0, dp(10), 0, dp(2));
        bar.setLayoutParams(lp);
        return bar;
    }

    private LinearLayout levelRail(String first, String second, String third, String fourth) {
        LinearLayout rail = row();
        rail.addView(levelText(first), rowWeight());
        rail.addView(levelText(second), rowWeight());
        rail.addView(levelText(third), rowWeight());
        rail.addView(levelText(fourth), rowWeight());
        return rail;
    }

    private TextView levelText(String text) {
        TextView t = smallText(text);
        t.setTextSize(11);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, dp(2));
        return t;
    }

    private void addProgressBar(LinearLayout parent, int max, int progress) {
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(Math.max(1, max));
        bar.setProgress(Math.max(0, progress));
        bar.setContentDescription(trf("Progress %d of %d", Math.max(0, progress), Math.max(1, max)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        lp.setMargins(0, dp(8), 0, dp(8));
        bar.setLayoutParams(lp);
        parent.addView(bar);
    }

    private int currentAccuracy() {
        int totalAnswered = prefs.getInt("total_answered", 0);
        int totalCorrect = prefs.getInt("total_correct", 0);
        return totalAnswered == 0 ? 0 : Math.round((100f * totalCorrect) / totalAnswered);
    }

    private int percent(int done, int total) {
        return total == 0 ? 0 : Math.round((100f * done) / total);
    }

    private int topicCompleted(List<Formula> topicFormulas, Set<String> completed) {
        int done = 0;
        for (Formula f : topicFormulas) if (completed.contains(f.id)) done++;
        return done;
    }

    private int countWeakFormulas(List<Formula> list) {
        int count = 0;
        for (Formula f : list) if (prefs.getInt("wrong_" + f.id, 0) > 0) count++;
        return count;
    }

    private int countMistakeMarks(List<Formula> list) {
        int count = 0;
        for (Formula f : list) count += prefs.getInt("wrong_" + f.id, 0);
        return count;
    }

    private String formulaStatus(Formula f, Set<String> completed) {
        if (prefs.getInt("wrong_" + f.id, 0) > 0) return "⚠";
        if (completed.contains(f.id)) return "✓";
        return "○";
    }

    private String topicIcon(String topic) {
        String t = topic.toLowerCase(Locale.US);
        if (t.contains("area") || t.contains("geometry") || t.contains("circle")) return "📐";
        if (t.contains("number") || t.contains("algebra") || t.contains("quadratic")) return "🔢";
        if (t.contains("mensuration") || t.contains("surface") || t.contains("volume")) return "📦";
        if (t.contains("profit") || t.contains("finance")) return "💰";
        if (t.contains("trigonometry")) return "📏";
        if (t.contains("coordinate")) return "📍";
        if (t.contains("sequence")) return "🔁";
        if (t.contains("probability")) return "🎲";
        return "📘";
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable roundedStroke(int color, int strokeColor, int radiusDp) {
        GradientDrawable drawable = rounded(color, radiusDp);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        applyDirection(r);
        return r;
    }

    private LinearLayout.LayoutParams rowWeight() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        return lp;
    }

    private TextView screenTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(TEXT);
        t.setTextSize(30);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(0, 0, 0, 0);
        t.setSingleLine(true);
        applyDirection(t);
        return t;
    }

    private TextView classTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(GREEN_DARK);
        t.setTextSize(24);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(0, 0, 0, 0);
        t.setSingleLine(true);
        applyDirection(t);
        return t;
    }

    private TextView metaText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(MUTED);
        t.setTextSize(12);
        t.setSingleLine(true);
        t.setPadding(0, dp(2), dp(4), dp(6));
        applyDirection(t);
        return t;
    }

    private TextView bigText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(18);
        t.setTextColor(TEXT);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(4), 0, dp(6));
        applyDirection(t);
        return t;
    }

    private TextView lessonTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(20);
        t.setTextColor(TEXT);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(7), 0, dp(4));
        applyDirection(t);
        return t;
    }

    private TextView lessonMetaTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(GREEN_DARK);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, 0, dp(8), 0);
        applyDirection(t);
        return t;
    }

    private TextView lessonChip(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(11);
        t.setTextColor(GREEN_DARK);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setBackground(roundedStroke(Color.WHITE, Color.rgb(167, 243, 208), 20));
        t.setPadding(dp(8), dp(4), dp(8), dp(4));
        applyDirection(t);
        return t;
    }

    private TextView compactFormulaBox(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(s.length() > 26 ? 19 : 24);
        t.setTextColor(GREEN_DARK);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(false);
        t.setBackground(roundedStroke(Color.WHITE, Color.rgb(209, 250, 229), 12));
        t.setPadding(dp(10), dp(8), dp(10), dp(9));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(3), 0, dp(6));
        t.setLayoutParams(lp);
        t.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        t.setTextDirection(View.TEXT_DIRECTION_LTR);
        return t;
    }

    private LinearLayout lessonInfoBlock(String title, String text) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackground(roundedStroke(Color.rgb(248, 250, 252), Color.rgb(209, 250, 229), 10));
        block.setPadding(dp(10), dp(7), dp(10), dp(7));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, 0);
        block.setLayoutParams(lp);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(GREEN_DARK);
        heading.setTextSize(12);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setPadding(0, 0, 0, dp(1));
        applyDirection(heading);

        TextView body = new TextView(this);
        body.setText(text);
        body.setTextColor(TEXT);
        body.setTextSize(13);
        body.setLineSpacing(dp(1), 1.0f);
        body.setPadding(0, 0, 0, 0);
        body.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        body.setTextDirection(View.TEXT_DIRECTION_LTR);

        block.addView(heading);
        block.addView(body);
        return block;
    }

    private TextView compactInfoText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(14);
        t.setTextColor(TEXT);
        t.setLineSpacing(dp(1), 1.0f);
        t.setPadding(0, dp(3), 0, dp(2));
        applyDirection(t);
        return t;
    }

    private TextView compactLessonText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(13);
        t.setTextColor(MUTED);
        t.setPadding(0, dp(1), 0, dp(2));
        applyDirection(t);
        return t;
    }

    private TextView bodyText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(16);
        t.setTextColor(TEXT);
        t.setLineSpacing(dp(2), 1.0f);
        t.setPadding(0, dp(4), 0, dp(8));
        applyDirection(t);
        return t;
    }

    private TextView smallText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(14);
        t.setTextColor(MUTED);
        t.setPadding(0, dp(3), 0, dp(5));
        applyDirection(t);
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
        t.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        t.setTextDirection(View.TEXT_DIRECTION_LTR);
        return t;
    }

    private TextView compactFormulaText(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(s.length() > 24 ? 20 : 24);
        t.setTextColor(GREEN_DARK);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(false);
        t.setPadding(dp(6), dp(6), dp(6), dp(8));
        t.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        t.setTextDirection(View.TEXT_DIRECTION_LTR);
        return t;
    }

    private Button primaryButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setContentDescription(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackground(rounded(GREEN, 12));
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        applyDirection(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(7), 0, dp(7));
        b.setLayoutParams(lp);
        return b;
    }

    private Button compactPrimaryButton(String s) {
        Button b = primaryButton(s);
        b.setTextSize(14);
        b.setMinHeight(0);
        b.setMinimumHeight(dp(42));
        b.setPadding(dp(10), dp(7), dp(10), dp(7));
        return b;
    }

    private Button classChoiceButton(String className) {
        boolean selected = className.equals(selectedClass);
        Button b = new Button(this);
        String label = displayClass(className);
        b.setText(selected ? label + "  " + tr("Selected") : label);
        b.setContentDescription(selected ? label + " " + tr("Selected") : trf("Choose %s", label));
        b.setTextColor(selected ? Color.WHITE : TEXT);
        b.setTextSize(18);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER_VERTICAL);
        applyDirection(b);
        b.setBackground(selected
                ? rounded(GREEN, 14)
                : roundedStroke(Color.WHITE, Color.rgb(209, 250, 229), 14));
        b.setPadding(dp(18), dp(14), dp(18), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(7), 0, dp(7));
        b.setLayoutParams(lp);
        return b;
    }

    private Button smallResetButton(String s) {
        Button b = ghostButton(s);
        b.setTextColor(RED);
        b.setTextSize(12);
        b.setMinHeight(0);
        b.setMinimumHeight(dp(28));
        b.setPadding(dp(8), 0, dp(8), 0);
        b.setBackground(roundedStroke(Color.rgb(254, 242, 242), Color.rgb(254, 202, 202), 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.END;
        lp.setMargins(0, dp(2), 0, dp(8));
        b.setLayoutParams(lp);
        return b;
    }

    private Button tinyActionButton(String s) {
        Button b = ghostButton(s);
        b.setTextSize(12);
        b.setTextColor(GREEN_DARK);
        b.setMinHeight(0);
        b.setMinimumHeight(dp(36));
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(10), 0, dp(10), 0);
        b.setBackground(roundedStroke(Color.rgb(236, 253, 245), BORDER, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(96), dp(36));
        lp.setMargins(dp(8), 0, 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    private Button secondaryButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setContentDescription(s);
        b.setTextColor(TEXT);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(roundedStroke(Color.rgb(236, 253, 245), BORDER, 12));
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        applyDirection(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private Button compactSecondaryButton(String s) {
        Button b = secondaryButton(s);
        b.setTextSize(13);
        b.setMinHeight(0);
        b.setMinimumHeight(dp(38));
        b.setPadding(dp(8), dp(6), dp(8), dp(6));
        return b;
    }

    private Button topNavButton(String s) {
        Button b = compactSecondaryButton(s);
        b.setTextSize(12);
        b.setMinimumHeight(dp(34));
        b.setPadding(dp(8), dp(4), dp(8), dp(4));
        return b;
    }

    private Button ghostButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setContentDescription(s);
        b.setTextColor(GREEN_DARK);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        applyDirection(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }

    private class GlowProgressBar extends View {
        private final int max;
        private final int progress;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        GlowProgressBar(int max, int progress) {
            super(MainActivity.this);
            this.max = Math.max(1, max);
            this.progress = Math.max(0, Math.min(progress, this.max));
            setContentDescription(trf("Progress %d of %d", this.progress, this.max));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float left = dp(4);
            float right = getWidth() - dp(4);
            float centerY = getHeight() / 2f;
            float barHeight = dp(13);
            float radius = barHeight / 2f;
            RectF track = new RectF(left, centerY - radius, right, centerY + radius);

            paint.setShader(null);
            paint.clearShadowLayer();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(226, 232, 240));
            canvas.drawRoundRect(track, radius, radius, paint);

            float ratio = (float) progress / (float) max;
            float fillRight = track.left + (track.width() * ratio);
            if (ratio > 0f) {
                RectF fill = new RectF(track.left, track.top, Math.max(track.left + dp(10), fillRight), track.bottom);
                paint.setShader(new LinearGradient(
                        track.left,
                        track.top,
                        track.right,
                        track.bottom,
                        new int[]{GREEN, Color.rgb(20, 184, 166), BLUE},
                        null,
                        Shader.TileMode.CLAMP
                ));
                paint.setShadowLayer(dp(9), 0, 0, Color.argb(130, 20, 184, 166));
                canvas.drawRoundRect(fill, radius, radius, paint);
                paint.clearShadowLayer();
                paint.setShader(null);
            }

            for (int i = 0; i < 4; i++) {
                float x = track.left + (track.width() * i / 3f);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(i / 3f <= ratio ? Color.WHITE : Color.rgb(203, 213, 225));
                canvas.drawCircle(x, centerY, dp(4), paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1));
                paint.setColor(i / 3f <= ratio ? GREEN_DARK : Color.rgb(148, 163, 184));
                canvas.drawCircle(x, centerY, dp(4), paint);
            }

            float knobX = Math.max(track.left, fillRight);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(null);
            paint.setColor(Color.WHITE);
            paint.setShadowLayer(dp(7), 0, 0, Color.argb(120, 37, 99, 235));
            canvas.drawCircle(knobX, centerY, dp(6), paint);
            paint.clearShadowLayer();
        }
    }

    private class FormulaDiagramView extends View {
        private final Formula formula;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        FormulaDiagramView(Formula formula) {
            super(MainActivity.this);
            this.formula = formula;
            setContentDescription(diagramDescription(formula));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            RectF bounds = new RectF(dp(8), dp(8), getWidth() - dp(8), getHeight() - dp(8));
            drawDiagramPanel(canvas, bounds);
            RectF area = new RectF(bounds.left + dp(16), bounds.top + dp(14), bounds.right - dp(16), bounds.bottom - dp(14));
            String topic = formula.topic.toLowerCase(Locale.US);
            String name = formula.name.toLowerCase(Locale.US);

            if (topic.contains("circle") || name.contains("diameter") || name.contains("arc") || name.contains("sector")) {
                drawPolishedCircleDiagram(canvas, area, name);
            } else if (topic.contains("mensuration")) {
                drawSolidDiagram(canvas, area, name);
            } else if (topic.contains("coordinate") || name.contains("slope") || name.contains("midpoint") || name.contains("distance") || name.contains("linear") || name.contains("quadratic") || name.contains("discriminant")) {
                drawGraphDiagram(canvas, area, name);
            } else if (topic.contains("algebra identities")) {
                drawAlgebraDiagram(canvas, area, name);
            } else if (topic.contains("profit") || topic.contains("finance")) {
                drawFinanceDiagram(canvas, area, name);
            } else if (topic.contains("number")) {
                drawNumberDiagram(canvas, area, name);
            } else if (topic.contains("probability")) {
                drawProbabilityDiagram(canvas, area);
            } else if (topic.contains("sequence")) {
                drawSequenceDiagram(canvas, area);
            } else if (topic.contains("geometry") || topic.contains("area")) {
                drawGeometryDiagram(canvas, area, name);
            } else if (topic.contains("trigonometry")) {
                drawTrigDiagram(canvas, area, name);
            } else {
                drawFallbackDiagram(canvas, area);
            }
        }

        private void drawDiagramPanel(Canvas canvas, RectF r) {
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                    new int[]{Color.WHITE, Color.rgb(236, 253, 245), Color.rgb(239, 246, 255)},
                    null, Shader.TileMode.CLAMP));
            paint.setShadowLayer(dp(8), 0, dp(3), Color.argb(45, 15, 118, 110));
            canvas.drawRoundRect(r, dp(18), dp(18), paint);
            paint.clearShadowLayer();
            paint.setShader(null);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(187, 247, 208));
            canvas.drawRoundRect(r, dp(18), dp(18), paint);
        }

        private void drawGeometryDiagram(Canvas canvas, RectF r, String name) {
            if (name.contains("pythagorean")) {
                drawPythagorasDiagram(canvas, r);
                return;
            }
            if (name.contains("heron") || name.contains("semi-perimeter") || name.contains("perimeter of triangle")) {
                drawTriangleSidesDiagram(canvas, r, name);
                return;
            }
            if (name.contains("triangle")) {
                drawTriangleAreaDiagram(canvas, r);
                return;
            }
            if (name.contains("parallelogram")) {
                drawParallelogramDiagram(canvas, r);
                return;
            }

            float left = r.left + dp(26);
            float top = r.top + dp(38);
            float right = r.right - dp(26);
            float bottom = r.bottom - dp(44);
            Path shape = new Path();
            shape.addRoundRect(new RectF(left, top, right, bottom), dp(8), dp(8), Path.Direction.CW);
            shape.close();

            fillPath(canvas, shape, Color.rgb(167, 243, 208), Color.rgb(45, 212, 191));
            strokePath(canvas, shape, GREEN_DARK, dp(2));
            String baseLabel = name.contains("square") ? "s" : "l";
            String heightLabel = name.contains("square") ? "s" : "w";
            drawDimension(canvas, left, bottom + dp(18), right, bottom + dp(18), baseLabel);
            drawDimension(canvas, right + dp(14), top, right + dp(14), bottom, heightLabel);
            drawLabelBox(canvas, name.contains("perimeter") ? "add all outside edges" : "inside area", r.centerX(), r.top + dp(24), 12, TEXT);
        }

        private void drawParallelogramDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(34);
            float right = r.right - dp(34);
            float top = r.top + dp(52);
            float bottom = r.bottom - dp(42);
            float skew = Math.min(dp(56), (right - left) * 0.2f);
            float topLeft = left + skew;
            float topRight = right;
            float bottomLeft = left;
            float bottomRight = right - skew;

            Path shape = new Path();
            shape.moveTo(topLeft, top);
            shape.lineTo(topRight, top);
            shape.lineTo(bottomRight, bottom);
            shape.lineTo(bottomLeft, bottom);
            shape.close();

            fillPath(canvas, shape, Color.rgb(167, 243, 208), Color.rgb(45, 212, 191));
            strokePath(canvas, shape, GREEN_DARK, dp(3));

            drawLabelBox(canvas, "Area = base x height", r.centerX(), r.top + dp(25), 12, TEXT);
            drawArrowDimension(canvas, bottomLeft, bottom + dp(22), bottomRight, bottom + dp(22), "base b");
            drawArrowDimension(canvas, topLeft, top, topLeft, bottom, "height h");

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.argb(150, 245, 158, 11));
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(topLeft, bottom, topLeft, top, paint);
            canvas.drawLine(topLeft, bottom, bottomLeft, bottom, paint);
            drawRightAngle(canvas, topLeft, bottom - dp(24));
        }

        private void drawRightTriangleDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(34);
            float bottom = r.bottom - dp(38);
            float right = r.right - dp(42);
            float top = r.top + dp(36);
            Path tri = new Path();
            tri.moveTo(left, bottom);
            tri.lineTo(right, bottom);
            tri.lineTo(right, top);
            tri.close();
            fillPath(canvas, tri, Color.rgb(219, 234, 254), Color.rgb(125, 211, 252));
            strokePath(canvas, tri, BLUE, dp(2));
            drawRightAngle(canvas, right - dp(24), bottom - dp(24));
            drawDimension(canvas, left, bottom + dp(18), right, bottom + dp(18), "base");
            drawDimension(canvas, right + dp(14), bottom, right + dp(14), top, "h");
            drawText(canvas, "c", (left + right) / 2f - dp(8), (top + bottom) / 2f - dp(10), 16, TEXT, Paint.Align.CENTER, true);
        }

        private void drawTriangleAreaDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(42);
            float bottom = r.bottom - dp(42);
            float right = r.right - dp(42);
            float top = r.top + dp(42);
            float apex = left + (right - left) * 0.58f;
            Path tri = new Path();
            tri.moveTo(left, bottom);
            tri.lineTo(right, bottom);
            tri.lineTo(apex, top);
            tri.close();
            fillPath(canvas, tri, Color.rgb(219, 234, 254), Color.rgb(125, 211, 252));
            strokePath(canvas, tri, BLUE, dp(3));
            drawArrowDimension(canvas, left, bottom + dp(22), right, bottom + dp(22), "base b");
            drawArrowDimension(canvas, apex, top, apex, bottom, "height h");
            drawRightAngle(canvas, apex, bottom - dp(24));
            drawLabelBox(canvas, "A = 1/2 x b x h", r.centerX(), r.top + dp(24), 12, TEXT);
        }

        private void drawTriangleSidesDiagram(Canvas canvas, RectF r, String name) {
            float left = r.left + dp(44);
            float bottom = r.bottom - dp(42);
            float right = r.right - dp(44);
            float top = r.top + dp(48);
            float apex = r.centerX();
            Path tri = new Path();
            tri.moveTo(left, bottom);
            tri.lineTo(right, bottom);
            tri.lineTo(apex, top);
            tri.close();
            fillPath(canvas, tri, Color.rgb(254, 249, 195), Color.rgb(167, 243, 208));
            strokePath(canvas, tri, GREEN_DARK, dp(3));
            drawText(canvas, "a", (apex + right) / 2f + dp(12), (top + bottom) / 2f, 15, TEXT, Paint.Align.CENTER, true);
            drawText(canvas, "b", (left + apex) / 2f - dp(12), (top + bottom) / 2f, 15, TEXT, Paint.Align.CENTER, true);
            drawText(canvas, "c", r.centerX(), bottom + dp(25), 15, TEXT, Paint.Align.CENTER, true);
            String label = name.contains("semi") || name.contains("heron") ? "s = (a + b + c) / 2" : "P = a + b + c";
            drawLabelBox(canvas, label, r.centerX(), r.top + dp(24), 12, TEXT);
        }

        private void drawPythagorasDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(42);
            float bottom = r.bottom - dp(42);
            float right = r.right - dp(48);
            float top = r.top + dp(44);
            Path tri = new Path();
            tri.moveTo(left, bottom);
            tri.lineTo(right, bottom);
            tri.lineTo(right, top);
            tri.close();
            fillPath(canvas, tri, Color.rgb(219, 234, 254), Color.rgb(147, 197, 253));
            strokePath(canvas, tri, BLUE, dp(3));
            drawRightAngle(canvas, right - dp(24), bottom - dp(24));
            drawArrowDimension(canvas, left, bottom + dp(22), right, bottom + dp(22), "a");
            drawArrowDimension(canvas, right + dp(18), bottom, right + dp(18), top, "b");
            drawText(canvas, "hypotenuse c", (left + right) / 2f - dp(16), (top + bottom) / 2f - dp(12), 12, TEXT, Paint.Align.CENTER, true);
            drawLabelBox(canvas, "a^2 + b^2 = c^2", r.centerX(), r.top + dp(24), 12, TEXT);
        }

        private void drawTrigDiagram(Canvas canvas, RectF r, String name) {
            float left = r.left + dp(42);
            float bottom = r.bottom - dp(42);
            float right = r.right - dp(50);
            float top = r.top + dp(44);
            Path tri = new Path();
            tri.moveTo(left, bottom);
            tri.lineTo(right, bottom);
            tri.lineTo(right, top);
            tri.close();
            fillPath(canvas, tri, Color.rgb(236, 253, 245), Color.rgb(191, 219, 254));
            strokePath(canvas, tri, GREEN_DARK, dp(3));
            drawRightAngle(canvas, right - dp(24), bottom - dp(24));
            drawText(canvas, "theta", left + dp(30), bottom - dp(10), 12, AMBER, Paint.Align.CENTER, true);
            drawText(canvas, "opposite", right + dp(8), (top + bottom) / 2f, 12, TEXT, Paint.Align.LEFT, true);
            drawText(canvas, "adjacent", (left + right) / 2f, bottom + dp(24), 12, TEXT, Paint.Align.CENTER, true);
            drawText(canvas, "hypotenuse", (left + right) / 2f - dp(12), (top + bottom) / 2f - dp(16), 12, TEXT, Paint.Align.CENTER, true);
            String label = name.contains("cos") ? "cos theta = adjacent / hypotenuse"
                    : name.contains("tan") ? "tan theta = opposite / adjacent"
                    : name.contains("identity") ? "sin^2 theta + cos^2 theta = 1"
                    : "sin theta = opposite / hypotenuse";
            drawLabelBox(canvas, label, r.centerX(), r.top + dp(24), 11, TEXT);
        }

        private void drawPolishedCircleDiagram(Canvas canvas, RectF r, String name) {
            float cx = r.centerX();
            float cy = r.centerY() + dp(6);
            float rad = Math.min(r.width(), r.height()) * 0.34f;
            RectF oval = new RectF(cx - rad, cy - rad, cx + rad, cy + rad);

            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(oval.left, oval.top, oval.right, oval.bottom,
                    Color.rgb(240, 253, 250), Color.rgb(191, 219, 254), Shader.TileMode.CLAMP));
            paint.setShadowLayer(dp(8), 0, dp(3), Color.argb(65, 15, 118, 110));
            canvas.drawOval(oval, paint);
            paint.clearShadowLayer();
            paint.setShader(null);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setColor(GREEN_DARK);
            canvas.drawOval(oval, paint);

            if (name.contains("diameter")) {
                drawLabelBox(canvas, "Diameter d = 2r", cx, r.top + dp(25), 13, TEXT);
                drawArrowDimension(canvas, cx - rad, cy, cx + rad, cy, "diameter d");
                drawLine(canvas, cx, cy, cx + rad, cy, BLUE, dp(3));
                drawText(canvas, "r", cx + rad / 2f, cy - dp(14), 14, BLUE, Paint.Align.CENTER, true);
                drawPoint(canvas, cx, cy, "center");
                return;
            }

            if (name.contains("sector") || name.contains("arc")) {
                float startAngle = -35f;
                float sweep = 95f;
                boolean arcOnly = name.contains("arc");

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(arcOnly ? Color.argb(45, 245, 158, 11) : Color.argb(155, 245, 158, 11));
                canvas.drawArc(oval, startAngle, sweep, true, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(arcOnly ? 7 : 4));
                paint.setColor(AMBER);
                canvas.drawArc(oval, startAngle, sweep, false, paint);

                double a1 = Math.toRadians(startAngle);
                double a2 = Math.toRadians(startAngle + sweep);
                float x1 = cx + rad * (float) Math.cos(a1);
                float y1 = cy + rad * (float) Math.sin(a1);
                float x2 = cx + rad * (float) Math.cos(a2);
                float y2 = cy + rad * (float) Math.sin(a2);
                drawLine(canvas, cx, cy, x1, y1, BLUE, dp(2));
                drawLine(canvas, cx, cy, x2, y2, BLUE, dp(2));
                drawText(canvas, "r", (cx + x1) / 2f + dp(4), (cy + y1) / 2f - dp(4), 13, BLUE, Paint.Align.CENTER, true);
                drawText(canvas, "theta", cx + dp(34), cy - dp(4), 12, TEXT, Paint.Align.CENTER, true);
                drawText(canvas, arcOnly ? "arc" : "sector", cx + dp(62), cy - dp(48), 12, AMBER, Paint.Align.CENTER, true);
                drawLabelBox(canvas, arcOnly ? "Arc = theta/360 x 2 pi r" : "Area = theta/360 x pi r^2", cx, r.top + dp(25), 11, TEXT);
                return;
            }

            drawLine(canvas, cx, cy, cx + rad * 0.72f, cy - rad * 0.42f, BLUE, dp(3));
            drawText(canvas, "radius r", cx + rad * 0.46f, cy - rad * 0.3f - dp(4), 12, BLUE, Paint.Align.CENTER, true);
            drawPoint(canvas, cx, cy, "center");

            if (name.contains("circumference")) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(8));
                paint.setColor(Color.argb(150, 245, 158, 11));
                canvas.drawOval(oval, paint);
                drawText(canvas, "distance around", cx, cy - rad - dp(14), 12, AMBER, Paint.Align.CENTER, true);
                drawLabelBox(canvas, "C = 2 pi r", cx, r.top + dp(25), 13, TEXT);
            } else {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(70, 20, 184, 166));
                canvas.drawOval(new RectF(cx - rad + dp(10), cy - rad + dp(10), cx + rad - dp(10), cy + rad - dp(10)), paint);
                drawText(canvas, "inside area", cx, cy + dp(8), 12, GREEN_DARK, Paint.Align.CENTER, true);
                drawLabelBox(canvas, "A = pi r^2", cx, r.top + dp(25), 13, TEXT);
            }
        }

        private void drawCircleDiagram(Canvas canvas, RectF r, String name) {
            float cx = r.centerX();
            float cy = r.centerY() + dp(2);
            float rad = Math.min(r.width(), r.height()) * 0.32f;
            RectF oval = new RectF(cx - rad, cy - rad, cx + rad, cy + rad);

            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(oval.left, oval.top, oval.right, oval.bottom,
                    Color.rgb(204, 251, 241), Color.rgb(147, 197, 253), Shader.TileMode.CLAMP));
            paint.setShadowLayer(dp(7), 0, dp(3), Color.argb(70, 37, 99, 235));
            canvas.drawOval(oval, paint);
            paint.clearShadowLayer();
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setColor(GREEN_DARK);
            canvas.drawOval(oval, paint);

            if (name.contains("diameter")) {
                drawLine(canvas, cx - rad, cy, cx + rad, cy, GREEN_DARK, dp(3));
                drawText(canvas, "r", cx - rad / 2f, cy - dp(12), 13, BLUE, Paint.Align.CENTER, true);
                drawText(canvas, "r", cx + rad / 2f, cy - dp(12), 13, BLUE, Paint.Align.CENTER, true);
                drawLabelBox(canvas, "d = 2r", cx, r.top + dp(24), 13, TEXT);
                return;
            }

            if (name.contains("sector") || name.contains("arc")) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(145, 245, 158, 11));
                canvas.drawArc(oval, -25, 85, true, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(4));
                paint.setColor(AMBER);
                canvas.drawArc(oval, -25, 85, false, paint);
                drawText(canvas, "theta", cx + dp(36), cy - dp(10), 12, TEXT, Paint.Align.CENTER, true);
                drawText(canvas, name.contains("arc") ? "arc length" : "sector area", cx + dp(58), cy - dp(46), 12, AMBER, Paint.Align.CENTER, true);
                drawLabelBox(canvas, name.contains("arc") ? "theta/360 x circumference" : "theta/360 x circle area", cx, r.top + dp(24), 11, TEXT);
                drawText(canvas, "θ", cx + dp(24), cy - dp(6), 18, TEXT, Paint.Align.CENTER, true);
            }

            if (name.contains("sector") || name.contains("arc")) {
                drawLabelBox(canvas, "theta", cx + dp(35), cy - dp(10), 11, TEXT);
            } else if (name.contains("circumference")) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(7));
                paint.setColor(Color.argb(135, 245, 158, 11));
                canvas.drawOval(oval, paint);
                drawText(canvas, "around edge = C", cx, cy - rad - dp(12), 12, AMBER, Paint.Align.CENTER, true);
                drawLabelBox(canvas, "C = 2 pi r", cx, r.top + dp(24), 12, TEXT);
            } else {
                drawText(canvas, "shaded area A", cx, cy - rad - dp(12), 12, TEXT, Paint.Align.CENTER, true);
                drawLabelBox(canvas, "A = pi x r^2", cx, r.top + dp(24), 12, TEXT);
            }

            drawLine(canvas, cx, cy, cx + rad * 0.72f, cy - rad * 0.42f, BLUE, dp(2));
            drawText(canvas, "r", cx + rad * 0.38f, cy - rad * 0.25f, 14, BLUE, Paint.Align.CENTER, true);
            if (!name.contains("sector") && !name.contains("arc")) {
                drawLine(canvas, cx - rad, cy + dp(16), cx + rad, cy + dp(16), GREEN_DARK, dp(2));
                drawText(canvas, "diameter d", cx, cy + dp(38), 12, GREEN_DARK, Paint.Align.CENTER, true);
            }
        }

        private void drawSolidDiagram(Canvas canvas, RectF r, String name) {
            if (name.contains("cylinder")) {
                drawCylinder(canvas, r, name);
            } else if (name.contains("cone")) {
                drawCone(canvas, r);
            } else if (name.contains("sphere")) {
                drawSphere(canvas, r, name);
            } else {
                drawCuboid(canvas, r, name);
            }
        }

        private void drawCuboid(Canvas canvas, RectF r, String name) {
            boolean cube = name.contains("cube");
            float left = r.left + dp(38);
            float top = r.top + dp(38);
            float w = cube ? dp(104) : dp(142);
            float h = dp(76);
            float depth = dp(34);
            Path front = rectPath(left, top + depth, left + w, top + depth + h);
            Path topFace = new Path();
            topFace.moveTo(left, top + depth);
            topFace.lineTo(left + depth, top);
            topFace.lineTo(left + w + depth, top);
            topFace.lineTo(left + w, top + depth);
            topFace.close();
            Path side = new Path();
            side.moveTo(left + w, top + depth);
            side.lineTo(left + w + depth, top);
            side.lineTo(left + w + depth, top + h);
            side.lineTo(left + w, top + depth + h);
            side.close();
            fillPath(canvas, topFace, Color.rgb(187, 247, 208), Color.rgb(94, 234, 212));
            fillPath(canvas, side, Color.rgb(153, 246, 228), Color.rgb(125, 211, 252));
            fillPath(canvas, front, Color.rgb(224, 242, 254), Color.rgb(186, 230, 253));
            strokePath(canvas, topFace, GREEN_DARK, dp(2));
            strokePath(canvas, side, GREEN_DARK, dp(2));
            strokePath(canvas, front, GREEN_DARK, dp(2));
            drawText(canvas, cube ? "s" : "l", left + w / 2f, top + depth + h + dp(22), 14, TEXT, Paint.Align.CENTER, true);
            drawText(canvas, cube ? "s" : "h", left + w + depth + dp(16), top + depth + h / 2f, 14, TEXT, Paint.Align.CENTER, true);
            drawText(canvas, cube ? "s" : "w", left + w + depth / 2f, top + dp(2), 14, TEXT, Paint.Align.CENTER, true);
            String label = name.contains("surface") && cube ? "6 faces, each s x s"
                    : name.contains("surface") ? "SA = sum of all 6 faces"
                    : cube ? "V = s x s x s"
                    : "V = l x w x h";
            drawLabelBox(canvas, label, r.centerX(), r.top + dp(24), 11, TEXT);
        }

        private void drawCylinder(Canvas canvas, RectF r, String name) {
            float cx = r.centerX();
            float top = r.top + dp(38);
            float bottom = r.bottom - dp(40);
            float rx = dp(58);
            float ry = dp(18);
            RectF topOval = new RectF(cx - rx, top - ry, cx + rx, top + ry);
            RectF bottomOval = new RectF(cx - rx, bottom - ry, cx + rx, bottom + ry);
            RectF body = new RectF(cx - rx, top, cx + rx, bottom);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(body.left, body.top, body.right, body.bottom,
                    Color.rgb(224, 242, 254), Color.rgb(94, 234, 212), Shader.TileMode.CLAMP));
            canvas.drawRect(body, paint);
            paint.setShader(null);
            drawLine(canvas, cx - rx, top, cx - rx, bottom, GREEN_DARK, dp(2));
            drawLine(canvas, cx + rx, top, cx + rx, bottom, GREEN_DARK, dp(2));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(204, 251, 241));
            canvas.drawOval(topOval, paint);
            paint.setColor(Color.rgb(186, 230, 253));
            canvas.drawOval(bottomOval, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(GREEN_DARK);
            canvas.drawOval(topOval, paint);
            canvas.drawOval(bottomOval, paint);
            drawText(canvas, "r", cx + dp(28), top + dp(2), 14, BLUE, Paint.Align.CENTER, true);
            drawDimension(canvas, cx + rx + dp(18), top, cx + rx + dp(18), bottom, "h");
            drawLabelBox(canvas, name.contains("curved") ? "curved side = 2 pi r h" : "base area pi r^2 x h", cx, r.top + dp(24), 11, TEXT);
        }

        private void drawCone(Canvas canvas, RectF r) {
            float cx = r.centerX();
            float top = r.top + dp(28);
            float bottom = r.bottom - dp(38);
            float rx = dp(70);
            Path cone = new Path();
            cone.moveTo(cx, top);
            cone.lineTo(cx - rx, bottom);
            cone.lineTo(cx + rx, bottom);
            cone.close();
            fillPath(canvas, cone, Color.rgb(219, 234, 254), Color.rgb(153, 246, 228));
            strokePath(canvas, cone, GREEN_DARK, dp(2));
            RectF base = new RectF(cx - rx, bottom - dp(16), cx + rx, bottom + dp(16));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(191, 219, 254));
            canvas.drawOval(base, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(GREEN_DARK);
            canvas.drawOval(base, paint);
            drawLine(canvas, cx, top, cx, bottom, AMBER, dp(2));
            drawText(canvas, "h", cx + dp(14), (top + bottom) / 2f, 14, AMBER, Paint.Align.LEFT, true);
            drawText(canvas, "r", cx + dp(34), bottom + dp(4), 14, BLUE, Paint.Align.CENTER, true);
            drawLabelBox(canvas, "V = 1/3 x pi r^2 h", cx, r.top + dp(24), 11, TEXT);
        }

        private void drawSphere(Canvas canvas, RectF r, String name) {
            float cx = r.centerX();
            float cy = r.centerY();
            float rad = Math.min(r.width(), r.height()) * 0.33f;
            RectF ball = new RectF(cx - rad, cy - rad, cx + rad, cy + rad);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(ball.left, ball.top, ball.right, ball.bottom,
                    Color.WHITE, Color.rgb(125, 211, 252), Shader.TileMode.CLAMP));
            paint.setShadowLayer(dp(8), 0, dp(4), Color.argb(65, 37, 99, 235));
            canvas.drawOval(ball, paint);
            paint.clearShadowLayer();
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(GREEN_DARK);
            canvas.drawOval(ball, paint);
            canvas.drawOval(new RectF(cx - rad, cy - rad * 0.28f, cx + rad, cy + rad * 0.28f), paint);
            drawLine(canvas, cx, cy, cx + rad * 0.7f, cy - rad * 0.22f, BLUE, dp(2));
            drawText(canvas, "r", cx + rad * 0.35f, cy - dp(8), 14, BLUE, Paint.Align.CENTER, true);
            drawLabelBox(canvas, name.contains("surface") ? "SA = 4 pi r^2" : "V = 4/3 pi r^3", cx, r.top + dp(24), 12, TEXT);
        }

        private void drawGraphDiagram(Canvas canvas, RectF r, String name) {
            float left = r.left + dp(20);
            float top = r.top + dp(18);
            float right = r.right - dp(12);
            float bottom = r.bottom - dp(24);
            drawGrid(canvas, left, top, right, bottom);
            if (name.contains("distance")) {
                drawDistanceGraph(canvas, r, left, top, right, bottom);
                return;
            }
            if (name.contains("midpoint")) {
                drawMidpointGraph(canvas, r, left, top, right, bottom);
                return;
            }
            if (name.contains("slope-intercept") || name.contains("linear standard")) {
                drawLineEquationGraph(canvas, r, left, top, right, bottom);
                return;
            }
            if (name.contains("slope")) {
                drawSlopeGraph(canvas, r, left, top, right, bottom);
                return;
            }
            if (name.contains("quadratic") || name.contains("discriminant") || name.contains("axis")) {
                drawQuadraticGraph(canvas, r, left, top, right, bottom, name);
                return;
            }
            if (name.contains("quadratic") || name.contains("discriminant") || name.contains("axis")) {
                Path curve = new Path();
                for (int i = 0; i <= 80; i++) {
                    float x = left + (right - left) * i / 80f;
                    float t = (x - (left + right) / 2f) / ((right - left) / 2f);
                    float y = bottom - dp(22) - (1 - t * t) * (bottom - top - dp(34));
                    if (i == 0) curve.moveTo(x, y); else curve.lineTo(x, y);
                }
                strokePath(canvas, curve, BLUE, dp(3));
                drawLine(canvas, (left + right) / 2f, top, (left + right) / 2f, bottom, AMBER, dp(2));
                drawText(canvas, "x = -b/(2a)", (left + right) / 2f + dp(4), top + dp(18), 12, TEXT, Paint.Align.LEFT, true);
            } else {
                float x1 = left + dp(34);
                float y1 = bottom - dp(28);
                float x2 = right - dp(34);
                float y2 = top + dp(44);
                drawLine(canvas, x1, y1, x2, y2, BLUE, dp(3));
                drawPoint(canvas, x1, y1, "A");
                drawPoint(canvas, x2, y2, "B");
                drawLine(canvas, x1, y1, x2, y1, AMBER, dp(2));
                drawLine(canvas, x2, y1, x2, y2, AMBER, dp(2));
                drawText(canvas, "Δx", (x1 + x2) / 2f, y1 + dp(18), 12, AMBER, Paint.Align.CENTER, true);
                drawText(canvas, "Δy", x2 + dp(16), (y1 + y2) / 2f, 12, AMBER, Paint.Align.CENTER, true);
            }
        }

        private void drawDistanceGraph(Canvas canvas, RectF r, float left, float top, float right, float bottom) {
            float x1 = left + dp(36);
            float y1 = bottom - dp(30);
            float x2 = right - dp(40);
            float y2 = top + dp(42);
            drawLine(canvas, x1, y1, x2, y2, BLUE, dp(3));
            drawLine(canvas, x1, y1, x2, y1, AMBER, dp(2));
            drawLine(canvas, x2, y1, x2, y2, AMBER, dp(2));
            drawPoint(canvas, x1, y1, "P1");
            drawPoint(canvas, x2, y2, "P2");
            drawText(canvas, "Delta x = x2 - x1", (x1 + x2) / 2f, y1 + dp(22), 11, AMBER, Paint.Align.CENTER, true);
            drawText(canvas, "Delta y = y2 - y1", x2 + dp(8), (y1 + y2) / 2f, 11, AMBER, Paint.Align.LEFT, true);
            drawLabelBox(canvas, "d = sqrt(Delta x^2 + Delta y^2)", r.centerX(), r.top + dp(24), 11, TEXT);
        }

        private void drawMidpointGraph(Canvas canvas, RectF r, float left, float top, float right, float bottom) {
            float x1 = left + dp(36);
            float y1 = bottom - dp(34);
            float x2 = right - dp(42);
            float y2 = top + dp(46);
            float mx = (x1 + x2) / 2f;
            float my = (y1 + y2) / 2f;
            drawLine(canvas, x1, y1, x2, y2, BLUE, dp(3));
            drawPoint(canvas, x1, y1, "A");
            drawPoint(canvas, x2, y2, "B");
            drawPoint(canvas, mx, my, "M");
            drawText(canvas, "halfway", mx, my + dp(24), 12, AMBER, Paint.Align.CENTER, true);
            drawLabelBox(canvas, "M = average of x and y", r.centerX(), r.top + dp(24), 11, TEXT);
        }

        private void drawSlopeGraph(Canvas canvas, RectF r, float left, float top, float right, float bottom) {
            float x1 = left + dp(38);
            float y1 = bottom - dp(34);
            float x2 = right - dp(42);
            float y2 = top + dp(52);
            drawLine(canvas, x1, y1, x2, y2, BLUE, dp(3));
            drawPoint(canvas, x1, y1, "(x1,y1)");
            drawPoint(canvas, x2, y2, "(x2,y2)");
            drawLine(canvas, x1, y1, x2, y1, AMBER, dp(2));
            drawLine(canvas, x2, y1, x2, y2, AMBER, dp(2));
            drawText(canvas, "run", (x1 + x2) / 2f, y1 + dp(22), 12, AMBER, Paint.Align.CENTER, true);
            drawText(canvas, "rise", x2 + dp(10), (y1 + y2) / 2f, 12, AMBER, Paint.Align.LEFT, true);
            drawLabelBox(canvas, "m = rise / run", r.centerX(), r.top + dp(24), 12, TEXT);
        }

        private void drawLineEquationGraph(Canvas canvas, RectF r, float left, float top, float right, float bottom) {
            float x1 = left + dp(34);
            float y1 = bottom - dp(28);
            float x2 = right - dp(36);
            float y2 = top + dp(54);
            float yAxis = left + (right - left) * 0.28f;
            drawLine(canvas, x1, y1, x2, y2, BLUE, dp(3));
            drawLine(canvas, yAxis, top, yAxis, bottom, TEXT, dp(2));
            float interceptY = y1 + (y2 - y1) * ((yAxis - x1) / (x2 - x1));
            drawPoint(canvas, yAxis, interceptY, "c");
            drawText(canvas, "y-intercept", yAxis + dp(12), interceptY + dp(20), 11, TEXT, Paint.Align.LEFT, true);
            drawLabelBox(canvas, "y = mx + c", r.centerX(), r.top + dp(24), 12, TEXT);
        }

        private void drawQuadraticGraph(Canvas canvas, RectF r, float left, float top, float right, float bottom, String name) {
            Path curve = new Path();
            float axis = (left + right) / 2f;
            float vertexY = bottom - dp(34);
            for (int i = 0; i <= 80; i++) {
                float x = left + (right - left) * i / 80f;
                float t = (x - axis) / ((right - left) / 2f);
                float y = vertexY - (1 - t * t) * (bottom - top - dp(46));
                if (i == 0) curve.moveTo(x, y); else curve.lineTo(x, y);
            }
            strokePath(canvas, curve, BLUE, dp(3));
            drawLine(canvas, axis, top, axis, bottom, AMBER, dp(2));
            drawText(canvas, "axis", axis + dp(8), top + dp(28), 12, AMBER, Paint.Align.LEFT, true);
            drawPoint(canvas, axis - dp(44), bottom - dp(52), "root");
            drawPoint(canvas, axis + dp(44), bottom - dp(52), "root");
            drawLabelBox(canvas, name.contains("discriminant") ? "D = b^2 - 4ac tells roots" : "x = -b / (2a)", r.centerX(), r.top + dp(24), 11, TEXT);
        }

        private void drawAlgebraDiagram(Canvas canvas, RectF r, String name) {
            if (name.contains("difference of squares") || name.contains("product of sum")) {
                drawDifferenceSquaresDiagram(canvas, r);
                return;
            }
            if (name.contains("difference")) {
                drawSquareIdentityDiagram(canvas, r, true);
                return;
            }
            drawSquareIdentityDiagram(canvas, r, false);
            return;
        }

        private void drawSquareIdentityDiagram(Canvas canvas, RectF r, boolean minus) {
            float x = r.left + dp(30);
            float y = r.top + dp(42);
            float a = dp(72);
            float b = dp(44);
            drawTile(canvas, x, y, a, a, Color.rgb(167, 243, 208), "a^2");
            drawTile(canvas, x + a + dp(8), y, b, a, minus ? Color.rgb(254, 202, 202) : Color.rgb(191, 219, 254), minus ? "-ab" : "ab");
            drawTile(canvas, x, y + a + dp(8), a, b, minus ? Color.rgb(254, 202, 202) : Color.rgb(191, 219, 254), minus ? "-ab" : "ab");
            drawTile(canvas, x + a + dp(8), y + a + dp(8), b, b, Color.rgb(253, 230, 138), "b^2");
            drawLabelBox(canvas, minus ? "(a - b)^2 = a^2 - 2ab + b^2" : "(a + b)^2 = a^2 + 2ab + b^2", r.centerX(), r.top + dp(24), 10, TEXT);
        }

        private void drawDifferenceSquaresDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(44);
            float top = r.top + dp(50);
            float big = dp(118);
            float small = dp(50);
            drawTile(canvas, left, top, big, big, Color.rgb(167, 243, 208), "a^2");
            drawTile(canvas, left + big - small - dp(8), top + big - small - dp(8), small, small, Color.rgb(254, 202, 202), "-b^2");
            drawArrowDimension(canvas, left, top + big + dp(18), left + big, top + big + dp(18), "a");
            drawArrowDimension(canvas, left + big + dp(18), top + big - small - dp(8), left + big + dp(18), top + big - dp(8), "b");
            drawLabelBox(canvas, "a^2 - b^2 = (a-b)(a+b)", r.centerX(), r.top + dp(24), 10, TEXT);
        }

        private void drawOldAlgebraFallback(Canvas canvas, RectF r, String name) {
            float x = r.left + dp(26);
            float y = r.top + dp(34);
            float a = dp(74);
            float b = dp(44);
            drawTile(canvas, x, y, a, a, Color.rgb(167, 243, 208), "a²");
            drawTile(canvas, x + a + dp(8), y, b, a, Color.rgb(191, 219, 254), "ab");
            drawTile(canvas, x, y + a + dp(8), a, b, Color.rgb(191, 219, 254), "ab");
            drawTile(canvas, x + a + dp(8), y + a + dp(8), b, b, Color.rgb(253, 230, 138), "b²");
            String sign = name.contains("difference") || name.contains("minus") ? "minus term changes sign" : "(a + b)² = a² + 2ab + b²";
            drawSmallFormula(canvas, sign, r.centerX(), r.bottom - dp(18));
        }

        private void drawFinanceDiagram(Canvas canvas, RectF r, String name) {
            if (name.contains("profit percentage")) {
                drawFinanceFraction(canvas, r, "Profit%", "Profit", "CP", "x 100");
                return;
            }
            if (name.contains("discount percentage")) {
                drawFinanceFraction(canvas, r, "Discount%", "Discount", "MP", "x 100");
                return;
            }
            if (name.contains("compound amount")) {
                drawCompoundGrowth(canvas, r, "A final amount");
                return;
            }
            if (name.contains("compound interest")) {
                drawFinanceEquationBars(canvas, r, "A", "P", "CI", "CI = A - P");
                return;
            }
            if (name.contains("simple interest")) {
                drawSimpleInterestDiagram(canvas, r);
                return;
            }
            if (name.contains("tax")) {
                drawFinanceEquationBars(canvas, r, "Price", "Tax", "Total", "Tax = rate% x price");
                return;
            }
            if (name.equals("amount")) {
                drawFinanceEquationBars(canvas, r, "Principal", "Interest", "Amount", "Amount = P + Interest");
                return;
            }
            if (name.equals("profit")) {
                drawFinanceEquationBars(canvas, r, "CP", "Profit", "SP", "SP = CP + Profit");
                return;
            }
            if (name.equals("loss")) {
                drawFinanceEquationBars(canvas, r, "SP", "Loss", "CP", "CP = SP + Loss");
                return;
            }
            if (name.contains("discount")) {
                drawFinanceEquationBars(canvas, r, "SP", "Discount", "MP", "MP = SP + Discount");
                return;
            }
            float left = r.left + dp(22);
            float top = r.top + dp(38);
            float width = r.width() - dp(44);
            if (name.contains("loss")) {
                drawMoneyBar(canvas, left, top, width * 0.75f, "SP", Color.rgb(191, 219, 254));
                drawMoneyBar(canvas, left, top + dp(54), width, "CP", Color.rgb(252, 165, 165));
                drawText(canvas, "Loss", left + width * 0.86f, top + dp(34), 14, RED, Paint.Align.CENTER, true);
            } else if (name.contains("discount")) {
                drawMoneyBar(canvas, left, top, width, "MP", Color.rgb(253, 230, 138));
                drawMoneyBar(canvas, left, top + dp(54), width * 0.78f, "SP", Color.rgb(167, 243, 208));
                drawText(canvas, "discount", left + width * 0.88f, top + dp(88), 14, RED, Paint.Align.CENTER, true);
            } else {
                drawMoneyBar(canvas, left, top, width * 0.7f, "P / CP", Color.rgb(191, 219, 254));
                drawMoneyBar(canvas, left + width * 0.7f, top, width * 0.22f, "+ profit/interest", Color.rgb(167, 243, 208));
                drawMoneyBar(canvas, left, top + dp(58), width * 0.92f, "Amount / SP", Color.rgb(204, 251, 241));
                drawText(canvas, "compare values", r.centerX(), r.bottom - dp(18), 13, TEXT, Paint.Align.CENTER, true);
            }
        }

        private void drawFinanceEquationBars(Canvas canvas, RectF r, String first, String second, String result, String label) {
            float left = r.left + dp(26);
            float top = r.top + dp(62);
            float width = r.width() - dp(52);
            drawMoneyBar(canvas, left, top, width * 0.46f, first, Color.rgb(191, 219, 254));
            drawText(canvas, "+", left + width * 0.51f, top + dp(23), 18, TEXT, Paint.Align.CENTER, true);
            drawMoneyBar(canvas, left + width * 0.56f, top, width * 0.28f, second, Color.rgb(167, 243, 208));
            drawMoneyBar(canvas, left, top + dp(62), width * 0.84f, result, Color.rgb(253, 230, 138));
            drawLabelBox(canvas, label, r.centerX(), r.top + dp(25), 11, TEXT);
        }

        private void drawFinanceFraction(Canvas canvas, RectF r, String title, String topLabel, String bottomLabel, String tail) {
            float cx = r.centerX();
            float top = r.top + dp(58);
            drawMoneyBar(canvas, cx - dp(92), top, dp(184), topLabel, Color.rgb(167, 243, 208));
            drawLine(canvas, cx - dp(104), top + dp(48), cx + dp(104), top + dp(48), TEXT, dp(2));
            drawMoneyBar(canvas, cx - dp(92), top + dp(64), dp(184), bottomLabel, Color.rgb(191, 219, 254));
            drawText(canvas, tail, cx + dp(122), top + dp(58), 13, AMBER, Paint.Align.CENTER, true);
            drawLabelBox(canvas, title + " = top / bottom x 100", cx, r.top + dp(25), 11, TEXT);
        }

        private void drawSimpleInterestDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(28);
            float top = r.top + dp(62);
            float box = dp(58);
            drawTile(canvas, left, top, box, dp(42), Color.rgb(191, 219, 254), "P");
            drawText(canvas, "x", left + box + dp(18), top + dp(27), 16, TEXT, Paint.Align.CENTER, true);
            drawTile(canvas, left + box + dp(34), top, box, dp(42), Color.rgb(167, 243, 208), "R%");
            drawText(canvas, "x", left + box * 2 + dp(52), top + dp(27), 16, TEXT, Paint.Align.CENTER, true);
            drawTile(canvas, left + box * 2 + dp(68), top, box, dp(42), Color.rgb(253, 230, 138), "T");
            drawLabelBox(canvas, "SI = P x R x T / 100", r.centerX(), r.top + dp(25), 12, TEXT);
            drawText(canvas, "principal   rate   time", r.centerX(), top + dp(72), 12, MUTED, Paint.Align.CENTER, false);
        }

        private void drawCompoundGrowth(Canvas canvas, RectF r, String label) {
            float left = r.left + dp(34);
            float bottom = r.bottom - dp(44);
            float stepW = (r.width() - dp(68)) / 4f;
            for (int i = 0; i < 4; i++) {
                float h = dp(34 + i * 18);
                drawMoneyBar(canvas, left + i * stepW, bottom - h, stepW - dp(10), "P" + (i == 0 ? "" : "+" + i + "r"), Color.rgb(167, 243, 208));
            }
            drawText(canvas, "rate r repeats for n periods", r.centerX(), bottom + dp(20), 12, AMBER, Paint.Align.CENTER, true);
            drawLabelBox(canvas, "A = P(1 + r/100)^n", r.centerX(), r.top + dp(25), 11, TEXT);
            drawText(canvas, label, r.right - dp(34), bottom - dp(86), 11, TEXT, Paint.Align.RIGHT, true);
        }

        private void drawNumberDiagram(Canvas canvas, RectF r, String name) {
            if (name.contains("average")) {
                drawAverageDiagram(canvas, r);
                return;
            }
            if (name.contains("percentage") || name.contains("fraction to percent")) {
                drawPercentDiagram(canvas, r, name);
                return;
            }
            if (name.contains("ratio")) {
                drawRatioDiagram(canvas, r);
                return;
            }
            if (name.contains("unit price")) {
                drawUnitPriceDiagram(canvas, r);
                return;
            }
            if (name.contains("time")) {
                drawRouteDiagram(canvas, r, "Time = distance / speed", "distance", "speed");
                return;
            }
            if (name.contains("rate")) {
                drawRateDiagram(canvas, r);
                return;
            }
            if (name.contains("speed")) {
                drawRouteDiagram(canvas, r, "Speed = distance / time", "distance", "time");
                return;
            }
            if (name.contains("speed") || name.contains("time") || name.contains("rate")) {
                float y = r.centerY();
                drawLine(canvas, r.left + dp(26), y, r.right - dp(26), y, GREEN_DARK, dp(4));
                drawPoint(canvas, r.left + dp(30), y, "A");
                drawPoint(canvas, r.right - dp(30), y, "B");
                drawText(canvas, "distance", r.centerX(), y - dp(18), 14, TEXT, Paint.Align.CENTER, true);
                drawText(canvas, "time", r.centerX(), y + dp(30), 14, AMBER, Paint.Align.CENTER, true);
                return;
            }
            float left = r.left + dp(24);
            float top = r.top + dp(42);
            float cell = dp(26);
            for (int i = 0; i < 10; i++) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(i < 4 ? Color.rgb(45, 212, 191) : Color.rgb(226, 232, 240));
                RectF block = new RectF(left + i * (cell + dp(4)), top, left + i * (cell + dp(4)) + cell, top + dp(70));
                canvas.drawRoundRect(block, dp(7), dp(7), paint);
            }
            drawText(canvas, name.contains("average") ? "balance the dots" : "part / whole × 100", r.centerX(), r.bottom - dp(24), 15, TEXT, Paint.Align.CENTER, true);
        }

        private void drawAverageDiagram(Canvas canvas, RectF r) {
            float y = r.centerY() + dp(18);
            float start = r.left + dp(54);
            float gap = (r.width() - dp(108)) / 4f;
            int[] heights = {34, 58, 82, 50, 66};
            for (int i = 0; i < heights.length; i++) {
                float x = start + gap * i;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.rgb(45, 212, 191));
                canvas.drawCircle(x, y - dp(heights[i] / 2), dp(8), paint);
            }
            drawLine(canvas, start - dp(22), y - dp(29), start + gap * 4 + dp(22), y - dp(29), AMBER, dp(2));
            drawLabelBox(canvas, "Average = sum / count", r.centerX(), r.top + dp(25), 12, TEXT);
            drawText(canvas, "balance level", r.centerX(), y - dp(38), 12, AMBER, Paint.Align.CENTER, true);
        }

        private void drawPercentDiagram(Canvas canvas, RectF r, String name) {
            float left = r.left + dp(34);
            float top = r.top + dp(74);
            float width = r.width() - dp(68);
            RectF whole = new RectF(left, top, left + width, top + dp(42));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(226, 232, 240));
            canvas.drawRoundRect(whole, dp(12), dp(12), paint);
            RectF part = new RectF(left, top, left + width * 0.42f, top + dp(42));
            paint.setColor(Color.rgb(45, 212, 191));
            canvas.drawRoundRect(part, dp(12), dp(12), paint);
            drawText(canvas, "part", part.centerX(), part.centerY() + dp(5), 13, Color.WHITE, Paint.Align.CENTER, true);
            drawText(canvas, "whole = 100%", whole.centerX(), top + dp(68), 12, TEXT, Paint.Align.CENTER, true);
            drawLabelBox(canvas, name.contains("fraction") ? "percent = fraction x 100" : "percent = part / whole x 100", r.centerX(), r.top + dp(25), 11, TEXT);
        }

        private void drawRatioDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(54);
            float top = r.top + dp(72);
            float cell = dp(34);
            for (int i = 0; i < 2; i++) drawTile(canvas, left + i * (cell + dp(6)), top, cell, cell, Color.rgb(96, 165, 250), "a");
            for (int i = 0; i < 3; i++) drawTile(canvas, left + dp(100) + i * (cell + dp(6)), top, cell, cell, Color.rgb(45, 212, 191), "b");
            drawLabelBox(canvas, "ratio compares a : b", r.centerX(), r.top + dp(25), 12, TEXT);
            drawText(canvas, "2 parts : 3 parts", r.centerX(), top + dp(72), 12, TEXT, Paint.Align.CENTER, true);
        }

        private void drawUnitPriceDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(42);
            float top = r.top + dp(62);
            for (int i = 0; i < 5; i++) {
                drawTile(canvas, left + i * dp(42), top, dp(30), dp(38), Color.rgb(191, 219, 254), "item");
            }
            drawLine(canvas, left, top + dp(58), left + dp(198), top + dp(58), TEXT, dp(2));
            drawText(canvas, "total price", left + dp(99), top + dp(80), 12, TEXT, Paint.Align.CENTER, true);
            drawLabelBox(canvas, "unit price = total price / quantity", r.centerX(), r.top + dp(25), 11, TEXT);
        }

        private void drawRouteDiagram(Canvas canvas, RectF r, String label, String topLabel, String bottomLabel) {
            float y = r.centerY() + dp(10);
            float left = r.left + dp(42);
            float right = r.right - dp(42);
            drawLine(canvas, left, y, right, y, GREEN_DARK, dp(4));
            drawPoint(canvas, left, y, "start");
            drawPoint(canvas, right, y, "finish");
            drawText(canvas, topLabel, r.centerX(), y - dp(24), 13, TEXT, Paint.Align.CENTER, true);
            drawText(canvas, bottomLabel, r.centerX(), y + dp(34), 13, AMBER, Paint.Align.CENTER, true);
            drawLabelBox(canvas, label, r.centerX(), r.top + dp(25), 12, TEXT);
        }

        private void drawRateDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(54);
            float bottom = r.bottom - dp(58);
            for (int i = 0; i < 4; i++) {
                float h = dp(28 + i * 14);
                RectF bar = new RectF(left + i * dp(48), bottom - h, left + i * dp(48) + dp(30), bottom);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.rgb(45, 212, 191));
                canvas.drawRoundRect(bar, dp(7), dp(7), paint);
            }
            drawText(canvas, "quantity", left + dp(80), bottom - dp(90), 12, TEXT, Paint.Align.CENTER, true);
            drawText(canvas, "time", left + dp(80), bottom + dp(24), 12, AMBER, Paint.Align.CENTER, true);
            drawLabelBox(canvas, "rate = quantity / time", r.centerX(), r.top + dp(25), 12, TEXT);
        }

        private void drawProbabilityDiagram(Canvas canvas, RectF r) {
            float left = r.left + dp(30);
            float top = r.top + dp(32);
            float size = dp(38);
            drawLabelBox(canvas, "P(E) = favourable / total", r.centerX(), r.top + dp(25), 12, TEXT);
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    int n = row * 3 + col + 1;
                    RectF die = new RectF(left + col * (size + dp(14)), top + row * (size + dp(14)),
                            left + col * (size + dp(14)) + size, top + row * (size + dp(14)) + size);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(n == 6 ? Color.rgb(167, 243, 208) : Color.WHITE);
                    canvas.drawRoundRect(die, dp(8), dp(8), paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(1));
                    paint.setColor(Color.rgb(148, 163, 184));
                    canvas.drawRoundRect(die, dp(8), dp(8), paint);
                    drawText(canvas, String.valueOf(n), die.centerX(), die.centerY() + dp(6), 16, TEXT, Paint.Align.CENTER, true);
                }
            }
            drawText(canvas, "green = favourable outcome", r.centerX(), r.bottom - dp(38), 12, GREEN_DARK, Paint.Align.CENTER, true);
            drawText(canvas, "6 total outcomes", r.centerX(), r.bottom - dp(18), 12, TEXT, Paint.Align.CENTER, true);
        }

        private void drawSequenceDiagram(Canvas canvas, RectF r) {
            float y = r.centerY();
            float start = r.left + dp(32);
            float gap = (r.width() - dp(64)) / 4f;
            String name = formula.name.toLowerCase(Locale.US);
            drawLine(canvas, start - dp(10), y, start + gap * 4 + dp(10), y, Color.rgb(148, 163, 184), dp(2));
            for (int i = 0; i < 5; i++) {
                float x = start + gap * i;
                String label = i == 0 ? "a" : i == 4 && name.contains("nth") ? "a_n" : "a+" + i + "d";
                drawPoint(canvas, x, y, label);
                if (i < 4) drawText(canvas, "d", x + gap / 2f, y - dp(18), 13, AMBER, Paint.Align.CENTER, true);
            }
            String topLabel = name.contains("sum") ? "S_n adds first n terms"
                    : name.contains("common") ? "d = a2 - a1"
                    : "a_n = a + (n - 1)d";
            drawLabelBox(canvas, topLabel, r.centerX(), r.top + dp(25), 11, TEXT);
        }

        private void drawFallbackDiagram(Canvas canvas, RectF r) {
            drawTile(canvas, r.left + dp(32), r.top + dp(44), dp(74), dp(58), Color.rgb(204, 251, 241), "Known");
            drawTile(canvas, r.centerX() - dp(37), r.top + dp(44), dp(74), dp(58), Color.rgb(191, 219, 254), "Formula");
            drawTile(canvas, r.right - dp(106), r.top + dp(44), dp(74), dp(58), Color.rgb(253, 230, 138), "Answer");
            drawLine(canvas, r.left + dp(114), r.top + dp(73), r.centerX() - dp(47), r.top + dp(73), GREEN_DARK, dp(2));
            drawLine(canvas, r.centerX() + dp(47), r.top + dp(73), r.right - dp(116), r.top + dp(73), GREEN_DARK, dp(2));
            drawText(canvas, "see the pattern", r.centerX(), r.bottom - dp(22), 15, TEXT, Paint.Align.CENTER, true);
        }

        private void drawGrid(Canvas canvas, float left, float top, float right, float bottom) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(226, 232, 240));
            for (int i = 0; i <= 5; i++) {
                float x = left + (right - left) * i / 5f;
                float y = top + (bottom - top) * i / 5f;
                canvas.drawLine(x, top, x, bottom, paint);
                canvas.drawLine(left, y, right, y, paint);
            }
            drawLine(canvas, left, bottom, right, bottom, TEXT, dp(2));
            drawLine(canvas, left, bottom, left, top, TEXT, dp(2));
        }

        private void drawTile(Canvas canvas, float left, float top, float width, float height, int color, String label) {
            RectF rect = new RectF(left, top, left + width, top + height);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setShadowLayer(dp(4), 0, dp(2), Color.argb(38, 15, 118, 110));
            canvas.drawRoundRect(rect, dp(9), dp(9), paint);
            paint.clearShadowLayer();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(GREEN_DARK);
            canvas.drawRoundRect(rect, dp(9), dp(9), paint);
            drawText(canvas, label, rect.centerX(), rect.centerY() + dp(5), 14, TEXT, Paint.Align.CENTER, true);
        }

        private void drawMoneyBar(Canvas canvas, float left, float top, float width, String label, int color) {
            RectF bar = new RectF(left, top, left + width, top + dp(34));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            canvas.drawRoundRect(bar, dp(10), dp(10), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(GREEN_DARK);
            canvas.drawRoundRect(bar, dp(10), dp(10), paint);
            drawText(canvas, label, bar.centerX(), bar.centerY() + dp(5), 13, TEXT, Paint.Align.CENTER, true);
        }

        private Path rectPath(float left, float top, float right, float bottom) {
            Path p = new Path();
            p.addRect(left, top, right, bottom, Path.Direction.CW);
            return p;
        }

        private void fillPath(Canvas canvas, Path path, int startColor, int endColor) {
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(0, 0, getWidth(), getHeight(), startColor, endColor, Shader.TileMode.CLAMP));
            paint.setShadowLayer(dp(5), 0, dp(2), Color.argb(40, 15, 118, 110));
            canvas.drawPath(path, paint);
            paint.clearShadowLayer();
            paint.setShader(null);
        }

        private void strokePath(Canvas canvas, Path path, int color, float width) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(width);
            paint.setColor(color);
            paint.setStrokeJoin(Paint.Join.ROUND);
            canvas.drawPath(path, paint);
        }

        private void drawLine(Canvas canvas, float x1, float y1, float x2, float y2, int color, float width) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(width);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        private void drawDimension(Canvas canvas, float x1, float y1, float x2, float y2, String label) {
            drawLine(canvas, x1, y1, x2, y2, AMBER, dp(2));
            float tx = (x1 + x2) / 2f;
            float ty = (y1 + y2) / 2f;
            drawText(canvas, label, tx, ty - dp(5), 13, AMBER, Paint.Align.CENTER, true);
        }

        private void drawArrowDimension(Canvas canvas, float x1, float y1, float x2, float y2, String label) {
            drawLine(canvas, x1, y1, x2, y2, AMBER, dp(2));
            float angle = (float) Math.atan2(y2 - y1, x2 - x1);
            drawArrowHead(canvas, x1, y1, angle + (float) Math.PI);
            drawArrowHead(canvas, x2, y2, angle);

            boolean vertical = Math.abs(y2 - y1) > Math.abs(x2 - x1);
            float tx = (x1 + x2) / 2f;
            float ty = (y1 + y2) / 2f;
            if (vertical) {
                drawText(canvas, label, tx + dp(9), ty + dp(4), 12, AMBER, Paint.Align.LEFT, true);
            } else {
                drawText(canvas, label, tx, ty - dp(7), 12, AMBER, Paint.Align.CENTER, true);
            }
        }

        private void drawArrowHead(Canvas canvas, float x, float y, float angle) {
            float len = dp(8);
            float spread = 0.55f;
            Path head = new Path();
            head.moveTo(x, y);
            head.lineTo(x - len * (float) Math.cos(angle - spread), y - len * (float) Math.sin(angle - spread));
            head.moveTo(x, y);
            head.lineTo(x - len * (float) Math.cos(angle + spread), y - len * (float) Math.sin(angle + spread));
            strokePath(canvas, head, AMBER, dp(2));
        }

        private void drawPoint(Canvas canvas, float x, float y, String label) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(GREEN);
            canvas.drawCircle(x, y, dp(6), paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, dp(3), paint);
            drawText(canvas, label, x, y - dp(12), 12, TEXT, Paint.Align.CENTER, true);
        }

        private void drawRightAngle(Canvas canvas, float x, float y) {
            Path p = new Path();
            p.moveTo(x, y + dp(24));
            p.lineTo(x + dp(24), y + dp(24));
            p.lineTo(x + dp(24), y);
            strokePath(canvas, p, AMBER, dp(2));
        }

        private void drawSmallFormula(Canvas canvas, String text, float x, float y) {
            drawText(canvas, text, x, y, 13, TEXT, Paint.Align.CENTER, true);
        }

        private void drawLabelBox(Canvas canvas, String text, float x, float y, int sizeSp, int color) {
            paint.setShader(null);
            paint.clearShadowLayer();
            paint.setTextSize(dp(sizeSp));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            float width = paint.measureText(text) + dp(14);
            float height = dp(sizeSp + 12);
            RectF box = new RectF(x - width / 2f, y - height + dp(4), x + width / 2f, y + dp(6));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(235, 255, 255, 255));
            canvas.drawRoundRect(box, dp(9), dp(9), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(209, 250, 229));
            canvas.drawRoundRect(box, dp(9), dp(9), paint);
            drawText(canvas, text, x, y, sizeSp, color, Paint.Align.CENTER, true);
        }

        private void drawText(Canvas canvas, String text, float x, float y, int sizeSp, int color, Paint.Align align, boolean bold) {
            paint.setShader(null);
            paint.clearShadowLayer();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(dp(sizeSp));
            paint.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            paint.setTextAlign(align);
            canvas.drawText(text, x, y, paint);
        }
    }

    private static class Formula {
        final String id;
        final int introducedClass;
        final String topic;
        final String name;
        final String formula;
        final String explanation;
        final String example;
        final String meaningQuestion;
        final String[] meaningOptions;
        final String meaningAnswer;

        Formula(String id, int introducedClass, String topic, String name, String formula, String explanation, String example, String meaningQuestion, String[] meaningOptions, String meaningAnswer) {
            this.id = id;
            this.introducedClass = introducedClass;
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
            f.add(new Formula("c10_quadratic_formula", 10, "Quadratic Equations", "Quadratic Formula", "x = (-b ± √(b² - 4ac)) / (2a)", "This solves ax² + bx + c = 0.", "For x² - 5x + 6 = 0, solutions are x=2 and x=3.", "What expression is under the square root?", new String[]{"b² - 4ac", "b² + 4ac", "2a", "-b"}, "b² - 4ac"));
            f.add(new Formula("c10_discriminant", 10, "Quadratic Equations", "Discriminant", "D = b² - 4ac", "Discriminant helps identify the type of roots of a quadratic equation.", "If D > 0, there are two real roots.", "In D = b² - 4ac, what does D mean?", new String[]{"Discriminant", "Diameter", "Distance", "Degree"}, "Discriminant"));
            f.add(new Formula("c10_ap_sum", 10, "Sequences", "Sum of AP", "Sₙ = n/2 × [2a + (n - 1)d]", "This finds the sum of first n terms of an arithmetic progression.", "If a=2, d=3, n=5, Sₙ=5/2 × [4+12]=40.", "In Sₙ, what does n mean?", new String[]{"Number of terms", "Numerator only", "Negative value", "New term"}, "Number of terms"));
            f.add(new Formula("c10_trig_identity", 10, "Trigonometry", "Pythagorean Trig Identity", "sin²θ + cos²θ = 1", "This identity is true for every angle θ.", "If sin²θ = 0.36, then cos²θ = 0.64.", "What does θ represent?", new String[]{"Angle", "Area", "Volume", "Radius"}, "Angle"));
            f.add(new Formula("c10_tan_identity", 10, "Trigonometry", "Tangent Identity", "tan θ = sin θ / cos θ", "Tangent can be written using sine and cosine.", "If sin θ=3/5 and cos θ=4/5, tan θ=3/4.", "What is in the denominator of tan θ = sin θ / cos θ?", new String[]{"cos θ", "sin θ", "tan θ", "1"}, "cos θ"));
            f.add(new Formula("c10_compound_interest", 10, "Finance", "Compound Amount", "A = P(1 + r/100)ⁿ", "Compound amount grows by a percentage rate every period.", "If P=1000, r=10, n=2, A=1210.", "In A = P(1 + r/100)ⁿ, what does n mean?", new String[]{"Number of periods", "Numerator", "New amount", "Negative rate"}, "Number of periods"));
            f.add(new Formula("c10_sector_area", 10, "Circle", "Area of Sector", "Area = θ/360 × πr²", "Sector area is a fraction of the full circle area.", "For θ=90°, area=90/360×πr²=1/4πr².", "In sector formula, what does θ mean?", new String[]{"Central angle", "Diameter", "Height", "Slope"}, "Central angle"));
            f.add(new Formula("c10_arc_length", 10, "Circle", "Arc Length", "Arc = θ/360 × 2πr", "Arc length is a fraction of the full circumference.", "For θ=180°, arc=1/2×2πr=πr.", "What full-circle formula is used here?", new String[]{"2πr", "πr²", "4s", "l × w"}, "2πr"));
            f.add(new Formula("c10_probability", 10, "Probability", "Probability", "P(E) = Favourable Outcomes / Total Outcomes", "Probability measures chance of an event.", "For rolling a 6 on a die, P(E)=1/6.", "What does P(E) mean?", new String[]{"Probability of event", "Perimeter of event", "Price estimate", "Power equation"}, "Probability of event"));

            addMoreFormulas(f);
            return f;
        }

        private static void addMoreFormulas(List<Formula> f) {
            // Extra Class 6 formulas
            f.add(new Formula("c6_triangle_perimeter", 6, "Area & Perimeter", "Perimeter of Triangle", "P = a + b + c", "Perimeter is the total length around a triangle.", "If sides are 3, 4, and 5, P = 12.", "In P = a + b + c, what are a, b, and c?", new String[]{"Side lengths", "Angles", "Areas", "Percentages"}, "Side lengths"));
            f.add(new Formula("c6_parallelogram_area", 6, "Area & Perimeter", "Area of Parallelogram", "A = b × h", "Area of a parallelogram is base times height.", "If b = 9 and h = 4, A = 36.", "In A = b × h, what does b mean?", new String[]{"Base", "Breadth only", "Balance", "Boundary"}, "Base"));
            f.add(new Formula("c6_ratio", 6, "Number Skills", "Ratio", "Ratio = a : b", "A ratio compares two quantities.", "If boys=2 and girls=3, ratio = 2:3.", "What does a ratio compare?", new String[]{"Two quantities", "Only money", "Only angles", "Only speed"}, "Two quantities"));
            f.add(new Formula("c6_fraction_percent", 6, "Number Skills", "Fraction to Percent", "Percent = Fraction × 100", "Multiply a fraction by 100 to convert it to percent.", "1/4 × 100 = 25%.", "What do you multiply by?", new String[]{"100", "10", "1", "0"}, "100"));

            // Extra Class 7 formulas
            f.add(new Formula("c7_discount_percent", 7, "Profit & Loss", "Discount Percentage", "Discount% = Discount/MP × 100", "Discount percent compares discount with marked price.", "If discount=50 and MP=500, Discount% = 10%.", "In Discount% formula, MP means?", new String[]{"Marked Price", "Maths Price", "Maximum Profit", "Main Point"}, "Marked Price"));
            f.add(new Formula("c7_tax", 7, "Finance", "Tax Amount", "Tax = Rate% × Price", "Tax is added as a percentage of price.", "If price=1000 and tax=5%, tax=50.", "Tax rate is usually written as what?", new String[]{"Percentage", "Area", "Distance", "Angle"}, "Percentage"));
            f.add(new Formula("c7_time", 7, "Number Skills", "Time", "Time = Distance / Speed", "Use this when distance and speed are known.", "If distance=120 km and speed=60 km/h, time=2 h.", "In this formula, speed is in the denominator. What does denominator mean?", new String[]{"Bottom part", "Top part", "Answer", "Unit only"}, "Bottom part"));
            f.add(new Formula("c7_rate", 7, "Number Skills", "Rate", "Rate = Quantity / Time", "Rate tells how much happens per unit time.", "If 30 pages are read in 3 hours, rate = 10 pages/hour.", "Rate compares quantity with what?", new String[]{"Time", "Area", "Mass", "Angle"}, "Time"));

            // Extra Class 8 formulas
            f.add(new Formula("c8_identity_product", 8, "Algebra Identities", "Product of Sum and Difference", "(a + b)(a - b) = a² - b²", "This identity is useful in factorization.", "(x + 5)(x - 5) = x² - 25.", "What does this identity produce?", new String[]{"Difference of squares", "Sum of squares", "Cube", "Ratio"}, "Difference of squares"));
            f.add(new Formula("c8_cone_volume", 8, "Mensuration", "Volume of Cone", "V = (1/3)πr²h", "Cone volume is one third of cylinder volume with same base and height.", "If r=3, h=4, V=12π.", "What does r mean?", new String[]{"Radius", "Rate", "Ratio", "Rectangle"}, "Radius"));
            f.add(new Formula("c8_sphere_volume", 8, "Mensuration", "Volume of Sphere", "V = 4/3πr³", "Sphere volume depends on radius cubed.", "If r=3, V=36π.", "What power is used on r?", new String[]{"3", "2", "1", "0"}, "3"));
            f.add(new Formula("c8_sphere_surface", 8, "Mensuration", "Surface Area of Sphere", "SA = 4πr²", "This finds the outside area of a sphere.", "If r=7, SA=196π.", "What does SA mean?", new String[]{"Surface Area", "Simple Average", "Side Angle", "Square Answer"}, "Surface Area"));

            // Extra Class 9 formulas
            f.add(new Formula("c9_line_standard", 9, "Algebra", "Linear Standard Form", "ax + by + c = 0", "A straight line can be written in standard form.", "2x + 3y - 6 = 0 is standard form.", "What type of graph does this represent?", new String[]{"Straight line", "Circle", "Parabola only", "Cube"}, "Straight line"));
            f.add(new Formula("c9_heron", 9, "Geometry", "Heron's Formula", "A = √(s(s-a)(s-b)(s-c))", "Heron's formula finds triangle area using three sides.", "First find s = (a+b+c)/2, then use the formula.", "What does s mean here?", new String[]{"Semi-perimeter", "Speed", "Side only", "Slope"}, "Semi-perimeter"));
            f.add(new Formula("c9_semiperimeter", 9, "Geometry", "Semi-perimeter", "s = (a + b + c) / 2", "Semi-perimeter is half the perimeter of a triangle.", "For sides 3, 4, 5: s = 6.", "Semi means what?", new String[]{"Half", "Double", "Square", "Angle"}, "Half"));
            f.add(new Formula("c9_ap_common_difference", 9, "Sequences", "Common Difference", "d = a₂ - a₁", "The common difference is the gap between consecutive AP terms.", "For 5, 8, 11, d = 3.", "What does consecutive mean?", new String[]{"Next to each other", "Very large", "Squared", "Divided"}, "Next to each other"));

            // Extra Class 10 formulas
            f.add(new Formula("c10_compound_interest_ci", 10, "Finance", "Compound Interest", "CI = A - P", "Compound interest is final amount minus principal.", "If A=1210 and P=1000, CI=210.", "What does P mean?", new String[]{"Principal", "Profit", "Percentage", "Perimeter"}, "Principal"));
            f.add(new Formula("c10_quadratic_axis", 10, "Quadratic Equations", "Axis of Symmetry", "x = -b / (2a)", "This gives the vertical symmetry line of y = ax² + bx + c.", "For y=x²-4x+3, x=2.", "Axis of symmetry is a what?", new String[]{"Line", "Area", "Volume", "Ratio"}, "Line"));
        }
    }
}
