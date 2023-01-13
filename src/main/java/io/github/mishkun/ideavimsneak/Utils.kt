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

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.helper.StringHelper

/**
 * Map some <Plug>(keys) command to given handler
 *  and create mapping to <Plug>(prefix)[keys]
 */
fun VimExtension.mapToFunctionAndProvideKeys(keys: String, handler: VimExtensionHandler) {
    VimExtensionFacade.putExtensionHandlerMapping(
        MappingMode.NXO,
        StringHelper.parseKeys(command(keys)),
        owner,
        handler,
        false
    )
    VimExtensionFacade.putKeyMapping(
        MappingMode.NXO,
        StringHelper.parseKeys(keys),
        owner,
        StringHelper.parseKeys(command(keys)),
        true
    )
}

private fun command(keys: String) = "<Plug>(sneak-$keys)"
