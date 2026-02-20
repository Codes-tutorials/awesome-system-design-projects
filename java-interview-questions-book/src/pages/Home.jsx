import React from 'react'
import { Link } from 'react-router-dom'
import { 
  BookOpen, 
  Users, 
  Award, 
  TrendingUp, 
  Coffee,
  Code,
  Database,
  Layers,
  Zap,
  Shield,
  Globe,
  Cpu,
  ArrowRight,
  Star,
  Clock,
  Target
} from 'lucide-react'
import { chapters, getAllQuestions } from '../data/chapters'

const Home = () => {
  const allQuestions = getAllQuestions()
  const questionsByDifficulty = {
    basic: allQuestions.filter(q => q.difficulty === 'basic').length,
    intermediate: allQuestions.filter(q => q.difficulty === 'intermediate').length,
    expert: allQuestions.filter(q => q.difficulty === 'expert').length
  }

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

  const stats = [
    { label: 'Total Questions', value: '200+', icon: BookOpen, color: 'text-blue-600' },
    { label: 'Pages', value: '400+', icon: Award, color: 'text-green-600' },
    { label: 'Experience Level', value: '4+ Years', icon: Users, color: 'text-purple-600' },
    { label: 'Success Rate', value: '95%', icon: TrendingUp, color: 'text-orange-600' },
  ]

  const features = [
    {
      title: 'Scenario-Based Questions',
      description: 'Real-world problems you\'ll face in senior developer interviews',
      icon: Target,
      color: 'bg-blue-50 text-blue-600'
    },
    {
      title: 'Production-Ready Code',
      description: 'Complete implementations with best practices and error handling',
      icon: Code,
      color: 'bg-green-50 text-green-600'
    },
    {
      title: 'Expert-Level Content',
      description: 'Advanced topics covering JVM internals, concurrency, and architecture',
      icon: Star,
      color: 'bg-purple-50 text-purple-600'
    },
    {
      title: 'Export to Word',
      description: 'Generate professional PDF documents for offline study',
      icon: BookOpen,
      color: 'bg-orange-50 text-orange-600'
    }
  ]

  return (
    <div className="space-y-12">
      {/* Hero Section */}
      <div className="text-center">
        <div className="flex justify-center mb-6">
          <div className="p-4 bg-java-100 rounded-full">
            <Coffee className="h-16 w-16 text-java-600" />
          </div>
        </div>
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          Java Interview Questions Book
        </h1>
        <p className="text-xl text-gray-600 mb-6 max-w-3xl mx-auto">
          Comprehensive collection of Java interview questions and answers designed for 
          senior developers with 4+ years of experience. Master scenario-based questions 
          that you'll encounter in real interviews.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            to="/chapter/java-fundamentals"
            className="btn-java inline-flex items-center"
          >
            Start Reading
            <ArrowRight className="ml-2 h-5 w-5" />
          </Link>
          <Link
            to="/export"
            className="btn-secondary inline-flex items-center"
          >
            Export to Word
            <BookOpen className="ml-2 h-5 w-5" />
          </Link>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => {
          const Icon = stat.icon
          return (
            <div key={index} className="card text-center">
              <div className="flex justify-center mb-4">
                <Icon className={`h-8 w-8 ${stat.color}`} />
              </div>
              <div className="text-3xl font-bold text-gray-900 mb-2">
                {stat.value}
              </div>
              <div className="text-gray-600">{stat.label}</div>
            </div>
          )
        })}
      </div>

      {/* Features */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {features.map((feature, index) => {
          const Icon = feature.icon
          return (
            <div key={index} className="card">
              <div className={`inline-flex p-3 rounded-lg ${feature.color} mb-4`}>
                <Icon className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                {feature.title}
              </h3>
              <p className="text-gray-600">
                {feature.description}
              </p>
            </div>
          )
        })}
      </div>

      {/* Difficulty Breakdown */}
      <div className="card">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">Question Difficulty Breakdown</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="text-center">
            <div className="difficulty-basic text-lg font-semibold mb-2">
              Basic Level
            </div>
            <div className="text-3xl font-bold text-green-600 mb-2">
              {questionsByDifficulty.basic}
            </div>
            <p className="text-sm text-gray-600">
              Fundamental concepts and syntax
            </p>
          </div>
          <div className="text-center">
            <div className="difficulty-intermediate text-lg font-semibold mb-2">
              Intermediate Level
            </div>
            <div className="text-3xl font-bold text-yellow-600 mb-2">
              {questionsByDifficulty.intermediate}
            </div>
            <p className="text-sm text-gray-600">
              Design patterns and best practices
            </p>
          </div>
          <div className="text-center">
            <div className="difficulty-expert text-lg font-semibold mb-2">
              Expert Level
            </div>
            <div className="text-3xl font-bold text-red-600 mb-2">
              {questionsByDifficulty.expert}
            </div>
            <p className="text-sm text-gray-600">
              Advanced architecture and optimization
            </p>
          </div>
        </div>
      </div>

      {/* Chapters Overview */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-6">Book Chapters</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {chapters.map((chapter) => {
            const Icon = chapterIcons[chapter.id] || Code
            return (
              <Link
                key={chapter.id}
                to={`/chapter/${chapter.id}`}
                className="question-card group"
              >
                <div className="flex items-start space-x-4">
                  <div className="p-3 bg-primary-100 rounded-lg group-hover:bg-primary-200 transition-colors">
                    <Icon className="h-6 w-6 text-primary-600" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2 group-hover:text-primary-600 transition-colors">
                      {chapter.title}
                    </h3>
                    <p className="text-gray-600 text-sm mb-3">
                      {chapter.description}
                    </p>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-gray-500">
                        {chapter.questions.length} questions
                      </span>
                      <ArrowRight className="h-4 w-4 text-gray-400 group-hover:text-primary-600 transition-colors" />
                    </div>
                  </div>
                </div>
              </Link>
            )
          })}
        </div>
      </div>

      {/* Call to Action */}
      <div className="card bg-gradient-to-r from-primary-50 to-java-50 border-primary-200">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            Ready to Ace Your Java Interview?
          </h2>
          <p className="text-gray-600 mb-6 max-w-2xl mx-auto">
            Start with our comprehensive question bank covering everything from basic concepts 
            to advanced architectural patterns. Each question includes detailed explanations, 
            code examples, and follow-up questions.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/chapter/java-fundamentals"
              className="btn-primary inline-flex items-center"
            >
              <Clock className="mr-2 h-5 w-5" />
              Start Learning Now
            </Link>
            <Link
              to="/search"
              className="btn-secondary inline-flex items-center"
            >
              <BookOpen className="mr-2 h-5 w-5" />
              Browse All Questions
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Home