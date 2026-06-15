# Run navigation and coaching update

This update improves the app navigation and makes the quiz feel more personal.

No screenshots are created by this update.

## What it adds

- Ask student name after class selection
- Address the student by name on home, lesson, quiz, and progress screens
- Save the name only on the device
- Change student name from home/progress
- Show hint during quiz
- Quit quiz option
- Confirm before abandoning quiz
- Better navigation from lesson to area/home
- Praise comments on correct answers
- Firm coaching comments on wrong answers
- Privacy policy draft note for local-only student name
- Store listing mention of hints and coaching

## Important

The firm wrong-answer feedback is intentionally not insulting. It is strict coaching, not humiliation. This is safer for a student app.

## Before running

If GitHub Desktop shows local changes from previous updates, commit and push them first.

Suggested commit summaries:

```text
Polish product experience
Polish lesson and quiz flow
```

## Steps using GitHub Desktop

1. Open GitHub Desktop.
2. Select `maths-formula-memorizer`.
3. Click **Fetch origin**.
4. Click **Pull origin**.
5. Go to **Repository > Open in Command Prompt**.
6. Run:

```cmd
python scripts\apply_navigation_coach_update.py
```

If `python` is not recognized, run:

```cmd
py scripts\apply_navigation_coach_update.py
```

7. Open Android Studio and press **Run**.
8. Test the app.
9. If it works, return to GitHub Desktop.
10. Commit with this summary:

```text
Improve navigation and coaching
```

11. Click **Push origin**.

## Test checklist

- New install asks for class
- Then asks for student name
- Home says `Hi, name`
- Change student name works
- Start lesson works
- Back to area works
- Home button works
- Start quiz works
- Show hint works
- Quit quiz opens confirmation
- Continue quiz returns to the same question
- Leave quiz goes home without completing lesson
- Correct answer shows praise
- Wrong answer shows firm coaching
- Progress report opens
- Privacy draft still says no internet/no account

## Do not do yet

Do not take Play Store screenshots yet. We will do screenshots later after final UI, formulas, and icon are ready.
