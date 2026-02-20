import React from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, ArrowRight, Clock, Tag, Users } from 'lucide-react'
import { getChapterById } from '../data/chapters'

const ChapterView = () => {
  const { chapterId } = useParams()
  const chapter = getChapterById(chapterId)

  if (!chapter) {
    return (
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Chapter Not Found</h1>
        <Link to="/" className="btn-primary">
          Back to Home
        </Link>
      </div>
    )
  }

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

  const difficultyStats = {
    basic: chapter.questions.filter(q => q.difficulty === 'basic').length,
    intermediate: chapter.questions.filter(q => q.difficulty === 'intermediate').length,
    expert: chapter.questions.filter(q => q.difficulty === 'expert').length
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <Link
          to="/"
          className="inline-flex items-center text-primary-600 hover:text-primary-700 mb-4"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Home
        </Link>
        
        <div className="card">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            {chapter.title}
          </h1>
          <p className="text-lg text-gray-600 mb-6">
            {chapter.description}
          </p>
          
          {/* Chapter Stats */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <div className="text-2xl font-bold text-gray-900">
                {chapter.questions.length}
              </div>
              <div className="text-sm text-gray-600">Total Questions</div>
            </div>
            <div className="text-center p-4 bg-green-50 rounded-lg">
              <div className="text-2xl font-bold text-green-600">
                {difficultyStats.basic}
              </div>
              <div className="text-sm text-gray-600">Basic</div>
            </div>
            <div className="text-center p-4 bg-yellow-50 rounded-lg">
              <div className="text-2xl font-bold text-yellow-600">
                {difficultyStats.intermediate}
              </div>
              <div className="text-sm text-gray-600">Intermediate</div>
            </div>
            <div className="text-center p-4 bg-red-50 rounded-lg">
              <div className="text-2xl font-bold text-red-600">
                {difficultyStats.expert}
              </div>
              <div className="text-sm text-gray-600">Expert</div>
            </div>
          </div>
        </div>
      </div>

      {/* Questions List */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-6">Questions</h2>
        
        {chapter.questions.length === 0 ? (
          <div className="card text-center py-12">
            <Clock className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Coming Soon
            </h3>
            <p className="text-gray-600">
              Questions for this chapter are being prepared. Check back soon!
            </p>
          </div>
        ) : (
          <div className="space-y-6">
            {chapter.questions.map((question, index) => (
              <Link
                key={question.id}
                to={`/question/${question.id}`}
                className="question-card group block"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center space-x-3">
                    <span className="text-sm font-medium text-gray-500">
                      Q{index + 1}
                    </span>
                    <span className={getDifficultyColor(question.difficulty)}>
                      {question.difficulty}
                    </span>
                    <span className="text-sm text-gray-500 bg-gray-100 px-2 py-1 rounded">
                      {question.category}
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
                    <p className="text-sm text-gray-700 bg-blue-50 p-3 rounded-lg">
                      {question.scenario}
                    </p>
                  </div>
                )}
                
                {question.tags && question.tags.length > 0 && (
                  <div className="flex items-center flex-wrap gap-2">
                    <Tag className="h-4 w-4 text-gray-400" />
                    {question.tags.slice(0, 4).map((tag) => (
                      <span
                        key={tag}
                        className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded"
                      >
                        {tag}
                      </span>
                    ))}
                    {question.tags.length > 4 && (
                      <span className="text-xs text-gray-500">
                        +{question.tags.length - 4} more
                      </span>
                    )}
                  </div>
                )}
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* Navigation */}
      {chapter.questions.length > 0 && (
        <div className="card">
          <div className="flex justify-between items-center">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                Ready to start?
              </h3>
              <p className="text-gray-600">
                Begin with the first question in this chapter.
              </p>
            </div>
            <Link
              to={`/question/${chapter.questions[0].id}`}
              className="btn-primary inline-flex items-center"
            >
              Start Chapter
              <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}

export default ChapterView