/*
    IdeaVim-Sneak plugin for IdeaVim mimicking vim-sneak plugin
    Copyright (C) 2020 Mikhail Levchenko

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

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.helper.EditorHelper
import java.awt.event.KeyEvent

class IdeaVimSneakExtension : VimExtension {
    override fun getName(): String = "sneak"

    override fun init() {
        mapToFunctionAndProvideKeys("s", SneakHandler(Direction.FORWARD))
        mapToFunctionAndProvideKeys("S", SneakHandler(Direction.BACKWARD))
    }

    private class SneakHandler(private val direction: Direction) : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            val charone = getChar(editor)
            val chartwo = getChar(editor)
            val caret = editor.caretModel.primaryCaret
            val line: Int = caret.logicalPosition.line
            val position = caret.logicalPosition.column
            val start = EditorHelper.getLineStartOffset(editor, line)
            val end = EditorHelper.getLineEndOffset(editor, line, true)
            val chars = editor.document.charsSequence
            val foundPosition = direction.findBiChar(chars, start, end, position, charone, chartwo)
            foundPosition?.let(editor.caretModel::moveToOffset)
        }

        private fun CharSequence.findBiChar(start: Int, end: Int, position: Int, charone: Char, chartwo: Char): Int? {
            for (i in (start + position) until end) {
                if (this[i] == charone && this[i + 1] == chartwo) {
                    return i
                }
            }
            return null
        }

        private fun CharSequence.findBackwardChar(start: Int, end: Int, position: Int, charone: Char, chartwo: Char): Int? {
            for (i in (start + position) downTo start) {
                if (this[i] == charone && this[i + 1] == chartwo) {
                    return i
                }
            }
            return null
        }


        private fun getChar(editor: Editor): Char {
            val key = VimExtensionFacade.inputKeyStroke(editor)
            val keyChar = key.keyChar
            return if (keyChar == KeyEvent.CHAR_UNDEFINED || keyChar.toInt() == KeyEvent.VK_ESCAPE) {
                0.toChar()
            } else keyChar
        }
    }

    private class SneakRepeatHandler(private val direction: RepeatDirection) : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {

        }
    }

    private enum class Direction {
        FORWARD {
            override fun findBiChar(charSequence: CharSequence, start: Int, end: Int, position: Int, charone: Char, chartwo: Char): Int? {
                for (i in (start + position) until end) {
                    if (charSequence[i] == charone && charSequence[i + 1] == chartwo) {
                        return i
                    }
                }
                return null
            }
        },
        BACKWARD {
            override fun findBiChar(charSequence: CharSequence, start: Int, end: Int, position: Int, charone: Char, chartwo: Char): Int? {
                for (i in (start + position) downTo start) {
                    if (charSequence[i] == charone && charSequence[i + 1] == chartwo) {
                        return i
                    }
                }
                return null
            }

        };
        abstract fun findBiChar(charSequence: CharSequence, start: Int, end: Int, position: Int, charone: Char, chartwo: Char): Int?
    }

    private enum class RepeatDirection {
        IDENTICAL {
            override fun map(direction: Direction): Direction = direction
        },
        REVERSE {
            override fun map(direction: Direction): Direction = when(direction) {
                Direction.FORWARD -> Direction.BACKWARD
                Direction.BACKWARD -> Direction.FORWARD
            }
        };
        abstract fun map(direction: Direction): Direction
    }
}
