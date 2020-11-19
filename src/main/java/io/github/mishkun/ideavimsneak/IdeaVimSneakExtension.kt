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
import com.intellij.openapi.editor.ScrollType
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.helper.StringHelper
import java.awt.event.KeyEvent

class IdeaVimSneakExtension : VimExtension {
    override fun getName(): String = "sneak"

    override fun init() {
        mapToFunctionAndProvideKeys("s", SneakHandler(Direction.FORWARD))
        mapToFunctionAndProvideKeys("S", SneakHandler(Direction.BACKWARD))

        // workaround to support ; and , commands
        mapToFunctionAndProvideKeys("f", SneakMemoryHandler("f"))
        mapToFunctionAndProvideKeys("F", SneakMemoryHandler("F"))
        mapToFunctionAndProvideKeys("t", SneakMemoryHandler("t"))
        mapToFunctionAndProvideKeys("T", SneakMemoryHandler("T"))

        mapToFunctionAndProvideKeys(";", SneakRepeatHandler(RepeatDirection.IDENTICAL))
        mapToFunctionAndProvideKeys(",", SneakRepeatHandler(RepeatDirection.REVERSE))
    }

    private class SneakHandler(private val direction: Direction) : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            val charone = getChar(editor) ?: return
            val chartwo = getChar(editor) ?: return
            jumpTo(editor, charone, chartwo, direction)
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

    private class SneakRepeatHandler(private val direction: RepeatDirection) : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            val lastSDirection = lastSDirection
            if (lastSDirection != null) {
                val (charone, chartwo) = lastSymbols.toList()
                jumpTo(editor, charone, chartwo, direction.map(lastSDirection))
            } else {
                VimExtensionFacade.executeNormalWithoutMapping(StringHelper.parseKeys(direction.symb), editor)
            }
        }
    }

    companion object {
        private var lastSDirection: Direction? = null
        private var lastSymbols: String = ""

        private fun jumpTo(editor: Editor, charone: Char, chartwo: Char, sneakDirection: Direction) {
            val caret = editor.caretModel.primaryCaret
            val position = caret.offset
            val chars = editor.document.charsSequence
            val foundPosition = sneakDirection.findBiChar(chars, position, charone, chartwo)
            foundPosition?.let(editor.caretModel::moveToOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    private enum class Direction(val offset: Int) {
        FORWARD(1) {
            override fun findBiChar(charSequence: CharSequence, position: Int, charone: Char, chartwo: Char): Int? {
                for (i in (position + offset) until charSequence.length - 1) {
                    if (charSequence[i] == charone &&
                            charSequence[i + 1] == chartwo) {
                        return i
                    }
                }
                return null
            }
        },
        BACKWARD(-1) {
            override fun findBiChar(charSequence: CharSequence, position: Int, charone: Char, chartwo: Char): Int? {
                for (i in (position + offset) downTo 0) {
                    if (charSequence[i] == charone &&
                            charSequence[i + 1] == chartwo) {
                        return i
                    }
                }
                return null
            }

        };
        abstract fun findBiChar(charSequence: CharSequence, position: Int, charone: Char, chartwo: Char): Int?
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
}
