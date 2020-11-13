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
package io.mishkun.github.ideavimsneak

import com.maddyhome.idea.vim.command.CommandState

class IdeaVimSneakTest : VimTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        enableExtensions("sneak")
    }

    fun testSneakForward() {
        val before = "som${c}e text"
        val after = "some te${c}xt"

        doTest("sxt", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakForwardVertical() {
        val before = """som${c}e text
        another line
        third line"""
        val after = """some text
        another line
        thi${c}rd line"""

        doTest("srd", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakForwardDoNotIgnoreCase() {
        val before = "som${c}e teXt"
        val after = "som${c}e teXt"

        doTest("sxt", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakForwardAndFindAgain() {
        val before = "som${c}e text text"
        val after = "some text te${c}xt"

        doTest("sxt;", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakForwardAndFindReverseAgain() {
        val before = "some tex${c}t text"
        val after = "some ${c}text text"

        doTest("ste,", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakBackward() {
        val before = "some tex${c}t"
        val after = "so${c}me text"

        doTest("Sme", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakBackwardVertical() {
        val before = """some text
        another line
        thi${c}rd line"""
        val after = """so${c}me text
        another line
        third line"""

        doTest("Sme", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakBackwardAndFindAgain() {
        // caret has to be before another character (space here)
        val before = "some text text${c} "
        val after = "some ${c}text text "

        doTest("Ste;", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testSneakBackwardAndFindReverseAgain() {
        val before = "some tex${c}t text"
        val after = "some text ${c}text"

        doTest("Ste,", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testEndOfFile() {
        val before = """first line
        some te${c}xt
        another line
        last line."""
        val after = """first line
        some text
        another line
        last lin${c}e."""

        doTest("se.", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }

    fun testStartOfFile() {
        val before = """first line
        some text
        another${c} line
        last line."""
        val after = """${c}first line
        some text
        another line
        last line."""

        doTest("Sfi", before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
    }
}
