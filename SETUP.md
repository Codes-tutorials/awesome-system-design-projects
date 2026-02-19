# Setup Instructions for GitHub

## Pushing to GitHub Repository

Follow these steps to add this project to your GitHub repository:

### Step 1: Initialize Git Repository (if not already done)

```bash
cd awesome-system-design-projects
git init
```

### Step 2: Add All Files

```bash
git add .
```

### Step 3: Create Initial Commit

```bash
git commit -m "Initial commit: Add awesome system design projects collection"
```

### Step 4: Add Remote Repository

```bash
git remote add origin https://github.com/Codes-tutorials/awesome-system-design-projects.git
```

### Step 5: Push to GitHub

```bash
# For first time push
git push -u origin main

# Or if your default branch is master
git push -u origin master
```

## Alternative: Add to Existing Repository

If you want to add this as a subdirectory to an existing repository:

### Option 1: Direct Copy

```bash
# From parent directory
cd ..
git clone https://github.com/Codes-tutorials/your-repo.git
cp -r awesome-system-design-projects your-repo/
cd your-repo
git add awesome-system-design-projects
git commit -m "Add awesome system design projects"
git push origin main
```

### Option 2: Subtree Merge

```bash
# From your main repository
git subtree add --prefix=awesome-system-design-projects https://github.com/Codes-tutorials/awesome-system-design-projects.git main --squash
```

## Creating the Repository on GitHub

1. Go to https://github.com/Codes-tutorials
2. Click "New repository"
3. Name it: `awesome-system-design-projects`
4. Description: "A curated collection of production-ready system design implementations"
5. Choose Public or Private
6. Do NOT initialize with README (we already have one)
7. Click "Create repository"
8. Follow the push instructions above

## Repository Settings Recommendations

### Topics to Add
- system-design
- distributed-systems
- java
- spring-boot
- microservices
- scalability
- architecture
- design-patterns
- redis
- kafka

### Branch Protection (Optional)
- Require pull request reviews
- Require status checks to pass
- Require branches to be up to date

## Next Steps

1. Add repository description and topics on GitHub
2. Enable GitHub Pages (optional) for documentation
3. Add CONTRIBUTING.md if you want community contributions
4. Set up GitHub Actions for CI/CD (optional)
5. Add badges to README (build status, license, etc.)

## Useful Git Commands

```bash
# Check status
git status

# View commit history
git log --oneline

# Create a new branch
git checkout -b feature/new-project

# Push new branch
git push -u origin feature/new-project

# Pull latest changes
git pull origin main
```

## Troubleshooting

### If remote already exists:
```bash
git remote remove origin
git remote add origin https://github.com/Codes-tutorials/awesome-system-design-projects.git
```

### If branch name mismatch:
```bash
# Rename local branch to main
git branch -M main
```

### If push is rejected:
```bash
# Force push (use with caution)
git push -f origin main

# Or pull and merge first
git pull origin main --allow-unrelated-histories
git push origin main
```
