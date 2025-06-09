package service.di

import data.market.MoexDataSource
import org.koin.dsl.module
import service.DcaService

val serviceModule =
    module {
        single { DcaService { get<MoexDataSource>().fetchMarketData() } }
    }
