import React, { useState } from 'react'
import { Download, FileText, Settings, CheckCircle, AlertCircle, Loader } from 'lucide-react'
import { chapters, getAllQuestions } from '../data/chapters'
import { exportToWord } from '../utils/wordExporter'

const ExportPage = () => {
  const [selectedChapters, setSelectedChapters] = useState(chapters.map(c => c.id))
  const [includeDifficulty, setIncludeDifficulty] = useState(['basic', 'intermediate', 'expert'])
  const [includeAnswers, setIncludeAnswers] = useState(true)
  const [includeCode, setIncludeCode] = useState(true)
  const [includeFollowUp, setIncludeFollowUp] = useState(true)
  const [isExporting, setIsExporting] = useState(false)
  const [exportStatus, setExportStatus] = useState(null)

  const allQuestions = getAllQuestions()
  const filteredQuestions = allQuestions.filter(q => 
    selectedChapters.includes(q.chapterId) && 
    includeDifficulty.includes(q.difficulty)
  )

  const handleChapterToggle = (chapterId) => {
    setSelectedChapters(prev => 
      prev.includes(chapterId)
        ? prev.filter(id => id !== chapterId)
        : [...prev, chapterId]
    )
  }

  const handleDifficultyToggle = (difficulty) => {
    setIncludeDifficulty(prev =>
      prev.includes(difficulty)
        ? prev.filter(d => d !== difficulty)
        : [...prev, difficulty]
    )
  }

  const handleExport = async () => {
    setIsExporting(true)
    setExportStatus(null)

    try {
      const exportOptions = {
        chapters: chapters.filter(c => selectedChapters.includes(c.id)),
        questions: filteredQuestions,
        includeAnswers,
        includeCode,
        includeFollowUp
      }

      await exportToWord(exportOptions)
      
      setExportStatus({
        type: 'success',
        message: `Successfully exported ${filteredQuestions.length} questions to Word document!`
      })
    } catch (error) {
      console.error('Export failed:', error)
      setExportStatus({
        type: 'error',
        message: 'Export failed. Please try again.'
      })
    } finally {
      setIsExporting(false)
    }
  }

  const getDifficultyStats = () => {
    const stats = {
      basic: filteredQuestions.filter(q => q.difficulty === 'basic').length,
      intermediate: filteredQuestions.filter(q => q.difficulty === 'intermediate').length,
      expert: filteredQuestions.filter(q => q.difficulty === 'expert').length
    }
    return stats
  }

  const stats = getDifficultyStats()

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="card">
        <div className="flex items-center mb-4">
          <FileText className="h-8 w-8 text-primary-600 mr-3" />
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Export to Word</h1>
            <p className="text-gray-600">
              Generate a professional Word document with your selected questions and answers
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Export Options */}
        <div className="lg:col-span-2 space-y-6">
          {/* Chapter Selection */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
              <Settings className="h-5 w-5 mr-2" />
              Select Chapters
            </h2>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-gray-700">Select All</span>
                <input
                  type="checkbox"
                  checked={selectedChapters.length === chapters.length}
                  onChange={(e) => {
                    if (e.target.checked) {
                      setSelectedChapters(chapters.map(c => c.id))
                    } else {
                      setSelectedChapters([])
                    }
                  }}
                  className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
              </div>
              <hr className="border-gray-200" />
              {chapters.map((chapter) => (
                <div key={chapter.id} className="flex items-center justify-between">
                  <div>
                    <label className="text-sm font-medium text-gray-700">
                      {chapter.title}
                    </label>
                    <p className="text-xs text-gray-500">
                      {chapter.questions.length} questions
                    </p>
                  </div>
                  <input
                    type="checkbox"
                    checked={selectedChapters.includes(chapter.id)}
                    onChange={() => handleChapterToggle(chapter.id)}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                  />
                </div>
              ))}
            </div>
          </div>

          {/* Difficulty Selection */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Difficulty Levels
            </h2>
            <div className="space-y-3">
              {[
                { id: 'basic', label: 'Basic', color: 'text-green-600' },
                { id: 'intermediate', label: 'Intermediate', color: 'text-yellow-600' },
                { id: 'expert', label: 'Expert', color: 'text-red-600' }
              ].map((difficulty) => (
                <div key={difficulty.id} className="flex items-center justify-between">
                  <div className="flex items-center">
                    <span className={`text-sm font-medium ${difficulty.color}`}>
                      {difficulty.label}
                    </span>
                    <span className="ml-2 text-xs text-gray-500">
                      ({stats[difficulty.id]} questions)
                    </span>
                  </div>
                  <input
                    type="checkbox"
                    checked={includeDifficulty.includes(difficulty.id)}
                    onChange={() => handleDifficultyToggle(difficulty.id)}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                  />
                </div>
              ))}
            </div>
          </div>

          {/* Content Options */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Content Options
            </h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <label className="text-sm font-medium text-gray-700">
                    Include Answers
                  </label>
                  <p className="text-xs text-gray-500">
                    Include detailed answers and explanations
                  </p>
                </div>
                <input
                  type="checkbox"
                  checked={includeAnswers}
                  onChange={(e) => setIncludeAnswers(e.target.checked)}
                  className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <label className="text-sm font-medium text-gray-700">
                    Include Code Examples
                  </label>
                  <p className="text-xs text-gray-500">
                    Include code blocks and examples
                  </p>
                </div>
                <input
                  type="checkbox"
                  checked={includeCode}
                  onChange={(e) => setIncludeCode(e.target.checked)}
                  className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <label className="text-sm font-medium text-gray-700">
                    Include Follow-up Questions
                  </label>
                  <p className="text-xs text-gray-500">
                    Include additional practice questions
                  </p>
                </div>
                <input
                  type="checkbox"
                  checked={includeFollowUp}
                  onChange={(e) => setIncludeFollowUp(e.target.checked)}
                  className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Export Summary */}
        <div className="space-y-6">
          {/* Summary Card */}
          <div className="card">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Export Summary
            </h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Total Questions:</span>
                <span className="text-sm font-medium text-gray-900">
                  {filteredQuestions.length}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Selected Chapters:</span>
                <span className="text-sm font-medium text-gray-900">
                  {selectedChapters.length}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Estimated Pages:</span>
                <span className="text-sm font-medium text-gray-900">
                  {Math.ceil(filteredQuestions.length * 2.5)}
                </span>
              </div>
            </div>

            <hr className="my-4 border-gray-200" />

            <div className="space-y-2">
              <h3 className="text-sm font-medium text-gray-700">By Difficulty:</h3>
              <div className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="text-green-600">Basic:</span>
                  <span>{stats.basic}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-yellow-600">Intermediate:</span>
                  <span>{stats.intermediate}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-red-600">Expert:</span>
                  <span>{stats.expert}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Export Button */}
          <div className="card">
            <button
              onClick={handleExport}
              disabled={isExporting || filteredQuestions.length === 0}
              className={`w-full flex items-center justify-center px-4 py-3 rounded-lg font-medium transition-colors ${
                isExporting || filteredQuestions.length === 0
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-primary-600 text-white hover:bg-primary-700'
              }`}
            >
              {isExporting ? (
                <>
                  <Loader className="animate-spin h-5 w-5 mr-2" />
                  Exporting...
                </>
              ) : (
                <>
                  <Download className="h-5 w-5 mr-2" />
                  Export to Word
                </>
              )}
            </button>

            {filteredQuestions.length === 0 && (
              <p className="text-sm text-red-600 mt-2 text-center">
                Please select at least one chapter and difficulty level
              </p>
            )}
          </div>

          {/* Status Message */}
          {exportStatus && (
            <div className={`card ${
              exportStatus.type === 'success' 
                ? 'bg-green-50 border-green-200' 
                : 'bg-red-50 border-red-200'
            }`}>
              <div className="flex items-center">
                {exportStatus.type === 'success' ? (
                  <CheckCircle className="h-5 w-5 text-green-600 mr-2" />
                ) : (
                  <AlertCircle className="h-5 w-5 text-red-600 mr-2" />
                )}
                <p className={`text-sm ${
                  exportStatus.type === 'success' ? 'text-green-700' : 'text-red-700'
                }`}>
                  {exportStatus.message}
                </p>
              </div>
            </div>
          )}

          {/* Export Info */}
          <div className="card bg-blue-50 border-blue-200">
            <h3 className="text-sm font-medium text-blue-900 mb-2">
              Export Information
            </h3>
            <ul className="text-xs text-blue-700 space-y-1">
              <li>• Document will be formatted for professional use</li>
              <li>• Code examples will be syntax highlighted</li>
              <li>• Questions are organized by chapter</li>
              <li>• Table of contents included</li>
              <li>• Compatible with Microsoft Word 2016+</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ExportPage