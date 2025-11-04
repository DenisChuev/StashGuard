package dc.stashguard

import android.app.Application
import dc.stashguard.di.initKoin
import org.koin.android.ext.koin.androidContext

class StashGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(
            appDeclaration = { androidContext(this@StashGuardApp) },
        )
    }
}