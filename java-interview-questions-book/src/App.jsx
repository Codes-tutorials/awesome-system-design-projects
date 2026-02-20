import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import ChapterView from './pages/ChapterView'
import QuestionView from './pages/QuestionView'
import ExportPage from './pages/ExportPage'
import SearchPage from './pages/SearchPage'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/chapter/:chapterId" element={<ChapterView />} />
        <Route path="/question/:questionId" element={<QuestionView />} />
        <Route path="/export" element={<ExportPage />} />
        <Route path="/search" element={<SearchPage />} />
      </Routes>
    </Layout>
  )
}

export default App