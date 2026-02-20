#!/usr/bin/env node

const { execSync } = require('child_process')
const fs = require('fs')

console.log('🔧 Fixing Java Interview Questions Book dependencies...\n')

// Install missing Tailwind typography plugin
console.log('📦 Installing @tailwindcss/typography...')
try {
  execSync('npm install --save-dev @tailwindcss/typography@^0.5.10', { stdio: 'inherit' })
  console.log('✅ @tailwindcss/typography installed successfully')
} catch (error) {
  console.error('❌ Failed to install @tailwindcss/typography')
  console.error('Please run manually: npm install --save-dev @tailwindcss/typography')
}

// Install any other missing dependencies
console.log('\n📦 Installing all dependencies...')
try {
  execSync('npm install', { stdio: 'inherit' })
  console.log('✅ All dependencies installed successfully')
} catch (error) {
  console.error('❌ Failed to install dependencies')
  console.error('Please run manually: npm install')
}

console.log('\n🎉 Dependencies fixed! You can now run:')
console.log('   npm run dev')
console.log('\n   to start the development server.')