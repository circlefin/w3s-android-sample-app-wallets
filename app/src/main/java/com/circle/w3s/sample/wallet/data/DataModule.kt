// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {
    @Provides
    @Singleton
    fun provideWalletSdkGateway(): WalletSdkGateway = DefaultWalletSdkGateway

    @Provides
    @Singleton
    fun provideWalletRepository(gateway: WalletSdkGateway): WalletRepository =
        WalletRepositoryImpl(gateway)
}
