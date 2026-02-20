import { Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType, Table, TableRow, TableCell, WidthType } from 'docx'
import { saveAs } from 'file-saver'

export const exportToWord = async (options) => {
  const { chapters, questions, includeAnswers, includeCode, includeFollowUp } = options

  // Create document sections
  const sections = []

  // Title page
  const titleSection = createTitlePage()
  sections.push(...titleSection)

  // Table of contents
  const tocSection = createTableOfContents(chapters)
  sections.push(...tocSection)

  // Chapters and questions
  for (const chapter of chapters) {
    const chapterQuestions = questions.filter(q => q.chapterId === chapter.id)
    if (chapterQuestions.length === 0) continue

    // Chapter header
    sections.push(
      new Paragraph({
        text: chapter.title,
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),
      new Paragraph({
        text: chapter.description,
        spacing: { after: 300 }
      })
    )

    // Chapter questions
    chapterQuestions.forEach((question, index) => {
      sections.push(...createQuestionSection(question, index + 1, {
        includeAnswers,
        includeCode,
        includeFollowUp
      }))
    })
  }

  // Create document
  const doc = new Document({
    sections: [{
      properties: {},
      children: sections
    }]
  })

  // Generate and save
  const blob = await Packer.toBlob(doc)
  const fileName = `Java_Interview_Questions_${new Date().toISOString().split('T')[0]}.docx`
  saveAs(blob, fileName)
}

const createTitlePage = () => {
  return [
    new Paragraph({
      text: "Java Interview Questions Book",
      heading: HeadingLevel.TITLE,
      alignment: AlignmentType.CENTER,
      spacing: { before: 2000, after: 400 }
    }),
    new Paragraph({
      text: "Senior Developer Edition",
      alignment: AlignmentType.CENTER,
      spacing: { after: 200 },
      children: [
        new TextRun({
          text: "Senior Developer Edition",
          size: 32,
          bold: true
        })
      ]
    }),
    new Paragraph({
      text: "4+ Years Experience",
      alignment: AlignmentType.CENTER,
      spacing: { after: 400 },
      children: [
        new TextRun({
          text: "4+ Years Experience",
          size: 24,
          italics: true
        })
      ]
    }),
    new Paragraph({
      text: "Comprehensive collection of scenario-based Java interview questions and answers",
      alignment: AlignmentType.CENTER,
      spacing: { after: 800 }
    }),
    new Paragraph({
      text: `Generated on ${new Date().toLocaleDateString()}`,
      alignment: AlignmentType.CENTER,
      spacing: { after: 400 }
    }),
    new Paragraph({
      text: "",
      pageBreakBefore: true
    })
  ]
}

const createTableOfContents = (chapters) => {
  const tocItems = [
    new Paragraph({
      text: "Table of Contents",
      heading: HeadingLevel.HEADING_1,
      spacing: { after: 400 }
    })
  ]

  chapters.forEach((chapter, index) => {
    tocItems.push(
      new Paragraph({
        text: `${index + 1}. ${chapter.title}`,
        spacing: { after: 100 },
        children: [
          new TextRun({
            text: `${index + 1}. ${chapter.title}`,
            size: 24
          })
        ]
      })
    )
  })

  tocItems.push(
    new Paragraph({
      text: "",
      pageBreakBefore: true
    })
  )

  return tocItems
}

const createQuestionSection = (question, questionNumber, options) => {
  const { includeAnswers, includeCode, includeFollowUp } = options
  const sections = []

  // Question header
  sections.push(
    new Paragraph({
      text: `Question ${questionNumber}`,
      heading: HeadingLevel.HEADING_2,
      spacing: { before: 400, after: 200 }
    })
  )

  // Difficulty and category
  sections.push(
    new Paragraph({
      children: [
        new TextRun({
          text: "Difficulty: ",
          bold: true
        }),
        new TextRun({
          text: question.difficulty.charAt(0).toUpperCase() + question.difficulty.slice(1),
          color: getDifficultyColor(question.difficulty)
        }),
        new TextRun({
          text: " | Category: ",
          bold: true
        }),
        new TextRun({
          text: question.category
        })
      ],
      spacing: { after: 200 }
    })
  )

  // Question text
  sections.push(
    new Paragraph({
      text: question.question,
      spacing: { after: 200 },
      children: [
        new TextRun({
          text: question.question,
          bold: true,
          size: 24
        })
      ]
    })
  )

  // Scenario
  if (question.scenario) {
    sections.push(
      new Paragraph({
        children: [
          new TextRun({
            text: "Real-World Scenario:",
            bold: true,
            underline: {}
          })
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: question.scenario,
        spacing: { after: 200 },
        children: [
          new TextRun({
            text: question.scenario,
            italics: true
          })
        ]
      })
    )
  }

  // Tags
  if (question.tags && question.tags.length > 0) {
    sections.push(
      new Paragraph({
        children: [
          new TextRun({
            text: "Tags: ",
            bold: true
          }),
          new TextRun({
            text: question.tags.join(", ")
          })
        ],
        spacing: { after: 200 }
      })
    )
  }

  // Answer
  if (includeAnswers && question.answer) {
    sections.push(
      new Paragraph({
        text: "Answer:",
        heading: HeadingLevel.HEADING_3,
        spacing: { before: 300, after: 200 }
      })
    )

    // Process answer text (simplified markdown parsing)
    const answerParagraphs = processAnswerText(question.answer, includeCode)
    sections.push(...answerParagraphs)
  }

  // Follow-up questions
  if (includeFollowUp && question.followUp && question.followUp.length > 0) {
    sections.push(
      new Paragraph({
        text: "Follow-up Questions:",
        heading: HeadingLevel.HEADING_3,
        spacing: { before: 300, after: 200 }
      })
    )

    question.followUp.forEach((followUpQ, index) => {
      sections.push(
        new Paragraph({
          text: `${index + 1}. ${followUpQ}`,
          spacing: { after: 100 }
        })
      )
    })
  }

  // Page break after each question
  sections.push(
    new Paragraph({
      text: "",
      spacing: { after: 400 }
    })
  )

  return sections
}

const processAnswerText = (answerText, includeCode) => {
  const paragraphs = []
  const lines = answerText.split('\n')
  let currentParagraph = []
  let inCodeBlock = false
  let codeLines = []

  for (const line of lines) {
    // Check for code block markers
    if (line.trim().startsWith('```')) {
      if (inCodeBlock) {
        // End of code block
        if (includeCode && codeLines.length > 0) {
          paragraphs.push(createCodeBlock(codeLines.join('\n')))
        }
        codeLines = []
        inCodeBlock = false
      } else {
        // Start of code block
        if (currentParagraph.length > 0) {
          paragraphs.push(new Paragraph({
            text: currentParagraph.join(' '),
            spacing: { after: 200 }
          }))
          currentParagraph = []
        }
        inCodeBlock = true
      }
      continue
    }

    if (inCodeBlock) {
      codeLines.push(line)
    } else {
      if (line.trim() === '') {
        if (currentParagraph.length > 0) {
          paragraphs.push(new Paragraph({
            text: currentParagraph.join(' '),
            spacing: { after: 200 }
          }))
          currentParagraph = []
        }
      } else {
        currentParagraph.push(line.trim())
      }
    }
  }

  // Add remaining paragraph
  if (currentParagraph.length > 0) {
    paragraphs.push(new Paragraph({
      text: currentParagraph.join(' '),
      spacing: { after: 200 }
    }))
  }

  return paragraphs
}

const createCodeBlock = (code) => {
  return new Paragraph({
    children: [
      new TextRun({
        text: code,
        font: "Courier New",
        size: 20
      })
    ],
    spacing: { before: 200, after: 200 }
  })
}

const getDifficultyColor = (difficulty) => {
  switch (difficulty) {
    case 'basic':
      return '22C55E' // Green
    case 'intermediate':
      return 'EAB308' // Yellow
    case 'expert':
      return 'DC2626' // Red
    default:
      return '000000' // Black
  }
}