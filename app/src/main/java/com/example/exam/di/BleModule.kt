package com.example.exam.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.Nullable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.exam.core.bluetooth.BleGattClient
import com.example.exam.core.bluetooth.BleManager
import com.example.exam.core.bluetooth.BleScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    @Nullable

    fun provideBluetoothAdapter(
        @ApplicationContext context: Context
    ): BluetoothAdapter? {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

        return bluetoothManager?.adapter
    }

    @Provides
    @Singleton
    fun provideBleScanner(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter?,
        applicationScope: CoroutineScope
    ): BleScanner {
        return BleScanner(
            btAdapter = bluetoothAdapter,
            scope = applicationScope,
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideBleGattClient(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter?,
        applicationScope: CoroutineScope
    ): BleGattClient {
        return BleGattClient(
            btAdapter = bluetoothAdapter,
            scope = applicationScope,
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideBleManager(
        scanner: BleScanner,
        gattClient: BleGattClient,
        applicationScope: CoroutineScope
    ): BleManager {
        return BleManager(
            scanner = scanner,
            gattClient = gattClient,
            scope = applicationScope
        )
    }
}