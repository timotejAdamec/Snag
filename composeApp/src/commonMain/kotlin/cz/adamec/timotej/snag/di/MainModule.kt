package cz.adamec.timotej.snag.di

import cz.adamec.timotej.snag.vm.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val mainModule =
    module {
        viewModelOf(::MainViewModel)
    }
