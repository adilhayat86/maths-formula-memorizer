# Run product polish update

I prepared a product-polish updater script at:

```text
scripts/apply_product_polish_update.py
```

## Steps using GitHub Desktop

1. Open GitHub Desktop.
2. Select `maths-formula-memorizer`.
3. Click **Fetch origin**.
4. Click **Pull origin**.
5. Go to **Repository > Open in Command Prompt**.
6. Run:

```cmd
python scripts\apply_product_polish_update.py
```

If `python` is not recognized, run:

```cmd
py scripts\apply_product_polish_update.py
```

7. Open Android Studio and press **Run**.
8. If the app builds and looks good, return to GitHub Desktop.
9. Commit with this summary:

```text
Polish product experience
```

10. Click **Push origin**.

## What this update improves

- More professional rounded cards and buttons
- Better home screen with today’s goal
- Accuracy, streak, and weak formula count
- Area progress bars
- Formula status icons
- Improved progress report
- More formulas for Class 6–10
- Better Play Store listing draft

## Test checklist after running

- App opens without crashing
- Class dropdown works
- Progress card shows correctly
- Choose area of study appears
- Area cards show progress bars
- Start area opens lessons
- Quiz still works
- Lesson complete screen works
- Weak formulas still work
- Progress report opens
