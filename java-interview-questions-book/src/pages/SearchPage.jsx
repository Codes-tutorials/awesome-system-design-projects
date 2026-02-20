import React, { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { Search, Filter, Tag, ArrowRight, Users } from 'lucide-react'
import { getAllQuestions } from '../data/chapters'

const SearchPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedDifficulty, setSelectedDifficulty] = useState('all')
  const [selectedCategory, setSelectedCategory] = useState('all')
  const [selectedChapter, setSelectedChapter] = useState('all')

  const allQuestions = getAllQuestions()

  // Get unique categories and chapters for filters
  const categories = [...new Set(allQuestions.map(q => q.category))]
  const chapters = [...new Set(allQuestions.map(q => ({ id: q.chapterId, title: q.chapterTitle })))]
    .reduce((acc, curr) => {
      if (!acc.find(item => item.id === curr.id)) {
        acc.push(curr)
      }
      return acc
    }, [])

  const filteredQuestions = useMemo(() => {
    return allQuestions.filter(question => {
      const matchesSearch = searchTerm === '' || 
        question.question.toLowerCase().includes(searchTerm.toLowerCase()) ||
        question.answer?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        question.tags?.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase())) ||
        question.scenario?.toLowerCase().includes(searchTerm.toLowerCase())

      const matchesDifficulty = selectedDifficulty === 'all' || question.difficulty === selectedDifficulty
      const matchesCategory = selectedCategory === 'all' || question.category === selectedCategory
      const matchesChapter = selectedChapter === 'all' || question.chapterId === selectedChapter

      return matchesSearch && matchesDifficulty && matchesCategory && matchesChapter
    })
  }, [searchTerm, selectedDifficulty, selectedCategory, selectedChapter, allQuestions])

  const getDifficultyColor = (difficulty) => {
    switch (difficulty) {
      case 'basic':
        return 'difficulty-basic'
      case 'intermediate':
        return 'difficulty-intermediate'
      case 'expert':
        return 'difficulty-expert'
      default:
        return 'difficulty-basic'
    }
  }

  const clearFilters = () => {
    setSearchTerm('')
    setSelectedDifficulty('all')
    setSelectedCategory('all')
    setSelectedChapter('all')
  }

  const activeFiltersCount = [
    selectedDifficulty !== 'all',
    selectedCategory !== 'all',
    selectedChapter !== 'all'
  ].filter(Boolean).length

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="card">
        <div className="flex items-center mb-4">
          <Search className="h-8 w-8 text-primary-600 mr-3" />
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Search Questions</h1>
            <p className="text-gray-600">
              Find specific questions by topic, difficulty, or keyword
            </p>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
          <input
            type="text"
            placeholder="Search questions, answers, tags, or scenarios..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
          />
        </div>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center">
            <Filter className="h-5 w-5 text-gray-600 mr-2" />
            <h2 className="text-lg font-semibold text-gray-900">Filters</h2>
            {activeFiltersCount > 0 && (
              <span className="ml-2 bg-primary-100 text-primary-700 text-xs px-2 py-1 rounded-full">
                {activeFiltersCount} active
              </span>
            )}
          </div>
          {activeFiltersCount > 0 && (
            <button
              onClick={clearFilters}
              className="text-sm text-primary-600 hover:text-primary-700"
            >
              Clear all
            </button>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Difficulty Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Difficulty
            </label>
            <select
              value={selectedDifficulty}
              onChange={(e) => setSelectedDifficulty(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="all">All Difficulties</option>
              <option value="basic">Basic</option>
              <option value="intermediate">Intermediate</option>
              <option value="expert">Expert</option>
            </select>
          </div>

          {/* Category Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Category
            </label>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="all">All Categories</option>
              {categories.map(category => (
                <option key={category} value={category}>{category}</option>
              ))}
            </select>
          </div>

          {/* Chapter Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Chapter
            </label>
            <select
              value={selectedChapter}
              onChange={(e) => setSelectedChapter(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="all">All Chapters</option>
              {chapters.map(chapter => (
                <option key={chapter.id} value={chapter.id}>{chapter.title}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Results */}
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-lg font-semibold text-gray-900">
            Search Results
          </h2>
          <span className="text-sm text-gray-600">
            {filteredQuestions.length} question{filteredQuestions.length !== 1 ? 's' : ''} found
          </span>
        </div>

        {filteredQuestions.length === 0 ? (
          <div className="text-center py-12">
            <Search className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              No questions found
            </h3>
            <p className="text-gray-600 mb-4">
              Try adjusting your search terms or filters
            </p>
            <button
              onClick={clearFilters}
              className="btn-primary"
            >
              Clear Filters
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredQuestions.map((question, index) => (
              <Link
                key={question.id}
                to={`/question/${question.id}`}
                className="question-card group block"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center space-x-3">
                    <span className="text-sm font-medium text-gray-500">
                      #{index + 1}
                    </span>
                    <span className={getDifficultyColor(question.difficulty)}>
                      {question.difficulty}
                    </span>
                    <span className="text-sm text-gray-500 bg-gray-100 px-2 py-1 rounded">
                      {question.category}
                    </span>
                    <span className="text-xs text-gray-500">
                      {question.chapterTitle}
                    </span>
                  </div>
                  <ArrowRight className="h-5 w-5 text-gray-400 group-hover:text-primary-600 transition-colors" />
                </div>

                <h3 className="text-lg font-semibold text-gray-900 mb-3 group-hover:text-primary-600 transition-colors">
                  {question.question}
                </h3>

                {question.scenario && (
                  <div className="mb-3">
                    <div className="flex items-center text-sm text-gray-600 mb-1">
                      <Users className="h-4 w-4 mr-1" />
                      Scenario
                    </div>
                    <p className="text-sm text-gray-700 bg-blue-50 p-3 rounded-lg line-clamp-2">
                      {question.scenario}
                    </p>
                  </div>
                )}

                {question.tags && question.tags.length > 0 && (
                  <div className="flex items-center flex-wrap gap-2">
                    <Tag className="h-4 w-4 text-gray-400" />
                    {question.tags.slice(0, 5).map((tag) => (
                      <span
                        key={tag}
                        className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded"
                      >
                        {tag}
                      </span>
                    ))}
                    {question.tags.length > 5 && (
                      <span className="text-xs text-gray-500">
                        +{question.tags.length - 5} more
                      </span>
                    )}
                  </div>
                )}
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* Quick Stats */}
      {filteredQuestions.length > 0 && (
        <div className="card bg-gray-50">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Results Summary
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">
                {filteredQuestions.filter(q => q.difficulty === 'basic').length}
              </div>
              <div className="text-sm text-gray-600">Basic Questions</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-yellow-600">
                {filteredQuestions.filter(q => q.difficulty === 'intermediate').length}
              </div>
              <div className="text-sm text-gray-600">Intermediate Questions</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-red-600">
                {filteredQuestions.filter(q => q.difficulty === 'expert').length}
              </div>
              <div className="text-sm text-gray-600">Expert Questions</div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default SearchPage