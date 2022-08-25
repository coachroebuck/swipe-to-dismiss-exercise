package com.coachroebuck.mytranslationnotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.ExperimentalUnitApi

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalUnitApi
interface MainNavigator {
    fun <T> navigate(
        applicationContext: Context,
        clazz: Class<T>,
        flags: Int = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK,
        bundle: Bundle? = null
    ) {
        val intent = Intent(applicationContext, clazz)
        intent.flags = flags
        bundle?.let { intent.putExtras(it) }
        applicationContext.startActivity(intent)
    }

    fun onFeatureSelected(context: Context, type: ScreenType)
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalUnitApi
class DefaultMainNavigator: MainNavigator {
    override fun onFeatureSelected(context: Context, type: ScreenType) {
        navigate(context = context, clazz = type.clazz)
    }

    private fun <T> navigate(
        context: Context,
        clazz: Class<T>,
    ) {
        val bundle = Bundle()
        navigate(
            applicationContext = context,
            clazz = clazz,
            bundle = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        )
    }
}