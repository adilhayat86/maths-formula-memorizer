# Run lesson and quiz polish update

This update makes the learning flow feel more like a polished memorization product.

No screenshots are created by this update.

## Before running

If GitHub Desktop shows local changes from the previous product polish update, commit and push those first.

Commit summary:

```text
Polish product experience
```

## Steps using GitHub Desktop

1. Open GitHub Desktop.
2. Select `maths-formula-memorizer`.
3. Click **Fetch origin**.
4. Click **Pull origin**.
5. Go to **Repository > Open in Command Prompt**.
6. Run:

```cmd
python scripts\apply_lesson_quiz_polish_update.py
```

If `python` is not recognized, run:

```cmd
py scripts\apply_lesson_quiz_polish_update.py
```

7. Open Android Studio and press **Run**.
8. Test the app.
9. If it works, return to GitHub Desktop.
10. Commit with this summary:

```text
Polish lesson and quiz flow
```

11. Click **Push origin**.

## What this update improves

- Lesson screen looks like a learning card instead of a plain page
- Formula explanation and example are clearer
- Quiz screen has a question progress card
- Feedback screen is more encouraging
- Lesson-complete screen shows stars and mastery/review status
- Mistake review screen starts with the weakest formula
- Launcher icon vectors are improved

## Test checklist

- App opens without crashing
- Home screen still works
- Start first lesson works
- Lesson screen shows formula, meaning, and example
- Start quiz works
- Question 1, 2, 3 work
- Correct and wrong feedback screens work
- Lesson complete screen works
- Mistake review opens
- Progress report still opens
- Back buttons work

## Do not do yet

Do not take Play Store screenshots yet. We will do screenshots later after the app icon and final wording are ready.
