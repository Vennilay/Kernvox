package com.vennilay.kernvox.ui.state

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

/**
 * Текст, основанный на ресурсах, передаваемый через ViewModels без жестко запрограммированных строк интерфейса.
 */
@Immutable
data class UiText(
    @param:StringRes val resId: Int,
    val args: List<Any> = emptyList(),
) {
    @Composable
    fun asString(): String = stringResource(resId, *args.toTypedArray())

    fun resolve(context: Context): String = context.getString(resId, *args.toTypedArray())

    companion object {
        fun resource(@StringRes resId: Int, vararg args: Any): UiText =
            UiText(resId = resId, args = args.toList())
    }
}
