import React, { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, ArrowRight, Tag, Users, HelpCircle, CheckCircle, Copy, ExternalLink } from 'lucide-react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import { getQuestionById, getAllQuestions } from '../data/chapters'

const QuestionView = () => {
  const { questionId } = useParams()
  const [showAnswer, setShowAnswer] = useState(false)
  const [copiedCode, setCopiedCode] = useState(false)
  
  const question = getQuestionById(questionId)
  const allQuestions = getAllQuestions()
  
  if (!question) {
    return (
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Question Not Found</h1>
        <Link to="/" className="btn-primary">
          Back to Home
        </Link>
      </div>
    )
  }

  const currentIndex = allQuestions.findIndex(q => q.id === questionId)
  const prevQuestion = currentIndex > 0 ? allQuestions[currentIndex - 1] : null
  const nextQuestion = currentIndex < allQuestions.length - 1 ? allQuestions[currentIndex + 1] : null

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

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text).then(() => {
      setCopiedCode(true)
      setTimeout(() => setCopiedCode(false), 2000)
    })
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <Link
          to={`/chapter/${question.chapterId}`}
          className="inline-flex items-center text-primary-600 hover:text-primary-700 mb-4"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to {question.chapterTitle}
        </Link>
        
        <div className="card">
          <div className="flex items-start justify-between mb-4">
            <div className="flex items-center space-x-3">
              <span className={getDifficultyColor(question.difficulty)}>
                {question.difficulty}
              </span>
              <span className="text-sm text-gray-500 bg-gray-100 px-2 py-1 rounded">
                {question.category}
              </span>
            </div>
            <div className="text-sm text-gray-500">
              Question {currentIndex + 1} of {allQuestions.length}
            </div>
          </div>
          
          <h1 className="text-2xl font-bold text-gray-900 mb-6">
            {question.question}
          </h1>
          
          {question.scenario && (
            <div className="mb-6">
              <div className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <Users className="h-4 w-4 mr-2" />
                Real-World Scenario
              </div>
              <div className="bg-blue-50 border-l-4 border-blue-400 p-4 rounded-r-lg">
                <p className="text-gray-700">{question.scenario}</p>
              </div>
            </div>
          )}
          
          {question.tags && question.tags.length > 0 && (
            <div className="flex items-center flex-wrap gap-2 mb-6">
              <Tag className="h-4 w-4 text-gray-400" />
              {question.tags.map((tag) => (
                <span
                  key={tag}
                  className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded"
                >
                  {tag}
                </span>
              ))}
            </div>
          )}
          
          <div className="flex items-center justify-between">
            <button
              onClick={() => setShowAnswer(!showAnswer)}
              className={`inline-flex items-center px-4 py-2 rounded-lg font-medium transition-colors ${
                showAnswer
                  ? 'bg-green-100 text-green-700 hover:bg-green-200'
                  : 'bg-primary-600 text-white hover:bg-primary-700'
              }`}
            >
              {showAnswer ? (
                <>
                  <CheckCircle className="mr-2 h-4 w-4" />
                  Answer Shown
                </>
              ) : (
                <>
                  <HelpCircle className="mr-2 h-4 w-4" />
                  Show Answer
                </>
              )}
            </button>
            
            <button
              onClick={() => copyToClipboard(question.question)}
              className="inline-flex items-center text-gray-500 hover:text-gray-700"
            >
              <Copy className="h-4 w-4 mr-1" />
              {copiedCode ? 'Copied!' : 'Copy Question'}
            </button>
          </div>
        </div>
      </div>

      {/* Answer Section */}
      {showAnswer && (
        <div className="card">
          <div className="flex items-center mb-6">
            <CheckCircle className="h-6 w-6 text-green-600 mr-3" />
            <h2 className="text-xl font-bold text-gray-900">Detailed Answer</h2>
          </div>
          
          <div className="prose prose-lg max-w-none">
            <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              rehypePlugins={[rehypeHighlight]}
              components={{
                code: ({ node, inline, className, children, ...props }) => {
                  const match = /language-(\w+)/.exec(className || '')
                  const language = match ? match[1] : ''
                  
                  if (!inline && language) {
                    return (
                      <div className="relative">
                        <div className="flex items-center justify-between bg-gray-800 text-gray-200 px-4 py-2 text-sm rounded-t-lg">
                          <span className="font-medium">{language}</span>
                          <button
                            onClick={() => copyToClipboard(String(children).replace(/\n$/, ''))}
                            className="flex items-center text-gray-400 hover:text-gray-200"
                          >
                            <Copy className="h-4 w-4 mr-1" />
                            Copy
                          </button>
                        </div>
                        <pre className="!mt-0 !rounded-t-none">
                          <code className={className} {...props}>
                            {children}
                          </code>
                        </pre>
                      </div>
                    )
                  }
                  
                  return (
                    <code className={className} {...props}>
                      {children}
                    </code>
                  )
                }
              }}
            >
              {question.answer}
            </ReactMarkdown>
          </div>
          
          {question.followUp && question.followUp.length > 0 && (
            <div className="mt-8 p-6 bg-yellow-50 border border-yellow-200 rounded-lg">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Follow-up Questions
              </h3>
              <ul className="space-y-2">
                {question.followUp.map((followUpQ, index) => (
                  <li key={index} className="flex items-start">
                    <HelpCircle className="h-4 w-4 text-yellow-600 mr-2 mt-0.5 flex-shrink-0" />
                    <span className="text-gray-700">{followUpQ}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* Navigation */}
      <div className="flex justify-between items-center">
        <div>
          {prevQuestion ? (
            <Link
              to={`/question/${prevQuestion.id}`}
              className="inline-flex items-center text-primary-600 hover:text-primary-700"
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Previous Question
            </Link>
          ) : (
            <div></div>
          )}
        </div>
        
        <div className="text-center">
          <div className="text-sm text-gray-500 mb-2">
            Progress: {currentIndex + 1} / {allQuestions.length}
          </div>
          <div className="w-64 bg-gray-200 rounded-full h-2">
            <div
              className="bg-primary-600 h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentIndex + 1) / allQuestions.length) * 100}%` }}
            ></div>
          </div>
        </div>
        
        <div>
          {nextQuestion ? (
            <Link
              to={`/question/${nextQuestion.id}`}
              className="inline-flex items-center text-primary-600 hover:text-primary-700"
            >
              Next Question
              <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          ) : (
            <Link
              to="/export"
              className="inline-flex items-center btn-primary"
            >
              Export Book
              <ExternalLink className="ml-2 h-4 w-4" />
            </Link>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card bg-gray-50">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
        <div className="flex flex-wrap gap-3">
          <Link
            to="/search"
            className="btn-secondary text-sm"
          >
            Search Questions
          </Link>
          <Link
            to={`/chapter/${question.chapterId}`}
            className="btn-secondary text-sm"
          >
            View Chapter
          </Link>
          <Link
            to="/export"
            className="btn-secondary text-sm"
          >
            Export to Word
          </Link>
          <button
            onClick={() => copyToClipboard(window.location.href)}
            className="btn-secondary text-sm"
          >
            Share Question
          </button>
        </div>
      </div>
    </div>
  )
}

export default QuestionView