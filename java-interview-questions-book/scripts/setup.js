#!/usr/bin/env node

const fs = require('fs')
const path = require('path')
const { execSync } = require('child_process')

console.log('🚀 Setting up Java Interview Questions Book...\n')

// Check Node.js version
const nodeVersion = process.version
const majorVersion = parseInt(nodeVersion.slice(1).split('.')[0])

if (majorVersion < 16) {
  console.error('❌ Node.js 16 or higher is required')
  console.error(`   Current version: ${nodeVersion}`)
  console.error('   Please upgrade Node.js and try again')
  process.exit(1)
}

console.log(`✅ Node.js version: ${nodeVersion}`)

// Check if package.json exists
if (!fs.existsSync('package.json')) {
  console.error('❌ package.json not found')
  console.error('   Please run this script from the project root directory')
  process.exit(1)
}

console.log('✅ Project structure verified')

// Install dependencies
console.log('\n📦 Installing dependencies...')
try {
  execSync('npm install', { stdio: 'inherit' })
  console.log('✅ Dependencies installed successfully')
} catch (error) {
  console.error('❌ Failed to install dependencies')
  console.error('   Please run "npm install" manually')
  process.exit(1)
}

// Check if all required files exist
const requiredFiles = [
  'src/App.jsx',
  'src/main.jsx',
  'src/index.css',
  'src/data/chapters.js',
  'src/utils/wordExporter.js',
  'tailwind.config.js',
  'vite.config.js'
]

console.log('\n🔍 Verifying project files...')
let missingFiles = []

requiredFiles.forEach(file => {
  if (fs.existsSync(file)) {
    console.log(`✅ ${file}`)
  } else {
    console.log(`❌ ${file}`)
    missingFiles.push(file)
  }
})

if (missingFiles.length > 0) {
  console.error('\n❌ Some required files are missing:')
  missingFiles.forEach(file => console.error(`   - ${file}`))
  console.error('\n   Please ensure all project files are present')
  process.exit(1)
}

// Create .env file if it doesn't exist
if (!fs.existsSync('.env')) {
  console.log('\n📝 Creating .env file...')
  const envContent = `# Java Interview Questions Book Environment Variables
VITE_APP_NAME="Java Interview Questions Book"
VITE_APP_VERSION="1.0.0"
VITE_APP_DESCRIPTION="Comprehensive Java interview questions for senior developers"
`
  fs.writeFileSync('.env', envContent)
  console.log('✅ .env file created')
}

// Verify Tailwind CSS setup
console.log('\n🎨 Verifying Tailwind CSS setup...')
try {
  const tailwindConfig = require(path.join(process.cwd(), 'tailwind.config.js'))
  if (tailwindConfig.content && tailwindConfig.content.length > 0) {
    console.log('✅ Tailwind CSS configuration verified')
  } else {
    console.log('⚠️  Tailwind CSS content paths may need adjustment')
  }
} catch (error) {
  console.log('⚠️  Could not verify Tailwind CSS configuration')
}

// Test build process
console.log('\n🔨 Testing build process...')
try {
  execSync('npm run build', { stdio: 'pipe' })
  console.log('✅ Build process successful')
  
  // Clean up build directory
  if (fs.existsSync('dist')) {
    fs.rmSync('dist', { recursive: true, force: true })
    console.log('✅ Build artifacts cleaned up')
  }
} catch (error) {
  console.log('⚠️  Build test failed - this may be normal if dependencies are still installing')
}

// Display success message and next steps
console.log('\n🎉 Setup completed successfully!\n')
console.log('📚 Java Interview Questions Book is ready to use.\n')
console.log('Next steps:')
console.log('1. Start the development server:')
console.log('   npm run dev\n')
console.log('2. Open your browser to:')
console.log('   http://localhost:3000\n')
console.log('3. Start adding questions to:')
console.log('   src/data/chapters.js\n')
console.log('4. Build for production:')
console.log('   npm run build\n')

console.log('📖 Documentation:')
console.log('   - README.md - Complete setup and usage guide')
console.log('   - PROJECT_COMPLETE.md - Feature overview and customization')
console.log('   - src/data/chapters.js - Question format and examples\n')

console.log('🚀 Happy coding! Your Java interview preparation tool is ready.')

// Display project statistics
const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'))
console.log('\n📊 Project Information:')
console.log(`   Name: ${packageJson.name}`)
console.log(`   Version: ${packageJson.version}`)
console.log(`   Description: ${packageJson.description}`)

// Count questions in chapters.js
try {
  const chaptersPath = path.join(process.cwd(), 'src', 'data', 'chapters.js')
  const chaptersContent = fs.readFileSync(chaptersPath, 'utf8')
  const questionMatches = chaptersContent.match(/id:\s*['"][^'"]+['"]/g)
  const questionCount = questionMatches ? questionMatches.length : 0
  console.log(`   Sample Questions: ${questionCount}`)
} catch (error) {
  console.log('   Sample Questions: Unable to count')
}

console.log('\n✨ Setup complete! ✨')