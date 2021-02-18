/*
    IdeaVim-Sneak plugin for IdeaVim mimicking vim-sneak plugin
    Copyright (C) 2020 Mikhail Levchenko
    Copyright (C) IdeaVim Authors

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.mishkun.ideavimsneak

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.Disposer
import com.maddyhome.idea.vim.VimProjectService
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.extension.highlightedyank.DEFAULT_HIGHLIGHT_DURATION
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.option.StrictMode
import com.maddyhome.idea.vim.option.OptionsManager
import java.awt.Font
import java.awt.event.KeyEvent
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val DEFAULT_HIGHLIGHT_DURATION: Long = 300
class IdeaVimSneakExtension : VimExtension {
    override fun getName(): String = "sneak"

    override fun init() {
        val highlightHandler = HighlightHandler()
        mapToFunctionAndProvideKeys("s", SneakHandler(highlightHandler, Direction.FORWARD))
        mapToFunctionAndProvideKeys("S", SneakHandler(highlightHandler, Direction.BACKWARD))

        // workaround to support ; and , commands
        mapToFunctionAndProvideKeys("f", SneakMemoryHandler("f"))
        mapToFunctionAndProvideKeys("F", SneakMemoryHandler("F"))
        mapToFunctionAndProvideKeys("t", SneakMemoryHandler("t"))
        mapToFunctionAndProvideKeys("T", SneakMemoryHandler("T"))

        mapToFunctionAndProvideKeys(";", SneakRepeatHandler(highlightHandler, RepeatDirection.IDENTICAL))
        mapToFunctionAndProvideKeys(",", SneakRepeatHandler(highlightHandler, RepeatDirection.REVERSE))
    }

    private class SneakHandler(
        private val highlightHandler: HighlightHandler,
        private val direction: Direction
    ) : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            val charone = getChar(editor) ?: return
            val chartwo = getChar(editor) ?: return
            val range = jumpTo(editor, charone, chartwo, direction)
            range?.let { highlightHandler.highlightSneakRange(editor, range) }
            lastSymbols = "${charone}${chartwo}"
            lastSDirection = direction
        }

        private fun getChar(editor: Editor): Char? {
            val key = VimExtensionFacade.inputKeyStroke(editor)
            return when {
                key.keyChar == KeyEvent.CHAR_UNDEFINED || key.keyCode == KeyEvent.VK_ESCAPE -> null
                else -> key.keyChar
            }
        }
    }

    /**
     * This class acts as proxy for normal find commands because we need to update [lastSDirection]
     */
    private class SneakMemoryHandler(private val char: String): VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            lastSDirection = null
            VimExtensionFacade.executeNormalWithoutMapping(StringHelper.parseKeys(char), editor)
        }
    }

    private class SneakRepeatHandler(
        private val highlightHandler: HighlightHandler,
        private val direction: RepeatDirection
    ) : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            val lastSDirection = lastSDirection
            if (lastSDirection != null) {
                val (charone, chartwo) = lastSymbols.toList()
                val jumpRange = jumpTo(editor, charone, chartwo, direction.map(lastSDirection))
                jumpRange?.let { highlightHandler.highlightSneakRange(editor, jumpRange) }
            } else {
                VimExtensionFacade.executeNormalWithoutMapping(StringHelper.parseKeys(direction.symb), editor)
            }
        }
    }

    companion object {
        private var lastSDirection: Direction? = null
        private var lastSymbols: String = ""

        private fun jumpTo(editor: Editor, charone: Char, chartwo: Char, sneakDirection: Direction): TextRange? {
            val caret = editor.caretModel.primaryCaret
            val position = caret.offset
            val chars = editor.document.charsSequence
            val foundPosition = sneakDirection.findBiChar(chars, position, charone, chartwo)
            foundPosition?.let(editor.caretModel::moveToOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            return foundPosition?.let { TextRange(foundPosition, foundPosition + 2) }
        }
    }

    private enum class Direction(val offset: Int) {
        FORWARD(1) {
            override fun findBiChar(charSequence: CharSequence, position: Int, charone: Char, chartwo: Char): Int? {
                for (i in (position + offset) until charSequence.length - 1) {
                    if (matches(charSequence, i, charone, chartwo)) {
                        return i
                    }
                }
                return null
            }
        },
        BACKWARD(-1) {
            override fun findBiChar(charSequence: CharSequence, position: Int, charone: Char, chartwo: Char): Int? {
                for (i in (position + offset) downTo 0) {
                    if (matches(charSequence, i, charone, chartwo)) {
                        return i
                    }
                }
                return null
            }

        };
        abstract fun findBiChar(charSequence: CharSequence, position: Int, charone: Char, chartwo: Char): Int?

        fun matches(charSequence: CharSequence, charPosition: Int, charOne: Char, charTwo: Char): Boolean {
            var match = charSequence[charPosition].equals(charOne, ignoreCase = OptionsManager.ignorecase.isSet) &&
                    charSequence[charPosition + 1].equals(charTwo, ignoreCase = OptionsManager.ignorecase.isSet)

            if (OptionsManager.ignorecase.isSet && OptionsManager.smartcase.isSet) {
                if (charOne.isUpperCase() || charTwo.isUpperCase()) {
                    match = charSequence[charPosition].equals(charOne, ignoreCase = false) &&
                            charSequence[charPosition + 1].equals(charTwo, ignoreCase = false)
                }
            }
            return match
        }
    }

    private enum class RepeatDirection(val symb: String) {
        IDENTICAL(";") {
            override fun map(direction: Direction): Direction = direction
        },
        REVERSE(",") {
            override fun map(direction: Direction): Direction = when(direction) {
                Direction.FORWARD -> Direction.BACKWARD
                Direction.BACKWARD -> Direction.FORWARD
            }
        };
        abstract fun map(direction: Direction): Direction
    }

    private class HighlightHandler {
        private var editor: Editor? = null
        private val sneakHighlighters: MutableSet<RangeHighlighter> = mutableSetOf()

        fun highlightSneakRange(editor: Editor, range: TextRange) {
            clearAllSneakHighlighters()

            this.editor = editor
            val project = editor.project
            if (project != null) {
                Disposer.register(VimProjectService.getInstance(project)) {
                    this.editor = null
                    sneakHighlighters.clear()
                }
            }

            if (range.isMultiple) {
                for (i in 0 until range.size()) {
                    highlightSingleRange(editor, range.startOffsets[i]..range.endOffsets[i])
                }
            } else {
                highlightSingleRange(editor, range.startOffset..range.endOffset)
            }
        }

        fun clearAllSneakHighlighters() {
            sneakHighlighters.forEach { highlighter ->
                editor?.markupModel?.removeHighlighter(highlighter) ?: StrictMode.fail("Highlighters without an editor")
            }

            sneakHighlighters.clear()
        }

        private fun highlightSingleRange(editor: Editor, range: ClosedRange<Int>) {
            val highlighter = editor.markupModel.addRangeHighlighter(
                range.start,
                range.endInclusive,
                HighlighterLayer.SELECTION,
                getHighlightTextAttributes(),
                HighlighterTargetArea.EXACT_RANGE
            )

            sneakHighlighters.add(highlighter)

            setClearHighlightRangeTimer(highlighter)
        }

        private fun setClearHighlightRangeTimer(highlighter: RangeHighlighter) {
            Executors.newSingleThreadScheduledExecutor().schedule({
                ApplicationManager.getApplication().invokeLater {
                    editor?.markupModel?.removeHighlighter(highlighter) ?: StrictMode.fail("Highlighters without an editor")
                }
            }, DEFAULT_HIGHLIGHT_DURATION, TimeUnit.MILLISECONDS)
        }

        private fun getHighlightTextAttributes() = TextAttributes(
            null,
            EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES.defaultAttributes.backgroundColor,
            editor?.colorsScheme?.getColor(EditorColors.CARET_COLOR),
            EffectType.SEARCH_MATCH,
            Font.PLAIN
        )
    }
}
