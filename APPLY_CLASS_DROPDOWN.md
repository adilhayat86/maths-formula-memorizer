# Apply class dropdown update

This repo contains a patch at:

```text
patches/class-dropdown-update.patch
```

The patch changes:

- Class selection from many buttons to one dropdown menu.
- Home screen wording from `Learning path` to `Choose area of study`.
- Area buttons from `Open unit` to `Start area`.
- A class dropdown at the top of the home screen so class can be changed quickly.

## Apply on Windows

Open Command Prompt or Android Studio Terminal inside the project folder and run:

```cmd
git pull
git apply patches/class-dropdown-update.patch
git status
git add README.md app/src/main/java/com/mominhayat/mathsformulamemorizer/MainActivity.java
git commit -m "Use class dropdown and improve study area wording"
git push
```

Then open Android Studio and press Run.

## If git apply says patch failed

Run:

```cmd
git status
```

If you have local changes, commit or discard them first. Then try again.
