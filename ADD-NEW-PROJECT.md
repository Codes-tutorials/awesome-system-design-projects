# Adding New Projects to the Repository

## Current Structure

All 15 projects are already included:
```
awesome-system-design-projects/
├── distributed-api-rate-limiter/
├── distributed-logging-system/
├── distributed-rate-limiter/
├── elevator-system-design/
├── hotel-management-system/
├── interface-anti-shake-demo/
├── kafka-batch-producer-demo/
├── large-table-update-demo/
├── parking-lot-system-design/
├── redis-cache-consistency-demo/
├── retry-safe-rate-limiter/
├── shared-db-refactoring/
├── twitter-rate-limiter/
├── vending-machine-system-design/
└── weibo-hot-list-demo/
```

## Initial Push (First Time)

### Option 1: Using the Batch Script (Windows)

1. Create the repository on GitHub first: https://github.com/new
   - Name: `awesome-system-design-projects`
   - Don't initialize with README

2. Double-click `push-to-github.bat` in the `awesome-system-design-projects` folder

### Option 2: Manual Commands

```bash
cd awesome-system-design-projects

# Initialize git
git init

# Add all files (includes all 15 projects)
git add .

# Commit everything
git commit -m "Initial commit: Add 15 system design projects with documentation"

# Set branch to main
git branch -M main

# Add remote
git remote add origin https://github.com/Codes-tutorials/awesome-system-design-projects.git

# Push everything
git push -u origin main
```

## Adding a New Project Later

### Step 1: Add the project folder

```bash
# Copy your new project into the directory
cp -r /path/to/new-project awesome-system-design-projects/

# Or on Windows
xcopy /E /I C:\path\to\new-project awesome-system-design-projects\new-project
```

### Step 2: Update the README

Edit `awesome-system-design-projects/README.md` and add your new project to the appropriate category.

### Step 3: Commit and push

```bash
cd awesome-system-design-projects

# Check what's new
git status

# Add the new project
git add new-project/

# Also update README if you modified it
git add README.md

# Commit
git commit -m "Add new-project: Brief description"

# Push
git push origin main
```

## Adding Multiple Projects at Once

```bash
cd awesome-system-design-projects

# Add all new changes
git add .

# Commit with descriptive message
git commit -m "Add multiple new projects: project1, project2, project3"

# Push
git push origin main
```

## Updating an Existing Project

```bash
cd awesome-system-design-projects

# Make your changes to the project files
# Then:

git add project-name/

git commit -m "Update project-name: description of changes"

git push origin main
```

## Best Practices

### 1. Commit Messages
Use clear, descriptive commit messages:
- ✅ `Add distributed-cache-demo: Redis-based caching solution`
- ✅ `Update hotel-management-system: Add payment gateway integration`
- ✅ `Fix bug in rate-limiter: Correct token bucket algorithm`
- ❌ `update`
- ❌ `changes`

### 2. Before Pushing
Always check what you're committing:
```bash
git status
git diff
```

### 3. Pull Before Push
If working with others:
```bash
git pull origin main
git push origin main
```

### 4. Branch for Major Changes
For significant updates:
```bash
git checkout -b feature/new-project
# Make changes
git add .
git commit -m "Add new project"
git push origin feature/new-project
# Then create a Pull Request on GitHub
```

## Verifying Your Push

After pushing, verify at:
```
https://github.com/Codes-tutorials/awesome-system-design-projects
```

You should see:
- All 15 project folders
- README.md with project descriptions
- .gitignore file
- All project files and subdirectories

## Troubleshooting

### Error: Remote already exists
```bash
git remote remove origin
git remote add origin https://github.com/Codes-tutorials/awesome-system-design-projects.git
```

### Error: Push rejected
```bash
# Pull first, then push
git pull origin main --allow-unrelated-histories
git push origin main
```

### Error: Authentication failed
Make sure you're logged in to GitHub. You may need to:
- Use a Personal Access Token instead of password
- Set up SSH keys
- Use GitHub CLI: `gh auth login`

## Quick Reference

```bash
# Check status
git status

# See what changed
git diff

# View commit history
git log --oneline

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Discard local changes
git checkout -- filename

# Update from remote
git pull origin main
```
