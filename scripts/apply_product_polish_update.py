from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_PATH = ROOT / "app" / "src" / "main" / "java" / "com" / "mominhayat" / "mathsformulamemorizer" / "MainActivity.java"
README_PATH = ROOT / "README.md"
STORE_PATH = ROOT / "STORE_LISTING_DRAFT.md"

java = JAVA_PATH.read_text(encoding="utf-8").replace("\r\n", "\n")
original_java = java

# --- Imports and constants -------------------------------------------------
if "import android.graphics.drawable.GradientDrawable;" not in java:
    java = java.replace("import android.graphics.Typeface;\n", "import android.graphics.Typeface;\nimport android.graphics.drawable.GradientDrawable;\n")

if "private static final int GREEN_LIGHT" not in java:
    java = java.replace(
        "    private static final int RED = Color.rgb(185, 28, 28);\n",
        "    private static final int RED = Color.rgb(185, 28, 28);\n"
        "    private static final int GREEN_LIGHT = Color.rgb(220, 252, 231);\n"
        "    private static final int BORDER = Color.rgb(209, 250, 229);\n"
        "    private static final int AMBER = Color.rgb(245, 158, 11);\n"
        "    private static final int BLUE = Color.rgb(37, 99, 235);\n"
    )

# --- Home screen -----------------------------------------------------------
old_show_home = '''    private void showHome() {
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

new_show_home = '''    private void showHome() {
        if (selectedClass == null) {
            showClassSelect();
            return;
        }

        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;
        int accuracy = currentAccuracy();
        int weakCount = countWeakFormulas(available);
        int percent = percent(completedCount, available.size());

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

        LinearLayout hero = cardTint(GREEN_LIGHT);
        hero.addView(sectionTitle("Today’s goal"));
        hero.addView(bigText("Learn 1 formula in 60 seconds"));
        hero.addView(smallText("Small lessons, instant feedback, and mistake review."));
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
'''

if old_show_home in java:
    java = java.replace(old_show_home, new_show_home)

# --- Topic screen ----------------------------------------------------------
old_show_topic = '''    private void showTopic(String topic) {
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
'''

new_show_topic = '''    private void showTopic(String topic) {
        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        List<Formula> topicFormulas = new ArrayList<>();
        for (Formula f : available) if (f.topic.equals(topic)) topicFormulas.add(f);
        int done = topicCompleted(topicFormulas, completed);
        int weak = countWeakFormulas(topicFormulas);

        LinearLayout root = baseScreen(topicIcon(topic) + " " + topic, selectedClass + " • 1-minute formula lessons");

        LinearLayout summary = cardTint(GREEN_LIGHT);
        summary.addView(bigText(done + " / " + topicFormulas.size() + " formulas completed"));
        addProgressBar(summary, topicFormulas.size(), done);
        summary.addView(smallText(weak == 0 ? "No weak formulas in this area yet." : weak + " formula(s) need review in this area."));
        root.addView(summary);

        for (Formula f : topicFormulas) {
            LinearLayout item = card();
            item.addView(bigText(formulaStatus(f, completed) + " " + f.name));
            item.addView(smallText("Class " + f.classNumber + " • " + f.topic));
            item.addView(formulaText(f.formula));
            item.addView(smallText(f.explanation));
            Button open = completed.contains(f.id) ? secondaryButton("Review again") : primaryButton("Start lesson");
            open.setOnClickListener(v -> showLesson(f));
            item.addView(open);
            root.addView(item);
        }

        Button back = ghostButton("Back to home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }
'''

if old_show_topic in java:
    java = java.replace(old_show_topic, new_show_topic)

# --- Finish lesson ---------------------------------------------------------
old_finish_lesson_card = '''        LinearLayout root = baseScreen("Lesson complete", f.name);
        LinearLayout card = card();
        card.addView(bigText("Score: " + session.correct + " / " + session.questions.size()));
        if (session.correct == session.questions.size()) {
            card.addView(bodyText("Excellent. This formula will appear less often in weak practice."));
        } else {
            card.addView(bodyText("Good practice. This formula has been added to weak formulas so you can repeat it."));
        }
        root.addView(card);

        Button continueBtn = primaryButton("Continue practice");
'''
new_finish_lesson_card = '''        LinearLayout root = baseScreen("Lesson complete", f.name);
        LinearLayout card = cardTint(session.correct == session.questions.size() ? GREEN_LIGHT : Color.rgb(255, 251, 235));
        card.addView(bigText("Score: " + session.correct + " / " + session.questions.size()));
        if (session.correct == session.questions.size()) {
            card.addView(bodyText("Excellent. This formula is marked as mastered."));
        } else {
            card.addView(bodyText("Good practice. This formula is now in mistake review."));
        }
        card.addView(smallText("Keep lessons short. One formula at a time builds memory faster."));
        root.addView(card);

        Button continueBtn = primaryButton("Next formula");
'''
java = java.replace(old_finish_lesson_card, new_finish_lesson_card)

# --- Progress screen -------------------------------------------------------
old_show_progress = '''    private void showProgress() {
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
'''

new_show_progress = '''    private void showProgress() {
        List<Formula> available = availableFormulas();
        Set<String> completed = getCompletedSet();
        int completedCount = 0;
        for (Formula f : available) if (completed.contains(f.id)) completedCount++;
        int accuracy = currentAccuracy();
        int weakCount = countWeakFormulas(available);

        LinearLayout root = baseScreen("Progress report", selectedClass);
        LinearLayout c = cardTint(GREEN_LIGHT);
        c.addView(bigText("Overall progress: " + percent(completedCount, available.size()) + "%"));
        addProgressBar(c, available.size(), completedCount);
        LinearLayout stats = row();
        stats.addView(statBox("Lessons", completedCount + "/" + available.size()), rowWeight());
        stats.addView(statBox("Accuracy", accuracy + "%"), rowWeight());
        stats.addView(statBox("Streak", prefs.getInt("streak", 0) + "d"), rowWeight());
        c.addView(stats);
        c.addView(smallText("Progress is saved only on this phone. No account and no internet needed."));
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

        Button reset = ghostButton("Reset progress for this phone");
        reset.setTextColor(RED);
        reset.setOnClickListener(v -> showResetConfirm());
        root.addView(reset);

        Button back = primaryButton("Back to home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }
'''

if old_show_progress in java:
    java = java.replace(old_show_progress, new_show_progress)

# --- Better UI helpers -----------------------------------------------------
old_card = '''    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackgroundColor(CARD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(10));
        c.setLayoutParams(lp);
        return c;
    }
'''
new_card = '''    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackground(roundedStroke(CARD, Color.rgb(229, 231, 235), 14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(10));
        c.setLayoutParams(lp);
        return c;
    }
'''
java = java.replace(old_card, new_card)

old_primary_bg = "        b.setBackgroundColor(GREEN);\n"
java = java.replace(old_primary_bg, "        b.setBackground(rounded(GREEN, 12));\n")
old_secondary_bg = "        b.setBackgroundColor(Color.rgb(236, 253, 245));\n"
java = java.replace(old_secondary_bg, "        b.setBackground(roundedStroke(Color.rgb(236, 253, 245), BORDER, 12));\n")

helper_block = '''
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

    private void addProgressBar(LinearLayout parent, int max, int progress) {
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(Math.max(1, max));
        bar.setProgress(Math.max(0, progress));
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
'''

if "private LinearLayout cardTint" not in java:
    java = java.replace("    private LinearLayout row() {\n", helper_block + "\n    private LinearLayout row() {\n")

# --- Extra formulas --------------------------------------------------------
extra_formulas = '''
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
            f.add(new Formula("c8_cone_volume", 8, "Mensuration", "Volume of Cone", "V = 1/3πr²h", "Cone volume is one third of cylinder volume with same base and height.", "If r=3, h=4, V=12π.", "What does r mean?", new String[]{"Radius", "Rate", "Ratio", "Rectangle"}, "Radius"));
            f.add(new Formula("c8_sphere_volume", 8, "Mensuration", "Volume of Sphere", "V = 4/3πr³", "Sphere volume depends on radius cubed.", "If r=3, V=36π.", "What power is used on r?", new String[]{"3", "2", "1", "0"}, "3"));
            f.add(new Formula("c8_sphere_surface", 8, "Mensuration", "Surface Area of Sphere", "SA = 4πr²", "This finds the outside area of a sphere.", "If r=7, SA=196π.", "What does SA mean?", new String[]{"Surface Area", "Simple Average", "Side Angle", "Square Answer"}, "Surface Area"));

            // Extra Class 9 formulas
            f.add(new Formula("c9_line_standard", 9, "Algebra", "Linear Standard Form", "ax + by + c = 0", "A straight line can be written in standard form.", "2x + 3y - 6 = 0 is standard form.", "What type of graph does this represent?", new String[]{"Straight line", "Circle", "Parabola only", "Cube"}, "Straight line"));
            f.add(new Formula("c9_heron", 9, "Geometry", "Heron's Formula", "A = √(s(s-a)(s-b)(s-c))", "Heron's formula finds triangle area using three sides.", "First find s = (a+b+c)/2, then use the formula.", "What does s mean here?", new String[]{"Semi-perimeter", "Speed", "Side only", "Slope"}, "Semi-perimeter"));
            f.add(new Formula("c9_semiperimeter", 9, "Geometry", "Semi-perimeter", "s = (a + b + c) / 2", "Semi-perimeter is half the perimeter of a triangle.", "For sides 3, 4, 5: s = 6.", "Semi means what?", new String[]{"Half", "Double", "Square", "Angle"}, "Half"));
            f.add(new Formula("c9_ap_common_difference", 9, "Sequences", "Common Difference", "d = a₂ - a₁", "The common difference is the gap between consecutive AP terms.", "For 5, 8, 11, d = 3.", "What does consecutive mean?", new String[]{"Next to each other", "Very large", "Squared", "Divided"}, "Next to each other"));

            // Extra Class 10 formulas
            f.add(new Formula("c10_compound_interest", 10, "Finance", "Compound Amount", "A = P(1 + R/100)ⁿ", "Compound amount grows by percentage each period.", "If P=1000, R=10, n=2, A=1210.", "What does n mean?", new String[]{"Number of periods", "New amount only", "Negative value", "Numerator"}, "Number of periods"));
            f.add(new Formula("c10_compound_interest_ci", 10, "Finance", "Compound Interest", "CI = A - P", "Compound interest is final amount minus principal.", "If A=1210 and P=1000, CI=210.", "What does P mean?", new String[]{"Principal", "Profit", "Percentage", "Perimeter"}, "Principal"));
            f.add(new Formula("c10_probability", 10, "Probability", "Probability", "P(E) = Favourable / Total", "Probability compares favourable outcomes with total outcomes.", "For one head in a coin toss, P=1/2.", "Probability is usually between what values?", new String[]{"0 and 1", "1 and 10", "10 and 100", "Only negative"}, "0 and 1"));
            f.add(new Formula("c10_quadratic_axis", 10, "Quadratic Equations", "Axis of Symmetry", "x = -b / 2a", "This gives the vertical symmetry line of y = ax² + bx + c.", "For y=x²-4x+3, x=2.", "Axis of symmetry is a what?", new String[]{"Line", "Area", "Volume", "Ratio"}, "Line"));
        }
'''

if "addMoreFormulas(f);" not in java:
    java = java.replace("            return f;\n        }\n", "            addMoreFormulas(f);\n            return f;\n        }\n" + extra_formulas)

# --- README and store copy -------------------------------------------------
README_PATH.write_text("""# Maths Formula Memorizer Offline

A simple native Android app for memorizing maths formulas with a Duolingo-style flow:

1. Choose class from a dropdown
2. Choose area of study
3. Learn one formula
4. Answer 3 quick questions
5. Repeat weak formulas
6. Track progress and streak locally

## What is already built

- Native Android Java app
- No server
- No login
- No ads
- No analytics
- No INTERNET permission in AndroidManifest.xml
- Local progress saved with SharedPreferences
- Class selection using a drop-down menu: Class 6, 7, 8, 9, 10, General Practice
- Choose area of study screen grouped by topic
- Polished home screen with today goal, accuracy, streak, and weak-formula count
- Formula lesson screen
- 3-question quiz screen
- Weak formulas screen
- Progress report with area-by-area progress
- App icon vector placeholder
- Formula database included in MainActivity.java

## How to open on Windows

1. Install Android Studio.
2. Open Android Studio.
3. Click **Open**.
4. Select this folder: `maths-formula-memorizer`.
5. Wait for Gradle sync.
6. Connect your Android phone with USB debugging enabled, or use an emulator.
7. Press **Run**.

## Product principle

The app should feel useful in 30 seconds:

Open app → choose class → choose area → learn one formula → answer 3 questions.

No login. No ads. No internet. No distractions.

## How to change the app name

Edit:

`app/src/main/res/values/strings.xml`

```xml
<string name="app_name">Maths Formula Memorizer</string>
```

## How to change package name before Play Store

Current package/application id:

`com.mominhayat.mathsformulamemorizer`

For Play Store, keep it stable. Once uploaded, do not change package name.

Edit in:

`app/build.gradle`

```gradle
namespace 'com.mominhayat.mathsformulamemorizer'
applicationId 'com.mominhayat.mathsformulamemorizer'
```

Also move the Java file folder if you rename the package.

## How to add formulas

Open:

`app/src/main/java/com/mominhayat/mathsformulamemorizer/MainActivity.java`

Search for:

```java
private static class FormulaRepository
```

Add formulas using this pattern:

```java
f.add(new Formula(
    "unique_id_here",
    8,
    "Topic Name",
    "Formula Name",
    "Formula here",
    "Short explanation here.",
    "Short example here.",
    "Meaning question here?",
    new String[]{"Correct option", "Wrong option", "Wrong option", "Wrong option"},
    "Correct option"
));
```

## Build a signed AAB later

In Android Studio:

1. Build > Generate Signed Bundle / APK
2. Choose Android App Bundle
3. Create or choose signing key
4. Build release
5. Upload the `.aab` to Play Console

## Important before launch

- Review every formula for accuracy.
- Replace placeholder icon with a professional Play Store icon.
- Test on at least 2 Android phones.
- Prepare Play Store screenshots.
- For a new personal Google Play account, expect closed testing before production release.

## Physics app later

This app can become a template. For Physics:

1. Copy this project.
2. Change app name.
3. Change package name.
4. Replace formula database.
5. Change icon and screenshots.
""", encoding="utf-8", newline="\n")

STORE_PATH.write_text("""# Store Listing Draft

## App name
Maths Formula Memorizer Offline

## Short description
Memorize maths formulas with quick lessons, quizzes, and mistake review. No ads.

## Full description
Maths Formula Memorizer Offline helps students remember important maths formulas using short, focused practice.

Choose your class, pick an area of study, learn one formula, and answer a quick 3-question quiz. The app repeats weak formulas so students can improve without distractions.

Key features:
- Class-wise formula practice
- Areas such as algebra, geometry, mensuration, trigonometry, finance, sequences, and probability
- Short 1-minute lessons
- 3-question quizzes
- Weak formula review
- Progress, accuracy, and streak tracking
- Works fully offline
- No ads
- No login
- No internet required

Designed for fast daily practice. Open the app, learn one formula, and close it.

## Privacy-friendly promise
This app does not require an account, does not show ads, and does not use internet permission. Progress is saved only on the user's device.

## Screenshot captions
1. Choose your class and start quickly
2. Pick an area of study
3. Learn one formula at a time
4. Answer quick quiz questions
5. Review weak formulas
6. Track progress and streak
""", encoding="utf-8", newline="\n")

if java != original_java:
    JAVA_PATH.write_text(java, encoding="utf-8", newline="\n")
    print("Updated MainActivity.java with product polish.")
else:
    print("MainActivity.java already looks polished or expected blocks were not found.")
print("Updated README.md and STORE_LISTING_DRAFT.md.")
