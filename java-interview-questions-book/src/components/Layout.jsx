import React, { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { 
  BookOpen, 
  Search, 
  Download, 
  Menu, 
  X, 
  Coffee,
  Code,
  Database,
  Layers,
  Zap,
  Shield,
  Globe,
  Cpu
} from 'lucide-react'
import { chapters } from '../data/chapters'

const Layout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()

  const navigation = [
    { name: 'Home', href: '/', icon: BookOpen },
    { name: 'Search', href: '/search', icon: Search },
    { name: 'Export to Word', href: '/export', icon: Download },
  ]

  const chapterIcons = {
    'java-fundamentals': Coffee,
    'oop-concepts': Code,
    'collections-framework': Database,
    'multithreading': Layers,
    'jvm-internals': Cpu,
    'performance-optimization': Zap,
    'security': Shield,
    'web-technologies': Globe,
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile sidebar */}
      <div className={`fixed inset-0 z-50 lg:hidden ${sidebarOpen ? 'block' : 'hidden'}`}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="fixed inset-y-0 left-0 flex w-full max-w-xs flex-col bg-white">
          <div className="flex h-16 items-center justify-between px-4">
            <h1 className="text-xl font-bold text-gray-900">Java Interview Book</h1>
            <button
              onClick={() => setSidebarOpen(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="h-6 w-6" />
            </button>
          </div>
          <nav className="flex-1 space-y-1 px-2 py-4">
            {navigation.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md ${
                    location.pathname === item.href
                      ? 'bg-primary-100 text-primary-700'
                      : 'text-gray-700 hover:text-gray-900 hover:bg-gray-100'
                  }`}
                  onClick={() => setSidebarOpen(false)}
                >
                  <Icon className="mr-3 h-5 w-5" />
                  {item.name}
                </Link>
              )
            })}
            
            <div className="pt-4">
              <h3 className="px-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Chapters
              </h3>
              <div className="mt-2 space-y-1">
                {chapters.map((chapter) => {
                  const Icon = chapterIcons[chapter.id] || Code
                  return (
                    <Link
                      key={chapter.id}
                      to={`/chapter/${chapter.id}`}
                      className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md ${
                        location.pathname === `/chapter/${chapter.id}`
                          ? 'bg-primary-100 text-primary-700'
                          : 'text-gray-700 hover:text-gray-900 hover:bg-gray-100'
                      }`}
                      onClick={() => setSidebarOpen(false)}
                    >
                      <Icon className="mr-3 h-4 w-4" />
                      {chapter.title}
                    </Link>
                  )
                })}
              </div>
            </div>
          </nav>
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
        <div className="flex flex-col flex-grow bg-white border-r border-gray-200">
          <div className="flex items-center h-16 px-4 bg-primary-600">
            <Coffee className="h-8 w-8 text-white mr-3" />
            <h1 className="text-xl font-bold text-white">Java Interview Book</h1>
          </div>
          
          <nav className="flex-1 px-2 py-4 space-y-1">
            {navigation.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md ${
                    location.pathname === item.href
                      ? 'bg-primary-100 text-primary-700'
                      : 'text-gray-700 hover:text-gray-900 hover:bg-gray-100'
                  }`}
                >
                  <Icon className="mr-3 h-5 w-5" />
                  {item.name}
                </Link>
              )
            })}
            
            <div className="pt-4">
              <h3 className="px-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Chapters
              </h3>
              <div className="mt-2 space-y-1">
                {chapters.map((chapter) => {
                  const Icon = chapterIcons[chapter.id] || Code
                  return (
                    <Link
                      key={chapter.id}
                      to={`/chapter/${chapter.id}`}
                      className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md ${
                        location.pathname === `/chapter/${chapter.id}`
                          ? 'bg-primary-100 text-primary-700'
                          : 'text-gray-700 hover:text-gray-900 hover:bg-gray-100'
                      }`}
                    >
                      <Icon className="mr-3 h-4 w-4" />
                      <span className="truncate">{chapter.title}</span>
                      <span className="ml-auto text-xs text-gray-500">
                        {chapter.questions.length}
                      </span>
                    </Link>
                  )
                })}
              </div>
            </div>
          </nav>
          
          <div className="p-4 border-t border-gray-200">
            <div className="text-xs text-gray-500">
              <p className="font-medium">Senior Developer Edition</p>
              <p>4+ Years Experience</p>
              <p className="mt-1">400+ Pages • 200+ Questions</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="lg:pl-64">
        {/* Mobile header */}
        <div className="sticky top-0 z-40 flex h-16 shrink-0 items-center gap-x-4 border-b border-gray-200 bg-white px-4 shadow-sm lg:hidden">
          <button
            type="button"
            className="text-gray-700"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu className="h-6 w-6" />
          </button>
          <div className="flex-1 text-sm font-semibold leading-6 text-gray-900">
            Java Interview Questions
          </div>
        </div>

        {/* Page content */}
        <main className="py-8">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  )
}

export default Layout