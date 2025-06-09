package service.di

import data.market.MoexDataSource
import data.macro.MacroDataSource
import org.koin.dsl.module
import service.DcaService

val serviceModule =
    module {
        single {
            DcaService(
                fetchMarketData = { get<MoexDataSource>().fetchMarketData() },
                fetchMacroData = { date -> get<MacroDataSource>().fetchMacroData(date) },
            )
        }
    }
